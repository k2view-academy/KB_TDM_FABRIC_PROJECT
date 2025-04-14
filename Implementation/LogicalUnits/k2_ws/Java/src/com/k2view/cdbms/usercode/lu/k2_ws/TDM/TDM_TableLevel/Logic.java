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

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;

import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetRetentionPeriod;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetTableFields;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.wrapWebServiceResults;
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
                if (interfaceRec.getActiveMode()) {
                    String interfaceType = interfaceRec.getTypeName();// == "DATABASE"
                    Map<String,Object> interfaceInput = new HashMap<>();
                    String intefaceName = interfaceRec.getName();
                    interfaceInput.put("interface_name", intefaceName);

                    List<Map<String, Object>> interfaceParams =  MtableLookup("TableLevelInterfaces",interfaceInput, MTable.Feature.caseInsensitive);
                    if (interfaceParams == null  || interfaceParams.isEmpty() || Boolean.parseBoolean(interfaceParams.get(0).get("suppress_indicator").toString()) == false) {
                        List<Map<String, Object>> interfaceTables = getInterfaceTables(intefaceName, interfaceType, source_env);
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
		
        fabric().execute("set environment='" + source_env + "';");
        
        String message = null;
        String errorCode = "";
        try {
            if (be_name == null || be_name.isEmpty()) {
                result = getTableByEnv(source_env);
            } else {
                
                Db.Rows luRows = db(TDM).fetch("SELECT pl.lu_name FROM " + 
                    TDMDB_SCHEMA + ".BUSINESS_ENTITIES be, " + TDMDB_SCHEMA + ".PRODUCT_LOGICAL_UNITS pl " + 
                    "WHERE be.be_name = ? AND be.be_status = 'Active' AND be.be_id = pl.be_id", be_name);
            
                for (Db.Row row : luRows) {
                    logicalUnits.add(row.get("lu_name").toString());
                }

                String luName;
            
                String broadwayCommand = "broadway TDM.refListLookup;";
                Db.Rows rows = fabric().fetch(broadwayCommand);
                ArrayList<String> tables = new ArrayList<>();
                for (Db.Row row : rows) {
                    Iterable<? extends Map<?, ?>> maps = ParamConvertor.toIterableOf(row.get("result"), ParamConvertor::toMap);
                    for (Map<?, ?> map : maps) {
                        luName = "" + map.get("lu_name");
                        if (logicalUnits.contains(luName)) {
                            String interfaceName = "" + map.get("interface_name");
                            String schemaName = "" + map.get("schema_name");
                            String tableName = "" + map.get("reference_table_name");

                            //TDM9.3.1 - Support dynamitc schema name
                            if (schemaName.startsWith("@")) {
                                String globalName = schemaName.replaceAll("@", "");
                                schemaName = getGlobal(globalName, luName);
                            }
                            
                            tables.add(interfaceName + "##" + schemaName + "##" + luName + "##" + tableName);
                        }
                    }
                }
                Collections.sort(tables);

                String interfaceName = "";
                String schemaName = ""; 
                String currentInterface = "";
                String currentSchema = "";
                
                HashMap<String, ArrayList<HashMap<String, Object>>> schemaData = new HashMap<>();
                ArrayList<HashMap<String, ArrayList<HashMap<String, Object>>>> interfaceData = new ArrayList<>();
                for (String str : tables) {
                    SortedMap<String, Object> tableData = new TreeMap<>();
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
                        currentInterface = interfaceName;
                        currentSchema = schemaName;
                        tableArray.add(new HashMap<>(tableData));
                        schemaData.put(currentSchema, tableArray);
                        interfaceData.add(new HashMap<>(schemaData));
                        
                    }
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
	    } catch (Exception e) {
		    message = e.getMessage();
		    log.error(message);
		    errorCode = "FAILED";
		}
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
            "and t.sync_mode != 'OFF' " +
            "and t.retention_period_value != 0 " +
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

    private static List<Map<String, Object>> getInterfaceTables(String dbInterfaceName, String interfaceType, String envName) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();

        Map<String,Object> interfaceInput = new HashMap<>();
        interfaceInput.put("dataPlatform", dbInterfaceName);

        List<Map<String, Object>> interfaceTables =  MtableLookup("catalog_field_info",interfaceInput, MTable.Feature.caseInsensitive);
		if (interfaceTables == null  || interfaceTables.isEmpty()) {
            if ("DATABASE".equalsIgnoreCase(interfaceType)) {
                result  = getIntefaceTablesByJDBC(dbInterfaceName, envName);
            }
        } else {
            result = getIntefaceTablesByCatalog(dbInterfaceName, envName, interfaceTables);
        }

		return result;
	}

    private static List<Map<String, Object>> getIntefaceTablesByJDBC(String dbInterfaceName, String envName) throws Exception{
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
                
                List<SortedMap<String, Object>> tableList  = new ArrayList<>();
			    rs = md.getTables(null, schemaName, "%", types);
			    while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    SortedMap<String, Object> tableData = getTableLastExecution(tableName, schemaName, dbInterfaceName, envName);
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
                
                List<SortedMap<String, Object>> tableList  = new ArrayList<>();
			    rs = md.getTables(catalogName, null, "%", types);
			    while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    SortedMap<String, Object> tableData = getTableLastExecution(tableName, catalogName, dbInterfaceName, envName);
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

            if (result.size() == 0) {
                List<SortedMap<String, Object>> tableList  = new ArrayList<>();
                rs = md.getTables(null, null, "%", types);
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    SortedMap<String, Object> tableData = getTableLastExecution(tableName, "main", dbInterfaceName, envName);
                    tableData.put("tableName", tableName);
				    tableList.add(tableData);
			    }
                if (tableList.size() > 0) {
                    Map <String, Object> tablesMap = new HashMap<>();
                    tablesMap.put("main", tableList);
                    result.add(tablesMap);
                }
            }

            return result;
        
        } catch( Exception e) {
            e.printStackTrace();
			throw new RuntimeException("Failed to get Meta Data of Interface " + dbInterfaceName + ", with Error Message: " + e.getMessage());
		} finally {
            if (rs != null) {
                rs.close();
            }
		}
    }


    private static List<Map<String, Object>> getIntefaceTablesByCatalog(String interfaceName, String envName, List<Map<String, Object>> interfaceData) throws Exception{
        List<Map<String, Object>> result = new ArrayList<>();

        Set<String> schemaSet = new HashSet<>();
        Map<String, SortedSet<String>> schemaTables = new HashMap<>();
       
        for (Map<String, Object> fieldRec : interfaceData) {
            String schemaName = fieldRec.get("schema").toString();
            String tableName = fieldRec.get("dataset").toString();
            
            if(!schemaSet.contains(schemaName)) {
                schemaSet.add(schemaName);
                schemaTables.put(schemaName,  new TreeSet<>(Arrays.asList(tableName)));
            } else {
                if (!schemaTables.get(schemaName).contains(tableName)) {
                    (schemaTables.get(schemaName)).add(tableName);
                }
            }

        }
        for (Map.Entry<String, SortedSet<String>> entry : schemaTables.entrySet()){
            String schema = entry.getKey();
            Map <String, Object> schemaMap = new HashMap<>();
            List<SortedMap<String, Object>> tableList  = new ArrayList<>();
            for (String table : entry.getValue()) {
                SortedMap<String, Object> tableData = getTableLastExecution(table, schema, interfaceName, envName);
                tableData.put("tableName", table);
                tableList.add(tableData);
            }
            schemaMap.put(schema, tableList);
            result.add(schemaMap);
        }
        return result;
    }

    private static SortedMap<String, Object> getTableLastExecution(String tableName, String schemaName, String interfaceName, String envName) throws Exception {
        SortedMap<String, Object > tableVersion = new TreeMap<>();

        String sql = "SELECT s.task_execution_id AS taskExecutionId, t.task_title AS taskName " +
            "FROM " + TDMDB_SCHEMA + ".task_ref_exe_stats s, " + TDMDB_SCHEMA + ".task_ref_tables rt, " + TDMDB_SCHEMA + ".tasks t " +
            "WHERE rt.ref_table_name = ? AND rt.task_ref_table_id = s.task_ref_table_id " +
            "AND rt.schema_name = ? AND rt.interface_name = ? AND rt.task_id = t.task_id " +
            "AND t.source_env_name = ? AND s.task_execution_id = (select MAX(s2.task_execution_id) " + 
            "FROM " + TDMDB_SCHEMA + ".task_ref_exe_stats s2, " + TDMDB_SCHEMA + ".task_execution_list l, " +
            TDMDB_SCHEMA + ".tasks t2 " +
            "WHERE s2.ref_table_name = ? " +
            "AND s2.task_ref_table_id = s.task_ref_table_id " +
            "AND s2.execution_status = 'completed' " + 
            "AND s2.task_execution_id = l.task_execution_id " +
            "AND t2.task_id = l.task_id AND t2.sync_mode != 'OFF' and t2.retention_period_value != 0 " +
            "AND (l.expiration_date is null OR l.expiration_date ='1970-01-01 00:00:00.0' OR l.expiration_date > CURRENT_TIMESTAMP AT TIME ZONE 'UTC'))";
        
        Db.Row tableData = db(TDM).fetch(sql, tableName, schemaName, interfaceName, envName, tableName).firstRow();
        if (tableData != null && !tableData.isEmpty()) {
            tableVersion.put("taskExecutionId", tableData.get("taskExecutionId"));
            tableVersion.put("taskName", tableData.get("taskName"));
        }
        return tableVersion;
    }

    @webService(path = "getTableFields", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = false)
    public static Object wsGetTableFields(String dbInterfaceName, String SchemaName, String tableName) throws Exception {
        HashMap<String,Object> response=new HashMap<>();
		List<HashMap<String, String>> result = new ArrayList<>();
		String errorCode="SUCCESS";
		String message=null;

        Map<String,Object> interfaceInput = new HashMap<>();
        interfaceInput.put("dataPlatform", dbInterfaceName);
        interfaceInput.put("schema", dbInterfaceName);
        interfaceInput.put("dataset", dbInterfaceName);

       result = fnGetTableFields(dbInterfaceName, SchemaName, tableName);

       response.put("errorCode",errorCode);
       response.put("message", message);
       response.put("result", result);

       return response;
    }

}
