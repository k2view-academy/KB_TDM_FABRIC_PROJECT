package com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils;

import static com.k2view.cdbms.shared.user.UserCode.db;
import static com.k2view.cdbms.shared.user.UserCode.fabric;
import static com.k2view.cdbms.shared.user.UserCode.getConnection;
import static com.k2view.cdbms.shared.user.UserCode.getGlobal;
import static com.k2view.cdbms.shared.user.UserCode.loadResource;
import static com.k2view.cdbms.shared.user.UserCode.sessionUser;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.AI_ENVIRONMENT;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_PARAMETERS_SEPARATOR;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.MtableLookup;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.getTDMDBSchema;
import static com.k2view.cdbms.usercode.common.TDM.TemplateUtils.SharedLogic.getDBCollection;
import static com.k2view.cdbms.usercode.common.TDM.TemplateUtils.SharedLogic.toSqliteType;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.MtableGetKeyValues;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.k2view.cdbms.usercode.common.TDM.TDMMaskingReportLogic;
import com.k2view.cdbms.lut.LUType;
import com.k2view.cdbms.lut.LudbRelationInfo;
import com.k2view.cdbms.lut.TableObject;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.LUTypeFactoryImpl;
import com.k2view.cdbms.shared.Utils;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.out;
import com.k2view.cdbms.utils.K2TimestampWithTimeZone;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.mtable.MTable;
import com.k2view.fabric.common.mtable.MTables;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked", "rawtypes"})
public class SharedLogic {
    public static final String COMPLEX_FIELD_TYPE = "COMPLEX";

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

    private static final String PENDING = "pending";
    private static final String RUNNING = "running";
    private static final String WAITING = "waiting";
    private static final String STOPPED = "stopped";
    private static final String RESUME = "resume";
    private static final String FAILED = "failed";
    private static final String COMPLETED = "completed";
	public static final String MAP_LU_NAME = "lu_name";
	public static final String MAP_LU_TABLE = "lu_table";
	public static final String MAP_LU_TABLE_FIELD = "lu_table_field";
	public static final String MAP_PARAM_NAME = "param_name";
	public static boolean inTest = false;
	public static Map<String, List<Map<String, Object>>> allTables = new HashMap<>();
	private static HashMap<String, String> luShortMap = new HashMap<>();
    private static HashMap<String, String> tdmSeparators = new HashMap<>();

	public enum PermissionErrorMsg {
		TASK_MODIFY_NOT_ALLOWED("Only an administrator or the task creator can modify this task."),
		TASK_CREATE_NOT_ALLOWED("The user is not allowed to create tasks."),
		TASK_EXECUTION_PROCESSES_NOT_ALLOWED("Only an administrator or the task creator can add execution processes to this task."),
		TASKS_GET_NOT_AUTHORIZED("The user is not authorized to get the tasks."),
		PERMISSION_GROUP_NOT_FOUND("Can't find a permission group for the user");

		private final String msg;

		PermissionErrorMsg(String msg) {
			this.msg = msg;
		}

		public String getMsg() {
			return msg;
		}
	}

	public enum OverrideParamKey {
		BE_ID,
		BE_NAME,
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
		SELECTED_SUBSET_TASK_EXE_ID,
		DATAFLUX_RETENTION_PARAMS,
		RESERVE_RETENTION_PARAMS,
		EXECUTION_NOTE,
		TABLE_FILTERS,
		TABLE_SAMPLING,
		PRE_EXECUTION_PROCESSES_PARAMS,
		POST_EXECUTION_PROCESSES_PARAMS,
		IMPLICIT_OVERRIDE_LOGICAL_UNITS
	}

	public static final Set<OverrideParamKey> INTERNAL_ONLY_OVERRIDE_KEYS = Collections.unmodifiableSet(
		EnumSet.of(
			OverrideParamKey.BE_NAME,
			OverrideParamKey.IMPLICIT_OVERRIDE_LOGICAL_UNITS
		)
	);

	/**
	 * Reduces a GENERATE_DATA_PARAMS map to its "light" persisted shape: for each field, keeps
	 * only the chosen value, stripping the rich editor/schema/default wrapper the GUI sends
	 * ({@code {"field": {"editor": {...}, "value": ...}}} -> {@code {"field": ...}}). Fields
	 * already in the light shape (no "value" sub-key) pass through unchanged.
	 */
	public static Map<String, Object> reduceGenerateDataParams(Map<String, Object> genData) {
		Map<String, Object> processed = new HashMap<>();
		if (genData == null) return processed;
		for (Map.Entry<String, Object> e : genData.entrySet()) {
			Object val = e.getValue();
			if (val instanceof Map && ((Map<?, ?>) val).containsKey("value")) {
				processed.put(e.getKey(), ((Map<?, ?>) val).get("value"));
			} else {
				processed.put(e.getKey(), val);
			}
		}
		return processed;
	}

    public static Object fnBatchStatistics(String i_batchId, String i_runMode) throws Exception {
        Object response;
        switch (i_runMode) {
            case "S":
                response = getFabricResponse("batch_summary ?", i_batchId);
                break;
            case "D":
                response = getFabricResponse("batch_details ?", i_batchId);
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

    public static Object getFabricResponse(String fabricCommand, Object... args) throws SQLException {
        List objects = new ArrayList();
        fabric().fetch(fabricCommand, args).forEach(row -> {
            Map rowMap = new HashMap<String, Object>();
            rowMap.putAll(row);
            objects.add(rowMap);
        });
        return objects;
    }

    // Hardening: these values are concatenated into a broadway/DSL command string that can't be
    // converted to a `?` bind (e.g. it's spliced into an outer command alongside the command's own
    // per-row `iid=?` placeholder, or it's an identifier). Sanitize at the point of concatenation instead.
    public static String sanitizeDslToken(String value) {
        return value == null ? null : value.replace("'", "").replace("\"", "");
    }

    public static String validateNumericId(String value) {
        return Long.toString(Long.parseLong(value));
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
			MatchQuery matchQuery = getListOfMatchingEntitiesQuery(beID, sourceEnv, res, analysisCount);
            Set <String> lus = matchQuery.lus;
            Object result = fabric().fetch("broadway TDM.CheckIfSchemasExists lus=?", lus).firstValue();
            if(result!=null){
                throw new RuntimeException(result.toString());
            }
            UserCode.log.info(matchQuery.sql);
			return matchQuery.sql;
		}
        String iidSeparator = "" + db(TDM).fetch("Select param_value from " + getTDMDBSchema() + ".tdm_general_parameters where LOWER(param_name) = 'iid_separator'").firstValue();
        //separator = !Util.isEmpty(iidSeparator) ? iidSeparator : "_";
        String separator = "_";
        if (!Util.isEmpty(iidSeparator) && !"null".equals(iidSeparator)) {
            separator = iidSeparator;
        }
		//UserCode.log.info("generateListOfMatchingEntitiesQuery - whereStmt: " + whereStmt);
		String rootLUsSql = "SELECT ARRAY_AGG(lu_name) FROM " + getTDMDBSchema() + ".product_logical_units WHERE " +
		    "be_id = ? AND lu_parent_id is null";
		
		String rootLUs = "" + db(TDM).fetch(rootLUsSql, beID).firstValue();
		
		String safeRootLUs = rootLUs.replace("'", "''");
		String safeSourceEnv = sourceEnv != null ? sourceEnv.replace("'", "''") : sourceEnv;
		String paramsSql = !Util.isEmpty(whereStmt) ? whereStmt + ")" : "";
		paramsSql = paramsSql.replaceAll("FROM " , "FROM " + getTDMDBSchema() + ".");
		paramsSql = paramsSql.replaceAll("WHERE ", "WHERE ROOT_LU_NAME = ANY('" + safeRootLUs + "') AND SOURCE_ENVIRONMENT = '" + safeSourceEnv + "' AND (");
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
		            "FROM " + getTDMDBSchema() + ".task_execution_entities " +
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
			String sql = "select * from " + getTDMDBSchema() + ".tdm_general_parameters where tdm_general_parameters.param_name = 'tdm_gui_params'";

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
			sql = "SELECT param_value from " + getTDMDBSchema() + ".tdm_general_parameters where param_name = 'MAX_RETENTION_DAYS_FOR_TESTER'";
			Object maxRetentionDays = db(TDM).fetch(sql).firstValue();
			if (maxRetentionDays != null) {
				Map<String, Object> testers = new HashMap<>();
				testers.put("units", "Days");
				testers.put("value",  Long.valueOf((String) maxRetentionDays));
				map.put("maxRetentionPeriodForTesters", testers);
			}
			sql = "SELECT param_value from " + getTDMDBSchema() + ".tdm_general_parameters where param_name = 'MAX_RESERVATION_DAYS_FOR_TESTER'";
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
	
    private static boolean isSessionWithoutUserRoles() {
        Set<String> roles = sessionUser() == null || sessionUser().roles() == null
                ? new HashSet<>() : new HashSet<>(sessionUser().roles());
        roles.remove("Everybody");
        return roles.isEmpty();
    }

    private static String sessionGlobal(String name) {
        try {
            Object value = getGlobal(name);
            return value == null ? "" : value.toString().trim();
        } catch (Throwable t) {
            return "";
        }
    }

    public static String fnGetSessionUserName() {
        String sessionName = sessionUser() == null ? "" : sessionUser().name();
        if (isSessionWithoutUserRoles()) {
            String globalUser = sessionGlobal("USER_NAME");
            if (!globalUser.isEmpty()) {
                UserCode.log.debug("fnGetSessionUserName - session user '" + sessionName
                        + "' has no roles, using USER_NAME global: '" + globalUser + "'");
                return globalUser;
            }
        }
        return sessionName;
    }

    public static String fnGetUserRoles(String userName) {
        try {
            String fabricRoles = "";
            if (userName == null || "".equals(userName) || userName.equalsIgnoreCase(sessionUser().name())
                    || userName.equalsIgnoreCase(fnGetSessionUserName())) {
                Set<String> roles = new HashSet<>(sessionUser().roles());
                roles.remove("Everybody");
                if (roles.isEmpty()) {
                    // Job session: take the roles the job applied for the submitting user.
                    String globalRoles = sessionGlobal("USER_FABRIC_ROLES");
                    if (!globalRoles.isEmpty()) {
                        roles.addAll(Arrays.asList(globalRoles.split(TDM_PARAMETERS_SEPARATOR)));
                        roles.remove("");
                    } else {
                        UserCode.log.warn("fnGetUserRoles - session user '" + sessionUser().name()
                                + "' has no Fabric roles and the USER_FABRIC_ROLES global is empty; the user will not be mapped to a TDM permission group");
                    }
                }
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
                    String command = "list users user_filter=?";
                    String found = "" + fabric().fetch(command, userName).firstRow().get("user");
                        if (userName.equals(found)) {
                            roles.add(found);
                        }
                    }
                fabricRoles = String.join(TDM_PARAMETERS_SEPARATOR, roles);
            }
            UserCode.log.info("fnGetUserRoles - user: '" + userName + "', session user: '" + sessionUser().name()
                    + "', roles: '" + fabricRoles + "'");
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

    public static boolean isKnownPermissionGroup(String permissionGroup) {
        return "admin".equals(permissionGroup) || "owner".equals(permissionGroup) || "tester".equals(permissionGroup);
    }

    public static String fnValidateEnvModifyPermission(Long envId, String permissionGroup, String testerDeniedMsg, String notOwnerMsg) throws Exception {
        if ("admin".equals(permissionGroup)) {
            return null;
        }
        if ("owner".equals(permissionGroup)) {
            return fnIsOwner(String.valueOf(envId)) ? null : notOwnerMsg;
        }
        if ("tester".equals(permissionGroup)) {
            return testerDeniedMsg;
        }
        return "Can't find a permission group for the user";
    }

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
				"From " + getTDMDBSchema() + ".environments env\n" +
				"Where env.environment_status = 'Active'";
			try (Db.Rows rows = Util.rte(() -> db(TDM).fetch(allEnvs))) {
				List<String> columnNames = rows.getColumnNames();
				for (Db.Row row : rows) {
					ResultSet resultSet = row.resultSet();
					Map<String, Object> rowMap = new HashMap<>();
					for (String columnName : columnNames) {
						Util.rte(() -> rowMap.put(columnName, resultSet.getObject(columnName)));
					}
					rowsList.add(rowMap);
				}
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
			UserCode.log.error("Can't find permission group for the user. roles: '" + roles + "'");
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
		String sql = "select permission_group from " + getTDMDBSchema() + ".permission_groups_mapping where fabric_role = ANY (string_to_array(?,?))";
		Util.rte(() -> db(TDM).fetch(sql, roles,TDM_PARAMETERS_SEPARATOR).forEach(row -> {
			Integer nextWeight = PERMISSION_GROUPS.get(row.get("permission_group"));
			if (nextWeight != null && nextWeight > weight[0]) {
				weight[0] = nextWeight;
			}
		}));
		return weight[0];
	}

	// The exclusion list below is rebuilt after each query in fnGetEnvsByUser from previously-fetched
	// environment_id values, then spliced into the next query's "not in (...)" clause. Build it as a
	// bind-variable placeholder list instead of a literal "(1,2,3)" string.
	private static List<Object> extractEnvIds(Set<Map<String, Object>> rowsList) {
		List<Object> envIds = new ArrayList<>();
		for (Map<String, Object> row : rowsList) {
			envIds.add(row.get("environment_id"));
		}
		return envIds;
	}

	private static String envIdExclusionClause(String columnRef, List<Object> envIds) {
		if (envIds.isEmpty()) {
			return "";
		}
		return " and " + columnRef + " not in (" + String.join(",", Collections.nCopies(envIds.size(), "?")) + ")";
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
			productFilterJoin = " JOIN " + getTDMDBSchema() + ".environment_products ep ON env.environment_id = ep.environment_id";
			productFilterWhere = " AND ep.status = 'Active' AND ep.enable_product = TRUE AND ep.product_id IN (" + productIdsString + ")";
		}
	
		//get the environments where the user is the owner
		String query1 = "select env.*, " +
			"CASE when env.allow_read = true and env.allow_write = true THEN 'BOTH' when env.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, 'owner' as role_id, 'owner' as assignment_type " +
			"from " + getTDMDBSchema() + ".environments env JOIN " + getTDMDBSchema() + ".environment_owners o on o.environment_id = env.environment_id" +
			productFilterJoin +
			" where (o.user_id = (?) or o.user_id = ANY(string_to_array(?, '" + TDM_PARAMETERS_SEPARATOR + "')))" +
			" and env.environment_status = 'Active'" +
			productFilterWhere;
	
		//log.info("fnGetEnvsByuser - query 1 for user Name " + userName + "is: " + query1);
		try (Db.Rows rows = db(TDM).fetch(query1, userName, fabricRoles)) {
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				rowsList.add(rowMap);
			}
		}
	
		List<Object> envIdParams = extractEnvIds(rowsList);

		//get the environments where the user is assigned to a role by their username
		String query2 = "select env.*, r.*, " +
			"CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' when r.allow_read = true THEN 'SOURCE' " +
			"when env.allow_read = true and env.allow_write = true THEN 'BOTH' when env.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, r.role_id, 'user' as assignment_type " +
			"from " + getTDMDBSchema() + ".environments env JOIN " + getTDMDBSchema() + ".environment_roles r on env.environment_id = r.environment_id JOIN " + getTDMDBSchema() + ".environment_role_users u on r.role_id = u.role_id " +
			productFilterJoin +
			" where lower(r.role_status) = 'active' " +
			" and u.user_id = (?) " +
			" and u.user_type = 'ID' " +
			" and env.environment_status = 'Active'";
		// remove the list of environments returned by query 1;
		query2 += envIdExclusionClause("env.environment_id", envIdParams);
		query2 += productFilterWhere;
		List<Object> query2Params = new ArrayList<>();
		query2Params.add(userName);
		query2Params.addAll(envIdParams);
		try (Db.Rows rows = db(TDM).fetch(query2, query2Params.toArray())) {
	
			//log.info("fnGetEnvsByuser - query 2 for user Name " + userName + "is: " + query2);
	
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				rowsList.add(rowMap);
			}
		}
	
		envIdParams = extractEnvIds(rowsList);

		//get the environments where the user id is one of the Fabric Roles
		String query3 = "select env.*, r.*, " +
			"CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' when r.allow_read = true THEN 'SOURCE' " +
			"when env.allow_read = true and env.allow_write = true THEN 'BOTH' when env.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, r.role_id, 'user' as assignment_type " +
			"from " + getTDMDBSchema() + ".environments env JOIN " + getTDMDBSchema() + ".environment_roles r on env.environment_id = r.environment_id JOIN " + getTDMDBSchema() + ".environment_role_users u on r.role_id = u.role_id " +
			productFilterJoin +
			" where lower(r.role_status) = 'active' " +
			" and u.user_id = ANY(string_to_array(?, '" + TDM_PARAMETERS_SEPARATOR + "')) " +
			" and u.user_type = 'GROUP' " +
			" and env.environment_status = 'Active'";
		// remove the list of environments returned by query 1+2;
		query3 += envIdExclusionClause("env.environment_id", envIdParams);
		query3 += productFilterWhere;
		List<Object> query3Params = new ArrayList<>();
		query3Params.add(fabricRoles);
		query3Params.addAll(envIdParams);
		try (Db.Rows rows = db(TDM).fetch(query3, query3Params.toArray())) {
	
			//log.info("fnGetEnvsByuser - query 3 for Fabric Roles < " + fabricRoles + "> is: " + query3);
	
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				rowsList.add(rowMap);
			}
		}
	
		envIdParams = extractEnvIds(rowsList);

		//get the environments where the user is assigned to a role by 'ALL' assignment
		String query4 = "select env.*, r.*, " +
			"CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' when r.allow_read = true THEN 'SOURCE' " +
			"when env.allow_read = true and env.allow_write = true THEN 'BOTH' when env.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type " +
			", r.role_id, 'all' as assignment_type " +
			"from " + getTDMDBSchema() + ".environments env JOIN " + getTDMDBSchema() + ".environment_roles r on env.environment_id = r.environment_id JOIN " + getTDMDBSchema() + ".environment_role_users u on r.role_id = u.role_id " +
			productFilterJoin +
			" where lower(r.role_status) = 'active' " +
			" and lower(u.username) = 'all' " +
			" and env.environment_status = 'Active'";
		// remove the list of environments returned by queries 1+2+3;
		query4 += envIdExclusionClause("env.environment_id", envIdParams);
		query4 += productFilterWhere;
		try (Db.Rows rows = db(TDM).fetch(query4, envIdParams.toArray())) {
	
			//log.info(" fnGetEnvsByuser - query 4 (get ALL roles) is: " + query4);
	
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				rowsList.add(rowMap);
			}
		}
		envIdParams = extractEnvIds(rowsList);

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
			"FROM " + getTDMDBSchema() + ".environments env " +
			productFilterJoin +
			" WHERE env.environment_status = 'Active' " +
			productFilterWhere +
			" ) " +
			"SELECT * FROM categorized_env " +
			"WHERE environment_type IN ('BOTH', 'SOURCE')";
		// remove the list of environments returned by queries 1+2+3+4;
		query5 += envIdExclusionClause("categorized_env.environment_id", envIdParams);
		try (Db.Rows rows = db(TDM).fetch(query5, envIdParams.toArray())) {
	
			// log.info("fnGetEnvsByuser - query 5 (get ALL Env) is: " + query5);
	
			List<String> columnNames = rows.getColumnNames();
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
		}
		// Return the final set of environments
		return rowsList;
	}

	//TDM 7.2 - This function gets the Override Attributes supplied when the task was executed.
	public static Map<String, Object> fnGetTaskExecOverrideAttrs(Long taskId, Long taskExecutionId) {
		
		Map<String, Object> overrideAttrubtes = new HashMap<>();
		String sql = "SELECT override_parameters FROM " + getTDMDBSchema() + ".task_execution_override_attrs WHERE task_id = ? and task_execution_id = ?";
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

	// Coerces override parameters parsed from task_execution_override_attrs.override_parameters JSON
	// (raw String-keyed map) back into an OverrideParamKey-keyed map with the types/shapes StartTask
	// and wsGetTaskDetailsWithOverrides expect (e.g. NO_OF_ENTITIES as Long, RESERVE_IND as Boolean,
	// nested objects as Map<String,String>). GENERATE_DATA_PARAMS is kept in its stored flat shape
	// here — callers that re-submit it to wsStartTaskV2 must re-wrap it themselves.
	public static Map<OverrideParamKey, Object> coerceStoredOverrideParams(Map<String, Object> storedParams) {
		Map<OverrideParamKey, Object> overrideParameters = new HashMap<>();
		if (storedParams == null)
			return overrideParameters;
		for (Map.Entry<String, Object> entry : storedParams.entrySet()) {
			try {
				OverrideParamKey key = OverrideParamKey.valueOf(entry.getKey());
				if (INTERNAL_ONLY_OVERRIDE_KEYS.contains(key))
					continue;
				Object value = entry.getValue();
				if (value == null)
					continue;
				if (key == OverrideParamKey.GENERATE_DATA_PARAMS && value instanceof Map) {
					// Preserve as-is: values can be complex objects (maps/lists), so
					// toString() would produce Java format instead of JSON
					overrideParameters.put(key, value);
				} else if (value instanceof Map) {
					// Nested objects (RESERVE_RETENTION_PARAMS, DATAFLUX_RETENTION_PARAMS) —
					// convert to Map<String, String>
					Map<String, String> stringMap = new HashMap<>();
					for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
						stringMap.put(e.getKey().toString(),
								e.getValue() != null ? e.getValue().toString() : null);
					}
					overrideParameters.put(key, stringMap);
				} else if (key == OverrideParamKey.NO_OF_ENTITIES) {
					overrideParameters.put(key, Long.parseLong(value.toString()));
				} else if (key == OverrideParamKey.RESERVE_IND) {
					overrideParameters.put(key, Boolean.parseBoolean(value.toString()));
				} else if (value instanceof List) {
					// PRE/POST_EXECUTION_PROCESSES_PARAMS, LOGICAL_UNITS, TABLE_FILTERS, TABLE_SAMPLING —
					// already a proper List after JSON parsing; preserve as-is
					overrideParameters.put(key, value);
				} else {
					overrideParameters.put(key, value.toString());
				}
			} catch (IllegalArgumentException ignored) {
				// unknown key — skip
			}
		}
		return overrideParameters;
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
					"From " + getTDMDBSchema() + ".environments env\n" +
					"Where env.environment_status = 'Active'";
			try (Db.Rows rows = db(TDM).fetch(allEnvs)) {
				List<String> columnNames = rows.getColumnNames();
				for (Db.Row row : rows) {
					ResultSet resultSet = row.resultSet();
					Map<String, Object> rowMap = new HashMap<>();
					for (String columnName : columnNames) {
						rowMap.put(columnName, resultSet.getObject(columnName));
					}
					rowsList.add(rowMap);
				}
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
		    Db.Rows checkTable = fabric().fetch("broadway " + sanitizeDslToken(luType.luName) + ".filterOutTDMTables tableName=?, luName=?, RESULT_STRUCTURE=ROW",
		        tableName, luType.luName);
		
		    if (checkTable != null && checkTable.firstValue() != null) {
		        tablesList.add(tableName);
		    }
			
			if (checkTable != null) {
				checkTable.close();
			}
		
		}
		
		
		return tablesList;
	}

	@desc("Checks whether any of the given tables already belong to the given Logical Unit's schema. " +
			"Returns an error message if so, or an empty string (\"\") if the tables are valid.")
	@out(name = "result", type = String.class, desc = "")
	public static String validateTablesNotInLuSchema(String luName, List<String> tableNames) throws Exception {
		if (luName == null || luName.isEmpty() || tableNames == null || tableNames.isEmpty()) {
			return "";
		}

		List<String> luTables = getLuTablesList(luName);
		for (String tableName : tableNames) {
			if (tableName != null && luTables.contains(tableName)) {
				return "Table \"" + tableName + "\" is already included in Logical Unit \"" + luName +
						"\" and cannot be selected individually.";
			}
		}
		return "";
	}

	private static boolean fnIsLimitRequested(Object limitValue) {
		if (limitValue == null) {
			return false;
		}
		try {
			return Long.parseLong(limitValue.toString()) >= 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@desc("Checks whether any pair of tables in the given list has a foreign key relation between them " +
			"where the PARENT (referenced) side requested a records/partitions limit - limiting the child " +
			"side alone is safe, since its rows still reference an unrestricted parent set. Returns an " +
			"error message if so, or an empty string (\"\") if the combination is valid.")
	@out(name = "result", type = String.class, desc = "")
	public static String validateNoLimitOnFkRelatedTables(List<Map<String, Object>> tableList) throws Exception {
		if (tableList == null || tableList.size() < 2) {
			return "";
		}

		// Group by (lu_name, interface_name, schema_name) first so target-override resolution below costs
		// one "RefList" mtable lookup per distinct source group instead of one per table - tables commonly
		// share the same source interface/schema within a task, so this is typically a handful of lookups
		// rather than one per table.
		Map<String, List<Map<String, Object>>> tablesBySourceGroup = new LinkedHashMap<>();
		for (Map<String, Object> table : tableList) {
			Object interfaceNameObj = table.get("interface_name");
			Object tableNameObj = table.get("reference_table_name") != null
					? table.get("reference_table_name")
					: table.get("ref_table_name");
			if (interfaceNameObj == null || tableNameObj == null) {
				continue;
			}
			String interfaceName = interfaceNameObj.toString();
			String schemaName = table.get("schema_name") != null ? table.get("schema_name").toString() : "";
			String luName = table.get("lu_name") != null ? table.get("lu_name").toString() : "";
			String groupKey = luName + "|" + interfaceName + "|" + schemaName;
			tablesBySourceGroup.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(table);
		}

		Map<String, Map<String, Map<String, Object>>> tablesByInterfaceAndSchema = new HashMap<>();
		for (List<Map<String, Object>> group : tablesBySourceGroup.values()) {
			Map<String, Object> first = group.get(0);
			String interfaceName = first.get("interface_name").toString();
			String schemaName = first.get("schema_name") != null ? first.get("schema_name").toString() : "";
			String luName = first.get("lu_name") != null ? first.get("lu_name").toString() : null;

			Map<String, Map<String, Object>> targetOverridesByTable = loadTargetOverrides(interfaceName, schemaName, luName);

			for (Map<String, Object> table : group) {
				Object tableNameObj = table.get("reference_table_name") != null
						? table.get("reference_table_name")
						: table.get("ref_table_name");
				String tableName = tableNameObj.toString();

				String targetInterfaceName = interfaceName;
				String targetSchemaName = schemaName;
				String targetTableName = tableName;
				Map<String, Object> override = targetOverridesByTable.get(tableName.toUpperCase());
				if (override != null) {
					Object tarInterface = override.get("target_interface_name");
					if (tarInterface != null && !tarInterface.toString().isEmpty()) {
						targetInterfaceName = tarInterface.toString();
					}
					Object tarSchema = override.get("target_schema_name");
					if (tarSchema != null && !tarSchema.toString().isEmpty()) {
						targetSchemaName = tarSchema.toString();
					}
					Object tarTable = override.get("target_ref_table_name");
					if (tarTable != null && !tarTable.toString().isEmpty()) {
						targetTableName = tarTable.toString();
					}
				}

				boolean limitRequested = fnIsLimitRequested(table.get("records_limit"))
						|| fnIsLimitRequested(table.get("partitions_limit"));

				Map<String, Object> tableEntry = new HashMap<>();
				tableEntry.put("name", targetTableName);
				tableEntry.put("limitRequested", limitRequested);

				tablesByInterfaceAndSchema
						.computeIfAbsent(targetInterfaceName + "|" + targetSchemaName, k -> new HashMap<>())
						.put(targetTableName.toUpperCase(), tableEntry);
			}
		}

		for (Map.Entry<String, Map<String, Map<String, Object>>> group : tablesByInterfaceAndSchema.entrySet()) {
			Map<String, Map<String, Object>> tables = group.getValue();
			if (tables.size() < 2) {
				continue;
			}
			String[] interfaceAndSchema = group.getKey().split("\\|", 2);
			String interfaceName = interfaceAndSchema[0];
			String schemaName = interfaceAndSchema.length > 1 ? interfaceAndSchema[1] : "";

			// Opened lazily - only if some table in this group isn't cataloged and needs a live JDBC lookup.
			DatabaseMetaData meta = null;
			boolean connectionFailed = false;

			for (Map<String, Object> child : tables.values()) {
				String childName = (String) child.get("name");

				List<Map<String, Object>> relations = getCatalogFkRelations(interfaceName, schemaName, childName);
				if (relations == null) {
					// Not cataloged at all - fall back to a live JDBC metadata lookup for this table
					if (meta == null && !connectionFailed) {
						try {
							meta = getConnection(interfaceName).getMetaData();
						} catch (Exception e) {
							connectionFailed = true;
						}
					}
					if (meta == null) {
						continue;
					}
					relations = getJdbcFkRelations(meta, interfaceName, schemaName, childName);
				}

				// Check as each relation is read instead of collecting them all first, so a violating
				// pair returns immediately without evaluating the rest of this table's relations.
				for (Map<String, Object> relation : relations) {
					Object pkTableName = relation.get("pkTableName");
					if (pkTableName == null) {
						continue;
					}
					Map<String, Object> parent = tables.get(pkTableName.toString().toUpperCase());
					if (parent == null) {
						continue;
					}
					String parentName = (String) parent.get("name");
					if (parentName.equalsIgnoreCase(childName)) {
						continue;
					}
					if (Boolean.TRUE.equals(parent.get("limitRequested"))) {
						return "The records/partitions limit option is not supported for the parent side of a foreign key relation: \"" +
								parentName + "\" is referenced by \"" + childName + "\" (interface=\"" + interfaceName + "\"" +
								(schemaName.isEmpty() ? "" : ", schema=\"" + schemaName + "\"") +
								"). Applying a limit to the parent table can leave child rows referencing excluded parent rows.";
					}
				}
			}
		}

		return "";
	}

	public static List<Map<String, Object>> getCatalogFkRelations(String interfaceName, String schemaName, String tableName)
			throws Exception {
		Map<String, Object> relationInputs = new HashMap<>();
		relationInputs.put("childDataPlatform", interfaceName);
		relationInputs.put("childSchema", schemaName);
		relationInputs.put("fkTableName", tableName);
		relationInputs.put("origin", "Crawler");

		List<Map<String, Object>> relationRows = MtableLookup("catalog_relations_info", relationInputs, MTable.Feature.caseInsensitive);
		if (relationRows != null && !relationRows.isEmpty()) {
			return relationRows;
		}

		Map<String, Object> fieldInputs = new HashMap<>();
		fieldInputs.put("dataPlatform", interfaceName);
		fieldInputs.put("schema", schemaName);
		fieldInputs.put("dataset", tableName);
		List<Map<String, Object>> fieldRows = MtableLookup("catalog_field_info", fieldInputs, MTable.Feature.caseInsensitive);
		if (fieldRows == null || fieldRows.isEmpty()) {
			return null;
		}
		return Collections.emptyList();
	}

	public static List<Map<String, Object>> getJdbcFkRelations(DatabaseMetaData meta, String interfaceName,
			String schemaName, String tableName) throws SQLException {
		List<Map<String, Object>> relations = new ArrayList<>();
		try (ResultSet rs = meta.getImportedKeys(null, schemaName == null || schemaName.isEmpty() ? null : schemaName, tableName)) {
			while (rs.next()) {
				String pkTable = rs.getString("PKTABLE_NAME");
				if (pkTable == null) {
					continue;
				}
				String pkSchema = Util.isEmpty(rs.getString("PKTABLE_SCHEM"))
						? rs.getString("PKTABLE_CAT")
						: rs.getString("PKTABLE_SCHEM");

				Map<String, Object> relation = new HashMap<>();
				relation.put("parentDataPlatform", interfaceName);
				relation.put("parentSchema", pkSchema);
				relation.put("pkTableName", pkTable);
				relations.add(relation);
			}
		}
		return relations;
	}

	private static Map<String, Map<String, Object>> loadTargetOverrides(String interfaceName, String schemaName,
			String luName) throws Exception {
		Map<String, Object> lookupInputs = new HashMap<>();
		lookupInputs.put("lu_name", luName);
		lookupInputs.put("interface_name", interfaceName);
		lookupInputs.put("schema_name", schemaName);

		List<Map<String, Object>> refList = MtableLookup("RefList", lookupInputs, MTable.Feature.caseInsensitive);
		Map<String, Map<String, Object>> overridesByTable = new HashMap<>();
		if (refList == null) {
			return overridesByTable;
		}
		for (Map<String, Object> row : refList) {
			Object refTableName = row.get("reference_table_name");
			if (refTableName != null) {
				overridesByTable.putIfAbsent(refTableName.toString().toUpperCase(), row);
			}
		}
		return overridesByTable;
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
			String getUserReserveCnt_sql = "select count(1) from " + getTDMDBSchema() + ".tdm_reserved_entities where env_id = ? and be_id = ? and reserve_owner = ? and " +
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


    public static List<HashMap<String, Object>> fnGetTableFields(String dbInterfaceName, String schemaName, String tableName, String catalogSchema) throws Exception {

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

	private static List<HashMap<String, Object>> getTableFieldsByJDBC(String dbInterfaceName, String schemaName,
		String tableName) throws SQLException {
	List<HashMap<String, Object>> result = new ArrayList<>();

	DatabaseMetaData metaData = getConnection(dbInterfaceName).getMetaData();
	ResultSet columns = metaData.getColumns(null, schemaName, tableName, null);

	while (columns.next()) {
		HashMap<String, Object> map = new HashMap<>();

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

   private static List<HashMap<String, Object>> getTableFieldsByCatalog(List<Map<String, Object>> interfaceTables) throws Exception {
	List<HashMap<String, Object>> result = new ArrayList<>();

	for (Map<String, Object> fieldRec : interfaceTables) {
		if (isComplexCatalogField(fieldRec)) {
			// nested descendant row (class != dataset) - describes a sub-field of an already-complex
			// top-level field (e.g. TomatoesClass;CriticClass.rating describes part of "tomatoes") and
			// is never a column of its own; the top-level field is serialized whole, depth included.
			continue;
		}

		String rawFieldName = fieldRec.get("field").toString();
		String fieldName = rawFieldName;
		String columnType;
		String sqliteType;

		if (isComplexFieldValue(fieldRec)) {
			// top-level field whose value is itself an object/array (e.g. Mongo sub-document or array) -
			// its own sqlDataType/definedBy is meaningless for this (e.g. reports VARCHAR for an object).
			columnType = COMPLEX_FIELD_TYPE;
			sqliteType = "BLOB";
		} else {
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
			}
			sqliteType = columnType;
		}

		if (fnCheckSpecialChars(fieldName)) {
			fieldName = "\"" + fieldName + "\"";
		}

		HashMap<String, Object> map = new HashMap<>();
		map.put("column_name", fieldName);
		map.put("column_type", columnType);
		map.put("column_sqlite_type", sqliteType);

		Object fieldAlias = fieldRec.get("fieldAlias");
		if (fieldAlias != null && !fieldAlias.toString().isEmpty()) {
			map.put("field_alias", fieldAlias.toString());
		}

		Object minValue = fieldRec.get("minimumValue");
		if (minValue != null && !minValue.toString().isEmpty()) {
			map.put("min_value", minValue.toString());
		}

		Object maxValue = fieldRec.get("maximumValue");
		if (maxValue != null && !maxValue.toString().isEmpty()) {
			map.put("max_value", maxValue.toString());
		}

		Set<String> validValues = getOptionSetValidValues(fieldRec, rawFieldName);
		if (validValues != null) {
			map.put("valid_values", validValues);
		}

		result.add(map);
	}

	return result;
}

	/**
	 * When a catalog field is flagged as an option set (profiled as having a bounded set of discrete
	 * values, independent of its masking classification - e.g. STATE/COUNTRY fields carry optionSet=true
	 * too), looks up its distinct values from the sibling "catalog_field_option_set" mtable, keyed by the
	 * same dataPlatform/schema/dataset/class/field identity as the catalog_field_info row. Returns null
	 * when the field isn't an option set, or when no option-set mtable/rows exist for it (e.g. the catalog
	 * schema was profiled before option-set generation was added).
	 */
	private static Set<String> getOptionSetValidValues(Map<String, Object> fieldRec, String fieldName) throws Exception {
		Object optionSetFlag = fieldRec.get("optionSet");
		if (optionSetFlag == null || !Boolean.parseBoolean(optionSetFlag.toString())) {
			return null;
		}

		Map<String, Object> optionSetKey = new HashMap<>();
		optionSetKey.put("dataPlatform", fieldRec.get("dataPlatform"));
		optionSetKey.put("schema", fieldRec.get("schema"));
		optionSetKey.put("dataset", fieldRec.get("dataset"));
		optionSetKey.put("class", fieldRec.get("class"));
		optionSetKey.put("field", fieldName);

		List<Map<String, Object>> optionSetRows = MtableLookup("catalog_field_option_set", optionSetKey, MTable.Feature.caseInsensitive);
		if (optionSetRows == null || optionSetRows.isEmpty()) {
			return null;
		}

		LinkedHashSet<String> values = new LinkedHashSet<>();
		for (Map<String, Object> row : optionSetRows) {
			Object value = row.get("value");
			if (value != null) {
				values.add(value.toString());
			}
		}

		return values.isEmpty() ? null : values;
	}

	private static final String ROW_MARKER_FIELD = "__ROW__";

	/**
	 * Shields a Collection/Map field value from Broadway's per-path-segment iterability check
	 * (com.k2view.broadway.model.port.path.AbstractNode#isIterable) so that an "iterate: Iterate" link
	 * on ".../result/value" (see SerializeComplexFields.flow's Wrap Value actor) iterates once per
	 * flattened {row, fieldName, value} entry rather than a second, unintended time into the value's
	 * own elements - which would otherwise fan Wrap/Serialize/Scatter out per array element or per map
	 * entry and leave fnScatterOneField's row.put overwriting down to just the last one.
	 */
	private static class ComplexFieldValueBox implements java.io.Serializable {
		final Object value;

		ComplexFieldValueBox(Object value) {
			this.value = value;
		}
	}

	/**
	 * Pulls out the value of every COMPLEX (document/nested) field across all rows into a flat list of
	 * {row, fieldName, value} entries, so each value can be run through a single-level iteration (e.g.
	 * ObjectSerialize/ObjectDeserialize) without needing nested per-row/per-field flow iteration. Each
	 * entry carries the originating row's own Map reference (not an index) so fnScatterOneField can
	 * write values back without needing a second, separate read of "rows" - the upstream source (see
	 * GetSourceDataPartition1's innerFlowClose:false in TDMTableLevelOrchestrator.flow) is a
	 * single-pass stream, exhausted after this method's one full traversal, so nothing downstream can
	 * safely read "rows" again. A marker entry (fieldName ROW_MARKER_FIELD) is emitted last for each
	 * row - after all of that row's field entries - so rows with zero populated complex fields are
	 * still represented, and so that when fnScatterOneField sees the marker it's already applied every
	 * preceding field entry for that same row (entries for one row are always adjacent and in order).
	 */
	public static List<Map<String, Object>> fnFlattenComplexFields(List<Map<String, Object>> rows,
			List<Map<String, Object>> fields) {
		List<Map<String, Object>> flat = new ArrayList<>();
		if (rows == null || fields == null) {
			return flat;
		}

		List<String> complexFieldNames = complexFieldNames(fields);
		for (Map<String, Object> row : rows) {
			for (String fieldName : complexFieldNames) {
				Object value = row.get(fieldName);
				if (value != null) {
					Map<String, Object> entry = new HashMap<>();
					entry.put("row", row);
					entry.put("fieldName", fieldName);
					entry.put("value", value instanceof java.util.Collection || value instanceof Map
							? new ComplexFieldValueBox(value) : value);
					flat.add(entry);
				}
			}

			Map<String, Object> marker = new HashMap<>();
			marker.put("row", row);
			marker.put("fieldName", ROW_MARKER_FIELD);
			marker.put("value", null);
			flat.add(marker);
		}

		return flat;
	}

	private static List<String> complexFieldNames(List<Map<String, Object>> fields) {
		List<String> names = new ArrayList<>();
		for (Map<String, Object> field : fields) {
			if (COMPLEX_FIELD_TYPE.equals(String.valueOf(field.get("column_type")))) {
				names.add(String.valueOf(field.get("column_name")).replaceAll("^\"|\"$", ""));
			}
		}
		return names;
	}

	/**
	 * Runs once per fnFlattenComplexFields entry (paired positionally with the entry's serialized/
	 * deserialized value) - see SerializeComplexFields.flow/DeserializeComplexFields.flow: a plain
	 * LuFunction downstream of an iterate:Iterate chain is invoked once per iteration, not once with
	 * the full aggregated arrays, so scattering has to happen per-entry here rather than in one final
	 * call. For a real field entry, writes {@code value} into the entry's row. For the row's marker
	 * entry (emitted last per row by fnFlattenComplexFields, after that row's real fields), emits the
	 * row itself so the flow's ArrayBuilder actor can accumulate exactly one copy of each row, fully
	 * populated, across all iterations.
	 */
	public static List<Map<String, Object>> fnScatterOneField(Object entry, Object value) {
		Map<String, Object> entryMap = (Map<String, Object>) entry;
		String fieldName = (String) entryMap.get("fieldName");
		Map<String, Object> row = (Map<String, Object>) entryMap.get("row");

		if (ROW_MARKER_FIELD.equals(fieldName)) {
			return new ArrayList<>(Collections.singletonList(row));
		}

		if (value instanceof ComplexFieldValueBox box) {
			value = box.value;
		}

		row.put(fieldName, value);
		return new ArrayList<>();
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

	public static boolean isComplexCatalogField(Map<String, Object> fieldRec) {
		Object fieldClass = fieldRec.get("class");
		Object dataset = fieldRec.get("dataset");
		return fieldClass != null && dataset != null && !dataset.toString().equals(fieldClass.toString());
	}

	/**
	 * True when a top-level catalog field's own value is an object/array (a Document DB sub-document or
	 * array field) rather than a scalar - primarily detected via sourceDataType ("object"/"array"), with
	 * a definedBy-shape fallback (ends with "Class" or starts with "Collection(") for catalogs where
	 * sourceDataType is absent. Not meaningful for rows where isComplexCatalogField() is already true.
	 */
	public static boolean isComplexFieldValue(Map<String, Object> fieldRec) {
		Object sourceDataType = fieldRec.get("sourceDataType");
		if (sourceDataType != null) {
			String s = sourceDataType.toString();
			if ("object".equalsIgnoreCase(s) || "array".equalsIgnoreCase(s)) {
				return true;
			}
		}
		Object definedBy = fieldRec.get("definedBy");
		if (definedBy != null) {
			String d = definedBy.toString();
			if (d.endsWith("Class") || d.startsWith("Collection(")) {
				return true;
			}
		}
		return false;
	}

	private static String getFieldTypeBydefinedBy(Map<String, Object> fieldRec) throws Exception{

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
            String sql = "SELECT param_name, param_value FROM " + getTDMDBSchema() + ".tdm_general_parameters WHERE " +
					"UPPER(param_name) in ('IID_SEPARATOR', 'IID_OPEN_SEPARATOR', 'IID_CLOSE_SEPARATOR')";

            try (Db.Rows rows = db(TDM).fetch(sql)) {
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
            executionMode = db(TDM).fetch("select execution_mode from " + getTDMDBSchema() + ".business_entities where be_id = ?", beId).firstValue().toString();
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

			String sql = "SELECT param_value FROM " + getTDMDBSchema() +
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
			String getEnvironmentSql = "SELECT * FROM " + getTDMDBSchema() + ".environments WHERE environment_id = ?";
			Db.Row env = db(TDM).fetch(getEnvironmentSql, envId).firstRow();

			if (env.isEmpty()) {
				response.put("errorCode", "SUCCESS");
				response.put("result", null);
				return response;
			}

			// getEnvProduct
			String sql = "SELECT * , " +
					"( SELECT COUNT(*) FROM " + getTDMDBSchema() + ".tasks " +
					"INNER JOIN " + getTDMDBSchema() + ".tasks_logical_units ON (tasks_logical_units.task_id = tasks.task_id) " +
					"INNER JOIN " + getTDMDBSchema()
					+ ".product_logical_units ON (product_logical_units.lu_id = tasks_logical_units.lu_id) " +
					"WHERE product_logical_units.product_id = products.product_id AND tasks.task_status = 'Active' ) AS taskcount "
					+
					"FROM " + getTDMDBSchema() + ".environment_products " +
					"INNER JOIN " + getTDMDBSchema() + ".products ON (environment_products.product_id = products.product_id) " +
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
		Db.Row config = fabric().fetch("list config section_filter=batch_process key_filter=MAX_WORKERS_PER_NODE;")
				.firstRow();
		Object globalValueObj = config == null ? null : config.get("Value");

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
			String sql = "SELECT * FROM " + getTDMDBSchema() + ".product_logical_units lu " + "INNER JOIN " + getTDMDBSchema()
					+ ".products p " +
					"ON (lu.product_id = p.product_id) " + "INNER JOIN " + getTDMDBSchema() + ".environment_products ep " +
					"ON (lu.product_id = ep.product_id " + "AND ep.status = \'Active\') " + "WHERE be_id = ?" +
					" AND environment_id = ? AND ep.enable_product=true";
			Db.Rows rows = db(TDM).fetch(sql, beId, envId);
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
	
    private static Set<String> resolveInterfaceNames(String interfaceName) throws Exception {
        if (interfaceName != null && !interfaceName.isEmpty()) {
            return Collections.singleton(interfaceName);
        }
        Set<String> names = new LinkedHashSet<>();
        for (Object value : MtableGetKeyValues("catalog_field_info", "dataPlatform")) {
            names.add(String.valueOf(value));
        }
        return names;
    }

    private static List<Map<String, Object>> getFieldInfoRows(String interfaceName) throws Exception {
        Map<String, Object> interfaceInput = new HashMap<>();
        interfaceInput.put("dataPlatform", interfaceName);
        return MtableLookup("catalog_field_info", interfaceInput, MTable.Feature.caseInsensitive);
    }

    public static boolean isPiiField(Map<String, Object> row) {
        return "true".equalsIgnoreCase(String.valueOf(row.get("pii")));
    }

    public static Map<String, Object> getFieldInfoRow(String dataPlatform, String schema, String dataset, String field) throws Exception {
        Map<String, Object> input = new HashMap<>();
        if (dataPlatform != null && !dataPlatform.isEmpty()) {
            input.put("dataPlatform", dataPlatform);
        }

		input.put("schema", schema);
        input.put("dataset", dataset);
        input.put("field", field);
        List<Map<String, Object>> rows = MtableLookup("catalog_field_info", input, MTable.Feature.caseInsensitive);
        return (rows == null || rows.isEmpty()) ? null : rows.get(0);
    }

    @out(name = "result", type = List.class, desc = "")
    public static List<Map<String, Object>> getInterfacesPIISummary() throws Exception {
        Map<String, String> interfaceNameByKey = new LinkedHashMap<>();
        Map<String, String> schemaNames = new LinkedHashMap<>();
        Map<String, Set<String>> datasetsByGroup = new LinkedHashMap<>();
        Map<String, Set<String>> datasetsWithPIIByGroup = new LinkedHashMap<>();
        Map<String, Integer> piiFieldCountByGroup = new LinkedHashMap<>();

        for (String interfaceName : resolveInterfaceNames(null)) {
            for (Map<String, Object> row : getFieldInfoRows(interfaceName)) {
                String schemaName = String.valueOf(row.get("schema"));
                String datasetName = String.valueOf(row.get("dataset"));
                String key = interfaceName + "|" + schemaName;

                interfaceNameByKey.putIfAbsent(key, interfaceName);
                schemaNames.putIfAbsent(key, schemaName);
                datasetsByGroup.computeIfAbsent(key, k -> new HashSet<>()).add(datasetName);

                if (isPiiField(row)) {
                    datasetsWithPIIByGroup.computeIfAbsent(key, k -> new HashSet<>()).add(datasetName);
                    piiFieldCountByGroup.merge(key, 1, Integer::sum);
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (String key : interfaceNameByKey.keySet()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("Interface Name", interfaceNameByKey.get(key));
            entry.put("Schema Name", schemaNames.get(key));
            entry.put("Total Number Of Datasets", datasetsByGroup.get(key).size());
            entry.put("Number Of Datasets With PII Fields", datasetsWithPIIByGroup.getOrDefault(key, Collections.emptySet()).size());
            entry.put("Number Of PII Fields", piiFieldCountByGroup.getOrDefault(key, 0));
            result.add(entry);
        }
        result.sort((a, b) -> {
            int cmp = String.valueOf(a.get("Interface Name")).compareToIgnoreCase(String.valueOf(b.get("Interface Name")));
            return cmp != 0 ? cmp : String.valueOf(a.get("Schema Name")).compareToIgnoreCase(String.valueOf(b.get("Schema Name")));
        });
        return result;
    }

    public static Map<String, String> getSystemByInterfaceMap() throws Exception {
        Map<String, String> systemByInterface = new HashMap<>();
        String productsSql = "SELECT product_name, related_interfaces FROM " + getTDMDBSchema()
                + ".products WHERE product_status = 'Active'";
        try (Db.Rows productRows = db(TDM).fetch(productsSql)) {
            for (Db.Row row : productRows) {
                String productName = row.get("product_name") != null ? row.get("product_name").toString() : null;
                Object rawInterfaces = row.get("related_interfaces");
                List<String> interfaces = new ArrayList<>();

                if (rawInterfaces instanceof java.sql.Array) {
                    String[] interfaceArray = (String[]) ((java.sql.Array) rawInterfaces).getArray();
                    interfaces = java.util.Arrays.asList(interfaceArray);
                } else if (rawInterfaces instanceof List) {
                    interfaces = (List<String>) rawInterfaces;
                }

                for (String interfaceName : interfaces) {
                    systemByInterface.put(interfaceName.trim().toUpperCase(), productName);
                }
            }
        } catch (Exception e) {
            UserCode.log.error("getSystemByInterfaceMap - failed to fetch products/related_interfaces", e);
            throw e;
        }
        return systemByInterface;
    }

    @out(name = "result", type = List.class, desc = "")
    public static List<Map<String, Object>> getInterfacesTablesSummary() throws Exception {
        return buildInterfacesTablesSummary(null);
    }

    @out(name = "result", type = List.class, desc = "")
    public static List<Map<String, Object>> getInterfacesTablesSummaryByInterface(String interfaceName) throws Exception {
        return buildInterfacesTablesSummary(interfaceName);
    }

    private static List<Map<String, Object>> buildInterfacesTablesSummary(String interfaceName) throws Exception {
        Map<String, String> systemByInterface = getSystemByInterfaceMap();

        Map<String, String> interfaceNameByKey = new LinkedHashMap<>();
        Map<String, String> schemaByKey = new LinkedHashMap<>();
        Map<String, String> systemByKey = new LinkedHashMap<>();
        Map<String, String> datasetByKey = new LinkedHashMap<>();
        Map<String, Integer> fieldCountByKey = new LinkedHashMap<>();
        Map<String, Integer> piiFieldCountByKey = new LinkedHashMap<>();

        for (String currentInterfaceName : resolveInterfaceNames(interfaceName)) {
            String systemName = systemByInterface.get(currentInterfaceName.toUpperCase());

            for (Map<String, Object> row : getFieldInfoRows(currentInterfaceName)) {
                String schemaName = String.valueOf(row.get("schema"));
                String datasetName = String.valueOf(row.get("dataset"));
                String key = currentInterfaceName + "|" + schemaName + "|" + datasetName;

                interfaceNameByKey.putIfAbsent(key, currentInterfaceName);
                schemaByKey.putIfAbsent(key, schemaName);
                systemByKey.putIfAbsent(key, systemName);
                datasetByKey.putIfAbsent(key, datasetName);
                fieldCountByKey.merge(key, 1, Integer::sum);

                if (isPiiField(row)) {
                    piiFieldCountByKey.merge(key, 1, Integer::sum);
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (String key : interfaceNameByKey.keySet()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("Interface Name", interfaceNameByKey.get(key));
            entry.put("Schema", schemaByKey.get(key));
            entry.put("System", systemByKey.get(key));
            entry.put("Dataset", datasetByKey.get(key));
            entry.put("Number Of Fields", fieldCountByKey.getOrDefault(key, 0));
            entry.put("Number Of Sensitive Fields", piiFieldCountByKey.getOrDefault(key, 0));
            result.add(entry);
        }
        return result;
    }

    public static String deriveDataConsistency(Map<String, Object> generatorRow) {
        if ("false".equalsIgnoreCase(String.valueOf(generatorRow.get("consistent")))) {
            return "Random";
        }
        if ("true".equalsIgnoreCase(String.valueOf(generatorRow.get("unique")))) {
            return "Consistent & Unique";
        }
        if ("true".equalsIgnoreCase(String.valueOf(generatorRow.get("seed")))) {
            return "Consistent Using Seed";
        }
        return "Consistent Using Table";
    }

    public static Map<String, Map<String, String>> getClassificationGeneratorMap() throws Exception {
        Map<String, Map<String, String>> byClassification = new LinkedHashMap<>();
        for (String category : new String[] { "enable_masking", "enable_masking_uniqueness" }) {
            Map<String, Object> categoryInput = new HashMap<>();
            categoryInput.put("category", category);
            List<Map<String, Object>> rows = MtableLookup("catalog_classification_generators", categoryInput, MTable.Feature.caseInsensitive);
            if (rows == null) {
                continue;
            }

            for (Map<String, Object> row : rows) {
                Object classification = row.get("classification");
                if (classification == null) {
                    continue;
                }

                Map<String, String> info = new HashMap<>();
                Object generator = row.get("generator");
                Object description = row.get("description");
                info.put("generator", generator != null ? generator.toString() : "");
                info.put("consistency", deriveDataConsistency(row));
                info.put("description", description != null ? description.toString() : "");
                byClassification.putIfAbsent(classification.toString().toUpperCase(), info);
            }
        }
        return byClassification;
    }

    public static String getFieldDataType(Map<String, Object> fieldRec) throws Exception {
        if (isComplexFieldValue(fieldRec)) {
            Object definedBy = fieldRec.get("definedBy");
            return definedBy != null ? definedBy.toString() : COMPLEX_FIELD_TYPE;
        }

        Object sqlDataType = fieldRec.get("sqlDataType");
        if (sqlDataType != null) {
            Object sourceDataType = fieldRec.get("sourceDataType");
            String sourceDataTypeStr = sourceDataType != null ? sourceDataType.toString() : null;
            return toSqliteType(Integer.parseInt(sqlDataType.toString()), sourceDataTypeStr);
        }
        return getFieldTypeBydefinedBy(fieldRec);
    }

    @out(name = "result", type = List.class, desc = "")
    public static List<Map<String, Object>> getInterfacesTableFieldsList() throws Exception {
        return buildInterfacesTableFieldsList(null, null);
    }

    @out(name = "result", type = List.class, desc = "")
    public static List<Map<String, Object>> getInterfacesTableFieldsListByInterface(String interfaceName, String datasetName) throws Exception {
        return buildInterfacesTableFieldsList(interfaceName, datasetName, true);
    }

    @out(name = "result", type = List.class, desc = "")
    public static List<Map<String, Object>> getInterfacesTableFieldsListByInterfaceNonPII(String interfaceName, String datasetName) throws Exception {
        return buildInterfacesTableFieldsList(interfaceName, datasetName, false);
    }

    private static List<Map<String, Object>> buildInterfacesTableFieldsList(String interfaceName, String datasetName) throws Exception {
        return buildInterfacesTableFieldsList(interfaceName, datasetName, true);
    }

    private static List<Map<String, Object>> buildInterfacesTableFieldsList(String interfaceName, String datasetName, boolean piiOnly) throws Exception {
        Map<String, String> systemByInterface = getSystemByInterfaceMap();
        Map<String, Map<String, String>> classificationGenerators = getClassificationGeneratorMap();

        List<Map<String, Object>> result = new ArrayList<>();
        for (String currentInterfaceName : resolveInterfaceNames(interfaceName)) {
            String systemName = systemByInterface.get(currentInterfaceName.toUpperCase());

            for (Map<String, Object> row : getFieldInfoRows(currentInterfaceName)) {
                if (piiOnly != isPiiField(row)) {
                    continue;
                }

                if (datasetName != null && !datasetName.isEmpty()
                        && !datasetName.equalsIgnoreCase(String.valueOf(row.get("dataset")))) {
                    continue;
                }

                Object classification = row.get("classification");
                Map<String, String> generatorInfo = classification != null
                        ? classificationGenerators.get(classification.toString().toUpperCase())
                        : null;

                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("Interface Name", currentInterfaceName);
                entry.put("Schema", String.valueOf(row.get("schema")));
                entry.put("System", systemName);
                entry.put("Dataset", String.valueOf(row.get("dataset")));
                entry.put("Object Name", row.get("class") != null ? row.get("class").toString() : null);
                entry.put("Field", row.get("field") != null ? row.get("field").toString() : null);
                entry.put("Data Type", getFieldDataType(row));
                entry.put("Classification", classification != null ? classification.toString() : null);
                entry.put("Data Generator", generatorInfo != null ? generatorInfo.get("generator") : null);
                entry.put("Data Consistency", generatorInfo != null ? generatorInfo.get("consistency") : null);
                entry.put("Description", generatorInfo != null ? generatorInfo.get("description") : null);
                result.add(entry);
            }
        }
        result.sort(Comparator
                .comparing((Map<String, Object> e) -> String.valueOf(e.get("Interface Name")), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(e -> String.valueOf(e.get("Schema")), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(e -> String.valueOf(e.get("Dataset")), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(e -> String.valueOf(e.get("Field")), String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    // ---------------------------------------------------------------------------------------------
    // Masking catalog per execution - the catalog CSVs stored at execution time for the masking reports:
    // tdm_exe_catalog_field_info (per catalog version) and tdm_exe_catalog_classification_generators
    // (per execution).
    // ---------------------------------------------------------------------------------------------

    /**
     * Parses CSV text (RFC 4180: quoted fields, "" escapes, line breaks inside quotes) into one
     * case-insensitive map per data row, keyed by the header fields. Empty values are returned as null.
     */
    public static List<Map<String, Object>> parseCsvText(String csvText) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<List<String>> records = parseCsvRecords(csvText);
        if (records.isEmpty()) {
            return rows;
        }

        List<String> header = records.get(0);
        for (int r = 1; r < records.size(); r++) {
            List<String> values = records.get(r);
            if (values.size() == 1 && values.get(0).isEmpty()) {
                continue;
            }
            Map<String, Object> row = new CaseInsensitiveMap<>();
            for (int c = 0; c < header.size(); c++) {
                String value = c < values.size() ? values.get(c) : "";
                row.put(header.get(c), value.isEmpty() ? null : value);
            }
            rows.add(row);
        }
        return rows;
    }

    /** Splits CSV text into records of raw field values; the first record is the header. */
    private static List<List<String>> parseCsvRecords(String csvText) {
        List<List<String>> records = new ArrayList<>();
        if (csvText == null || csvText.isEmpty()) {
            return records;
        }

        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int len = csvText.length();
        for (int i = 0; i < len; i++) {
            char c = csvText.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < len && csvText.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                record.add(field.toString());
                field.setLength(0);
            } else if (c == '\r' || c == '\n') {
                if (c == '\r' && i + 1 < len && csvText.charAt(i + 1) == '\n') {
                    i++;
                }
                record.add(field.toString());
                field.setLength(0);
                records.add(record);
                record = new ArrayList<>();
            } else {
                field.append(c);
            }
        }
        if (field.length() > 0 || !record.isEmpty()) {
            record.add(field.toString());
            records.add(record);
        }
        if (!records.isEmpty() && !records.get(0).isEmpty() && records.get(0).get(0).startsWith("\uFEFF")) {
            records.get(0).set(0, records.get(0).get(0).substring(1));
        }
        return records;
    }

    private static final String CATALOG_FIELD_INFO_MTABLE = "catalog_field_info";
    private static final String CATALOG_CLASSIFICATION_GENERATORS_MTABLE = "catalog_classification_generators";
    // catalog_field_info columns used by the masking reports - the only ones stored and hashed
    private static final Set<String> CATALOG_FIELD_INFO_REPORT_COLUMNS = Set.of(
            "dataset", "class", "field", "sqldatatype", "sourcedatatype", "definedby", "pii", "classification");
    // last execution whose catalog is stored - lets its other LUs on this node skip the TDMDB check
    private static volatile Long lastStoredCatalogExecution;

    /**
     * Stores the masking catalog of an execution for the masking report: the changed catalog_field_info of the
     * interfaces it masks, and the classification generators. Runs once per execution.
     */
    public static void fnStoreMaskingCatalogForExecution(Object taskExecutionId) throws Exception {
        Long taskExecutionIdValue = toLongOrNull(taskExecutionId);
        if (taskExecutionIdValue == null || taskExecutionIdValue.equals(lastStoredCatalogExecution)) {
            return;
        }
        String schema = getTDMDBSchema();
        Object stored = db(TDM).fetch("SELECT 1 FROM " + schema
                + ".tdm_exe_catalog_classification_generators WHERE task_execution_id = ?", taskExecutionIdValue).firstValue();
        if (stored != null) {
            lastStoredCatalogExecution = taskExecutionIdValue;
            return;
        }

        storeCatalogFieldInfo(schema, maskedInterfacesOfExecution(schema, taskExecutionIdValue));

        MTable generators = null;
        try {
            generators = MTables.get(CATALOG_CLASSIFICATION_GENERATORS_MTABLE);
        } catch (Exception e) {
            UserCode.log.warn("fnStoreMaskingCatalogForExecution - MTable " + CATALOG_CLASSIFICATION_GENERATORS_MTABLE + " is not loaded", e);
        }
        db(TDM).execute("INSERT INTO " + schema + ".tdm_exe_catalog_classification_generators (task_execution_id, csv_file) "
                + "VALUES (?, ?) ON CONFLICT (task_execution_id) DO NOTHING",
                taskExecutionIdValue, generators != null ? mtableToCsv(generators) : "");
        lastStoredCatalogExecution = taskExecutionIdValue;
    }

    /**
     * The (interface, schema) keys masked by the execution, in lower case: those of the catalog masking actors of its
     * LUs, and those of its tables (table level).
     */
    private static Set<List<String>> maskedInterfacesOfExecution(String schema, long taskExecutionId) throws Exception {
        Set<List<String>> keys = new LinkedHashSet<>();

        List<String> luNames = new ArrayList<>();
        try (Db.Rows rows = db(TDM).fetch("SELECT DISTINCT tl.lu_name FROM " + schema + ".task_execution_list el JOIN " + schema
                + ".tasks_logical_units tl ON tl.task_id = el.task_id AND tl.lu_id = el.lu_id WHERE el.task_execution_id = ?", taskExecutionId)) {
            for (Db.Row row : rows) {
                String luName = asString(row.get("lu_name"));
                if (!Util.isEmpty(luName)) {
                    luNames.add(luName);
                }
            }
        }
        for (String luName : luNames) {
            try {
                for (TDMMaskingReportLogic.InterfaceSchema interfaceSchema : TDMMaskingReportLogic.getLuMaskedTables(luName).keySet()) {
                    keys.add(catalogKey(interfaceSchema.getInterfaceName(), interfaceSchema.getSchemaName()));
                }
            } catch (Exception e) {
                UserCode.log.warn("maskedInterfacesOfExecution - failed to get the masked tables of LU " + luName
                        + ", its catalog_field_info is not stored", e);
            }
        }

        try (Db.Rows rows = db(TDM).fetch("SELECT DISTINCT interface_name, schema_name FROM " + schema
                + ".task_ref_exe_stats WHERE task_execution_id = ?", taskExecutionId)) {
            for (Db.Row row : rows) {
                String interfaceName = asString(row.get("interface_name"));
                String schemaName = asString(row.get("schema_name"));
                if (!Util.isEmpty(interfaceName) && !Util.isEmpty(schemaName)) {
                    keys.add(catalogKey(interfaceName, schemaName));
                }
            }
        }
        return keys;
    }

    private static List<String> catalogKey(String interfaceName, String schemaName) {
        return Arrays.asList(interfaceName.toLowerCase(java.util.Locale.ROOT), schemaName.toLowerCase(java.util.Locale.ROOT));
    }

    /**
     * Stores the catalog_field_info of the given interfaces whose hash differs from their latest copy (latest update_date).
     * A new hash is inserted; an existing one (reverted catalog) only gets its update_date reset.
     */
    private static void storeCatalogFieldInfo(String schema, Set<List<String>> relevantKeys) throws Exception {
        if (relevantKeys.isEmpty()) {
            return;
        }
        MTable fieldInfo;
        try {
            fieldInfo = MTables.get(CATALOG_FIELD_INFO_MTABLE);
        } catch (Exception e) {
            UserCode.log.warn("storeCatalogFieldInfo - MTable " + CATALOG_FIELD_INFO_MTABLE + " is not loaded, catalog_field_info is not stored", e);
            return;
        }
        List<String> columns = fieldInfo.columns();

        // the rows of each relevant (interface, schema)
        Map<List<String>, List<Object[]>> rowsByInterface = new LinkedHashMap<>();
        int interfacePos = fieldInfo.columnPosition("dataPlatform");
        int schemaPos = fieldInfo.columnPosition("schema");
        try (MTable.AllRows<Object[]> rows = fieldInfo.allRows()) {
            for (Object[] row : rows) {
                String interfaceName = asString(row[interfacePos]);
                String schemaName = asString(row[schemaPos]);
                if (interfaceName != null && schemaName != null && relevantKeys.contains(catalogKey(interfaceName, schemaName))) {
                    rowsByInterface.computeIfAbsent(Arrays.asList(interfaceName, schemaName), k -> new ArrayList<>()).add(row);
                }
            }
        }
        if (rowsByInterface.isEmpty()) {
            return;
        }

        // the CSV and its hash of each (interface, schema)
        Map<List<String>, String> csvByInterface = new LinkedHashMap<>();
        Map<List<String>, String> hashByInterface = new LinkedHashMap<>();
        for (Map.Entry<List<String>, List<Object[]>> entry : rowsByInterface.entrySet()) {
            String csv = interfaceCatalogCsv(columns, entry.getValue());
            csvByInterface.put(entry.getKey(), csv);
            hashByInterface.put(entry.getKey(), sha256Hex(csv));
        }

        // the hash of the latest copy of each interface
        Map<List<String>, String> latestHash = new HashMap<>();
        List<Object> params = new ArrayList<>();
        for (List<String> key : hashByInterface.keySet()) {
            params.add(key.get(0));
            params.add(key.get(1));
        }
        String pairs = String.join(", ", Collections.nCopies(hashByInterface.size(), "(?, ?)"));
        Db tdmDb = db(TDM);
        try (Db.Rows rows = tdmDb.fetch("SELECT DISTINCT ON (interface_name, schema_name) interface_name, schema_name, catalog_hash FROM "
                + schema + ".tdm_exe_catalog_field_info WHERE (interface_name, schema_name) IN (" + pairs + ") "
                + "ORDER BY interface_name, schema_name, update_date DESC NULLS LAST", params.toArray())) {
            for (Db.Row row : rows) {
                latestHash.put(Arrays.asList(asString(row.get("interface_name")), asString(row.get("schema_name"))),
                        asString(row.get("catalog_hash")));
            }
        }
        hashByInterface.entrySet().removeIf(entry -> entry.getValue().equals(latestHash.get(entry.getKey())));
        if (hashByInterface.isEmpty()) {
            return;
        }

        for (Map.Entry<List<String>, String> entry : hashByInterface.entrySet()) {
            List<String> key = entry.getKey();
            tdmDb.execute("INSERT INTO " + schema + ".tdm_exe_catalog_field_info (interface_name, schema_name, catalog_hash, csv_file) "
                    + "VALUES (?, ?, ?, ?) ON CONFLICT (interface_name, schema_name, catalog_hash) DO UPDATE SET update_date = now()",
                    key.get(0), key.get(1), entry.getValue(), csvByInterface.get(key));
        }
    }

    /**
     * The catalog CSV of one interface: its non-empty report columns, sorted by name, and its rows, sorted -
     * so the hash does not depend on the MTable column/row order or on other interfaces' columns.
     */
    private static String interfaceCatalogCsv(List<String> columns, List<Object[]> rows) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            if (!CATALOG_FIELD_INFO_REPORT_COLUMNS.contains(columns.get(i).toLowerCase(java.util.Locale.ROOT))) {
                continue;
            }
            for (Object[] row : rows) {
                if (row[i] != null && !row[i].toString().isEmpty()) {
                    positions.add(i);
                    break;
                }
            }
        }
        positions.sort(Comparator.comparing(columns::get, String.CASE_INSENSITIVE_ORDER));
        List<String> lines = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            lines.add(positions.stream().map(i -> csvValue(row[i])).collect(Collectors.joining(",")));
        }
        Collections.sort(lines);
        StringBuilder csv = new StringBuilder();
        csv.append(positions.stream().map(i -> csvValue(columns.get(i))).collect(Collectors.joining(","))).append("\n");
        for (String line : lines) {
            csv.append(line).append("\n");
        }
        return csv.toString();
    }

    private static String sha256Hex(String text) throws java.security.NoSuchAlgorithmException {
        return java.util.HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    private static String mtableToCsv(MTable mtable) {
        StringBuilder csv = new StringBuilder();
        csv.append(mtable.columns().stream().map(SharedLogic::csvValue).collect(Collectors.joining(","))).append("\n");
        try (MTable.AllRows<Object[]> rows = mtable.allRows()) {
            for (Object[] row : rows) {
                csv.append(Arrays.stream(row).map(SharedLogic::csvValue).collect(Collectors.joining(","))).append("\n");
            }
        }
        return csv.toString();
    }

    private static String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String s = value.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String asString(Object o) {
        return o == null ? null : o.toString();
    }

    private static Long toLongOrNull(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        String s = o.toString().trim();
        if (s.isEmpty()) {
            return null;
        }
        return new BigDecimal(s).longValue();
    }
}
