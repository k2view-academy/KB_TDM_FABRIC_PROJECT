/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.TDMUpgrade;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.*;
import java.math.*;
import java.io.*;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.Globals;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.usercode.lu.TDM.*;
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.TDM.Globals.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnUpdateDistinctFieldData;

@SuppressWarnings({"DefaultAnnotationParam",  "unchecked"})
public class Logic extends UserCode {
    private static final String TDM = "TDM";
	public static void updateParamDistictValues() throws Exception {
		try {
		log.info("Starting updateParamDistictValues");
		
		String insertDistintValuesSql = "INSERT INTO " + TDMDB_SCHEMA + ".TDM_PARAMS_DISTINCT_VALUES " +
		    "(LU_NAME, FIELD_NAME, NUMBER_OF_VALUES, FIELD_VALUES, IS_NUMERIC, MIN_VALUE, MAX_VALUE) " +
		    "VALUES (?, ? ,?, string_to_array(?, '" + TDM_PARAMETERS_SEPARATOR + "'), ?, ?, ?)";
		
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
			String[] columnsArr = tableRow.get("columns").toString().split(",");
			for (int idx = 0; idx < columnsArr.length; idx++) {
				columnsArr[idx] = "array_to_string(" + columnsArr[idx] + ", '" + TDM_PARAMETERS_SEPARATOR + "') as " + columnsArr[idx];
			}
			String newSelClause = String.join(",", columnsArr);
		    String query = "SELECT " + newSelClause + " FROM " + tableName;
		    Db.Rows tableRecords = db(TDM).fetch(query);
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
		        db(TDM).execute(insertDistintValuesSql, luName.toUpperCase(), key, numberOfValues, newFieldvalues, isNumeric, minValue, maxValue);
		
		    }
		}
		log.info("Finished updateParamDistictValues");
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
