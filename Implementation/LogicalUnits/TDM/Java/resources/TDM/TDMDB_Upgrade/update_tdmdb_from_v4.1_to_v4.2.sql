ALTER TABLE ${@schema}.source_environments 
ADD COLUMN IF NOT EXISTS environment_created_by character varying(200);

ALTER TABLE ${@schema}.source_environments 
ADD COLUMN IF NOT EXISTS environment_creation_date date;

ALTER TABLE ${@schema}.source_environment_roles 
ADD COLUMN IF NOT EXISTS role_created_by character varying(50);

ALTER TABLE ${@schema}.source_environment_roles
ADD COLUMN IF NOT EXISTS role_creation_date character varying(50);
				  
DROP TABLE IF EXISTS ${@schema}.tdm_lu_type_rel_tar_eid;
				  
CREATE TABLE ${@schema}.tdm_lu_type_rel_tar_eid
(
  target_env character varying(200) NOT NULL,
  source_env character varying(200) NOT NULL,	
  task_execution_id bigint NOT NULL,
  lu_type_1 character varying(200) NOT NULL,
  lu_type_2 character varying(200) NOT NULL,
  lu_type1_eid character varying(50) NOT NULL,
  lu_type2_eid character varying(50) NOT NULL,
  creation_date timestamp without time zone,
  CONSTRAINT tdm_lu_type_relation_tar_eid_pk PRIMARY KEY (target_env, task_execution_id, lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid)
);

-- Alter table task_execution_entities- add target_entity_id field
ALTER TABLE ${@schema}.task_execution_entities drop constraint IF EXISTS task_execution_entities_pkey;

-- Update existing records before re-creating the new PK , sinsce the target_entity_id cannot be null
UPDATE ${@schema}.task_execution_entities set target_entity_id = entity_id where target_entity_id is null;

ALTER TABLE ${@schema}.task_execution_entities
  ADD CONSTRAINT task_execution_entities_pkey PRIMARY KEY(task_execution_id, lu_name, entity_id, etl_execution_id, target_entity_id);

-- Alter instance_table_count- remove the table_count field from the PK
ALTER TABLE ${@schema}.instance_table_count drop CONSTRAINT IF EXISTS instance_table_count_pkey;

ALTER TABLE ${@schema}.instance_table_count
ADD CONSTRAINT instance_table_count_pkey PRIMARY KEY (lu_name, ei, table_name);

-- Alter TDM_SEQ_MAPPING- add entity_sequence field to support synthetic data
ALTER TABLE ${@schema}.tdm_seq_mapping
ADD COLUMN IF NOT EXISTS entity_sequence bigint;

-- Add the role_id to the unique index of environment_role_users
DROP INDEX IF EXISTS ENV_ROLE_USER_IX;
CREATE UNIQUE INDEX ENV_ROLE_USER_IX ON environment_role_users   (environment_id, user_id, role_id);

-- Add the new utils to support task parameters

-- utils functions (working with parameters)
-- json_cast function adds changes the format of the json data
CREATE OR REPLACE FUNCTION json_cast(data json) RETURNS json IMMUTABLE AS
$body$
    SELECT ('{'|| string_agg(to_json(UPPER(key)) || ':' || '"' || replace(regexp_split_to_array(replace(regexp_replace(value::text, '\[(.*)\]', '\1'), '"',''), ',')::text, '"','') || '"', ',') || '}')::json
    FROM (
        SELECT * FROM json_each(data) WHERE value::TEXT <> 'null' AND value::TEXT <> '""'
    ) t;
$body$
LANGUAGE sql;

-- json_add_prefix function adds luName as prefix to json
CREATE OR REPLACE FUNCTION json_add_prefix(luName text, data json = '{}') RETURNS json IMMUTABLE AS
$body$
declare
  result json;
begin
        if(data IS NOT NULL AND luName IS NOT NULL) then EXECUTE 'SELECT (''{''||string_agg(to_json(''' || luName || '.' || ''' || key)||'':''||value, '','')||''}'')::json FROM (SELECT * FROM json_each('''||json_cast(data)||''')) t;' into result;
                return result;
        else return '{}';
        end if;

end;
$body$
LANGUAGE plpgsql;

-- json_append function receives two jsons and merges them to one
CREATE OR REPLACE FUNCTION json_append(data json, first_data json = '{}', second_data json = '{}') RETURNS json IMMUTABLE AS
$body$
    SELECT ('{'||string_agg(to_json(key)||':'||value, ',')||'}')::json
    FROM (
        SELECT * FROM json_each(data)
        UNION ALL
        SELECT * FROM json_each(first_data)
        UNION ALL
        SELECT * FROM json_each(second_data)
    ) t;
$body$
LANGUAGE sql;

-- param_values function returns json with param names as keys and param values as values
CREATE OR REPLACE FUNCTION ${@schema}.param_values(luname text,_entity_id text, source_env text)
  RETURNS SETOF json AS
$BODY$
BEGIN

    IF(luname IS NOT NULL) THEN
        RETURN QUERY EXECUTE 'select row_to_json(' || LOWER(luName) || '_params' || ') from ' || LOWER(luName) || '_params' || ' WHERE source_environment=''' || source_env || ''' AND entity_id=''' || _entity_id || ''' ';
    ELSE RETURN QUERY EXECUTE  ' SELECT ''{}''::json AS param_values';
    END IF;


END
$BODY$
LANGUAGE plpgsql;

-- eval function executes received string expression as query
CREATE OR REPLACE FUNCTION eval(expression text) RETURNS void
as
$body$
declare
  result integer;
begin
  execute expression;
  return;
end;
$body$
LANGUAGE plpgsql;
