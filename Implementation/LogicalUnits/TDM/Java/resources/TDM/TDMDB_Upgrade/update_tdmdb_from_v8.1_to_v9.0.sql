ALTER TABLE ${@schema}.task_execution_list ALTER COLUMN task_executed_by TYPE text;
ALTER TABLE ${@schema}.task_execution_summary ALTER COLUMN task_executed_by TYPE text;

-- Add new environment for AI
INSERT INTO ${@schema}.environments(environment_name, environment_description, environment_expiration_date, 
	environment_point_of_contact_first_name, environment_point_of_contact_last_name, environment_point_of_contact_phone1, 
	environment_point_of_contact_phone2, environment_point_of_contact_email, environment_id, environment_created_by, 
	environment_creation_date, environment_last_updated_date, environment_last_updated_by, environment_status, allow_write, 
	allow_read, sync_mode,mask_sensitive_data) VALUES('AI','This is the AI environment.',NULL,NULL,NULL,NULL,NULL,NULL,-2,
	'admin',NOW(),NOW(),'admin','Active',true ,true,'FORCE',false) ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.environment_role_users(environment_id, role_id, user_type, username, user_id) VALUES(-2,-2,'ID','ALL','-2') ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.environment_roles(environment_id, role_name, role_description, allowed_delete_before_load, 
	allowed_creation_of_synthetic_data,allowed_random_entity_selection,allowed_request_of_fresh_data,
	allowed_task_scheduling,allowed_number_of_entities_to_copy, role_id, role_created_by, role_creation_date, 
	role_last_updated_date,role_last_updated_by,role_status,allowed_refresh_reference_data, allowed_replace_sequences, 
	allow_read, allow_write,allowed_number_of_entities_to_read, allowed_entity_versioning, allowed_test_conn_failure, 
	allowed_number_of_reserved_entities) VALUES(-2,'AI','Role for AI Environment',false,false,false,false,false,1000,-2,
	'admin',NOW(),NOW(),'admin','Active',false,false,true,true,1000,false,false,1000) ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.environment_products(environment_product_id, environment_id, product_id, product_version, created_by, creation_date, 
	last_updated_date, last_updated_by, status)(select nextval('environment_product_id_seq'::regclass),-2,product_id,'AI',
	'admin',NOW(),NOW(),'admin','Active' from products where product_status = 'Active') ON CONFLICT DO NOTHING;
 
-- Remove redundant fields from tasks,task table
ALTER table ${@schema}.tasks
DROP COLUMN IF EXISTS entity_exclusion_list,
DROP COLUMN IF EXISTS fabric_environment_name,
DROP COLUMN IF EXISTS selected_version_task_name,
DROP COLUMN IF EXISTS selected_version_datetime,
DROP COLUMN IF EXISTS selected_ref_version_task_name,
DROP COLUMN IF EXISTS selected_ref_version_datetime,
ADD COLUMN IF NOT EXISTS task_description text,
ADD COLUMN IF NOT EXISTS custom_logic_lu_name text,
ADD COLUMN IF NOT EXISTS clone_ind boolean NOT NULL DEFAULT false,
ADD COLUMN IF NOT EXISTS selected_subset_task_exe_id bigint DEFAULT 0,
ALTER COLUMN selected_ref_version_task_exe_id SET DEFAULT 0,
ALTER COLUMN selected_version_task_exe_id SET DEFAULT 0;

-- Update previous records to have value zero when seletced version is NULL
UPDATE ${@schema}.tasks
SET selected_ref_version_task_exe_id=0 WHERE selected_ref_version_task_exe_id is NULL;

UPDATE ${@schema}.tasks
SET selected_version_task_exe_id=0 WHERE selected_version_task_exe_id is NULL;

-- Update the custom logic in selection param value :
UPDATE ${@schema}.tasks
SET 
    custom_logic_lu_name = SPLIT_PART(selection_param_value, '#', 1),
    selection_param_value = SPLIT_PART(selection_param_value, '#', 2)
WHERE 
    selection_param_value LIKE '%#%';

-- Adjust tables : replace column version_datetime with version task_execution_id.

ALTER table ${@schema}.task_execution_list
DROP COLUMN IF EXISTS version_datetime,
DROP COLUMN IF EXISTS fabric_environment_name,
ADD COLUMN IF NOT EXISTS version_task_execution_id bigint DEFAULT 0,
ADD COLUMN IF NOT EXISTS subset_task_execution_id bigint DEFAULT 0;

UPDATE ${@schema}.task_execution_list 
SET version_task_execution_id =
CASE 
    WHEN tasks.selected_ref_version_task_exe_id <> 0 THEN tasks.selected_ref_version_task_exe_id
    ELSE tasks.selected_version_task_exe_id
  END
FROM ${@schema}.tasks
WHERE task_execution_list.task_id = tasks.task_id;

ALTER TABLE ${@schema}.task_execution_entities
DROP COLUMN IF EXISTS version_datetime,
ADD COLUMN IF NOT EXISTS version_task_execution_id bigint DEFAULT 0, 
ADD COLUMN IF NOT EXISTS subset_task_execution_id bigint DEFAULT 0;

UPDATE ${@schema}.task_execution_entities 
SET version_task_execution_id = (SELECT version_task_execution_id FROM ${@schema}.task_execution_list 
WHERE task_execution_list.task_execution_id = task_execution_entities.task_execution_id::bigint limit 1);

ALTER TABLE ${@schema}.tdm_lu_type_relation_eid
DROP CONSTRAINT IF EXISTS tdm_lu_type_relation_eid_pk,
ADD COLUMN IF NOT EXISTS version_task_execution_id bigint DEFAULT 0,
ADD COLUMN IF NOT EXISTS subset_task_execution_id bigint DEFAULT 0,
DROP COLUMN IF EXISTS version_name cascade,
DROP COLUMN IF EXISTS version_datetime;

WITH task_execution_info AS (
  SELECT DISTINCT parent.version_task_execution_id,
                  parent.iid AS parent_iid,
                  child.iid AS child_iid,
                  child.lu_name AS child_lu_name,
                  parent.lu_name AS parent_lu_name
  FROM ${@schema}.task_execution_entities parent, ${@schema}.task_execution_entities child, ${@schema}.tdm_lu_type_relation_eid rel
  WHERE parent.lu_name = rel.lu_type_1 
    AND parent.source_env = rel.source_env 
    AND parent.iid = rel.lu_type1_eid 
    AND child.task_execution_id = parent.task_execution_id 
    AND child.clone_no = parent.clone_no 
    AND child.lu_name = rel.lu_type_2
    AND child.source_env = rel.source_env 
    AND child.iid = rel.lu_type2_eid
)
UPDATE ${@schema}.tdm_lu_type_relation_eid rel
SET version_task_execution_id = task_execution_info.version_task_execution_id
FROM task_execution_info
WHERE rel.lu_type_1 = task_execution_info.parent_lu_name
  AND rel.lu_type1_eid = task_execution_info.parent_iid
  AND rel.lu_type_2 = task_execution_info.child_lu_name
  AND rel.lu_type2_eid = task_execution_info.child_iid;

ALTER TABLE ${@schema}.tdm_lu_type_relation_eid
ALTER COLUMN version_task_execution_id SET NOT NULL,
ALTER COLUMN version_task_execution_id SET DEFAULT 0,
ADD CONSTRAINT tdm_lu_type_relation_eid_pk PRIMARY KEY 
    (source_env, lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid, creation_date, version_task_execution_id);

ALTER TABLE ${@schema}.task_execution_summary
DROP COLUMN IF EXISTS version_datetime,
DROP COLUMN IF EXISTS fabric_environment_name,
ADD COLUMN IF NOT EXISTS version_task_execution_id bigint DEFAULT 0,
ADD COLUMN IF NOT EXISTS subset_task_execution_id bigint DEFAULT 0;

UPDATE ${@schema}.task_execution_summary 
SET version_task_execution_id = (SELECT version_task_execution_id FROM ${@schema}.task_execution_list 
WHERE task_execution_list.task_execution_id = task_execution_summary.task_execution_id limit 1); 

DROP INDEX IF EXISTS ${@schema}.task_execution_entities_2ix;
DROP INDEX IF EXISTS ${@schema}.tdm_lu_type_relation_eid_1ix;
DROP INDEX IF EXISTS ${@schema}.tdm_lu_type_relation_eid_2ix;

CREATE INDEX IF NOT EXISTS task_execution_entities_2ix ON ${@schema}.task_execution_entities (task_execution_id, lu_name, source_env, iid, version_task_execution_id);
CREATE INDEX IF NOT EXISTS tdm_lu_type_relation_eid_1ix ON ${@schema}.tdm_lu_type_relation_eid (source_env,lu_type_1,lu_type1_eid,version_task_execution_id); 
CREATE INDEX IF NOT EXISTS tdm_lu_type_relation_eid_2ix ON ${@schema}.tdm_lu_type_relation_eid (source_env,lu_type_2,lu_type2_eid,version_task_execution_id);
 
ALTER TABLE ${@schema}.task_ref_tables ADD COLUMN IF NOT EXISTS table_filter text;
ALTER TABLE ${@schema}.task_ref_tables ADD COLUMN IF NOT EXISTS filter_type text;
ALTER TABLE ${@schema}.task_ref_tables ADD COLUMN IF NOT EXISTS target_table_prefix text;
ALTER TABLE ${@schema}.task_ref_tables ADD COLUMN IF NOT EXISTS target_table_suffix text;
ALTER TABLE ${@schema}.task_ref_tables ADD COLUMN IF NOT EXISTS version_task_execution_id bigint default 0;
ALTER TABLE ${@schema}.task_ref_tables ADD COLUMN IF NOT EXISTS version_task_name text;
ALTER TABLE ${@schema}.task_ref_tables ADD COLUMN IF NOT EXISTS gui_filter text;
ALTER TABLE ${@schema}.task_ref_tables ADD COLUMN IF NOT EXISTS filter_parameters text;

ALTER TABLE IF EXISTS ${@schema}.tasks_post_exe_process RENAME TO tasks_exe_process;
ALTER TABLE ${@schema}.tasks_exe_process ADD COLUMN IF NOT EXISTS process_type TEXT;

UPDATE ${@schema}.tasks_exe_process SET process_type='post';

ALTER TABLE IF EXISTS ${@schema}.tdm_be_post_exe_process RENAME TO tdm_be_exe_process;
ALTER TABLE ${@schema}.tdm_be_exe_process ADD COLUMN IF NOT EXISTS process_type TEXT;

UPDATE ${@schema}.tdm_be_exe_process SET process_type='post';

DROP INDEX IF EXISTS ${@schema}.tdm_be_exe_process_ix1;
CREATE UNIQUE INDEX IF NOT EXISTS tdm_be_exe_process_ix1 ON ${@schema}.tdm_be_exe_process (process_name, be_id, process_type);

ALTER TABLE ${@schema}.task_execution_summary
ADD COLUMN IF NOT EXISTS tot_num_of_processed_pre_executions numeric(10,0),
ADD COLUMN IF NOT EXISTS tot_num_of_succeeded_pre_executions numeric(10,0),
ADD COLUMN IF NOT EXISTS tot_num_of_failed_pre_executions numeric(10,0);

ALTER INDEX IF EXISTS ${@schema}.post_exe_process_id_seq RENAME TO exe_process_id_seq;

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

ALTER TABLE ${@schema}.tdm_params_distinct_values DROP CONSTRAINT IF EXISTS tdm_params_distinct_values_pkey;
truncate table ${@schema}.tdm_params_distinct_values;
ALTER TABLE ${@schema}.tdm_params_distinct_values ADD COLUMN IF NOT EXISTS source_environment text NOT NULL;
ALTER TABLE ${@schema}.tdm_params_distinct_values ADD CONSTRAINT tdm_params_distinct_values_pkey PRIMARY KEY (source_environment, lu_name, field_name);

ALTER TABLE ${@schema}.product_logical_units
DROP CONSTRAINT IF EXISTS product_logical_units_pkey,
ADD CONSTRAINT product_logical_units_pkey PRIMARY KEY (be_id,lu_id);

ALTER TABLE ${@schema}.task_execution_summary
RENAME COLUMN version_expiration_date To expiration_date;
ALTER TABLE ${@schema}.task_execution_list
RENAME COLUMN version_expiration_date To expiration_date;