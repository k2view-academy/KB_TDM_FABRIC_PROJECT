/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.TDMUpgrade;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.UserCode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnUpdateDistinctFieldData;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.lu.TDM.Globals.TDM_PARAMETERS_SEPARATOR;

@SuppressWarnings({"DefaultAnnotationParam",  "unchecked"})
public class Logic extends UserCode {
    private static final String TDM = "TDM";
	public static void updateParamDistictValues() throws Exception {
		try {
		//log.info("Starting updateParamDistictValues");
		
		String insertDistintValuesSql = "INSERT INTO " + TDMDB_SCHEMA + ".TDM_PARAMS_DISTINCT_VALUES " +
		    "(SOURCE_ENVIRONMENT, LU_NAME, FIELD_NAME, NUMBER_OF_VALUES, FIELD_VALUES, IS_NUMERIC, MIN_VALUE, MAX_VALUE) " +
		    "VALUES (?, ?, ? ,?, string_to_array(?, '" + TDM_PARAMETERS_SEPARATOR + "'), ?, ?, ?)";
		

		db(TDM).execute("truncate table " + TDMDB_SCHEMA + ".TDM_PARAMS_DISTINCT_VALUES");
		Db.Rows tableData = db(TDM).fetch("select table_name, '\"' || array_to_string(array_agg(column_name), '\",\"') || '\"' as columns " +
		    " FROM information_schema.columns where table_schema = '" + TDMDB_SCHEMA + "'" +
		    " and table_name like '%_params' and column_name like '%.%'" +
		    " group by table_name" +
		    " order by table_name");
		
		for (Db.Row tableRow : tableData) {
		    String tableName = tableRow.get("table_name").toString();
		    String luName = tableName.split("_params")[0].toUpperCase();
			//log.info("luName: " + luName + ", tableName: " + tableName);
            Db.Rows srcEnvList = db(TDM).fetch("select distinct source_environment from " + tableName);
            for (Db.Row srvEnvRec : srcEnvList) {
            
                String srcEnv = srvEnvRec.get("source_environment").toString();
                String[] columnsArr = tableRow.get("columns").toString().split(",");
                for (int idx = 0; idx < columnsArr.length; idx++) {
                    columnsArr[idx] = "array_to_string(" + columnsArr[idx] + ", '" + TDM_PARAMETERS_SEPARATOR + "') as " + columnsArr[idx];
                }
                String newSelClause = String.join(",", columnsArr);
                String query = "SELECT " + newSelClause + " FROM " +  TDMDB_SCHEMA + "." + tableName + " where source_environment = ?";
                Db.Rows tableRecords = db(TDM).fetch(query, srcEnv);
                List<String> columnNames = tableRecords.getColumnNames();
                Map<String, Map<String, Object>> fieldValues = new HashMap<>();
                for (Db.Row row : tableRecords) {
                    //ResultSet resultSet = row.resultSet();
                    for (String columnName : columnNames) {
                        //log.info("column_name: " + columnName);
                        if (row.get(columnName) != null) {
                            //log.info("column_name: " + columnName +", value: " + row.get(columnName));
                            String value = row.get(columnName).toString();
                            value = value.replace("{", "");
                            value = value.replace("}", "");
                            HashSet<String> values = new HashSet<String>(Arrays.stream(value.split(TDM_PARAMETERS_SEPARATOR)).collect(Collectors.toSet()));
            
                            fieldValues = fnUpdateDistinctFieldData(columnName, fieldValues, values);
                        }
                    }
                }
            
                for (String key : fieldValues.keySet()) {
                    Map<String, Object> fieldinfo = fieldValues.get(key);
                    Long numberOfValues = Long.parseLong(fieldinfo.get("numberOfValues").toString());
                    Boolean isNumeric  = Boolean.parseBoolean(fieldinfo.get("isNumeric").toString());
                    String minValue = fieldinfo.get("minValue").toString();
                    String maxValue = fieldinfo.get("maxValue").toString();
            
                    HashSet<String> valuesSet = (HashSet<String>)fieldinfo.get("fieldValues");
                    String newFieldvalues= String.join(TDM_PARAMETERS_SEPARATOR, valuesSet);
                    //newFieldvalues = "{" + newFieldvalues + "}";
                    //log.info("Loading - columnName:"  + key + ", numberOfValues: " + numberOfValues);
                    db(TDM).execute(insertDistintValuesSql, srcEnv, luName.toUpperCase(), key, numberOfValues, newFieldvalues, isNumeric, minValue, maxValue);
            
                }
            }
		}
		//log.info("Finished updateParamDistictValues");
		} catch (Exception e) {
		    StringWriter sw = new StringWriter();
		    PrintWriter pw = new PrintWriter(sw);
		    e.printStackTrace(pw);
		    String sStackTrace = sw.toString();
		    e.printStackTrace();
		    log.error("Failed - " + sStackTrace);
		}
	}
	
}
