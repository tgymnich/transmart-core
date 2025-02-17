ALTER TABLE DEAPP.DE_SNP_SUBJECT_SORTED_DEF
 ADD BIO_ASSAY_PLATFORM_ID  NUMERIC(18, 0);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD BIO_ASY_GENO_PLATFORM_PROBE_ID  NUMERIC(22);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD GENOTYPE_PROBE_ANNOTATION_ID  NUMERIC(22);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD A1  VARCHAR(4000);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD A2  VARCHAR(4000);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD A1_CLOB  TEXT;

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD A2_CLOB  TEXT;

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD IMPUTE_QUALITY  NUMERIC;

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD GPS_BY_PROBE_BLOB  bytea;

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD GTS_BY_PROBE_BLOB  bytea;

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD DOSE_BY_PROBE_BLOB  bytea;

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD GT_PROBABILITY_THRESHOLD  NUMERIC;

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD MAF  NUMERIC;

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD MINOR_ALLELE  VARCHAR(2);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD C_A1_A1  NUMERIC(10);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD C_A1_A2  NUMERIC(10);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD C_A2_A2  NUMERIC(10);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD C_NOCALL  NUMERIC(10);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD CREATED_BY  VARCHAR(30);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD CREATED_DATE  DATE;

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD MODIFIED_BY  VARCHAR(30);

ALTER TABLE DEAPP.DE_SNP_DATA_BY_PROBE
 ADD MODIFIED_DATE  DATE;

CREATE INDEX DE_SNP_CHROMPOS_IND ON DEAPP.DE_SNP_INFO
(CHROM, CHROM_POS);

CREATE UNIQUE INDEX IDX_DSDBP_PROBE_IDNAME ON DEAPP.DE_SNP_DATA_BY_PROBE
(SNP_DATA_BY_PROBE_ID, PROBE_NAME);

CREATE INDEX IDX_DSDBP1_ANNO_ID ON DEAPP.DE_SNP_DATA_BY_PROBE
(GENOTYPE_PROBE_ANNOTATION_ID);

CREATE INDEX IDX_DSDBP1_GP_PROBE_ID ON DEAPP.DE_SNP_DATA_BY_PROBE
(BIO_ASY_GENO_PLATFORM_PROBE_ID);

CREATE INDEX IDX_DSDBP_TRIAL_NAME ON DEAPP.DE_SNP_DATA_BY_PROBE
(TRIAL_NAME);

CREATE INDEX IND_VCF_RSID ON DEAPP.DE_RC_SNP_INFO
(RS_ID);

CREATE INDEX IND_VCF_POS ON DEAPP.DE_RC_SNP_INFO
(POS);

CREATE INDEX DE_R_S_I_IND4 ON DEAPP.DE_RC_SNP_INFO
(SNP_INFO_ID);

CREATE UNIQUE INDEX DE_RSNP_HGRS_IND ON DEAPP.DE_RC_SNP_INFO
(HG_VERSION, RS_ID);

CREATE INDEX DE_RSNP_CHROM_COMP_IDX ON DEAPP.DE_RC_SNP_INFO
(CHROM, HG_VERSION, POS);

CREATE INDEX DE_RSNP_CHROMPOS_IND ON DEAPP.DE_RC_SNP_INFO
(CHROM, POS);

CREATE VIEW DEAPP.DE_SNP_INFO_HG19_MV
AS SELECT rs_id,
       chrom,
       pos,
       gene_name AS rsgene,
       exon_intron,
       recombination_rate,
       regulome_score
  FROM deapp.de_rc_snp_info info
 WHERE info.hg_version = '19';

CREATE OR REPLACE FUNCTION DEAPP.FUN_SNP_DATA_BY_PPROBE_ID() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   IF NEW.SNP_DATA_BY_PROBE_ID IS NULL
   THEN
     SELECT nextval('DEAPP.SEQ_DATA_ID')
      INTO NEW.SNP_DATA_BY_PROBE_ID;
   END IF;
   RETURN NEW;
END;
$$;

CREATE TRIGGER TRG_SNP_DATA_BY_PPROBE_ID BEFORE INSERT ON DEAPP.DE_SNP_DATA_BY_PROBE FOR EACH ROW EXECUTE PROCEDURE DEAPP.FUN_SNP_DATA_BY_PPROBE_ID();