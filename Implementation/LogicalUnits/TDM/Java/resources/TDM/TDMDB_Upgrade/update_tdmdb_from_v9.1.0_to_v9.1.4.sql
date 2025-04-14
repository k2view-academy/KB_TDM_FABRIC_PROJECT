ALTER TABLE ${@schema}.task_execution_entities ALTER COLUMN task_execution_id type BIGINT USING task_execution_id::BIGINT;

ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS root_target_entity_id TEXT;
ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS parent_lu_name TEXT;
ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS parent_entity_id TEXT;
ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS parent_target_entity_id TEXT;

ALTER TABLE ${@schema}.task_execution_entities DROP CONSTRAINT task_execution_entities_pkey;

CREATE INDEX IF NOT EXISTS task_execution_entities_1ix ON ${@schema}.task_execution_entities (task_execution_id, root_lu_name, root_entity_id);
UPDATE ${@schema}.task_execution_entities SET root_entity_id = '' WHERE root_entity_id IS NULL;

CREATE OR REPLACE PROCEDURE ${@schema}.update_parent_root_info(schemaName text)
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	recNum bigint;
	rec record;
	curr_cursor refcursor := 'curs';
	
BEGIN
	recNum := 0;

	EXECUTE $_$DECLARE curs cursor WITH HOLD for 
				select distinct task_execution_id, root_lu_name, root_entity_id, target_entity_id
				from ${@schema}.task_execution_entities
				where lu_name = root_lu_name and iid = root_entity_id
			$_$;

    
	LOOP
		fetch from curr_cursor into rec;
		exit when not found;
		recNum := recNum + 1;

		update ${@schema}.task_execution_entities e
				set root_target_entity_id = rec.target_entity_id
				where task_execution_id = rec.task_execution_id
                    and root_lu_name = rec.root_lu_name and root_entity_id = rec.root_entity_id and root_target_entity_id is null;
	
		IF recNum % 1000 = 0 THEN
			commit;
		END IF;
	END LOOP;
	CLOSE curr_cursor;

END;
$BODY$;

call ${@schema}.update_parent_root_info('${@schema}');
drop procedure ${@schema}.update_parent_root_info(IN TEXT);

UPDATE ${@schema}.task_execution_entities SET root_target_entity_id = '' WHERE root_target_entity_id IS NULL;

ALTER TABLE ${@schema}.task_execution_entities ADD CONSTRAINT task_execution_entities_pkey 
    PRIMARY KEY (task_execution_id, lu_name, entity_id, target_entity_id, root_entity_id, root_target_entity_id);

ANALYZE  ${@schema}.task_execution_entities;
