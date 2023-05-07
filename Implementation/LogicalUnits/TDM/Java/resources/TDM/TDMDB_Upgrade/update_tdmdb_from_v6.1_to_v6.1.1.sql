-- New Table task_exe_error_detailed, TDM 6.1.1

CREATE TABLE IF NOT EXISTS ${@schema}.task_exe_error_detailed
(
        task_execution_id bigint NOT NULL,
        lu_name character varying(200) NOT NULL,
        entity_id text NOT NULL,
        iid character varying(50) NOT NULL,
        target_entity_id text NOT NULL,
        etl_execution_id numeric(10,0),
        fabric_execution_id character varying(200),
        error_msg character varying(4000) NOT NULL,
        creation_date timestamp without time zone NOT NULL DEFAULT now()
);

-- Index: task_exe_error_detailed_1ix

-- DROP INDEX ${@schema}.task_exe_error_detailed_1ix;

CREATE INDEX IF NOT EXISTS task_exe_error_detailed_1ix ON ${@schema}.task_exe_error_detailed (task_execution_id, lu_name, target_entity_id);