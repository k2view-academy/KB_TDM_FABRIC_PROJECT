-- Update TDM version
update ${@schema}.tdm_general_parameters set param_value = '8.1' where param_name = 'TDM_VERSION' ;

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


ALTER TABLE ${@schema}.environments 
ADD COLUMN IF NOT EXISTS mask_sensitive_data boolean NOT NULL DEFAULT true;

update ${@schema}.environments set mask_sensitive_data = false;

ALTER TABLE ${@schema}.tasks  
ADD COLUMN IF NOT EXISTS mask_sensitive_data boolean NOT NULL DEFAULT true;

update ${@schema}.tasks t set mask_sensitive_data = (select e.mask_sensitive_data from ${@schema}.environments e where e.environment_id = t.source_environment_id);

ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS root_lu_name text;

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
				select e.CTID as RECID, e.lu_name, e.iid, e.source_env, l.be_id, l.parent_lu_id
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
				where CTID = rec.RECID and lu_name = rec.lu_name and iid = rec.iid;

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
					and lu_type_1 = lu_name and be_id = rec.be_id;
		
				IF luParentId is null THEN
					update ${@schema}.task_execution_entities 
						set root_lu_name = rootLuName, root_entity_id = rootIID
						where CTID = rec.RECID and lu_name = rec.lu_name and iid = rec.iid;

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

ALTER PROCEDURE ${@schema}.update_root_info(IN TEXT)
    OWNER TO tdm;

call ${@schema}.update_root_info('${@schema}');

drop procedure ${@schema}.update_root_info(IN TEXT);

CREATE OR REPLACE PROCEDURE ${@schema}.update_params_tables(schemaName text)
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
	pkName text;
BEGIN

	open curs;
	LOOP
		fetch from curs into rec;
		exit when not found;
		
		tableFullName := schemaName || '.' || rec.table_name;
		pkName := rec.table_name || '_pkey';
		
		if currTableName != rec.table_name THEN
			EXECUTE format('ALTER TABLE ' || tableFullName || ' DROP CONSTRAINT '||pkName);
			EXECUTE format('ALTER TABLE ' || tableFullName || ' ADD CONSTRAINT ' || pkName || ' PRIMARY KEY (root_lu_name, root_iid, entity_id, source_environment)');
			currTableName := rec.table_name;
		END IF;
								
		EXECUTE format('ALTER TABLE ' || tableFullName || ' ALTER COLUMN "'|| rec.column_name || '" TYPE TEXT[] USING "' || rec.column_name||'"::TEXT[]');
	END LOOP;
	CLOSE curs;
END;
$BODY$;

ALTER PROCEDURE ${@schema}.update_params_tables(IN TEXT)
    OWNER TO tdm;

call ${@schema}.update_params_tables('${@schema}');

drop procedure ${@schema}.update_params_tables(IN TEXT);
