CREATE OR REPLACE FUNCTION public.param_values(
	parentlu text,
	entity_id text,
	table_name text,
	env text,
	cols text,
	child_arr text,
	select_col text,
	lu_type_2 text)
    RETURNS SETOF json 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
BEGIN
RETURN QUERY EXECUTE
CASE 
WHEN lu_type_2 IS NULL THEN 'SELECT ''{}''::json'
WHEN EXISTS(SELECT 1 FROM tdm_lu_type_relation_eid WHERE source_env = env AND lu_type_1 = parentlu) THEN
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
 
-- TDM 7.1- add the permision_groups_mapping TDM table 
-- DROP TABLE public.permission_groups_mapping;

CREATE TABLE  IF NOT EXISTS public.permission_groups_mapping
(
    description character varying(500),
    fabric_role character varying(100) NOT NULL,
    permission_group character varying(100) NOT NULL,
    created_by character varying(100) NOT NULL,
    updated_by character varying(100) NOT NULL,
    creation_date timestamp without time zone,
    update_date timestamp without time zone,
    CONSTRAINT permission_groups_mapping_pkey PRIMARY KEY (fabric_role)
);

-- Add initial mapping for admin user
insert into public.permission_groups_mapping (
	description,
	fabric_role,
	permission_group,
	created_by,
	updated_by,
	creation_date,
	update_date
) values ('Initial mapping for admin user', 'admin', 'admin', 'admin', 'admin', NOW(), NOW());

-- Updte structure of public.task_exe_error_detailed
ALTER TABLE public.task_exe_error_detailed DROP COLUMN IF EXISTS etl_execution_id;
ALTER TABLE public.task_exe_error_detailed DROP COLUMN IF EXISTS fabric_execution_id;
ALTER TABLE public.task_exe_error_detailed DROP COLUMN IF EXISTS error_msg;
ALTER TABLE public.task_exe_error_detailed ADD COLUMN IF NOT EXISTS error_category character varying(100) NOT NULL;
ALTER TABLE public.task_exe_error_detailed ADD COLUMN IF NOT EXISTS error_code character varying(100);
ALTER TABLE public.task_exe_error_detailed ADD COLUMN IF NOT EXISTS error_message character varying(4000) NOT NULL;
ALTER TABLE public.task_exe_error_detailed ADD COLUMN IF NOT EXISTS flow_name character varying(100);
ALTER TABLE public.task_exe_error_detailed ADD COLUMN IF NOT EXISTS stage_name character varying(100);
ALTER TABLE public.task_exe_error_detailed ADD COLUMN IF NOT EXISTS actor_name character varying(100);
ALTER TABLE public.task_exe_error_detailed ADD COLUMN IF NOT EXISTS actor_parameters character varying(500);

-- Create new sequence public.post_exe_process_id_seq
CREATE SEQUENCE public.post_exe_process_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
	
-- Create new table public.tdm_be_post_exe_process
CREATE TABLE IF NOT EXISTS public.tdm_be_post_exe_process (
	process_id bigint NOT NULL DEFAULT nextval('post_exe_process_id_seq'::regclass),
	process_name character varying(500),
	process_description character varying(500),
	be_id bigint,
	execution_order integer NOT NULL,
	CONSTRAINT be_post_exe_process_pkey PRIMARY KEY (process_id)
);
CREATE UNIQUE INDEX tdm_be_post_exe_process_ix1 ON public.tdm_be_post_exe_process (process_name, be_id);

-- Create new table public.tasks_post_exe_process
CREATE TABLE IF NOT EXISTS public.tasks_post_exe_process (
	task_id bigint NOT NULL,
	process_id bigint NOT NULL,
	process_name character varying(500) NOT NULL,
	execution_order integer NOT NULL,
	CONSTRAINT tasks_post_exe_process_pkey PRIMARY KEY (task_id, process_id)
);

-- Create new table public.task_exe_stats_detailed
CREATE TABLE IF NOT EXISTS public.task_exe_stats_detailed
(
    task_execution_id bigint NOT NULL,
    lu_name character varying(200)  NOT NULL,
    entity_id text NOT NULL,
    target_entity_id text NOT NULL,
    table_name character varying(100) NOT NULL,
    stage_name character varying(100),
    flow_name character varying(100),
    actor_name character varying(100),
    creation_date timestamp without time zone,
    source_count character varying(20),
    target_count character varying(20),
    diff character varying(20),
    results character varying(20)
);