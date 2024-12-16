/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM_TableLevel.TableLevelUtils;

import java.util.*;
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
import com.k2view.cdbms.usercode.lu.TDM_TableLevel.*;
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;
import com.k2view.fabric.common.mtable.MTable;
import java.lang.reflect.Field;

import com.k2view.fabric.interfaceSchema.InterfaceSchemaLogic;

import com.k2view.fabric.interfaceSchema.InterfaceSchemaLogic.TableInfoResult;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.TDM_TableLevel.Globals.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.MtableLookup;

@SuppressWarnings({"DefaultAnnotationParam", "unchecked"})
public class Logic extends UserCode {
	
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
		lookupInputs.put("table_name", null);
        List<Map<String, Object>> schemaDefinitions =  MtableLookup("TableLevelDefinitions",lookupInputs, MTable.Feature.caseInsensitive);
        lookupInputs.put("schema_name", null);
        List<Map<String, Object>> interfaceDefinitions =  MtableLookup("TableLevelDefinitions",lookupInputs, MTable.Feature.caseInsensitive);
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

    @out(name = "result", type = Map.class, desc = "")
    public static Map<String, Object> fnGetTablesSetsByOrder(Map<String, Integer> tablesOrder, String interfaceName, String schemaName) throws Exception {
        SortedMap<Integer, Set<String>> tablesList = new TreeMap<>();
        Map<String, Object> result = new HashMap<>();

        Map<String,Object> lookupInputs = new HashMap<>();
        lookupInputs.put("interface_name",interfaceName);
        lookupInputs.put("schema_name",schemaName);
        
        Integer maxOrder = 0;

        for( Map.Entry<String, Integer> tableEntry : tablesOrder.entrySet()) {
            String tableName = tableEntry.getKey();
            Integer order = tableEntry.getValue();
            lookupInputs.put("table_name",tableName);
            List<Map<String, Object>> tableDefinitions =  MtableLookup("TableLevelDefinitions",lookupInputs, MTable.Feature.caseInsensitive);
            if (tableDefinitions != null && tableDefinitions.size() > 0) {
                Object orderObj = tableDefinitions.get(0).get("table_order");
                if (orderObj != null && !"".equals(orderObj.toString())) {
                    try {
                        int tableOrder = Integer.parseInt(orderObj.toString());
                        order = tableOrder;
                    } catch (Exception e) {
                        log.error("The order of table: "+ tableName + " defined in TableLevelDefinitions Mtable is invalid, ingoring it");
                    }
                }
            }

            if (order > maxOrder) {
                maxOrder = order;
            }
            if(tablesList.get(order) != null) {
        
                tablesList.get(order).add(tableName);
            } else {
                Set<String> set = new HashSet<>();
                set.add(tableName);
                tablesList.put(order, set);
            }
            
        }

        result.put("tablesList", tablesList);
        result.put("maxOrder", maxOrder + 1);
        return result;
    }

    @out(name = "result", type = List.class, desc = "")
    public static List<Map<String, Object>> fnGetTablesOfTask(String taskExecutionId) throws Exception {

        String sql = "SELECT  rt.lu_name, rt.interface_name, rt.schema_name, es.ref_table_name as table_name " +
            "FROM  " + TDMDB_SCHEMA + ".TASK_REF_EXE_STATS es, " + TDMDB_SCHEMA + ".TASK_REF_TABLES rt, " + 
            TDMDB_SCHEMA + ".tasks t " +
            "WHERE  rt.task_id = t.task_id " + 
            //"AND (lower(es.execution_status) = 'pending' or (lower(t.sync_mode) != 'off' and lower(es.execution_status) = 'running')) " +
            "AND rt.task_id = es.task_id AND rt.task_ref_table_id = es.task_ref_table_id " +
            "AND es.task_execution_id = ?";

        Map<String, Map<String, Object>> interfaceSChemaList = new HashMap<>();

        Db.Rows rows = db(TDM).fetch(sql, taskExecutionId);
        for (Db.Row row : rows) {
            String luName = row.get("lu_name").toString();
            String interfaceName = row.get("interface_name").toString();
            String schemaName = row.get("schema_name").toString();
            String tableName = row.get("table_name").toString();
        
            Map<String,Object> lookupInputs = new HashMap<>();
            lookupInputs.put("lu_name",luName);
            lookupInputs.put("interface_name",interfaceName);
            lookupInputs.put("schema_name",schemaName);
            lookupInputs.put("reference_table_name",tableName);
            List<Map<String, Object>> tableInfo =  MtableLookup("RefList",lookupInputs, MTable.Feature.caseInsensitive);
            if (tableInfo != null && tableInfo.size() > 0) {
                if (tableInfo.get(0).get("target_ref_table_name") != null && !"".equals(tableInfo.get(0).get("target_ref_table_name").toString())) {
                    tableName = tableInfo.get(0).get("target_ref_table_name").toString();
                }

                if (tableInfo.get(0).get("target_interface_name") != null && !"".equals(tableInfo.get(0).get("target_interface_name").toString())) {
                    interfaceName = tableInfo.get(0).get("target_interface_name").toString();
                }

                if (tableInfo.get(0).get("target_schema_name") != null && !"".equals(tableInfo.get(0).get("target_schema_name").toString())) {
                    schemaName = tableInfo.get(0).get("target_schema_name").toString();
                    
                }
            }

            String key = interfaceName + "<#>" + schemaName;
            Map<String, Object> interfaceSchemaEntry = interfaceSChemaList.get(key);
            if (interfaceSchemaEntry != null) {
                Set<String> tableSet = (Set<String>)interfaceSchemaEntry.get("tableSet");
                tableSet.add(tableName);

            } else {
                Map<String, Object> newEntry = new HashMap<>();
                newEntry.put("lu_name", luName);
                newEntry.put("interfaceName", interfaceName);
                newEntry.put("schemaName", schemaName);
                Set<String> set = new HashSet<>();
                set.add(tableName);
                newEntry.put("tableSet", set);
                interfaceSChemaList.put(key, newEntry);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> map : interfaceSChemaList.entrySet()) {
            result.add(map.getValue());
        }

        return result;
    }


    @out(name = "result", type = Map.class, desc = "")
    public static Map<String, Integer> fnGetTablesOrder(ArrayList<String> tableList, String dbInterfaceName, String dbSchemaName, String envName) throws Exception {
  
        Map<String, Integer> tablesList = new HashMap<>();
       
        Map <String, Set<String>> tableParents = new HashMap<>();

        //try {
           for (String tableName : tableList) { 
                TableInfoResult tableInfo = new TableInfoResult();
                tableInfo = (TableInfoResult)InterfaceSchemaLogic.INSTANCE.getTableInfo(dbInterfaceName, null, dbSchemaName, tableName, envName, "false","true", null, null);
                if (!tableParents.containsKey(tableName)) {
                    tableParents.put(tableName, new HashSet<>());
                }

                Field privateField = TableInfoResult.class.getDeclaredField("fks");
                privateField.setAccessible(true); 
                List<Map<String, ?>> fks = (List<Map<String, ?>>)privateField.get(tableInfo);

                for (Map<String, ?> tableFKs : fks) {
                    if (tableName.equals(tableFKs.get("PKTABLE_NAME").toString())) {
                        String childTable = tableFKs.get("FKTABLE_NAME").toString();
                        //log.info("fnGetTablesOrder - tableName: " + tableName + ", parent: " + parentTable);
                        if (tableList.contains(childTable)) {
                            //log.info("fnGetTablesOrder - tableName: " + tableName + ", adding parent: " + parentTable);
                            if (tableParents.containsKey(childTable)) {
                                tableParents.get(childTable).add(tableName);
                            } else {
                                tableParents.put(childTable, new HashSet<>());
                                tableParents.get(childTable).add(tableName);
                            }
                        }
                    }
                }
            }

                Map <String, Set<String>> tableParentsBck = new HashMap<>(tableParents);
                Map<String, Integer> visited = new HashMap<>();
                //log.info("fnGetTablesOrder - size of tableParents: " + tableParents.size());
                // Add leaf nodes (tables without incoming FKs)

                for (String table : tableParentsBck.keySet()) {
                    if (tableParentsBck.get(table).isEmpty()) {
                        //log.info("fnGetTablesOrder - Adding 0 to visited: " + tableName);
                        visited.put(table, 0);
                        tableParents.remove(table);
                    }
                }
                while(tableParents != null && !tableParents.isEmpty()) {
                    Map <String, Set<String>> tableParentsBck2 = new HashMap<>(tableParents);
                    for (String table : tableParentsBck2.keySet()) {
                        Set<String> remainingTables = new HashSet<>(tableParents.get(table));
                        Integer order = 0;
                        for (String parentTable : remainingTables) {
                            if (visited.get(parentTable) != null) {
                                Integer tableOrder = visited.get(parentTable);
                                if (order < tableOrder + 1) {
                                    order = tableOrder + 1;
                                }
                                tableParents.get(table).remove(parentTable);
                            }
                        }
                        if (tableParents.get(table).size() == 0) {
                            visited.put(table, order);
                            tableParents.remove(table);
                        }
                    }

                }
                
                for(Map.Entry<String, Integer> entry : visited.entrySet()) {
                    String table = entry.getKey();
                    Integer order = entry.getValue();
                    
                    tablesList.put(table, order);
                }
                
        
      /*  } catch (Exception e) {
            log.info("Unable to get FKs for iterface: " + dbInterfaceName + ", setting the order of all the tables to 0");

            for (String tableName : tableList) {
                tablesList.put(tableName, 0);
            }
        }*/

        return tablesList;
   }
}

