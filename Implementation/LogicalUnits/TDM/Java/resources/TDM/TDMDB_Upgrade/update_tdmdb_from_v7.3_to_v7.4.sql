-- Update TDM version
update ${@schema}.tdm_general_parameters set param_value = '7.4.0' where param_name = 'TDM_VERSION';
insert into ${@schema}.tdm_general_parameters (param_name, param_value) values ('MAX_RESERVATION_DAYS_FOR_TESTER', 10);

-- Alter long VARCHAR fields to TEXT
alter table ${@schema}.TASK_REF_EXE_STATS alter column error_msg type text;
alter table ${@schema}.business_entities alter column be_description  type text;
alter table ${@schema}.environment_roles alter column role_description type text;
alter table ${@schema}.environments alter column environment_description type text;
alter table ${@schema}.product_logical_units alter column lu_description type text;
alter table ${@schema}.products alter column product_description type text;
alter table ${@schema}.tdm_be_env_exclusion_list alter column exclusion_list type text;
alter table ${@schema}.task_exe_error_summary alter column error_msg type text;
alter table ${@schema}.task_exe_error_detailed alter column actor_parameters type text;
alter table ${@schema}.tdm_be_post_exe_process alter column process_name type text;
alter table ${@schema}.tdm_be_post_exe_process alter column process_description type text;
alter table ${@schema}.tasks_post_exe_process alter column process_name type text;
alter table ${@schema}.permission_groups_mapping alter column description type text;

-- Table: ${@schema}.tasks
alter table ${@schema}.TASKS add column if not exists reserve_ind boolean NOT NULL default false;
alter table ${@schema}.TASKS add column if not exists reserve_retention_period_type character varying(20);
alter table ${@schema}.TASKS add column if not exists reserve_retention_period_value numeric;
update ${@schema}.tasks set load_entity = true where task_type = 'LOAD' and selection_method= 'REF';
alter table ${@schema}.tasks rename column number_of_entities_to_copy to num_of_entities;

-- Table: ${@schema}.tdm_reserved_entities
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


-- Table environment_roles
ALTER TABLE ${@schema}.environment_roles
add COLUMN IF NOT EXISTS allowed_number_of_reserved_entities BIGINT  DEFAULT 0;

-- TABLE task_execution_list
ALTER TABLE ${@schema}.task_execution_list
add COLUMN IF NOT EXISTS execution_note text;