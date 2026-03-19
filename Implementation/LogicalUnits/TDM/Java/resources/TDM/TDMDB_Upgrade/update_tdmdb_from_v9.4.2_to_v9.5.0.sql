SET search_path = ${@schema};

INSERT INTO ${@schema}.tdm_general_parameters (param_name, param_value)
VALUES ('CREATE_NEW_TASK_VERSION_ON_UPDATE', 'false') ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tdm_general_parameters(param_name, param_value)
VALUES ('TABLES_USE_SPLIT_API', 'true') ON CONFLICT DO NOTHING; 

INSERT INTO ${@schema}.tdm_general_parameters (param_name, param_value)
VALUES ('MAX_NO_OF_WORKERS_FOR_EXECUTION', -1) ON CONFLICT DO NOTHING; 

ALTER TABLE ${@schema}.environment_products
ADD COLUMN IF NOT EXISTS max_number_of_workers bigint;

ALTER TABLE ${@schema}.task_execution_list
ADD COLUMN IF NOT EXISTS source_max_no_of_workers bigint,
ADD COLUMN IF NOT EXISTS target_max_no_of_workers bigint,
ADD COLUMN IF NOT EXISTS source_affinity text,
ADD COLUMN IF NOT EXISTS target_affinity text;

ALTER TABLE ${@schema}.tasks_logical_units
ADD COLUMN IF NOT EXISTS source_max_no_of_workers bigint,
ADD COLUMN IF NOT EXISTS target_max_no_of_workers bigint,
ADD COLUMN IF NOT EXISTS source_affinity text,
ADD COLUMN IF NOT EXISTS target_affinity text;

-- handle old records of UPDATE public.task_execution_list
WITH AffinitySource AS (
    -- 1. Calculate the final intended affinity values for both source and target environments.
    SELECT
        t.task_id,
        plu.lu_id,
        COALESCE(
            NULLIF(tlu.source_affinity, ''),
            NULLIF(ep_source.data_center_name, '')
        ) AS computed_source_affinity,
        COALESCE(
            NULLIF(tlu.target_affinity, ''),
            NULLIF(ep_target.data_center_name, '')
        ) AS computed_target_affinity
    FROM
        ${@schema}.tasks t
    INNER JOIN
        ${@schema}.tasks_logical_units tlu ON tlu.task_id = t.task_id
    INNER JOIN
        ${@schema}.product_logical_units plu ON plu.lu_id = tlu.lu_id
    LEFT JOIN
        ${@schema}.environment_products ep_source ON TRIM(ep_source.product_id::text) = TRIM(plu.product_id::text)
                                              AND TRIM(ep_source.environment_id::text) = TRIM(t.source_environment_id::text)
                                              AND ep_source.status = 'Active'
    LEFT JOIN
        ${@schema}.environment_products ep_target ON TRIM(ep_target.product_id::text) = TRIM(plu.product_id::text)
                                              AND TRIM(ep_target.environment_id::text) = TRIM(t.environment_id::text)
                                              AND ep_target.status = 'Active'
    WHERE
        t.environment_id IS NOT NULL 
        OR t.source_environment_id IS NOT NULL
)
UPDATE ${@schema}.task_execution_list AS tel
SET
    source_affinity = COALESCE(
        ASrc.computed_source_affinity,
        tel.source_affinity
    ),
    
    target_affinity = COALESCE(
        ASrc.computed_target_affinity,
        tel.target_affinity
    )
FROM
    AffinitySource AS ASrc
WHERE
    tel.task_id = ASrc.task_id
    AND tel.lu_id = ASrc.lu_id
    AND tel.be_id != -1
    AND tel.lu_id != 0
    AND (ASrc.computed_source_affinity IS NOT NULL OR ASrc.computed_target_affinity IS NOT NULL);

ALTER TABLE ${@schema}.task_execution_list
DROP COLUMN IF EXISTS data_center_name;

ALTER TABLE ${@schema}.task_ref_tables
ADD COLUMN IF NOT EXISTS source_max_no_of_workers bigint,
ADD COLUMN IF NOT EXISTS target_max_no_of_workers bigint,
ADD COLUMN IF NOT EXISTS source_affinity text,
ADD COLUMN IF NOT EXISTS target_affinity text;

ALTER TABLE ${@schema}.products ADD COLUMN IF NOT EXISTS related_interfaces TEXT[] NOT NULL DEFAULT '{}';

ALTER TABLE ${@schema}.tasks ADD COLUMN IF NOT EXISTS in_place_masking_ind boolean NOT NULL DEFAULT false;
ALTER TABLE ${@schema}.task_ref_tables ADD COLUMN IF NOT EXISTS count_ind boolean DEFAULT true;
ALTER TABLE ${@schema}.task_ref_exe_stats ADD COLUMN IF NOT EXISTS number_of_partitions bigint default 1;
ALTER TABLE ${@schema}.task_ref_exe_stats ADD COLUMN IF NOT EXISTS number_of_failed_records bigint;


CREATE TABLE IF NOT EXISTS ${@schema}.task_ref_partition
(
  task_id bigint NOT NULL, 
  task_execution_id bigint NOT NULL,
  task_ref_table_id bigint NOT NULL,
  table_name text,
  partition_no bigint default 1,
  instance_id text,
  batch_id text,
  start_time timestamp without time zone,
  end_time timestamp without time zone,
  execution_status text,
  number_of_records_to_process bigint,
  number_of_processed_records bigint,
  number_of_failed_records bigint,
  error_msg text,
  CONSTRAINT task_ref_partition_pkey PRIMARY KEY (task_execution_id,task_ref_table_id,partition_no) 
);

UPDATE ${@schema}.task_ref_exe_stats s
SET number_of_partitions = -1
WHERE NOT EXISTS (
    SELECT 1
    FROM ${@schema}.task_ref_partition p
    WHERE p.task_execution_id = s.task_execution_id
);

-- task_ref_exe_stats
DROP INDEX IF EXISTS ${@schema}.task_ref_exe_stats_IX1;
DROP INDEX IF EXISTS ${@schema}.task_ref_exe_stats_IX2;
DROP INDEX IF EXISTS ${@schema}.task_ref_exe_stats_IX3;

DO $$
BEGIN
    
    IF NOT EXISTS (
        SELECT constraint_name
        FROM information_schema.table_constraints
        WHERE table_name = 'task_ref_exe_stats' 
          AND constraint_type = 'PRIMARY KEY'
    ) THEN        
        ALTER TABLE ${@schema}.task_ref_exe_stats ADD CONSTRAINT task_ref_exe_stats_pkey PRIMARY KEY (task_execution_id,task_ref_table_id);
    END IF;
END $$;
