SELECT task_title,
       CAST(refresh_reference_data as varchar(256)) as refresh_reference_data,
       CAST(delete_before_load as varchar(256)) as delete_before_load,
       CAST(replace_sequences as varchar(256)) as replace_sequences,
       num_of_entities,
       selection_method,
       selection_param_value,
       custom_logic_lu_name,
       CAST(load_entity as varchar(256))as load_entity ,
       source_env_name,
       source_environment_id,
       UPPER(task_type) as task_type,
       CAST(version_ind as varchar(256)) as version_ind,
       retention_period_type,
       CAST(retention_period_value as varchar(256)) as retention_period_value,
       scheduling_end_date,
       CAST(selected_version_task_exe_id as varchar(256)) as selected_version_task_exe_id,
       CAST(selected_subset_task_exe_id as varchar(256)) as selected_subset_task_exe_id,
       CAST(selected_ref_version_task_exe_id as varchar(256)) as selected_ref_version_task_exe_id,
       sync_mode,
       reserve_ind,
       reserve_retention_period_type,
       reserve_retention_period_value,
       parameters,
       reserve_note,
       filterout_reserved,
       CAST(mask_sensitive_data as varchar(10)) as mask_sensitive_data,
       clone_ind
FROM   @TDMDB_SCHEMA@.TASKS
WHERE  task_id = ?
