-- Update TDM version
update ${@schema}.tdm_general_parameters set param_value = '8.1' where param_name = 'TDM_VERSION' ;

ALTER TABLE ${@schema}.product_logical_units
DROP COLUMN lu_dc_name;

ALTER TABLE ${@schema}.tdm_generate_task_field_mappings 
ADD COLUMN param_order bigint;

update ${@schema}.tdm_generate_task_field_mappings m set param_order = m2.seqnum 
from (select m2.*, row_number() over (PARTITION BY task_id order by task_id) as seqnum
	 from ${@schema}.tdm_generate_task_field_mappings m2
	 ) m2
where m.task_id = m2.task_id
and m.param_name = m2.param_name;
