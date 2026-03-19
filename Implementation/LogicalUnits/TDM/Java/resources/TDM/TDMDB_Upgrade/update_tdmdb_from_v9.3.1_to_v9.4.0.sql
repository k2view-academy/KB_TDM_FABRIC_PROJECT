SET search_path = ${@schema};
ALTER TABLE ${@schema}.task_execution_entities ADD COLUMN IF NOT EXISTS execution_note text;
ALTER TABLE ${@schema}.tasks_exe_process ADD COLUMN IF NOT EXISTS status text DEFAULT 'Active';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'tdm_seq_mapping'::regclass
          AND contype = 'p'  -- 'p' = primary key
    ) THEN

        delete from ${@schema}.tdm_seq_mapping t1
        using ${@schema}.tdm_seq_mapping t2
        where t1.task_execution_id = t2.task_execution_id
            and t1.lu_type = t2.lu_type
            and t1.source_env = t2.source_env
            and t1.entity_target_id = t2.entity_target_id
            and t1.seq_name = t2.seq_name
            and t1.table_name = t2.table_name
            and t1.source_id = t2.source_id
            and t2.ctid < t1.ctid;

        ALTER TABLE ${@schema}.tdm_seq_mapping
        ADD CONSTRAINT tdm_seq_mapping_pkey PRIMARY KEY (task_execution_id,lu_type,source_env,entity_target_id,seq_name,table_name,source_id);
    END IF;
END
$$;
