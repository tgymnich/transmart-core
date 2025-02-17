/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package tests.rest.v1

import annotations.RequiresStudy
import annotations.RequiresV1ApiSupport
import base.RESTSpec

import static base.ContentTypeFor.JSON
import static base.ContentTypeFor.XML
import static config.Config.*

@RequiresV1ApiSupport(true)
@RequiresStudy(EHR_ID)
class PatientSetsSpec extends RESTSpec {

    /**
     *  given: "study EHR is loaded"
     *  when: "I create a patient set"
     *  then: "a set is created and returned"
     */
    def "v1 post patient set"() {
        given: "study EHR is loaded"
        def body = '<ns4:query_definition xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/"><specificity_scale>0</specificity_scale><panel><panel_number></panel_number><invert>0</invert><total_item_occurrences>1</total_item_occurrences><item><item_name>Heart Rate</item_name><item_key>\\\\Vital Signs\\Vital Signs\\Heart Rate\\</item_key><tooltip>\\Vital Signs\\Heart Rate\\</tooltip><hlevel>1</hlevel><class>ENC</class></item><panel_timing>ANY</panel_timing></panel><panel><panel_number>2</panel_number><invert>0</invert><total_item_occurrences>1</total_item_occurrences><item><item_name>Age</item_name><item_key>\\\\Public Studies\\Public Studies\\SHARED_CONCEPTS_STUDY_A\\Demography\\Age\\</item_key><tooltip>\\Public Studies\\SHARED_CONCEPTS_STUDY_A\\Demography\\Age\\</tooltip><hlevel>3</hlevel><class>ENC</class><constrain_by_value><value_operator>LT</value_operator><value_constraint>70</value_constraint><value_unit_of_measure>ratio</value_unit_of_measure><value_type>NUMBER</value_type></constrain_by_value></item><panel_timing>ANY</panel_timing></panel><query_timing>ANY</query_timing></ns4:query_definition>'

        when: "I create a patient set"
        def responseData = post([
                path       : V1_PATH_PATIENT_SETS,
                contentType: XML,
                body       : body,
                statusCode : 201,
                user       : ADMIN_USER
        ])

        then: "a set is created and returned"
        assert responseData.id != null
        assert responseData.patients.size() == 2
        responseData.patients.collect { it.subjectIds['SUBJ_ID'] } as Set == ['SCSA:67', 'SCSA:57'] as Set
    }

    /**
     *  given: "study EHR is loaded"
     *  when: "I get a patient set by id"
     *  then: "a set is returned"
     */
    def "v1 get patient set"() {
        given: "study EHR is loaded"
        def body = '<ns4:query_definition xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/"><specificity_scale>0</specificity_scale><panel><panel_number></panel_number><invert>0</invert><total_item_occurrences>1</total_item_occurrences><item><item_name>Heart Rate</item_name><item_key>\\\\Vital Signs\\Vital Signs\\Heart Rate\\</item_key><tooltip>\\Vital Signs\\Heart Rate\\</tooltip><hlevel>1</hlevel><class>ENC</class></item><panel_timing>ANY</panel_timing></panel><panel><panel_number>2</panel_number><invert>0</invert><total_item_occurrences>1</total_item_occurrences><item><item_name>Age</item_name><item_key>\\\\Public Studies\\Public Studies\\SHARED_CONCEPTS_STUDY_A\\Demography\\Age\\</item_key><tooltip>\\Public Studies\\SHARED_CONCEPTS_STUDY_A\\Demography\\Age\\</tooltip><hlevel>3</hlevel><class>ENC</class><constrain_by_value><value_operator>LT</value_operator><value_constraint>70</value_constraint><value_unit_of_measure>ratio</value_unit_of_measure><value_type>NUMBER</value_type></constrain_by_value></item><panel_timing>ANY</panel_timing></panel><query_timing>ANY</query_timing></ns4:query_definition>'
        def id = post([
                path       : V1_PATH_PATIENT_SETS,
                contentType: XML,
                body       : body,
                statusCode : 201,
                user       : ADMIN_USER
        ]).id

        when: "I get a patient set by id"
        def responseData = get([
                path      : V1_PATH_PATIENT_SETS + "/${id}",
                acceptType: JSON,
                user      : ADMIN_USER
        ])

        then: "a set is returned"
        assert responseData.id == id
        assert responseData.patients.size() == 2
        responseData.patients.collect { it.subjectIds['SUBJ_ID'] } as Set == ['SCSA:67', 'SCSA:57'] as Set
    }

}
