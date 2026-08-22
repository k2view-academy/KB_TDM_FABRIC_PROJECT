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
import com.k2view.cdbms.interfaces.FileSystemInterface;
import com.k2view.cdbms.lut.InterfacesManager;
import com.k2view.fabric.common.ParamConvertor;
import com.k2view.fabric.common.mtable.MTable;
import com.k2view.fabric.common.mtable.MTables;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;

import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetRetentionPeriod;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetTableFields;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getAllSuppressedInterfaces;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getProductsForEnvironment;
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
import static com.k2view.cdbms.usercode.common.TDM.TDMRef.SharedLogic.*;

@SuppressWarnings({"DefaultAnnotationParam" , "unchecked"})
public class Logic extends WebServiceUserCode {

    private static final String TDM = "TDM";

    @desc("Get Tables By Business Entity And Environment")
    @webService(path = "getTableByBeAndEnv", verb = {
            MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
                                                {
              "result": [
                {
                  "interfaceName": "BILLING_DB",
                  "config": {
                    "max_number_of_workers": 4,
                    "affinity": ""
                  },
                  "schemas": [
                    {
                      "schemaName": "public",
                      "tables": [
                        {
                          "taskName": "all tables extract",
                          "taskExecutionId": 113,
                          "countIndicator": "true",
                          "luName": "Billing",
                          "tableName": "contract_offer_mapping"
                        }
                      ]
                    }
                  ]
                },
                {
                  "interfaceName": "CRM_DB",
                  "config": {
                    "max_number_of_workers": 6,
                    "affinity": "LOCAL_DC"
                  },
                  "schemas": [
                    {
                      "schemaName": "public",
                      "tables": [
                        {
                          "countIndicator": "false",
                          "luName": "Customer",
                          "tableName": "devicestable2017"
                        }
                      ]
                    }
                  ]
                }
              ],
              "errorCode": "SUCCESS",
              "message": null
            }
                                                """)
    public static Object wsGetTableByBeAndEnv(String be_name, String source_env, Long envId) throws Exception {
        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        String message = null;
        String errorCode = "SUCCESS";

        try {
            fabric().execute("set environment = ?", source_env);
            Map<String, Map<String, Object>> configLookup = buildConfigLookup(envId);

            // Standardized list of table records
            List<Map<String, Object>> flatTableList = new ArrayList<>();

            if (be_name == null || be_name.isEmpty()) {
                // --- BRANCH A: Logic (Table level) ---                
                List<Map<String, Object>> envData = getTableByEnv(source_env, configLookup);

                for (Map<String, Object> interfaceNode : envData) {
                    String intfName = (String) interfaceNode.get("interfaceName");
                    List<Map<String, Object>> rawSchemaList = (List<Map<String, Object>>) interfaceNode.get("tables");

                    for (Map<String, Object> schemaMap : rawSchemaList) {
                        for (Map.Entry<String, Object> entry : schemaMap.entrySet()) {
                            String schemaName = entry.getKey();
                            List<Map<String, Object>> tables = (List<Map<String, Object>>) entry.getValue();

                            for (Map<String, Object> t : tables) {
                                Map<String, Object> norm = new HashMap<>();
                                norm.put("interface_name", intfName);
                                norm.put("schema_name", schemaName);
                                norm.put("table_name", t.get("tableName"));
                                norm.put("lu_name", "TDM_TableLevel"); 
                                norm.put("count_indicator", t.get("countIndicator"));
                                flatTableList.add(norm);
                            }
                        }
                    }
                }
            } else {
                // --- BRANCH B: Logic (Business Entity level) ---
                List<String> logicalUnits = new ArrayList<>();
                String luSql = "SELECT pl.lu_name FROM " + TDMDB_SCHEMA + ".BUSINESS_ENTITIES be, " +
                        TDMDB_SCHEMA + ".PRODUCT_LOGICAL_UNITS pl " +
                        "WHERE be.be_name = ? AND be.be_status = 'Active' AND be.be_id = pl.be_id";

                try (Db.Rows luRows = db(TDM).fetch(luSql, be_name)) {
                    for (Db.Row row : luRows) {
                        logicalUnits.add(row.get("lu_name").toString());
                    }
                }

                try (Db.Rows rows = fabric().fetch("broadway TDM.refListLookup;")) {
                    for (Db.Row row : rows) {
                        Iterable<? extends Map<?, ?>> maps = ParamConvertor.toIterableOf(row.get("result"),
                                ParamConvertor::toMap);
                        for (Map<?, ?> map : maps) {
                            if (logicalUnits.contains(String.valueOf(map.get("lu_name")))) {
                                flatTableList.add(normalizeBroadwayMap(map));
                            }
                        }
                    }
                }
            }

            // --- FINAL STEP: Group everything into Interface > Schema > Table hierarchy
            result = performFinalGrouping(flatTableList, configLookup, source_env);

        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error("Error in wsGetTableByBeAndEnv: ", e);
        }

        response.put("result", result);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    private static List<Map<String, Object>> performFinalGrouping(List<Map<String, Object>> flatList,
            Map<String, Map<String, Object>> configLookup, String source_env) throws Exception {
        // Grouping: Interface -> Schema -> List of Table Objects
        Map<String, Map<String, List<Map<String, Object>>>> grouped = new LinkedHashMap<>();

        for (Map<String, Object> row : flatList) {
            String intf = (String) row.get("interface_name");
            String schema = (String) row.get("schema_name");
            String table = (String) row.get("table_name");

            // Fetch taskExecutionId and taskName
            Map<String, Object> execInfo = getTableLastExecution(table, schema, intf, source_env);

            Map<String, Object> tableObj = new LinkedHashMap<>(execInfo);
            tableObj.put("tableName", table);
            tableObj.put("luName", row.get("lu_name"));
            tableObj.put("countIndicator", String.valueOf(row.getOrDefault("count_indicator", "true")));

            grouped.computeIfAbsent(intf, k -> new LinkedHashMap<>())
                    .computeIfAbsent(schema, k -> new ArrayList<>())
                    .add(tableObj);
        }

        // Build final nested List — interfaces, schemas, and tables sorted alphabetically
        TreeMap<String, Map<String, List<Map<String, Object>>>> sortedGrouped =
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sortedGrouped.putAll(grouped);
        List<Map<String, Object>> finalResult = new ArrayList<>();
        sortedGrouped.forEach((intfName, schemasMap) -> {
            Map<String, Object> intfNode = new LinkedHashMap<>();
            intfNode.put("interfaceName", intfName);
            intfNode.put("config", configLookup.getOrDefault(intfName, createDefaultConfig()));

            TreeMap<String, List<Map<String, Object>>> sortedSchemas =
                    new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            sortedSchemas.putAll(schemasMap);
            List<Map<String, Object>> schemaList = new ArrayList<>();
            sortedSchemas.forEach((schemaName, tables) -> {
                tables.sort(Comparator.comparing(
                        (Map<String, Object> t) -> (String) t.get("tableName"), String.CASE_INSENSITIVE_ORDER));
                Map<String, Object> schemaNode = new LinkedHashMap<>();
                schemaNode.put("schemaName", schemaName);
                schemaNode.put("tables", tables);
                schemaList.add(schemaNode);
            });
            intfNode.put("schemas", schemaList);
            finalResult.add(intfNode);
        });
        return finalResult;
    }

    private static Map<String, Object> normalizeBroadwayMap(Map<?, ?> map) {
        Map<String, Object> norm = new HashMap<>();
        String lu = String.valueOf(map.get("lu_name"));
        String schema = String.valueOf(map.get("schema_name"));
        if (schema.startsWith("@"))
            schema = getGlobal(schema.substring(1, schema.length() -1), lu);

        norm.put("interface_name", String.valueOf(map.get("interface_name")));
        norm.put("schema_name", schema);
        norm.put("table_name", String.valueOf(map.get("reference_table_name")));
        norm.put("lu_name", lu);
        Object ciValue = map.get("count_indicator");
         //   tableObj.put("countIndicator", (ciValue == null || ciValue.toString().isEmpty()) ? "true" : ciValue.toString());
        norm.put("count_indicator", (ciValue == null || ciValue.toString().isEmpty()) ? "true" : ciValue.toString());
        return norm;
    }
            
	@desc("Get Table's Versions. If fromDate and toDate are not provided, all versions are returned.")
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

	public static Object wsGetTableVersions(String table_name, String env_name, String fromDate, String toDate) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        List<HashMap<String, Object>> result = new ArrayList<>();
        String errorCode = "SUCCESS";
        String message = null;

        List<Object> params = new ArrayList<>();
        params.add(table_name);
        params.add(env_name);

        String sql = "Select distinct t.task_title, exe.task_execution_id, split_part(l.task_executed_by, '##', 1) as task_executed_by, "
                +
                "exe.start_time, t.task_description, exe.number_of_processed_records " +
                "from " + TDMDB_SCHEMA + ".task_ref_tables ref, " + TDMDB_SCHEMA + ".task_ref_exe_stats exe , " +
                TDMDB_SCHEMA + ".task_execution_list l, " + TDMDB_SCHEMA + ".tasks t " +
                "Where ref.ref_table_name  = exe.ref_table_name and ref.schema_name = exe.schema_name and ref.interface_name = exe.interface_name " +
                "And ref.ref_table_name = ? " +
                "And exe.execution_status = 'completed' " +
                "AND LOWER(exe.execution_action) <> 'delete' " +
                "and exe.task_execution_id = l.task_execution_id " +
                "and lower(l.execution_status) = 'completed' " +
                "and l.source_env_name = ? " +
                "and (l.expiration_date is null OR l.expiration_date ='1970-01-01 00:00:00.0' OR l.expiration_date > CURRENT_TIMESTAMP AT TIME ZONE 'UTC')"
                +
                "and l.task_id = t.task_id " +
                "and ref.task_id = t.task_id " +
                "and t.sync_mode != 'OFF' " +
                "and t.retention_period_value != 0 ";

        if (fromDate != null && !fromDate.trim().isEmpty() && toDate != null && !toDate.trim().isEmpty()) {
            sql += "and exe.start_time::date >= ? and exe.start_time::date <= ? ";
            params.add(fromDate);
            params.add(toDate);
        }

        sql += "order by exe.task_execution_id desc";

        try (Db.Rows rows = db(TDM).fetch(sql, params.toArray())) {
            for (Db.Row row : rows) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("task_name", row.get("task_title"));
                map.put("task_execution_id", row.get("task_execution_id"));
                map.put("execution_datetime", row.get("start_time"));

                Object executedByObj = row.get("task_executed_by");
                String[] executeBy = (executedByObj != null) ? executedByObj.toString().split("##")
                        : new String[] { "" };

                map.put("executed_by", executeBy[0]);
                map.put("task_description", row.get("task_description"));
                map.put("number_of_records", row.get("number_of_processed_records"));

                result.add(map);
            }
        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error("Error in wsGetTableVersions: " + message, e);
        }

        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("result", result);
        return response;
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

       result = fnGetTableFields(dbInterfaceName, SchemaName, tableName, SchemaName);

       response.put("errorCode",errorCode);
       response.put("message", message);
       response.put("result", result);

       return response;
    }
    
    /* API #2: Get Schemas / Catalogs schmeas List – wsGetInterfaceSchemaList */
    @desc("Get Schemas/Catalogs list for a given interface in an environment")
    @webService(path = "getInterfaceSchemaList", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(
        mediaType = Produce.JSON,
        example = "{\n" +
                "  \"result\": [\"public\", \"sales\", \"main\"],\n" +
                "  \"errorCode\": \"SUCCESS\",\n" +
                "  \"message\": null\n" +
                "}"
    )
    public static Object wsGetInterfaceSchemaList(String environment, String interfaceName) throws Exception {
        Map<String, Object> response = new LinkedHashMap<>();
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        String errorCode = "SUCCESS";
        String message = null;

        try {
            if (environment == null || environment.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing 'Environment' input.");
            }
            if (interfaceName == null || interfaceName.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing 'Interface' input.");
            }
            fabric().execute("set environment='" + environment + "';");

            // 1) Try to get schemas from catalog mtable
            Set<String> schemasFromCatalog = getSchemasFromCatalog(interfaceName);
            if (!schemasFromCatalog.isEmpty()) {
                result.addAll(schemasFromCatalog);
            } else {
                // 2) JDBC metadata
                Set<String> jdbcSchemas = getSchemasViaJdbc(interfaceName);
                if (!jdbcSchemas.isEmpty()) {
                    result.addAll(jdbcSchemas);
                }
            }

        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error(message, e);
        }

        response.put("result", result);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    /* API #3: Get Tables’ List based on environment interface and schema fnGetSchemaTableList */
    @desc("Get Tables list for a given Interface/Schema in an Environment")
    @webService(path = "getSchemaTableList", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(
        mediaType = Produce.JSON,
        example = "{\n" +
                "  \"result\": [\"invoice\", \"offer\", \"payment\"],\n" +
                "  \"errorCode\": \"SUCCESS\",\n" +
                "  \"message\": null\n" +
                "}"
    )
    public static Object wsGetSchemaTableList(String environment, String interfaceName, String schemaName) throws Exception {
        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        String errorCode = "SUCCESS";
        String message = null;

        try {
            if (environment == null || environment.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing 'Environment' input.");
            }
            if (interfaceName == null || interfaceName.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing 'Interface' input.");
            }
            if (schemaName == null || schemaName.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing 'Schema' input.");
            }
            fabric().execute("set environment='" + environment + "';");

            // 1) Catalog : Get table from Catalog
            Set<String> tables = getTablesFromCatalog(interfaceName, schemaName);

            // 2) JDBC: If not found bring tables from JDBC
            if (tables.isEmpty()) {
                tables = getTablesViaJdbc(interfaceName, schemaName);
            }
            for (String table : tables){
                result.add(fnCheckTableLastExecution(environment, interfaceName, schemaName,table));
            }
            result.sort(Comparator.comparing(
                item -> item.get("table_name").toString(), String.CASE_INSENSITIVE_ORDER));

        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error(message, e);
        }

        response.put("result", result);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    public static Map<String, Object> fnCheckTableLastExecution(String environment,String interfaceName,String schemaName,String tableName) throws Exception {
        Map<String, Object> result   = new LinkedHashMap<>();
        String message   = null;
        try {
            String sql =
                "SELECT s.task_execution_id, t.task_title " +
                "FROM " + TDMDB_SCHEMA + ".task_ref_tables rt " +
                "JOIN " + TDMDB_SCHEMA + ".tasks t               ON t.task_id = rt.task_id " +
                "JOIN " + TDMDB_SCHEMA + ".task_ref_exe_stats s  ON s.task_id = rt.task_id AND s.ref_table_name = rt.ref_table_name AND s.schema_name = rt.schema_name AND s.interface_name = rt.interface_name " +
                "JOIN " + TDMDB_SCHEMA + ".task_execution_list l ON l.task_execution_id = s.task_execution_id " +
                "WHERE rt.interface_name = ? " +
                "  AND rt.schema_name = ? " +
                "  AND rt.ref_table_name = ? " +
                "  AND t.source_env_name = ? " +
                "  AND s.execution_status = 'completed' " +
                "  AND (l.expiration_date IS NULL OR l.expiration_date = '1970-01-01 00:00:00.0' OR l.expiration_date > CURRENT_TIMESTAMP AT TIME ZONE 'UTC') " +
                "  AND t.sync_mode != 'OFF' " +
                "  AND t.retention_period_value != 0 " +
                "ORDER BY s.task_execution_id DESC " +
                "LIMIT 1";

            Db.Row r = db(TDM).fetch(sql, interfaceName, schemaName, tableName, environment).firstRow();

            result.put("interface_name", interfaceName);
            result.put("schema_name", schemaName);
            result.put("table_name", tableName);
            result.put("count_ind", fnGetTableCountIndicator(interfaceName, schemaName, tableName, "count_ind",
                    "TableLevelDefinitions", "TDM_TableLevel"));

            if (r != null && !r.isEmpty()) {
                result.put("task_name", r.get("task_title"));
                result.put("task_execution_id", r.get("task_execution_id"));
            }
        } catch (Exception e) {
            message = e.getMessage();
            log.error(message, e);
        }
        return result;
    }

    private static Set<String> discoverInterfacesWithCatalog(Set<String> suppressedInterfaces) throws Exception {
        Set<String> interfacesFound = new HashSet<>();
        Set<Object> interfaceTables = MtableGetKeyValues("catalog_field_info", "dataPlatform");
        if (interfaceTables != null && !interfaceTables.isEmpty()) {
            for (Object interf : interfaceTables) {
                if (!suppressedInterfaces.contains(interf.toString())) {
                    interfacesFound.add(interf.toString());
                }
            }
        }
        return interfacesFound;
    }
    
    private static Set<String> getSchemasFromCatalog(String interfaceName) throws Exception {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        Map<String,Object> interfaceInput = new HashMap<>();
        interfaceInput.put("dataPlatform", interfaceName);
        List<Map<String, Object>> interfaceTables =  MtableLookup("catalog_field_info",interfaceInput, MTable.Feature.caseInsensitive);
        for (Map<String, Object> interf : interfaceTables) {
            Object sc = interf.get("schema");
            out.add(String.valueOf(sc));
        }
        return out;
    }

    private static Set<String> getSchemasViaJdbc(String interfaceName) throws Exception {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        ResultSet rsSchemas = null, rsCatalogs = null;
        try {
            DatabaseMetaData md = getConnection(interfaceName).getMetaData();

            rsSchemas = md.getSchemas();
            while (rsSchemas.next()) {
                String schema = rsSchemas.getString("TABLE_SCHEM");
                if (schema != null && !schema.isEmpty()) out.add(schema);
            }
            if (out.isEmpty()) {
                rsCatalogs = md.getCatalogs();
                while (rsCatalogs.next()) {
                    String catalog = rsCatalogs.getString("TABLE_CAT");
                    if (catalog != null && !catalog.isEmpty()) out.add(catalog);
                }
            }
        } catch (Exception e) {
            log.error("Schema discovery via JDBC failed for interface " + interfaceName + ": " + e.getMessage());
        } finally {
            if (rsSchemas != null) try { rsSchemas.close(); } catch (Exception ignore) {}
            if (rsCatalogs != null) try { rsCatalogs.close(); } catch (Exception ignore) {}
        }
        return out;
    }

    private static Set<String> getTablesFromCatalog(String interfaceName, String schemaName) throws Exception {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        Map<String,Object> interfaceInput = new HashMap<>();
        interfaceInput.put("dataPlatform", interfaceName);
        interfaceInput.put("schema", schemaName);
        List<Map<String, Object>> interfaceTables =  MtableLookup("catalog_field_info",interfaceInput, MTable.Feature.caseInsensitive);
        for (Map<String, Object> interf : interfaceTables) {
            Object tab = interf.get("dataset");
            out.add(String.valueOf(tab));
        }
        return out;
    }

    private static Set<String> getTablesViaJdbc(String interfaceName, String schemaOrCatalog) throws Exception {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        String[] types = new String[] {"TABLE"};
        DatabaseMetaData md = getConnection(interfaceName).getMetaData();
        // try as schema
        try (ResultSet rs = md.getTables(null, schemaOrCatalog, "%", types)) {
            while (rs.next()) {
                String tbl = rs.getString("TABLE_NAME");
                if (tbl != null && !tbl.isEmpty()) out.add(tbl);
            }
        } catch (Exception e) {
            log.error("getTables via schema failed for " + interfaceName + "." + schemaOrCatalog + ": " + e.getMessage());
        }

        // if nothing found, try as catalog
        if (out.isEmpty()) {
            try (ResultSet rs = md.getTables(schemaOrCatalog, null, "%", types)) {
                while (rs.next()) {
                    String tbl = rs.getString("TABLE_NAME");
                    if (tbl != null && !tbl.isEmpty()) out.add(tbl);
                }
            } catch (Exception e) {
                log.warn("getTables via catalog failed for " + interfaceName + "." + schemaOrCatalog + ": " + e.getMessage());
            }
        }

        return out;
    }
	
    private static List<Map<String, Object>> getTableByEnv(String source_env,
            Map<String, Map<String, Object>> configLookup) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();

        // 1. Get and Filter Fabric Interfaces based on active status and visibility
        Set<FabricInterface> interfaces = InterfacesManager.getInstance().getAllInterfaces(source_env);
        Set<String> suppressed = getAllSuppressedInterfaces();
        Set<String> withCatalog = discoverInterfacesWithCatalog(suppressed);

        for (FabricInterface iface : interfaces) {
            String interfaceName = iface.getName();
            String interfaceType = iface.getTypeName();

            // 2. TDM visibility rules filter
            if (iface.getActiveMode() && (!suppressed.contains(interfaceName) || withCatalog.contains(interfaceName))) {

                // 3. Fetch table details with count metadata
                List<Map<String, Object>> interfaceTables = getInterfaceTablesWithCountIndicator(interfaceName,
                        interfaceType, source_env);

                Map<String, Object> interfaceEntry = new LinkedHashMap<>();
                interfaceEntry.put("interfaceName", interfaceName);

                // 4. Use the pre-passed lookup for config
                interfaceEntry.put("config", configLookup.getOrDefault(interfaceName, createDefaultConfig()));
                interfaceEntry.put("tables", interfaceTables);

                result.add(interfaceEntry);
            }
        }
        return result;
    }

    private static List<Map<String, Object>> getInterfaceTablesWithCountIndicator(String interfaceName, String interfaceType, String envName) throws Exception {
        // Retrieve the base table structure (from Catalog or JDBC)
        List<Map<String, Object>> rawData = getInterfaceTables(interfaceName, interfaceType, envName);
        
        for (Map<String, Object> schemaMap : rawData) {
            for (Map.Entry<String, Object> entry : schemaMap.entrySet()) {
                String schemaName = entry.getKey();
                List<Map<String, Object>> tables = (List<Map<String, Object>>) entry.getValue();
                
                for (Map<String, Object> tableData : tables) {
                    String tableName = (String) tableData.get("tableName");
                    
                    // 1. Capture the return as an Object
                    Object countIndObj = fnGetTableCountIndicator(
                        interfaceName, 
                        schemaName, 
                        tableName, 
                        "count_ind", 
                        "TableLevelDefinitions", 
                        "TDM_TableLevel"
                    );
                    
                    // 2. Convert to String safely
                    // If the object is null, we usually default to "true" in TDM logic
                    String countInd = (countIndObj == null) ? "true" : String.valueOf(countIndObj);
                    
                    tableData.put("countIndicator", countInd);
                }
            }
        }
        return rawData;
    }
    
    private static SortedMap<String, Object> getTableLastExecution(String tableName, String schemaName, String interfaceName, String envName) throws Exception {
        SortedMap<String, Object > tableVersion = new TreeMap<>();
        String sql = "SELECT s.task_execution_id AS taskExecutionId, t.task_title AS taskName " +
            "FROM " + TDMDB_SCHEMA + ".task_ref_exe_stats s, " + TDMDB_SCHEMA + ".task_ref_tables rt, " + TDMDB_SCHEMA + ".tasks t " +
            "WHERE rt.ref_table_name = ? AND s.task_id = rt.task_id AND s.ref_table_name = rt.ref_table_name AND s.schema_name = rt.schema_name AND s.interface_name = rt.interface_name " +
            "AND rt.schema_name = ? AND rt.interface_name = ? AND rt.task_id = t.task_id " +
            "AND t.source_env_name = ? AND s.task_execution_id = (select MAX(s2.task_execution_id) " +
            "FROM " + TDMDB_SCHEMA + ".task_ref_exe_stats s2, " + TDMDB_SCHEMA + ".task_execution_list l, " +
            TDMDB_SCHEMA + ".tasks t2, " + TDMDB_SCHEMA + ".task_ref_tables rt2 " +
            "WHERE rt2.ref_table_name = ? " +
            "AND rt2.schema_name = ? AND rt2.interface_name = ? " +
            "AND s2.task_id = rt2.task_id AND s2.ref_table_name = rt2.ref_table_name AND s2.schema_name = rt2.schema_name AND s2.interface_name = rt2.interface_name " +
            "AND s2.execution_status = 'completed' " + 
            "AND s2.task_execution_id = l.task_execution_id " +
            "AND t2.task_id = l.task_id AND t2.source_env_name = ? " +
            "AND t2.sync_mode != 'OFF' and t2.retention_period_value != 0 " +
            "AND (l.expiration_date is null OR l.expiration_date ='1970-01-01 00:00:00.0' OR l.expiration_date > CURRENT_TIMESTAMP AT TIME ZONE 'UTC'))";
        
        Db.Row tableData = db(TDM).fetch(sql, tableName, schemaName, interfaceName, envName, tableName, schemaName, interfaceName, envName).firstRow();
        if (tableData != null && !tableData.isEmpty()) {
            tableVersion.put("taskExecutionId", tableData.get("taskExecutionId"));
            tableVersion.put("taskName", tableData.get("taskName"));
        }
        return tableVersion;
    }

    private static List<Map<String, Object>> getInterfaceTables(String dbInterfaceName, String interfaceType, String envName) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();

        if ("true".equalsIgnoreCase(getGlobal(SUPPRESS_TABLE_LEVEL_SUPPRESS_FILE_SYSTEMS))) {
            FabricInterface iface = InterfacesManager.getInstance().getInterface(dbInterfaceName);
            if (iface instanceof FileSystemInterface) {
                return result;
            }
        }

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

    private static List<Map<String, Object>> getIntefaceTablesByJDBC(String dbInterfaceName, String envName)
            throws Exception {
        String[] types = { "TABLE" };
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            DatabaseMetaData md = getConnection(dbInterfaceName).getMetaData();
            List<String> schemaList = new ArrayList<>();
            List<String> catalogList = new ArrayList<>();

            // 1. Discover Schemas
            try (ResultSet schemas = md.getSchemas()) {
                while (schemas.next()) {
                    String s = schemas.getString("TABLE_SCHEM");
                    if (s != null)
                        schemaList.add(s);
                }
            }

            // 2. Discover Catalogs if no schemas
            if (schemaList.isEmpty()) {
                try (ResultSet catalogs = md.getCatalogs()) {
                    while (catalogs.next()) {
                        String c = catalogs.getString("TABLE_CAT");
                        if (c != null)
                            catalogList.add(c);
                    }
                }
            }

            // 3. Process Schemas
            for (String schemaName : schemaList) {
                Map<String, Object> schemaMap = new HashMap<>();
                List<SortedMap<String, Object>> tableList = new ArrayList<>();
                try (ResultSet rs = md.getTables(null, schemaName, "%", types)) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        SortedMap<String, Object> tableData = getTableLastExecution(tableName, schemaName,
                                dbInterfaceName, envName);
                        tableData.put("tableName", tableName);
                        tableList.add(tableData);
                    }
                }
                if (!tableList.isEmpty()) {
                    schemaMap.put(schemaName, tableList);
                    result.add(schemaMap);
                }
            }

            // 4. Process Catalogs (only if schemas were empty)
            for (String catalogName : catalogList) {
                Map<String, Object> catalogMap = new HashMap<>();
                List<SortedMap<String, Object>> tableList = new ArrayList<>();
                try (ResultSet rs = md.getTables(catalogName, null, "%", types)) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        SortedMap<String, Object> tableData = getTableLastExecution(tableName, catalogName,
                                dbInterfaceName, envName);
                        tableData.put("tableName", tableName);
                        tableList.add(tableData);
                    }
                }
                if (!tableList.isEmpty()) {
                    catalogMap.put(catalogName, tableList);
                    result.add(catalogMap);
                }
            }

            // 5. Fallback for main/default
            if (result.isEmpty()) {
                List<SortedMap<String, Object>> tableList = new ArrayList<>();
                try (ResultSet rs = md.getTables(null, null, "%", types)) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        SortedMap<String, Object> tableData = getTableLastExecution(tableName, "main", dbInterfaceName,
                                envName);
                        tableData.put("tableName", tableName);
                        tableList.add(tableData);
                    }
                }
                if (!tableList.isEmpty()) {
                    Map<String, Object> tablesMap = new HashMap<>();
                    tablesMap.put("main", tableList);
                    result.add(tablesMap);
                }
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to get Meta Data for " + dbInterfaceName, e);
            throw new RuntimeException("Error: " + e.getMessage());
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

    @desc("Get all active interfaces for an environment with their associated product configurations")
    @webService(path = "getEnvInterfaceListWithConfig", verb = {
            MethodType.POST }, version = "1", isRaw = false, produce = { Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
                            {
              "result": [
                {
                  "interfaceName": "CRM_DB",
                  "config": {
                    "max_number_of_workers": 6,
                    "affinity": "LOCAL_DC"
                  }
                },
                {
                  "interfaceName": "ORDERS_DB",
                  "config": {
                    "max_number_of_workers": null,
                    "affinity": null
                  }
                },
                {
                  "interfaceName": "COLLECTION_DB",
                  "config": {
                    "max_number_of_workers": null,
                    "affinity": null
                  }
                },
                {
                  "interfaceName": "TAR_COLLECTION_DB",
                  "config": {
                    "max_number_of_workers": null,
                    "affinity": null
                  }
                },
                {
                  "interfaceName": "TAR_ORDERS_DB",
                  "config": {
                    "max_number_of_workers": null,
                    "affinity": null
                  }
                },
                {
                  "interfaceName": "BILLING_DB",
                  "config": {
                    "max_number_of_workers": 6,
                    "affinity": "LOCAL_DC"
                  }
                },
                {
                  "interfaceName": "TAR_CRM_DB",
                  "config": {
                    "max_number_of_workers": null,
                    "affinity": null
                  }
                },
                {
                  "interfaceName": "TAR_BILLING_DB",
                  "config": {
                    "max_number_of_workers": null,
                    "affinity": null
                  }
                }
              ],
              "errorCode": "SUCCESS",
              "message": null
            }
                            """)

    public static Object wsGetEnvInterfaceListWithConfig(
            @param(required = true) String environment,
            @param(required = true) Long envId) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        String errorCode = "SUCCESS";
        String message = null;

        try {
            String envName = (environment != null) ? environment.trim() : "";

            if (envName.isEmpty()) {
                throw new IllegalArgumentException("Environment name cannot be empty.");
            }

            // 1. Validate environment match
            validateEnvironmentMatch(envId, envName);

            // 2. Set Fabric Context
            fabric().execute("set environment = ?", envName);

            // 3. Build Config Lookup
            Map<String, Map<String, Object>> configLookup = buildConfigLookup(envId);

            // 4. Get and Filter Fabric Interfaces
            Set<FabricInterface> interfaces = InterfacesManager.getInstance().getAllInterfaces(envName);
            Set<String> suppressed = getAllSuppressedInterfaces();
            Set<String> withCatalog = discoverInterfacesWithCatalog(suppressed);

            for (FabricInterface iface : interfaces) {
                String interfaceName = iface.getName();
                if ("true".equalsIgnoreCase(SUPPRESS_TABLE_LEVEL_SUPPRESS_FILE_SYSTEMS)) {
                    if (iface instanceof FileSystemInterface) {
                       continue;
                    }
                }
        
                if (iface.getActiveMode()
                        && (!suppressed.contains(interfaceName) || withCatalog.contains(interfaceName))) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("interfaceName", interfaceName);
                    item.put("config", configLookup.getOrDefault(interfaceName, createDefaultConfig()));
                    resultList.add(item);
                }
            }

            resultList.sort(Comparator.comparing(
                item -> item.get("interfaceName").toString(), String.CASE_INSENSITIVE_ORDER));

        } catch (IllegalArgumentException e) {
            errorCode = "FAILED";
            message = e.getMessage();
        } catch (Exception e) {
            errorCode = "FAILED";
            message = "Internal error: " + e.getMessage();
            log.error("Service Error for environment " + environment, e);
        }

        response.put("result", resultList);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    private static void validateEnvironmentMatch(Long envId, String envName) throws Exception {
        // Query to check if the ID exists and if the name matches
        String sql = "SELECT environment_name FROM " + TDMDB_SCHEMA + ".environments WHERE environment_id = ?";
        Db.Row row = db("TDM").fetch(sql, envId).firstRow();

        if (row.isEmpty()) {
            throw new IllegalArgumentException("Environment ID " + envId + " does not exist.");
        }

        String dbEnvName = row.get("environment_name").toString();
        if (!dbEnvName.equalsIgnoreCase(envName)) {
            throw new IllegalArgumentException(
                    "Environment ID " + envId + " ( " + dbEnvName + " ) does not match the provided name: " + envName);
        }
    }

    private static Map<String, Map<String, Object>> buildConfigLookup(Long envId) throws Exception {
        Map<String, Map<String, Object>> lookup = new HashMap<>();

        Object productsRes = getProductsForEnvironment(envId);

        if (productsRes instanceof Map) {
            Map<String, Object> resMap = (Map<String, Object>) productsRes;
            List<Map<String, Object>> productsList = (List<Map<String, Object>>) resMap.get("result");

            if (productsList != null) {
                for (Map<String, Object> product : productsList) {
                    // Parse the interfaces list from the product
                    List<String> names = parseInterfacesList(product.get("related_interfaces"));

                    if (names != null) {
                        for (String name : names) {
                            if (!lookup.containsKey(name)) {
                                Map<String, Object> config = new HashMap<>();
                                config.put("max_number_of_workers", product.get("max_number_of_workers"));
                                config.put("affinity", product.get("data_center_name"));
                                lookup.put(name, config);
                            }
                        }
                    }
                }
            }
        }
        return lookup;
    }

    private static List<String> parseInterfacesList(Object raw) throws Exception {
        if (raw instanceof java.sql.Array) {
            return java.util.Arrays.asList((String[]) ((java.sql.Array) raw).getArray());
        } else if (raw instanceof List) {
            return (List<String>) raw;
        }
        return null;
    }

    private static Map<String, Object> createDefaultConfig() {
        Map<String, Object> def = new HashMap<>();
        def.put("max_number_of_workers", null);
        def.put("affinity", null);
        return def;
    }
    
}
