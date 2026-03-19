ALTER TABLE ${@schema}.product_logical_units
DROP COLUMN IF EXISTS lu_dc_name;

ALTER TABLE ${@schema}.tdm_generate_task_field_mappings 
ADD COLUMN IF NOT EXISTS param_order bigint;

update ${@schema}.tdm_generate_task_field_mappings m set param_order = m2.seqnum 
from (select m2.*, row_number() over (PARTITION BY task_id order by task_id) as seqnum
	 from ${@schema}.tdm_generate_task_field_mappings m2
	 ) m2
where m.task_id = m2.task_id
and m.param_name = m2.param_name;

-- add a new row in tdm_general_params for max retention days for tester 
INSERT INTO ${@schema}.tdm_general_parameters (param_name,param_value)
select 'MAX_RETENTION_DAYS_FOR_TESTER','90'
where not exists (select 1 from ${@schema}.tdm_general_parameters where param_name = 'MAX_RETENTION_DAYS_FOR_TESTER');

-- add a new row in tdm_general_params for max reservation days for tester
INSERT INTO ${@schema}.tdm_general_parameters (param_name,param_value)
select 'MAX_RESERVATION_DAYS_FOR_TESTER','90'
where not exists (select 1 from ${@schema}.tdm_general_parameters where param_name = 'MAX_RESERVATION_DAYS_FOR_TESTER');

-- Seperate Avaliable Options For Reservation And Retention 
UPDATE ${@schema}.tdm_general_parameters
SET param_value = '{"retentionDefaultPeriod":{"units":"Do Not Delete","value":-1},"reservationDefaultPeriod":{"units":"Days","value":5},"versioningRetentionPeriod":{"units":"Days","value":5,"allow_doNotDelete":True},"versioningRetentionPeriodForTesters":{"units":"Days","value":5,"allow_doNotDelete":False},"permissionGroups":["admin","owner","tester"],"retentionPeriodTypes":[{"name":"Minutes","units":0.00069444444},{"name":"Hours","units":0.04166666666},{"name":"Days","units":1},{"name":"Weeks","units":7},{"name":"Years","units":365}],"reservationPeriodTypes":[{"name":"Minutes","units":0.00069444444},{"name":"Hours","units":0.04166666666},{"name":"Days","units":1},{"name":"Weeks","units":7},{"name":"Years","units":365}],"enable_reserve_by_params":False}'
WHERE param_name = 'tdm_gui_params';

ALTER TABLE ${@schema}.environments 
ADD COLUMN IF NOT EXISTS mask_sensitive_data boolean NOT NULL DEFAULT true;

update ${@schema}.environments set mask_sensitive_data = false;

ALTER TABLE ${@schema}.tasks  
ADD COLUMN IF NOT EXISTS mask_sensitive_data boolean NOT NULL DEFAULT true;

update ${@schema}.tasks t set mask_sensitive_data = (select e.mask_sensitive_data from ${@schema}.environments e where e.environment_id = t.source_environment_id);

ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS root_lu_name text;

CREATE TABLE IF NOT EXISTS ${@schema}.tdm_params_distinct_values
(
    lu_name text NOT NULL,
    field_name text NOT NULL,
    number_of_values bigint,
    field_values text[],
    is_numeric boolean,
    min_value text,
    max_value text,
    CONSTRAINT tdm_params_distinct_values_pkey PRIMARY KEY (lu_name, field_name)
);

CREATE OR REPLACE PROCEDURE ${@schema}.update_params_columns(schemaName text)
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	rec record;
	curs cursor for select table_name, column_name from  information_schema.columns 
			where table_schema = '${@schema}' 
			and table_name like '%_params' and column_name like '%.%'
			order by table_name;
	currTableName text = '';
	tableFullName text;

BEGIN

	open curs;
	LOOP
		fetch from curs into rec;
		exit when not found;
		
		tableFullName := schemaName || '.' || rec.table_name;
										
		EXECUTE format('ALTER TABLE ' || tableFullName || ' ALTER COLUMN "'|| rec.column_name || '" TYPE TEXT[] USING "' || rec.column_name||'"::TEXT[]');
	END LOOP;
	CLOSE curs;
END;
$BODY$;

ALTER PROCEDURE ${@schema}.update_params_columns(IN TEXT)
    OWNER TO tdm;

call ${@schema}.update_params_columns('${@schema}');
drop procedure ${@schema}.update_params_columns(IN TEXT);

CREATE OR REPLACE PROCEDURE ${@schema}.update_params_tables(schemaName text)
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	rec record;
	curs cursor for select table_name from  information_schema.columns 
			where table_schema = '${@schema}' 
			and table_name like '%_params'
			order by table_name;
	currTableName text = '';
	tableFullName text;

BEGIN

	open curs;
	LOOP
		fetch from curs into rec;
		exit when not found;
		
		tableFullName := schemaName || '.' || rec.table_name;
		
		if currTableName != rec.table_name THEN
			EXECUTE format('ALTER TABLE ' || tableFullName || ' ADD COLUMN IF NOT EXISTS root_lu_name text');
			EXECUTE format('ALTER TABLE ' || tableFullName || ' ADD COLUMN IF NOT EXISTS root_iid text');
			currTableName := rec.table_name;
		END IF;

	END LOOP;
	CLOSE curs;
END;
$BODY$;

call ${@schema}.update_params_tables('${@schema}');
drop procedure ${@schema}.update_params_tables(IN TEXT);

CREATE OR REPLACE PROCEDURE ${@schema}.update_root_info(schemaName text)
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	rootLuName text default '';
	rootIID text default '';
	luParentId bigint;
	recNum bigint;
	rootFound boolean;
	currLuName text;
	currIId text;
	rec record;
	query text;
	curr_cursor refcursor := 'curs';
	tableName text;
	tableFullName text;

BEGIN
	recNum := 0;

	EXECUTE $_$DECLARE curs cursor WITH HOLD for 
				select e.CTID as RECID, e.task_execution_id, e.lu_name, e.entity_id, e.target_entity_id,
                    e.iid, e.source_env, l.be_id, l.parent_lu_id
				from ${@schema}.task_execution_entities e, ${@schema}.task_execution_list l, ${@schema}.product_logical_units p
				where e.root_lu_name is null
				and e.task_execution_id = cast(l.task_execution_id as text)
				and l.lu_id = p.lu_id and l.be_id = p.be_id
				and e.lu_name = p.lu_name
			$_$;

	LOOP
		fetch from curr_cursor into rec;
		exit when not found;
		recNum := recNum + 1;

		tableName := lower(rec.lu_name) || '_params';
		tableFullName := schemaName || '.' || lower(rec.lu_name) || '_params';

		IF rec.parent_lu_id is null THEN
			update ${@schema}.task_execution_entities e
				set root_lu_name = lu_name, root_entity_id = iid
				where CTID = rec.RECID and task_execution_id = rec.task_execution_id
                    and lu_name = rec.lu_name and entity_id = rec.entity_id
                    and target_entity_id = rec.target_entity_id and iid = rec.iid;

			 IF EXISTS
            	( SELECT 1
              	FROM   information_schema.tables 
              	WHERE  table_schema = '${@schema}'
              	AND    table_name = tableName
           	 )
       	    THEN
				EXECUTE format('update ' || tableFullName || ' set root_lu_name = $1, root_iid = $2 where entity_id = $3 and source_environment = $4') using rec.lu_name, rec.iid, rec.iid, rec.source_env;
			END IF;
					
		ELSE 

			currLuName := rec.lu_name;
			currIId := rec.iid;
			rootFound := false;

			WHILE rootFound = false LOOP
				select  lu_type_1, lu_type1_eid, lu_parent_id  into rootLuName, rootIID, luParentId
					from ${@schema}.tdm_lu_type_relation_eid, ${@schema}.product_logical_units
					where lu_type_2 = currLuName and lu_type2_eid = currIId
					and lu_type_1 = lu_name and be_id = rec.be_id
                    and source_env = rec.source_env;
		
				IF luParentId is null THEN
				    update ${@schema}.task_execution_entities 
				    set root_lu_name = rootLuName, root_entity_id = rootIID
					where CTID = rec.RECID and task_execution_id = rec.task_execution_id
                    and lu_name = rec.lu_name and entity_id = rec.entity_id
                    and target_entity_id = rec.target_entity_id and iid = rec.iid;
				
                    IF EXISTS
            			( SELECT 1
              			FROM   information_schema.tables 
              			WHERE  table_schema = '${@schema}'
              			AND    table_name = tableName
           	 		)
       	 		    THEN
						EXECUTE format('update ' || tableFullName || ' set root_lu_name = $1, root_iid = $2 where entity_id = $3 and source_environment = $4') using rootLuName, rootIID, rec.iid, rec.source_env;
					END IF;

					rootFound := true;
				ELSE
					currLuName := rootLuName;
					currIId := currIId;
				END IF;
			END LOOP;
					
		END IF;
		IF recNum % 100000 = 0 THEN
			commit;
		END IF;
	END LOOP;
	CLOSE curr_cursor;
END;
$BODY$;

call ${@schema}.update_root_info('${@schema}');
drop procedure ${@schema}.update_root_info(IN TEXT);

CREATE OR REPLACE PROCEDURE ${@schema}.modify_param_tables_pk(schemaName text)
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	rec record;
	curs cursor for select distinct table_name from  information_schema.columns 
			where table_schema = '${@schema}' 
			and table_name like '%_params'
			order by table_name;
	tableFullName text;
	pkName text;
BEGIN

	open curs;
	LOOP
		fetch from curs into rec;
		exit when not found;	
		
		tableFullName := schemaName || '.' || rec.table_name;
		pkName := rec.table_name || '_pkey';
		EXECUTE format('DELETE FROM ' || tableFullName || ' WHERE root_lu_name is null');
		EXECUTE format('ALTER TABLE ' || tableFullName || ' DROP CONSTRAINT '||pkName);
		EXECUTE format('ALTER TABLE ' || tableFullName || ' ADD CONSTRAINT ' || pkName || ' PRIMARY KEY (root_lu_name, root_iid, entity_id, source_environment)');
	END LOOP;
	CLOSE curs;
END;
$BODY$;

call ${@schema}.modify_param_tables_pk('${@schema}');
drop procedure ${@schema}.modify_param_tables_pk(IN TEXT);
