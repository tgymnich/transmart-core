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

package org.transmartproject.db.querytool

import org.transmartproject.core.userquery.UserQuerySetInstance

@Deprecated
class QuerySetInstance implements UserQuerySetInstance {

    Long id
    Long objectId
    QuerySet querySet

    static belongsTo = QuerySet

    static mapping = {
        table schema: 'BIOMART_USER'
        querySet fetch: 'join'
        version false
    }

    static constraints = {
        id generator: 'sequence', params: [sequence: 'query_set_instance_id_seq', schema: 'biomart_user']
        querySet column: 'query_set_id'
    }

}
