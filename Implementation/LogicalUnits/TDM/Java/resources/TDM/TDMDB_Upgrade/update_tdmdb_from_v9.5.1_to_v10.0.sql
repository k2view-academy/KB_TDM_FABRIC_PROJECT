-- TDM 10.0
SET search_path = ${@schema};
BEGIN;
  CREATE SEQUENCE IF NOT EXISTS ${@schema}.task_group_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
     
  CREATE SEQUENCE IF NOT EXISTS ${@schema}.permission_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE IF NOT EXISTS ${@schema}.task_groups (
    task_group_id bigint NOT NULL DEFAULT nextval('${@schema}.task_group_id_seq'::regclass),
    task_group_name text NOT NULL,
    task_group_desc text,
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    created_by text,
    CONSTRAINT task_groups_pkey PRIMARY KEY (task_group_id)
);
COMMIT;

CREATE UNIQUE INDEX IF NOT EXISTS TASKGROUPS_NAME_IX ON ${@schema}.task_groups (task_group_name);

CREATE TABLE IF NOT EXISTS ${@schema}.task_group_mapping (
    task_id bigint NOT NULL,
    task_group_id bigint NOT NULL,
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    created_by text,
    CONSTRAINT task_group_mapping_pkey PRIMARY KEY (task_id,task_group_id),
    CONSTRAINT fk_task_id FOREIGN KEY (task_id) REFERENCES ${@schema}.tasks(task_id),
    CONSTRAINT fk_task_group_id FOREIGN KEY (task_group_id) REFERENCES ${@schema}.task_groups(task_group_id)
);

CREATE TABLE IF NOT EXISTS ${@schema}.task_exe_permissions (
    permission_id bigint NOT NULL DEFAULT nextval('${@schema}.permission_id_seq'::regclass),
    task_id bigint NOT NULL,   
    permitted_user text NOT NULL,
    user_type text NOT NULL,
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    created_by text,
    CONSTRAINT task_exe_permissions_pkey PRIMARY KEY (permission_id),
    CONSTRAINT fk_task_id FOREIGN KEY (task_id) REFERENCES ${@schema}.tasks(task_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS task_exe_permissions_ix1 ON ${@schema}.task_exe_permissions (task_id, permitted_user);

CREATE TABLE IF NOT EXISTS ${@schema}.task_user_favorites (
    user_id text NOT NULL,
    favorite_item_type text NOT NULL,
    favorite_item_id bigint NOT NULL,
    CONSTRAINT task_user_favorites_pkey PRIMARY KEY (user_id,favorite_item_type,favorite_item_id)
);

CREATE TABLE IF NOT EXISTS ${@schema}.task_execution_prompt_text (
    task_type text NOT NULL,
    prompt_text text NOT NULL,
    CONSTRAINT task_execution_prompt_text_pkey PRIMARY KEY (task_type)
);

-- Table permission_groups_mapping
ALTER TABLE ${@schema}.permission_groups_mapping
add COLUMN IF NOT EXISTS can_create_tasks boolean NOT NULL DEFAULT true;

ALTER TABLE ${@schema}.tasks
add COLUMN IF NOT EXISTS task_override_fields JSONB DEFAULT '{}';

ALTER TABLE ${@schema}.tasks
ALTER COLUMN be_id DROP NOT NULL;


ALTER TABLE ${@schema}.tasks
DROP COLUMN IF EXISTS mask_sensitive_data;

ALTER TABLE ${@schema}.tasks
ALTER COLUMN environment_id DROP NOT NULL;

ALTER TABLE ${@schema}.tasks
ADD COLUMN IF NOT EXISTS env_name text;

ALTER TABLE ${@schema}.task_execution_list
ADD COLUMN IF NOT EXISTS env_name text;

ALTER TABLE ${@schema}.task_execution_summary
ADD COLUMN IF NOT EXISTS env_name text;

ALTER TABLE ${@schema}.tasks
ADD COLUMN IF NOT EXISTS enable_sequence_report boolean;

ALTER TABLE ${@schema}.tasks
ADD COLUMN IF NOT EXISTS statistics_report_flag text;

ALTER TABLE ${@schema}.tdm_be_exe_process 
ADD COLUMN IF NOT EXISTS lu_name text;

ALTER TABLE ${@schema}.tdm_be_exe_process DROP CONSTRAINT
    IF EXISTS be_exe_process_pkey;

DROP INDEX IF EXISTS ${@schema}.tdm_be_exe_process_ix1;

UPDATE ${@schema}.tdm_be_exe_process SET lu_name = '' WHERE lu_name is null;
ALTER TABLE ${@schema}.tdm_be_exe_process ADD CONSTRAINT
    be_exe_process_pkey PRIMARY KEY (process_id,be_id,process_type, lu_name);

CREATE UNIQUE INDEX IF NOT EXISTS tdm_be_exe_process_ix1 ON ${@schema}.tdm_be_exe_process (process_name, be_id, process_type, lu_name);

ALTER TABLE ${@schema}.tasks_exe_process 
ADD COLUMN IF NOT EXISTS lu_name text;

ALTER TABLE ${@schema}.tasks_exe_process DROP CONSTRAINT
    IF EXISTS tasks_exe_pkey;

UPDATE ${@schema}.tasks_exe_process SET lu_name = '' WHERE lu_name is null;
ALTER TABLE ${@schema}.tasks_exe_process ADD CONSTRAINT
    tasks_exe_pkey PRIMARY KEY (task_id, process_id,process_type, lu_name);

UPDATE ${@schema}.tasks t
SET env_name = e.environment_name
FROM ${@schema}.environments e
WHERE e.environment_id = t.environment_id;

UPDATE ${@schema}.task_execution_list t
SET env_name = e.environment_name
FROM ${@schema}.environments e
WHERE e.environment_id = t.environment_id;

UPDATE ${@schema}.task_execution_summary t
SET env_name = e.environment_name
FROM ${@schema}.environments e
WHERE e.environment_id = t.environment_id;

INSERT INTO ${@schema}.task_groups(task_group_name, task_group_desc, created_by) VALUES('General','General','system') ON CONFLICT DO NOTHING;

UPDATE ${@schema}.permission_groups_mapping SET can_create_tasks = 'true';

-- Update new field statistics_report_flag to ALL
update ${@schema}.tasks set statistics_report_flag = 'ALL' where statistics_report_flag is null;

-- Alter all character varying fields into text, the change was done in TDM 9.2, but without upgrade script for all fields.
DO $$
DECLARE
    v_sql TEXT;
BEGIN
    SELECT string_agg(
        'ALTER TABLE ' || quote_ident(table_schema) || '.' || quote_ident(table_name) ||
        ' ALTER COLUMN ' || quote_ident(column_name) || ' TYPE TEXT;',
        E'\n'
    )
    INTO v_sql
    FROM information_schema.columns
    WHERE table_schema = '${@schema}'
      AND table_name in ('activities', 'business_entities', 'environment_owners', 'environment_products', 
        'environment_role_users', 'environment_roles', 'environments', 'parameters', 'product_logical_units',
        'products', 'task_execution_list', 'tasks', 'tdm_be_env_exclusion_list', 'tdm_seq_mapping', 'task_execution_entities',
        'tasks_logical_units', 'task_ref_tables', 'task_ref_exe_stats', 'tdm_general_parameters', 'task_globals',
        'tdm_env_globals', 'task_execution_summary', 'task_exe_error_summary', 'task_exe_error_detailed', 'tdm_be_exe_process',
        'task_exe_stats_detailed', 'permission_groups_mapping', 'task_execution_override_attrs', 'tdm_reserved_entities',
        'tdm_generate_task_field_mappings', 'tdm_params_distinct_values', 'tdm_ai_gen_iid_mapping')
      AND data_type = 'character varying';

    IF v_sql IS NOT NULL THEN
        EXECUTE v_sql;
    END IF;
END;
$$;

INSERT INTO ${@schema}.task_groups(task_group_name, task_group_desc, created_by) VALUES('General','General','system') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_groups(task_group_name, task_group_desc, created_by) VALUES('Product predefined tasks','Product predefined tasks','system') ON CONFLICT DO NOTHING;

-- Map all existing tasks to 'General' group before inserting predefined tasks
INSERT INTO ${@schema}.task_group_mapping(task_id, task_group_id, creation_date, created_by)
SELECT DISTINCT t.task_id, tg.task_group_id, NOW(), 'admin'
FROM ${@schema}.tasks t, ${@schema}.task_groups tg
WHERE tg.task_group_name = 'General'
ON CONFLICT DO NOTHING;

-- Predefined tasks
INSERT INTO ${@schema}.tasks (
    be_id, environment_id, scheduler, delete_before_load, num_of_entities,
    selection_method, selection_param_value, custom_logic_lu_name, task_execution_status,
    task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by,
    task_status, task_title, parameters, refresh_reference_data, replace_sequences,
    source_environment_id, source_env_name, load_entity, task_type,
    scheduling_end_date, version_ind, retention_period_type, retention_period_value,
    selected_version_task_exe_id, selected_subset_task_exe_id, task_globals, selected_ref_version_task_exe_id,
    sync_mode, reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note,
    filterout_reserved, task_description, clone_ind, execution_mode,
    in_place_masking_ind, permission_group, fabric_roles,
    statistics_report_flag, enable_sequence_report,
    task_override_fields
)
VALUES (
    NULL, NULL, 'immediate', false, 0,
    'P', NULL, NULL, 'Active',
    'admin', NOW(), NOW(), 'admin',
    'Active', 'Extract entities', NULL, NULL, false,
    NULL, '', false, 'EXTRACT',
    NULL, false, 'Do Not Delete', -1,
    0, 0, false, 0,
    'ON', false, 'Days', 5, NULL,
    'NA', 'Extract entities', false, 'INHERITED',
    false, 'admin', 'admin',
    'ALL', true,
    '{"business_entity": {"is_editable": true, "field_connector": "be_name"}, "selection_method": {"random": {"is_editable": true}, "entity_list": {"is_editable": true}, "is_editable": true, "custom_logic": {"is_editable": true, "can_add_params": {"is_editable": true}}, "max_entities": {"is_editable": true}, "field_connector": "selection_method", "business_parameters": {"is_editable": true}}, "reservation_period": {"is_editable": false, "field_connector": "reservation_period"}, "source_environment": {"is_editable": true, "field_connector": "source_env_name"}, "target_environment": {"is_editable": false, "field_connector": "environment_name"}, "task_globals": {"is_editable": true, "field_connector": "task_globals"}, "retention_period": {"is_editable": true, "field_connector": "retention_period"}, "data_version_name": {"is_editable": true, "field_connector": "data_version_name"}}'
)
ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tasks (
    be_id, environment_id, scheduler, delete_before_load, num_of_entities,
    selection_method, selection_param_value, custom_logic_lu_name, task_execution_status,
    task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by,
    task_status, task_title, parameters, refresh_reference_data, replace_sequences,
    source_environment_id, source_env_name, load_entity, task_type,
    scheduling_end_date, version_ind, retention_period_type, retention_period_value,
    selected_version_task_exe_id, selected_subset_task_exe_id, task_globals, selected_ref_version_task_exe_id,
    sync_mode, reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note,
    filterout_reserved, task_description, clone_ind, execution_mode,
    in_place_masking_ind, permission_group, fabric_roles,
    statistics_report_flag, enable_sequence_report,
    task_override_fields
)
VALUES (
    NULL, NULL, 'immediate', false, 0,
    'P', NULL, NULL, 'Active',
    'admin', NOW(), NOW(), 'admin',
    'Active', 'Extract and Load entities', NULL, NULL, false,
    NULL, '', true, 'LOAD',
    NULL, false, 'Do Not Delete', -1,
    0, 0, false, 0,
    'ON', false, 'Days', 5, NULL,
    'OTHERS', 'Extract and Load entities', false, 'INHERITED',
    false, 'admin', 'admin',
    'ALL', true,
    '{"business_entity": {"is_editable": true, "field_connector": "be_name"}, "selection_method": {"random": {"is_editable": true}, "entity_list": {"is_editable": true}, "is_editable": true, "custom_logic": {"is_editable": true, "can_add_params": {"is_editable": true}}, "max_entities": {"is_editable": true}, "field_connector": "selection_method", "business_parameters": {"is_editable": true}}, "reservation_period": {"is_editable": false, "field_connector": "reservation_period"}, "source_environment": {"is_editable": true, "field_connector": "source_env_name"}, "target_environment": {"is_editable": true, "field_connector": "environment_name"}, "task_globals": {"is_editable": true, "field_connector": "task_globals"}, "retention_period": {"is_editable": true, "field_connector": "retention_period"}, "data_version_name": {"is_editable": true, "field_connector": "data_version_name"}}'
)
ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tasks (
    be_id, environment_id, scheduler, delete_before_load, num_of_entities,
    selection_method, selection_param_value, custom_logic_lu_name, task_execution_status,
    task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by,
    task_status, task_title, parameters, refresh_reference_data, replace_sequences,
    source_environment_id, source_env_name, load_entity, task_type,
    scheduling_end_date, version_ind, retention_period_type, retention_period_value,
    selected_version_task_exe_id, selected_subset_task_exe_id, task_globals, selected_ref_version_task_exe_id,
    sync_mode, reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note,
    filterout_reserved, task_description, clone_ind, execution_mode,
    in_place_masking_ind, permission_group, fabric_roles,
    statistics_report_flag, enable_sequence_report,
    task_override_fields
)
VALUES (
    NULL, NULL, 'immediate', false, 0,
    'P', NULL, NULL, 'Active',
    'admin', NOW(), NOW(), 'admin',
    'Active', 'Load entities', NULL, NULL, false,
    NULL, '', true, 'LOAD',
    NULL, false, 'Do Not Delete', -1,
    0, 0, false, 0,
    'OFF', false, 'Days', 5, NULL,
    'OTHERS', 'Load entities (load only)', false, 'INHERITED',
    false, 'admin', 'admin',
    'ALL', true,
    '{"business_entity": {"is_editable": true, "field_connector": "be_name"}, "selection_method": {"random": {"is_editable": true}, "entity_list": {"is_editable": true}, "is_editable": true, "custom_logic": {"is_editable": true, "can_add_params": {"is_editable": true}}, "max_entities": {"is_editable": true}, "field_connector": "selection_method", "business_parameters": {"is_editable": true}}, "reservation_period": {"is_editable": false, "field_connector": "reservation_period"}, "source_environment": {"is_editable": true, "field_connector": "source_env_name"}, "target_environment": {"is_editable": true, "field_connector": "environment_name"}, "task_globals": {"is_editable": true, "field_connector": "task_globals"}, "retention_period": {"is_editable": true, "field_connector": "retention_period"}, "data_version_name": {"is_editable": true, "field_connector": "data_version_name"}}'
)
ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tasks (
    be_id, environment_id, scheduler, delete_before_load, num_of_entities, 
    selection_method, selection_param_value, custom_logic_lu_name, task_execution_status, 
    task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by, 
    task_status, task_title, parameters, refresh_reference_data, replace_sequences, 
    source_environment_id, source_env_name, load_entity, task_type, 
    scheduling_end_date, version_ind, retention_period_type, retention_period_value, 
    selected_version_task_exe_id, selected_subset_task_exe_id, task_globals, selected_ref_version_task_exe_id, 
    sync_mode, reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note, 
    filterout_reserved, task_description, clone_ind, execution_mode, 
    in_place_masking_ind, permission_group, fabric_roles,
    statistics_report_flag, enable_sequence_report,
    task_override_fields
)
VALUES (
    NULL, -1, 'immediate', false, 0, 
    'GENERATE', NULL, NULL, 'Active', 
    'admin', NOW(), NOW(), 'admin', 
    'Active', 'Generate entities', NULL, NULL, false, 
    -1, 'Synthetic', false, 'GENERATE', 
    NULL, false, 'Do Not Delete', -1, 
    0, 0, false, 0, 
    'ON', false, 'Days', 5, NULL, 
    'NA', 'Generate entities (rule based)', false, 'INHERITED',
    false, 'admin', 'admin',
    'ALL', true,
    '{"business_entity": {"is_editable": true, "field_connector": "be_name"}, "selection_method": {"random": {"is_editable": false}, "entity_list": {"is_editable": false}, "is_editable": false, "custom_logic": {"is_editable": false, "can_add_params": {"is_editable": false}}, "generate_data_params": {"is_editable": true, "can_add_params": {"is_editable": true}}, "max_entities": {"is_editable": true}, "field_connector": "selection_method", "business_parameters": {"is_editable": false}}, "reservation_period": {"is_editable": false, "field_connector": "reservation_period"}, "source_environment": {"is_editable": false, "field_connector": "source_env_name"}, "target_environment": {"is_editable": false, "field_connector": "environment_name"}, "task_globals": {"is_editable": true, "field_connector": "task_globals"}, "retention_period": {"is_editable": true, "field_connector": "retention_period"}, "data_version_name": {"is_editable": true, "field_connector": "data_version_name"}}'
) 
ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tasks (
    be_id, environment_id, scheduler, delete_before_load, num_of_entities, 
    selection_method, selection_param_value, custom_logic_lu_name, task_execution_status, 
    task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by, 
    task_status, task_title, parameters, refresh_reference_data, replace_sequences, 
    source_environment_id, source_env_name, load_entity, task_type, 
    scheduling_end_date, version_ind, retention_period_type, retention_period_value, 
    selected_version_task_exe_id, selected_subset_task_exe_id, task_globals, 
    selected_ref_version_task_exe_id, sync_mode, reserve_ind, 
    reserve_retention_period_type, reserve_retention_period_value, reserve_note, 
    filterout_reserved, task_description, clone_ind, 
    execution_mode, in_place_masking_ind, permission_group, fabric_roles,
    statistics_report_flag, enable_sequence_report,
    task_override_fields
)
VALUES (
    NULL, NULL, 'immediate', false, 0, 
    'GENERATE', NULL, NULL, 'Active', 
    'admin', NOW(), NOW(), 'admin', 
    'Active', 'Generate and load entities', NULL, NULL, true,
    -1, 'Synthetic', true, 'LOAD',
    NULL, false, 'Do Not Delete', -1, 
    0, 0, false, 0, 'ON', false, 
    'Days', 5, NULL, 'OTHERS', 'Generate and load entities', false,
    'INHERITED', 
    false, 'admin', 'admin',
    'ALL', true,
    '{"business_entity": {"is_editable": true, "field_connector": "be_name"}, "selection_method": {"random": {"is_editable": false}, "entity_list": {"is_editable": false}, "is_editable": false, "custom_logic": {"is_editable": false, "can_add_params": {"is_editable": false}}, "generate_data_params": {"is_editable": true, "can_add_params": {"is_editable": true}}, "max_entities": {"is_editable": true}, "field_connector": "selection_method", "business_parameters": {"is_editable": false}}, "reservation_period": {"is_editable": false, "field_connector": "reservation_period"}, "source_environment": {"is_editable": false, "field_connector": "source_env_name"}, "target_environment": {"is_editable": true, "field_connector": "environment_name"}, "task_globals": {"is_editable": true, "field_connector": "task_globals"}, "retention_period": {"is_editable": true, "field_connector": "retention_period"}, "data_version_name": {"is_editable": true, "field_connector": "data_version_name"}}'
) 
ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tasks (
    be_id, environment_id, scheduler, delete_before_load, num_of_entities, 
    selection_method, selection_param_value, custom_logic_lu_name, task_execution_status, 
    task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by, 
    task_status, task_title, parameters, refresh_reference_data, replace_sequences, 
    source_environment_id, source_env_name, load_entity, task_type, 
    scheduling_end_date, version_ind, retention_period_type, retention_period_value, 
    selected_version_task_exe_id, selected_subset_task_exe_id, task_globals, selected_ref_version_task_exe_id, 
    sync_mode, reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note, 
    filterout_reserved, task_description, clone_ind, execution_mode, 
    in_place_masking_ind, permission_group, fabric_roles,
    statistics_report_flag, enable_sequence_report,
    task_override_fields
)
VALUES (
    NULL, NULL, 'immediate', true, 0, 
    'P', NULL, NULL, 'Active', 
    'admin', NOW(), NOW(), 'admin', 
    'Active', 'Delete entities', NULL, NULL, false, 
    NULL, '', false, 'DELETE', 
    NULL, false, 'Do Not Delete', -1, 
    0, 0, false, 0, 
    NULL, false, 'Days', 5, NULL, 
    'OTHERS', 'Delete entities', false, 'INHERITED',
    false, 'admin', 'admin',
    'ALL', true,
    '{"business_entity": {"is_editable": true, "field_connector": "be_name"}, "selection_method": {"random": {"is_editable": true}, "entity_list": {"is_editable": true}, "is_editable": true, "custom_logic": {"is_editable": true, "can_add_params": {"is_editable": true}}, "max_entities": {"is_editable": true}, "field_connector": "selection_method", "business_parameters": {"is_editable": true}}, "reservation_period": {"is_editable": false, "field_connector": "reservation_period"}, "source_environment": {"is_editable": false, "field_connector": "source_env_name"}, "target_environment": {"is_editable": true, "field_connector": "environment_name"}, "task_globals": {"is_editable": true, "field_connector": "task_globals"}, "retention_period": {"is_editable": true, "field_connector": "retention_period"}, "data_version_name": {"is_editable": true, "field_connector": "data_version_name"}}'
) 
ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tasks (
    be_id, environment_id, scheduler, delete_before_load, num_of_entities, 
    selection_method, selection_param_value, custom_logic_lu_name, task_execution_status, 
    task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by, 
    task_status, task_title, parameters, refresh_reference_data, replace_sequences, 
    source_environment_id, source_env_name, load_entity, task_type, 
    scheduling_end_date, version_ind, retention_period_type, retention_period_value, 
    selected_version_task_exe_id, selected_subset_task_exe_id, task_globals, selected_ref_version_task_exe_id, 
    sync_mode, reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note, 
    filterout_reserved, task_description, clone_ind, execution_mode, 
    in_place_masking_ind, permission_group, fabric_roles,
    statistics_report_flag, enable_sequence_report,
    task_override_fields
)
VALUES (
    NULL, NULL, 'immediate', false, 0, 
    'P', NULL, NULL, 'Active', 
    'admin', NOW(), NOW(), 'admin', 
    'Active', 'Reserve entities', NULL, NULL, false, 
    NULL, '', false, 'RESERVE', 
    NULL, false, 'Do Not Delete', -1, 
    0, 0, false, 0, 
    NULL, true, 'Days', 5, NULL, 
    'OTHERS', 'Reserve entities', false, 'INHERITED', 
    false, 'admin', 'admin',
    'ALL', true,
    '{"business_entity": {"is_editable": true, "field_connector": "be_name"}, "selection_method": {"random": {"is_editable": true}, "entity_list": {"is_editable": true}, "is_editable": true, "custom_logic": {"is_editable": true, "can_add_params": {"is_editable": true}}, "max_entities": {"is_editable": true}, "field_connector": "selection_method", "business_parameters": {"is_editable": true}}, "reservation_period": {"is_editable": true, "field_connector": "reservation_period"}, "source_environment": {"is_editable": false, "field_connector": "source_env_name"}, "target_environment": {"is_editable": true, "field_connector": "environment_name"}, "task_globals": {"is_editable": true, "field_connector": "task_globals"}, "retention_period": {"is_editable": true, "field_connector": "retention_period"}, "data_version_name": {"is_editable": true, "field_connector": "data_version_name"}}'
) 
ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tasks (
    be_id, environment_id, scheduler, delete_before_load, num_of_entities, 
    selection_method, selection_param_value, custom_logic_lu_name, task_execution_status, 
    task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by, 
    task_status, task_title, parameters, refresh_reference_data, replace_sequences, 
    source_environment_id, source_env_name, load_entity, task_type, 
    scheduling_end_date, version_ind, retention_period_type, retention_period_value, 
    selected_version_task_exe_id, selected_subset_task_exe_id, task_globals, selected_ref_version_task_exe_id, 
    sync_mode, reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note, 
    filterout_reserved, task_description, clone_ind, execution_mode, 
    in_place_masking_ind, permission_group, fabric_roles,
    statistics_report_flag, enable_sequence_report,
    task_override_fields
)
VALUES (
    NULL, NULL, 'immediate', false, 0, 
    'P', NULL, NULL, 'Active', 
    'admin', NOW(), NOW(), 'admin', 
    'Active', 'Clone entities', NULL, NULL, false, 
    NULL, '', true, 'LOAD', 
    NULL, false, 'Do Not Delete', -1, 
    0, 0, false, 0, 
    'ON', false, 'Days', 5, NULL, 
    'OTHERS', 'Clone entities', true, 'INHERITED',
    false, 'admin', 'admin',
    'ALL', true,
    '{"business_entity": {"is_editable": true, "field_connector": "be_name"}, "selection_method": {"random": {"is_editable": true}, "entity_list": {"is_editable": true}, "is_editable": true, "custom_logic": {"is_editable": true, "can_add_params": {"is_editable": true}}, "max_entities": {"is_editable": true}, "field_connector": "selection_method", "business_parameters": {"is_editable": true}}, "reservation_period": {"is_editable": false, "field_connector": "reservation_period"}, "source_environment": {"is_editable": true, "field_connector": "source_env_name"}, "target_environment": {"is_editable": true, "field_connector": "environment_name"}, "task_globals": {"is_editable": true, "field_connector": "task_globals"}, "retention_period": {"is_editable": true, "field_connector": "retention_period"}, "data_version_name": {"is_editable": true, "field_connector": "data_version_name"}}'
) 
ON CONFLICT DO NOTHING;

-- Map predefined tasks to 'Product predefined tasks' group
INSERT INTO ${@schema}.task_group_mapping (task_id, task_group_id, creation_date, created_by)
SELECT t.task_id, tg.task_group_id, NOW(), 'system'
FROM ${@schema}.tasks t, ${@schema}.task_groups tg
WHERE t.task_title IN ('Extract entities', 'Extract and Load entities', 'Load entities', 'Generate entities', 'Generate and load entities', 'Delete entities', 'Reserve entities', 'Clone entities')
  AND tg.task_group_name = 'Product predefined tasks'
ON CONFLICT DO NOTHING;

-- add execution permission for ALL on the predfinied tasks
INSERT INTO ${@schema}.task_exe_permissions (task_id, permitted_user, user_type,creation_date, created_by)
SELECT t.task_id, 'ALL', 'ID', NOW(), 'system'
FROM ${@schema}.tasks t
WHERE t.task_status = 'Active'
  AND t.task_title IN (
      'Extract entities',
      'Extract and Load entities',
      'Load entities',
      'Generate entities',
      'Generate and load entities',
      'Delete entities',
      'Reserve entities',
      'Clone entities'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM ${@schema}.task_exe_permissions p
      WHERE p.task_id         = t.task_id
        AND p.permitted_user  = 'ALL'
        AND p.user_type       = 'ID'
  );

INSERT INTO ${@schema}.tdm_general_parameters (param_name, param_value) VALUES ('MAX_DATE_RANGE_IN_MONTHS', 12) ON CONFLICT DO NOTHING;


-- insert values for task_execution_prompt_text
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Extract entities','Extract data by <be_name> from <source_env_name>. 
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Extract and load entities','Copy data by <be_name> from <source_env_name> to <environment_name>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Extract, load, and reserve entities','Copy data by <be_name> from <source_env_name> to <environment_name>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.
Reserve the entities for <reserve_retention_period_value> <reserve_retention_period_type>') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Load entities','Copy <source_env_name> data from the Test Data Store by <be_name> to <environment_name>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Load and reserve entities','Copy <source_env_name> data from the Test Data Store by <be_name> to <environment_name>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.
Reserve the entities for <reserve_retention_period_value> <reserve_retention_period_type>.') ON CONFLICT DO NOTHING;

-- entities + tables
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Extract entities + tables','Extract data by <be_name> and referential tables from <source_env_name>. 
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Extract and load entities + tables','Copy data by <be_name> and referential tables from <source_env_name> to <environment_name>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Extract, load, and reserve entities + tables','Copy data by <be_name> and referential tables from <source_env_name> to <environment_name>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.
Reserve the entities for <reserve_retention_period_value> <reserve_retention_period_type>.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Load entities + tables','Copy <source_env_name> data by <be_name> and referential tables from the Test Data to <environment_name>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Load and reserve entities + tables','Copy <source_env_name> data by <be_name> and referential tables from the Test Data to <environment_name>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.
Reserve the entities for <reserve_retention_period_value> <reserve_retention_period_type>.') ON CONFLICT DO NOTHING;

-- generate & AI - should be 10 entries
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Rule-based Generate entities','Generate <num_of_entities> synthetic <be_name> entities.
The entities are generated based on the following parameters:') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('AI-based Generate entities','Generate <num_of_entities> synthetic <be_name> entities.
The entities are generated based on the following AI training model:') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Rule-based Generate and load entities','Generate <num_of_entities> synthetic <be_name> entities and load them to <environment_name>.
The entities are generated based on the following parameters:') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Rule-based Generate load and reserve entities','Generate <num_of_entities> synthetic <be_name> entities and load them to <environment_name>.
The entities are generated based on the following parameters:
Reserve the entities for <reserve_retention_period_value> <reserve_retention_period_type>.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('AI-based Generate and load entities','Generate <num_of_entities> synthetic <be_name> entities and load them to <environment_name>.
The entities are generated based AI training model.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('AI-based Generate load and reserve entities','Generate <num_of_entities> synthetic <be_name> entities and load them to <environment_name>.
The entities are generated based AI training model.
Reserve the entities for <reserve_retention_period_value> <reserve_retention_period_type>.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Load generated entities (AI/rule based)','Copy pre-generated synthetic  <be_name> entities from the Test Data Store to <environment_name>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Load execution generation entities (AI/rule based)','Copy pre-generated synthetic  <be_name> entities from the Test Data Store to <environment_name> using data generation <generation_title> (created on <generation_creation_date>, <generation_number_of_entities> entities).') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Load and reserve generated entities (AI/rule based)','Copy pre-generated synthetic  <be_name> entities from the Test Data Store to <environment_name>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.
Reserve the entities for <reserve_retention_period_value> <reserve_retention_period_type>.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Training','Extract data by <be_name> from <source_env_name>. Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities. Run AI training on the extracted entities.') ON CONFLICT DO NOTHING;

-- basic
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Delete entities','Delete data by <be_name> from <environment_name>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Reserve entities','Reserve data by <be_name> in <environment_name> for <reserve_retention_period_value> <reserve_retention_period_type>.
Subset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Clone entities','Clone <be_name> data from <source_env_name> to <environment_name> 
Subset by <selection_method> using: <selection_param_value>. Clone the entity <num_of_entities> times.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Extract tables','Extract tables from <source_env_name>.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Extract and load tables','Copy tables from <source_env_name> to <environment_name>.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Load tables','Copy <source_env_name> tables from the Test Data Store to <environment_name>.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('In-place masking','Applying in-place data masking to selected tables in <environment_name>.') ON CONFLICT DO NOTHING;
INSERT INTO ${@schema}.task_execution_prompt_text(task_type, prompt_text) VALUES('Load data version entities','Load <scope> by <be_name> from <source_env_name> to <environment_name> using data snapshot <data_version_name> (created on <version_creation_date>).') ON CONFLICT DO NOTHING;
 
-- task_notes
CREATE SEQUENCE IF NOT EXISTS ${@schema}.task_notes_note_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE IF NOT EXISTS ${@schema}.task_notes (
    note_id          bigint NOT NULL DEFAULT nextval('${@schema}.task_notes_note_id_seq'::regclass),
    task_id          bigint NOT NULL,
    note_title       text NOT NULL,
    note_description text,
    note_date        timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT task_notes_pkey PRIMARY KEY (note_id),
    CONSTRAINT fk_task_notes_task_id FOREIGN KEY (task_id) REFERENCES ${@schema}.tasks(task_id)
);

CREATE INDEX IF NOT EXISTS task_notes_task_id_ix ON ${@schema}.task_notes (task_id);


-- table level changes

ALTER TABLE ${@schema}.task_ref_exe_stats
    ADD COLUMN IF NOT EXISTS interface_name text,
    ADD COLUMN IF NOT EXISTS schema_name text,
    ADD COLUMN IF NOT EXISTS execution_action text,
    ADD COLUMN IF NOT EXISTS table_order bigint,
    ADD COLUMN IF NOT EXISTS filter_parameters text;

UPDATE ${@schema}.task_ref_exe_stats es
SET interface_name = trt.interface_name,
    schema_name    = trt.schema_name,
    filter_parameters = trt.filter_parameters
FROM ${@schema}.task_ref_tables trt
WHERE es.task_ref_table_id = trt.task_ref_table_id
  AND es.task_id = trt.task_id;

UPDATE ${@schema}.task_ref_exe_stats set execution_action ='Extract & Load';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = '${@schema}'
          AND table_name = 'task_ref_exe_stats'
          AND column_name = 'job_uid'
    ) THEN
        ALTER TABLE ${@schema}.task_ref_exe_stats RENAME COLUMN job_uid TO batch_id;
    END IF;
END $$;


ALTER TABLE ${@schema}.task_ref_exe_stats
    DROP CONSTRAINT IF EXISTS task_ref_exe_stats_pkey;

ALTER TABLE ${@schema}.task_ref_exe_stats
    ADD CONSTRAINT task_ref_exe_stats_pkey
    PRIMARY KEY (task_id, task_execution_id, task_ref_table_id, execution_action);

ALTER TABLE ${@schema}.task_execution_override_attrs
    ADD COLUMN IF NOT EXISTS created_by_ai boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS draft boolean NOT NULL DEFAULT false;

 ALTER TABLE ${@schema}.tasks_logical_units
    ADD COLUMN IF NOT EXISTS override_fields JSONB DEFAULT '{"max_no_of_workers": false, "source_affinity": false, "target_affinity": false}';

ALTER TABLE IF EXISTS ${@schema}.task_globals
    ADD COLUMN IF NOT EXISTS is_editable boolean NOT NULL DEFAULT false;

ALTER TABLE IF EXISTS ${@schema}.task_execution_summary 
    ADD COLUMN IF NOT EXISTS task_type_label TEXT; 

UPDATE ${@schema}.tasks
SET refresh_reference_data = CASE
    WHEN selection_method = 'TABLES' OR EXISTS (
        SELECT 1 FROM ${@schema}.task_ref_tables trt WHERE trt.task_id = tasks.task_id
    ) THEN true
    ELSE false
END;

UPDATE ${@schema}.task_execution_summary tes
SET task_type_label = CASE
    WHEN t.in_place_masking_ind = true THEN 'In-place masking'
    WHEN tes.task_type = 'EXTRACT' THEN 'Extract'
    WHEN tes.task_type = 'LOAD' AND t.sync_mode <> 'OFF' AND t.delete_before_load = true THEN 'Extract & Delete & Load'
    WHEN tes.task_type = 'LOAD' AND tes.source_environment_id < 0 AND t.selection_method NOT IN ('GENERATE', 'AI_GENERATED') THEN 'Load SDG entities'
    WHEN tes.task_type = 'LOAD' AND t.selection_method = 'GENERATE' THEN 'Rule-based SDG & Load'
    WHEN tes.task_type = 'LOAD' AND t.sync_mode <> 'OFF' THEN 'Extract & Load'
    WHEN tes.task_type = 'LOAD' AND t.reserve_ind = true AND t.delete_before_load = false THEN 'Load & Reserve'
    WHEN tes.task_type = 'LOAD' AND t.reserve_ind = true AND t.delete_before_load = true THEN 'Load & Reserve & Delete'
    WHEN tes.task_type = 'LOAD' AND t.reserve_ind = false AND t.delete_before_load = true THEN 'Load & Delete'
    WHEN tes.task_type = 'LOAD' THEN 'Load'
    WHEN tes.task_type = 'DELETE' THEN 'Delete'
    WHEN tes.task_type = 'RESERVE' THEN 'Reserve'
    WHEN tes.task_type = 'TRAINING' THEN 'AI training'
    WHEN tes.task_type = 'GENERATE' THEN 'Rule-based generation'
    WHEN tes.task_type = 'AI_GENERATED' THEN 'AI-based generation'
    ELSE tes.task_type
END
FROM ${@schema}.tasks t
WHERE tes.task_id = t.task_id;

ALTER TABLE ${@schema}.tdm_generate_task_field_mappings
    ADD COLUMN IF NOT EXISTS is_editable boolean DEFAULT false;
