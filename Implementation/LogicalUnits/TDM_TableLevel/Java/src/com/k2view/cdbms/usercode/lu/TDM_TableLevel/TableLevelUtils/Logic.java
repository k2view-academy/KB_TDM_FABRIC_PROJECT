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

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.TDM_TableLevel.Globals.*;

@SuppressWarnings({"DefaultAnnotationParam"})
public class Logic extends UserCode {
	
    public static HashMap<Integer, Set<String>> tablesList = new HashMap<>();

    @out(name = "result", type = Integer.class, desc = "")
    public static Integer fnGetTablesOrderOld(ArrayList<String> tableList, String dbInterfaceName, String dbSchemaName, String SourceEnv, String taskExecutionId) throws Exception {
        tablesList.clear();
        DatabaseMetaData md = getConnection(dbInterfaceName).getMetaData();
        Map <String, Set<String>> tableChildren = new HashMap<>();

        for (String tableName : tableList) {
            log.info("fnGetTablesOrder - tableName: " + tableName);
            ResultSet exportedKeys = md.getExportedKeys(null, dbSchemaName, tableName);

            tableChildren.put(tableName, new HashSet<>());
            while (exportedKeys.next()) {
                String childTable = exportedKeys.getString("FKTABLE_NAME");
                log.info("fnGetTablesOrder - tableName: " + tableName + ", child: " + childTable);
                if (tableList.contains(childTable)) {
                    log.info("fnGetTablesOrder - tableName: " + tableName + ", adding child: " + childTable);
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
        log.info("fnGetTablesOrder - size of tableChildren: " + tableChildren.size());
        // Add leaf nodes (tables without incoming FKs)

        for (String tableName : tableChildrenBck.keySet()) {
            if (tableChildrenBck.get(tableName).isEmpty()) {
                log.info("fnGetTablesOrder - Adding 0 to visied: " + tableName);
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
        
        Integer maxOrder = 0;
        for(Map.Entry<String, Integer> entry : visited.entrySet()) {
            String tableName = entry.getKey();
            Integer order = entry.getValue();
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
        return maxOrder + 1;
    }
	
    @out(name = "result", type = Set.class, desc = "")
    public static Set<String> fnGetTablesByOrder(Integer order) throws Exception {
        //log.info("fnGetTablesByOrder - order: " + order + ", tables: " + tablesList);
       
        return tablesList.get(order);
    }


    @out(name = "result", type = Integer.class, desc = "")
    public static Integer fnGetTablesOrder(ArrayList<String> tableList, String dbInterfaceName, String dbSchemaName, String SourceEnv, String taskExecutionId) throws Exception {
        tablesList.clear();
        DatabaseMetaData md = getConnection(dbInterfaceName).getMetaData();
        Map <String, Set<String>> tableParents = new HashMap<>();

        for (String tableName : tableList) {
            //log.info("fnGetTablesOrder - tableName: " + tableName);
            ResultSet importedKeys = md.getImportedKeys(null, dbSchemaName, tableName);

            tableParents.put(tableName, new HashSet<>());
            while (importedKeys.next()) {
                String parentTable = importedKeys.getString("PKTABLE_NAME");
                //log.info("fnGetTablesOrder - tableName: " + tableName + ", parent: " + parentTable);
                if (tableList.contains(parentTable)) {
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
        
        Integer maxOrder = 0;
        for(Map.Entry<String, Integer> entry : visited.entrySet()) {
            String tableName = entry.getKey();
            Integer order = entry.getValue();
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
        return maxOrder + 1;
    }
}
