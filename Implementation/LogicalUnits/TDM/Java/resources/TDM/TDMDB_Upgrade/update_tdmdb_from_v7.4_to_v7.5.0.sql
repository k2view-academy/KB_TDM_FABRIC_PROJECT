-- Update TDM version
update ${@schema}.tdm_general_parameters set param_value = '7.5.0' where param_name = 'TDM_VERSION';

-- Alter long VARCHAR fields to TEXT
alter table ${@schema}.TASKS alter column parameters type text;
alter table ${@schema}.TASKS alter column selection_param_value type text;

-- Remove INSTANCE_TABLE_COUNT table
drop table if exists ${@schema}.INSTANCE_TABLE_COUNT;
