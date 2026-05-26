ALTER TABLE ${@schema}.task_ref_tables ADD COLUMN IF NOT EXISTS filter_fields text;
