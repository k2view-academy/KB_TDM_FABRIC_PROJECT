-- Table task_execution_entities
ALTER TABLE ${@schema}.task_execution_entities
add COLUMN IF NOT EXISTS fabric_execution_id character varying(200);

ALTER TABLE ${@schema}.task_execution_entities
add COLUMN IF NOT EXISTS iid character varying(50)  NOT NULL DEFAULT '';

ALTER TABLE ${@schema}.task_execution_entities
add COLUMN IF NOT EXISTS source_env character varying(50) NOT NULL DEFAULT ''; 

ALTER TABLE ${@schema}.task_execution_entities
add COLUMN IF NOT EXISTS version_name character varying(200) NOT NULL DEFAULT '';

ALTER TABLE ${@schema}.task_execution_entities
add COLUMN IF NOT EXISTS version_datetime timestamp without time zone NOT NULL DEFAULT 'epoch';

update ${@schema}.task_execution_entities set iid = split_part(entity_id, '_', 2), source_env = split_part(entity_id, '_', 1) where iid = '';

update ${@schema}.task_execution_entities set iid = (select regexp_replace(iid,param_value, '') from ${@schema}.tdm_general_parameters where param_name = 'IID_OPEN_SEPARATOR')
where iid in (select iid from task_execution_entities, tdm_general_parameters where param_name = 'IID_OPEN_SEPARATOR' and strpos(iid, param_value) >0 );

update ${@schema}.task_execution_entities set iid = (select regexp_replace(iid,param_value, '') from ${@schema}.tdm_general_parameters where param_name = 'IID_CLOSE_SEPARATOR')
where iid in (select iid from task_execution_entities, tdm_general_parameters where param_name = 'IID_CLOSE_SEPARATOR' and strpos(iid, param_value) >0 );

update ${@schema}.task_execution_entities set version_name = split_part(entity_id, '_', 3), version_datetime = to_timestamp(split_part(entity_id, '_', 4), 'yyyyMMddhh24miss')
where (CHAR_LENGTH(entity_id) - CHAR_LENGTH(REPLACE(entity_id, '_', '')))/ CHAR_LENGTH('_') > 1 and version_name ='';

CREATE INDEX IF NOT EXISTS task_execution_entities_2ix ON ${@schema}.task_execution_entities (task_execution_id, lu_name, source_env, iid, version_name,version_datetime);

-- Table environment_roles
ALTER TABLE ${@schema}.environment_roles 
add COLUMN IF NOT EXISTS allowed_test_conn_failure boolean NOT NULL DEFAULT false;

-- Table tdm_lu_type_relation_eid
ALTER TABLE ${@schema}.tdm_lu_type_relation_eid DROP CONSTRAINT tdm_lu_type_relation_eid_pk;

ALTER TABLE ${@schema}.tdm_lu_type_relation_eid
add COLUMN IF NOT EXISTS version_name character varying(200) NOT NULL DEFAULT '';

ALTER TABLE ${@schema}.tdm_lu_type_relation_eid
add COLUMN IF NOT EXISTS version_datetime timestamp without time zone NOT NULL DEFAULT 'epoch';

ALTER TABLE ${@schema}.tdm_lu_type_relation_eid ADD CONSTRAINT tdm_lu_type_relation_eid_pk 
 PRIMARY KEY(source_env,lu_type_1,lu_type_2,lu_type1_eid,lu_type2_eid,version_name,version_datetime);

CREATE INDEX IF NOT EXISTS tdm_lu_type_relation_eid_1ix ON ${@schema}.tdm_lu_type_relation_eid (source_env,lu_type_1,lu_type1_eid,version_name,version_datetime);
CREATE INDEX IF NOT EXISTS tdm_lu_type_relation_eid_2ix ON ${@schema}.tdm_lu_type_relation_eid (source_env,lu_type_2,lu_type2_eid,version_name,version_datetime);

--Table tdm_lu_type_rel_tar_eid
ALTER TABLE ${@schema}.tdm_lu_type_rel_tar_eid DROP CONSTRAINT tdm_lu_type_relation_tar_eid_pk;

ALTER TABLE ${@schema}.tdm_lu_type_rel_tar_eid 
add COLUMN IF NOT EXISTS version_name character varying(200) NOT NULL DEFAULT '';

ALTER TABLE ${@schema}.tdm_lu_type_rel_tar_eid
add COLUMN IF NOT EXISTS version_datetime timestamp without time zone NOT NULL DEFAULT 'epoch';

update ${@schema}.tdm_lu_type_rel_tar_eid rel set version_name = (select distinct t.selected_version_task_name from tasks t, task_execution_list l 
where l.task_execution_id = rel.task_execution_id and l.task_id = t.task_id and t.selected_version_task_name is not null)
where rel.task_execution_id in (select l.task_execution_id from ${@schema}.tasks t, ${@schema}.task_execution_list l where l.task_id = t.task_id and t.selected_version_task_name is not null)
and rel.version_name = '';

update ${@schema}.tdm_lu_type_rel_tar_eid rel set version_datetime = (select distinct to_timestamp(t.selected_version_datetime, 'YYYYMMDDHH24MISS') from tasks t, task_execution_list l 
where l.task_execution_id = rel.task_execution_id and l.task_id = t.task_id and t.selected_version_task_name is not null)
where rel.task_execution_id in (select l.task_execution_id from ${@schema}.tasks t, ${@schema}.task_execution_list l where l.task_id = t.task_id and t.selected_version_task_name is not null)
and rel.version_datetime = 'epoch';

ALTER TABLE ${@schema}.tdm_lu_type_rel_tar_eid ADD CONSTRAINT tdm_lu_type_rel_tar_eid_pk
 PRIMARY KEY(target_env, task_execution_id, lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid,version_name,version_datetime);

CREATE INDEX IF NOT EXISTS tdm_lu_type_rel_tar_eid_1ix ON ${@schema}.tdm_lu_type_rel_tar_eid (task_execution_id, lu_type_2, lu_type2_eid);
CREATE INDEX IF NOT EXISTS tdm_lu_type_rel_tar_eid_2ix ON ${@schema}.tdm_lu_type_rel_tar_eid (lu_type_1, lu_type1_eid);
CREATE INDEX IF NOT EXISTS tdm_lu_type_rel_tar_eid_3ix ON ${@schema}.tdm_lu_type_rel_tar_eid (lu_type_2, lu_type2_eid);

-- New Table task_execution_summary
CREATE TABLE IF NOT EXISTS ${@schema}.task_execution_summary
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
  CONSTRAINT task_execution_summary_pkey PRIMARY KEY (task_execution_id)
);
-- DROP INDEX ${@schema}.task_exec_summary_ix1;

CREATE INDEX task_exec_summary_ix1
  ON ${@schema}.task_execution_summary  (task_id);

-- New Table task_exe_stats_summary
CREATE TABLE IF NOT EXISTS ${@schema}.task_exe_stats_summary
(
	task_execution_id bigint NOT NULL,
	lu_name character varying(200) NOT NULL,
	creation_date timestamp without time zone,
	table_name character varying(100),
	source_count character varying(20),
	target_count character varying(20),
	diff character varying(20),
	results character varying(20),
	CONSTRAINT task_exe_stats_summary_pkey PRIMARY KEY (task_execution_id,lu_name,table_name)
);

-- New Table task_exe_error_summary
CREATE TABLE IF NOT EXISTS ${@schema}.task_exe_error_summary
(
	task_execution_id bigint NOT NULL,
	etl_execution_id numeric(10,0),
	lu_name character varying(200) NOT NULL,
	error_category character varying(200) NOT NULL,
	error_code character varying(100) NOT NULL,
	error_msg character varying(4000) NOT NULL,
	creation_date timestamp without time zone,
	no_of_records numeric(10,0),
	no_of_entities numeric(10,0),
	CONSTRAINT task_exe_error_summary_pkey PRIMARY KEY (task_execution_id,etl_execution_id,lu_name,error_category,error_code,error_msg)
);
