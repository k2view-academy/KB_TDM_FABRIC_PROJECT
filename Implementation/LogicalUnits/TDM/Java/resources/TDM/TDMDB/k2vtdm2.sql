-- start CREATE SEQUENCE IF NOT EXISTS


-- DROP SEQUENCE IF EXISTS ${@schema}.environment_product_id_seq;
CREATE SEQUENCE IF NOT EXISTS ${@schema}.environment_product_id_seq
        INCREMENT 1
        START 1
        MINVALUE 1
        MAXVALUE 9223372036854775807
        CACHE 1;

-- DROP SEQUENCE IF EXISTS ${@schema}.business_entities_be_id_seq;
CREATE SEQUENCE IF NOT EXISTS ${@schema}.business_entities_be_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS ${@schema}.environment_roles_role_id_seq;
CREATE SEQUENCE IF NOT EXISTS ${@schema}.environment_roles_role_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS ${@schema}.environments_environment_id_seq;
CREATE SEQUENCE IF NOT EXISTS ${@schema}.environments_environment_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS ${@schema}.product_logical_units_lu_id_seq;
CREATE SEQUENCE IF NOT EXISTS ${@schema}.product_logical_units_lu_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS ${@schema}.products_product_id_seq;
CREATE SEQUENCE IF NOT EXISTS ${@schema}.products_product_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS ${@schema}.tasks_task_id_seq;
CREATE SEQUENCE IF NOT EXISTS ${@schema}.tasks_task_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS ${@schema}.tasks_task_execution_id_seq;
CREATE SEQUENCE IF NOT EXISTS ${@schema}.tasks_task_execution_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

  -- DROP SEQUENCE IF EXISTS ${@schema}.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq;
  CREATE SEQUENCE IF NOT EXISTS ${@schema}.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
-- DROP SEQUENCE IF EXISTS ${@schema}.tasks_task_execution_id_seq;
CREATE SEQUENCE IF NOT EXISTS ${@schema}.tasks_task_execution_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

  -- TDM 5.1
  -- DROP SEQUENCE IF EXISTS ${@schema}.tasks_ref_table_id_seq;
  CREATE SEQUENCE IF NOT EXISTS ${@schema}.tasks_ref_table_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

  -- TDM 7.0.1
  -- DROP SEQUENCE IF EXISTS ${@schema}.post_exe_process_id_seq;
  CREATE SEQUENCE IF NOT EXISTS ${@schema}.post_exe_process_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

  -- TDM 8.0
  -- DROP SEQUENCE IF EXISTS ${@schema}.instance_id_seq;
  CREATE SEQUENCE IF NOT EXISTS ${@schema}.instance_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 50;
