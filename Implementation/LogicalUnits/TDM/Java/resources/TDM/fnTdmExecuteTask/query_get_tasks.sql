SELECT DISTINCT
    tt.task_id
  , tt.be_id
  , tt.task_type
  , tt.creation_date
  , tt.data_center_name
  , tt.environment_id
  , 0 AS parent_lu_id
  , tt.lu_id
  , tt.task_execution_id
  , tt.execution_status as parent_lu_status
  , 0 as product_id
  , null as tdm_target_product_version
  , tt.num_of_processed_entities
  , tt.process_id
  , null as tdm_source_product_version
  , tt.version_task_execution_id
  , tt.subset_task_execution_id
  , e.environment_name  as source_environment_name
  , e2.environment_name as target_environment_name
  , SPLIT_PART(tt.task_executed_by, '##', 1) as task_executed_by
  , CASE
        WHEN SPLIT_PART(tt.task_executed_by, '##', 1) = 'TDM.tdmTaskScheduler' THEN
            SPLIT_PART(ts.task_created_by, '##', 2)
        ELSE
            SPLIT_PART(tt.task_executed_by, '##', 2)
    END AS user_roles
FROM
    ${@TDMDB_SCHEMA}.task_execution_list tt
  , ${@TDMDB_SCHEMA}.environments         e
  , ${@TDMDB_SCHEMA}.environments         e2
  , ${@TDMDB_SCHEMA}.tasks ts
WHERE
    UPPER(tt.execution_status)   = 'PENDING'
    AND ts.task_id = tt.task_id
    and ts.selection_method = 'TABLES'
    AND tt.source_environment_id = e.environment_id
	AND tt.environment_id        = e2.environment_id
    AND (
        NOT EXISTS 
            (SELECT 1 FROM ${@TDMDB_SCHEMA}.task_execution_list tt2, ${@TDMDB_SCHEMA}.tasks_exe_process ep 
            WHERE tt2.task_execution_id = tt.task_execution_id AND tt2.process_id > 0 
                AND upper(tt2.execution_status) in ('PENDING', 'RUNNING') 
                AND tt2.task_id = ep.task_id AND tt2.process_id = ep.process_id AND ep.process_type = 'pre'
            )
    )
UNION
SELECT DISTINCT
    tt.task_id
  , tt.be_id
  , tt.task_type
  , tt.creation_date
  , tt.data_center_name
  , tt.environment_id
  , 0 AS parent_lu_id
  , tt.lu_id
  , tt.task_execution_id
  , tt.execution_status as parent_lu_status
  , tt.product_id
  , tt.product_version as tdm_target_product_version
  , tt.num_of_processed_entities
  , tt.process_id
  , ep.product_version as tdm_source_product_version
  , tt.version_task_execution_id
  , tt.subset_task_execution_id
  , e.environment_name  as source_environment_name
  , e2.environment_name as target_environment_name
  , SPLIT_PART(tt.task_executed_by, '##', 1) as task_executed_by
  , CASE
        WHEN SPLIT_PART(tt.task_executed_by, '##', 1) = 'TDM.tdmTaskScheduler' THEN
            SPLIT_PART(ts.task_created_by, '##', 2)
        ELSE
            SPLIT_PART(tt.task_executed_by, '##', 2)
    END AS user_roles
FROM
    ${@TDMDB_SCHEMA}.TASK_EXECUTION_LIST tt
  , ${@TDMDB_SCHEMA}.environments         e
  , ${@TDMDB_SCHEMA}.environments         e2
  , ${@TDMDB_SCHEMA}.environment_products ep
  , ${@TDMDB_SCHEMA}.tasks ts
WHERE
    UPPER(ep.status)                 = 'ACTIVE'
    AND UPPER(tt.execution_status)   = 'PENDING'
    AND (
        tt.parent_lu_id is null
        or not exists(
            select 1 from ${@TDMDB_SCHEMA}.TASK_EXECUTION_LIST par where par.task_execution_id = tt.task_execution_id and par.lu_id = tt.parent_lu_id
        )
    )
    AND (
        NOT EXISTS 
            (SELECT 1 FROM ${@TDMDB_SCHEMA}.task_execution_list tt2, ${@TDMDB_SCHEMA}.tasks_exe_process ep 
            WHERE tt2.task_execution_id = tt.task_execution_id AND tt2.process_id > 0 
                AND upper(tt2.execution_status) in ('PENDING', 'RUNNING') 
                AND tt2.task_id = ep.task_id AND tt2.process_id = ep.process_id AND ep.process_type = 'pre'
            )
    )
    AND tt.source_environment_id = e.environment_id
    AND e.environment_id         = ep.environment_id
    AND ep.product_id            = tt.product_id
	AND tt.environment_id        = e2.environment_id
	AND tt.lu_id > 0
    AND ts.task_id = tt.task_id
UNION
SELECT DISTINCT
    tt.task_id
  , tt.be_id
  , tt.task_type
  , tt.creation_date
  , tt.data_center_name
  , tt.environment_id
  , tt.parent_lu_id
  , tt.lu_id
  , tt.task_execution_id
  , p.execution_status as parent_lu_status
  , tt.product_id
  , tt.product_version as tdm_target_product_version
  , tt.num_of_processed_entities
  , tt.process_id
  , ep.product_version as tdm_source_product_version
  , tt.version_task_execution_id
  , tt.subset_task_execution_id
  , e.environment_name as source_environment_name
  , e2.environment_name as target_environment_name
  , SPLIT_PART(tt.task_executed_by, '##', 1) as task_executed_by
  , CASE
        WHEN SPLIT_PART(tt.task_executed_by, '##', 1) = 'TDM.tdmTaskScheduler' THEN
            SPLIT_PART(ts.task_created_by, '##', 2)
        ELSE
            SPLIT_PART(tt.task_executed_by, '##', 2)
    END AS user_roles
FROM
    ${@TDMDB_SCHEMA}.TASK_EXECUTION_LIST tt
  , ${@TDMDB_SCHEMA}.TASK_EXECUTION_LIST p
  , ${@TDMDB_SCHEMA}.environments         e
  , ${@TDMDB_SCHEMA}.environments         e2
  , ${@TDMDB_SCHEMA}.environment_products ep
  , ${@TDMDB_SCHEMA}.tasks ts
WHERE
    UPPER(tt.execution_status)       = 'PENDING'
    AND tt.task_execution_id         = p.task_execution_id
    AND tt.parent_lu_id              = p.lu_id
    AND UPPER(p.execution_status) in ('STOPPED', 'FAILED' , 'KILLED' ,'COMPLETED')
    AND UPPER(ep.status) = 'ACTIVE'
    AND tt.source_environment_id = e.environment_id
    AND e.environment_id         = ep.environment_id
    AND ep.product_id            = tt.product_id
	AND tt.environment_id        = e2.environment_id
    AND ts.task_id               = tt.task_id
	AND tt.lu_id > 0
UNION
SELECT DISTINCT
    tt.task_id
  , tt.be_id
  , tt.task_type
  , tt.creation_date
  , tt.data_center_name
  , tt.environment_id
  , 0 as parent_lu_id
  , tt.lu_id
  , tt.task_execution_id
  , '' as parent_lu_status
  , tt.product_id
  , tt.product_version as tdm_target_product_version
  , tt.num_of_processed_entities
  , tt.process_id
  , '' as tdm_source_product_version
  , tt.version_task_execution_id
  , tt.subset_task_execution_id
  , tt.source_env_name as source_environment_name
  , e.environment_name as target_environment_name
  , SPLIT_PART(tt.task_executed_by, '##', 1) as task_executed_by
  , CASE
        WHEN SPLIT_PART(tt.task_executed_by, '##', 1) = 'TDM.tdmTaskScheduler' THEN
            SPLIT_PART(ts.task_created_by, '##', 2)
        ELSE
            SPLIT_PART(tt.task_executed_by, '##', 2)
    END AS user_roles
FROM
      ${@TDMDB_SCHEMA}.TASK_EXECUTION_LIST tt
    , ${@TDMDB_SCHEMA}.environments  e
    , ${@TDMDB_SCHEMA}.tasks ts
    , ${@TDMDB_SCHEMA}.tasks_exe_process ep 
WHERE
    UPPER(tt.execution_status)   = 'PENDING'
    AND tt.environment_id        = e.environment_id
    AND tt.process_id !=0
    AND ts.task_id               = tt.task_id
    AND ts.task_id = ep.task_id AND tt.process_id = ep.process_id
    AND (ep.process_type = 'pre' OR (ep.process_type = 'post' AND
        NOT EXISTS (SELECT 1 FROM ${@TDMDB_SCHEMA}.TASK_EXECUTION_LIST tt2 WHERE tt2.task_execution_id = tt.task_execution_id 
        AND tt2.process_id != ep.process_id and upper(execution_status) in ('PENDING', 'RUNNING') )))
