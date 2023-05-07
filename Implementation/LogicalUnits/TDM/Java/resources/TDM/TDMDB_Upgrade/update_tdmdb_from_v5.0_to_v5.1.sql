
DROP SEQUENCE IF EXISTS ${@schema}.tasks_ref_table_id_seq;

CREATE SEQUENCE ${@schema}.tasks_ref_table_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS selected_ref_version_task_name character varying(200);

ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS selected_ref_version_datetime character varying(20);

ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS selected_ref_version_task_exe_id bigint;

ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS task_globals boolean;

ALTER TABLE ${@schema}.environment_product_interfaces
ALTER COLUMN db_password TYPE varchar(200);

ALTER TABLE ${@schema}.PRODUCT_LOGICAL_UNITS 
ADD COLUMN IF NOT EXISTS lu_dc_name character varying(200);

ALTER TABLE ${@schema}.task_execution_list 
ADD COLUMN IF NOT EXISTS updated_by character varying(100);

CREATE TABLE IF NOT EXISTS ${@schema}.task_ref_tables
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

CREATE TABLE IF NOT EXISTS ${@schema}.task_ref_exe_stats
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
  error_msg character varying(500), 
  updated_by character varying(100)   
  );

Create INDEX IF NOT EXISTS task_ref_exe_stats_IX1 on ${@schema}.task_ref_exe_stats(task_execution_id);
Create INDEX IF NOT EXISTS task_ref_exe_stats_IX2 on ${@schema}.task_ref_exe_stats(task_execution_id, execution_status);
Create INDEX IF NOT EXISTS task_ref_exe_stats_IX3 on ${@schema}.task_ref_exe_stats(task_execution_id, task_ref_table_id, execution_status);

-- Table: ${@schema}.tdm_general_parameters

-- DROP TABLE IF EXISTS ${@schema}.tdm_general_parameters

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_general_parameters
(
  param_name character varying(200) NOT NULL,
  param_value character varying(200),
  CONSTRAINT tdm_general_parameters_pk PRIMARY KEY (param_name)
);

INSERT INTO ${@schema}.tdm_general_parameters(
            param_name, param_value)
    VALUES ('cleanup_active_ind', 'true');

INSERT INTO ${@schema}.tdm_general_parameters(
            param_name, param_value)
    VALUES ('cleanup_retention_period', '2');
    
--INSERT INTO ${@schema}.tdm_general_parameters(
--            param_name, param_value)
--    VALUES ('iid_close_separator', '>>>');

--INSERT INTO ${@schema}.tdm_general_parameters(
--            param_name, param_value)
--    VALUES ('iid_open_separator', '<<<');

CREATE TABLE IF NOT EXISTS ${@schema}.task_globals
(
  task_id bigint NOT NULL,
  global_name character varying(200),
  global_value character varying(200)
);

Create INDEX IF NOT EXISTS task_globals_ix on ${@schema}.task_globals(task_id);
