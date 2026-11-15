-- TDM 10.1
SET search_path = ${@schema};

ALTER TABLE ${@schema}.task_ref_tables
    ADD COLUMN IF NOT EXISTS records_limit bigint DEFAULT -1;

ALTER TABLE ${@schema}.task_ref_tables
    ADD COLUMN IF NOT EXISTS partitions_limit bigint DEFAULT -1;

-- Execution-level record sampling overrides (TABLE_SAMPLING); NULL means use task_ref_tables
ALTER TABLE ${@schema}.task_ref_exe_stats
    ADD COLUMN IF NOT EXISTS records_limit bigint;

ALTER TABLE ${@schema}.task_ref_exe_stats
    ADD COLUMN IF NOT EXISTS partitions_limit bigint;

ALTER TABLE ${@schema}.task_execution_list
    ADD COLUMN IF NOT EXISTS tables_batch_id text;

ALTER TABLE ${@schema}.task_execution_summary
    ADD COLUMN IF NOT EXISTS sync_batch_id text;

ALTER TABLE ${@schema}.task_execution_summary
    ADD COLUMN IF NOT EXISTS sync_retry_count integer DEFAULT 0;

ALTER TABLE ${@schema}.task_execution_summary
    ADD COLUMN IF NOT EXISTS synced_to_fabric boolean DEFAULT false;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = '${@schema}' AND table_name = 'task_execution_list' AND column_name = 'synced_to_fabric'
    ) THEN
        UPDATE ${@schema}.task_execution_summary tes
        SET synced_to_fabric = tel.synced_to_fabric
        FROM (
            SELECT task_execution_id, bool_and(coalesce(synced_to_fabric, false)) AS synced_to_fabric
            FROM ${@schema}.task_execution_list
            GROUP BY task_execution_id
        ) tel
        WHERE tel.task_execution_id = tes.task_execution_id;
    END IF;
END $$;

ALTER TABLE ${@schema}.task_execution_list
    DROP COLUMN IF EXISTS synced_to_fabric;

INSERT INTO ${@schema}.tdm_general_parameters (param_name, param_value)
VALUES ('ENABLE_TABLE_LEVEL_PARTITION_FLOW', 'false') ON CONFLICT DO NOTHING;

-- update prompts
UPDATE ${@schema}.task_execution_prompt_text
SET prompt_text = 'Copy all pre-generated synthetic <be_name> entities (<generation_number_of_entities> total) from the Test Data Store to <environment_name>. Source generation: ''<generation_title>'' (Created: <generation_creation_date>).'
WHERE task_type = 'Load execution generation entities (AI/rule based)';

INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Clone and reserve entities','Clone <be_name> data from <source_env_name> to <environment_name> 
Subset by <selection_method> using: <selection_param_value>. Clone the entity <num_of_entities> times.
Reserve the entities for <reserve_retention_period_value> <reserve_retention_period_type>.') ON CONFLICT DO NOTHING;


CREATE TABLE IF NOT EXISTS ${@schema}.tdm_exe_catalog_field_info
(
    catalog_version text NOT NULL DEFAULT '-1',
    interface_name text NOT NULL,
    schema_name text NOT NULL,
    csv_file text,
    creation_date timestamp without time zone DEFAULT now(),
    CONSTRAINT tdm_exe_catalog_field_info_pkey PRIMARY KEY (interface_name, schema_name, catalog_version)
);

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_exe_catalog_classification_generators
(
    task_execution_id bigint NOT NULL,
    csv_file text,
    CONSTRAINT tdm_exe_catalog_classification_generators_pkey PRIMARY KEY (task_execution_id)
);

CREATE TABLE IF NOT EXISTS ${@schema}.task_execution_masking_info
(
    task_execution_id bigint NOT NULL,
    interface_name text NOT NULL,
    schema_name text NOT NULL,
    table_name text NOT NULL,
    catalog_version text DEFAULT '-1',
    number_of_records bigint DEFAULT 0,
    sync_time timestamp without time zone,
    CONSTRAINT task_execution_masking_info_pkey PRIMARY KEY (task_execution_id, interface_name, schema_name, table_name)
);

Create INDEX IF NOT EXISTS TASK_EXEC_MASKING_INFO_IX1 ON ${@schema}.task_execution_masking_info (interface_name, schema_name, catalog_version);

-- Provenance for AI-prepared executions: which aifusion chat/session produced this override.
-- One row per override, so a session that prepares several drafts simply owns several rows.
-- Survives onto the real execution, since starting a draft reuses the row (draft -> false).
ALTER TABLE ${@schema}.task_execution_override_attrs
    ADD COLUMN IF NOT EXISTS agent_session_id text;

-- TDM 10.1 - AI test-data agent batch jobs (test-data-agent/createTask API)
CREATE TABLE IF NOT EXISTS ${@schema}.tdm_ai_job (
    job_id             text NOT NULL,
    external_story_id  text,
    name               text,
    description        text,
    source_environment text,
    target_environment text,
    business_entity    text,
    status             text NOT NULL,
    failure_reason     text,
    submitted_by       text,
    roles              text,
    started_at         timestamp without time zone,
    completed_at       timestamp without time zone,
    CONSTRAINT tdm_ai_job_pkey PRIMARY KEY (job_id)
);

CREATE INDEX IF NOT EXISTS tdm_ai_job_story_ix ON ${@schema}.tdm_ai_job (external_story_id);

-- For a database that already ran this script before roles existed: the CREATE above is
-- IF NOT EXISTS, so it cannot add the column to a table it finds already there.
ALTER TABLE ${@schema}.tdm_ai_job
    ADD COLUMN IF NOT EXISTS roles text;


CREATE TABLE IF NOT EXISTS ${@schema}.tdm_ai_job_case (
    job_id             text NOT NULL,
    test_case_id       text NOT NULL,
    content            text,
    required_count     integer,
    status             text NOT NULL,
    task_id            bigint,
    draft_execution_id bigint,
    deeplink           text,
    explanation        text,
    failure_reason     text,
    report_memory_id   text,
    agent_session_id   text,
    started_at         timestamp without time zone,
    completed_at       timestamp without time zone,
    CONSTRAINT tdm_ai_job_case_pkey PRIMARY KEY (job_id, test_case_id),
    CONSTRAINT fk_tdm_ai_job_case_job_id FOREIGN KEY (job_id) REFERENCES ${@schema}.tdm_ai_job(job_id)
);

CREATE INDEX IF NOT EXISTS tdm_ai_job_case_status_ix ON ${@schema}.tdm_ai_job_case (job_id, status);
