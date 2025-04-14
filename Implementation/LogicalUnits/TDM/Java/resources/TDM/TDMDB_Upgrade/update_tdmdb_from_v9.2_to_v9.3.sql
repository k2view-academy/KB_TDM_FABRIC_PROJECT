ALTER TABLE ${@schema}.tasks ADD COLUMN IF NOT EXISTS enable_execution Boolean DEFAULT 'true';
ALTER TABLE ${@schema}.environment_products ADD COLUMN IF NOT EXISTS enable_product Boolean DEFAULT 'true';
ALTER TABLE ${@schema}.task_ref_tables ADD COLUMN IF NOT EXISTS filter_fields text;

DROP INDEX IF EXISTS ${@schema}.tdm_be_exe_process_ix1;
CREATE UNIQUE INDEX IF NOT EXISTS tdm_be_exe_process_ix1 ON ${@schema}.tdm_be_exe_process (process_name, be_id, process_type);

-- TDM 9.1.4/5/6, in case upgrading from TDM 9.2 which does not include the these changes

ALTER TABLE ${@schema}.task_execution_entities ALTER COLUMN task_execution_id type BIGINT USING task_execution_id::BIGINT;

ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS root_target_entity_id TEXT;
ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS parent_lu_name TEXT;
ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS parent_entity_id TEXT;
ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS parent_target_entity_id TEXT;

DROP INDEX IF EXISTS ${@schema}.task_execution_entities_2ix;
CREATE INDEX IF NOT EXISTS task_execution_entities_1ix ON ${@schema}.task_execution_entities (task_execution_id, root_lu_name, root_entity_id);

UPDATE ${@schema}.task_execution_entities SET root_entity_id = '' WHERE root_entity_id IS NULL;

CREATE OR REPLACE PROCEDURE ${@schema}.update_parent_root_info(schemaName text)
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	recNum bigint;
	rec record;
    executeInd boolean;
	curr_cursor refcursor := 'curs';
	
BEGIN
	recNum := 0; 

    SELECT CASE WHEN param_value like '9.2%' THEN true else false end INTO executeInd FROM ${@schema}.tdm_general_parameters WHERE param_name = 'TDM_VERSION';

    IF executeInd = true THEN

        ALTER TABLE ${@schema}.task_execution_entities DROP CONSTRAINT task_execution_entities_pkey;

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
    END IF;
END;
$BODY$;

call ${@schema}.update_parent_root_info('${@schema}');
drop procedure ${@schema}.update_parent_root_info(IN TEXT);

ANALYZE  ${@schema}.task_execution_entities;

UPDATE ${@schema}.task_execution_entities SET root_target_entity_id = '' WHERE root_target_entity_id IS NULL;

ALTER TABLE ${@schema}.task_execution_entities  ADD CONSTRAINT task_execution_entities_pkey
PRIMARY KEY (task_execution_id, lu_name, entity_id, clone_no, root_entity_id, root_target_entity_id);

INSERT INTO ${@schema}.tdm_general_parameters(
        param_name, param_value)
    VALUES ('FOOTER_TEXT', 'Copyright K2view') ON CONFLICT DO NOTHING;  