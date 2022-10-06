SELECT task_title,
       CAST(refresh_reference_data as varchar(256)) as refresh_reference_data,
       CAST(delete_before_load as varchar(256)) as delete_before_load,
       CAST(replace_sequences as varchar(256)) as replace_sequences,
       num_of_entities,
       selection_method,
       selection_param_value,
       entity_exclusion_list,
       CAST(load_entity as varchar(256))as load_entity ,
       source_env_name,
       source_environment_id,
       UPPER(task_type) as task_type,
       CAST(version_ind as varchar(256)) as version_ind,
       retention_period_type,
       selected_version_task_name,
       CAST(retention_period_value as varchar(256)) as retention_period_value,
       (select to_char(version_datetime, 'yyyyMMddhh24miss') from @TDMDB_SCHEMA@.task_execution_list where task_execution_id = selected_version_task_exe_id limit 1) as selected_version_datetime,
       scheduling_end_date,
       CAST(selected_version_task_exe_id as varchar(256)) as selected_version_task_exe_id,
       selected_ref_version_task_name,
       selected_ref_version_datetime,
       CAST(selected_ref_version_task_exe_id as varchar(256)) as selected_ref_version_task_exe_id,
       sync_mode,
       reserve_ind,
       reserve_retention_period_type,
       reserve_retention_period_value,
       parameters,
       reserve_note
FROM   @TDMDB_SCHEMA@.TASKS
WHERE  task_id = ?
