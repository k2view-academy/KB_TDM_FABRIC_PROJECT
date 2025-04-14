
-- Set the Schema in case it is not the default one
SET search_path = ${@schema};

-- Table: ${@schema}.activities.

--DROP TABLE IF EXISTS ${@schema}.activities;

CREATE TABLE IF NOT EXISTS ${@schema}.activities
(
    date timestamp without time zone,
    action text,
    entity text,
    username text,
    description text,
    user_id text
);

-- Table: ${@schema}.business_entities

--DROP TABLE IF EXISTS ${@schema}.business_entities;

CREATE TABLE IF NOT EXISTS ${@schema}.business_entities
(
    be_name text NOT NULL,
    be_description text,
    be_id bigint NOT NULL DEFAULT nextval('business_entities_be_id_seq'::regclass),
    be_created_by text,
    be_creation_date timestamp without time zone,
    be_last_updated_date timestamp without time zone,
    be_last_updated_by text,
    be_status text,
    execution_mode text DEFAULT 'HORIZONTAL',
    CONSTRAINT business_entities_pkey PRIMARY KEY (be_id)
);

Create UNIQUE INDEX IF NOT EXISTS BE_NAME_FOR_ACTIVE_IX ON ${@schema}.Business_Entities (be_name) where be_status = 'Active';

-- Table: ${@schema}.environment_owners

--DROP TABLE IF EXISTS ${@schema}.environment_owners;

CREATE TABLE IF NOT EXISTS ${@schema}.environment_owners
(
    environment_id bigint NOT NULL,
    user_type text NOT NULL,
    user_name text,
    user_id text,
    CONSTRAINT check_env_owner_type CHECK (user_type = 'ID' OR user_type = 'GROUP')
);

-- Table: ${@schema}.environment_products

--DROP TABLE IF EXISTS ${@schema}.environment_products;

CREATE TABLE IF NOT EXISTS ${@schema}.environment_products
(
    environment_product_id bigint NOT NULL DEFAULT nextval('environment_product_id_seq'::regclass),
    environment_id bigint NOT NULL,
    product_id bigint NOT NULL,
    product_version text,
    created_by text,
    creation_date timestamp without time zone,
    last_updated_date timestamp without time zone,
    last_updated_by text,
    status text NOT NULL,
    data_center_name text,
    enable_product Boolean DEFAULT 'true',
    CONSTRAINT environment_products_pkey PRIMARY KEY (environment_product_id)
);

Create UNIQUE INDEX IF NOT EXISTS ENV_PROD_FOR_ACTIVE_IX ON ${@schema}.environment_products (environment_id, product_id) where status = 'Active';

-- Table: ${@schema}.environment_role_users

--DROP TABLE IF EXISTS ${@schema}.environment_role_users;

CREATE TABLE IF NOT EXISTS ${@schema}.environment_role_users
(
    environment_id bigint NOT NULL,
    role_id bigint NOT NULL,
    user_type text NOT NULL,
    username text,
    user_id text,
   CONSTRAINT check_user_type CHECK (user_type = 'ALL' OR user_type = 'ID' OR user_type = 'GROUP') 
);

   
Create UNIQUE INDEX IF NOT EXISTS ENV_ROLE_USER_IX ON ${@schema}.environment_role_users (environment_id, user_id);

-- Table: ${@schema}.environment_roles

-- 31-Dec-18- add allow_read, allow_write, and allowed_number_of_entities_to_read fields to ${@schema}.environment_roles 

--DROP TABLE IF EXISTS ${@schema}.environment_roles;

CREATE TABLE IF NOT EXISTS ${@schema}.environment_roles
(
    environment_id bigint NOT NULL,
    role_name text NOT NULL,
    role_description text,
    allowed_delete_before_load boolean NOT NULL DEFAULT false,
    allowed_creation_of_synthetic_data boolean NOT NULL DEFAULT false,
    allowed_random_entity_selection boolean NOT NULL DEFAULT false,
    allowed_request_of_fresh_data boolean NOT NULL DEFAULT false,
    allowed_task_scheduling boolean NOT NULL DEFAULT false,
    allowed_number_of_entities_to_copy bigint NOT NULL DEFAULT 1000,
    role_id bigint NOT NULL DEFAULT nextval('environment_roles_role_id_seq'::regclass),
    role_created_by text,
    role_creation_date timestamp without time zone,
    role_last_updated_date timestamp without time zone,
    role_expiration_date timestamp without time zone,
    role_last_updated_by text,
    role_status text,
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

Create UNIQUE INDEX IF NOT EXISTS ENV_ROLE_FOR_ACTIVE_IX ON ${@schema}.environment_roles  (environment_id, role_name) where role_status = 'Active';

-- Table: ${@schema}.environments

-- 31-Dec-18- add fabric_environment_name field to environments

--DROP TABLE IF EXISTS ${@schema}.environments;

CREATE TABLE IF NOT EXISTS ${@schema}.environments
(
    environment_name text NOT NULL,
    environment_description text,
    environment_expiration_date date,
    environment_point_of_contact_first_name text,
    environment_point_of_contact_last_name text,
    environment_point_of_contact_phone1 text,
    environment_point_of_contact_phone2 text,
    environment_point_of_contact_email text,
    environment_id bigint NOT NULL DEFAULT nextval('environments_environment_id_seq'::regclass),
    environment_created_by text,
    environment_creation_date timestamp without time zone,
    environment_last_updated_date timestamp without time zone,
    environment_last_updated_by text,
    environment_status text,
    allow_write boolean NOT NULL DEFAULT true,
    allow_read boolean NOT NULL DEFAULT false,
    sync_mode text DEFAULT 'ON',
    mask_sensitive_data boolean NOT NULL DEFAULT true, -- TDM 8.1
    CONSTRAINT environments_pkey PRIMARY KEY (environment_id)
);

Create UNIQUE INDEX IF NOT EXISTS ENV_NAME_FOR_ACTIVE_IX ON ${@schema}.environments (environment_name) where environment_status = 'Active'; 

-- Table: ${@schema}.parameters

--DROP TABLE IF EXISTS ${@schema}.parameters;

CREATE TABLE IF NOT EXISTS ${@schema}.parameters
(
    be_id bigint NOT NULL,
    param_name text NOT NULL,
    param_type text,
    valid_values text[],
    min_value numeric[],
    max_value numeric[],
    CONSTRAINT parameters_pkey PRIMARY KEY (be_id, param_name)
);

-- Table: ${@schema}.product_logical_units

--DROP TABLE IF EXISTS ${@schema}.product_logical_units;

CREATE TABLE IF NOT EXISTS ${@schema}.product_logical_units
(
    lu_name text NOT NULL,
    lu_description text,
    be_id bigint NOT NULL,
    lu_parent_id bigint,
    lu_id bigint NOT NULL DEFAULT nextval('product_logical_units_lu_id_seq'::regclass),
    product_name text,
    lu_parent_name text,
    product_id bigint,
    CONSTRAINT product_logical_units_pkey PRIMARY KEY (be_id,lu_id)
);

-- Table: ${@schema}.products

--DROP TABLE IF EXISTS ${@schema}.products;

CREATE TABLE IF NOT EXISTS ${@schema}.products
(
    product_name text NOT NULL,
    product_description text,
    product_vendor text,
    product_versions text,
    product_id bigint NOT NULL DEFAULT nextval('products_product_id_seq'::regclass),
    product_created_by text,
    product_creation_date timestamp without time zone,
    product_last_updated_date timestamp without time zone,
    product_last_updated_by text,
    product_status text,
    CONSTRAINT products_pkey PRIMARY KEY (product_id)
   --, CONSTRAINT products_product_name_key UNIQUE (product_name)
);

Create UNIQUE INDEX IF NOT EXISTS PROD_NAME_FOR_ACTIVE_IX ON ${@schema}.products (product_name) where product_status = 'Active';

-- Table: ${@schema}.task_execution_list

-- 31-DEC-18- add new fields to task_execution_list- fabric_execution_id, version_datetime, and version_expiration_date
-- 17-FEB-19- add task type field
-- 11-JAN-24- drop column version_datetime, add column version_task_execution_id (TDM9)

--DROP TABLE IF EXISTS ${@schema}.task_execution_list;

CREATE TABLE IF NOT EXISTS ${@schema}.task_execution_list
(
    task_id bigint NOT NULL, 
    task_type text,
    task_execution_id bigint NOT NULL,
    creation_date timestamp without time zone,
    be_id bigint,
    environment_id bigint NOT NULL,
    product_id bigint,
    product_version text COLLATE pg_catalog."default",
    execution_status text COLLATE pg_catalog."default",
    start_execution_time timestamp without time zone,
    end_execution_time timestamp without time zone,
    num_of_processed_entities numeric(10),
    num_of_copied_entities numeric(10),
    num_of_failed_entities numeric(10),
    data_center_name text,
    lu_id bigint NOT NULL default 0,
    num_of_processed_ref_tables numeric(10,0),
    num_of_copied_ref_tables numeric(10,0),
    num_of_failed_ref_tables numeric(10,0),
    parent_lu_id bigint,
    source_env_name text, 
    source_environment_id bigint, 
    task_executed_by text,
    fabric_execution_id text,
	subset_task_execution_id bigint DEFAULT 0,
    version_task_execution_id bigint DEFAULT 0,
    expiration_date timestamp without time zone,
    synced_to_fabric boolean DEFAULT false, 
    updated_by text, 
    clean_redis boolean DEFAULT false, -- TDM 5.5
    process_id bigint NOT NULL default 0, -- IDM 7.0.1
    execution_note text, -- TDM 7.4
    source_product_version text, -- TDM 7.5.2
    entity_inclusion_query TEXT, -- TDM 9.1 params coupling 
    CONSTRAINT task_execution_list_pkey PRIMARY KEY (task_execution_id, lu_id, process_id)
);

-- TDM 7.2 - Remove the unique index as it prevents rerunning the same task with different overriden attributes.
--Create UNIQUE INDEX IF NOT EXISTS TASK_EXEC_IX on ${@schema}.task_execution_list(task_id, lu_id, process_id) where upper(execution_status) IN ('RUNNING','EXECUTING','STARTED','PENDING','PAUSED', 'STARTEXECUTIONREQUESTED');
Create INDEX IF NOT EXISTS TASK_EXEC_IX2 ON ${@schema}.task_execution_list (task_execution_id, process_id); 

-- Table: ${@schema}.tasks
-- Tali E.- 10-May-17- remove limitation of 1000 chars from selection_param_value and exclusion_list fields

-- 31-Dec-18- add task_type, version_ind, retention_period_type, retention_period_value, selected_version_task_name, selected_version_datetime, scheduling_end_date

-- 11-JAN-24-drop column entity_exclusion_list,
-- drop column fabric_environment_name,
-- drop column selected_version_task_name,
-- drop column selected_version_datetime,
-- drop column selected_ref_version_task_name,
-- drop column selected_ref_version_datetime (TDM9)

--DROP TABLE IF EXISTS ${@schema}.tasks;

CREATE TABLE IF NOT EXISTS ${@schema}.tasks
(
    task_id bigint NOT NULL DEFAULT nextval('tasks_task_id_seq'::regclass),
    task_title text NOT NULL,
    task_status text DEFAULT 'Active',
    task_execution_status text DEFAULT 'Active',
    num_of_entities bigint,
    environment_id bigint NOT NULL,
    be_id bigint NOT NULL,
    selection_method text NOT NULL,
    selection_param_value text,
    custom_logic_lu_name text,
    parameters text,
    refresh_reference_data boolean,
    delete_before_load boolean NOT NULL DEFAULT false,
    replace_sequences boolean,
    scheduler text,
    task_created_by text,
    task_creation_date timestamp without time zone,
    task_last_updated_date timestamp without time zone,
    task_last_updated_by text,
    source_env_name text NOT NULL,
    source_environment_id bigint, 
    load_entity boolean,
    task_type text NOT NULL,
    version_ind  boolean NOT NULL DEFAULT false,
    retention_period_type text,
    retention_period_value numeric,
    selected_version_task_exe_id bigint DEFAULT 0, 
    selected_subset_task_exe_id bigint DEFAULT 0, 
    scheduling_end_date timestamp without time zone, 
    selected_ref_version_task_exe_id bigint DEFAULT 0,  
    task_globals boolean,
    sync_mode text,
    reserve_ind boolean NOT NULL DEFAULT false,
    reserve_retention_period_type text COLLATE pg_catalog."default",
    reserve_retention_period_value numeric,
    reserve_note text, -- TDM 7.5.2
    filterout_reserved TEXT DEFAULT 'NA', --TDM 9.2
    mask_sensitive_data boolean default true, -- TDM 8.1
    task_description text,
    clone_ind boolean NOT NULL DEFAULT false,
    execution_mode text DEFAULT 'INHERITED',
    enable_execution boolean DEFAULT 'true',
    CONSTRAINT tasks_pkey PRIMARY KEY (task_id)
);

Create UNIQUE INDEX IF NOT EXISTS TASK_NAME_FOR_ACTIVE_IX on ${@schema}.tasks (task_title) where task_status = 'Active';

-- Table: ${@schema}.tdm_be_env_exclusion_list

--DROP TABLE IF EXISTS ${@schema}.tdm_be_env_exclusion_list;

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_be_env_exclusion_list
(
  be_id bigint,
  environment_id bigint,
  exclusion_list text,
  requested_by text,
  update_date timestamp without time zone,
  created_by text,
  updated_by text,
  be_env_exclusion_list_id integer NOT NULL DEFAULT nextval('tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq'::regclass),
  creation_date timestamp without time zone,
  CONSTRAINT tdm_be_env_exclusion_list_be_env_exclusion_list_id_pk PRIMARY KEY (be_env_exclusion_list_id)
);

Create UNIQUE INDEX IF NOT EXISTS BE_ENV_EXCLUSION_LIST_IX on ${@schema}.tdm_be_env_exclusion_list (BE_ID,ENVIRONMENT_ID,REQUESTED_BY);

-- Table: ${@schema}.tdm_seq_mapping

--DROP TABLE IF EXISTS ${@schema}.tdm_seq_mapping;

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_seq_mapping
(
  task_execution_id      bigint NOT NULL,
  lu_type text NOT NULL,
  source_env      text NOT NULL,
  entity_target_id        text,
  seq_name        text,
  table_name      text,
  column_name     text,
  source_id       text,
  target_id       text,
  is_instance_id  text,
  entity_sequence bigint
);

Create INDEX IF NOT EXISTS TDM_SEQ_MAPPING_IX on ${@schema}.tdm_seq_mapping (task_execution_id,lu_type,source_env);

-- Table: ${@schema}.task_execution_entities

-- DROP TABLE IF EXISTS ${@schema}.task_execution_entities;
-- 11-JAN-24- drop column version_datetime, add column version_task_execution_id (TDM9)

CREATE TABLE IF NOT EXISTS ${@schema}.task_execution_entities
(
  task_execution_id bigint NOT NULL,
  lu_name text NOT NULL,
  entity_id text NOT NULL,
  target_entity_id text,
  creation_date timestamp without time zone,
  entity_end_time timestamp without time zone,
  entity_start_time timestamp without time zone,
  env_id text,
  execution_status text,
  id_type text,
  fabric_execution_id text, -- TDM 6.1
  iid text  NOT NULL DEFAULT '', -- TDM 6.1
  source_env text NOT NULL DEFAULT '', -- TDM 6.1
  version_task_execution_id bigint DEFAULT 0, --TDM 9.0
  subset_task_execution_id bigint DEFAULT 0, --TDM 9.0
  fabric_get_time bigint,
  total_processing_time bigint,
  clone_no text DEFAULT '0',
  parent_lu_name text, -- TDM 9.3
  parent_entity_id text, -- TDM 9.3
  parent_target_entity_id text, -- TDM 9.3
  root_lu_name text, -- TDM 8.1
  root_entity_id text, -- TDM 8.0
  root_target_entity_id text, -- TDM 9.3
  CONSTRAINT task_execution_entities_pkey PRIMARY KEY (task_execution_id, lu_name, entity_id, clone_no, root_entity_id, root_target_entity_id)
);

CREATE TABLE IF NOT EXISTS ${@schema}.tasks_logical_units
(
  task_id bigint NOT NULL,
  lu_id  bigint NOT NULL,
  lu_name text,
  CONSTRAINT tasks_logical_units_pkey PRIMARY KEY (task_id, lu_name)
);

-- Table: ${@schema}.task_ref_tables

--DROP TABLE IF EXISTS ${@schema}.task_ref_tables;

CREATE TABLE IF NOT EXISTS ${@schema}.task_ref_tables
(
  task_ref_table_id bigint NOT NULL DEFAULT nextval('tasks_ref_table_id_seq'::regclass),
  task_id bigint NOT NULL, 
  ref_table_name text,
  lu_name text,
  schema_name text,
  interface_name text,
  update_date timestamp without time zone,
  table_filter text,
  filter_type text,
  target_table_prefix text,
  target_table_suffix text,
  version_task_execution_id bigint default 0,
  version_task_name text,
  gui_filter text,
  filter_parameters text,
  filter_fields text,
  CONSTRAINT task_ref_tables_pkey PRIMARY KEY (task_ref_table_id) 
);

-- Table: ${@schema}.task_ref_exe_stats

--DROP TABLE IF EXISTS ${@schema}.task_ref_exe_stats;

CREATE TABLE IF NOT EXISTS ${@schema}.task_ref_exe_stats
(
  task_id bigint NOT NULL,
  task_execution_id bigint NOT NULL,
  task_ref_table_id bigint,
  ref_table_name text,
  job_uid text,
  update_date timestamp without time zone,
  start_time timestamp without time zone,
  end_time timestamp without time zone,
  execution_status text, 
  number_of_records_to_process numeric(10,0),
  number_of_processed_records numeric(10,0),
  error_msg text,
  updated_by text,
  table_filter text,
  filter_type text		
  );

Create INDEX IF NOT EXISTS task_ref_exe_stats_IX1 on ${@schema}.task_ref_exe_stats(task_execution_id);
Create INDEX IF NOT EXISTS task_ref_exe_stats_IX2 on ${@schema}.task_ref_exe_stats(task_execution_id, execution_status);
Create INDEX IF NOT EXISTS task_ref_exe_stats_IX3 on ${@schema}.task_ref_exe_stats(task_execution_id, task_ref_table_id, execution_status);

-- Table: ${@schema}.tdm_general_parameters

-- DROP TABLE IF EXISTS ${@schema}.tdm_general_parameters

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_general_parameters
(
  param_name text NOT NULL,
  param_value text,
  CONSTRAINT tdm_general_parameters_pk PRIMARY KEY (param_name)
);

INSERT INTO ${@schema}.tdm_general_parameters(
            param_name, param_value)
    select 'cleanup_retention_period', '0.25'  
where not exists (select 1 from ${@schema}.tdm_general_parameters where param_name = 'cleanup_retention_period');

INSERT INTO ${@schema}.tdm_general_parameters(
            param_name, param_value)
     select 'tdm_gui_params','{"retentionDefaultPeriod":{"units":"Do Not Delete","value":-1},"reservationDefaultPeriod":{"units":"Days","value":5},"versioningRetentionPeriod":{"units":"Days","value":5,"allow_doNotDelete":True},"versioningRetentionPeriodForTesters":{"units":"Days","value":5,"allow_doNotDelete":False},"permissionGroups":["admin","owner","tester"],"retentionPeriodTypes":[{"name":"Minutes","units":0.00069444444},{"name":"Hours","units":0.04166666666},{"name":"Days","units":1},{"name":"Weeks","units":7},{"name":"Years","units":365}],"reservationPeriodTypes":[{"name":"Minutes","units":0.00069444444},{"name":"Hours","units":0.04166666666},{"name":"Days","units":1},{"name":"Weeks","units":7},{"name":"Years","units":365}],"enable_reserve_by_params":False}'
where not exists (select 1 from ${@schema}.tdm_general_parameters where param_name = 'tdm_gui_params');

INSERT INTO ${@schema}.tdm_general_parameters(
	   param_name, param_value) 
    select 'TDM_VERSION', '9.3.1' 
where not exists (select 1 from ${@schema}.tdm_general_parameters where param_name = 'TDM_VERSION');

INSERT INTO ${@schema}.tdm_general_parameters(
        param_name, param_value)
    VALUES ('FOOTER_TEXT', 'Copyright K2view') ON CONFLICT DO NOTHING;  

insert into ${@schema}.tdm_general_parameters(
		param_name, param_value) 
	select 'MAX_RESERVATION_DAYS_FOR_TESTER', 90 
where not exists (select 1 from ${@schema}.tdm_general_parameters where param_name = 'MAX_RESERVATION_DAYS_FOR_TESTER');

insert into ${@schema}.tdm_general_parameters(
		param_name, param_value) 
	select 'MAX_RETENTION_DAYS_FOR_TESTER', 90 
where not exists (select 1 from ${@schema}.tdm_general_parameters where param_name = 'MAX_RETENTION_DAYS_FOR_TESTER');

insert into ${@schema}.tdm_general_parameters (
        param_name, param_value) 
    select 'TABLE_DEFAULT_DISTRIBUTION_MIN', 1 
where not exists (select 1 from ${@schema}.tdm_general_parameters where param_name = 'TABLE_DEFAULT_DISTRIBUTION_MIN');

insert into ${@schema}.tdm_general_parameters (
        param_name, param_value)
    select 'TABLE_DEFAULT_DISTRIBUTION_MAX', 3
where not exists (select 1 from ${@schema}.tdm_general_parameters where param_name = 'TABLE_DEFAULT_DISTRIBUTION_MAX');

INSERT INTO ${@schema}.tdm_general_parameters(param_name, param_value)
    VALUES ('ENABLE_PARAMETERS_AUTO_WIDTH', 'false') ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tdm_general_parameters(
        param_name, param_value)
    VALUES ('PARAMS_COUPLING', 'false') ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tdm_general_parameters(
        param_name, param_value)
    VALUES ('ADD_LU_NAME_TO_PARAM_NAME', 'false') ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tdm_general_parameters (param_name, param_value)
VALUES ('ENABLE_TASK_LU_EDITING_FOR_TESTERS', 'true') ON CONFLICT DO NOTHING;
-- Table: ${@schema}.task_globals

--DROP TABLE IF EXISTS ${@schema}.task_globals;

CREATE TABLE IF NOT EXISTS ${@schema}.task_globals
(
  task_id bigint NOT NULL,
  global_name text,
  global_value text
);

Create INDEX IF NOT EXISTS task_globals_ix on ${@schema}.task_globals(task_id);

-- Table: ${@schema}.tdm_env_globals

--DROP TABLE IF EXISTS ${@schema}.tdm_env_globals;

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_env_globals
(
   ENVIRONMENT_ID bigint ,
   GLOBAL_NAME text,
   GLOBAL_VALUE text,
   UPDATE_DATE timestamp without time zone,
   UPDATED_BY text
);

create UNIQUE INDEX IF NOT EXISTS ENV_ID_GLOBAL_NAME_IX on ${@schema}.tdm_env_globals (ENVIRONMENT_ID,GLOBAL_NAME);

-- New Table task_execution_summary, TDM 6.1
-- 11-JAN-24- drop column version_datetime, add column version_task_execution_id (TDM9)
--DROP TABLE IF EXISTS ${@schema}.task_execution_summary;
CREATE TABLE IF NOT EXISTS ${@schema}.task_execution_summary
(
  task_execution_id bigint NOT NULL,
  task_id bigint NOT NULL,
  task_type text,
  creation_date timestamp without time zone,
  be_id bigint,
  environment_id bigint NOT NULL,
  execution_status text,
  start_execution_time timestamp without time zone,
  end_execution_time timestamp without time zone,
  tot_num_of_processed_root_entities numeric(10,0),
  tot_num_of_copied_root_entities numeric(10,0),
  tot_num_of_failed_root_entities numeric(10,0),
  tot_num_of_processed_ref_tables numeric(10,0),
  tot_num_of_copied_ref_tables numeric(10,0),
  tot_num_of_failed_ref_tables numeric(10,0),
  source_env_name text,
  source_environment_id bigint,
  task_executed_by text,
  version_task_execution_id bigint DEFAULT 0,
  subset_task_execution_id bigint DEFAULT 0, --TDM 9.0
  expiration_date timestamp without time zone,
  update_date timestamp without time zone,
  tot_num_of_processed_post_executions numeric(10,0),
  tot_num_of_succeeded_post_executions numeric(10,0),
  tot_num_of_failed_post_executions numeric(10,0),
  tot_num_of_processed_pre_executions numeric(10,0),
  tot_num_of_succeeded_pre_executions numeric(10,0),
  tot_num_of_failed_pre_executions numeric(10,0),
  CONSTRAINT task_execution_summary_pkey PRIMARY KEY (task_execution_id)
);
-- DROP INDEX IF EXISTS ${@schema}.task_exec_summary_ix1;

CREATE INDEX IF NOT EXISTS task_exec_summary_ix1
  ON ${@schema}.task_execution_summary  (task_id);

-- New Table task_exe_stats_summary, TDM 6.1
--DROP TABLE IF EXISTS ${@schema}.task_exe_stats_summary;
--CREATE TABLE IF NOT EXISTS ${@schema}.task_exe_stats_summary
--(
--	task_execution_id bigint NOT NULL,
--	lu_name text NOT NULL,
--	creation_date timestamp without time zone,
--	table_name text,
--	source_count text,
--	target_count text,
--	diff text,
--	results text,
--	CONSTRAINT task_exe_stats_summary_pkey PRIMARY KEY (task_execution_id,lu_name,table_name)
--);

-- New Table task_exe_error_summary, TDM 6.1
--DROP TABLE IF EXISTS ${@schema}.task_exe_error_summary;
CREATE TABLE IF NOT EXISTS ${@schema}.task_exe_error_summary
(
	task_execution_id bigint NOT NULL,
	etl_execution_id numeric(10,0),
	lu_name text NOT NULL,
	error_category text NOT NULL,
	error_code text NOT NULL,
	error_msg text NOT NULL,
	creation_date timestamp without time zone,
	no_of_records numeric(10,0),
	no_of_entities numeric(10,0),
	CONSTRAINT task_exe_error_summary_pkey PRIMARY KEY (task_execution_id,etl_execution_id,lu_name,error_category,error_code,error_msg)
);

-- New Table task_exe_error_detailed, TDM 6.1.1
--DROP TABLE IF EXISTS ${@schema}.task_exe_error_detailed;
CREATE TABLE IF NOT EXISTS ${@schema}.task_exe_error_detailed
(
	task_execution_id bigint NOT NULL,
	lu_name text NOT NULL,
	entity_id text NOT NULL,
	iid text NOT NULL,
	target_entity_id text NOT NULL,
	error_category text NOT NULL,
	error_code text,
	error_message text NOT NULL,
	creation_date timestamp without time zone NOT NULL DEFAULT now(),
	flow_name text,
	stage_name text,
	actor_name text,
	actor_parameters text
);

-- Index: task_exe_error_detailed_1ix

-- DROP INDEX IF EXISTS ${@schema}.task_exe_error_detailed_1ix;

CREATE INDEX IF NOT EXISTS task_exe_error_detailed_1ix ON ${@schema}.task_exe_error_detailed (task_execution_id, lu_name, target_entity_id);

-- Support Post Execution Process, TDM 7.0.1
--  Support Pre Execution Process, TDM 9.0 
-- Table ${@schema}.tdm_be_exe_process
--DROP TABLE IF EXISTS ${@schema}.tdm_be_exe_process;
CREATE TABLE IF NOT EXISTS ${@schema}.tdm_be_exe_process (
	process_id bigint NOT NULL DEFAULT nextval('exe_process_id_seq'::regclass),
	process_name text,
    process_type TEXT,
	process_description text,
	be_id bigint,
	execution_order integer NOT NULL,
	CONSTRAINT be_exe_process_pkey PRIMARY KEY (process_id,be_id,process_type)
);

CREATE UNIQUE INDEX IF NOT EXISTS tdm_be_exe_process_ix1 ON ${@schema}.tdm_be_exe_process (process_name, be_id, process_type);

-- Table ${@schema}.tasks_exe_process
--DROP TABLE IF EXISTS ${@schema}.tasks_exe_process;
CREATE TABLE IF NOT EXISTS ${@schema}.tasks_exe_process (
	task_id bigint NOT NULL,
	process_id bigint NOT NULL,
	process_name text NOT NULL,
	execution_order integer NOT NULL,
    process_type TEXT,
    parameters TEXT,
	CONSTRAINT tasks_exe_pkey PRIMARY KEY (task_id, process_id)
);

-- Table ${@schema}.task_exe_stats_detailed
--DROP TABLE IF EXISTS ${@schema}.task_exe_stats_detailed;
CREATE TABLE IF NOT EXISTS ${@schema}.task_exe_stats_detailed
(
    task_execution_id bigint NOT NULL,
    lu_name text  NOT NULL,
    entity_id text NOT NULL,
    target_entity_id text NOT NULL,
    table_name text NOT NULL,
    stage_name text,
    flow_name text,
    actor_name text,
    creation_date timestamp without time zone,
    source_count text,
    target_count text,
    diff text,
    suppressed_error_count text,
    results text
);

-- DROP INDEX IF EXISTS ${@schema}.task_exe_stats_detailed_1ix;
CREATE INDEX IF NOT EXISTS task_exe_stats_detailed_1ix ON ${@schema}.task_exe_stats_detailed (task_execution_id, lu_name, target_entity_id);

-- Table ${@schema}.permission_groups_mapping; - TDM 7.1
-- DROP TABLE IF EXISTS ${@schema}.permission_groups_mapping;

CREATE TABLE IF NOT EXISTS ${@schema}.permission_groups_mapping
(
    description text,
    fabric_role text NOT NULL,
    permission_group text NOT NULL,
    created_by text NOT NULL,
    updated_by text NOT NULL,
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
	update_date) 
select'Initial mapping for admin user', 'admin', 'admin', 'admin', 'admin', NOW(), NOW()  
where not exists (select 1 from ${@schema}.permission_groups_mapping where fabric_role = 'admin');


-- Table: ${@schema}.task_execution_override_attrs - TDM 7.2

-- DROP TABLE IF EXISTS ${@schema}.task_execution_override_attrs;

CREATE TABLE IF NOT EXISTS ${@schema}.task_execution_override_attrs
(
	task_id bigint NOT NULL,
	task_execution_id bigint NOT NULL,
	override_parameters json NOT NULL,
	CONSTRAINT task_execution_override_attrs_pkey PRIMARY KEY (task_execution_id, task_id)
);

-- Table: ${@schema}.tdm_reserved_entities - TDM 7.4
--DROP TABLE IF EXISTS ${@schema}.tdm_reserved_entities;

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_reserved_entities
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

--- Table: ${@schema}.tdm_generate_task_field_mappings - TDM 8.0
--DROP TABLE IF EXISTS ${@schema}.tdm_generate_task_field_mappings;

CREATE TABLE IF NOT EXISTS  ${@schema}.tdm_generate_task_field_mappings
(
    task_id bigint NOT NULL,
    param_name text COLLATE pg_catalog."default",
    param_type text COLLATE pg_catalog."default" NOT NULL,
    param_value text COLLATE pg_catalog."default",
	param_order bigint,
    CONSTRAINT tdm_generate_task_field_mappings_pkey PRIMARY KEY (task_id, param_name)
);

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_params_distinct_values -- New Table TDM 8.1
(
    source_environment text NOT NULL,
    lu_name text NOT NULL,
    field_name text NOT NULL,
    number_of_values bigint,
    field_values text[],
    is_numeric boolean,
    min_value text,
    max_value text,
    field_type text,
    CONSTRAINT tdm_params_distinct_values_pkey PRIMARY KEY (source_environment, lu_name, field_name)
);

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_ai_gen_iid_mapping (
 task_execution_id bigint,
 lu_name text,
 env_name text,
 generated_iid text,
 imported_lui text,
 root_lu_name text,
 root_generated_iid text,
 root_imported_lui text,
 CONSTRAINT gen_mapping_pkey PRIMARY KEY (lu_name,generated_iid, imported_lui,root_lu_name,root_generated_iid,root_imported_lui)
);
-- utils functions (working with parameters)
-- json_cast function adds changes the format of the json data
CREATE OR REPLACE FUNCTION ${@schema}.json_cast(data json) RETURNS json IMMUTABLE AS 
$body$
    SELECT ('{'|| string_agg(to_json(UPPER(key)) || ':' || '"' || replace(regexp_split_to_array(replace(regexp_replace(value::text, '\[(.*)\]', '\1'), '"',''), ',')::text, '"','') || '"', ',') || '}')::json
    FROM (
        SELECT * FROM json_each(data) WHERE value::TEXT <> 'null' AND value::TEXT <> '""'
    ) t;
$body$ 
LANGUAGE sql;

-- json_add_prefix function adds luName as prefix to json
CREATE OR REPLACE FUNCTION ${@schema}.json_add_prefix(luName text, data json = '{}') RETURNS json IMMUTABLE AS 
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
CREATE OR REPLACE FUNCTION ${@schema}.json_append(data json, first_data json = '{}', second_data json = '{}') RETURNS json IMMUTABLE AS 
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

-- eval function executes received string expression as query
CREATE OR REPLACE FUNCTION ${@schema}.eval(expression text) RETURNS void
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

INSERT INTO ${@schema}.environments (environment_name, environment_description, environment_expiration_date, environment_point_of_contact_first_name, 
	environment_point_of_contact_last_name, environment_point_of_contact_phone1, environment_point_of_contact_phone2, environment_point_of_contact_email, 
	environment_id,environment_created_by, environment_creation_date, environment_last_updated_date, environment_last_updated_by, environment_status, allow_write, 
	allow_read, sync_mode,mask_sensitive_data) 
	VALUES ('Synthetic','This is the synthetic environment.',
        NULL,NULL,NULL,NULL,NULL,NULL,-1,'admin',NOW(),NOW(),'admin','Active',false,true,'FORCE', false) ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.environment_role_users(environment_id, role_id, user_type, username, user_id)VALUES (-1, -1, 'ID', 'ALL', '-1') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.environment_roles(environment_id, role_name, role_description, allowed_delete_before_load, allowed_creation_of_synthetic_data, 
	allowed_random_entity_selection, allowed_request_of_fresh_data, allowed_task_scheduling, allowed_number_of_entities_to_copy, role_id, role_created_by, 
	role_creation_date, role_last_updated_date, role_expiration_date, role_last_updated_by, role_status, allowed_refresh_reference_data, allowed_replace_sequences, 
	allow_read, allow_write, allowed_number_of_entities_to_read, allowed_entity_versioning, allowed_test_conn_failure, allowed_number_of_reserved_entities)
	VALUES (-1,'Synthetic','Role for Synethetic Environment',false,false,false,false,false,0,-1,'admin',NOW(),NOW(),NULL,'admin','Active',
	false,false,true,false,1000,false,false,0) ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.environments (environment_name, environment_description, environment_expiration_date, environment_point_of_contact_first_name, 
	environment_point_of_contact_last_name, environment_point_of_contact_phone1, environment_point_of_contact_phone2, environment_point_of_contact_email, 
	environment_id,environment_created_by, environment_creation_date, environment_last_updated_date, environment_last_updated_by, environment_status, allow_write, 
	allow_read, sync_mode,mask_sensitive_data) 
	VALUES ('AI','This is the AI Generationa and Training environment.',
        NULL,NULL,NULL,NULL,NULL,NULL,-2,'admin',NOW(),NOW(),'admin','Active',true,true,'OFF', false) ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.environment_role_users(environment_id, role_id, user_type, username, user_id)VALUES (-2, -2, 'ID', 'ALL', '-1') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.environment_roles(environment_id, role_name, role_description, allowed_delete_before_load, 
	allowed_creation_of_synthetic_data,allowed_random_entity_selection,allowed_request_of_fresh_data,
	allowed_task_scheduling,allowed_number_of_entities_to_copy, role_id, role_created_by, role_creation_date, 
	role_last_updated_date,role_last_updated_by,role_status,allowed_refresh_reference_data, allowed_replace_sequences, 
	allow_read, allow_write,allowed_number_of_entities_to_read, allowed_entity_versioning, allowed_test_conn_failure, 
	allowed_number_of_reserved_entities) VALUES(-2,'AI','Role for AI Environment',false,false,false,false,false,1000,-2,
	'admin',NOW(),NOW(),'admin','Active',false,false,true,true,1000,false,false,1000) ON CONFLICT DO NOTHING;