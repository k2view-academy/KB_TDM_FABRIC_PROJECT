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