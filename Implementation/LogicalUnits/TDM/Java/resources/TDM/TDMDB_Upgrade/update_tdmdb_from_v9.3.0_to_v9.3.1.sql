ALTER TABLE ${@schema}.task_exe_stats_detailed ADD COLUMN IF NOT EXISTS suppressed_error_count text;
UPDATE ${@schema}.task_exe_stats_detailed SET suppressed_error_count = diff;

INSERT INTO ${@schema}.tdm_general_parameters (param_name, param_value)
VALUES ('ENABLE_TASK_LU_EDITING_FOR_TESTERS', 'true') ON CONFLICT DO NOTHING;