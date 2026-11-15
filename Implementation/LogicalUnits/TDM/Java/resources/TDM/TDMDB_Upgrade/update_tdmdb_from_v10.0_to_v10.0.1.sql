-- TDM 10.0.1
SET search_path = ${@schema};

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_agent_info (
    task_id bigint NOT NULL,
    agent_info text NOT NULL,    
    job_last_updated_date timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT tdm_agent_info_pkey PRIMARY KEY (task_id)
);

ALTER TABLE ${@schema}.task_execution_list
    ADD COLUMN IF NOT EXISTS tables_batch_id text;

ALTER TABLE ${@schema}.task_execution_list
    ADD COLUMN IF NOT EXISTS run_type text default 'new';

ALTER TABLE ${@schema}.task_execution_list
DROP COLUMN IF EXISTS clean_redis;

ALTER TABLE ${@schema}.task_ref_exe_stats
    ADD COLUMN IF NOT EXISTS table_filter text,
    ADD COLUMN IF NOT EXISTS filter_type text;

ALTER TABLE ${@schema}.tasks ALTER COLUMN source_environment_id DROP NOT NULL;
ALTER TABLE ${@schema}.tasks ALTER COLUMN source_env_name DROP NOT NULL;
ALTER TABLE ${@schema}.tasks ALTER COLUMN be_id DROP NOT NULL;
ALTER TABLE ${@schema}.tasks ALTER COLUMN environment_id DROP NOT NULL;
ALTER TABLE ${@schema}.task_execution_entities ALTER COLUMN target_entity_id DROP NOT NULL;