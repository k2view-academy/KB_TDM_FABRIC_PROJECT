ALTER TABLE ${@schema}.business_entities ADD COLUMN IF NOT EXISTS execution_mode TEXT DEFAULT 'HORIZONTAL';

ALTER TABLE ${@schema}.tasks ADD COLUMN IF NOT EXISTS execution_mode TEXT DEFAULT 'INHERITED';

ALTER TABLE ${@schema}.tasks 
ALTER COLUMN filterout_reserved TYPE TEXT
USING CASE 
    WHEN filterout_reserved = true THEN 'OTHERS'
    WHEN filterout_reserved = false THEN 'NA'
    ELSE NULL 
END;

INSERT INTO ${@schema}.tdm_general_parameters(param_name, param_value)
    VALUES ('ADD_LU_NAME_TO_PARAM_NAME', 'false') ON CONFLICT DO NOTHING;