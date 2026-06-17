WITH pending_tt AS (
    SELECT
        tt.*,
        SPLIT_PART(tt.task_executed_by, '##', 1) AS executed_by_user,
        SPLIT_PART(tt.task_executed_by, '##', 2) AS executed_by_role
    FROM ${@TDMDB_SCHEMA}.task_execution_list tt
    WHERE UPPER(tt.execution_status) = 'PENDING'
),
base_tt AS (
    SELECT
        tt.*,
        ts.selection_method,
        ts.task_created_by,
        CASE
            WHEN tt.executed_by_user = 'TDM.tdmTaskScheduler'
                THEN SPLIT_PART(ts.task_created_by, '##', 2)
            ELSE tt.executed_by_role
        END AS user_roles
    FROM pending_tt tt
    JOIN ${@TDMDB_SCHEMA}.tasks ts
      ON ts.task_id = tt.task_id
),
active_env_products AS (
    SELECT
        environment_id,
        product_id,
        product_version
    FROM ${@TDMDB_SCHEMA}.environment_products
    WHERE UPPER(status) = 'ACTIVE'
),
blocked_by_pre AS (
    SELECT DISTINCT
        tt2.task_execution_id
    FROM ${@TDMDB_SCHEMA}.task_execution_list tt2
    JOIN ${@TDMDB_SCHEMA}.tasks_exe_process ep
      ON ep.task_id = tt2.task_id
     AND ep.process_id = tt2.process_id
     AND ep.process_type = 'pre'
    WHERE tt2.process_id > 0
      AND UPPER(tt2.execution_status) IN ('PENDING', 'RUNNING')
),
running_pre_process AS (
    SELECT DISTINCT
        tt2.task_execution_id,
        tt2.process_id
    FROM ${@TDMDB_SCHEMA}.task_execution_list tt2
    JOIN ${@TDMDB_SCHEMA}.tasks_exe_process ep2
      ON ep2.task_id = tt2.task_id
     AND ep2.process_id = tt2.process_id
     AND ep2.process_type = 'pre'
    WHERE UPPER(tt2.execution_status) IN ('PENDING', 'RUNNING')
)

SELECT
    tt.task_id,
    tt.be_id,
    tt.task_type,
    tt.creation_date,
    tt.environment_id,
    tt.source_environment_id,
    0 AS parent_lu_id,
    tt.lu_id,
    tt.task_execution_id,
    tt.execution_status AS parent_lu_status,
    0 AS product_id,
    NULL AS tdm_target_product_version,
    tt.num_of_processed_entities,
    tt.process_id,
    NULL AS tdm_source_product_version,
    tt.version_task_execution_id,
    tt.subset_task_execution_id,
    tt.source_env_name AS source_environment_name,
    tt.env_name AS target_environment_name,
    tt.source_max_no_of_workers AS source_max_workers_per_node,
    tt.target_max_no_of_workers AS target_max_workers_per_node,
    tt.source_affinity,
    tt.target_affinity,
    tt.executed_by_user AS task_executed_by,
    tt.user_roles
FROM base_tt tt
WHERE tt.selection_method = 'TABLES'
  AND NOT EXISTS (
      SELECT 1
      FROM blocked_by_pre bp
      WHERE bp.task_execution_id = tt.task_execution_id
  )

UNION

SELECT
    tt.task_id,
    tt.be_id,
    tt.task_type,
    tt.creation_date,
    tt.environment_id,
    tt.source_environment_id,
    0 AS parent_lu_id,
    tt.lu_id,
    tt.task_execution_id,
    tt.execution_status AS parent_lu_status,
    tt.product_id,
    tt.product_version AS tdm_target_product_version,
    tt.num_of_processed_entities,
    tt.process_id,
    ep.product_version AS tdm_source_product_version,
    tt.version_task_execution_id,
    tt.subset_task_execution_id,
    tt.source_env_name AS source_environment_name,
    tt.env_name AS target_environment_name,
    tt.source_max_no_of_workers AS source_max_workers_per_node,
    tt.target_max_no_of_workers AS target_max_workers_per_node,
    tt.source_affinity,
    tt.target_affinity,
    tt.executed_by_user AS task_executed_by,
    tt.user_roles
FROM base_tt tt
JOIN active_env_products ep
  ON ep.environment_id = tt.environment_id
 AND ep.product_id = tt.product_id
WHERE tt.lu_id > 0
  AND (
        tt.parent_lu_id IS NULL
        OR NOT EXISTS (
            SELECT 1
            FROM ${@TDMDB_SCHEMA}.task_execution_list par
            WHERE par.task_execution_id = tt.task_execution_id
              AND par.lu_id = tt.parent_lu_id
        )
      )
  AND NOT EXISTS (
      SELECT 1
      FROM blocked_by_pre bp
      WHERE bp.task_execution_id = tt.task_execution_id
  )

UNION

SELECT
    tt.task_id,
    tt.be_id,
    tt.task_type,
    tt.creation_date,
    tt.environment_id,
    tt.source_environment_id,
    tt.parent_lu_id,
    tt.lu_id,
    tt.task_execution_id,
    parent_tt.execution_status AS parent_lu_status,
    tt.product_id,
    tt.product_version AS tdm_target_product_version,
    tt.num_of_processed_entities,
    tt.process_id,
    ep.product_version AS tdm_source_product_version,
    tt.version_task_execution_id,
    tt.subset_task_execution_id,
    tt.source_env_name AS source_environment_name,
    tt.env_name AS target_environment_name,
    tt.source_max_no_of_workers AS source_max_workers_per_node,
    tt.target_max_no_of_workers AS target_max_workers_per_node,
    tt.source_affinity,
    tt.target_affinity,
    tt.executed_by_user AS task_executed_by,
    tt.user_roles
FROM base_tt tt
JOIN ${@TDMDB_SCHEMA}.task_execution_list parent_tt
  ON parent_tt.task_execution_id = tt.task_execution_id
 AND parent_tt.lu_id = tt.parent_lu_id
JOIN active_env_products ep
  ON ep.environment_id = tt.environment_id
 AND ep.product_id = tt.product_id
WHERE tt.lu_id > 0
  AND UPPER(parent_tt.execution_status) IN ('STOPPED', 'FAILED', 'KILLED', 'COMPLETED')

UNION

SELECT
    tt.task_id,
    tt.be_id,
    tt.task_type,
    tt.creation_date,
    tt.environment_id,
    tt.source_environment_id,
    0 AS parent_lu_id,
    tt.lu_id,
    tt.task_execution_id,
    '' AS parent_lu_status,
    tt.product_id,
    tt.product_version AS tdm_target_product_version,
    tt.num_of_processed_entities,
    tt.process_id,
    '' AS tdm_source_product_version,
    tt.version_task_execution_id,
    tt.subset_task_execution_id,
    tt.source_env_name AS source_environment_name,
    tt.env_name AS target_environment_name,
    tt.source_max_no_of_workers AS source_max_workers_per_node,
    tt.target_max_no_of_workers AS target_max_workers_per_node,
    tt.source_affinity,
    tt.target_affinity,
    tt.executed_by_user AS task_executed_by,
    tt.user_roles
FROM base_tt tt
JOIN ${@TDMDB_SCHEMA}.tasks_exe_process ep
  ON ep.task_id = tt.task_id
 AND ep.process_id = tt.process_id
WHERE tt.process_id != 0
  AND (
        ep.process_type = 'pre'
        OR (
            ep.process_type = 'post'
            AND NOT EXISTS (
                SELECT 1
                FROM running_pre_process rpp
                WHERE rpp.task_execution_id = tt.task_execution_id
                  AND rpp.process_id != ep.process_id
            )
        )
      );