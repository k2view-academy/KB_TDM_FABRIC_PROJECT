WITH task_max_execution_id AS
    (
        SELECT
            task_id                AS max_task_id
          , lu_id                  AS max_lu_id
          , max(task_execution_id) AS max_task_execution_id
        FROM
            TASK_EXECUTION_LIST
        GROUP BY
            task_id
          , lu_id
    )
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
  , tt.execution_status parent_lu_status
  , tt.product_id
  , tt.product_version
  , tt.num_of_processed_entities
  , tt.process_id
  , ep.product_version                                  source_product_version
  , to_char(tt.version_datetime, 'yyyyMMddHH24miss') as version_datetime
  , e.fabric_environment_name  as source_environment_name
  , e2.fabric_environment_name as target_environment_name
FROM
    TASK_EXECUTION_LIST tt
   , task_max_execution_id
  , environments         e
  , environments         e2
  , environment_products ep
WHERE
    tt.task_id                       = max_task_id
    AND tt.task_execution_id         = max_task_execution_id
    AND tt.lu_id                     = max_lu_id
    AND UPPER(ep.status) = 'ACTIVE'
    AND UPPER(tt.execution_status)   = 'PENDING'
    AND (
        tt.parent_lu_id is null
        or not exists(
            select 1 from TASK_EXECUTION_LIST par where par.task_execution_id = tt.task_execution_id and par.lu_id = tt.parent_lu_id
        ) or exists(
            select 1 from TASKS t where tt.task_id = t.task_id and t.selection_method = 'REF'
        )
    )
    AND tt.source_environment_id = e.environment_id
    AND e.environment_id         = ep.environment_id
    AND ep.product_id            = tt.product_id
	AND tt.environment_id        = e2.environment_id
	AND tt.lu_id > 0
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
  , p.execution_status parent_lu_status
  , tt.product_id
  , tt.product_version
  , tt.num_of_processed_entities
  , tt.process_id
  , ep.product_version                                  source_product_version
  , to_char(tt.version_datetime, 'yyyyMMddHH24miss') as version_datetime
  , e.fabric_environment_name as source_environment_name
  , e2.fabric_environment_name as target_environment_name
FROM
    TASK_EXECUTION_LIST tt
  , TASK_EXECUTION_LIST p
  , task_max_execution_id
  , environments         e
  , environments         e2
  , environment_products ep
WHERE
    tt.task_id                       = max_task_id
    AND tt.task_execution_id         = max_task_execution_id
    AND tt.lu_id                     = max_lu_id
    AND UPPER(tt.execution_status)   = 'PENDING'
    AND tt.task_execution_id         = p.task_execution_id
    AND tt.parent_lu_id              = p.lu_id
    AND UPPER(p.execution_status) in ('STOPPED', 'FAILED' , 'KILLED' ,'COMPLETED')
    AND UPPER(ep.status) = 'ACTIVE'
    AND tt.source_environment_id = e.environment_id
    AND e.environment_id         = ep.environment_id
    AND ep.product_id            = tt.product_id
	AND tt.environment_id        = e2.environment_id
	AND tt.lu_id > 0
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
  , p.execution_status parent_lu_status
  , tt.product_id
  , tt.product_version tdm_target_product_version
  , tt.num_of_processed_entities
  , tt.process_id
  , ep.product_version tdm_source_product_version
  , to_char(tt.version_datetime, 'yyyyMMddHH24miss') as version_datetime
  , e.fabric_environment_name as source_environment_name
  , e2.fabric_environment_name as target_environment_name
FROM
      TASK_EXECUTION_LIST tt
    , TASK_EXECUTION_LIST p
    , task_max_execution_id
    , environments         e
    , environments         e2
    , environment_products ep
WHERE
    tt.task_id                       = max_task_id
    AND tt.task_execution_id         = max_task_execution_id
    AND UPPER(ep.status)             = 'ACTIVE'
    AND UPPER(tt.execution_status)   = 'PENDING'
    AND tt.source_environment_id     = e.environment_id
    AND tt.environment_id            = e2.environment_id
    AND tt.process_id > 0