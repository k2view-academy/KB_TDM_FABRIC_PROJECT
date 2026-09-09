/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM_TableLevel.TableLevelUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
import com.k2view.cdbms.usercode.common.SharedGlobals;
import com.k2view.cdbms.usercode.lu.TDM_TableLevel.*;
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.mtable.MTable;
import java.lang.reflect.Field;

import com.k2view.fabric.interfaceSchema.InterfaceSchemaLogic;

import com.k2view.fabric.interfaceSchema.InterfaceSchemaLogic.TableInfoResult;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetTaskReferenceTableForSpecificTable;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.TDM_TableLevel.Globals.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.MtableLookup;
import static com.k2view.cdbms.usercode.common.TDM.TDMRef.SharedLogic.*;



@SuppressWarnings({"DefaultAnnotationParam", "unchecked"})
public class Logic extends UserCode {

    private static final Map<TableKey, Map<String, Object>> executionTableInfo = new ConcurrentHashMap<>();
    private static final String KEY_DEFS = "Definitions";
    private static final String KEY_PARTS = "PartitionFlowInputs";
    private static final String KEY_MASKING_WHERE = "MaskingUpdateFields";
    private static final String KEY_MASKING_FIELDS = "MaskingFields";
    private static final String KEY_TASK_REF = "TaskRefTables";
    private static final String KEY_ORDER = "CalculatedOrder";
    private static final String KEY_TAR_INFO = "TargetInfo";
    private static final String KEY_SOURCE_INFO = "SourceInfo";
    private static final String KEY_TASK_INFO = "TaskInfo";
    private static final String KEY_COUNT = "RecordsCount";

    private static class TableKey {
        final Long taskExecutionId;
        final String interfaceName;
        final String schemaName;
        final String tableName;

        public TableKey(Long taskExecutionId, String interfaceName, String schemaName, String tableName) {
            this.taskExecutionId = taskExecutionId;
            this.interfaceName = interfaceName != null ? interfaceName : "";
            this.schemaName = schemaName != null ? schemaName : "";
            this.tableName = tableName != null ? tableName : "";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TableKey tableKey = (TableKey) o;
            return Objects.equals(taskExecutionId, tableKey.taskExecutionId) &&
                    Objects.equals(interfaceName, tableKey.interfaceName) &&
                    Objects.equals(schemaName, tableKey.schemaName) &&
                    Objects.equals(tableName, tableKey.tableName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(taskExecutionId, interfaceName, schemaName, tableName);
        }
    }
        
        @out(name = "result", type = Set.class, desc = "")
        public static Set<String> fnGetTablesByOrder(SortedMap<Integer, Set<String>> tablesList, Integer order) throws Exception {
            //log.info("fnGetTablesByOrder - order: " + order + ", tables: " + tablesList);
           
            return tablesList.get(order);
        }
    
        @out(name = "result", type = Object.class, desc = "")
        public static Object fnGetTableDefinitions(String interfaceName, String schemaName, String tableName, String attrName) throws Exception {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> tempResult = new HashMap<>();
            Map<String,Object> lookupInputs = new HashMap<>();
            lookupInputs.put("interface_name", interfaceName);
            if (!"".equals(schemaName)) {
                lookupInputs.put("schema_name", schemaName);
            }
            
            lookupInputs.put("table_name", tableName);
            List<Map<String, Object>> tableDefinitions =  MtableLookup("TableLevelDefinitions",lookupInputs, MTable.Feature.caseInsensitive);
            //List<Map<String, Object>> tableDefinitions =  fnGetInterfaceInfo(lookupInputs, interfaceName);
            //TDM 9.3.1 - Check if the schema is dynamic
            if (!"".equals(schemaName) && (tableDefinitions == null || tableDefinitions.size() == 0)) {
                //lookupInputs.put("schema_name", null);
                lookupInputs.remove("schema_name");
                List<Map<String, Object>> tableDefinitions2 =  MtableLookup("TableLevelDefinitions",lookupInputs, MTable.Feature.caseInsensitive);
                //List<Map<String, Object>> tableDefinitions2 =  fnGetInterfaceInfo(lookupInputs, interfaceName);
                fabric().execute("set environment = ?", getGlobal("TDM_SOURCE_ENVIRONMENT_NAME","TDM_TableLevel"));
                Map<String, Object> matchEntry = findMatchedEntry(tableDefinitions2, getLuType().luName, "schema_name");
                if (matchEntry != null) {
                    tableDefinitions = new ArrayList<>();
                    tableDefinitions.add(matchEntry );
                }
                if (!"".equals(schemaName)) {
                    lookupInputs.put("schema_name", schemaName);
                }
            }
    
            //Look for entries without table name in Mtable
            lookupInputs.put("table_name", null);
            //lookupInputs.remove("table_name");
            List<Map<String, Object>> schemaDefinitions =  MtableLookup("TableLevelDefinitions",lookupInputs, MTable.Feature.caseInsensitive);
            //List<Map<String, Object>> schemaDefinitions = fnGetInterfaceInfo(lookupInputs, interfaceName);
            //lookupInputs.put("schema_name", null);
            lookupInputs.remove("schema_name");
            //List<Map<String, Object>> interfaceDefinitions =  MtableLookup("TableLevelDefinitions",lookupInputs, MTable.Feature.caseInsensitive);
            List<Map<String, Object>> interfaceDefinitions = fnGetInterfaceInfo("TableLevelDefinitions" ,lookupInputs, interfaceName);
    
            Boolean tableExists = false;
            Boolean schemaExists = false;
            Boolean interfaceExists = false;
            if (tableDefinitions != null && tableDefinitions.size() > 0) {
                tempResult = tableDefinitions.get(0);
                tableExists = true;
            }
            if (schemaDefinitions != null && schemaDefinitions.size() > 0) {
                schemaExists = true;
                if (!tableExists) {
                    tempResult = schemaDefinitions.get(0);
                }
                
            }
            if (interfaceDefinitions != null && interfaceDefinitions.size() > 0) {
                interfaceExists = true;
                if (!tableExists && !schemaExists){
                    tempResult = interfaceDefinitions.get(0);
                } 
    
            }
            result.put("interface_name", interfaceName);
            result.put("schema_name", schemaName);
            result.put("table_name", tableName);
            result.put("extract_flow", "");
            result.put("table_order", "");
            result.put("delete_flow", "");
            result.put("load_flow", "");
            
            for (Map.Entry<String, Object> entry : tempResult.entrySet()) {
                if (entry.getValue() == null || "".equals(entry.getValue())) {
                    if (tableExists) {
                        if (schemaExists && schemaDefinitions.get(0).get(entry.getKey()) != null &&
                            !"".equals(schemaDefinitions.get(0).get(entry.getKey()).toString())) {
                                result.put(entry.getKey(), schemaDefinitions.get(0).get(entry.getKey()));
                        } else if (interfaceExists && interfaceDefinitions.get(0).get(entry.getKey()) != null &&
                            !"".equals(interfaceDefinitions.get(0).get(entry.getKey()).toString())) {
                               result.put(entry.getKey(), interfaceDefinitions.get(0).get(entry.getKey()));
                            }
                    } else if (schemaExists && interfaceExists && interfaceDefinitions.get(0).get(entry.getKey()) != null &&
                        !"".equals(interfaceDefinitions.get(0).get(entry.getKey()).toString())) {
                            result.put(entry.getKey(), interfaceDefinitions.get(0).get(entry.getKey()));
                    }           
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result.get(attrName);
        }
            
        @out(name = "result", type = Object.class, desc = "")
        public static Map<String, Object> fnGetAllTableDefinitions(String interfaceName, String schemaName,
                String tableName) throws Exception {
            final String[] ATTRS = { "count_indicator", "record_count_flow", "table_order", "extract_flow", "delete_flow",
                    "load_flow", "partition_count_source", "partition_records_flow", "inplace_masking_update_flow", "commit_size" };
            Map<String, Object> finalResult = new HashMap<>();

            // Initial Setup
            for (String attr : ATTRS) {
                finalResult.put(attr, "");
            }
            finalResult.put("interface_name", interfaceName);
            finalResult.put("schema_name", schemaName);
            finalResult.put("table_name", tableName);

            //For count_indicator, empty values means true
            finalResult.put("count_indicator", "true");

            Map<Integer, List<Map<String, Object>>> hierarchy = fnGetHierarchyLevels("TableLevelDefinitions",
                    interfaceName, schemaName, tableName);

            // Merge attributes: Table level columns take priority over Schema/Interface
            for (int i = 0; i <= 2; i++) {
                List<Map<String, Object>> records = hierarchy.get(i);
                if (records == null || records.isEmpty())
                    continue;

                Map<String, Object> levelRow = records.get(0);
                for (String attr : ATTRS) {
                    Object currentVal = finalResult.get(attr);
                    if ("count_indicator".equals(attr)) {
                        if ("true".equals(currentVal)) {
                            Object newValue = levelRow.get(attr);
                            if (newValue != null && !"".equals(newValue.toString())) {
                                finalResult.put(attr, newValue);
                            }
                        }
                    } else {
                        if (currentVal == null || "".equals(currentVal.toString())) {
                            Object newValue = levelRow.get(attr);
                            if (newValue != null && !"".equals(newValue.toString())) {
                                finalResult.put(attr, newValue);
                            }
                        }
                    }
                    
                }
            }

            Object commitSize = finalResult.get("commit_size");
            if (commitSize == null || "".equals(commitSize.toString())) {
                String globalDefault = getGlobal("TDM_REF_UPD_SIZE", getLuType().luName);
                finalResult.put("commit_size", !"".equals(globalDefault) ? globalDefault : "1000");
            }

            return finalResult;
        }
    
        @out(name = "result", type = Map.class, desc = "")
        public static Map<String, Integer> fnGetTablesOrder(ArrayList<String> tableList, String dbInterfaceName, String dbSchemaName) throws Exception {
            Map<String, Integer> tablesList = new HashMap<>();
             
            try {
    
                if (tableList.size() == 1) {
                    tablesList.put(tableList.get(0), 0);
                    return tablesList;
                }
    
                DatabaseMetaData md = null;
                Map <String, Set<String>> tableParents = new HashMap<>();
    
                for (String tableName : tableList) {
                    //log.info("fnGetTablesOrder - tableName: " + tableName);
    
                    //Check if the table has a predefined order
    
                    Object tableOrder = fnGetTableDefinitions(dbInterfaceName, dbSchemaName, tableName, "table_order");
    
                    if (tableOrder != null && !"".equals(tableOrder.toString())) {
                        Integer order = Util.rte(() -> Integer.parseInt(tableOrder.toString()));
    
                        if (order != null) {
                            tablesList.put(tableName, order);
                            continue;
                        }
                    }
    
                    if (md == null) {
                        md = getConnection(dbInterfaceName).getMetaData();
                    }
                    
                    ResultSet importedKeys = md.getImportedKeys(null, dbSchemaName, tableName);
    
                    tableParents.put(tableName, new HashSet<>());
                    while (importedKeys.next()) {
                        String parentTable = importedKeys.getString("PKTABLE_NAME");
                        //log.info("fnGetTablesOrder - tableName: " + tableName + ", parent: " + parentTable);
    
                        // Add only tables that are part of the task and ignore self reference FKs
                        if (tableList.contains(parentTable) && !tableName.equals(parentTable)) {
                            //log.info("fnGetTablesOrder - tableName: " + tableName + ", adding parent: " + parentTable);
                            tableParents.get(tableName).add(parentTable);
                        }
                    }
    
                    if (importedKeys != null) {
                        importedKeys.close();
                    }
                }
    
                Map <String, Set<String>> tableParentsBck = new HashMap<>(tableParents);
                //tableChildrenBck = tableChildren;
                Map<String, Integer> visited = new HashMap<>();
                //log.info("fnGetTablesOrder - size of tableParents: " + tableParents.size());
                // Add leaf nodes (tables without incoming FKs)
    
                for (String tableName : tableParentsBck.keySet()) {
                    if (tableParentsBck.get(tableName).isEmpty()) {
                        //log.info("fnGetTablesOrder - Adding 0 to visited: " + tableName);
                        visited.put(tableName, 0);
                        tableParents.remove(tableName);
                    }
                }
                while(tableParents != null && !tableParents.isEmpty()) {
                    Map <String, Set<String>> tableParentsBck2 = new HashMap<>(tableParents);
                    for (String tableName : tableParentsBck2.keySet()) {
                        Set<String> remainingTables = new HashSet<>(tableParents.get(tableName));
                        Integer order = 0;
                        for (String parentTable : remainingTables) {
                            if (visited.get(parentTable) != null) {
                                Integer tableOrder = visited.get(parentTable);
                                if (order < tableOrder + 1) {
                                    order = tableOrder + 1;
                                }
                                tableParents.get(tableName).remove(parentTable);
                            }
                        }
                        if (tableParents.get(tableName).size() == 0) {
                            visited.put(tableName, order);
                            tableParents.remove(tableName);
                        }
                    }
    
                }
                
                for(Map.Entry<String, Integer> entry : visited.entrySet()) {
                    String table = entry.getKey();
                    Integer order = entry.getValue();
                    
                    tablesList.put(table, order);
                }
                
                return tablesList;
            } catch(Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("is not a db interface")) {
                    for (String tableName : tableList) {
                        tablesList.put(tableName, 0);
                    } 
                    return tablesList;
                } else {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to connect to Interface: " + dbInterfaceName);
                }
            }
    
        }
    
        public static Map<String, Object> getTableInfo(String interfaceName, String schemaName, String tableName,
                Long taskExecutionId, Integer order, boolean isInPlaceMaskingTask)
                throws Exception {

            // 1. Create the unique key for this request
            TableKey key = new TableKey(taskExecutionId, interfaceName, schemaName, tableName);

            // 2. Check the cache for this specific key
            Map<String, Object> cachedResult = executionTableInfo.get(key);

            if (cachedResult != null) {
                // Cache Hit: Return the stored result
                return cachedResult;
            }

            // Cache Miss: Must load the data.

            // 3. Load ALL data
            Map<String, Object> newResult = loadAllTableConfig(interfaceName, schemaName, tableName, taskExecutionId,
                    order, isInPlaceMaskingTask);

            // 4. Store the entire block under the unique key
            executionTableInfo.put(key, newResult);

            return newResult;
        }

        private static Map<String, Object> loadAllTableConfig(String interfaceName, String schemaName, String tableName,
                Long taskExecutionId, Integer order, boolean isInPlaceMaskingTask)
                throws Exception {

            Map<String, Object> allDataContainer = new HashMap<>();

            // A. Load Table Level Definitions (Extract/Load flows, Commit Size, etc.)
            Map<String, Object> defs = fnGetAllTableDefinitions(interfaceName, schemaName, tableName);
            allDataContainer.put(KEY_DEFS, defs);

            // B. Load TDM Task Meta-Data (Sync Mode, Versioning, etc.)
            Map<String, Object> taskInfo = fnGetTaskInfo(taskExecutionId);
            allDataContainer.put(KEY_TASK_INFO, taskInfo);

            // C. Load Partitioning Inputs (Chunks, Parallelism settings)
            Map<String, Map<String, Object>> parts = fnGetMTablePartitionInputs(interfaceName, schemaName, tableName);
            allDataContainer.put(KEY_PARTS, parts);

            // D. Load Task Reference Table mapping (Mapping source to task execution
            // tables)
            Object refTables = fnGetTaskRefTables(taskExecutionId, interfaceName, schemaName, tableName);
            allDataContainer.put(KEY_TASK_REF, refTables);

            String luName = "";
            if (refTables instanceof List) {
                List<Map<String, Object>> refList = (List<Map<String, Object>>) refTables;
                if (!refList.isEmpty()) {
                    Map<String, Object> firstRecord = refList.get(0);
                    luName = firstRecord.get("lu_name") != null ? firstRecord.get("lu_name").toString() : "";
                }
            }

            // E. Load Target Database Information (Interface/Schema mappings from RefList)
            Map<String, Object> tarInfo = fnLoadTargetInfoFromRefList(interfaceName, schemaName, tableName, luName);
            allDataContainer.put(KEY_TAR_INFO, tarInfo);

            Map<String, Object> srcInfo = fnLoadSourceInfoFromRefList(interfaceName, schemaName, tableName, luName);
            allDataContainer.put(KEY_SOURCE_INFO, srcInfo);

            // F. Store Calculated Execution Order
            allDataContainer.put(KEY_ORDER, order);

            // G. Load Masking Configuration (Only for In-Place Masking Tasks)
            if (isInPlaceMaskingTask) {
                // G.1 Load In-Place Masking Fields (Used for WHERE clause / Primary Keys)
                List<String> masking = fnGetMTableMaskingFields(interfaceName, schemaName, tableName);
                allDataContainer.put(KEY_MASKING_WHERE, masking);

                // G.2 Load Masking Fields from Catalog (Used for SET clause / PII logic)
                List<String> maskingFields = fnGetMaskingFieldsForTable(interfaceName, schemaName, tableName, taskInfo);
                allDataContainer.put(KEY_MASKING_FIELDS, maskingFields);
            }

            //H. Initiate table count_indicator
            allDataContainer.put(KEY_COUNT, -1);
            
            return allDataContainer;
        }

        private static Map<String, Object> fnGetTaskInfo(Long taskExecutionId) throws SQLException {
            Map<String, Object> taskInfo = new HashMap<>();
            String sql = "SELECT distinct t.task_id, t.delete_before_load, t.load_entity, " +
                    "t.retention_period_value, t.task_title, t.environment_id, t.source_environment_id, t.source_env_name, e.run_type "
                    +
                    "FROM " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".task_execution_list e " +
                    "WHERE t.task_id = e.task_id AND e.task_execution_id = ?";

            Db.Row row = db(TDM).fetch(sql, taskExecutionId).firstRow();

            if (row != null) {
                taskInfo.put("taskId", row.get("task_id"));
                taskInfo.put("deleteBeforeLoad", row.get("delete_before_load"));
                taskInfo.put("loadEntity", row.get("load_entity"));
                taskInfo.put("retentionPeriodValue", row.get("retention_period_value"));
                taskInfo.put("taskTitle", row.get("task_title"));
                taskInfo.put("targetEnvId", row.get("environment_id"));
                taskInfo.put("sourceEnvId", row.get("source_environment_id"));
                taskInfo.put("sourceEnvName", row.get("source_env_name"));
                taskInfo.put("runType", row.get("run_type"));

            }
            return taskInfo;
        }

        private static Map<Integer, List<Map<String, Object>>> fnGetHierarchyLevels(String mtableName,
                String interfaceName,
                String schemaName, String tableName) throws Exception {
            Map<Integer, List<Map<String, Object>>> levels = new HashMap<>();
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("interface_name", interfaceName);

            // Level 0: Table Level (Specific or @Dynamic)
            inputs.put("schema_name", schemaName);
            inputs.put("table_name", tableName);
            List<Map<String, Object>> tableRecs = MtableLookup(mtableName, inputs, MTable.Feature.caseInsensitive);

            // Dynamic Schema (@) Check
            if ((tableRecs == null || tableRecs.isEmpty()) && !"".equals(schemaName)) {
                inputs.remove("schema_name");
                List<Map<String, Object>> entries = MtableLookup(mtableName, inputs, MTable.Feature.caseInsensitive);
                fabric().execute("set environment = ?", getGlobal("TDM_SOURCE_ENVIRONMENT_NAME","TDM_TableLevel"));
                Map<String, Object> matchEntry = findMatchedEntry(entries, getLuType().luName, "schema_name");
                if (matchEntry != null) {
                    tableRecs = new ArrayList<>();
                    tableRecs.add(matchEntry );
                }               
            }
            levels.put(0, tableRecs);

            // Level 1: Schema Level
            inputs.put("schema_name", schemaName);
            inputs.put("table_name", null);
            levels.put(1, MtableLookup(mtableName, inputs, MTable.Feature.caseInsensitive));

            // Level 2: Interface Level
            inputs.remove("schema_name");
            levels.put(2, fnGetInterfaceInfo(mtableName, inputs, interfaceName));

            return levels;
        }

        public static Map<String, Map<String, Object>> fnGetMTablePartitionInputs(String interfaceName,
                String schemaName,
                String tableName) throws Exception {
            Map<String, Map<String, Object>> result = new HashMap<>();
            Map<Integer, List<Map<String, Object>>> hierarchy = fnGetHierarchyLevels("TableLevelDefinitions",
                    interfaceName, schemaName, tableName);

            // Fill from Priority 0 (Table) to Priority 2 (Interface)
            for (int i = 0; i <= 2; i++) {
                List<Map<String, Object>> records = hierarchy.get(i);
                if (records == null || records.size() == 0) {
                    continue;
                }

                Map<String, Object> levelRow = records.get(0);
                Object flowInputs = levelRow.get("partition_flow_inputs");
                Object flowName = levelRow.get("partition_records_flow");
                
                if(flowName != null && flowInputs != null){
                    Map<String, Object> flowParamJson = Json.get().fromJson(flowInputs.toString(), Map.class);

                    if (flowParamJson != null && !(flowParamJson.isEmpty())) {
                        flowParamJson.forEach((key, value) -> {
                            result.computeIfAbsent(flowName.toString(), k -> new HashMap<>()).putIfAbsent(key, value);
                        });
                        
                    }
                }
            }
            return result;
        }

        public static List<String> fnGetMTableMaskingFields(String interfaceName, String schemaName, String tableName)
                throws Exception {

            final String MTABLE_NAME = "TableLevelInPlaceMasking";

            // --- 1. Prepare Lookup Inputs for Table Level ---
             Map<String, Object> lookupInputs = new HashMap<>();

            // a. Initial Lookup Inputs (Specific Interface, Schema, Table)
            lookupInputs.put("interface_name", interfaceName);
            if (!"".equals(schemaName)) {
                lookupInputs.put("schema_name", schemaName);
            }
            lookupInputs.put("table_name", tableName);

            // --- 2. Perform the Specific Table-Level Lookup ---
            List<Map<String, Object>> tableDefinitions = MtableLookup(MTABLE_NAME, lookupInputs,
                    MTable.Feature.caseInsensitive);
            boolean tableExists = tableDefinitions != null && !tableDefinitions.isEmpty();

            // --- 3. Dynamic Schema Check (If initial specific lookup failed) ---
            if (!tableExists && !"".equals(schemaName)) {
                // Prepare new lookup inputs without the specific schema name
                lookupInputs.remove("schema_name");

                // Lookup without schema name
                List<Map<String, Object>> tableDefinitions2 = MtableLookup(MTABLE_NAME, lookupInputs,
                        MTable.Feature.caseInsensitive);

                fabric().execute("set environment = ?", getGlobal("TDM_SOURCE_ENVIRONMENT_NAME","TDM_TableLevel"));
                Map<String, Object> matchEntry = findMatchedEntry(tableDefinitions2, getLuType().luName, "schema_name");
                if (matchEntry != null) {
                    tableDefinitions = new ArrayList<>();
                    tableDefinitions.add(matchEntry);
                    tableExists = true;
                }
            }

            // --- 4. Extract Field Names from the Found Records ---
            List<String> fieldNames = new ArrayList<>();

            if (tableExists) {
                for (Map<String, Object> record : tableDefinitions) {
                    Object fieldObj = record.get("field_name");

                    if (fieldObj != null) {
                        String fieldName = fieldObj.toString();
                        if (!fieldName.isEmpty()) {
                            fieldNames.add(fieldName);
                        }
                    }
                }
            }

            if (fieldNames.isEmpty()) {
                log.info("No masking fields found for " + tableName + ". Fetching PK fields instead.");
                fieldNames = fnGetPKFieldsOnly(interfaceName, schemaName, tableName);
                if (fieldNames.isEmpty()) {
                    log.error("Table: " + tableName + ", has no key fields for updates and not Primary Key, failing the table");
                    //throw new RuntimeException("Table: " + tableName + ", has no key fields for updates and not Primary Key, failing the table");
                    fieldNames = null;
                }
            }
            return fieldNames;
        }

        public static List<String> fnGetPKFieldsOnly(String interfaceName, String schemaName, String tableName) {
            List<String> pkFields = new ArrayList<>();
            String mtableName = "catalog_field_info";

            Map<String, Object> lookupInputs = new HashMap<>();
            lookupInputs.put("dataPlatform", interfaceName);
            lookupInputs.put("schema", schemaName);
            lookupInputs.put("dataset", tableName);
            lookupInputs.put("pk", "true"); // Specifically filter for PKs

            try {
                List<Map<String, Object>> results = MtableLookup(mtableName, lookupInputs,
                        MTable.Feature.caseInsensitive);
                if (results != null) {
                    for (Map<String, Object> row : results) {
                        Object fieldName = row.get("field");
                        if (fieldName != null && !fieldName.toString().isEmpty()) {
                            pkFields.add(fieldName.toString());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch PK fields from catalog for table " + tableName + ": " + e.getMessage());
            }

            return pkFields;
        }

        public static List<String> fnGetMaskingFieldsForTable(String interfaceName, String schemaName, String tableName,
                Map<String, Object> taskInfo) throws Exception {
            List<String> maskingFieldsList = new ArrayList<>();
            String mtableName = "catalog_field_info";

            if (taskInfo != null && !taskInfo.containsKey("in_place_masking_pk")) {
                taskInfo.put("in_place_masking_pk", false);
            }

            Map<String, Object> lookupInputs = new HashMap<>();
            lookupInputs.put("dataPlatform", interfaceName);
            lookupInputs.put("schema", schemaName);
            lookupInputs.put("dataset", tableName);

            try {
                List<Map<String, Object>> results = MtableLookup(mtableName, lookupInputs,
                        MTable.Feature.caseInsensitive);

                if (results != null) {
                    for (Map<String, Object> row : results) {
                        Object fieldName = row.get("field");
                        if (fieldName == null || fieldName.toString().isEmpty())
                            continue;

                        String fieldStr = fieldName.toString();

                        // 1. Check PII (Standard boolean check)
                        Object piiVal = row.get("pii");
                        boolean isPII = piiVal != null && "true".equalsIgnoreCase(piiVal.toString());

                        // If it's PII, we include it
                        if (isPII) {
                            if (!maskingFieldsList.contains(fieldStr)) {
                                maskingFieldsList.add(fieldStr);
                            }

                            // 3. Check if this sensitive field is a PK
                            Object pkVal = row.get("pk");
                            boolean isPk = pkVal != null && "true".equalsIgnoreCase(pkVal.toString());
                            if (isPk && taskInfo != null) {
                                taskInfo.put("in_place_masking_pk", true);
                            }
                        }
                    }
                }

                List<String> whereFields = fnGetMTableMaskingFields(interfaceName, schemaName, tableName);
                if (!Util.isEmpty(whereFields)) {
                    Set<String> set = new LinkedHashSet<>(maskingFieldsList);
                    set.addAll(whereFields);

                    maskingFieldsList.clear();
                    maskingFieldsList.addAll(set);
                }

            } catch (Exception e) {
                log.warn("Catalog lookup failed for table " + tableName + ": " + e.getMessage());
            }
            return maskingFieldsList;
        }

        private static Object fnGetTaskRefTables(Long taskExecutionId, String interfaceName, String schemaName,
                String tableName) throws Exception {
            return fnGetTaskReferenceTableForSpecificTable(taskExecutionId, interfaceName, schemaName, tableName);
        }

        public static Map<String, Object> fnLoadTargetInfoFromRefList(String interfaceName, String schemaName,
                String tableName, String luName) throws Exception {
            Map<String, Object> targetInfo = new HashMap<>();

            // Default values: Source is Target
            targetInfo.put("target_ref_table_name", tableName);
            targetInfo.put("target_interface_name", interfaceName);
            targetInfo.put("target_schema_name", schemaName);

            Map<String, Object> lookupInputs = new HashMap<>();
            lookupInputs.put("lu_name", luName);
            lookupInputs.put("interface_name", interfaceName);
            lookupInputs.put("schema_name", schemaName);
            lookupInputs.put("reference_table_name", tableName);

            // 1. Try exact match lookup
            List<Map<String, Object>> refList = MtableLookup("RefList", lookupInputs, MTable.Feature.caseInsensitive);

            // 2. Dynamic Schema Fallback (If exact match fails)
            if ((refList == null || refList.isEmpty()) && schemaName != null && !schemaName.isEmpty()) {
                lookupInputs.remove("schema_name");
                List<Map<String, Object>> refListNoSchema = MtableLookup("RefList", lookupInputs,
                        MTable.Feature.caseInsensitive);

                Map<String, Object> matched = findMatchedEntry(refListNoSchema, luName, "schema_name");
                if (matched != null) {
                    updateTargetInfoMap(targetInfo, matched);
                    return targetInfo;
                }
            }

            // 3. Final assignment if exact match was found
            if (refList != null && !refList.isEmpty()) {
                updateTargetInfoMap(targetInfo, refList.get(0));
            }

            return targetInfo;
        }

        private static Map<String, Object> fnLoadSourceInfoFromRefList(String interfaceName, String schemaName,
                String tableName, String luName) throws Exception {
            Map<String, Object> sourceInfo = new HashMap<>();

            // Default values: source info mirrors the table's own identity
            sourceInfo.put("source_ref_table_name", tableName);
            sourceInfo.put("source_interface_name", interfaceName);
            sourceInfo.put("source_schema_name", schemaName);

            Map<String, Object> lookupInputs = new HashMap<>();
            lookupInputs.put("lu_name", luName);
            lookupInputs.put("interface_name", interfaceName);
            lookupInputs.put("schema_name", schemaName);
            lookupInputs.put("reference_table_name", tableName);

            // 1. Try exact match lookup
            List<Map<String, Object>> refList = MtableLookup("RefList", lookupInputs, MTable.Feature.caseInsensitive);

            // 2. Dynamic Schema Fallback (If exact match fails)
            if ((refList == null || refList.isEmpty()) && schemaName != null && !schemaName.isEmpty()) {
                lookupInputs.remove("schema_name");
                List<Map<String, Object>> refListNoSchema = MtableLookup("RefList", lookupInputs,
                        MTable.Feature.caseInsensitive);

                Map<String, Object> matched = findMatchedEntry(refListNoSchema, luName, "schema_name");
                if (matched != null) {
                    updateSourceInfoMap(sourceInfo, matched);
                    return sourceInfo;
                }
            }

            // 3. Final assignment if exact match was found
            if (refList != null && !refList.isEmpty()) {
                updateSourceInfoMap(sourceInfo, refList.get(0));
            }

            return sourceInfo;
        }


        private static String resolveSchemaIfGlobal(String schemaValue, String envGlobalName) throws Exception {
            if (schemaValue == null || !schemaValue.startsWith("@")) {
                return schemaValue;
            }
            fabric().execute("set environment = ?", getGlobal(envGlobalName, getLuType().luName));
            String gVar = schemaValue.replaceAll("@", "");
            return getGlobal(gVar, getLuType().luName);
        }

        private static void updateTargetInfoMap(Map<String, Object> targetMap, Map<String, Object> refRow) throws Exception {
            Object tarTable = refRow.get("target_ref_table_name");
            if (tarTable != null && !"".equals(tarTable.toString())) {
                targetMap.put("target_ref_table_name", tarTable.toString());
            }

            Object tarInt = refRow.get("target_interface_name");
            if (tarInt != null && !"".equals(tarInt.toString())) {
                targetMap.put("target_interface_name", tarInt.toString());
            }

            Object tarSchema = refRow.get("target_schema_name");
            if (tarSchema != null && !"".equals(tarSchema.toString())) {
                targetMap.put("target_schema_name", resolveSchemaIfGlobal(tarSchema.toString(), "TDM_TAR_ENV_NAME"));
            }
        }

        private static void updateSourceInfoMap(Map<String, Object> sourceMap, Map<String, Object> refRow) throws Exception {
            Object refTable = refRow.get("reference_table_name");
            if (refTable != null && !"".equals(refTable.toString())) {
                sourceMap.put("source_ref_table_name", refTable.toString());
            }

            Object srcInt = refRow.get("interface_name");
            if (srcInt != null && !"".equals(srcInt.toString())) {
                sourceMap.put("source_interface_name", srcInt.toString());
            }

            Object srcSchema = refRow.get("schema_name");
            if (srcSchema != null && !"".equals(srcSchema.toString())) {
                sourceMap.put("source_schema_name", resolveSchemaIfGlobal(srcSchema.toString(), "TDM_SOURCE_ENVIRONMENT_NAME"));
            }
        }

       /**
         * Retrieves a specific configuration section for a table from the cache.
         * ...
         * @return The object stored under the requested section, or null if not found.
         * * Valid Sections:
         * - "Definitions": Map of flow names and execution order.
         * - "PartitionFlowInputs": Map of partition logic and variables.
         * - "MaskingFields": List<String> of actual PII/Masking columns to be transformed.
         * - "MaskingUpdateFields": List<String> of columns used for identifying records 
         * (e.g., PKs) during in-place masking updates.
         * - "TargetInfo": Map containing target interface and schema details.
         * - "SourceInfo": Map containing source interface, schema, and reference table details.
         * - "TaskRefTables": List of Maps containing TDM reference metadata.
         * - "TaskInfo": Map containing task flags like 'in_place_masking_pk'.
         */
        @out(name = "result", type = Object.class, desc = "")
        public static Object fnGetCachedTableInfoBySection(String interfaceName, String schemaName, String tableName,
                Long taskExecutionId, String section, String attrName) throws Exception {
            TableKey key = new TableKey(taskExecutionId, interfaceName, schemaName, tableName);
            Map<String, Object> cachedTableData = executionTableInfo.get(key);

            // 1. Lazy load on cache miss (handles worker nodes that don't run LoadTablesDataToCache)
            if (cachedTableData == null) {
                log.info(String.format("TDM Cache Miss: Lazy loading table [%s.%s] for execution [%d].",
                        schemaName, tableName, taskExecutionId));
                cachedTableData = lazyLoadTableInfo(interfaceName, schemaName, tableName, taskExecutionId);
            }

            if (cachedTableData == null) {
                log.warn(String.format("TDM Cache Miss: Table [%s.%s] could not be loaded for execution [%d].",
                        schemaName, tableName, taskExecutionId));
                return null;
            }

            // 2. Access and Log Section Miss
            Object sectionData = cachedTableData.get(section);
            if (sectionData == null) {
                log.warn(String.format("TDM Cache Info: Section [%s] not found for table [%s.%s].",
                        section, schemaName, tableName));
                return null;
            }

            // 3. Extract data based on the section's data structure
            if (sectionData instanceof Map) {
                // Covers KEY_DEFS, KEY_PARTS, KEY_TAR_INFO, KEY_SOURCE_INFO, and KEY_TASK_INFO
                Map<String, Object> sectionMap = (Map<String, Object>) sectionData;
                Object value = sectionMap.get(attrName);

                if (value == null) {
                    log.warn(String.format("Attribute [%s] not found in Map section [%s] for table [%s].",
                            attrName, section, tableName));
                }
                return value;

            } else if (sectionData instanceof List) {
                List<?> list = (List<?>) sectionData;
                if (list.isEmpty()) {
                    log.warn(String.format("List section [%s] is empty for table [%s].", section, tableName));
                    return null;
                }

                // Case A: Flat List of Strings (e.g., MaskingUpdateFields)
                if (!"".equals(attrName)) {
                    if (list.get(0) instanceof String) {
                        for (Object item : list) {
                            if (item != null && item.toString().equalsIgnoreCase(attrName)) {
                                return item;
                            }
                        }
                        log.warn(String.format("Column [%s] not found in masking list for table [%s].", attrName,
                                tableName));
                        return null;
                    }
                }

                if ("".equals(attrName)) {
                    return list;
                }

                // Case B: List of Maps (e.g., TaskRefTables)
                else if (list.get(0) instanceof Map) {
                    for (Object item : list) {
                        Map<String, Object> entry = (Map<String, Object>) item;
                        if (entry != null && entry.containsKey(attrName)) {
                            return entry.get(attrName);
                        }
                    }
                    log.warn(String.format("Field [%s] not found in any entry of list section [%s] for table [%s].",
                            attrName, section, tableName));
                    return null;
                }
            } else if (sectionData instanceof String || sectionData instanceof Number || sectionData instanceof Boolean) {
                return sectionData;
            }
            return null;
        }

        @out(name = "result", type = List.class, desc = "")
        public static List<TableKey> fnGetTablesByAttributeValue(
                Long taskExecutionId,
                String section,
                String attrName,
                String attrVal) throws Exception {
            List<TableKey> result = new ArrayList<>();

            for (Map.Entry<TableKey, Map<String, Object>> entry : executionTableInfo.entrySet()) {
                TableKey key = entry.getKey();

                // 1. Filter by taskExecutionId immediately
                if (taskExecutionId != null && !taskExecutionId.equals(key.taskExecutionId)) {
                    continue;
                }

                // 2. Proceed with section and attribute check
                Object sectionData = entry.getValue().get(section);
                if (sectionData == null)
                    continue;

                if (isMatch(sectionData, attrName, attrVal)) {
                    result.add(key);
                }
            }
            return result;
        }

        private static boolean isMatch(Object data, String attrName, String targetVal) {
            if (data instanceof Map) {
                Object val = ((Map<?, ?>) data).get(attrName);
                return Objects.equals(targetVal, String.valueOf(val));
            }

            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (isMatch(item, attrName, targetVal))
                        return true;
                }
            }

            // Fallback for primitive/direct values
            return Objects.equals(targetVal, String.valueOf(data));
        }

        public static void fnSetTableCount(String interfaceName, String schemaName, String tableName,
            Long taskExecutionId, Long tableCount) {

            TableKey key = new TableKey(taskExecutionId, interfaceName, schemaName, tableName);
            Map<String, Object> cachedTableData = executionTableInfo.get(key);
            cachedTableData.put(KEY_COUNT, tableCount);
        }   

        private static Map<String, Object> lazyLoadTableInfo(String interfaceName, String schemaName,
                String tableName, Long taskExecutionId) throws Exception {
            TableKey key = new TableKey(taskExecutionId, interfaceName, schemaName, tableName);

            Map<String, Object> cached = executionTableInfo.get(key);
            if (cached != null) return cached;

            Integer order = 0;
            try {
                Object orderVal = db(TDM).fetch(
                        "SELECT table_order FROM " + TDMDB_SCHEMA + ".task_ref_exe_stats " +
                        "WHERE task_execution_id = ? AND interface_name = ? AND schema_name = ? AND ref_table_name = ? LIMIT 1",
                        taskExecutionId, interfaceName, schemaName, tableName).firstValue();
                if (orderVal != null && !orderVal.toString().isEmpty()) {
                    order = Integer.valueOf(orderVal.toString());
                }
            } catch (Exception e) {
                log.warn("lazyLoadTableInfo: could not retrieve table_order for " + schemaName + "." + tableName + ": " + e.getMessage());
            }

            boolean isInPlaceMasking = false;
            try {
                Object flag = db(TDM).fetch(
                        "SELECT t.in_place_masking_ind FROM " + TDMDB_SCHEMA + ".tasks t " +
                        "JOIN " + TDMDB_SCHEMA + ".task_execution_list e ON t.task_id = e.task_id " +
                        "WHERE e.task_execution_id = ? LIMIT 1",
                        taskExecutionId).firstValue();
                isInPlaceMasking = flag != null && Boolean.parseBoolean(flag.toString());
            } catch (Exception e) {
                log.warn("lazyLoadTableInfo: could not retrieve in_place_masking_ind for execution " + taskExecutionId + ": " + e.getMessage());
            }

            Map<String, Object> loaded = loadAllTableConfig(interfaceName, schemaName, tableName,
                    taskExecutionId, order, isInPlaceMasking);

            // putIfAbsent handles the race: whichever thread wins sets the value; losers discard their copy
            Map<String, Object> existing = executionTableInfo.putIfAbsent(key, loaded);
            return existing != null ? existing : loaded;
        }   

        public static void fnClearTaskCache(Long taskExecutionId) {
            if (taskExecutionId == null)
                return;

            // Removes all cached tables belonging to this specific execution ID
            executionTableInfo.keySet().removeIf(key -> taskExecutionId.equals(key.taskExecutionId));

            //log.info("TDM Cache Cleanup: Memory released for Task Execution: " + taskExecutionId);
        }
}
