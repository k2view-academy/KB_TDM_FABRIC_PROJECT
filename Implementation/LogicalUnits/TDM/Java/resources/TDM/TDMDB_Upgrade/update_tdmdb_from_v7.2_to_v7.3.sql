-- TDM 7.3 - Add user_type field to environment_role_users & environment_owners tables
ALTER TABLE ${@schema}.environment_role_users ADD COLUMN IF NOT EXISTS user_type character varying(10) NOT NULL default 'NA';

update ${@schema}.environment_role_users set user_type = 'ID' where upper(username) != 'ALL';
update ${@schema}.environment_role_users set user_type = 'ALL' where upper(username) = 'ALL';
ALTER TABLE ${@schema}.environment_role_users ADD CONSTRAINT check_user_type CHECK (user_type = 'ALL' OR user_type = 'ID' OR user_type = 'GROUP');

ALTER TABLE ${@schema}.environment_owners ADD COLUMN IF NOT EXISTS user_type character varying(10) NOT NULL default 'NA';
update ${@schema}.environment_owners set user_type = 'ID';
ALTER TABLE ${@schema}.environment_owners ADD CONSTRAINT check_env_owner_type CHECK (user_type = 'ID' OR user_type = 'GROUP');

ALTER TABLE ${@schema}.environments DROP COLUMN IF EXISTS fabric_environment_name;

INSERT INTO ${@schema}.tdm_general_parameters(param_name, param_value) VALUES ('TDM_VERSION', '7.3.0');

alter table ${@schema}.tasks alter column delete_before_load set default false;
alter table ${@schema}.tasks alter column task_execution_status set default 'Active';
alter table ${@schema}.tasks alter column task_status set default 'Active';
alter table ${@schema}.tasks alter column task_title set NOT NULL;
alter table ${@schema}.tasks alter column environment_id set NOT NULL;
alter table ${@schema}.tasks alter column be_id set NOT NULL;
alter table ${@schema}.tasks alter column selection_method set NOT NULL;
alter table ${@schema}.tasks alter column source_env_name set NOT NULL;
alter table ${@schema}.tasks alter column source_environment_id set NOT NULL;
alter table ${@schema}.tasks alter column task_type set NOT NULL;

alter table ${@schema}.task_exe_error_detailed ALTER COLUMN error_message type text;
