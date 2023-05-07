-- Add the source environment name to environments 
ALTER TABLE ${@schema}.environments
ADD COLUMN IF NOT EXISTS fabric_environment_name character varying(200);

ALTER TABLE ${@schema}.environments
ADD COLUMN IF NOT EXISTS allow_write boolean NOT NULL DEFAULT true;

Create INDEX IF NOT EXISTS ENV_FABRIC_NAME_FOR_ACTIVE_IX ON environments (fabric_environment_name) where environment_status = 'Active';  

-- Update the data of environments- get the name from source_environments table. source environments tables will no longer be in use

update ${@schema}.environments e
set fabric_environment_name = (select source_environment_name from source_environments s where trim(s.source_environment_name) = trim(e.environment_name));

ALTER TABLE ${@schema}.environment_roles 
ADD COLUMN IF NOT EXISTS allow_read boolean NOT NULL DEFAULT false;

ALTER TABLE ${@schema}.environment_roles
ADD COLUMN IF NOT EXISTS allow_write boolean NOT NULL DEFAULT false;

ALTER TABLE ${@schema}.environment_roles
ADD COLUMN IF NOT EXISTS allowed_number_of_entities_to_read bigint NOT NULL DEFAULT 1000;

ALTER TABLE ${@schema}.environment_roles
ADD COLUMN IF NOT EXISTS allowed_entity_versioning boolean NOT NULL DEFAULT false;

-- Update the data of the roles for environments
update ${@schema}.environment_roles  set allow_write = TRUE ;

update ${@schema}.environment_roles r set allow_read = TRUE 
where exists (select 1 from ${@schema}.environments e where e.environment_id = r.environment_id and e.fabric_environment_name is not null);

-- Create the source environments and their roles in environment tables based on the source environment tables 
 INSERT INTO ${@schema}.environments(
            environment_name, environment_description, environment_expiration_date, 
            environment_point_of_contact_first_name, environment_point_of_contact_last_name, 
            environment_point_of_contact_phone1, environment_point_of_contact_phone2, 
            environment_point_of_contact_email, environment_id, environment_created_by, 
            environment_creation_date, environment_last_updated_date, environment_last_updated_by, 
            environment_status, fabric_environment_name, allow_write)
SELECT source_environment_name, environment_description, environment_expiration_date, 
       environment_point_of_contact_first_name, environment_point_of_contact_last_name, 
       environment_point_of_contact_phone1, environment_point_of_contact_phone2, 
       environment_point_of_contact_email, nextval('environments_environment_id_seq'::regclass), 
       environment_created_by , environment_creation_date, now(), 'TDM-UPGRADE', environment_status, 
       source_environment_name, FALSE 
       from ${@schema}.source_environments 
       where environment_status = 'Active' 
       and not exists (select 1 from  ${@schema}.environments e where e.fabric_environment_name = source_environments.source_environment_name);

       INSERT INTO ${@schema}.environment_roles(
            environment_id, role_name, role_description, allowed_delete_before_load, 
            allowed_creation_of_synthetic_data, allowed_random_entity_selection, 
            allowed_request_of_fresh_data, allowed_task_scheduling, allowed_number_of_entities_to_copy, 
            role_id, role_created_by, role_creation_date, role_last_updated_date, 
            role_expiration_date, role_last_updated_by, role_status, allowed_refresh_reference_data, 
            allowed_replace_sequences, allow_read, allow_write, allowed_number_of_entities_to_read)
     SELECT e.environment_id, sr.role_name, sr.role_description, false, 
     false, false, 
     false,  false, 0, 
     nextval('environment_roles_role_id_seq'::regclass), role_created_by, TO_TIMESTAMP(role_creation_date,'YYYY-MM-DD HH24:MI:SS'), now(), 
     role_expiration_date, 'TDM-UPGRADE', role_status, false,
     false, true, false, 1000 
  FROM ${@schema}.source_environment_roles sr, source_environments s, environments e 
  where sr.source_environment_id = s.source_environment_id 
  and s.source_environment_name = e.environment_name 
  and e.environment_last_updated_by = 'TDM-UPGRADE' 
  and sr.role_status = 'Active' ;                                                                                                                                
                                 
INSERT INTO ${@schema}.environment_role_users(environment_id, role_id, username, user_id) 
 SELECT er.environment_id, er.role_id, sru.username, sru.user_id
  FROM ${@schema}.source_environment_role_users sru, source_environment_roles sr, environment_roles er 
  where er.role_name =  sr.role_name 
  and sr.role_id = sru.role_id 
  and er.role_last_updated_by = 'TDM-UPGRADE' ;                                                                                      

-- Update task tables 
ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS task_type character varying(20);

ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS version_ind  boolean NOT NULL DEFAULT false;

ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS retention_period_type character varying(20);

ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS retention_period_value numeric;

ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS selected_version_task_name character varying(200);

ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS selected_version_datetime character varying(20);

ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS scheduling_end_date timestamp without time zone; 

ALTER TABLE ${@schema}.tasks 
ADD COLUMN IF NOT EXISTS selected_version_task_exe_id bigint; 

-- update the data of tasks- set the type of existing tasks to LOAD 

update tasks set task_type = 'LOAD' ;  

ALTER TABLE ${@schema}.task_execution_list
ADD COLUMN IF NOT EXISTS task_executed_by character varying(200);

ALTER TABLE ${@schema}.task_execution_list
ADD COLUMN IF NOT EXISTS fabric_execution_id character varying(200);

ALTER TABLE ${@schema}.task_execution_list
ADD COLUMN IF NOT EXISTS version_datetime timestamp without time zone;

ALTER TABLE ${@schema}.task_execution_list
ADD COLUMN IF NOT EXISTS version_expiration_date timestamp without time zone;

ALTER TABLE ${@schema}.task_execution_list
ADD COLUMN IF NOT EXISTS task_type character varying(20);

ALTER TABLE ${@schema}.task_execution_list
ADD COLUMN IF NOT EXISTS synced_to_fabric boolean DEFAULT false;

update task_execution_list l set task_type = (select t.task_type from tasks t where t.task_id = l.task_id);
                                                                                                                                      
Create INDEX IF NOT EXISTS TASK_EXEC_IX2 ON task_execution_list (task_execution_id);
