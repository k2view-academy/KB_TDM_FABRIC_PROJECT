-- Update TDM version
update tdm_general_parameters set param_value = '7.4.0' where param_name = 'TDM_VERSION';
insert into tdm_general_parameters (param_name, param_value) values ('MAX_RESERVATION_DAYS_FOR_TESTER', 10);

-- Alter long VARCHAR fields to TEXT
alter table TASK_REF_EXE_STATS alter column error_msg type text;
alter table business_entities alter column be_description  type text;
alter table environment_roles alter column role_description type text;
alter table environments alter column environment_description type text;
alter table product_logical_units alter column lu_description type text;
alter table products alter column product_description type text;
alter table tdm_be_env_exclusion_list alter column exclusion_list type text;
alter table task_exe_error_summary alter column error_msg type text;
alter table task_exe_error_detailed alter column actor_parameters type text;
alter table tdm_be_post_exe_process alter column process_name type text;
alter table tdm_be_post_exe_process alter column process_description type text;
alter table tasks_post_exe_process alter column process_name type text;
alter table permission_groups_mapping alter column description type text;

-- Table: public.tasks
alter table TASKS add column if not exists reserve_ind boolean NOT NULL default false;
alter table TASKS add column if not exists reserve_retention_period_type character varying(20);
alter table TASKS add column if not exists reserve_retention_period_value numeric;
update tasks set load_entity = true where task_type = 'LOAD' and selection_method= 'REF';
alter table tasks rename column number_of_entities_to_copy to num_of_entities;

-- Table: public.tdm_reserved_entities
CREATE TABLE IF NOT EXISTS public.tdm_reserved_entities
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
ALTER TABLE public.environment_roles
add COLUMN IF NOT EXISTS allowed_number_of_reserved_entities BIGINT  DEFAULT 0;

-- TABLE task_execution_list
ALTER TABLE public.task_execution_list
add COLUMN IF NOT EXISTS execution_note text;

CREATE OR REPLACE FUNCTION public.param_values(
	parentlu text,
	entity_id text,
	table_name text,
	env text,
	cols text,
	child_arr text,
	select_col text,
	lu_type2 text)
    RETURNS SETOF json 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
BEGIN
RETURN QUERY EXECUTE
CASE 
WHEN lu_type2 IS NULL THEN 'SELECT ''{}''::json'
WHEN EXISTS(SELECT 1 FROM tdm_lu_type_relation_eid WHERE source_env = env AND lu_type_1 = parentlu AND lu_type1_eid='''|| entity_id || ''') THEN 
'
SELECT row_to_json(allparams) as p from(
SELECT ' || cols ||' FROM public.' || lower(table_name) || ' WHERE entity_id in (
SELECT rel_base.'|| select_col || ' FROM ' || lower(parentlu) || '_params
LEFT JOIN ( SELECT * FROM tdm_lu_type_relation_eid
WHERE tdm_lu_type_relation_eid.lu_type_1 = ''' || parentlu || '''
AND tdm_lu_type_relation_eid.source_env = ''' || env || '''
AND (tdm_lu_type_relation_eid.lu_type_2 ' || child_arr || ')
AND tdm_lu_type_relation_eid.version_name = '''') rel_base
ON ' || lower(parentlu) || '_params.entity_id = rel_base.lu_type1_eid
WHERE ' || lower(parentlu) || '_params.source_environment = ''' || env || '''
AND lu_type1_eid='''|| entity_id || ''') AND source_environment = ''' || env || ''') allparams'

 

ELSE
'
SELECT row_to_json(allparams) as p from(
SELECT ' || cols ||' FROM public.' || lower(table_name) || ' WHERE entity_id in (
SELECT entity_id FROM ' || lower(parentlu) || '_params
WHERE ' || lower(parentlu) || '_params.source_environment = ''' || env || '''
AND entity_id='''|| entity_id || ''') AND source_environment = ''' || env || ''') allparams'
END;

 

END;
$BODY$;

ALTER FUNCTION public.param_values(text, text, text, text, text, text, text, text)
    OWNER TO tdm;
