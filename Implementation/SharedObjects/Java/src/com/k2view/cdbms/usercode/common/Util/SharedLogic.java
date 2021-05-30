/////////////////////////////////////////////////////////////////////////
// Project Shared Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common.Util;

import java.lang.reflect.Type;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import java.util.regex.Pattern;						   
import java.util.stream.Collectors;

import com.github.jknack.handlebars.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.Globals;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.usercode.*;										 
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;

import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class SharedLogic {


	@out(name = "result", type = String.class, desc = "")
	public static String transform(String templateContent, Object data) throws Exception {
		Handlebars handlebars = new Handlebars();
		
		handlebars.registerHelper("indexPlus", new Helper<Integer>() {
			public Integer apply(Integer index, Options options) {
				return index + 1;
			}
		});
		
		Template template = handlebars.compileInline(templateContent);

		return template.apply(data);
		
	}

	@out(name = "res", type = Object.class, desc = "")
	public static Object buildTemplateData(String luTable, String targetDbInterface, String targetDbSchema, String targetDbTable) throws Exception {
		LUType luType = getLuType();
		if(luType == null)
			return null;
		
		List<String> luTableColumns = getLuTableColumns(luTable);
		Object[] targetTableData = getDbTableColumns(targetDbInterface, targetDbSchema, targetDbTable);
		
		Map<String, Object> map = new TreeMap<>();
		map.put("LU_TABLE", luTable);
		map.put("LU_TABLE_COLUMNS", luTableColumns);
		map.put("TARGET_INTERFACE", targetDbInterface);
		map.put("TARGET_SCHEMA", targetDbSchema);
		map.put("TARGET_TABLE", targetDbTable);
		map.put("TARGET_TABLE_COLUMNS", targetTableData[0]);
		map.put("TARGET_TABLE_PKS", targetTableData[1]);
		
		return map;
	}

	@out(name = "res", type = List.class, desc = "")
	public static List<String> getLuTableColumns(String table) throws Exception {
		List<String> al = null;// = new ArrayList<>();
		LUType luType = getLuType();
		if(luType == null || !luType.ludbObjects.containsKey(table))
			return al;
		//luType.ludbObjects.get(table).getLudbObjectColumns().forEach((s, col) -> al.add(col.getName()));
		al = new ArrayList<>(luType.ludbObjects.get(table).getLudbObjectColumns().keySet());
		return al;
	}

	@out(name = "res", type = List.class, desc = "")
	public static List<String> getLuTables() throws Exception {
		List<String> al = new ArrayList<>();
		LUType luType = getLuType();
		if(luType == null)
			return al;
		luType.ludbTables.forEach((s, s2) -> al.add(s));
		return al;
	}

	@out(name = "res", type = Object.class, desc = "")
	public static Object getLuTablesMappedByOrder(Boolean reverseInd) throws Exception {
		List<List<String>> buckets = new ArrayList<>();
		LUType luType = getLuType();
		if(luType == null)
			return "";
		List<TablePopulation> populations = luType.getPopulationCollection();
		// populations already ordered
		Set<String> tables = new HashSet<>();
		List<String> tmpBucket = new ArrayList<>();
		int currentOrder = 0;
		for (TablePopulation p : populations) {
			if (p.gettablePopulationOrder() > currentOrder) {
				if (!tmpBucket.isEmpty()) {
					buckets.add(tmpBucket);
					tmpBucket = new ArrayList<>();
				}
				currentOrder = p.gettablePopulationOrder();
			}
		
			//The table name in TablePopulation is kept in Upper case, to get the original name, loop over luType.ludbTables
			String originalTableName = p.getLudbObjectName();
			//log.info("handling Population Table: " + p.getLudbObjectName());
			for (String tableName : luType.ludbTables.keySet()) {
				if (tableName.equalsIgnoreCase(p.getLudbObjectName())) {
					originalTableName = tableName;
					break;
				}
			}
			//log.info("handling  Table: " + originalTableName);
			String tableFiltered = "" + fabric().fetch("broadway " + luType.luName + ".filterOutTDMTables tableName=" +
					originalTableName + ", luName=" + luType.luName).firstValue();
			if( !tables.contains(originalTableName) && !"null".equals(tableFiltered) && !Util.isEmpty(tableFiltered)) {
				tmpBucket.add(originalTableName);
				tables.add(originalTableName);
			}
		}
		// The last bucket
		if (!tmpBucket.isEmpty()) {
			buckets.add(tmpBucket);
		}
		if (reverseInd) {
			Collections.reverse(buckets);
		}
		
		return buckets;
		//return Json.get().toJson(buckets);
	}


	@out(name = "columns", type = Object[].class, desc = "")
	@out(name = "pks", type = Object[].class, desc = "")
	public static Object[] getDbTableColumns(String dbInterfaceName, String schema, String table) throws Exception {
		ResultSet rs = null;
		//Object[] result = new Object[2];
		try {
			DatabaseMetaData md = getConnection(dbInterfaceName).getMetaData();
			rs = md.getColumns(null, schema, table, null);
			List<String> al = new ArrayList<>();
			while (rs.next()) {
				al.add(rs.getString("COLUMN_NAME"));
			}
			//result[0] = al;
		
			// get PKs
			rs = md.getPrimaryKeys(null, schema, table);
			List<String> al2 = new ArrayList<>();
			while (rs.next()) {
				al2.add(rs.getString("COLUMN_NAME"));
			}
			//result[1] = al2;
			return new Object[]{al,al2};
		} finally {
			if (rs != null)
				rs.close();
		}
	}


	@out(name = "res", type = List.class, desc = "")
	public static List<String> getDbTables(String dbInterfaceName, String schema) throws Exception {
		ResultSet rs = null;
		List<String> al = new ArrayList<>();
		try {
			DatabaseMetaData md = getConnection(dbInterfaceName).getMetaData();
			rs = md.getTables(null, schema, "%", null);
			while (rs.next()) {
				al.add(rs.getString(3));
			}
		} finally {
			if (rs != null)
				rs.close();
		}
		return al;
	}


	@out(name = "res", type = Object.class, desc = "")
	public static Object buildSeqTemplateData(String seqName, String cacheDBName, String redisOrDBName, String initiationScriptOrValue) throws Exception {
		Map<String, Object> map = new TreeMap<>();
		map.put("SEQUENCE_NAME", seqName);
		map.put("CACHE_DB_NAME", cacheDBName);
		map.put("SEQUENCE_REDIS_DB", redisOrDBName);
		map.put("INITIATE_VALUE_FLOW", initiationScriptOrValue);
		return map;
	}

}
