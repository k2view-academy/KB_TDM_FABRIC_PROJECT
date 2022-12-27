-- Table: public.task_execution_override_attrs - TDM 7.2

-- DROP TABLE public.task_execution_override_attrs;

CREATE TABLE IF NOT EXISTS public.task_execution_override_attrs
(
	task_id bigint NOT NULL,
    task_execution_id bigint NOT NULL,
    override_parameters json NOT NULL,
	CONSTRAINT task_execution_override_attrs_pkey PRIMARY KEY (task_execution_id, task_id)
);

-- TDM 7.2 - To support task execution overrode attributes, the unique index on task_execution_list table should be removed
DROP INDEX IF EXISTS TASK_EXEC_IX;

-- environment_role_users
-- update index
drop index if exists ENV_ROLE_USER_IX;

-- cleanup
DELETE FROM public.environment_role_users eru
USING public.environment_roles er
WHERE eru.environment_id=er.environment_id AND eru.role_id=er.role_id AND er.role_status='Inactive';

-- Increase size of param_value field in tdm_general_parameters table
alter table tdm_general_parameters alter column param_value type character varying(2000);

-- Add default parameters for TDM GUI
INSERT INTO public.tdm_general_parameters(
            param_name, param_value)
    VALUES ('tdm_gui_params', '{"maxRetentionPeriod":90,"defaultPeriod":{"unit":"Days","value":5},"permissionGroups":["admin","owner","tester"],"availableOptions":[{"name":"Minutes","units":0.00069444444},{"name":"Hours","units":0.04166666666},{"name":"Days","units":1},{"name":"Weeks","units":7},{"name":"Years","units":365}]}');


Create UNIQUE INDEX IF NOT EXISTS ENV_ROLE_USER_IX ON environment_role_users (environment_id, user_id);

-- Remove ADI fields
ALTER TABLE product_logical_units DROP COLUMN IF EXISTS lu_is_ref;
ALTER TABLE product_logical_units DROP COLUMN IF EXISTS execution_plan_name;
ALTER TABLE product_logical_units DROP COLUMN IF EXISTS last_executed_lu;
ALTER TABLE task_execution_list DROP COLUMN IF EXISTS last_executed_lu;
