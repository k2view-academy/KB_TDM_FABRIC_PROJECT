-- TDM 7.3 - Add user_type field to environment_role_users & environment_owners tables
ALTER TABLE environment_role_users ADD COLUMN IF NOT EXISTS user_type character varying(10) NOT NULL default 'NA';

update environment_role_users set user_type = 'ID' where upper(username) != 'ALL';
update environment_role_users set user_type = 'ALL' where upper(username) = 'ALL';
ALTER TABLE environment_role_users ADD CONSTRAINT check_user_type CHECK (user_type = 'ALL' OR user_type = 'ID' OR user_type = 'GROUP');

ALTER TABLE environment_owners ADD COLUMN IF NOT EXISTS user_type character varying(10) NOT NULL default 'NA';
update environment_owners set user_type = 'ID';
ALTER TABLE environment_owners ADD CONSTRAINT check_env_owner_type CHECK (user_type = 'ID' OR user_type = 'GROUP');

ALTER TABLE environments DROP COLUMN IF EXISTS fabric_environment_name;

INSERT INTO public.tdm_general_parameters(param_name, param_value) VALUES ('TDM_VERSION', '7.3.0');

alter table tasks alter column delete_before_load set default false;
alter table tasks alter column task_execution_status set default 'Active';
alter table tasks alter column task_status set default 'Active';
alter table tasks alter column task_title set NOT NULL;
alter table tasks alter column environment_id set NOT NULL;
alter table tasks alter column be_id set NOT NULL;
alter table tasks alter column selection_method set NOT NULL;
alter table tasks alter column source_env_name set NOT NULL;
alter table tasks alter column source_environment_id set NOT NULL;
alter table tasks alter column task_type set NOT NULL;

alter table task_exe_error_detailed ALTER COLUMN error_message type text;


-- TDM 7.3 Fix the param_values function
DROP FUNCTION public.param_values(text, text, text, text, text, text, text, text);

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
WHEN EXISTS(SELECT 1 FROM tdm_lu_type_relation_eid WHERE source_env = env AND lu_type_1 = parentlu AND 
	   ((lu_type2 = '-1' and lu_type1_eid='''|| entity_id || ''') or lu_type2 != '-1')) THEN
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
