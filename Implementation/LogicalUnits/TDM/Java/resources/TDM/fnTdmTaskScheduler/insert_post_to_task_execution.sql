INSERT INTO @TDMDB_SCHEMA@.TASK_EXECUTION_LIST(
    task_id,
    task_execution_id,
    creation_date,
    be_id,
    environment_id,
    product_id,
    product_version,
    lu_id,
    execution_status,
    data_center_name,
    num_of_processed_entities,
    parent_lu_id,
    source_environment_id,
    source_env_name,
    task_type,
    process_id
    )
VALUES(?,?,localtimestamp,?,?,?,?,?,'pending',?,?,?,?,?,?,?)