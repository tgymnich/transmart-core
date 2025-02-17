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

package org.transmartproject.db.ontology

import grails.orm.HibernateCriteriaBuilder
import org.hibernate.criterion.MatchMode
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.Study
import org.transmartproject.core.concept.ConceptFullName
import org.transmartproject.core.concept.ConceptKey
import org.transmartproject.db.util.StringUtils

abstract class AbstractAcrossTrialsOntologyTerm
        implements OntologyTerm, MetadataSelectQuerySpecification {

    public final static String ACROSS_TRIALS_TABLE_CODE = 'xtrials'
    public final static String ACROSS_TRIALS_TOP_TERM_NAME = "Across Trials"

    /* Relies mainly on fullName; also on level */

    ConceptKey getConceptKey() {
        new ConceptKey(ACROSS_TRIALS_TABLE_CODE, fullName)
    }

    @Override
    String getKey() {
        conceptKey.toString()
    }

    @Override
    String getName() {
        conceptKey.conceptFullName.parts[-1]
    }

    @Override
    String getTooltip() {
        name
    }

    @Override
    OntologyTerm getParent() {
        HibernateCriteriaBuilder c
        def fullNameSearch = this.conceptKey.conceptFullName.parent.toString()

        c = createCriteria()
        List<AbstractI2b2Metadata> ret = c.list {
            and {
                eq 'fullName', fullNameSearch
                eq 'level', level - 1
            }
        }
        if (!ret || ret.empty) {
            return null
        }
        def parentNode = ret[0]
        parentNode
    }

    @Override
    Study getStudy() {
        /* null for now. We may want to create a fake study for
         * xtrials purposes */
    }

    @Override
    List<OntologyTerm> getChildren() {
        getDescendants(false)
    }

    @Override
    List<OntologyTerm> getChildren(boolean showHidden, boolean showSynonyms) {
        getDescendants(false, showHidden, showSynonyms)
    }

    @Override
    List<OntologyTerm> getAllDescendants() {
        getDescendants(true)
    }

    @Override
    List<OntologyTerm> getAllDescendants(boolean showHidden, boolean showSynonyms) {
        getAllDescendants(true, showHidden, showSynonyms)
    }
	
	@Override
	List<OntologyTerm> getHDforAllDescendants() {
		throw new UnsupportedOperationException('XTrial concept does not support returning HD descendants.')
	}

	@Override
	List<String> getAllDescendantsForFacets() {
		getAllDescendants()*.fullName
	}

    private List<OntologyTerm> getDescendants(boolean allDescendants,
                                              boolean showHidden = false /* ignored */,
                                              boolean showSynonyms = false /* ignored */) {
        // do not include ACROSS_TRIALS_TOP_TERM_NAME part in prefix:
        String pathPrefix = conceptKey.conceptFullName.length > 1 ?
                "\\${conceptKey.conceptFullName[1..-1].join '\\'}\\" : null

        ((HibernateCriteriaBuilder)ModifierDimensionView.createCriteria()).list {
            if (pathPrefix) {
                add(StringUtils.like('path', pathPrefix, MatchMode.START))
            }
            if (!allDescendants) {
                eq 'level',
                        level - 1L /* "Across Trials" */ + 1L /* children */
            }
        }.collect { new AcrossTrialsOntologyTerm(modifierDimension: it) }
    }

    @Override
    List<Patient> getPatients() {
        // can't work right now because this object doesn't have access to
        // user in context. To support this we'll probably have to move
        // the user in context bean from transmartApp to core-db
        throw new UnsupportedOperationException("Retrieving patients for " +
                "x-trial node not supported yet")
    }

    /* query specification methods */

    @Override
    String getFactTableColumn() {
        'modifier_cd'
    }

    @Override
    String getDimensionTableName() {
        'modifier_dimension'
    }

    @Override
    String getColumnName() {
        'modifier_path'
    }

    @Override
    String getColumnDataType() {
        'T' // text
    }

    @Override
    String getOperator() {
        'LIKE'
    }

    @Override
    String getDimensionCode() {
        def conceptFullName = new ConceptFullName(fullName)
        if (conceptFullName.length == 1) {
            '\\'
        } else {
            "\\${conceptFullName[1..-1].join '\\'}\\"
        }
    }
}
