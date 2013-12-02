package org.transmartproject.db.test

import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import javax.annotation.PostConstruct
import javax.sql.DataSource

import static org.transmartproject.db.test.H2Views.ObjectStatus.*

/**
 * This class is for integration test purposes, but has to be here due to
 * technical limitations. In particular, when resources.groovy is run, neither
 * the script's classloader nor the context classloader know anything about
 * the test classpath.
 */
class H2Views {

    @Autowired
    @Qualifier('dataSource')
    DataSource dataSource

    private Sql sql

    @PostConstruct
    void init() {
        this.sql = new Sql(dataSource.connection)

        try {
            if (!isH2()) {
                return
            }

            createSearchBioMkrCorrelView()
            createBioMarkerCorrelMv()
        } finally {
            this.sql.close()
        }
    }

    void createBioMarkerCorrelMv() {
        if (handleCurrentState('BIOMART', 'BIO_MARKER_CORREL_MV')) {
            return
        }

        sql.execute '''
            CREATE VIEW BIOMART.BIO_MARKER_CORREL_MV (
                BIO_MARKER_ID,
                ASSO_BIO_MARKER_ID,
                CORREL_TYPE,
                MV_ID ) AS
            SELECT DISTINCT
                b.bio_marker_id,
                b.bio_marker_id AS asso_bio_marker_id,
                'GENE' AS correl_type,
                1 AS mv_id
            FROM
                    biomart.bio_marker b
            WHERE
                    b.bio_marker_type = 'GENE'
            UNION
            SELECT DISTINCT
                b.bio_marker_id,
                b.bio_marker_id AS asso_bio_marker_id,
                'Protein' AS correl_type,
                4 AS mv_id
            FROM
                biomart.bio_marker b
            WHERE
               b.bio_marker_type = 'Protein'
            UNION
            SELECT DISTINCT
                c.bio_data_id AS bio_marker_id,
                c.asso_bio_data_id AS asso_bio_marker_id,
                'PATHWAY_GENE' AS correl_type,
                2 AS mv_id
            FROM
                biomart.bio_marker b,
                biomart.bio_data_correlation c,
                biomart.bio_data_correl_descr d
            WHERE
                b.bio_marker_id = c.bio_data_id
                AND c.bio_data_correl_descr_id = d.bio_data_correl_descr_id
                AND b.primary_source_code <> 'ARIADNE'
                AND d.correlation = 'PATHWAY GENE'
            UNION
            SELECT DISTINCT
                c.bio_data_id AS bio_marker_id,
                c.asso_bio_data_id AS asso_bio_marker_id,
                'HOMOLOGENE_GENE' AS correl_type,
                3 AS mv_id
            FROM
                biomart.bio_marker b,
                biomart.bio_data_correlation c,
                biomart.bio_data_correl_descr d
            WHERE
                b.bio_marker_id = c.bio_data_id
                AND c.bio_data_correl_descr_id = d.bio_data_correl_descr_id
                AND d.correlation = 'HOMOLOGENE GENE'
            UNION
            SELECT DISTINCT
                c.bio_data_id AS bio_marker_id,
                c.asso_bio_data_id AS asso_bio_marker_id,
                'PROTEIN TO GENE' AS correl_type,
                5 AS mv_id
            FROM
                biomart.bio_marker b,
                biomart.bio_data_correlation c,
                biomart.bio_data_correl_descr d
            WHERE
                b.bio_marker_id = c.bio_data_id
                AND c.bio_data_correl_descr_id = d.bio_data_correl_descr_id
                AND d.correlation = 'PROTEIN TO GENE'
            UNION
            SELECT DISTINCT
                c.bio_data_id AS bio_marker_id,
                c.asso_bio_data_id AS asso_bio_marker_id,
                'GENE TO PROTEIN' AS correl_type,
                6 AS mv_id
            FROM
                biomart.bio_marker b,
                biomart.bio_data_correlation c,
                biomart.bio_data_correl_descr d
            WHERE
                b.bio_marker_id = c.bio_data_id
                AND c.bio_data_correl_descr_id = d.bio_data_correl_descr_id
                AND d.correlation = 'GENE TO PROTEIN';'''
    }

    void createSearchBioMkrCorrelView() {
        if (handleCurrentState('SEARCHAPP', 'SEARCH_BIO_MKR_CORREL_VIEW')) {
            return
        }

        sql.execute '''
            CREATE VIEW SEARCHAPP.SEARCH_BIO_MKR_CORREL_VIEW (
                DOMAIN_OBJECT_ID,
                ASSO_BIO_MARKER_ID,
                CORREL_TYPE,
                VALUE_METRIC,
                MV_ID ) AS
            SELECT
                domain_object_id,
                asso_bio_marker_id,
                correl_type,
                value_metric,
                mv_id
            FROM (
                    SELECT
                        i.SEARCH_GENE_SIGNATURE_ID AS domain_object_id,
                        i.BIO_MARKER_ID AS asso_bio_marker_id,
                        'GENE_SIGNATURE_ITEM' AS correl_type,
                        CASE
                            WHEN i.FOLD_CHG_METRIC IS NULL THEN 1
                            ELSE i.FOLD_CHG_METRIC
                        END AS value_metric,
                        1 AS mv_id
                    FROM
                        SEARCHAPP.SEARCH_GENE_SIGNATURE_ITEM i,
                        SEARCHAPP.SEARCH_GENE_SIGNATURE gs
                    WHERE
                        i.SEARCH_GENE_SIGNATURE_ID = gs.SEARCH_GENE_SIGNATURE_ID
                        AND gs.DELETED_FLAG IS FALSE
                        AND i.bio_marker_id IS NOT NULL
                    UNION
                        ALL
                    SELECT
                        i.SEARCH_GENE_SIGNATURE_ID AS domain_object_id,
                        bada.BIO_MARKER_ID AS asso_bio_marker_id,
                        'GENE_SIGNATURE_ITEM' AS correl_type,
                        CASE
                            WHEN i.FOLD_CHG_METRIC IS NULL THEN 1
                            ELSE i.FOLD_CHG_METRIC
                        END AS value_metric,
                        2 AS mv_id
                    FROM
                        SEARCHAPP.SEARCH_GENE_SIGNATURE_ITEM i,
                        SEARCHAPP.SEARCH_GENE_SIGNATURE gs,
                        BIOMART.BIO_ASSAY_DATA_ANNOTATION bada
                    WHERE
                        i.SEARCH_GENE_SIGNATURE_ID = gs.SEARCH_GENE_SIGNATURE_ID
                        AND gs.DELETED_FLAG IS FALSE
                        AND bada.bio_assay_feature_group_id = i.bio_assay_feature_group_id
                        AND i.bio_assay_feature_group_id IS NOT NULL ) A; '''
    }

    enum ObjectStatus {
        IS_VIEW,
        IS_TABLE,
        DOES_NOT_EXIST
    }

    ObjectStatus getCurrentStatus(String schema, String viewName) {
        def res

        res = sql.firstRow """
            SELECT EXISTS(
                SELECT TABLE_NAME
                FROM INFORMATION_SCHEMA.VIEWS
                WHERE TABLE_SCHEMA = $schema AND TABLE_NAME = $viewName)"""
        if (res[0]) {
            return IS_VIEW
        }

        res = sql.firstRow """
            SELECT EXISTS(
                SELECT TABLE_NAME
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = $schema AND TABLE_NAME = $viewName)"""
        if (res[0]) {
            return IS_TABLE
        }

        DOES_NOT_EXIST
    }

    Boolean handleCurrentState(String schema, String viewName) {
        switch (getCurrentStatus(schema, viewName)) {
            case DOES_NOT_EXIST:
                return false
            case IS_TABLE:
                sql.execute("DROP TABLE $schema.$viewName" as String)
                return false
            case IS_VIEW:
                return true
        }
    }

    Boolean isH2() {
        sql.connection.metaData.databaseProductName.equalsIgnoreCase('h2')
    }
}
