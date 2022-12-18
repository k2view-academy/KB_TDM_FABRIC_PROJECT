-- Table: public.activities
DROP TABLE IF EXISTS public.activities;

-- Table: public.business_entities
DROP TABLE IF EXISTS public.business_entities;

-- Table: public.environment_owners
DROP TABLE IF EXISTS public.environment_owners;

-- Table: public.environment_products
DROP TABLE IF EXISTS public.environment_products;

-- Table: public.environment_role_users
DROP TABLE IF EXISTS public.environment_role_users;

-- Table: public.environment_roles
DROP TABLE IF EXISTS public.environment_roles;

-- Table: public.environments
DROP TABLE IF EXISTS public.environments;

-- Table: public.parameters
DROP TABLE IF EXISTS public.parameters;

-- Table: public.product_logical_units
DROP TABLE IF EXISTS public.product_logical_units;

-- Table: public.products
DROP TABLE IF EXISTS public.products;

-- Table: public.task_execution_list
DROP TABLE IF EXISTS public.task_execution_list;

-- Table: public.tasks
DROP TABLE IF EXISTS public.tasks;

-- Table: public.tdm_be_env_exclusion_list
DROP TABLE IF EXISTS public.tdm_be_env_exclusion_list;

-- Table: public.tdm_seq_mapping
DROP TABLE IF EXISTS public.tdm_seq_mapping;

-- Table: public.task_execution_entities
DROP TABLE IF EXISTS public.task_execution_entities;

-- Table: public.tdm_lu_type_relation_eid
DROP TABLE IF EXISTS public.tdm_lu_type_relation_eid;


-- Table: public.tdm_lu_type_rel_tar_eid
DROP TABLE IF EXISTS public.tdm_lu_type_rel_tar_eid;

-- Table: public.tasks_logical_units
DROP TABLE IF EXISTS public.tasks_logical_units;

-- Table: public.tdm_env_globals
DROP TABLE IF EXISTS public.tdm_env_globals;

-- Add tables for TDM 5.1

-- Table: public.task_ref_tables
DROP TABLE IF EXISTS public.task_ref_tables;

-- Table: public.task_ref_exe_stats
DROP TABLE IF EXISTS public.task_ref_exe_stats;

-- Table: public.task_globals
DROP TABLE IF EXISTS public.task_globals; 

-- Table public.tdm_general_parameters
DROP TABLE IF EXISTS tdm_general_parameters;

-- Table public.task_execution_summary
DROP TABLE IF EXISTS task_execution_summary;

-- Table public.task_exe_stats_detailed
DROP TABLE IF EXISTS task_exe_stats_detailed;


-- Table public.task_exe_error_summary
DROP TABLE IF EXISTS task_exe_error_summary;

-- Table public.task_exe_error_detailed
DROP TABLE IF EXISTS task_exe_error_detailed;

-- Table public.tdm_be_post_exe_process
DROP TABLE IF EXISTS tdm_be_post_exe_process;

-- Table public.tasks_post_exe_process
DROP TABLE IF EXISTS tasks_post_exe_process;

-- Table: public.task_execution_override_attrs - TDM 7.2
DROP TABLE IF EXISTS public.task_execution_override_attrs;

-- Table: public.tdm_reserved_entities - TDM 7.4
DROP TABLE IF EXISTS public.tdm_reserved_entities;

-- start create SEQUENCE

DROP SEQUENCE IF EXISTS public.data_centers_data_center_id_seq;
CREATE SEQUENCE public.data_centers_data_center_id_seq
        INCREMENT 1
        START 1
        MINVALUE 1
        MAXVALUE 9223372036854775807
        CACHE 1;

DROP SEQUENCE IF EXISTS public.environment_product_id_seq;
CREATE SEQUENCE public.environment_product_id_seq
        INCREMENT 1
        START 1
        MINVALUE 1
        MAXVALUE 9223372036854775807
        CACHE 1;

DROP SEQUENCE IF EXISTS public.business_entities_be_id_seq;
CREATE SEQUENCE public.business_entities_be_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

DROP SEQUENCE IF EXISTS public.environment_roles_role_id_seq;
CREATE SEQUENCE public.environment_roles_role_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

DROP SEQUENCE IF EXISTS public.environments_environment_id_seq;
CREATE SEQUENCE public.environments_environment_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

DROP SEQUENCE IF EXISTS public.product_interfaces_interface_id_seq;
CREATE SEQUENCE public.product_interfaces_interface_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

DROP SEQUENCE IF EXISTS public.product_logical_units_lu_id_seq;
CREATE SEQUENCE public.product_logical_units_lu_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

DROP SEQUENCE IF EXISTS public.products_product_id_seq;
CREATE SEQUENCE public.products_product_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

DROP SEQUENCE IF EXISTS public.tasks_task_id_seq;
CREATE SEQUENCE public.tasks_task_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

DROP SEQUENCE IF EXISTS public.tasks_task_execution_id_seq;
CREATE SEQUENCE public.tasks_task_execution_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

DROP SEQUENCE IF EXISTS public.source_environments_source_environment_id_seq;
CREATE SEQUENCE public.source_environments_source_environment_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 10000000000
  START 1
  CACHE 1;

DROP SEQUENCE IF EXISTS public.source_environment_roles_role_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.source_environment_roles_role_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 21
  CACHE 1;

  DROP SEQUENCE IF EXISTS public.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq;
  CREATE SEQUENCE public.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
DROP SEQUENCE IF EXISTS public.tasks_task_execution_id_seq;
CREATE SEQUENCE public.tasks_task_execution_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

DROP SEQUENCE IF EXISTS public.source_environments_source_environment_id_seq;
CREATE SEQUENCE public.source_environments_source_environment_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

DROP SEQUENCE IF EXISTS public.source_environment_roles_role_id_seq;
CREATE SEQUENCE IF NOT EXISTS public.source_environment_roles_role_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 21
  CACHE 1;

  DROP SEQUENCE IF EXISTS public.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq;
  CREATE SEQUENCE public.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
  
  -- TDM 5.1
  DROP SEQUENCE IF EXISTS public.tasks_ref_table_id_seq;
  CREATE SEQUENCE public.tasks_ref_table_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

  -- TDM 7.0.1
  DROP SEQUENCE IF EXISTS public.post_exe_process_id_seq;
  CREATE SEQUENCE public.post_exe_process_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

