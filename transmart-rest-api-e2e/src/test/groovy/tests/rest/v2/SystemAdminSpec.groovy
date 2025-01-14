/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package tests.rest.v2

import base.RESTSpec
import base.RestHelper
import org.transmartproject.core.multidimquery.ErrorResponse
import tests.rest.constraints

import static base.ContentTypeFor.JSON
import static config.Config.*

class SystemAdminSpec extends RESTSpec {

    def "clear cache as admin"() {
        when: "I try to clear the tree node cache as admin"
        def responseData = get([
                path      : PATH_SYSTEM_CLEAR_CACHE,
                acceptType: JSON,
                user      : ADMIN_USER,
        ])
        then: "an empty body is returned"
        assert responseData == null
    }

    def "clear cache"() {
        when: "I try to clear the tree node cache as a regular user"
        def request = [
                path      : PATH_SYSTEM_CLEAR_CACHE,
                acceptType: JSON,
                statusCode: 403,
                user: DEFAULT_USER
        ]
        def responseData = RestHelper.toObject get(request), ErrorResponse

        then: "I do not have an access"
        responseData.status == 403
        responseData.error == 'Forbidden'
        responseData.message == 'Access is denied'
    }

    def "after data loading call cache counts pers study and concept for admin user"() {
        given:
        def constraint = [constraint: [type: constraints.TrueConstraint]]
        def user = ADMIN_USER

        when:
        afterDataLoadingCallSucceeds()
        def countsPerStudyNConceptData = countsPerStudyAndConceptForTrueConstraint(user, constraint)

        then: 'counts have been returned'
        countsPerStudyNConceptData.countsPerStudy
    }

    private Map countsPerStudyAndConceptForTrueConstraint(user, constraint) {
        def response = get([
                path      : PATH_COUNTS_PER_STUDY_AND_CONCEPT,
                acceptType: JSON,
                user      : user,
                query     : constraint,
                statusCode: 200
        ])
        RestHelper.toObject(response, Map)
    }

    private boolean afterDataLoadingCallSucceeds() {
        def updateResponse = get([
                path      : AFTER_DATA_LOADING_UPDATE,
                acceptType: JSON,
                user      : ADMIN_USER,
                statusCode: 200
        ])
        int times = 0
        while (!['FAILED', 'COMPLETED'].contains(updateResponse.status) && ++times < 1000) {
            updateResponse = get([
                    path      : UPDATE_STATUS,
                    acceptType: JSON,
                    user      : ADMIN_USER,
                    statusCode: 200
            ])
            Thread.sleep(500)
        }
        assert updateResponse.status == 'COMPLETED': "Update has failed: ${updateResponse.message}"
    }
}
