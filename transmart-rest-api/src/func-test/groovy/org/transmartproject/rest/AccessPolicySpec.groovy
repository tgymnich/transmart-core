package org.transmartproject.rest

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.transmartproject.core.multidimquery.Counts
import org.transmartproject.core.multidimquery.query.Constraint
import org.transmartproject.core.multidimquery.query.StudyNameConstraint
import org.transmartproject.core.multidimquery.query.TrueConstraint
import org.transmartproject.mock.MockUser
import org.transmartproject.rest.marshallers.MarshallerSpec
import spock.lang.Shared
import spock.lang.Unroll

import java.time.Instant

import static org.transmartproject.core.users.PatientDataAccessLevel.*
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.FORBIDDEN


@Slf4j
class AccessPolicySpec extends MarshallerSpec {

    @CompileStatic
    @Canonical
    class RequestBody {
        Constraint constraint
    }

    @Shared
    MockUser admin = new MockUser('admin', true)
    @Shared
    MockUser study1User = new MockUser('study1User', [study1: MEASUREMENTS])
    @Shared
    MockUser thresholdUser = new MockUser('thresholdUser', [study1: COUNTS_WITH_THRESHOLD])
    @Shared
    MockUser study2User = new MockUser('study2User', [study2: SUMMARY])
    @Shared
    MockUser study1And2User = new MockUser('study1And2User', [study1: SUMMARY, study2: SUMMARY])

    List<MockUser> getAllUsers() {
        [admin, thresholdUser, study1User, study2User, study1And2User]
    }

    @Shared
    Constraint trueConstraint = new TrueConstraint()
    @Shared
    Constraint study1Constraint = new StudyNameConstraint('study1')

    void setupData() {
        testResource.clearTestData()

        // Create studies
        def publicStudy = testResource.createTestStudy('publicStudy', true, null)
        def study1 = testResource.createTestStudy('study1', false, null)
        def study2 = testResource.createTestStudy('study2', false, null)

        // Create concepts
        def concept1 = testResource.createTestConcept('categorical_concept1')
        def concept2 = testResource.createTestConcept('numerical_concept2')

        // Create patients
        def patient1 = testResource.createTestPatient('Subject 1')
        def patient2 = testResource.createTestPatient('Subject 2')
        def patient3 = testResource.createTestPatient('Subject 3')
        def patient4 = testResource.createTestPatient('Subject from public study')

        // Create observations
        // public study: 1 subject
        // study 1: 2 subjects (1 shared with study 2)
        // study 2: 2 subjects (1 shared with study 1)
        // total: 4 subjects
        Date dummyDate = Date.from(Instant.parse('2001-02-03T13:18:54Z'))
        testResource.createTestCategoricalObservations(patient1, concept1, study1[0], [['@': 'value1']], dummyDate)
        testResource.createTestNumericalObservations(patient1, concept2, study1[0], [['@': 100], ['@': 200]], dummyDate)
        testResource.createTestCategoricalObservations(patient2, concept1, study1[0], [['@': 'value2'], ['@': 'value3']], dummyDate)
        testResource.createTestNumericalObservations(patient2, concept2, study1[0], [['@': 300]], dummyDate)
        testResource.createTestNumericalObservations(patient2, concept2, study2[0], [['@': 400]], dummyDate)
        testResource.createTestCategoricalObservations(patient3, concept1, study2[0], [['@': 'value4']], dummyDate)
        testResource.createTestCategoricalObservations(patient4, concept1, publicStudy[0], [['@': 'value1']], dummyDate)
    }

    @Unroll
    void 'test if mock users can access the counts that they have access to'(
            Constraint constraint, MockUser user, HttpStatus expectedStatus, Long patientCount) {
        given:
        setupData()
        def url = "${baseURL}/v2/observations/counts"

        expect:
        selectUser(user)
        def body = new RequestBody(constraint)

        def response = postJson(url, body)
        checkResponseStatus(response, expectedStatus, user)
        switch (expectedStatus) {
            case OK:
                def counts = toObject(response, Counts)
                assert counts
                assert counts.patientCount == patientCount:
                    "Unexpected patient count ${counts.patientCount} for user ${user.username}, " +
                            "constraint ${constraint.toJson()}. Expected ${patientCount}."
                break
            case FORBIDDEN:
                def error = toObject(response, Map)
                assert (error.message as String).startsWith('Access denied to study or study does not exist')
                break
            default:
                throw new IllegalArgumentException("Unexpected status: ${expectedStatus.value()}")
        }

        where:
        constraint          | user              | expectedStatus    | patientCount
        trueConstraint      | admin             | OK                | 4
        trueConstraint      | study1User        | OK                | 3
        trueConstraint      | thresholdUser     | OK                | 1
        trueConstraint      | study2User        | OK                | 3
        trueConstraint      | study1And2User    | OK                | 4
        study1Constraint    | admin             | OK                | 2
        study1Constraint    | study1User        | OK                | 2
        study1Constraint    | thresholdUser     | FORBIDDEN         | null
        study1Constraint    | study2User        | FORBIDDEN         | null
        study1Constraint    | study1And2User    | OK                | 2
    }

}
