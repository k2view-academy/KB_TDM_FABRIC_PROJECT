-- Update TDM version
update ${@schema}.tdm_general_parameters set param_value = '7.6' where param_name = 'TDM_VERSION';

-- Remove unused tables if they exists
DROP table IF EXISTS ${@schema}.source_environment_role_users;
DROP table IF EXISTS ${@schema}.source_environment_roles;
DROP table IF EXISTS ${@schema}.source_environments;


-- Add new field to tasks table
alter table ${@schema}.TASKS add COLUMN IF NOT EXISTS filterout_reserved boolean DEFAULT true;

-- Set the flow name in the task_exe_stats_detailed
update ${@schema}.task_exe_stats_detailed t set flow_name = 'load_' ||t.table_name where flow_name is null;

-- Update the parameters in tasks table in case of combo parameter, to include the new indicator and set the type accordingly
update ${@schema}.tasks set parameters = replace (parameters, '"type":"combo"', '"type":"text","comboIndicator":"true"') where parameters like'%"type":"combo"%';

