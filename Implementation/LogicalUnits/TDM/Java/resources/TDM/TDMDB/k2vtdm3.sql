-- Table: public.activities.

--DROP TABLE IF EXISTS public.activities;

CREATE TABLE IF NOT EXISTS public.activities
(
    date timestamp without time zone,
    action character varying(200),
    entity character varying,
    username character varying(200),
    description character varying(200),
    user_id character varying(200)
);

-- Table: public.business_entities

--DROP TABLE IF EXISTS public.business_entities;

CREATE TABLE IF NOT EXISTS public.business_entities
(
    be_name character varying(200) NOT NULL,
    be_description text,
    be_id bigint NOT NULL DEFAULT nextval('business_entities_be_id_seq'::regclass),
    be_created_by character varying(200),
    be_creation_date timestamp without time zone,
    be_last_updated_date timestamp without time zone,
    be_last_updated_by character varying(200),
    be_status character varying(50),
    CONSTRAINT business_entities_pkey PRIMARY KEY (be_id)
);

Create UNIQUE INDEX IF NOT EXISTS BE_NAME_FOR_ACTIVE_IX ON Business_Entities (be_name) where be_status = 'Active';

-- Table: public.environment_owners

--DROP TABLE IF EXISTS public.environment_owners;

CREATE TABLE IF NOT EXISTS public.environment_owners
(
    environment_id bigint NOT NULL,
    user_type character varying(10) NOT NULL,
    user_name character varying(200),
    user_id character varying(200)
);
ALTER TABLE environment_owners ADD CONSTRAINT check_env_owner_type CHECK (user_type = 'ID' OR user_type = 'GROUP');
-- Table: public.environment_products

--DROP TABLE IF EXISTS public.environment_products;

CREATE TABLE IF NOT EXISTS public.environment_products
(
    environment_product_id bigint NOT NULL DEFAULT nextval('environment_product_id_seq'::regclass),
    environment_id bigint NOT NULL,
    product_id bigint NOT NULL,
    product_version character varying(200),
    created_by character varying,
    creation_date timestamp without time zone,
    last_updated_date timestamp without time zone,
    last_updated_by character varying,
    status character varying(200) NOT NULL,
    data_center_name character varying(200),
    CONSTRAINT environment_products_pkey PRIMARY KEY (environment_product_id)
);

Create UNIQUE INDEX IF NOT EXISTS ENV_PROD_FOR_ACTIVE_IX ON environment_products (environment_id, product_id) where status = 'Active';

-- Table: public.environment_role_users

--DROP TABLE IF EXISTS public.environment_role_users;

CREATE TABLE IF NOT EXISTS public.environment_role_users
(
    environment_id bigint NOT NULL,
    role_id bigint NOT NULL,
    user_type character varying(10) NOT NULL,
    username character varying,
    user_id character varying(200)
);

ALTER TABLE environment_role_users ADD CONSTRAINT check_user_type CHECK (user_type = 'ALL' OR user_type = 'ID' OR user_type = 'GROUP');
   
Create UNIQUE INDEX IF NOT EXISTS ENV_ROLE_USER_IX ON environment_role_users (environment_id, user_id);

-- Table: public.environment_roles

-- 31-Dec-18- add allow_read, allow_write, and allowed_number_of_entities_to_read fields to public.environment_roles 

--DROP TABLE IF EXISTS public.environment_roles;

CREATE TABLE IF NOT EXISTS public.environment_roles
(
    environment_id bigint NOT NULL,
    role_name character varying(200) NOT NULL,
    role_description text,
    allowed_delete_before_load boolean NOT NULL DEFAULT false,
    allowed_creation_of_synthetic_data boolean NOT NULL DEFAULT false,
    allowed_random_entity_selection boolean NOT NULL DEFAULT false,
    allowed_request_of_fresh_data boolean NOT NULL DEFAULT false,
    allowed_task_scheduling boolean NOT NULL DEFAULT false,
    allowed_number_of_entities_to_copy bigint NOT NULL DEFAULT 1000,
    role_id bigint NOT NULL DEFAULT nextval('environment_roles_role_id_seq'::regclass),
    role_created_by character varying(200),
    role_creation_date timestamp without time zone,
    role_last_updated_date timestamp without time zone,
    role_expiration_date timestamp without time zone,
    role_last_updated_by character varying(200),
    role_status character varying(50),
    allowed_refresh_reference_data boolean,
    allowed_replace_sequences boolean,
    allow_read boolean NOT NULL DEFAULT false, 
    allow_write boolean NOT NULL DEFAULT false, 
    allowed_number_of_entities_to_read bigint NOT NULL DEFAULT 1000,
    allowed_entity_versioning boolean NOT NULL DEFAULT false,
    allowed_test_conn_failure boolean NOT NULL DEFAULT false, -- TDM 6.1
    allowed_number_of_reserved_entities BIGINT  DEFAULT 0, -- TDM 7.4
    CONSTRAINT environment_roles_pkey PRIMARY KEY (role_id)
);

Create UNIQUE INDEX IF NOT EXISTS ENV_ROLE_FOR_ACTIVE_IX ON environment_roles  (environment_id, role_name) where role_status = 'Active';

-- Table: public.environments

-- 31-Dec-18- add fabric_environment_name field to environments

--DROP TABLE IF EXISTS public.environments;

CREATE TABLE IF NOT EXISTS public.environments
(
    environment_name character varying(200) NOT NULL,
    environment_description text,
    environment_expiration_date date,
    environment_point_of_contact_first_name character varying,
    environment_point_of_contact_last_name character varying(50),
    environment_point_of_contact_phone1 character varying(50),
    environment_point_of_contact_phone2 character varying(50),
    environment_point_of_contact_email character varying(50),
    environment_id bigint NOT NULL DEFAULT nextval('environments_environment_id_seq'::regclass),
    environment_created_by character varying(200),
    environment_creation_date timestamp without time zone,
    environment_last_updated_date timestamp without time zone,
    environment_last_updated_by character varying(200),
    environment_status character varying,
    allow_write boolean NOT NULL DEFAULT true,
    allow_read boolean NOT NULL DEFAULT false,
    sync_mode character varying(20) DEFAULT 'ON',
    CONSTRAINT environments_pkey PRIMARY KEY (environment_id)
);

Create UNIQUE INDEX IF NOT EXISTS ENV_NAME_FOR_ACTIVE_IX ON environments (environment_name) where environment_status = 'Active'; 

-- Table: public.parameters

--DROP TABLE IF EXISTS public.parameters;

CREATE TABLE IF NOT EXISTS public.parameters
(
    be_id bigint NOT NULL,
    param_name character varying(200) NOT NULL,
    param_type character varying(200),
    valid_values text[],
    min_value numeric[],
    max_value numeric[],
    CONSTRAINT parameters_pkey PRIMARY KEY (be_id, param_name)
);

-- Table: public.product_logical_units

--DROP TABLE IF EXISTS public.product_logical_units;

CREATE TABLE IF NOT EXISTS public.product_logical_units
(
    lu_name character varying(200) NOT NULL,
    lu_description text,
    be_id bigint NOT NULL,
    lu_parent_id bigint,
    lu_id bigint NOT NULL DEFAULT nextval('product_logical_units_lu_id_seq'::regclass),
    product_name character varying(200),
    lu_parent_name character varying(200),
    product_id bigint,
    lu_dc_name character varying(200), 
    CONSTRAINT product_logical_units_pkey PRIMARY KEY (lu_id)
        --, CONSTRAINT product_logical_units_lu_name_key UNIQUE (lu_name)
);

-- Table: public.products

--DROP TABLE IF EXISTS public.products;

CREATE TABLE IF NOT EXISTS public.products
(
    product_name character varying(200) NOT NULL,
    product_description text,
    product_vendor character varying(200),
    product_versions character varying(200),
    product_id bigint NOT NULL DEFAULT nextval('products_product_id_seq'::regclass),
    product_created_by character varying(200),
    product_creation_date timestamp without time zone,
    product_last_updated_date timestamp without time zone,
    product_last_updated_by character varying(200),
    product_status character varying(50),
    CONSTRAINT products_pkey PRIMARY KEY (product_id)
   --, CONSTRAINT products_product_name_key UNIQUE (product_name)
);

Create UNIQUE INDEX IF NOT EXISTS PROD_NAME_FOR_ACTIVE_IX ON products (product_name) where product_status = 'Active';

-- Table: public.task_execution_list

-- 31-DEC-18- add new fields to task_execution_list- fabric_execution_id, version_datetime, and version_expiration_date
-- 17-FEB-19- add task type field

--DROP TABLE IF EXISTS public.task_execution_list;

CREATE TABLE IF NOT EXISTS public.task_execution_list
(
    task_id bigint NOT NULL, 
    task_type character varying(20),
    task_execution_id bigint NOT NULL,
    creation_date timestamp without time zone,
    be_id bigint,
    environment_id bigint NOT NULL,
    product_id bigint,
    product_version character varying(50) COLLATE pg_catalog."default",
    execution_status character varying(50) COLLATE pg_catalog."default",
    start_execution_time timestamp without time zone,
    end_execution_time timestamp without time zone,
    num_of_processed_entities numeric(10),
    num_of_copied_entities numeric(10),
    num_of_failed_entities numeric(10),
    data_center_name character varying(200),
    lu_id bigint NOT NULL default 0,
    num_of_processed_ref_tables numeric(10,0),
    num_of_copied_ref_tables numeric(10,0),
    num_of_failed_ref_tables numeric(10,0),
    parent_lu_id bigint,
    source_env_name character varying(300), 
    source_environment_id bigint, 
    fabric_environment_name character varying(300),
    task_executed_by character varying(200),
    fabric_execution_id character varying(200),
    version_datetime timestamp without time zone,
    version_expiration_date timestamp without time zone,							
    synced_to_fabric boolean DEFAULT false, 
    updated_by character varying(100), 
    clean_redis boolean DEFAULT false, -- TDM 5.5
    process_id bigint NOT NULL default 0, -- IDM 7.0.1
    execution_note text, -- TDM 7.4
    source_product_version character varying(50), -- TD< 7.5.2
    CONSTRAINT task_execution_list_pkey PRIMARY KEY (task_execution_id, lu_id, process_id)
);

-- TDM 7.2 - Remove the unique index as it prevents rerunning the same task with different overriden attributes.
--Create UNIQUE INDEX IF NOT EXISTS TASK_EXEC_IX on task_execution_list(task_id, lu_id, process_id) where upper(execution_status) IN ('RUNNING','EXECUTING','STARTED','PENDING','PAUSED', 'STARTEXECUTIONREQUESTED');
Create INDEX IF NOT EXISTS TASK_EXEC_IX2 ON task_execution_list (task_execution_id, process_id); 

-- Table: public.tasks
-- Tali E.- 10-May-17- remove limitation of 1000 chars from selection_param_value and exclusion_list fields

-- 31-Dec-18- add task_type, version_ind, retention_period_type, retention_period_value, selected_version_task_name, selected_version_datetime, scheduling_end_date

--DROP TABLE IF EXISTS public.tasks;

CREATE TABLE IF NOT EXISTS public.tasks
(
    task_id bigint NOT NULL DEFAULT nextval('tasks_task_id_seq'::regclass),
    task_title character varying(200) NOT NULL,
    task_status character varying(200) DEFAULT 'Active',
    task_execution_status character varying(10) DEFAULT 'Active',
    num_of_entities bigint,
    environment_id bigint NOT NULL,
    be_id bigint NOT NULL,
    selection_method character varying(200) NOT NULL,
    selection_param_value text,
    entity_exclusion_list text,
    parameters text,
    refresh_reference_data boolean,
    delete_before_load boolean NOT NULL DEFAULT false,
    replace_sequences boolean,
    scheduler character varying(200),
    task_created_by character varying(200),
    task_creation_date timestamp without time zone,
    task_last_updated_date timestamp without time zone,
    task_last_updated_by character varying(200),
    source_env_name character varying(300) NOT NULL,
    source_environment_id bigint, 
    fabric_environment_name character varying(300), 	
    load_entity boolean,
    task_type character varying(20) NOT NULL,
    version_ind  boolean NOT NULL DEFAULT false,
    retention_period_type character varying(20),
    retention_period_value numeric,
    selected_version_task_name character varying(200),
    selected_version_datetime character varying(20),
    selected_version_task_exe_id bigint, 
    scheduling_end_date timestamp without time zone, 
    selected_ref_version_task_name character varying(200), 
    selected_ref_version_datetime character varying(20), 
    selected_ref_version_task_exe_id bigint,  
    task_globals boolean,
    sync_mode character varying(20),
    reserve_ind boolean NOT NULL DEFAULT false,
    reserve_retention_period_type character varying(20) COLLATE pg_catalog."default",
    reserve_retention_period_value numeric,
    reserve_note text, -- TDM 7.5.2
    filterout_reserved boolean DEFAULT true, --TDM 7.6
    CONSTRAINT tasks_pkey PRIMARY KEY (task_id)
);

Create UNIQUE INDEX IF NOT EXISTS TASK_NAME_FOR_ACTIVE_IX on tasks (task_title) where task_status = 'Active';

-- Table: public.tdm_be_env_exclusion_list

--DROP TABLE IF EXISTS public.tdm_be_env_exclusion_list;

CREATE TABLE IF NOT EXISTS public.tdm_be_env_exclusion_list
(
  be_id bigint,
  environment_id bigint,
  exclusion_list text,
  requested_by character varying(200),
  update_date timestamp without time zone,
  created_by character varying(200),
  updated_by character varying(200),
  be_env_exclusion_list_id integer NOT NULL DEFAULT nextval('tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq'::regclass),
  creation_date timestamp without time zone,
  CONSTRAINT tdm_be_env_exclusion_list_be_env_exclusion_list_id_pk PRIMARY KEY (be_env_exclusion_list_id)
);

Create UNIQUE INDEX IF NOT EXISTS BE_ENV_EXCLUSION_LIST_IX on tdm_be_env_exclusion_list (BE_ID,ENVIRONMENT_ID,REQUESTED_BY);

-- Table: public.tdm_seq_mapping

--DROP TABLE IF EXISTS public.tdm_seq_mapping;

CREATE TABLE IF NOT EXISTS public.tdm_seq_mapping
(task_execution_id      bigint NOT NULL,
lu_type character varying(30) NOT NULL,
source_env      character varying(100) NOT NULL,
entity_target_id        character varying(100),
seq_name        character varying(100),
table_name      character varying(100),
column_name     character varying(100),
source_id       character varying(100),
target_id       character varying(100),
is_instance_id  character varying(1),
entity_sequence bigint)
;

-- Table: public.task_execution_entities

-- DROP TABLE IF EXISTS public.task_execution_entities;

CREATE TABLE IF NOT EXISTS public.task_execution_entities
(
  task_execution_id text NOT NULL,
  lu_name text NOT NULL,
  entity_id text NOT NULL,
  target_entity_id text,
  creation_date timestamp without time zone,
  entity_end_time timestamp without time zone,
  entity_start_time timestamp without time zone,
  env_id text,
  execution_status text,
  id_type text,
  fabric_execution_id character varying(200), -- TDM 6.1
  iid character varying(50)  NOT NULL DEFAULT '', -- TDM 6.1
  source_env character varying(50) NOT NULL DEFAULT '', -- TDM 6.1
  version_name character varying(200) NOT NULL DEFAULT '', -- TDM 6.1
  version_datetime timestamp without time zone NOT NULL DEFAULT 'epoch', -- TDM 6.1
  fabric_get_time bigint,
  total_processing_time bigint,
  clone_no character varying(50) DEFAULT '0',
  CONSTRAINT task_execution_entities_pkey PRIMARY KEY (task_execution_id, lu_name, entity_id, target_entity_id)
);

CREATE INDEX IF NOT EXISTS task_execution_entities_2ix ON public.task_execution_entities (task_execution_id, lu_name, source_env, iid, version_name,version_datetime); -- TDM 6.1

-- Table: public.tdm_lu_type_relation_eid

--DROP TABLE IF EXISTS public.tdm_lu_type_relation_eid;

CREATE TABLE IF NOT EXISTS public.tdm_lu_type_relation_eid
(
  source_env character varying(200) NOT NULL,
  lu_type_1 character varying(200) NOT NULL,
  lu_type_2 character varying(200) NOT NULL,
  lu_type1_eid character varying(50) NOT NULL,
  lu_type2_eid character varying(50) NOT NULL,
  creation_date timestamp without time zone,
  version_name character varying(200) NOT NULL DEFAULT '', -- TDM 6.1
  version_datetime timestamp without time zone NOT NULL DEFAULT 'epoch', -- TDM 6.1
  CONSTRAINT tdm_lu_type_relation_eid_pk PRIMARY KEY (source_env,lu_type_1,lu_type_2,lu_type1_eid,lu_type2_eid,version_name,version_datetime)
);

CREATE INDEX IF NOT EXISTS tdm_lu_type_relation_eid_1ix ON public.tdm_lu_type_relation_eid (source_env,lu_type_1,lu_type1_eid,version_name,version_datetime); -- TDM 6.1
CREATE INDEX IF NOT EXISTS tdm_lu_type_relation_eid_2ix ON public.tdm_lu_type_relation_eid (source_env,lu_type_2,lu_type2_eid,version_name,version_datetime); -- TDM 6.1

 -- Table: public.tdm_lu_type_rel_tar_eid
				  
--DROP TABLE IF EXISTS public.tdm_lu_type_rel_tar_eid;
				  
CREATE TABLE IF NOT EXISTS public.tdm_lu_type_rel_tar_eid
(
	target_env character varying(200) NOT NULL,
	lu_type_1 character varying(200) NOT NULL,
	lu_type_2 character varying(200) NOT NULL,
	lu_type1_eid character varying(50) NOT NULL,
	lu_type2_eid character varying(50) NOT NULL,
	creation_date timestamp without time zone,
	CONSTRAINT tdm_lu_type_rel_tar_eid_pk PRIMARY KEY (target_env, lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid)
);

CREATE INDEX IF NOT EXISTS tdm_lu_type_rel_tar_eid_2ix ON public.tdm_lu_type_rel_tar_eid (lu_type_1, lu_type1_eid); -- TDM 6.1
CREATE INDEX IF NOT EXISTS tdm_lu_type_rel_tar_eid_3ix ON public.tdm_lu_type_rel_tar_eid (lu_type_2, lu_type2_eid); -- TDM 6.1

CREATE TABLE IF NOT EXISTS public.tasks_logical_units
(
  task_id bigint NOT NULL,
  lu_id  bigint NOT NULL,
  lu_name character varying(200),
  CONSTRAINT tasks_logical_units_pkey PRIMARY KEY (task_id, lu_name)
);

-- Table: public.task_ref_tables

--DROP TABLE IF EXISTS public.task_ref_tables;

CREATE TABLE IF NOT EXISTS public.task_ref_tables
(
  task_ref_table_id bigint NOT NULL DEFAULT nextval('tasks_ref_table_id_seq'::regclass),
  task_id bigint NOT NULL, 
  ref_table_name character varying(100),
  lu_name character varying(100),
  schema_name character varying(200),
  interface_name character varying(200),
 update_date timestamp without time zone,
CONSTRAINT task_ref_tables_pkey PRIMARY KEY (task_ref_table_id) 
);

-- Table: public.task_ref_exe_stats

--DROP TABLE IF EXISTS public.task_ref_exe_stats;

CREATE TABLE IF NOT EXISTS public.task_ref_exe_stats
(
  task_id bigint NOT NULL,
  task_execution_id bigint NOT NULL,
  task_ref_table_id bigint,
  ref_table_name character varying(100),
  job_uid character varying(100),
  update_date timestamp without time zone,
  start_time timestamp without time zone,
  end_time timestamp without time zone,
  execution_status character varying(50), 
  number_of_records_to_process numeric(10,0),
  number_of_processed_records numeric(10,0),
  error_msg text,
  updated_by character varying(100)	
  );

Create INDEX IF NOT EXISTS task_ref_exe_stats_IX1 on public.task_ref_exe_stats(task_execution_id);
Create INDEX IF NOT EXISTS task_ref_exe_stats_IX2 on public.task_ref_exe_stats(task_execution_id, execution_status);
Create INDEX IF NOT EXISTS task_ref_exe_stats_IX3 on public.task_ref_exe_stats(task_execution_id, task_ref_table_id, execution_status);

-- Table: public.tdm_general_parameters

-- DROP TABLE IF EXISTS public.tdm_general_parameters

CREATE TABLE IF NOT EXISTS public.tdm_general_parameters
(
  param_name character varying(200) NOT NULL,
  param_value character varying(2000),
  CONSTRAINT tdm_general_parameters_pk PRIMARY KEY (param_name)
);

INSERT INTO public.tdm_general_parameters(
            param_name, param_value)
    VALUES ('cleanup_retention_period', '2');

INSERT INTO public.tdm_general_parameters(
            param_name, param_value)
    VALUES ('tdm_gui_params', '{"maxRetentionPeriod":90,"defaultPeriod":{"unit":"Days","value":5},"permissionGroups":["admin","owner","tester"],"availableOptions":[{"name":"Minutes","units":0.00069444444},{"name":"Hours","units":0.04166666666},{"name":"Days","units":1},{"name":"Weeks","units":7},{"name":"Years","units":365}]}');
    
INSERT INTO public.tdm_general_parameters(
	   param_name, param_value) 
    VALUES ('TDM_VERSION', '7.5.2');

insert into public.tdm_general_parameters(
		param_name, param_value) 
	values ('MAX_RESERVATION_DAYS_FOR_TESTER', 10);

-- Table: public.task_globals

--DROP TABLE IF EXISTS public.task_globals;

CREATE TABLE IF NOT EXISTS public.task_globals
(
  task_id bigint NOT NULL,
  global_name character varying(200),
  global_value character varying(200)
);

Create INDEX IF NOT EXISTS task_globals_ix on public.task_globals(task_id);

-- Table: public.tdm_env_globals

--DROP TABLE IF EXISTS public.tdm_env_globals;

CREATE TABLE IF NOT EXISTS public.tdm_env_globals
(
   ENVIRONMENT_ID bigint ,
   GLOBAL_NAME character varying (200),
   GLOBAL_VALUE character varying (200),
   UPDATE_DATE timestamp without time zone,
   UPDATED_BY character varying (200)
);

create UNIQUE INDEX IF NOT EXISTS ENV_ID_GLOBAL_NAME_IX on tdm_env_globals (ENVIRONMENT_ID,GLOBAL_NAME);

-- New Table task_execution_summary, TDM 6.1
--DROP TABLE IF EXISTS public.task_execution_summary;
CREATE TABLE IF NOT EXISTS public.task_execution_summary
(
  task_execution_id bigint NOT NULL,
  task_id bigint NOT NULL,
  task_type character varying(20),
  creation_date timestamp without time zone,
  be_id bigint,
  environment_id bigint NOT NULL,
  execution_status character varying(50),
  start_execution_time timestamp without time zone,
  end_execution_time timestamp without time zone,
  tot_num_of_processed_root_entities numeric(10,0),
  tot_num_of_copied_root_entities numeric(10,0),
  tot_num_of_failed_root_entities numeric(10,0),
  tot_num_of_processed_ref_tables numeric(10,0),
  tot_num_of_copied_ref_tables numeric(10,0),
  tot_num_of_failed_ref_tables numeric(10,0),
  source_env_name character varying(300),
  source_environment_id bigint,
  fabric_environment_name character varying(300),
  task_executed_by character varying(200),
  version_datetime timestamp without time zone,
  version_expiration_date timestamp without time zone,
  update_date timestamp without time zone,
  tot_num_of_processed_post_executions numeric(10,0),
  tot_num_of_succeeded_post_executions numeric(10,0),
  tot_num_of_failed_post_executions numeric(10,0),
  CONSTRAINT task_execution_summary_pkey PRIMARY KEY (task_execution_id)
);
-- DROP INDEX IF EXISTS public.task_exec_summary_ix1;

CREATE INDEX IF NOT EXISTS task_exec_summary_ix1
  ON public.task_execution_summary  (task_id);

-- New Table task_exe_stats_summary, TDM 6.1
--DROP TABLE IF EXISTS public.task_exe_stats_summary;
--CREATE TABLE IF NOT EXISTS public.task_exe_stats_summary
--(
--	task_execution_id bigint NOT NULL,
--	lu_name character varying(200) NOT NULL,
--	creation_date timestamp without time zone,
--	table_name character varying(100),
--	source_count character varying(20),
--	target_count character varying(20),
--	diff character varying(20),
--	results character varying(20),
--	CONSTRAINT task_exe_stats_summary_pkey PRIMARY KEY (task_execution_id,lu_name,table_name)
--);

-- New Table task_exe_error_summary, TDM 6.1
--DROP TABLE IF EXISTS public.task_exe_error_summary;
CREATE TABLE IF NOT EXISTS public.task_exe_error_summary
(
	task_execution_id bigint NOT NULL,
	etl_execution_id numeric(10,0),
	lu_name character varying(200) NOT NULL,
	error_category character varying(200) NOT NULL,
	error_code character varying(100) NOT NULL,
	error_msg text NOT NULL,
	creation_date timestamp without time zone,
	no_of_records numeric(10,0),
	no_of_entities numeric(10,0),
	CONSTRAINT task_exe_error_summary_pkey PRIMARY KEY (task_execution_id,etl_execution_id,lu_name,error_category,error_code,error_msg)
);

-- New Table task_exe_error_detailed, TDM 6.1.1
--DROP TABLE IF EXISTS public.task_exe_error_detailed;
CREATE TABLE IF NOT EXISTS public.task_exe_error_detailed
(
	task_execution_id bigint NOT NULL,
	lu_name character varying(200) NOT NULL,
	entity_id text NOT NULL,
	iid character varying(50) NOT NULL,
	target_entity_id text NOT NULL,
	error_category character varying(100) NOT NULL,
	error_code character varying(100),
	error_message text NOT NULL,
	creation_date timestamp without time zone NOT NULL DEFAULT now(),
	flow_name character varying(100),
	stage_name character varying(100),
	actor_name character varying(100),
	actor_parameters text
);

-- Index: task_exe_error_detailed_1ix

-- DROP INDEX IF EXISTS public.task_exe_error_detailed_1ix;

CREATE INDEX IF NOT EXISTS task_exe_error_detailed_1ix ON public.task_exe_error_detailed (task_execution_id, lu_name, target_entity_id);

-- Support Post Execution Process, TDM 7.0.1
-- Table public.tdm_be_post_exe_process
--DROP TABLE IF EXISTS public.tdm_be_post_exe_process;
CREATE TABLE IF NOT EXISTS public.tdm_be_post_exe_process (
	process_id bigint NOT NULL DEFAULT nextval('post_exe_process_id_seq'::regclass),
	process_name text,
	process_description text,
	be_id bigint,
	execution_order integer NOT NULL,
	CONSTRAINT be_post_exe_process_pkey PRIMARY KEY (process_id)
);
CREATE UNIQUE INDEX IF NOT EXISTS tdm_be_post_exe_process_ix1 ON public.tdm_be_post_exe_process (process_name, be_id);

-- Table public.tasks_post_exe_process
--DROP TABLE IF EXISTS public.tasks_post_exe_process;
CREATE TABLE IF NOT EXISTS public.tasks_post_exe_process (
	task_id bigint NOT NULL,
	process_id bigint NOT NULL,
	process_name text NOT NULL,
	execution_order integer NOT NULL,
	CONSTRAINT tasks_post_exe_process_pkey PRIMARY KEY (task_id, process_id)
);

-- Table public.task_exe_stats_detailed
--DROP TABLE IF EXISTS public.task_exe_stats_detailed;
CREATE TABLE IF NOT EXISTS public.task_exe_stats_detailed
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

-- Table public.permission_groups_mapping; - TDM 7.1
-- DROP TABLE IF EXISTS public.permission_groups_mapping;

CREATE TABLE IF NOT EXISTS public.permission_groups_mapping
(
    description text,
    fabric_role character varying(100) NOT NULL,
    permission_group character varying(100) NOT NULL,
    created_by character varying(100) NOT NULL,
    updated_by character varying(100) NOT NULL,
    creation_date timestamp without time zone,
    update_date timestamp without time zone,
    CONSTRAINT permission_groups_mapping_pkey PRIMARY KEY (fabric_role)
);

-- Add initial mapping for admin user
insert into public.permission_groups_mapping (
	description,
	fabric_role,
	permission_group,
	created_by,
	updated_by,
	creation_date,
	update_date
) values ('Initial mapping for admin user', 'admin', 'admin', 'admin', 'admin', NOW(), NOW());


-- Table: public.task_execution_override_attrs - TDM 7.2

-- DROP TABLE IF EXISTS public.task_execution_override_attrs;

CREATE TABLE IF NOT EXISTS public.task_execution_override_attrs
(
	task_id bigint NOT NULL,
	task_execution_id bigint NOT NULL,
	override_parameters json NOT NULL,
	CONSTRAINT task_execution_override_attrs_pkey PRIMARY KEY (task_execution_id, task_id)
);

-- Table: public.tdm_reserved_entities - TDM 7.4
--DROP TABLE IF EXISTS public.tdm_reserved_entities;

CREATE TABLE IF NOT EXISTS public.tdm_reserved_entities
(
	entity_id text NOT NULL,
	be_id bigint NOT NULL,
	env_id bigint NOT NULL,
	task_id bigint NOT NULL,
	task_execution_id bigint NOT NULL,
	start_datetime timestamp without time zone,
	end_datetime timestamp without time zone,
	reserve_owner text,
	reserve_consumers text,
	reserve_notes text,
	reserve_tags json,
	CONSTRAINT tdm_reserved_entities_pkey PRIMARY KEY (entity_id, be_id, env_id)
);


-- utils functions (working with parameters)
-- json_cast function adds changes the format of the json data
CREATE OR REPLACE FUNCTION json_cast(data json) RETURNS json IMMUTABLE AS 
$body$
    SELECT ('{'|| string_agg(to_json(UPPER(key)) || ':' || '"' || replace(regexp_split_to_array(replace(regexp_replace(value::text, '\[(.*)\]', '\1'), '"',''), ',')::text, '"','') || '"', ',') || '}')::json
    FROM (
        SELECT * FROM json_each(data) WHERE value::TEXT <> 'null' AND value::TEXT <> '""'
    ) t;
$body$ 
LANGUAGE sql;

-- json_add_prefix function adds luName as prefix to json
CREATE OR REPLACE FUNCTION json_add_prefix(luName text, data json = '{}') RETURNS json IMMUTABLE AS 
$body$
declare
  result json;
begin
	if(data IS NOT NULL AND luName IS NOT NULL) then EXECUTE 'SELECT (''{''||string_agg(to_json(''' || luName || '.' || ''' || key)||'':''||value, '','')||''}'')::json FROM (SELECT * FROM json_each('''||json_cast(data)||''')) t;' into result;
		return result;
	else return '{}';
	end if;

end;
$body$ 
LANGUAGE plpgsql;

-- json_append function receives two jsons and merges them to one
CREATE OR REPLACE FUNCTION json_append(data json, first_data json = '{}', second_data json = '{}') RETURNS json IMMUTABLE AS 
$body$
    SELECT ('{'||string_agg(to_json(key)||':'||value, ',')||'}')::json
    FROM (
        SELECT * FROM json_each(data)
        UNION ALL
        SELECT * FROM json_each(first_data)
        UNION ALL
        SELECT * FROM json_each(second_data)
    ) t;
$body$ 
LANGUAGE sql;

			     
-- param_values function returns json with param names as keys and param values as values
CREATE OR REPLACE FUNCTION public.param_values(
parentlu text,
entity_id text,
table_name text,
env text,
cols text,
child_arr text,
select_col text)
RETURNS SETOF json AS
$BODY$
BEGIN
RETURN QUERY EXECUTE
CASE WHEN EXISTS(SELECT 1 FROM tdm_lu_type_relation_eid WHERE source_env = env AND lu_type_1 = parentlu) THEN
'
SELECT row_to_json(allparams) as p from(
SELECT ' || cols ||' FROM public.' || lower(table_name) || ' WHERE entity_id in (
SELECT rel_base.'|| select_col || ' FROM ' || lower(parentlu) || '_params
LEFT JOIN ( SELECT * FROM tdm_lu_type_relation_eid
WHERE tdm_lu_type_relation_eid.lu_type_1 = ''' || parentlu || '''
AND tdm_lu_type_relation_eid.source_env = ''' || env || '''
AND (tdm_lu_type_relation_eid.lu_type_2 ' || child_arr || ')
AND tdm_lu_type_relation_eid.version_name = '''') rel_base
ON ' || lower(parentlu) || '_params.entity_id = rel_base.lu_type1_eid
WHERE ' || lower(parentlu) || '_params.source_environment = ''' || env || '''
AND lu_type1_eid='''|| entity_id || ''') AND source_environment = ''' || env || ''') allparams'

ELSE
'
SELECT row_to_json(allparams) as p from(
SELECT ' || cols ||' FROM public.' || lower(table_name) || ' WHERE entity_id in (
SELECT entity_id FROM ' || lower(parentlu) || '_params
WHERE ' || lower(parentlu) || '_params.source_environment = ''' || env || '''
AND entity_id='''|| entity_id || ''') AND source_environment = ''' || env || ''') allparams'
END;

END;
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100
ROWS 1000;
ALTER FUNCTION public.param_values(text, text, text, text, text, text, text)
OWNER TO tdm;
			     
CREATE OR REPLACE FUNCTION param_values(
	parentlu text,
	entity_id text,
	table_name text,
	env text,
	cols text,
	child_arr text,
	select_col text,
	lu_type2 text,
	schema text)
    RETURNS SETOF json 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$

DECLARE
 cnt integer := 0;
BEGIN
 EXECUTE format('SELECT 1 FROM %s.tdm_lu_type_relation_eid WHERE source_env =''%s'' AND lu_type_1 = ''%s'' AND lu_type1_eid = ''%s''', schema, env, parentlu, entity_id) into cnt;
RETURN QUERY EXECUTE
CASE 
WHEN lu_type2 IS NULL THEN 'SELECT ''{}''::json'
WHEN cnt = 1 THEN 
'
SELECT row_to_json(allparams) as p from(
SELECT ' || cols ||' FROM ' || schema|| '.' || lower(table_name) || ' WHERE entity_id in (
SELECT rel_base.'|| select_col || ' FROM ' || schema|| '.' || lower(parentlu) || '_params
LEFT JOIN ( SELECT * FROM ' || schema|| '.' || 'tdm_lu_type_relation_eid
WHERE tdm_lu_type_relation_eid.lu_type_1 = ''' || parentlu || '''
AND tdm_lu_type_relation_eid.source_env = ''' || env || '''
AND (tdm_lu_type_relation_eid.lu_type_2 ' || child_arr || ')
AND tdm_lu_type_relation_eid.version_name = '''') rel_base
ON ' || lower(parentlu) || '_params.entity_id = rel_base.lu_type1_eid
WHERE ' || lower(parentlu) || '_params.source_environment = ''' || env || '''
AND lu_type1_eid='''|| entity_id || ''') AND source_environment = ''' || env || ''') allparams'

 

ELSE
'
SELECT row_to_json(allparams) as p from(
SELECT ' || cols ||' FROM ' || schema|| '.' || lower(table_name) || ' WHERE entity_id in (
SELECT entity_id FROM ' || schema|| '.' || lower(parentlu) || '_params
WHERE ' || lower(parentlu) || '_params.source_environment = ''' || env || '''
AND entity_id='''|| entity_id || ''') AND source_environment = ''' || env || ''') allparams'
END;

END;
$BODY$;

ALTER FUNCTION param_values(text, text, text, text, text, text, text, text, text)
    OWNER TO tdm;

-- eval function executes received string expression as query
CREATE OR REPLACE FUNCTION eval(expression text) RETURNS void
as
$body$
declare
  result integer;
begin
  execute expression;
  return;
end;
$body$
LANGUAGE plpgsql;

