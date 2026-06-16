package com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils;

import com.google.gson.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.LUTypeFactoryImpl;
import com.k2view.cdbms.shared.Utils;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.out;
import com.k2view.cdbms.utils.K2TimestampWithTimeZone;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.mtable.MTable;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.AI_ENVIRONMENT;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_PARAMETERS_SEPARATOR;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.MtableLookup;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnGetIIDListForMigration;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.TemplateUtils.SharedLogic.getDBCollection;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.TemplateUtils.SharedLogic.toSqliteType;
import static java.lang.Math.min;

import java.io.File;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked", "rawtypes"})
public class SharedLogic {
    private static final Map<String, Integer> PERMISSION_GROUPS = new HashMap() {{
        put("admin", 3);
        put("owner", 2);
        put("tester", 1);
    }};

	private static final Set<String> SQLITE_KEYWORDS = Set.of(
		"abort", "action", "add", "after", "all", "alter", "analyze", "and", "as",
		"asc", "attach", "autoincrement", "avg", "before", "begin", "between", "by",
		"cascade", "case", "cast", "check", "collate", "column", "commit", "conflict",
		"constraint", "count", "create", "cross", "cume_dist", "current",
		"current_date", "current_time", "current_timestamp", "database", "default",
		"deferrable", "deferred", "delete", "dense_rank", "desc", "detach",
		"distinct", "do", "drop", "each", "else", "end", "escape", "except",
		"exclude", "exclusive", "exists", "explain", "fail", "filter", "first_value",
		"following", "for", "foreign", "from", "full", "glob", "group", "groups",
		"having", "if", "ignore", "immediate", "in", "index", "indexed", "initially",
		"inner", "insert", "instead", "intersect", "into", "is", "isnull", "join",
		"key", "lag", "last_value", "lead", "left", "like", "limit", "match",
		"max", "min", "natural", "no", "not", "nothing", "notnull", "nth_value",
		"ntile", "null", "of", "offset", "on", "or", "order", "others", "outer",
		"over", "partition", "percent_rank", "plan", "pragma", "preceding",
		"primary", "query", "raise", "range", "rank", "recursive", "references",
		"regexp", "reindex", "release", "rename", "replace", "restrict", "right",
		"rollback", "row", "row_number", "rows", "returning", "savepoint", "select",
		"set", "sum", "table", "temp", "temporary", "then", "ties", "to",
		"transaction", "trigger", "unbounded", "union", "unique", "update", "using",
		"vacuum", "values", "view", "virtual", "when", "where", "window", "with",
		"without");

    private static final String TDM = "TDM";
    private static final String TABLES = "TABLES";
    private static final String PRODUCT_LOGICAL_UNITS = TDMDB_SCHEMA + ".product_logical_units";
    private static final String TASK_REF_EXE_STATS = TDMDB_SCHEMA + ".TASK_REF_EXE_STATS";
    private static final String TASKS_LOGICAL_UNITS = TDMDB_SCHEMA + ".tasks_logical_units";

    private static final String PENDING = "pending";
    private static final String RUNNING = "running";
    private static final String WAITING = "waiting";
    private static final String STOPPED = "stopped";
    private static final String RESUME = "resume";
    private static final String FAILED = "failed";
    private static final String COMPLETED = "completed";
    private static final String PARENTS_SQL = "SELECT lu_name FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE be_id=? AND lu_parent_id is null";
    private static final String GET_CHILDREN_SQL = "WITH RECURSIVE children AS ( " +
            "SELECT lu_name,lu_id,lu_parent_id,lu_parent_name FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE lu_name=? and be_id=? " +
            "UNION ALL SELECT a.lu_name, a.lu_id, a.lu_parent_id,a.lu_parent_name " +
            "FROM " + TDMDB_SCHEMA + ".product_logical_units a " +
            "INNER JOIN children b ON a.lu_parent_id = b.lu_id) " +
            "SELECT  string_agg('''' ||  unnest || '''' , ',') FROM children ,unnest(string_to_array(children.lu_name, ',')); ";
	public static final String MAP_LU_NAME = "lu_name";
	public static final String MAP_LU_TABLE = "lu_table";
	public static final String MAP_LU_TABLE_FIELD = "lu_table_field";
	public static final String MAP_PARAM_NAME = "param_name";
	public static boolean inTest = false;
	public static Map<String, List<Map<String, Object>>> allTables = new HashMap<>();
	private static HashMap<String, String> luShortMap = new HashMap<>();
    private static HashMap<String, String> tdmSeparators = new HashMap<>();

	public enum OverrideParamKey {
		BE_ID,
		LOGICAL_UNITS,
		SOURCE_ENVIRONMENT_NAME,
		TARGET_ENVIRONMENT_NAME,
		SELECTION_METHOD,
		ENTITY_LIST,
		CUSTOM_LOGIC_FLOW,
		CUSTOM_LOGIC_LU_NAME,
		BP_QUERY,
		PARAMETERS,
		GENERATE_DATA_PARAMS,
		NO_OF_ENTITIES,
		TASK_GLOBALS,
		RESERVE_IND,
		SELECTED_VERSION_TASK_EXE_ID,
		DATAFLUX_RETENTION_PARAMS,
		RESERVE_RETENTION_PARAMS,
		EXECUTION_NOTE,
		TABLE_FILTERS,
		PRE_EXECUTION_PROCESSES_PARAMS,
		POST_EXECUTION_PROCESSES_PARAMS,
		IMPLICIT_OVERRIDE_LOGICAL_UNITS
	}

    public static Object fnBatchStatistics(String i_batchId, String i_runMode) throws Exception {
        Object response;
        switch (i_runMode) {
            case "S":
                response = getFabricResponse("batch_summary '" + i_batchId + "'");
                break;
            case "D":
                response = getFabricResponse("batch_details '" + i_batchId + "'");
                break;
            case "H":
                Map<String, String> migHeader = new LinkedHashMap<>();
                fabric().fetch("batch_info ?", i_batchId).forEach(row -> {
                    if ("Batch command".equalsIgnoreCase(row.get("key").toString())) {
                        migHeader.put("Migration Command", row.get("value").toString());
                    }
                });

                return migHeader;
            default:
                response = new HashMap() {{
                    put("errorCode", "FAILED");
                    put("message", "Unknown run mode '" + i_runMode + "'. Available modes are 'S' for batch summary, 'D' for the details and 'H'.");
                }};
                break;
        }
        return response;
    }

    public static Object getFabricResponse(String fabricCommand) throws SQLException {
        List objects = new ArrayList();
        fabric().fetch(fabricCommand).forEach(row -> {
            Map rowMap = new HashMap<String, Object>();
            rowMap.putAll(row);
            objects.add(rowMap);
        });
        return objects;
    }

	public interface Rule {}

	public class ParameterType implements Rule {
		Rules group;
		String operator;
	}

	public class Rules {
		List<Rule> rules;
		String operator;
	}

	public interface ComplexString {
		List<String> getList();
	}

	public static class ComplexStringImpl implements ComplexString {
		String str;

		public ComplexStringImpl(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}

		@Override
		public List<String> getList() {
			String[] arr = str.split(",");
			List<String> list = new ArrayList<>();
			for (int i= 0; i < arr.length; i++) {
				list.add(arr[i].trim());
			}
			return list;
		}
	}

	public static class ComplexListStringImpl implements ComplexString {
		List<String> list;

		public ComplexListStringImpl(List list) {
			this.list = list;
		}

		@Override
		public List<String> getList() {
			return list;
		}
	}

	public class RuleDetail implements Rule {
		String condition;
		String field;
		String operator;
		List<Object> validValues;
		String type;
		ComplexString data;
		String table;
	}

	public static class InterfaceAdapter implements JsonDeserializer {
		public Rule deserialize(JsonElement jsonElement, Type type,
							 JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			Object prim = jsonObject.get("group");
			if (prim == null) {
				return jsonDeserializationContext.deserialize(jsonElement, RuleDetail.class);
			} else {
				return jsonDeserializationContext.deserialize(jsonElement, ParameterType.class);
			}
		}
	}

	public static class InterfaceStringAdapter implements JsonDeserializer {
		public ComplexString deserialize(JsonElement jsonElement, Type type,
								JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			if (jsonElement.isJsonPrimitive() ) {
				return new ComplexStringImpl(jsonElement.getAsString()) ;
			}
			return new ComplexListStringImpl(jsonDeserializationContext.deserialize(jsonElement, List.class));
		}
	}

	public static class MatchQuery {
		String sql;
		Set<String> lus;

		public MatchQuery(String sql, Set<String> lus) {
			this.sql = sql;
			this.lus = lus;
		}
	}

	public static MatchQuery getListOfMatchingEntitiesQuery(Long beID, String sourceEnv, ParameterType res, boolean analysisCount)  {
		StringBuffer ret = new StringBuffer(" ( ");
		Set<LuTable> usedTables = new HashSet<>();
		Set<String> lus = new HashSet<>();
		processGroup(res, ret, usedTables, lus,analysisCount);
		expandSql(beID, sourceEnv, usedTables, ret, lus);
		ret.append(" )");
		MatchQuery mq = new MatchQuery(ret.toString(), lus);
		return mq;
	}

	private static void processGroup(ParameterType res, StringBuffer ret, Set<LuTable> usedTables, Set<String> lus,boolean analysisCount)  {
		Rules group = res.group;
		String prevOperator = "";
		boolean isList = false;
		for (Rule r : group.rules) {
			if (r instanceof RuleDetail) {
				RuleDetail rule = (RuleDetail) r;
				//  and ad.state = 'NY'
				if (rule.condition.contains("IN")) {
					isList = true;
				} else {
					isList = false;
				}
				Map<String, Object> map = getMap(rule.field);
                boolean isUnaryOp = false ;
                if(rule.condition.contains("NULL")){
                    isUnaryOp = true;
                }
				if (problematic(rule.condition)) {
					ret.append(prevOperator).append(formatValue(rule.data, rule.type, isList,analysisCount,isUnaryOp)).append(" ").
							append(rule.condition).append(" ").append(reMap(map)).append(" \n    ");
				} else {
					ret.append(prevOperator).append(reMap(map)).append(" ").
							append(rule.condition).append(" ").append(formatValue(rule.data, rule.type, isList,analysisCount,isUnaryOp)).append(" \n    ");
				}
				prevOperator = " " + rule.operator + " ";
				usedTables.add(getTableName(map));
				lus.add(map.get(MAP_LU_NAME).toString());
			} else {
				ParameterType rule = (ParameterType) r;
				StringBuffer ret1 = new StringBuffer();
				processGroup(rule, ret1, usedTables, lus,analysisCount);
				ret.append(prevOperator).append(" ( ").append(ret1).append(" )");
				prevOperator = " " + rule.operator + " ";
			}
		}
	}

	private static boolean problematic(String condition) {
		// These 4 asymmetric (<, >, <=, >=) that cause reverse of operators; others (like IN) do not.
		return condition.contains("<") || condition.equalsIgnoreCase(">") ;
	}

	static void expandSql(Long beID, String sourceEnv, Set<LuTable> usedTables, StringBuffer ret, Set<String> usedLu) {
		StringBuffer prep = new StringBuffer("select distinct be1.root_iid as iid, root1.task_execution_id from \n");
		processLUs(beID, sourceEnv, usedLu, prep);
		StringBuffer ret1 = processTableDependencies(usedTables);
		processUsedTables(usedTables, prep);
		prep.append("\n  where \n");
		prep.append(ret1);
		ret.insert(0, prep);
		ret.append("\n");
	}

	private static void processLUs(Long beID, String sourceEnv, Set<String> usedLu, StringBuffer prep) {
		int i = 1;
		boolean first = true;
		for (String luName : usedLu) {
			// Customer.tdm_be_iids be1
			//    INNER JOIN Customer.fabric_tdm_root root1 ON be1.be_id = 1 and root1.iid =  be1.iid  and root1.source_env = 'Production'
			//
			if (!first) {
				prep.append("\n INNER JOIN ");
			}
			prep.append("   ").append(luName).append(".tdm_be_iids be" ).append(i);
			if (!first) {
				prep.append(" ON be1.be_id = be").append(i).append(".be_id ").append("and be1.root_iid = be").append(i).append(".root_iid");
			}

			prep.append("\n INNER JOIN  ").append(luName).append(".fabric_tdm_root root" ).append(i).append(" ON ");
			if (first) {
				prep.append(" be1.be_id = '").append(beID).append("' AND ");
			}
			prep.append("root").append(i).append(".__iid =  be").append(i).append(".__iid and root").
					append(i).append(".source_env = '").append(sourceEnv).append( "'");
			luShortMap.put(luName, "be"+i);
			i ++;
			first = false;
		}
	}

	private static void processUsedTables(Set<LuTable> usedTables, StringBuffer prep) {
		for (LuTable table : usedTables) {
			prep.append("\n INNER JOIN ").append(table.luName).append(".").append(table.luTable);
			prep.append(" ON ").append(getLuShort(table.luName)).append(".__iid = ").append(table.luName).append(".").append(table.luTable).append(".__iid ");
		}
	}
				
	private static String getLuShort(String luName) {
		return luShortMap.get(luName);
	}

	private static StringBuffer processTableDependencies(Set<LuTable> usedTables) {
		StringBuffer prep = new StringBuffer();
		if (inTest) {
			return prep;
		}
		boolean first = true;
		List<LuTable> addedTables = new ArrayList<>();
		for (LuTable table1 : usedTables) {
			for (LuTable table2 : usedTables) {
				if (table1 == table2) {
					// skip same tables
					continue;
				}
				if (!table1.luName.equals(table2.luName)) {
					// skip non same LU
					continue;
				}
				UserCode.log.info("processTableDependencies LU {} for {} and {} ", table1.luName, table1.luTable, table2.luTable);
				LUType luType = LUTypeFactoryImpl.getInstance().getTypeByName(table1.luName);
				List<String> path = buildParentTablePath(luType, table1, table2);
				UserCode.log.info("processTableDependencies path {}", path);
				if (!Util.isEmpty(path)) {
					Map<String, Map<String, List<LudbRelationInfo>>> rel = luType.getLudbPhysicalRelations();
					for (int i = 1 ; i < path.size(); i ++) {
						TableObject tableParent = (TableObject) luType.ludbObjects.get(path.get(i-1));
						TableObject tableChild = (TableObject) luType.ludbObjects.get(path.get(i));
						UserCode.log.info("processTableDependencies for from  {} to {} ",  path.get(i-1), path.get(i));

						List<LudbRelationInfo> childRelations = rel.get(tableParent.k2StudioObjectName).get(tableChild.k2StudioObjectName);
						for (LudbRelationInfo childRelation : childRelations) {
							if (!first) {
								prep.append(" AND ");
							}
							prep.append(table1.luName).append(".").
									append(tableParent.ludbObjectName).append(".").
									append(childRelation.from.get("column")).append("=").
									append(table1.luName).append(".").
									append(tableChild.ludbObjectName).append(".").
									append(childRelation.to.get("column")).append("\n");
							UserCode.log.info("processTableDependencies SQL :: \n {} ", prep);
							addedTables.add(new LuTable(table1.luName, tableParent.ludbObjectName.toUpperCase()));
							first = false;
						}
					}
				}
			}
		}
		if (!first) {
			prep.append(" AND ");
			UserCode.log.info("processTableDependencies SQL :: \n {} ", prep);
		}
		for (LuTable lt : addedTables) {
			if (! usedTables.contains(lt)) {
				usedTables.add(lt);
				UserCode.log.info("added used table {} ", lt);
			}
		}
		return prep;
	}

	/**
	 * Find the parent path fom table1 to table2, if such exists
	 *
	 * @param luType
	 * @param table1
	 * @param table2
	 * @return path, if exists; empty list otherwise,
	 * list of strings - table names starting with table1, ending with table2;
	 * Each next name is the direct parent table of the previous table.
	 */
	private static List<String> buildParentTablePath(LUType luType, LuTable table1, LuTable table2) {
		List<String> path = new ArrayList<>();
		TableObject table1Obj = (TableObject) luType.ludbObjects.get(table1.luTable);
		buildPath(luType, table2, table1.luTable, path);
		return path;
	}

	private static boolean buildPath(LUType luType, LuTable table2, String table1, List<String> res) {
        try{
            TableObject table1Obj = (TableObject) luType.ludbObjects.get(table1);
            if (table1Obj.isRootObject()) {
                UserCode.log.info("buildPath stop on root object {}", table1Obj.ludbObjectName);
                return false;
            }
            if (table1Obj.ludbObjectName.equalsIgnoreCase(table2.luTable)) {
                res.add(table2.luTable);
                UserCode.log.info("buildPath stop on table2.luTable {}", table2.luTable);
                return true;
            }
            Map<String, List<LudbRelationInfo>> map = luType.ludbOppositePhysicalRelations.get(table1);
            for (String parent : map.keySet()) {
                UserCode.log.info("buildPath iterate on table2.luTable {}",  parent);
                boolean res1 = buildPath(luType, table2, parent, res);
                if (res1) {
                    UserCode.log.info("buildPath ADDED on table1 {}",  table1);
                    res.add(table1);
                    return true;
                }
            }
            return false;
        }catch (Exception e){
            throw new RuntimeException("No FK relation has been identified, unable to build path between tables " + table2.luTable + " and " + table1 + " the implementation must be fixed.");
        }
	}

	static LuTable getTableName(Map<String, Object> map) {
		return new LuTable( map.get(MAP_LU_NAME).toString(), map.get(MAP_LU_TABLE).toString().toUpperCase());
	}

	static CharSequence reMap(Map<String, Object> map) {
		StringBuffer str = new StringBuffer();
		str.append(map.get(MAP_LU_NAME)).append(".").append(map.get(MAP_LU_TABLE)).append(".").append(map.get(MAP_LU_TABLE_FIELD));
		return str;
	}

	static Map<String, Object> getMap(String field) {
		String columns [] = field.split("\\.");
		Map<String, Object> mapListInputs = new HashMap<>();
		mapListInputs.put(MAP_LU_NAME,columns[0]);
		mapListInputs.put(MAP_PARAM_NAME,columns[1]);
		try {
			List<Map<String, Object>> mapList;
			if (!inTest) {
				 mapList = MtableLookup("LuParamsMapping", mapListInputs, MTable.Feature.caseInsensitive);
			} else {
				mapList = localMap("LuParamsMapping", mapListInputs, MTable.Feature.caseInsensitive);
			}
			return mapList.get(0);
		} catch (Exception e) {
			UserCode.log.error("Failed to process MTable LuParamsMapping " + mapListInputs, e);
		}
		return null;
	}

	private static List<Map<String, Object>> localMap(String luParamsMapping, Map<String, Object> mapListInputs, MTable.Feature feature) {
		List<Map<String, Object>> list = allTables.get(luParamsMapping);
		List<Map<String, Object>> mapList = new ArrayList<>();
		if (list != null) {
			Map<String, Object> map = new CaseInsensitiveMap<>();
			for (Map<String, Object> rowMap : list) {
				boolean res = true;
				for (String key : mapListInputs.keySet()) {
					if (!mapListInputs.get(key).toString().equalsIgnoreCase(rowMap.get(key).toString())) {
						res = false;
					}
				}
				if (res) {
					mapList.add(rowMap);
				}
			}
		}
		return mapList;
	}

	public static void setMap(String name, List<String> columns, List<List<String>> data) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (List<String>row : data) {
			int i  = 0;
			Map<String, Object> map = new CaseInsensitiveMap<>();
			for (String col : columns) {
				map.put(col, row.get(i));
				i ++;
			}
			list.add(map);
		}
		allTables.put(name,list);
	}

	static CharSequence formatValue(ComplexString value, String type, boolean isList,boolean analysisCount,boolean isUnaryOp) {
        if (value == null) {
            return "";
        }
        
        String wrap = "'";
        if (type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("real")
                || type.equalsIgnoreCase("number")) {
            wrap = "";
        }
        
        StringBuffer buff = new StringBuffer();
        boolean first = true;
        if (isList) {
            buff.append("(");
        }
        
        for (int i = 0; i < value.getList().size(); i++) {
            if (!first) {
                buff.append(", ");
            }
            // Replace special characters
            String val = value.getList().get(i);
            if(!analysisCount){
                val = val.replace("'", "'''").replace("\\\\", "\\").replace("\\\\", "");
            }else{
                val = val.replace("'", "''").replace("\\\\", "\\").replace("\\\\", "");
            }

            if(!isUnaryOp){
            buff.append(wrap).append(val).append(wrap);
            }
            first = false;
        }
        
        if (isList) {
            buff.append(")");
        }
        
        return buff;
    }
    

	@out(name = "result", type = String.class, desc = "")
	public static String generateListOfMatchingEntitiesQuery(Long beID, Boolean paramsCoupling, String json, String whereStmt, String sourceEnv,Boolean cloneInd,boolean analysisCount) throws Exception {
		if (paramsCoupling) {
			Gson gson = new Gson().newBuilder().registerTypeAdapter(Rule.class, new InterfaceAdapter()).
					registerTypeAdapter(ComplexString.class, new InterfaceStringAdapter()).create();
			ParameterType res = gson.fromJson(json, ParameterType.class);
			MatchQuery matchQuery = getListOfMatchingEntitiesQuery(beID, sourceEnv, res,analysisCount);
            Set <String> lus = matchQuery.lus;
            Object result = fabric().fetch("broadway TDM.CheckIfSchemasExists lus=?", lus).firstValue();
            if(result!=null){
                throw new RuntimeException(result.toString());
            }
            UserCode.log.info(matchQuery.sql);
			return matchQuery.sql;
		}
        String iidSeparator = "" + db(TDM).fetch("Select param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where LOWER(param_name) = 'iid_separator'").firstValue();
        //separator = !Util.isEmpty(iidSeparator) ? iidSeparator : "_";
        String separator = "_";
        if (!Util.isEmpty(iidSeparator) && !"null".equals(iidSeparator)) {
            separator = iidSeparator;
        }
		//UserCode.log.info("generateListOfMatchingEntitiesQuery - whereStmt: " + whereStmt);
		String rootLUsSql = "SELECT ARRAY_AGG(lu_name) FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE " +
		    "be_id = ? AND lu_parent_id is null";
		
		String rootLUs = "" + db(TDM).fetch(rootLUsSql, beID).firstValue();
		
		String paramsSql = !Util.isEmpty(whereStmt) ? whereStmt + ")" : "";
		paramsSql = paramsSql.replaceAll("FROM " , "FROM " + TDMDB_SCHEMA + ".");
		paramsSql = paramsSql.replaceAll("WHERE ", "WHERE ROOT_LU_NAME = ANY('" + rootLUs + "') AND SOURCE_ENVIRONMENT = '" + sourceEnv + "' AND (");
        paramsSql = paramsSql.replaceAll("INTERSECT ", ") INTERSECT ");
        paramsSql = paramsSql.replaceAll("UNION ", ") UNION ");
        if(AI_ENVIRONMENT.equals(sourceEnv) && cloneInd ){
            paramsSql = "SELECT distinct '" + sourceEnv + "'||'" + separator + "'||" + "root_iid as entity_id FROM (" + paramsSql + ") p";
        }else{
            paramsSql = "SELECT distinct root_iid as entity_id FROM (" + paramsSql + ") p";
        }
				
		//UserCode.log.info("generateListOfMatchingEntitiesQuery - paramsSql: " + paramsSql);
		return paramsSql;
	}

	@out(name = "result", type = Map.class, desc = "")
	public static Map<String,String> fnGetRootEntityId(String luName, String iid, String entityId, String taskExecId) throws Exception {
		Map<String, String> rootEntityInfo = new HashMap<>();

        Boolean childLUInd  = false;
        Object childLUObj = fabric().fetch("set CHILD_LU_IND").firstValue();
        if (childLUObj != null) {
            childLUInd  = Boolean.parseBoolean(childLUObj.toString());
        } 
		
		String rootEntityId = iid;
		String rootLuName = luName;
        String parentEntityId = null;
        String parentLuName = "";
		
        if(childLUInd) {
		    String parentRootSql = "SELECT parent_lu_name, parent_entity_id, root_lu_name, root_entity_id " +
		            "FROM " + TDMDB_SCHEMA + ".task_execution_entities " +
		            "WHERE task_execution_id = ? AND lu_name = ? AND entity_id = ?";
		
		
		    Db.Row parentRootFields = db(TDM).fetch(parentRootSql, taskExecId, luName, entityId).firstRow();
            rootEntityId = parentRootFields.get("root_entity_id").toString();
		    rootLuName = parentRootFields.get("root_lu_name").toString();
            parentEntityId = parentRootFields.get("parent_entity_id").toString();
            parentLuName = parentRootFields.get("parent_lu_name").toString();
		}
		rootEntityInfo.put("rootLuName", rootLuName);
		rootEntityInfo.put("rootEntityId", rootEntityId);
            rootEntityInfo.put("parentLuName", parentLuName);
		    rootEntityInfo.put("parentEntityId", parentEntityId);
		return rootEntityInfo;
	}

    public static boolean checkWsResponse(Map<String, Object> response) {
        if (response != null && response.get("errorCode") != null && response.get("errorCode").equals("SUCCESS")) {
            return true;
        } else {
            return false;
        }
    }

    public static Map<String, Object> wrapWebServiceResults(String errorCode, Object message, Object result) {
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("result", result);
        return response;
    }

	@out(name = "result", type = Map.class, desc = "")
	public static Map<String,Object> fnGetRetentionPeriod() throws Exception {
		Map<String, Object> map;
		try {
			String sql = "select * from " + TDMDB_SCHEMA + ".tdm_general_parameters where tdm_general_parameters.param_name = 'tdm_gui_params'";

			Object params = db(TDM).fetch(sql).firstRow().get("param_value");
			Map result = Json.get().fromJson((String) params, Map.class);

			map = new HashMap<>();

			Object retentionDefaultPeriod = result.get("retentionDefaultPeriod");
			if (retentionDefaultPeriod != null) {
				map.put("retentionDefaultPeriod", retentionDefaultPeriod);
			}
			Object retentionPeriodTypes = result.get("retentionPeriodTypes");
			if (retentionPeriodTypes != null) {
				map.put("retentionPeriodTypes", retentionPeriodTypes);
			}
			Object reserveDefaultPeriod = result.get("reservationDefaultPeriod");
			if (reserveDefaultPeriod != null) {
				map.put("reservationDefaultPeriod", reserveDefaultPeriod);
			}
			Object reservationPeriodTypes = result.get("reservationPeriodTypes");
			if (reservationPeriodTypes != null) {
				map.put("reservationPeriodTypes", reservationPeriodTypes);
			}
			Object versioningRetentionPeriod = result.get("versioningRetentionPeriod");
			if (versioningRetentionPeriod != null) {
				map.put("versioningRetentionPeriod", versioningRetentionPeriod);
			}
			Object versioningRetentionPeriodForTesters = result.get("versioningRetentionPeriodForTesters");
			if (versioningRetentionPeriodForTesters != null) {
				map.put("versioningRetentionPeriodForTesters", versioningRetentionPeriodForTesters);
			}
			sql = "SELECT param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'MAX_RETENTION_DAYS_FOR_TESTER'";
			Object maxRetentionDays = db(TDM).fetch(sql).firstValue();
			if (maxRetentionDays != null) {
				Map<String, Object> testers = new HashMap<>();
				testers.put("units", "Days");
				testers.put("value",  Long.valueOf((String) maxRetentionDays));
				map.put("maxRetentionPeriodForTesters", testers);
			}
			sql = "SELECT param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'MAX_RESERVATION_DAYS_FOR_TESTER'";
			Object maxReserveDays = db(TDM).fetch(sql).firstValue();
			if (maxReserveDays != null) {
				Map<String, Object> testers = new HashMap<>();
				testers.put("units", "Days");
				testers.put("value",  Long.valueOf((String) maxReserveDays));
				map.put("maxReservationPeriodForTesters", testers);
			}
			return map;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to get retention Period Definitions from tdm_general_parameters TDMDB");
		}
	}
	
	public static String fnGetUserPermissionGroup(String userName) {
        try {
			String fabricRoles = fnGetUserRoles(userName);
            return fnGetPermissionGroupByRoles(fabricRoles);
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage());
        }
    }
	
    public static String fnGetUserRoles(String userName) {
        try {
            String fabricRoles = "";
            if (userName == null || "".equals(userName) || userName.equalsIgnoreCase(sessionUser().name())) {
                Set<String> roles = new HashSet<>(sessionUser().roles());
                roles.remove("Everybody");
                fabricRoles = String.join(TDM_PARAMETERS_SEPARATOR, roles);
            } else {
                List<String> roles = new ArrayList<>();
                if (userName.contains("##")) {
                    String[] userData = userName.split("##");
                    String rolePart = userData[1];    
                    if (rolePart.contains(TDM_PARAMETERS_SEPARATOR)) {
                        String[] roleGroups = rolePart.split(TDM_PARAMETERS_SEPARATOR);
                        roles.addAll(Arrays.asList(roleGroups));
                    } else {
                        roles.add(rolePart); 
                    }
                } else {
                    String command = "list users user_filter=" + "'" + userName + "'";
                    String found = "" + fabric().fetch(command).firstRow().get("user");
                        if (userName.equals(found)) {
                            roles.add(found);
                        }
                    }
                fabricRoles = String.join(TDM_PARAMETERS_SEPARATOR, roles);
            }
            return fabricRoles;
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage());
        }
    }
	
	//checks if the registered user is an owner of the given environment
    public static Boolean fnIsOwner(String envId) throws Exception {
        List<Map<String, Object>> envsList = fnGetUserEnvs("");
        boolean found = false;
        for (Map<String, Object> envsGroup : envsList) {
            Map.Entry<String, Object> entry = envsGroup.entrySet().iterator().next();
            List<Map<String, Object>> groupByType = (List<Map<String, Object>>) entry.getValue();
            for (Map<String, Object> env : groupByType) {
                if ("owner".equals(env.get("role_id").toString())) {
                    if (env.get("environment_id").toString().equals(envId)) {
                        found = true;
                        break;
                    }
                }
            }
        }
        return found;
    }

	//checks if the registered user is an owner of the given environment or admin
    public static Boolean fnIsAdminOrOwner(String envId, String userName) throws Exception {
		String permissionGroup = fnGetUserPermissionGroup(userName);
		if("admin".equalsIgnoreCase(permissionGroup)) {
			return true;
		}
        List<Map<String, Object>> envsList = fnGetListOfEnvsByUser(userName);
        boolean found = false;
        for (Map<String, Object> envsGroup : envsList) {
            Map.Entry<String, Object> entry = envsGroup.entrySet().iterator().next();
            List<Map<String, Object>> groupByType = (List<Map<String, Object>>) entry.getValue();
            for (Map<String, Object> env : groupByType) {
                if ("owner".equals(env.get("role_id").toString())) {
                    if (env.get("environment_id").toString().equals(envId)) {
                        found = true;
                        break;
                    }
                }
            }
        }
        return found;
    }
    public static Boolean fnIsAdminByRole(String role) throws Exception {
        String permissionGroup = fnGetPermissionGroupByRoles(role);
        return "admin".equalsIgnoreCase(permissionGroup);
    }

    public static Boolean fnIsAdminOrOwnerByRoles(String roles) throws Exception {
        String permissionGroup = fnGetPermissionGroupByRoles(roles);
        return "admin".equalsIgnoreCase(permissionGroup) || "owner".equalsIgnoreCase(permissionGroup);
    }

	public static List<Map<String, Object>> fnGetListOfEnvsByUser(String userName) {
		List<Map<String, Object>> rowsList = new ArrayList<>();

		//Check the permission group of the user.
		//If the permission group is Admin => select all the active environments
		String permissionGroup = fnGetUserPermissionGroup(userName);
		if ("admin".equalsIgnoreCase(permissionGroup)){
			String allEnvs = "Select env.environment_id,env.environment_name,\n" +
				"  Case When env.allow_read = True And env.allow_write = True Then 'BOTH'\n" +
				"    When env.allow_write = True Then 'TARGET' Else 'SOURCE'\n" +
				"  End As environment_type,\n" +
				"  'admin' As role_id,\n" +
				"  'admin' As assignment_type\n" +
				"From " + TDMDB_SCHEMA + ".environments env\n" +
				"Where env.environment_status = 'Active'";
			Db.Rows rows = Util.rte(() -> db(TDM).fetch(allEnvs));
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					Util.rte(() -> rowMap.put(columnName, resultSet.getObject(columnName)));
				}
			rowsList.add(rowMap);
			}
			if (rows != null) {
				rows.close();
			}

		} else {
			Util.rte(() -> rowsList.addAll(fnGetEnvsByUser(userName, null)));
		}

		List<Map<String, Object>> result = new ArrayList<>();
		List<Map<String, Object>> sourceEnvs = new ArrayList<>();
		List<Map<String, Object>> targetEnvs = new ArrayList<>();

		for(Map<String, Object> row:rowsList){
			Map<String, Object> envData=new HashMap<>();
			envData.put("environment_id",row.get("environment_id"));
			envData.put("environment_name",row.get("environment_name"));
			envData.put("role_id",row.get("role_id"));
			envData.put("assignment_type",row.get("assignment_type"));

			if("SOURCE".equals(row.get("environment_type"))||"BOTH".equals(row.get("environment_type"))){
				sourceEnvs.add(envData);
			}
			if("TARGET".equals(row.get("environment_type"))||"BOTH".equals(row.get("environment_type"))){
				targetEnvs.add(envData);
			}
		}

		Map<String, Object> sourceEnvsMap=new HashMap<>();
		sourceEnvsMap.put("source environments",sourceEnvs);
		result.add(sourceEnvsMap);
		Map<String, Object> targetEnvsMap=new HashMap<>();
		targetEnvsMap.put("target environments",targetEnvs);
		result.add(targetEnvsMap);

		return result;
	}
	@out(name = "result", type = String.class, desc = "")
	public static String fnGetPermissionGroupByRoles(String roles) throws Exception {
		Integer[] weight = {0};
		weight[0]= Util.rte(() -> fnGetPermissionGroupWeight(roles));
		if (weight[0] == 0) {
			UserCode.log.error("Can't find permission group for the user.");
			return "";
		} else {
			String permissionGroup = null;
			for (Map.Entry<String, Integer> e : PERMISSION_GROUPS.entrySet()) {
				if (e.getValue().equals(weight[0])) {
					permissionGroup = e.getKey();
					break;
				}
			}

			return permissionGroup;
		}
	}
	@out(name = "result", type = Integer.class, desc = "")
	public static Integer fnGetPermissionGroupWeight(String roles) throws Exception {
		Integer[] weight = {0};
		String sql = "select permission_group from " + TDMDB_SCHEMA + ".permission_groups_mapping where fabric_role = ANY (string_to_array(?,?))";
		Util.rte(() -> db(TDM).fetch(sql, roles,TDM_PARAMETERS_SEPARATOR).forEach(row -> {
			Integer nextWeight = PERMISSION_GROUPS.get(row.get("permission_group"));
			if (nextWeight != null && nextWeight > weight[0]) {
				weight[0] = nextWeight;
			}
		}));
		return weight[0];
	}

	@out(name = "result", type = List.class, desc = "")
	public static Set<Map<String,Object>> fnGetEnvsByUser(String userName, Set<Long> productIds) throws Exception {
		Set<Map<String, Object>> rowsList = new HashSet<>();
		String fabricRoles="";
		try{
			fabricRoles=fnGetUserRoles(userName);
		} catch(Throwable t) {
			throw new RuntimeException(t.getMessage());
		}
	
		String productFilterJoin = "";
		String productFilterWhere = "";
	
		if (productIds != null && !productIds.isEmpty()) {
			String productIdsString = productIds.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));
			productFilterJoin = " JOIN " + TDMDB_SCHEMA + ".environment_products ep ON env.environment_id = ep.environment_id";
			productFilterWhere = " AND ep.status = 'Active' AND ep.enable_product = TRUE AND ep.product_id IN (" + productIdsString + ")";
		}
	
		//get the environments where the user is the owner
		String query1 = "select env.*, " +
			"CASE when env.allow_read = true and env.allow_write = true THEN 'BOTH' when env.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, 'owner' as role_id, 'owner' as assignment_type " +
			"from " + TDMDB_SCHEMA + ".environments env JOIN " + TDMDB_SCHEMA + ".environment_owners o on o.environment_id = env.environment_id" +
			productFilterJoin +
			" where (o.user_id = (?) or o.user_id = ANY(string_to_array(?, '" + TDM_PARAMETERS_SEPARATOR + "')))" +
			" and env.environment_status = 'Active'" +
			productFilterWhere;
	
		//log.info("fnGetEnvsByuser - query 1 for user Name " + userName + "is: " + query1);
		Db.Rows rows = db(TDM).fetch(query1, userName, fabricRoles);
	
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			rowsList.add(rowMap);
		}
	
		String envIds = "(";
		if (!rowsList.isEmpty()) {
			for (Map<String, Object> row : rowsList) envIds += row.get("environment_id") + ",";
			envIds = envIds.substring(0, envIds.length() - 1);
		}
		envIds += ")";
	
		//get the environments where the user is assigned to a role by their username
		String query2 = "select env.*, r.*, " +
			"CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, r.role_id, 'user' as assignment_type " +
			"from " + TDMDB_SCHEMA + ".environments env JOIN " + TDMDB_SCHEMA + ".environment_roles r on env.environment_id = r.environment_id JOIN " + TDMDB_SCHEMA + ".environment_role_users u on r.role_id = u.role_id " +
			productFilterJoin +
			" where lower(r.role_status) = 'active' " +			
			" and u.user_id = (?) " +
			" and u.user_type = 'ID' " +
			" and env.environment_status = 'Active'";
		// remove the list of environments returned by query 1;
		query2 += "()".equals(envIds) ? "" : " and env.environment_id not in " + envIds;
		query2 += productFilterWhere;
		rows = db(TDM).fetch(query2, userName);
	
		//log.info("fnGetEnvsByuser - query 2 for user Name " + userName + "is: " + query2);
	
		columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			rowsList.add(rowMap);
		}
	
		envIds = "(";
		if (!rowsList.isEmpty()) {
			for (Map<String, Object> row : rowsList) envIds += row.get("environment_id") + ",";
			envIds = envIds.substring(0, envIds.length() - 1);
		}
		envIds += ")";
	
		//get the environments where the user id is one of the Fabric Roles
		String query3 = "select env.*, r.*, " +
			"CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, r.role_id, 'user' as assignment_type " +
			"from " + TDMDB_SCHEMA + ".environments env JOIN " + TDMDB_SCHEMA + ".environment_roles r on env.environment_id = r.environment_id JOIN " + TDMDB_SCHEMA + ".environment_role_users u on r.role_id = u.role_id " +
			productFilterJoin +
			" where lower(r.role_status) = 'active' " +			
			" and u.user_id = ANY(string_to_array(?, '" + TDM_PARAMETERS_SEPARATOR + "')) " +
			" and u.user_type = 'GROUP' " +
			" and env.environment_status = 'Active'";
		// remove the list of environments returned by query 1+2;
		query3 += "()".equals(envIds) ? "" : " and env.environment_id not in " + envIds;
		query3 += productFilterWhere;
		rows = db(TDM).fetch(query3, fabricRoles);
	
		//log.info("fnGetEnvsByuser - query 3 for Fabric Roles < " + fabricRoles + "> is: " + query3);
	
		columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			rowsList.add(rowMap);
		}
	
		envIds = "(";
		if (!rowsList.isEmpty()) {
			for (Map<String, Object> row : rowsList) envIds += row.get("environment_id") + ",";
			envIds = envIds.substring(0, envIds.length() - 1);
		}
		envIds += ")";
	
		//get the environments where the user is assigned to a role by 'ALL' assignment
		String query4 = "select env.*, r.*, " +
			"CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type " +
			", r.role_id, 'all' as assignment_type " +
			"from " + TDMDB_SCHEMA + ".environments env JOIN " + TDMDB_SCHEMA + ".environment_roles r on env.environment_id = r.environment_id JOIN " + TDMDB_SCHEMA + ".environment_role_users u on r.role_id = u.role_id " +
			productFilterJoin +
			" where lower(r.role_status) = 'active' " +			
			" and lower(u.username) = 'all' " +
			" and env.environment_status = 'Active'";
		// remove the list of environments returned by queries 1+2+3;
		query4 += "()".equals(envIds) ? "" : " and env.environment_id not in " + envIds;
		query4 += productFilterWhere;
		rows = db(TDM).fetch(query4);
	
		//log.info(" fnGetEnvsByuser - query 4 (get ALL roles) is: " + query4);
	
		columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			rowsList.add(rowMap);
		}
		envIds = "(";
		if (!rowsList.isEmpty()) {
			for (Map<String, Object> row : rowsList) envIds += row.get("environment_id") + ",";
			envIds = envIds.substring(0, envIds.length() - 1);
		}
		envIds += ")";
	
		// Query 5: Fetch all active environments that the user is not
		String query5 = "WITH categorized_env AS ( " +
			"SELECT env.*, " +
			"CASE " +
			"   WHEN env.allow_read = true AND env.allow_write = true THEN 'BOTH' " +
			"   WHEN env.allow_write = true THEN 'TARGET' " +
			"   ELSE 'SOURCE' " +
			"END AS environment_type, " +
			"'user' AS assignment_type, " +
			"0 AS role_id " +
			"FROM " + TDMDB_SCHEMA + ".environments env " +
			productFilterJoin +
			" WHERE env.environment_status = 'Active' " +
			productFilterWhere +
			" ) " +
			"SELECT * FROM categorized_env " +
			"WHERE environment_type IN ('BOTH', 'SOURCE')";
		// remove the list of environments returned by queries 1+2+3+4;
		query5 += "()".equals(envIds) ? "" : " and categorized_env.environment_id not in " + envIds;
		rows = db(TDM).fetch(query5);
	
		// log.info("fnGetEnvsByuser - query 5 (get ALL Env) is: " + query5);
	
		columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				if("sync_mode".equalsIgnoreCase(columnName)){
					rowMap.put(columnName, "OFF");
				}else if ("environment_type".equalsIgnoreCase(columnName)){
					rowMap.put(columnName, "SOURCE");
				}else{
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
			}
			rowsList.add(rowMap);
		}
		if (rows != null) {
			rows.close();
		}
		// Return the final set of environments
		return rowsList;
	}

	//TDM 7.2 - This function gets the Override Attributes supplied when the task was executed.
	public static Map<String, Object> fnGetTaskExecOverrideAttrs(Long taskId, Long taskExecutionId) {
		
		Map<String, Object> overrideAttrubtes = new HashMap<>();
		String sql = "SELECT override_parameters FROM " + TDMDB_SCHEMA + ".task_execution_override_attrs WHERE task_id = ? and task_execution_id = ?";
		//log.info("getTaskExecOverrideAttrs - Starting");
		try {
			Object overrideAttrVal = db(TDM).fetch(sql, taskId, taskExecutionId).firstValue();
			String overrideAttrStr = overrideAttrVal != null ?  overrideAttrVal.toString() : "";
			//log.info("getTaskExecOverrideAttrs - overrideAttrStr: " + overrideAttrStr);
			if (!"".equals(overrideAttrStr)) {
				// Replace gson with K2view Json
				//Gson gson = new Gson();
				//overrideAttrubtes = gson.fromJson(overrideAttrStr, mapType);
				overrideAttrubtes = Json.get().fromJson(overrideAttrStr);
			}
		} catch (SQLException e) {
			UserCode.log.error("Failed to get override attributes for task_execution_id: " + taskExecutionId);
			return null;
		}
		
		//log.info("getTaskExecOverrideAttrs - overrideAttrubtes: " + overrideAttrubtes);
		return overrideAttrubtes;
	}
	
	static public List<Map<String, Object>> fnGetUserEnvs(String userName) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String errorCode = "";
		String message = null;
		List<Map<String, Object>> rowsList = new ArrayList<>();
		String userId = sessionUser().name();
		//log.info("wsGetListOfEnvsByUser after defining userId");
		//Check the permission group of the user.
		//If the permission group is Admin => select all the active environments
        
		String permissionGroup = fnGetUserPermissionGroup(userName);

		if ("admin".equalsIgnoreCase(permissionGroup)){
			String allEnvs = "Select env.environment_id,env.environment_name,\n" +
					"  Case When env.allow_read = True And env.allow_write = True Then 'BOTH'\n" +
					"    When env.allow_write = True Then 'TARGET' Else 'SOURCE'\n" +
					"  End As environment_type,\n" +
					"  'admin' As role_id,\n" +
					"  'admin' As assignment_type\n" +
					"From " + TDMDB_SCHEMA + ".environments env\n" +
					"Where env.environment_status = 'Active'";
			Db.Rows rows= db(TDM).fetch(allEnvs);
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				rowsList.add(rowMap);
			}
			
			if (rows != null) {
				rows.close();
			}
	
		} else {
            if("TDM.tdmTaskScheduler".equalsIgnoreCase(userId)){
                userId = userName;
            }
			rowsList.addAll(fnGetEnvsByUser(userId, null));
		}
	
		List<Map<String, Object>> result = new ArrayList<>();
		List<Map<String, Object>> sourceEnvs = new ArrayList<>();
		List<Map<String, Object>> targetEnvs = new ArrayList<>();
		for(Map<String, Object> row:rowsList){
			Map<String, Object> envData=new HashMap<>();
			envData.put("environment_id",row.get("environment_id"));
			envData.put("environment_name",row.get("environment_name"));
			envData.put("role_id",row.get("role_id"));
			envData.put("assignment_type",row.get("assignment_type"));
	
			if("SOURCE".equals(row.get("environment_type"))||"BOTH".equals(row.get("environment_type"))){
				sourceEnvs.add(envData);
			}
			if("TARGET".equals(row.get("environment_type"))||"BOTH".equals(row.get("environment_type"))){
				targetEnvs.add(envData);
			}
		}
	
		Map<String, Object> sourceEnvsMap=new HashMap<>();
		sourceEnvsMap.put("source environments",sourceEnvs);
		result.add(sourceEnvsMap);
		Map<String, Object> targetEnvsMap=new HashMap<>();
		targetEnvsMap.put("target environments",targetEnvs);
		result.add(targetEnvsMap);

		return result;
	}

	// @out(name = "result", type = JSONArray.class, desc = "")
	// public static JSONArray createJsonArrayFromTableRecords(String taskExecID, String tableName, ResultSet refTableRS, int colsCount, int recordsCount) throws SQLException {
    //     JSONArray tableRecords = new JSONArray();
    //     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    //     int processedCounter = 0;
    //     int updateStatsSize = Integer.parseInt(TDM_REF_UPD_SIZE);
    //     AtomicInteger counter = new AtomicInteger(0);
    //     AtomicBoolean failed = new AtomicBoolean(false);
    //     fabric().execute("set TDM_TASK_EXE_ID = " + taskExecID);
    //     int tdm_rec_id = 1;
    //     while (refTableRS.next()) {
    //         if (failed.get()) {
    //             throw new RuntimeException("failed to insert... ");
    //         }

    //         try {
    //             // build a JSON object with the record's columns + values
    //             String JSONObject;
    //             Map<String, Object> dataRec = new LinkedHashMap<String, Object>();

    //             for (int j = 1; j <= colsCount; j++) {
    //                 //log.info("type: " + refTableRS.getMetaData().getColumnTypeName(j).toLowerCase());
    //                 Object fieldVal =  typeCheck(refTableRS.getObject(j));
    //                 String colName = refTableRS.getMetaData().getColumnName(j);
    //                 dataRec.put(colName, fieldVal);
    //             }

    //             JSONObject = Json.get(Json.Feature.SERIALIZE_NULLS).toJson(dataRec);
    //             tableRecords.put(JSONObject);

    //         }
    //         catch(Exception e) {
    //             throw new RuntimeException(e.getMessage());
    //         }
    //         processedCounter++;
    //         if(processedCounter%updateStatsSize == 0) {
    //             db(TDM).execute("Update " + TASK_REF_EXE_STATS + " set number_of_processed_records = ?, number_of_records_to_process = ? " +
    //                    "where task_execution_id = ? and trim(lower(ref_table_name)) = ?; ", processedCounter, recordsCount, taskExecID, tableName.toLowerCase());
    //         }
    //     }

    //     db(TDM).execute("Update " + TASK_REF_EXE_STATS + " set number_of_processed_records = ?, number_of_records_to_process = ? " +
    //             "where task_execution_id = ? and trim(lower(ref_table_name)) = ?; ", processedCounter, recordsCount, taskExecID, tableName.toLowerCase());
    //     return tableRecords;
    // }

    private static boolean isCommonlyUsedType(Object val) {
        // Cover all cases of Integer, Decimal, Double, Float etc...
        if (val instanceof Number) {
            return true;
        }
        if (val instanceof String) {
            return true;
        }
        return false;
    }

    public static Object typeCheck(Object val) throws Exception {

        try {

            if (val instanceof Utils.NullType || val == null) {
                return null;
            }

            // Check first for the commonly used types to save all other 'instanceof'.
            if (isCommonlyUsedType(val)) {
                Class cls = val.getClass();
                //log.info("val instanceof " + cls.getName());
                if (val instanceof java.math.BigDecimal){
                    return  ((BigDecimal) val).doubleValue();
                }else{
                    return val;
                }
            }

            if (val instanceof K2TimestampWithTimeZone) {
                //log.info("val " + val + "instanceof K2TimestampWithTimeZone ");
                return ((Date) val).toString();
            }

            if (val instanceof java.sql.Timestamp) {
                //return ((Date) val).getTime();
                return ((Date) val).toString();
            }

            if (val instanceof java.sql.Date) {
                return ((Date) val).toString();
            }

            if (val instanceof java.sql.Time) {
                return ((Date) val).toString();
            }

            if (val instanceof java.util.Date) {
                //log.info("val " + val + " instanceof java.util.Date");
                return ((Date) val).toString();
            }

            if (val instanceof Blob) {
                //return ((Blob) val).getBytes(1, (int) ((Blob) val).length());
                return ((ByteBuffer)val).array();
            }

            if (val instanceof Clob) {
                return Utils.clobToString(((Clob) val));
            }

            if (val instanceof ByteBuffer){
                return ((ByteBuffer)val).array();
            }

            return val;
        } catch (Exception e) {
            UserCode.log.warn(e);
            return null;
        }
    }

	@desc("Get Resource File of LU")
	@out(name = "result", type = Object.class, desc = "")
	public static Object loadFromLUResource(String path) throws Exception {
		return loadResource(path);
	}
    @out(name = "result", type = Map.class, desc = "")
    public static Map<String,Object> getDbTableColumnsAndTypes(String dbInterfaceName, String catalogSchema, String table) throws Exception {
        ResultSet rs = null;
        ResultSet rs1 = null;
        String[] types = {"TABLE"};
        String targetTableName = table;
        Map<String, Object> tableData = new LinkedHashMap<String, Object>();

        try {
            DatabaseMetaData md = getConnection(dbInterfaceName).getMetaData();
            String[] dbSchemaType = getDBCollection(md, catalogSchema);
            String catalog = dbSchemaType[0];
            String schema = dbSchemaType[1];
            rs = md.getTables(catalog, schema, "%", types);
            while (rs.next()) {
                if (table.equalsIgnoreCase(rs.getString(3))) {
                    targetTableName = rs.getString(3);
                    break;
                }
            }
            rs1 = md.getColumns(catalog, schema, targetTableName, null);
            while (rs1.next()){
                tableData.put(rs1.getString("COLUMN_NAME"), rs1.getString("TYPE_NAME"));
            }
            return tableData;

        } finally {
            if (rs != null)
                rs.close();
            if (rs1 != null)
                rs1.close();
        }
    }
	
	@desc("Get the tables of give LU without TDM Tables add to LU for TDM mechanisms")
	@out(name = "result", type = List.class, desc = "")
	public static List<String> getLuTablesList(String luName) throws Exception {
		List<String> tablesList = new ArrayList<>();
		
		LUType luType = LUType.getTypeByName(luName);
		
		for (String tableName : luType.ludbTables.keySet()) {
		    Db.Rows checkTable = fabric().fetch("broadway " + luType.luName + ".filterOutTDMTables tableName='" +
		        tableName + "', luName=" + luType.luName + ", RESULT_STRUCTURE=ROW");
		
		    if (checkTable != null && checkTable.firstValue() != null) {
		        tablesList.add(tableName);
		    }
			
			if (checkTable != null) {
				checkTable.close();
			}
		
		}
		
		
		return tablesList;
	}
    
    @out(name="result", type = Boolean.class, desc = "")
    public static Boolean fnIsJSONValid(String jsonInString) {
        try {
            JSONObject jsonObjOne = new JSONObject(jsonInString);
            return true;
        } catch(Exception ex) { 
            return false;
        }
  }

	public static Long fnGetReservedEntitiesNumber (String envId,String beId,String userId) throws SQLException {
		try {
			String getUserReserveCnt_sql = "select count(1) from " + TDMDB_SCHEMA + ".tdm_reserved_entities where env_id = ? and be_id = ? and reserve_owner = ? and " +
					"end_datetime > CURRENT_TIMESTAMP";
			Long entCount = (Long) UserCode.db(TDM).fetch(getUserReserveCnt_sql, envId, beId, userId).firstValue();
			return entCount;
		}catch (Exception e){
			throw new RuntimeException(e);
		}
	}

    public static void deleteFile(String fileName)
    {
        File file = new File(fileName);
        if (file.isDirectory()) {
            deleteDirectory(file);
            file.delete();
        } else {
            file.delete();
        }
    }
    private static void deleteDirectory(File file)
    {
        // store all the paths of files and folders present
        // inside directory
        for (File subfile : file.listFiles()) {
            // if it is a subfolder,e.g Rohan and Ritik,
            //  recursively call function to empty subfolder
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }
            // delete files and empty subfolders
            subfile.delete();
        }
    }


    public static List<HashMap<String, String>> fnGetTableFields(String dbInterfaceName, String schemaName, String tableName, String catalogSchema) throws Exception {

		tableName = tableName.replaceAll("^\"|\"$", "");
		Map<String,Object> interfaceInput = new HashMap<>();
        interfaceInput.put("dataPlatform", dbInterfaceName);
        interfaceInput.put("schema", catalogSchema);
        interfaceInput.put("dataset", tableName);

        List<Map<String, Object>> interfaceTables =  MtableLookup("catalog_field_info",interfaceInput, MTable.Feature.caseInsensitive);
		if (interfaceTables == null  || interfaceTables.isEmpty()) {
            return getTableFieldsByJDBC(dbInterfaceName, schemaName, tableName);
        } else {
            return getTableFieldsByCatalog(interfaceTables);
        }

    }

	private static List<HashMap<String, String>> getTableFieldsByJDBC(String dbInterfaceName, String schemaName,
		String tableName) throws SQLException {
	List<HashMap<String, String>> result = new ArrayList<>();

	DatabaseMetaData metaData = getConnection(dbInterfaceName).getMetaData();
	ResultSet columns = metaData.getColumns(null, schemaName, tableName, null);

	while (columns.next()) {
		HashMap<String, String> map = new HashMap<>();

		map.put("column_name", columns.getString("COLUMN_NAME"));
		int dataType = columns.getInt("DATA_TYPE");
		String typeName = columns.getString("TYPE_NAME");
		String columnType = toSqliteType(dataType, typeName);

		String columnName = columns.getString("COLUMN_NAME");
		
		if (fnCheckSpecialChars(columnName)) {
			columnName = "\"" + columnName + "\"";
		}
		map.put("column_name", columnName);
		map.put("column_type", columnType);
		map.put("column_sqlite_type", columnType);
		result.add(map);

	}

	if (columns != null) {
		columns.close();
	}

	return result;
}

   private static List<HashMap<String, String>> getTableFieldsByCatalog(List<Map<String, Object>> interfaceTables) throws Exception {
	List<HashMap<String, String>> result = new ArrayList<>();

	for (Map<String, Object> fieldRec : interfaceTables) {
		HashMap<String, String> map = new HashMap<>();
		String fieldName = fieldRec.get("field").toString();

		String columnType = "TEXT";
		Boolean addField = true;
		
		Object sqlDataType = fieldRec.get("sqlDataType");
		if (sqlDataType != null) {
			int fieldDataType = Integer.parseInt(sqlDataType.toString());
			String sourceDataTypeStr = null;
			Object sourceDataType = fieldRec.get("sourceDataType");
			if (sourceDataType != null) {
				sourceDataTypeStr = sourceDataType.toString();
			}
			columnType = toSqliteType(fieldDataType, sourceDataTypeStr);
		} else {
			columnType = getFieldTypeBydefinedBy(fieldRec);
			if ("Complex field".equals(columnType)) {
				addField = false;
			}
		}

		if (addField) {
			if (fnCheckSpecialChars(fieldName)) {
				fieldName = "\"" + fieldName + "\"";
			}

			map.put("column_name", fieldName);
			map.put("column_type", columnType);
			map.put("column_sqlite_type", columnType);
			result.add(map);
		}
	}

	return result;
}

	public static boolean isSqliteKeyword(String name) {
    	return name != null && SQLITE_KEYWORDS.contains(name.toLowerCase());
	}
	
	public static boolean fnCheckSpecialChars(String input) {
		if (input == null || input.isEmpty()) {
			return false;
		}

		//Check if already quoted
		String quoteChar = "\"";
		if (input.startsWith(quoteChar) && input.endsWith(quoteChar)) return false;
		
		//Check if the field name is reserved word in Sqlite
		if (isSqliteKeyword(input)) {
			return true;
		}

		//Check if field name starts with a digit
		if (input.matches("^[0-9].*")) {
			return true;
		};

		// Check if the field name includes special characters
		for (char c : input.toCharArray()) {
			if (!Character.isLetterOrDigit(c) && c != '_') {
				return true;
			}
		}

		return false;
	}

	private static String getFieldTypeBydefinedBy(Map<String, Object> fieldRec) throws Exception{
		
		String fieldClass = fieldRec.get("class").toString();
		String dataset = fieldRec.get("dataset").toString();

		if (!dataset.equals(fieldClass)) {
			return "Complex field";
		}
		
		String definedBy = fieldRec.get("definedBy").toString();
		String fieldType = "";
		switch (definedBy) {
			case "STRING":
				fieldType = "TEXT";
				break;
			case "BYTES":
				fieldType = "BLOB";
				break;
			case "BOOLEAN":
			fieldType = "INTEGER";
				break;
			case "COLLECTION":
				fieldType = "TEXT";
					break;
			case "UNKNOWN":
				fieldType = "TEXT";
				break;
			default:
				fieldType = definedBy;
				break;
		}

		return fieldType;
	}

    private record LuTable(String luName, String luTable) {
        @Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			LuTable luTable1 = (LuTable) o;
			return Objects.equals(luName, luTable1.luName) && Objects.equals(luTable, luTable1.luTable);
		}

		@Override
		public int hashCode() {
			return Objects.hash(luName, luTable);
		} 


		public String toString() {
			return luName + "." + luTable;
        }
	}
    
    public static HashMap<String, String> fnGetSeparators() throws Exception{
        if (tdmSeparators == null || tdmSeparators.size() == 0) {
            tdmSeparators.put("IID_SEPARATOR", "_");
            tdmSeparators.put("IID_OPEN_SEPARATOR", null);
            tdmSeparators.put("IID_CLOSE_SEPARATOR", null);
            String sql = "SELECT param_name, param_value FROM " + TDMDB_SCHEMA + ".tdm_general_parameters WHERE " +
					"UPPER(param_name) in ('IID_SEPARATOR', 'IID_OPEN_SEPARATOR', 'IID_CLOSE_SEPARATOR')";

            Db.Rows rows = db(TDM).fetch(sql);

            for (Db.Row row : rows) {
                switch (row.get("param_name").toString().toUpperCase()) {
                    case "IID_SEPARATOR":
                        tdmSeparators.put("IID_SEPARATOR", row.get("param_value").toString());
                        break;
                    case "IID_OPEN_SEPARATOR":
                        tdmSeparators.put("IID_OPEN_SEPARATOR", row.get("param_value").toString());
                        break;
                    case "IID_CLOSE_SEPARATOR":
                        tdmSeparators.put("IID_CLOSE_SEPARATOR", row.get("param_value").toString());
                        break;
                    default:
                        break;
                }
            }
        }
        
        return tdmSeparators;
    }

    public static String fnGetTaskExecutionMode(String executionMode, String taskAction, Long beId, Boolean cloneInd) throws Exception {
		String result = "HORIZONTAL";

        if ("HORIZONTAL".equalsIgnoreCase(executionMode)) {
            return result;
        }
    
        if (cloneInd || beId < 0 ||
            (!"extract".equalsIgnoreCase(taskAction) && !"load".equalsIgnoreCase(taskAction) && !"delete".equalsIgnoreCase(taskAction))) {
            return result;
        }
        if ("INHERITED".equalsIgnoreCase(executionMode)) {
            executionMode = db(TDM).fetch("select execution_mode from " + TDMDB_SCHEMA + ".business_entities where be_id = ?", beId).firstValue().toString();
        }
    
		return executionMode;
	}

	public static void fnGetEvaluationReport(String evaluationExeID, String savedFilePath, String AI_Interface, String k2systemSchema) throws Exception {
		extractZipColumnToFiles(
			evaluationExeID,
			savedFilePath,
			AI_Interface,
			k2systemSchema,
			"evaluation_report",
			"EVALUATION",
			"No evaluation report found for ID: "
		);
	}

	public static void fnGetArtifactsFiles(String taskExeID, String savedFilePath, String AI_Interface, String k2systemSchema, String taskType) throws Exception {
		extractZipColumnToFiles(
			taskExeID,
			savedFilePath,
			AI_Interface,
			k2systemSchema,
			"artifacts",
			taskType,
			"No artifacts found for ID: "
		);
	}
		
	private static void extractZipColumnToFiles(String taskExeID,String savedFilePath,String AI_Interface,String k2systemSchema,String columnName,String taskType,String notFoundMessage) throws Exception {

		String sql = "SELECT " + columnName +
					" FROM " + k2systemSchema +
					".task_executions WHERE id = ? AND task_type = ?";

		try (Connection conn = getConnection(AI_Interface);
			PreparedStatement query = conn.prepareStatement(sql)) {

			query.setString(1, taskExeID);
			query.setString(2, taskType);

			try (ResultSet resultSet = query.executeQuery()) {
				if (!resultSet.next()) {
					UserCode.log.error(notFoundMessage + taskExeID);
					return;
				}

				byte[] zipBytes = resultSet.getBytes(columnName);
				if (zipBytes == null || zipBytes.length == 0) {
					UserCode.log.error("Column '" + columnName + "' is empty for ID: " + taskExeID);
					return;
				}

				unzipToDirectory(zipBytes, savedFilePath);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	private static void unzipToDirectory(byte[] zipBytes, String savedFilePath) throws IOException {

		Path targetDir = Paths.get(savedFilePath);

		try (ZipInputStream zipInputStream =
				new ZipInputStream(new ByteArrayInputStream(zipBytes))) {

			ZipEntry entry;

			while ((entry = zipInputStream.getNextEntry()) != null) {

				Path resolvedPath = targetDir.resolve(entry.getName()).normalize();

				// Prevent Zip Slip
				if (!resolvedPath.startsWith(targetDir)) {
					throw new IOException("Bad zip entry: " + entry.getName());
				}

				if (entry.isDirectory()) {
					Files.createDirectories(resolvedPath);
				} else {
					Files.createDirectories(resolvedPath.getParent());
					Files.copy(zipInputStream, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
				}

				zipInputStream.closeEntry();
			}
		}
	}	
	
	public static Set<String> getAllSuppressedInterfaces() throws Exception {
		Set<String> suppressedSet = new HashSet<>();

		Map<String, Object> input = new HashMap<>();
		input.put("suppress_indicator", "true");

		List<Map<String, Object>> rows = MtableLookup("TableLevelInterfaces", input, MTable.Feature.caseInsensitive);

		if (rows != null) {
			for (Map<String, Object> row : rows) {
				Object name = row.get("interface_name");
				if (name != null) {
					suppressedSet.add(String.valueOf(name));
				}
			}
		}

		return suppressedSet;
	}

	public static String findMaxNumOfWorkers(String maxWorkersFromTask) {

		String maxWorkers;

		// 1. Check for value from the task (maxWorkersFromTask)
		if (!Util.isEmpty(maxWorkersFromTask)) {
			maxWorkers = maxWorkersFromTask;
		} else {
			// 2. Query the database if task value is empty

			String sql = "SELECT param_value FROM " + TDMDB_SCHEMA +
					".tdm_general_parameters WHERE param_name = 'MAX_NO_OF_WORKERS_FOR_EXECUTION'";
			try {
				Object maxWorkersConfig = db(TDM).fetch(sql).firstValue();

				if (maxWorkersConfig != null) {
					String dbValue = (String) maxWorkersConfig;

					// If the value from the database is "-1", treat it as empty/null.
					if ("-1".equals(dbValue.trim())) {
						maxWorkers = null; // Treat as empty
					} else {
						maxWorkers = dbValue;
					}

				} else {
					// If not found in DB at all, treat as empty
					maxWorkers = null;
				}

			} catch (SQLException e) {
				throw new RuntimeException(
						"Failed to retrieve MAX_NO_OF_WORKERS_FOR_EXECUTION from tdm_general_parameters.", e);
			}
		}

		if (!Util.isEmpty(maxWorkers)) {
			try {
				int max = Integer.parseInt(maxWorkers.trim());
				return " MAX_WORKERS_PER_NODE=" + max;
			} catch (NumberFormatException e) {
				return "";
			}
		}

		return "";
	}

	public static String getAffinityString(String affinity) {
		if (!Util.isEmpty(affinity)) {
			return "affinity='" + affinity + "'";
		}
		return "";
	}

	@desc("""
			Retrieve all products associated with the given environment ID, including task counts and version information.
			@param envId Environment identifier.
			@return A map containing 'errorCode', optional 'message', and 'result' with a list of product records or null if the environment does not exist.
			""")
	public static Object getProductsForEnvironment(Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";

		try {
			// getEnv
			String getEnvironmentSql = "SELECT * FROM " + schema + ".environments WHERE environment_id = ?";
			Db.Row env = db(TDM).fetch(getEnvironmentSql, envId).firstRow();

			if (env.isEmpty()) {
				response.put("errorCode", "SUCCESS");
				response.put("result", null);
				return response;
			}

			// getEnvProduct
			String sql = "SELECT * , " +
					"( SELECT COUNT(*) FROM " + schema + ".tasks " +
					"INNER JOIN " + schema + ".tasks_logical_units ON (tasks_logical_units.task_id = tasks.task_id) " +
					"INNER JOIN " + schema
					+ ".product_logical_units ON (product_logical_units.lu_id = tasks_logical_units.lu_id) " +
					"WHERE product_logical_units.product_id = products.product_id AND tasks.task_status = 'Active' ) AS taskcount "
					+
					"FROM " + schema + ".environment_products " +
					"INNER JOIN " + schema + ".products ON (environment_products.product_id = products.product_id) " +
					"WHERE environment_products.environment_id = ?";

			// 3. Use try-with-resources to ensure 'rows' is closed automatically
			try (Db.Rows rows = db(TDM).fetch(sql, envId)) {
				List<HashMap<String, Object>> products = new ArrayList<>();
				List<String> columnNames = rows.getColumnNames();

				for (Db.Row row : rows) {
					HashMap<String, Object> productData = new HashMap<>();
					ResultSet resultset = row.resultSet();
					for (String columnName : columnNames) {
						productData.put(columnName, resultset.getObject(columnName));
					}

					if (envId < 0) {
						productData.put("product_versions", productData.get("product_version"));
					}
					products.add(productData);
				}
				response.put("result", products);
			}

			errorCode = "SUCCESS";

		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			UserCode.log.error(message);
		}

		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	@desc("""
			Retrieve interface execution configuration (max_number_of_workers and affinity) for a specific interface in a given environment.
			@param envId Environment identifier for which to fetch the configuration.
			@param interfaceName Logical interface name whose configuration is requested.
			@return Map containing errorCode, message and result (with keys max_number_of_workers and affinity).
			""")
	public static Object getInterfaceConfig(Long envId, String interfaceName) {
		HashMap<String, Object> response = new HashMap<>();
		// Initialize result with nulls as the default "not found" state
		HashMap<String, Object> result = new HashMap<>();
		result.put("max_number_of_workers", null);
		result.put("affinity", null);

		String message = null;
		String errorCode = "SUCCESS";

		try {
			Object productsResponse = getProductsForEnvironment(envId);

			if (productsResponse instanceof Map) {
				Map<String, Object> resMap = (Map<String, Object>) productsResponse;

				if ("SUCCESS".equals(resMap.get("errorCode"))) {
					List<Map<String, Object>> productsList = (List<Map<String, Object>>) resMap.get("result");

					if (productsList != null) {
						for (Map<String, Object> product : productsList) {
							Object rawInterfaces = product.get("related_interfaces");
							List<String> interfaces = new ArrayList<>();

							if (rawInterfaces instanceof java.sql.Array) {
								String[] interfaceArray = (String[]) ((java.sql.Array) rawInterfaces).getArray();
								interfaces = java.util.Arrays.asList(interfaceArray);
							} else if (rawInterfaces instanceof List) {
								interfaces = (List<String>) rawInterfaces;
							}

							// If found, overwrite the nulls with actual data
							if (interfaces.contains(interfaceName)) {
								result.put("max_number_of_workers", product.get("max_number_of_workers"));
								result.put("affinity", product.get("data_center_name"));
								break;
							}
						}
					}
				} else {
					errorCode = "FAILED";
					message = "Error calling products service: " + resMap.get("message");
				}
			}

			response.put("result", result);

		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			UserCode.log.error("Failed to find interface config: " + message);
		}

		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	public static List<Map<String, Object>> fnGetTables(String dbInterfaceName, String schemaName, String tablePattern, String catalogSchema) throws Exception {

		Map<String, Object> interfaceInput = new HashMap<>();
		interfaceInput.put("dataPlatform", dbInterfaceName);
		interfaceInput.put("schema", catalogSchema);

		// If your catalog supports filtering by dataset/table, pass pattern.
		// If it doesn't, remove this line and filter in Java.
		interfaceInput.put("dataset", tablePattern);

		List<Map<String, Object>> interfaceTables =
				MtableLookup("catalog_table_info", interfaceInput, MTable.Feature.caseInsensitive);

		if (interfaceTables == null || interfaceTables.isEmpty()) {
			return getTablesByJDBC(dbInterfaceName, schemaName, tablePattern);
		} else {
			return interfaceTables;
		}
	}

	private static List<Map<String, Object>> getTablesByJDBC(String dbInterfaceName, String schemaName, String tablePattern ) throws SQLException {

		List<Map<String, Object>> result = new ArrayList<>();

		DatabaseMetaData metaData = getConnection(dbInterfaceName).getMetaData();

		String schemaPattern = schemaName;
		String tblPattern = (tablePattern == null || tablePattern.isEmpty()) ? "%" : tablePattern;

		if (schemaPattern != null) {
			if (metaData.storesUpperCaseIdentifiers()) schemaPattern = schemaPattern.toUpperCase();
			else if (metaData.storesLowerCaseIdentifiers()) schemaPattern = schemaPattern.toLowerCase();
		}

		String[] types = new String[] { "TABLE" }; 

		try (ResultSet tables = metaData.getTables(null, schemaPattern, tblPattern, types)) {
			while (tables.next()) {
				Map<String, Object> map = new HashMap<>();
				map.put("table_name", tables.getObject("TABLE_NAME"));
				result.add(map);
			}
		}

		if (result.isEmpty() && schemaPattern != null) {
			try (ResultSet tables = metaData.getTables(schemaPattern, null, tblPattern, types)) {
				while (tables.next()) {
					Map<String, Object> map = new HashMap<>();
					map.put("table_name", tables.getObject("TABLE_NAME"));
					result.add(map);
				}
			}
		}

		return result;
	}

	public static int getGlobalMaxWorkersLimit() throws Exception {
		Object globalValueObj = null;
		Db.Rows configs = fabric().fetch("list config;");

		for (Db.Row config : configs) {
			if ("MAX_WORKERS_PER_NODE".equals(config.get("Key"))) {
				globalValueObj = config.get("Value");
				break;
			}
		}

		if (globalValueObj == null) {
			throw new Exception("Global config MAX_WORKERS_PER_NODE not found");
		}

		return Integer.parseInt(globalValueObj.toString());
	}

	public static Object fnGetLogicalUnitsByEnvironmentAndBusinessentity(Long beId, Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		try {
			String sql = "SELECT * FROM " + TDMDB_SCHEMA + ".product_logical_units lu " + "INNER JOIN " + TDMDB_SCHEMA
					+ ".products p " +
					"ON (lu.product_id = p.product_id) " + "INNER JOIN " + TDMDB_SCHEMA + ".environment_products ep " +
					"ON (lu.product_id = ep.product_id " + "AND ep.status = \'Active\') " + "WHERE be_id = " + beId +
					" AND environment_id = " + envId + " AND ep.enable_product=true";
			Db.Rows rows = db(TDM).fetch(sql);
			List<HashMap<String, Object>> result = new ArrayList<>();

			HashMap<String, Object> lU;
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				lU = new HashMap<>();
				lU.put("lu_id", resultSet.getInt("lu_id"));
				lU.put("lu_parent_name", resultSet.getString("lu_parent_name"));
				lU.put("lu_parent_id",
						row.get("lu_parent_id") != null ? Long.parseLong(row.get("lu_parent_id").toString()) : null);
				lU.put("lu_name", resultSet.getString("lu_name"));
				lU.put("product_name", resultSet.getString("product_name"));
				lU.put("env_max_number_of_workers", resultSet.getString("max_number_of_workers"));
				lU.put("env_affinity", resultSet.getString("data_center_name"));
				result.add(lU);
			}

			errorCode = "SUCCESS";
			response.put("result", result);
			if (rows != null) {
				rows.close();
			}

		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			UserCode.log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

}