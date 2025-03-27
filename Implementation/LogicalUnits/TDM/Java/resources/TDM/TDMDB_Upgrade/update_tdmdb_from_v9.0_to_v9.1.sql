INSERT INTO ${@schema}.tdm_general_parameters(param_name, param_value)
    VALUES ('PARAMS_COUPLING', 'false') ON CONFLICT DO NOTHING;

INSERT INTO ${@schema}.tdm_general_parameters(
            param_name, param_value)
            values ('ENABLE_PARAMETERS_AUTO_WIDTH', 'false') ON CONFLICT DO NOTHING;

ALTER TABLE ${@schema}.tdm_params_distinct_values ADD COLUMN IF NOT EXISTS field_type TEXT;

UPDATE ${@schema}.tdm_params_distinct_values 
SET field_type = CASE WHEN is_numeric = true THEN 'INTEGER' ELSE 'TEXT' END;
                 
ALTER TABLE ${@schema}.task_execution_list ADD COLUMN IF NOT EXISTS entity_inclusion_query TEXT;

ALTER TABLE ${@schema}.tasks_exe_process ADD COLUMN IF NOT EXISTS parameters TEXT;

ALTER TABLE ${@schema}.tasks ALTER COLUMN mask_sensitive_data DROP NOT NULL;