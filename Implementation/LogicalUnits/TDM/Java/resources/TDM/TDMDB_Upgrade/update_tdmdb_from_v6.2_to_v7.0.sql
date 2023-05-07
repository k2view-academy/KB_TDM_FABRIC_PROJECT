ALTER TABLE ${@schema}.environments
ADD COLUMN IF NOT EXISTS allow_read boolean NOT NULL DEFAULT false;

update ${@schema}.environments set allow_read = true where fabric_environment_name is not null;

ALTER TABLE ${@schema}.environment_products
ADD COLUMN IF NOT EXISTS data_center_name character varying(200);

update ${@schema}.environment_products e set data_center_name = (select data_center_name from data_centers d where d.data_center_id = e.data_center_id);

ALTER TABLE ${@schema}.environment_products
DROP COLUMN IF EXISTS data_center_id;

ALTER TABLE ${@schema}.task_execution_list
ADD COLUMN IF NOT EXISTS data_center_name character varying(200);

update ${@schema}.task_execution_list l set data_center_name = (select data_center_name from data_centers d where d.data_center_id = l.data_center_id);

ALTER TABLE ${@schema}.task_execution_list
DROP COLUMN IF EXISTS data_center_id;

-- Support Post Execution Process, TDM 7.0.1
DROP SEQUENCE IF EXISTS ${@schema}.post_exe_process_id_seq;
CREATE SEQUENCE ${@schema}.post_exe_process_id_seq
	INCREMENT 1
	START 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	CACHE 1;

-- Support Post Execution Process, TDM 7.0.1
CREATE TABLE IF NOT EXISTS ${@schema}.tdm_be_post_exe_process (
	process_id bigint NOT NULL DEFAULT nextval('post_exe_process_id_seq'::regclass),
	process_name character varying(500),
	process_description character varying(500),
	be_id bigint,
	execution_order integer NOT NULL,
	CONSTRAINT be_post_exe_process_pkey PRIMARY KEY (process_id)
);
CREATE UNIQUE INDEX tdm_be_post_exe_process_ix1 ON ${@schema}.tdm_be_post_exe_process (process_name, be_id);

CREATE TABLE IF NOT EXISTS ${@schema}.tasks_post_exe_process (
	task_id bigint NOT NULL,
	process_id bigint NOT NULL,
	process_name character varying(500) NOT NULL,
	execution_order integer NOT NULL,
	CONSTRAINT tasks_post_exe_process_pkey PRIMARY KEY (task_id, process_id)
);

alter table task_execution_list ADD COLUMN IF NOT EXISTS process_id bigint default 0;
alter table task_execution_list drop CONSTRAINT task_execution_list_pkey;
alter table task_execution_list ADD CONSTRAINT task_execution_list_pkey PRIMARY KEY (lu_id, task_execution_id, task_id, process_id);
                                                    
alter table environments ADD COLUMN IF NOT EXISTS sync_mode character varying(20) DEFAULT 'ON';
alter table tasks ADD COLUMN IF NOT EXISTS sync_mode character varying(20);
                                                    
alter table task_execution_summary ADD COLUMN IF NOT EXISTS tot_num_of_processed_post_executions numeric(10,0);
alter table task_execution_summary ADD COLUMN IF NOT EXISTS tot_num_of_succeeded_post_executions numeric(10,0);
alter table task_execution_summary ADD COLUMN IF NOT EXISTS tot_num_of_failed_post_executions numeric(10,0);

alter table task_execution_list alter column product_id drop NOT NULL;
alter table task_execution_list alter column product_version drop NOT NULL;
alter table task_execution_list alter column lu_id set default 0;
alter table task_execution_list alter column process_id set NOT NULL;

alter table task_execution_list drop COLUMN IF EXISTS etl_execution_id;
alter table task_execution_list drop COLUMN IF EXISTS etl_ip_address;

drop index if exists TASK_EXEC_IX;
Create UNIQUE INDEX TASK_EXEC_IX on task_execution_list(task_id, lu_id, process_id) where upper(execution_status) IN ('RUNNING','EXECUTING','STARTED','PENDING','PAUSED', 'STARTEXECUTIONREQUESTED');

alter table task_execution_entities drop CONSTRAINT task_execution_entities_pkey;
alter table task_execution_entities drop COLUMN IF EXISTS etl_execution_id;
alter table task_execution_entities ADD CONSTRAINT task_execution_entities_pkey PRIMARY KEY (task_execution_id, lu_name, entity_id, target_entity_id);
alter table task_execution_entities ADD COLUMN IF NOT EXISTS fabric_get_time bigint;
alter table task_execution_entities ADD COLUMN IF NOT EXISTS total_processing_time bigint;
alter table task_execution_entities ADD COLUMN IF NOT EXISTS clone_no character varying(50) DEFAULT '0';


DROP TABLE ${@schema}.tdm_lu_type_rel_tar_eid;
				  
CREATE TABLE ${@schema}.tdm_lu_type_rel_tar_eid
(
	target_env character varying(200) NOT NULL,
	lu_type_1 character varying(200) NOT NULL,
	lu_type_2 character varying(200) NOT NULL,
	lu_type1_eid character varying(50) NOT NULL,
	lu_type2_eid character varying(50) NOT NULL,
	creation_date timestamp without time zone,
	CONSTRAINT tdm_lu_type_rel_tar_eid_pk PRIMARY KEY (target_env, lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid)
);

CREATE INDEX IF NOT EXISTS tdm_lu_type_rel_tar_eid_2ix ON ${@schema}.tdm_lu_type_rel_tar_eid (lu_type_1, lu_type1_eid); -- TDM 6.1
CREATE INDEX IF NOT EXISTS tdm_lu_type_rel_tar_eid_3ix ON ${@schema}.tdm_lu_type_rel_tar_eid (lu_type_2, lu_type2_eid); -- TDM 6.1


DROP TABLE ${@schema}.task_exe_error_detailed;

CREATE TABLE IF NOT EXISTS ${@schema}.task_exe_error_detailed
(
	task_execution_id bigint NOT NULL,
	lu_name character varying(200) NOT NULL,
	entity_id text NOT NULL,
	iid character varying(50) NOT NULL,
	target_entity_id text NOT NULL,
	error_category character varying(100) NOT NULL,
	error_code character varying(100),
	error_message character varying(4000) NOT NULL,
	creation_date timestamp without time zone NOT NULL DEFAULT now(),
	flow_name character varying(100),
	stage_name character varying(100),
	actor_name character varying(100),
	actor_parameters character varying(500)
);

-- Index: task_exe_error_detailed_1ix

-- DROP INDEX ${@schema}.task_exe_error_detailed_1ix;

CREATE INDEX IF NOT EXISTS task_exe_error_detailed_1ix ON ${@schema}.task_exe_error_detailed (task_execution_id, lu_name, target_entity_id);

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

