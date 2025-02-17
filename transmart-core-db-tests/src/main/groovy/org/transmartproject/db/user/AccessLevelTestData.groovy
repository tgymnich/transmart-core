/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.user

import org.transmartproject.db.accesscontrol.AccessLevelCoreDb
import org.transmartproject.db.accesscontrol.SecuredObject
import org.transmartproject.db.accesscontrol.SecuredObjectAccess
import org.transmartproject.db.i2b2data.Study
import org.transmartproject.db.ontology.ConceptTestData
import org.transmartproject.db.ontology.I2b2
import org.transmartproject.db.ontology.I2b2Secure

import static org.transmartproject.db.dataquery.highdim.HighDimTestData.save
import static org.transmartproject.db.ontology.ConceptTestData.createI2b2Secure

class AccessLevelTestData {

    public static final String EVERYONE_GROUP_NAME = 'EVERYONE_GROUP'

    /**
     * The public study (has token EXP:PUBLIC)
     */
    public static final String STUDY1 = 'STUDY_ID_1'

    /**
     * The private study (has token EXP:STUDY_ID_2)
     */
    public static final String STUDY2 = 'STUDY_ID_2'

    /**
     * Private study (token EXP:STUDY_ID_3), but EVERYONE_GROUP has permissions here
     */
    public static final String STUDY3 = 'STUDY_ID_3'

    public static final String STUDY2_SECURE_TOKEN = 'EXP:STUDY_ID_2'
    public static final String STUDY3_SECURE_TOKEN = 'EXP:STUDY_ID_3'

    static AccessLevelTestData createDefault() {
        def result = new AccessLevelTestData()
        result.conceptTestData = ConceptTestData.createDefault()
        result
    }

    /*
     * The alternative concept data still has hard requirements;
     * the study names should be a subset of STUDY_ID_{1,2,3}
     */

    static AccessLevelTestData createWithAlternativeConceptData(
            ConceptTestData conceptTestData) {
        def result = new AccessLevelTestData()
        result.conceptTestData = conceptTestData
        result
    }

    ConceptTestData conceptTestData

    @Lazy
    /* private */ List<String> studies = {
        conceptTestData.i2b2List*.cComment.collect {
            def split = it?.split(':') as List
            split ? split[1] : null
        }.findAll()
    }()

    @Lazy
    List<I2b2Secure> i2b2Secures = {
        conceptTestData.i2b2List.collect { I2b2 i2b2 ->
            def i2b2sec = createI2b2Secure(
                    i2b2.metaClass.properties.findAll {
                        it.name in ['level', 'fullName', 'name', 'cComment']
                    }.collectEntries {
                        [it.name, it.getProperty(i2b2)]
                    })
            if (i2b2sec.fullName.contains('study1') || i2b2.cComment == null) {
                i2b2sec.secureObjectToken = 'EXP:PUBLIC'
            } else {
                i2b2sec.secureObjectToken = i2b2.cComment.replace('trial', 'EXP')
            }
            i2b2sec.dimensionTableName = 'concept_dimension'
            i2b2sec.columnName = 'concept_path'
            i2b2sec.operator = 'like'
            i2b2sec.dimensionCode = i2b2sec.fullName
            i2b2sec
        }
    }()

    @Lazy
    List<SecuredObject> securedObjects = {
        Set<String> tokens = i2b2Secures*.secureObjectToken as Set
        tokens -= 'EXP:PUBLIC'

        long id = -500L
        tokens.collect { token ->
            def secObj = new SecuredObject()
            secObj.dataType = 'BIO_CLINICAL_TRIAL'
            secObj.bioDataUniqueId = token
            secObj.id = --id
            secObj
        }
    }()

    @Lazy
    List<Study> dimensionStudies = {
        i2b2Secures.findAll { it.level == 1 }.collect {
            new Study(
                    studyId: it.name,
                    secureObjectToken: it.secureObjectToken == 'EXP:PUBLIC' ? Study.PUBLIC : it.secureObjectToken
            )
        }
    }()

    List<AccessLevelCoreDb> accessLevels = {
        long id = -600L
        [
                [name: 'MEASUREMENTS', value: 8],
                [name: 'SUMMARY', value: 5],
                [name: 'COUNTS_WITH_THRESHOLD', value: 1],
        ].collect {
            def accessLevel = new AccessLevelCoreDb()
            it.each { k, v ->
                accessLevel."$k" = v
            }
            accessLevel.id = --id
            accessLevel
        }
    }()

    List<RoleCoreDb> roles = {
        long id = -100L
        [
                [authority: 'ROLE_ADMIN', description: 'admin user'],
                [authority: 'ROLE_STUDY_OWNER', description: 'study owner'],
                [authority: 'ROLE_SPECTATOR', description: 'spectator user'],
                [authority: 'ROLE_DATASET_EXPLORER_ADMIN', description: 'dataset Explorer admin users - can view all trials'],
                [authority: 'ROLE_PUBLIC_USER', description: 'public user'],
        ].collect {
            def role = new RoleCoreDb()
            it.each { k, v ->
                role."$k" = v
            }
            role.id = --id
            role
        }
    }()

    static List<User> createUsers(int count, long baseId) {
        (1..count).collect {
            long id = baseId - it
            String username = "user_$id"
            def ret = new User()
            ret.username = username
            ret.uniqueId = username
            ret.enabled = true
            ret.id = id
            ret
        }
    }

    static List<Group> createGroups(int count, long baseId) {
        (1..count).collect {
            long id = baseId - it
            def name = "group_$id"
            def ret = new Group()
            ret.category = name
            ret.uniqueId = name
            ret.enabled = true
            ret.id = id
            ret
        }
    }

    List<Group> groups = {
        def ret = []
        def everyoneGroup = new Group(
                category: EVERYONE_GROUP_NAME,
                uniqueId: EVERYONE_GROUP_NAME)
        everyoneGroup.id = -1

        ret << everyoneGroup
        ret.addAll(
                createGroups(2, -200L))
        ret
    }()

    List<User> users = {
        createUsers(6, -300L)
    }()

    static SecuredObjectAccess createSecuredObjectAccess(Map properties) {
        def result = new SecuredObjectAccess()
        properties.each { k, v ->
            result."$k" = v
        }
        result
    }

    /* 1 first user is admin
     * 2 second user is in group test_-201, which has access to study 2
     * 3 third user has direct access to study 2
     * 4 fourth user has no access to study 2
     * 5 fifth user has only SUMMARY permissions on study 2
     * 6 sixth user has both SUMMARY and MEASUREMENTS permissions on study2 (this
     *   probably can't happen in transmart anyway).
     * 7 EVERYONE_GROUP has access to study 3
     */
    @Lazy
    List<SecuredObjectAccess> securedObjectAccesses = {
        List<SecuredObjectAccess> ret = []
        if (STUDY2 in studies) {
            ret += [
                    createSecuredObjectAccess( // 2
                            principal: groups.find { it.category == 'group_-201' },
                            securedObject: securedObjects.find { it.bioDataUniqueId == STUDY2_SECURE_TOKEN },
                            accessLevel: accessLevels.find { it.name == 'MEASUREMENTS' }),
                    createSecuredObjectAccess( // 3
                            principal: users[2],
                            securedObject: securedObjects.find { it.bioDataUniqueId == STUDY2_SECURE_TOKEN },
                            accessLevel: accessLevels.find { it.name == 'COUNTS_WITH_THRESHOLD' }),
                    createSecuredObjectAccess( // 5
                            principal: users[4],
                            securedObject: securedObjects.find { it.bioDataUniqueId == STUDY2_SECURE_TOKEN },
                            accessLevel: accessLevels.find { it.name == 'SUMMARY' }),
                    createSecuredObjectAccess( // 6 (1)
                            principal: users[5],
                            securedObject: securedObjects.find { it.bioDataUniqueId == STUDY2_SECURE_TOKEN },
                            accessLevel: accessLevels.find { it.name == 'SUMMARY' }),
                    createSecuredObjectAccess( // 6 (2)
                            principal: users[5],
                            securedObject: securedObjects.find { it.bioDataUniqueId == STUDY2_SECURE_TOKEN },
                            accessLevel: accessLevels.find { it.name == 'MEASUREMENTS' }),
            ]
        }
        if (STUDY3 in studies) {
            ret += createSecuredObjectAccess( // 7
                    principal: groups.find { it.category == EVERYONE_GROUP_NAME },
                    securedObject: securedObjects.find { it.bioDataUniqueId == STUDY3_SECURE_TOKEN },
                    accessLevel: accessLevels.find { it.name == 'MEASUREMENTS' })
        }

        long id = -700L
        ret.each { it.id = --id }
        ret
    }()


    void saveAuthorities() {
        save accessLevels
        save roles
        save groups
        save users
        users[0].addToRoles(roles.find { it.authority == 'ROLE_ADMIN' })
        users[1].addToGroups(groups.find { it.category == 'group_-201' })
    }

    void saveAll() {
        conceptTestData.saveAll()

        save i2b2Secures
        save securedObjects
        saveAuthorities()
        save securedObjectAccesses
        save dimensionStudies
    }

}
