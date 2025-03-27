ALTER TABLE ${@schema}.business_entities ADD COLUMN IF NOT EXISTS execution_mode TEXT DEFAULT 'HORIZONTAL';

ALTER TABLE ${@schema}.tasks ADD COLUMN IF NOT EXISTS execution_mode TEXT DEFAULT 'INHERITED';

ALTER TABLE ${@schema}.tasks 
ALTER COLUMN filterout_reserved TYPE TEXT;

ALTER TABLE ${@schema}.tasks 
ALTER COLUMN task_created_by TYPE TEXT;

ALTER TABLE ${@schema}.tasks 
ALTER COLUMN task_last_updated_by TYPE TEXT;

UPDATE ${@schema}.tasks SET filterout_reserved =
     CASE WHEN filterout_reserved = 'true' THEN 'OTHERS'
          WHEN filterout_reserved = 'false' THEN 'NA'
          ELSE filterout_reserved END;

INSERT INTO ${@schema}.tdm_general_parameters(param_name, param_value)
    VALUES ('ADD_LU_NAME_TO_PARAM_NAME', 'false') ON CONFLICT DO NOTHING;