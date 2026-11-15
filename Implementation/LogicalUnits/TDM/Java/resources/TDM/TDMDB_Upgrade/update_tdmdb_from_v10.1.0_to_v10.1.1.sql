SET search_path = ${@schema};

DROP TABLE IF EXISTS ${@schema}.tdm_exe_catalog_field_info;

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_exe_catalog_field_info
(
    interface_name text NOT NULL,
    schema_name text NOT NULL,
    catalog_hash text NOT NULL,
    csv_file text,
    update_date timestamp without time zone DEFAULT now(),
    CONSTRAINT tdm_exe_catalog_field_info_pkey PRIMARY KEY (interface_name, schema_name, catalog_hash)
);

DROP INDEX IF EXISTS ${@schema}.task_exec_masking_info_ix1;

ALTER TABLE ${@schema}.task_execution_masking_info
    DROP COLUMN IF EXISTS catalog_version;

ALTER TABLE ${@schema}.task_execution_masking_info
    ADD COLUMN IF NOT EXISTS catalog_hash text;
