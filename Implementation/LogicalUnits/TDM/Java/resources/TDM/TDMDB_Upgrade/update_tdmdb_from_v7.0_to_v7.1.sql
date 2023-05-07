-- TDM 7.1- add the permision_groups_mapping TDM table 
-- DROP TABLE ${@schema}.permission_groups_mapping;

CREATE TABLE  IF NOT EXISTS ${@schema}.permission_groups_mapping
(
    description character varying(500),
    fabric_role character varying(100) NOT NULL,
    permission_group character varying(100) NOT NULL,
    created_by character varying(100) NOT NULL,
    updated_by character varying(100) NOT NULL,
    creation_date timestamp without time zone,
    update_date timestamp without time zone,
    CONSTRAINT permission_groups_mapping_pkey PRIMARY KEY (fabric_role)
);

-- Add initial mapping for admin user
insert into ${@schema}.permission_groups_mapping (
	description,
	fabric_role,
	permission_group,
	created_by,
	updated_by,
	creation_date,
	update_date
) values ('Initial mapping for admin user', 'admin', 'admin', 'admin', 'admin', NOW(), NOW());

-- Updte structure of ${@schema}.task_exe_error_detailed
ALTER TABLE ${@schema}.task_exe_error_detailed DROP COLUMN IF EXISTS etl_execution_id;
ALTER TABLE ${@schema}.task_exe_error_detailed DROP COLUMN IF EXISTS fabric_execution_id;
ALTER TABLE ${@schema}.task_exe_error_detailed DROP COLUMN IF EXISTS error_msg;
ALTER TABLE ${@schema}.task_exe_error_detailed ADD COLUMN IF NOT EXISTS error_category character varying(100) NOT NULL;
ALTER TABLE ${@schema}.task_exe_error_detailed ADD COLUMN IF NOT EXISTS error_code character varying(100);
ALTER TABLE ${@schema}.task_exe_error_detailed ADD COLUMN IF NOT EXISTS error_message character varying(4000) NOT NULL;
ALTER TABLE ${@schema}.task_exe_error_detailed ADD COLUMN IF NOT EXISTS flow_name character varying(100);
ALTER TABLE ${@schema}.task_exe_error_detailed ADD COLUMN IF NOT EXISTS stage_name character varying(100);
ALTER TABLE ${@schema}.task_exe_error_detailed ADD COLUMN IF NOT EXISTS actor_name character varying(100);
ALTER TABLE ${@schema}.task_exe_error_detailed ADD COLUMN IF NOT EXISTS actor_parameters character varying(500);

-- Create new sequence ${@schema}.post_exe_process_id_seq
CREATE SEQUENCE ${@schema}.post_exe_process_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
	
-- Create new table ${@schema}.tdm_be_post_exe_process
CREATE TABLE IF NOT EXISTS ${@schema}.tdm_be_post_exe_process (
	process_id bigint NOT NULL DEFAULT nextval('post_exe_process_id_seq'::regclass),
	process_name character varying(500),
	process_description character varying(500),
	be_id bigint,
	execution_order integer NOT NULL,
	CONSTRAINT be_post_exe_process_pkey PRIMARY KEY (process_id)
);
CREATE UNIQUE INDEX tdm_be_post_exe_process_ix1 ON ${@schema}.tdm_be_post_exe_process (process_name, be_id);

-- Create new table ${@schema}.tasks_post_exe_process
CREATE TABLE IF NOT EXISTS ${@schema}.tasks_post_exe_process (
	task_id bigint NOT NULL,
	process_id bigint NOT NULL,
	process_name character varying(500) NOT NULL,
	execution_order integer NOT NULL,
	CONSTRAINT tasks_post_exe_process_pkey PRIMARY KEY (task_id, process_id)
);

-- Create new table ${@schema}.task_exe_stats_detailed
CREATE TABLE IF NOT EXISTS ${@schema}.task_exe_stats_detailed
(
    task_execution_id bigint NOT NULL,
    lu_name character varying(200)  NOT NULL,
    entity_id text NOT NULL,
    target_entity_id text NOT NULL,
    table_name character varying(100) NOT NULL,
    stage_name character varying(100),
    flow_name character varying(100),
    actor_name character varying(100),
    creation_date timestamp without time zone,
    source_count character varying(20),
    target_count character varying(20),
    diff character varying(20),
    results character varying(20)
);