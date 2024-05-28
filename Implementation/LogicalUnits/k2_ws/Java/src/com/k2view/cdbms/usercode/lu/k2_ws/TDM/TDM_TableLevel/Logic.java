/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_TableLevel;

//import com.k2view.cdbms.FabricEncryption.FabricEncryption;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.GlobalProperties;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.fabric.api.endpoint.Endpoint.MethodType;
import com.k2view.fabric.api.endpoint.Endpoint.Produce;
import com.k2view.fabric.api.endpoint.Endpoint.resultMetaData;
import com.k2view.fabric.api.endpoint.Endpoint.webService;
import com.k2view.fabric.common.Json;
import com.k2view.cdbms.interfaces.FabricInterface;
import com.k2view.cdbms.lut.InterfacesManager;
import com.k2view.fabric.common.ParamConvertor;
import com.k2view.fabric.common.mtable.MTable;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetRetentionPeriod;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.wrapWebServiceResults;
import static com.k2view.cdbms.usercode.common.TDM.TemplateUtils.SharedLogic.toSqliteType;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.fabric.api.endpoint.Endpoint.*;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.*;

@SuppressWarnings({"DefaultAnnotationParam"})
public class Logic extends WebServiceUserCode {
	
	public static final String TDM = "TDM";
	
	private static List<HashMap<String, Object>> getTableByEnv(String source_env) throws Exception {
		
	    List<HashMap<String, Object>> result = new ArrayList<>();

		Set<FabricInterface> interfaces = InterfacesManager.getInstance().getAllInterfaces(source_env);
		for(FabricInterface interfaceRec : interfaces) {
		    if (interfaceRec.getActiveMode() && interfaceRec.getTypeName() == "DATABASE") {
                Map<String,Object> interfaceInput = new HashMap<>();
		        String intefaceName = interfaceRec.getName();
                interfaceInput.put("interface_name", intefaceName);

                List<Map<String, Object>> interfaceParams =  MtableLookup("TableLevelInterfaces",interfaceInput, MTable.Feature.caseInsensitive);
		        if (interfaceParams == null  || interfaceParams.isEmpty() || Boolean.parseBoolean(interfaceParams.get(0).get("suppress_indicator").toString()) == false) {
		            List<Map<String, Object>> interfaceTables = getInterfaceTables(intefaceName, source_env);
		            HashMap<String, Object> interfaceMap = new HashMap<>();
		            interfaceMap.put(intefaceName, interfaceTables);
		            result.add(interfaceMap);
		        }
		    }
	    }
		return result;
	}

	@desc("Get Tables By Business Entity And Environment")
	@webService(path = "getTableByBeAndEnv", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"reference_table_name\": \"devicestable2017\",\r\n" +
			"      \"logical_unit_name\": \"Customer\",\r\n" +
			"      \"interface_name\": \"CRM_DB\",\r\n" +
			"      \"target_interface_name\": \"CRM_DB\",\r\n" +
			"      \"schema_name\": \"public\",\r\n" +
			"      \"count_indicator\": \"TRUE\",\r\n" +
			"      \"truncate_indicator\": \"TRUE\",\r\n" +
			"      \"table_pk_list\": null,\r\n" +
			"      \"lu_name\": \"Customer\",\r\n" +
			"      \"count_flow\": null,\r\n" +
			"      \"target_schema_name\": \"public\",\r\n" +
			"      \"id\": \"1\",\r\n" +
			"      \"target_ref_table_name\": \"devicestable2017\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"reference_table_name\": \"orders\",\r\n" +
			"      \"logical_unit_name\": \"Customer\",\r\n" +
			"      \"interface_name\": \"ORDERS_DB\",\r\n" +
			"      \"target_interface_name\": \"ORDERS_DB\",\r\n" +
			"      \"schema_name\": \"public\",\r\n" +
			"      \"count_indicator\": \"TRUE\",\r\n" +
			"      \"truncate_indicator\": \"TRUE\",\r\n" +
			"      \"table_pk_list\": null,\r\n" +
			"      \"lu_name\": \"Customer\",\r\n" +
			"      \"count_flow\": null,\r\n" +
			"      \"target_schema_name\": \"public\",\r\n" +
			"      \"id\": \"2\",\r\n" +
			"      \"target_ref_table_name\": \"orders\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"reference_table_name\": \"contract_offer_mapping\",\r\n" +
			"      \"logical_unit_name\": \"Customer\",\r\n" +
			"      \"interface_name\": \"BILLING_DB\",\r\n" +
			"      \"target_interface_name\": \"BILLING_DB\",\r\n" +
			"      \"schema_name\": \"public\",\r\n" +
			"      \"count_indicator\": \"TRUE\",\r\n" +
			"      \"truncate_indicator\": \"TRUE\",\r\n" +
			"      \"table_pk_list\": null,\r\n" +
			"      \"lu_name\": \"Customer\",\r\n" +
			"      \"count_flow\": null,\r\n" +
			"      \"target_schema_name\": \"public\",\r\n" +
			"      \"id\": \"3\",\r\n" +
			"      \"target_ref_table_name\": \"contract_offer_mapping\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"reference_table_name\": \"case_note\",\r\n" +
			"      \"logical_unit_name\": \"Customer\",\r\n" +
			"      \"interface_name\": \"CRM_DB\",\r\n" +
			"      \"target_interface_name\": \"CRM_DB\",\r\n" +
			"      \"schema_name\": \"public\",\r\n" +
			"      \"count_indicator\": \"TRUE\",\r\n" +
			"      \"truncate_indicator\": \"TRUE\",\r\n" +
			"      \"table_pk_list\": null,\r\n" +
			"      \"lu_name\": \"Customer\",\r\n" +
			"      \"count_flow\": null,\r\n" +
			"      \"target_schema_name\": \"public\",\r\n" +
			"      \"id\": \"5\",\r\n" +
			"      \"target_ref_table_name\": \"case_note\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetTableByBeAndEnv(String be_name, String source_env) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		List<String> logicalUnits = new ArrayList<>();
        List<HashMap<String, Object>> result = new ArrayList<>();
		
        String message = null;
        String errorCode = "";
        if (be_name == null || be_name.isEmpty()) {
            result = getTableByEnv(source_env);
        } else {
            //try {
                Db.Rows luRows = db(TDM).fetch("SELECT pl.lu_name FROM " + 
                    TDMDB_SCHEMA + ".BUSINESS_ENTITIES be, " + TDMDB_SCHEMA + ".PRODUCT_LOGICAL_UNITS pl " + 
                    "WHERE be.be_name = ? AND be.be_status = 'Active' AND be.be_id = pl.be_id", be_name);
            
                for (Db.Row row : luRows) {
                    logicalUnits.add(row.get("lu_name").toString());
                }

                String luName;
            
                String broadwayCommand = "broadway TDM.refListLookup;";
                Db.Rows rows = fabric().fetch(broadwayCommand);
                //Map<String, HashMap<String, ArrayList<Map<String, String>>>> interfacesMaps = new HashMap<>();
                ArrayList<String> tables = new ArrayList<>();
                for (Db.Row row : rows) {
                    Iterable<? extends Map<?, ?>> maps = ParamConvertor.toIterableOf(row.get("result"), ParamConvertor::toMap);
                    for (Map<?, ?> map : maps) {
                        luName = "" + map.get("lu_name");
                        if (logicalUnits.contains(luName)) {
                            String interfaceName = "" + map.get("interface_name");
                            String schemaName = "" + map.get("schema_name");
                            String tableName = "" + map.get("reference_table_name");
                            
                            tables.add(interfaceName + "##" + schemaName + "##" + luName + "##" + tableName);
                        }
                    }
                }
                Collections.sort(tables);

                String interfaceName = "";
                String schemaName = ""; 
                String currentInterface = "";
                String currentSchema = "";
                String previousInterface = "";
                
                HashMap<String, ArrayList<HashMap<String, Object>>> schemaData = new HashMap<>();
                ArrayList<HashMap<String, ArrayList<HashMap<String, Object>>>> interfaceData = new ArrayList<>();
                for (String str : tables) {
                    HashMap<String, Object> tableData = new HashMap<>();
                    String[] strs = str.split("##");
                    interfaceName = strs[0];
                    schemaName = strs[1];
                    luName = strs[2];
                    String tableName = strs[3];
                    tableData = getTableLastExecution(tableName, schemaName, interfaceName, source_env);
                    tableData.put("tableName", tableName);
                    tableData.put("luName", luName);
                    
                    if (currentInterface.equals(interfaceName)) {
                        if (currentSchema.equals(schemaName)) {
                            schemaData.get(currentSchema).add(new HashMap<>(tableData));
                        } else {
                            interfaceData.add(new HashMap<>(schemaData));
                            currentSchema = schemaName;
                            schemaData.clear();
                            ArrayList<HashMap<String, Object>> tableArray = new ArrayList<>();
                            tableArray.add(new HashMap<>(tableData));
                            schemaData.put(currentSchema, tableArray);

                        }
                    } else {
                        if(!"".equals(currentInterface)) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(currentInterface, new ArrayList<>(interfaceData));
                            interfaceData.clear();
                            result.add(map);
                        } 
                        schemaData.clear();
                        ArrayList<HashMap<String, Object>> tableArray = new ArrayList<>();
                        previousInterface = currentInterface;
                        currentInterface = interfaceName;
                        currentSchema = schemaName;
                        tableArray.add(new HashMap<>(tableData));
                        schemaData.put(currentSchema, tableArray);
                        interfaceData.add(new HashMap<>(schemaData));
                        
                    }
                }
                
                if (!previousInterface.equals(interfaceName)) {
                    interfaceData.clear();
                } 

                interfaceData.add(schemaData);
                HashMap<String, Object> map = new HashMap<>();
                map.put(currentInterface, new ArrayList<>(interfaceData));
                result.add(map);
                
		        if (rows != null) {
    		        rows.close();
	    	    }
            }
		    errorCode = "SUCCESS";
		/*   } catch (Exception e) {
		    message = e.getMessage();
		    log.error(message);
		    errorCode = "FAILED";
		}*/
        response.put("result", result);
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Get Table's Versions")
	@webService(path = "getTableVersions", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"task_name\": \"task1\",\r\n" +
			"      \"task_description\": \"\",\r\n" +
			"      \"executed_by\": \"tahata@k2view.com##[k2view_k2v_user]\",\r\n" +
			"      \"execution_datetime\": \"2024-02-13 07:46:01.232883\",\r\n" +
			"      \"task_execution_id\": 1,\r\n" +
			"      \"number_of_records\": 10\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetTableVersions(String table_name, String env_name) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		List<HashMap<String, Object>> result = new ArrayList<>();
		String errorCode="SUCCESS";
		String message=null;
		
		String sql = "Select distinct t.task_title, exe.task_execution_id, split_part(l.task_executed_by, '##', 1) as task_executed_by, " +
		    "exe.start_time, t.task_description, exe.number_of_processed_records " +
		    "from " + TDMDB_SCHEMA + ".task_ref_tables ref, " + TDMDB_SCHEMA + ".task_ref_exe_stats exe , " +
		        TDMDB_SCHEMA + ".task_execution_list l, " + TDMDB_SCHEMA + ".tasks t " +
		    "Where ref.ref_table_name  = exe.ref_table_name " + 
		    "And ref.ref_table_name = ? " +
		    "And exe.execution_status = 'completed' " +
		    "and exe.task_execution_id = l.task_execution_id " +
		    "and lower(l.execution_status) = 'completed' " +
		    "and l.source_env_name = ? " +
		    "and (l.expiration_date is null OR l.expiration_date ='1970-01-01 00:00:00.0' OR l.expiration_date > CURRENT_TIMESTAMP AT TIME ZONE 'UTC')" +
		    "and l.task_id = t.task_id " +
            "and ref.task_id = t.task_id " +
            "order by exe.task_execution_id desc";
		    
		Db.Rows rows = db(TDM).fetch(sql, table_name, env_name);
		
		for (Db.Row row : rows) {
		    HashMap<String, Object> map = new HashMap<>();
		    map.put("task_name", row.get("task_title"));
		    map.put("task_execution_id", row.get("task_execution_id"));
		    map.put("execution_datetime", row.get("start_time"));
            String[] executeBy = row.get("task_executed_by").toString().split("##");
		    map.put("executed_by", executeBy[0]);
		    map.put("task_description", row.get("task_description"));
		    map.put("number_of_records", row.get("number_of_processed_records"));
		
		    result.add(map);
		}
		
		response.put("errorCode",errorCode);
		response.put("message", message);
		response.put("result", result);
		return response;
	}

    private static List<Map<String, Object>> getInterfaceTables(String dbInterfaceName, String envName) throws Exception {
		ResultSet rs = null;
        String[] types = {"TABLE"};
		List<Map<String, Object>> result = new ArrayList<>();
		try {
			DatabaseMetaData md = getConnection(dbInterfaceName).getMetaData();
            ResultSet schemas = md.getSchemas();
            List<String> schemaList = new ArrayList<>();
            List<String> catalogList = new ArrayList<>();

            while (schemas.next()) {
                schemaList.add(schemas.getString("TABLE_SCHEM"));
            }
            
            if (schemaList.size() == 0) {
                ResultSet catalogs = md.getCatalogs();
                while (catalogs.next()) {
                    catalogList.add(catalogs.getString("TABLE_CAT"));
                } 
                if (catalogs != null) {
                    catalogs.close();
                }
            }

            if (schemas != null) {
                schemas.close();
            }

            for (String schemaName : schemaList) {
                Map <String, Object> schemaMap = new HashMap<>();
                
                List<Map<String, Object>> tableList  = new ArrayList<>();
			    rs = md.getTables(null, schemaName, "%", types);
			    while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    Map<String, Object> tableData = getTableLastExecution(tableName, schemaName, dbInterfaceName, envName);
                    tableData.put("tableName", tableName);
				    tableList.add(tableData);
			    }
                if (tableList.size() > 0) {
                    schemaMap.put(schemaName, tableList);
                    result.add(schemaMap);
                }
                
                if (rs != null) {
				    rs.close();
                }
            }
            for (String catalogName : catalogList) {
                Map <String, Object> catalogMap = new HashMap<>();
                
                List<Map<String, Object>> tableList  = new ArrayList<>();
			    rs = md.getTables(catalogName, null, "%", types);
			    while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    Map<String, Object> tableData = getTableLastExecution(tableName, catalogName, dbInterfaceName, envName);
                    tableData.put("tableName", tableName);
				    tableList.add(tableData);
			    }
                if (tableList.size() > 0) {
                    catalogMap.put(catalogName, tableList);
                    result.add(catalogMap);
                }
                
                if (rs != null) {
				    rs.close();
                }
            }
           
		} finally {
            if (rs != null) {
                rs.close();
            }
		}
		return result;
	}


    private static HashMap<String, Object> getTableLastExecution(String tableName, String schemaName, String interfaceName, String envName) throws Exception {
        HashMap<String, Object > tableVersion = new HashMap<>();

        String sql = "SELECT s.task_execution_id AS taskExecutionId, t.task_title AS taskName " +
            "FROM " + TDMDB_SCHEMA + ".task_ref_exe_stats s, " + TDMDB_SCHEMA + ".task_ref_tables rt, " + TDMDB_SCHEMA + ".tasks t " +
            "WHERE rt.ref_table_name = ? AND rt.task_ref_table_id = s.task_ref_table_id " +
            "AND rt.schema_name = ? AND rt.interface_name = ? AND rt.task_id = t.task_id " +
            "AND t.source_env_name = ? AND s.task_execution_id = (select MAX(s2.task_execution_id) " + 
            "FROM " + TDMDB_SCHEMA + ".task_ref_exe_stats s2, " + TDMDB_SCHEMA + ".task_execution_list l " +
            "WHERE s2.ref_table_name = ? " +
            "AND s2.execution_status = 'completed' " + 
            "AND s2.task_execution_id = l.task_execution_id " +
            "AND (l.expiration_date is null OR l.expiration_date ='1970-01-01 00:00:00.0' OR l.expiration_date > CURRENT_TIMESTAMP AT TIME ZONE 'UTC'))";
        
        Db.Row tableData = db(TDM).fetch(sql, tableName, schemaName, interfaceName, envName, tableName).firstRow();
        if (tableData != null && !tableData.isEmpty()) {
            tableVersion.put("taskExecutionId", tableData.get("taskExecutionId"));
            tableVersion.put("taskName", tableData.get("taskName"));
        }
        return tableVersion;
    }

    @webService(path = "getTableFields", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = false)
    public static Object wsGetTableFields(String dbInterfaceName, String SchemaName, String tableName) throws SQLException {
        HashMap<String,Object> response=new HashMap<>();
		List<HashMap<String, String>> result = new ArrayList<>();
		String errorCode="SUCCESS";
		String message=null;

        DatabaseMetaData metaData = getConnection(dbInterfaceName).getMetaData();
        ResultSet columns = metaData.getColumns(null, SchemaName, tableName, null);
        
        while (columns.next()) {
            HashMap<String, String> map = new HashMap<>();
            
            map.put("column_name", columns.getString("COLUMN_NAME"));
            int dataType = columns.getInt("DATA_TYPE");
            String columnType = toSqliteType(dataType);
            String generalColumnType = "TEXT";
            Boolean addField = true;
            switch (columnType) {
                case "INTEGER":
                case "REAL":
                    generalColumnType = "NUMBER";
                    break;
                case "TEXT":
                    generalColumnType = "TEXT";
                    break;
                case "BLOB":
                    addField = false;
                    break;
                default:
                    generalColumnType = "TEXT";
                    break;
            }
            if (addField) {
                map.put("column_name", columns.getString("COLUMN_NAME"));
                map.put("column_type", generalColumnType);
                result.add(map);
            }
        }

        if (columns != null) {
            columns.close();
        }
        response.put("errorCode",errorCode);
		response.put("message", message);
		response.put("result", result);
		return response;
    }

    public static Map<Integer, Set<String>> fnGetTablesOrder(Set<String> tableList, String dbInterfaceName, String dbSchemaName) throws Exception {
        DatabaseMetaData md = getConnection(dbInterfaceName).getMetaData();
        Map <String, Set<String>> tableChildren = new HashMap<>();

        for (String tableName : tableList) {
            ResultSet exportedKeys = md.getExportedKeys(null, dbSchemaName, tableName);

            tableChildren.put(tableName, new HashSet<>());
            while (exportedKeys.next()) {
                String childTable = exportedKeys.getString("FKTABLE_NAME");
                if (tableList.contains(childTable)) {
                    tableChildren.get(tableName).add(childTable);
                }
            }

            if (exportedKeys != null) {
                exportedKeys.close();
            }
        }

        Map <String, Set<String>> tableChildrenBck = new HashMap<>(tableChildren);
        //tableChildrenBck = tableChildren;
        Map<String, Integer> visited = new HashMap<>();
        // Add leaf nodes (tables without incoming FKs)

        for (String tableName : tableChildrenBck.keySet()) {
            if (tableChildrenBck.get(tableName).isEmpty()) {
                visited.put(tableName, 0);
                tableChildren.remove(tableName);
            }
        }
        while(tableChildren != null && !tableChildren.isEmpty()) {
            Map <String, Set<String>> tableChildrenBck2 = new HashMap<>(tableChildren);
            for (String tableName : tableChildrenBck2.keySet()) {
                Set<String> remainingTables = new HashSet<>(tableChildren.get(tableName));
                Integer order = 0;
                for (String childTable : remainingTables) {
                    if (visited.get(childTable) != null) {
                        Integer tableOrder = visited.get(childTable);
                        if (order < tableOrder + 1) {
                            order = tableOrder + 1;
                        }
                        tableChildren.get(tableName).remove(childTable);
                    }
                }
                if (tableChildren.get(tableName).size() == 0) {
                    visited.put(tableName, order);
                    tableChildren.remove(tableName);
                }
            }

        }
        Map<Integer, Set<String>> result = new HashMap<>();
        for(Map.Entry<String, Integer> entry : visited.entrySet()) {
            String tableName = entry.getKey();
            Integer order = entry.getValue();
            if(result.get(order) != null) {
                result.get(order).add(tableName);
            } else {
                Set<String> set = new HashSet<>();
                set.add(tableName);
                result.put(order, set);
            }
        }
        return result;
    }
}
