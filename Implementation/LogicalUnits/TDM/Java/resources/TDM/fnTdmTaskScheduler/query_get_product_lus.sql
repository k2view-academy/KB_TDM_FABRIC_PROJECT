SELECT distinct t.task_id, t.lu_id, t.lu_name, env.product_id, env.product_version, env.data_center_name
  FROM @TDMDB_SCHEMA@.tasks_logical_units t, @TDMDB_SCHEMA@.product_logical_units p , @TDMDB_SCHEMA@.environment_products env, @TDMDB_SCHEMA@.environments e
   where t.lu_id = p.lu_id
   and p.product_id = env.product_id
   and t.task_id = ?
   and env.environment_id = e.environment_id
   and (env.environment_id = ?);