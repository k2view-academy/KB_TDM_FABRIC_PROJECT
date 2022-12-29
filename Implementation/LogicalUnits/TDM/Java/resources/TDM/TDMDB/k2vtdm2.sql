-- start CREATE SEQUENCE IF NOT EXISTS

-- DROP SEQUENCE IF EXISTS public.data_centers_data_center_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.data_centers_data_center_id_seq
        INCREMENT 1
        START 1
        MINVALUE 1
        MAXVALUE 9223372036854775807
        CACHE 1;

-- DROP SEQUENCE IF EXISTS public.environment_product_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.environment_product_id_seq
        INCREMENT 1
        START 1
        MINVALUE 1
        MAXVALUE 9223372036854775807
        CACHE 1;

-- DROP SEQUENCE IF EXISTS public.business_entities_be_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.business_entities_be_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS public.environment_roles_role_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.environment_roles_role_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS public.environments_environment_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.environments_environment_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS public.product_interfaces_interface_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.product_interfaces_interface_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS public.product_logical_units_lu_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.product_logical_units_lu_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS public.products_product_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.products_product_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS public.tasks_task_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.tasks_task_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS public.tasks_task_execution_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.tasks_task_execution_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS public.source_environments_source_environment_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.source_environments_source_environment_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 10000000000
  START 1
  CACHE 1;

-- DROP SEQUENCE IF EXISTS public.source_environment_roles_role_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.source_environment_roles_role_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 21
  CACHE 1;

  -- DROP SEQUENCE IF EXISTS public.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq;
  CREATE SEQUENCE IF NOT EXISTS public.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
-- DROP SEQUENCE IF EXISTS public.tasks_task_execution_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.tasks_task_execution_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- DROP SEQUENCE IF EXISTS public.source_environments_source_environment_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.source_environments_source_environment_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

-- DROP SEQUENCE IF EXISTS public.source_environment_roles_role_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.source_environment_roles_role_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 21
  CACHE 1;

  -- DROP SEQUENCE IF EXISTS public.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq;
  CREATE SEQUENCE IF NOT EXISTS public.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
  
  -- TDM 5.1
  -- DROP SEQUENCE IF EXISTS public.tasks_ref_table_id_seq;
  CREATE SEQUENCE IF NOT EXISTS public.tasks_ref_table_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

  -- TDM 7.0.1
  -- DROP SEQUENCE IF EXISTS public.post_exe_process_id_seq;
  CREATE SEQUENCE IF NOT EXISTS public.post_exe_process_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
