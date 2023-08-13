-- Update TDM version
update ${@schema}.tdm_general_parameters set param_value = '8.0' where param_name = 'TDM_VERSION' ;

-- Change the default retention period to 'Do Not Delete'
UPDATE ${@schema}.tdm_general_parameters
SET param_value = '{"maxRetentionPeriod":90,"retentionDefaultPeriod":{"unit":"Do Not Delete","value":-1},"maxReservationPeriod":90,"reservationDefaultPeriod":{"unit":"Days","value":5},"versioningRetentionPeriod":{"unit":"Days","value":5,"allow_doNotDelete":True},"retentionPeriodForTesters":{"unit":"Days","value":5,"allow_doNotDelete":False},"permissionGroups":["admin","owner","tester"],"availableOptions":[{"name":"Minutes","units":0.00069444444},{"name":"Hours","units":0.04166666666},{"name":"Days","units":1},{"name":"Weeks","units":7},{"name":"Years","units":365}],"enable_reserve_by_params":False}'
WHERE param_name = 'tdm_gui_params';

update ${@schema}.tasks set retention_period_type='Do Not Delete', retention_period_value= -1 where retention_period_value = 0;

insert into ${@schema}.tdm_general_parameters (param_name, param_value) values('TABLE_DEFAULT_DISTRIBUTION_MIN', 1) ON CONFLICT DO NOTHING;
insert into ${@schema}.tdm_general_parameters (param_name, param_value) values('TABLE_DEFAULT_DISTRIBUTION_MAX', 3) ON CONFLICT DO NOTHING;


--- Table: ${@schema}.tdm_generate_task_field_mappings - TDM 8.0
--DROP TABLE IF EXISTS ${@schema}.tdm_generate_task_field_mappings;

CREATE TABLE IF NOT EXISTS  ${@schema}.tdm_generate_task_field_mappings
(
    task_id bigint NOT NULL,
    param_name text COLLATE pg_catalog."default",
    param_type text COLLATE pg_catalog."default" NOT NULL,
    param_value text COLLATE pg_catalog."default",
    CONSTRAINT tdm_generate_task_field_mappings_pkey PRIMARY KEY (task_id, param_name)
);

ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS root_entity_id text;

INSERT INTO ${@schema}.environments(environment_name, environment_description, environment_expiration_date, 
	environment_point_of_contact_first_name, environment_point_of_contact_last_name, environment_point_of_contact_phone1, 
	environment_point_of_contact_phone2, environment_point_of_contact_email, environment_id, environment_created_by, 
	environment_creation_date, environment_last_updated_date, environment_last_updated_by, environment_status, allow_write, 
	allow_read, sync_mode) VALUES('Synthetic','This is the synthetic environment.',NULL,NULL,NULL,NULL,NULL,NULL,-1,
	'admin',NOW(),NOW(),'admin','Active',false,true,'FORCE') ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.environment_role_users(environment_id, role_id, user_type, username, user_id) VALUES(-1,-1,'ID','ALL','-1') ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.environment_roles(environment_id, role_name, role_description, allowed_delete_before_load, 
	allowed_creation_of_synthetic_data,allowed_random_entity_selection,allowed_request_of_fresh_data,
	allowed_task_scheduling,allowed_number_of_entities_to_copy, role_id, role_created_by, role_creation_date, 
	role_last_updated_date,role_last_updated_by,role_status,allowed_refresh_reference_data, allowed_replace_sequences, 
	allow_read, allow_write,allowed_number_of_entities_to_read, allowed_entity_versioning, allowed_test_conn_failure, 
	allowed_number_of_reserved_entities) VALUES(-1,'Synthetic','Role for Synethetic Environment',false,false,false,false,false,0,-1,
	'admin',NOW(),NOW(),'admin','Active',false,false,true,false,1000,false,false,0) ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.environment_products(environment_product_id, environment_id, product_id, product_version, created_by, creation_date, 
	last_updated_date, last_updated_by, status)(select nextval('environment_product_id_seq'::regclass),-1,product_id,'Synthetic',
	'admin',NOW(),NOW(),'admin','Active' from products where product_status = 'Active') ON CONFLICT DO NOTHING;

Create INDEX IF NOT EXISTS TDM_SEQ_MAPPING_IX on ${@schema}.tdm_seq_mapping (task_execution_id,lu_type,source_env);
CREATE INDEX IF NOT EXISTS task_exe_stats_detailed_1ix ON ${@schema}.task_exe_stats_detailed (task_execution_id, lu_name, target_entity_id);

CREATE SEQUENCE IF NOT EXISTS ${@schema}.instance_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 50;
	
