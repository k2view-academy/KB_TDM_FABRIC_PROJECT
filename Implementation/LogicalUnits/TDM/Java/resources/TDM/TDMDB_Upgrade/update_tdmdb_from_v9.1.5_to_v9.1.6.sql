DROP INDEX IF EXISTS ${@schema}.task_execution_entities_2ix;
ALTER TABLE ${@schema}.task_execution_entities  DROP CONSTRAINT task_execution_entities_pkey;
ALTER TABLE ${@schema}.task_execution_entities  ADD CONSTRAINT task_execution_entities_pkey PRIMARY KEY (task_execution_id, lu_name, entity_id, clone_no, root_entity_id, root_target_entity_id);
