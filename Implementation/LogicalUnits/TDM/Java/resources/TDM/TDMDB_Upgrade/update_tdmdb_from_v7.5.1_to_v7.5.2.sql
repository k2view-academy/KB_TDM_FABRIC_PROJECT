-- Update TDM version
update ${@schema}.tdm_general_parameters set param_value = '7.5.2' where param_name = 'TDM_VERSION';

alter table ${@schema}.TASK_EXECUTION_LIST add COLUMN IF NOT EXISTS source_product_version character varying(50);
alter table ${@schema}.TASKS add COLUMN IF NOT EXISTS reserve_note text;

-- Update param_values function
CREATE OR REPLACE FUNCTION ${@schema}.param_values(
	parentlu text,
	entity_id text,
	table_name text,
	env text,
	cols text,
	child_arr text,
	select_col text,
	lu_type2 text,
	schema text)
    RETURNS SETOF json 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$

DECLARE
 cnt integer := 0;
BEGIN
 EXECUTE format('SELECT 1 FROM %s.tdm_lu_type_relation_eid WHERE source_env =''%s'' AND lu_type_1 = ''%s'' AND lu_type1_eid = ''%s''', schema, env, parentlu, entity_id) into cnt;
RETURN QUERY EXECUTE
CASE 
WHEN lu_type2 IS NULL THEN 'SELECT ''{}''::json'
WHEN cnt = 1 THEN 
'
SELECT row_to_json(allparams) as p from(
SELECT ' || cols ||' FROM ' || schema|| '.' || lower(table_name) || ' WHERE entity_id in (
SELECT rel_base.'|| select_col || ' FROM ' || schema|| '.' || lower(parentlu) || '_params
LEFT JOIN ( SELECT * FROM ' || schema|| '.' || 'tdm_lu_type_relation_eid
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
SELECT ' || cols ||' FROM ' || schema|| '.' || lower(table_name) || ' WHERE entity_id in (
SELECT entity_id FROM ' || schema|| '.' || lower(parentlu) || '_params
WHERE ' || lower(parentlu) || '_params.source_environment = ''' || env || '''
AND entity_id='''|| entity_id || ''') AND source_environment = ''' || env || ''') allparams'
END;

END;
$BODY$;

ALTER FUNCTION ${@schema}.param_values(text, text, text, text, text, text, text, text, text)
    OWNER TO tdm;
