ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS execution_note text;
ALTER TABLE ${@schema}.tasks_exe_process ADD COLUMN IF NOT EXISTS status text DEFAULT 'Active';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = '${@schema}.tdm_seq_mapping'::regclass
          AND contype = 'p'  -- 'p' = primary key
    ) THEN
        ALTER TABLE ${@schema}.tdm_seq_mapping
        ADD CONSTRAINT tdm_seq_mapping_pkey PRIMARY KEY (task_execution_id,lu_type,source_env,entity_target_id,seq_name,table_name,source_id);
    END IF;
END
$$;
