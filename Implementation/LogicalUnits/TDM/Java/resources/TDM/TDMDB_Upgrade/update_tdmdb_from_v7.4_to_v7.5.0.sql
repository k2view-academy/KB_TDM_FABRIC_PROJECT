-- Update TDM version
update tdm_general_parameters set param_value = '7.5.0' where param_name = 'TDM_VERSION';

-- Alter long VARCHAR fields to TEXT
alter table TASKS alter column parameters type text;
alter table TASKS alter column selection_param_value type text;

-- Remove INSTANCE_TABLE_COUNT table
drop table if exists INSTANCE_TABLE_COUNT;
