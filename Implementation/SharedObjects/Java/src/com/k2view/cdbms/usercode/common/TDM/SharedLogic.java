/////////////////////////////////////////////////////////////////////////
// Project Shared Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.cdbms.exceptions.InstanceNotFoundException;
import com.k2view.cdbms.lut.DbInterface;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.logging.LogEntry;
import com.k2view.cdbms.shared.logging.MsgId;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.out;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.type;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.mtable.MTable;
import com.k2view.fabric.common.mtable.MTables;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.management.RuntimeErrorException;

import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.DecisionFunction;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.RootFunction;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_BATCH_LIMIT;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_TASK_ID;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_PARAMETERS_SEPARATOR;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked", "rawtypes"})
public class SharedLogic {
 public static String TDMDB_SCHEMA;
    static {
        try {
            TDMDB_SCHEMA = fabric().fetch("Broadway TDM.getTDMDBSchema").firstValue().toString();
        } catch (Exception e) {
            log.error("Failed to fetch TDMDB schema", e);
        }
    }	
	public static final String TDM = "TDM";
	public static final String TASKS = "TASKS";
	public static final String TASK_EXECUTION_LIST = "task_execution_list";
	public static final String TASK_REF_TABLES = "TASK_REF_TABLES";
	public static final String PRODUCT_LOGICAL_UNITS = "product_logical_units";
	public static final String TASK_REF_EXE_STATS = "TASK_REF_EXE_STATS";
	public static final String TASKS_LOGICAL_UNITS = "tasks_logical_units";
	public static final String PENDING = "pending";
	public static final String RUNNING = "running";
	public static final String WAITING = "waiting";
	public static final String STOPPED = "stopped";
	public static final String RESUME = "resume";

	public static final String FAILED = "failed";
	public static final String COMPLETED = "completed";
	public static final String GET_CHILDREN_SQL = "WITH RECURSIVE children AS ( " +
			"SELECT lu_name,lu_id,lu_parent_id,lu_parent_name FROM product_logical_units WHERE lu_name=? and be_id=? " +
			"UNION ALL SELECT a.lu_name, a.lu_id, a.lu_parent_id,a.lu_parent_name " +
			"FROM product_logical_units a " +
			"INNER JOIN children b ON a.lu_parent_id = b.lu_id) " +
			"SELECT  string_agg('''' ||  unnest || '''' , ',') FROM children ,unnest(string_to_array(children.lu_name, ',')); ";
	public static final String PARENTS_SQL = "SELECT lu_name  FROM product_logical_units WHERE be_id=? AND lu_parent_id is null";

	@desc("New function for TDM 5.1- this function is called if the open and close separators are populated for the IID.\r\nThe function returns the IID without the separators + the end position of the IID (including the separators) in the UID")
	@out(name = "res", type = Object[].class, desc = "")
	public static Object[] fnRemoveSeparatorsFromIID(String i_UID, String openSeparator, String closeSeprator) {
		String instanceID = "";
		int endOfIidIndex = 0;

		if(openSeparator != null && !openSeparator.equals(""))
		{
			int indexOfOpenSeparator = i_UID.indexOf(openSeparator);
			int indexOfCloseSeparator = i_UID.indexOf(closeSeprator);
			int startIndex = indexOfOpenSeparator + openSeparator.length();

			endOfIidIndex= indexOfCloseSeparator + closeSeprator.length();
			instanceID= i_UID.substring(startIndex,indexOfCloseSeparator);
		}
		return new Object[]{instanceID, endOfIidIndex};
	}

	@desc("output contains Object array \r\n" +
			"position 0 - k2_tdm_eid \r\n" +
			"position 1 - source_env\r\n" +
			"position 2 - instance ID itself\r\n" +
			"position 3 - version task execution id ( in case of versioning )\r\n" +
			"The instance id may be in the following formats : \r\n" +
			"_dev_<entity_id>\r\n" +
			"_dev_<entity_id>_<version_task_execution_id>\r\n" +
			"<environment>_<entity_id>\r\n" +
			"<environment>_<entity_id>_<version_task_execution_id>")
	@out(name = "rs", type = Object[].class, desc = "")
	public static Object[] fnValidateNdGetInstance() throws Exception {
		String origUid = getInstanceID();

		// TDM 7.1 - fix- get out the clone id before the split. For example: SRC_66#params#{"clone_id"=1}|//

		Object[] splitCloneId = origUid.split("#params#");
		origUid = "" + splitCloneId[0];

		// end of Fix

		Object[] splitIID = fnSplitUID(origUid);

		return new Object[]{origUid, splitIID[1], splitIID[0], splitIID[2]};
	}


	@type(RootFunction)
	@out(name = "k2_tdm_eid", type = String.class, desc = "")
	@out(name = "source_env", type = String.class, desc = "")
	@out(name = "IID", type = String.class, desc = "")
	@out(name = "task_execution_id", type = String.class, desc = "")
	public static void fnRtK2TDMRoot(String TDM_INSTANCE_ID) throws Exception {
		UserCode.yield(fnValidateNdGetInstance());
	}

	public static void fnEnrichmentLuParams() throws Exception {
		//*****************************************************//
		//    Function used to update the table LU_PARAMS      //
		//                                                     //
		//This function reads the parameters                   //
		//configured in the Mtable -                           //
		//execute the query associated -                       //
		//and finally run and update statement into LU_PARAMS  //
		//                                                     //
		//*****************************************************//
		String sourceEnvName = "" + fabric().fetch("set TDM_SOURCE_ENVIRONMENT_NAME").firstValue();

		String luName = ("" + getLuType().luName);
		fnInitiateLuParams(luName,null,false);
	}

    public static void fnGenerationLuParams(String luName, String iid) throws Exception {
		fnInitiateLuParams(luName,iid,true);
	}
	public static void fnInitiateLuParams( String luName, String iid,Boolean isGenAITask) throws Exception {
		//*****************************************************//
		//    Function used to update the table LU_PARAMS      //
		//                                                     //
		//This function reads the parameters                   //
		//configured in the Mtable -                           //
		//execute the query associated -                       //
		//and finally run and update statement into LU_PARAMS  //
		//                                                     //
		//*****************************************************//
        // TDM9.1 introducing params coupling no need to populate nor create _params tables//
        Boolean paramCoupling = isParamsCoupling();
		Db ciTDM = db(TDM);
		Set<String> pgColsList = new HashSet<>();
		Set<String> luColsList = new HashSet<>();
		String tblName = luName.toLowerCase() + "_params";
		String tblNameFabric = luName + ".LU_PARAMS";
		
		String SchemaName = luName.toLowerCase();
		String prefix = "";
		String stringInsertFabricLuParam = "INSERT OR REPLACE INTO " + tblNameFabric + " (ROOT_LU_NAME, ROOT_IID ,ENTITY_ID, SOURCE_ENVIRONMENT, PARAMS_JSON) " +
				" VALUES(?, ?, ?, ?, ?)";

		//log.info("fnEnrichmentLuParams is Starting - " + luName);

        // If running from Studio, the debug will be enabled
        if ("true".equalsIgnoreCase(getGlobal("TDM_DEBUG_MODE", TDM) )) {
            fabric().execute("set DEBUG_MODE=true");
        }
        //log.info("fnEnrichmentLuParams inDebug22 - " + inDebugMode());

        //In case of debug mode, create the lu params table if it does not exist or update it if required
        if (!paramCoupling ) {
            fnCreateUpdateLUParams(luName);
        }
		//TDM 8.1 transaltion trnLuParams is coverted to Mtable {LuName}LuParams
		// Reading the mTable data and gettig the queries

		String broadwayCommand = "broadway TDM.luParamsLookup" + " luName=" + luName + " RESULT_STRUCTURE=CURSOR";
		Db.Rows paramData = fabric().fetch(broadwayCommand);

		boolean tdmLuParamAltered = false;
		ciTDM.beginTransaction();

        StringBuilder sqlUpdateTDM = new StringBuilder().append(" ON CONFLICT ON CONSTRAINT " + tblName + "_pkey Do update set ");
		StringBuilder sqlInsertTDM = new StringBuilder().append("insert into " + TDMDB_SCHEMA + "." + tblName + "(");
		StringBuilder sqlInsertTDMBind = new StringBuilder().append(" values (");
		ArrayList params = new ArrayList<>();
		ArrayList<String> luColsArray = new ArrayList<>();
		Object[] insRs;
		int i = 0;

        //TDM 9.1 - Params Coupling
        Boolean isVersionTask = Boolean.parseBoolean(fabric().fetch("set TDM_DATAFLUX_TASK").firstValue().toString());

        if (isVersionTask) {
            return;
        }

		if(!isGenAITask){
			iid = getInstanceID();
		}else{
            fabric().execute("set sync off");
			fabric().execute("get ?.?",luName,iid);
		}

		insRs = fnSplitUID(iid);
        Object entityId = insRs[0];
        Object sourceEnv = insRs[1];

        if (paramData.resultSet() != null) {

            prefix = "";

            for (Db.Row indx : paramData) {
                String luParamColName = indx.get("COLUMN_NAME").toString().trim();
                String s = luName.toUpperCase() + "." + luParamColName.toUpperCase();
                luColsList.add(s);
                luColsArray.add(i, s);
                String columnName = "\"" + luName.toUpperCase() + "." + luParamColName.toUpperCase() + "\"";

                //Building the update statement +Execution of the query
                sqlUpdateTDM.append(prefix + columnName + " = ?");
                sqlInsertTDM.append(prefix + columnName);
                sqlInsertTDMBind.append(prefix + " ? ");

                StringBuilder values = new StringBuilder();
                String sql = indx.get("SQL").toString();

                //Check if SQL query contains distinct and add it if not
                if (!sql.contains("distinct")) {
                    sql = sql.replace("select", "select distinct");
                }
                Object[] valuesArr = null;
                Db.Rows rs1 = null;
                rs1 = ludb().fetch(sql);
                values.append("{");
                for (Db.Row row : rs1) {
                    //Skip null values
                    if (row.cell(0) != null) {
                        String val = "" + row.cell(0);
                        val = val.replaceAll("[\\\\\"\'\n\r\t\0\f\b]", "\\\\$0");
                        values.append("\"" + val + "\",");
                    }
                }

                //Check if the last element is a comma and remove it
                if (values.lastIndexOf(",") == values.length() - 1) {
                    values.deleteCharAt(values.lastIndexOf(","));
                }
                values.append("}");

                //If no values, set NULL
                if (values.toString().equals("{}")) {
                    params.add(i, null);
                } else {
                    params.add(i, values.toString());
                }
                i++;
                prefix = ",";
                rs1.close();
            }
        }

        if (paramData != null) {
            paramData.close();
        }

        //TDM 8.1 - Get the ROOT_IID
        String rootIid = "" + fabric().fetch("set ROOT_IID").firstValue();
        String rootLuName = "" + fabric().fetch("set ROOT_LU_NAME").firstValue();

        if (luColsList.size() > 0) {
            Object[] finParams = new Object[params.size() * 2 + 4];

            for (int j = 0; j < params.size(); j++) {
                finParams[j] = params.get(j);
            }
            finParams[params.size()] = rootLuName;
            finParams[params.size() + 1] = rootIid;
            finParams[params.size() + 2] = entityId;
            finParams[params.size() + 3] = sourceEnv;


            for (i = 0; i < params.size(); i++) {
                finParams[params.size() + 4 + i] = params.get(i);
            }

            // add a stringInsertFabricLuParam to insert the columns without the concatenation of the lu name
            sqlInsertTDM.append(", root_lu_name, root_iid, entity_id, source_environment) ");
            sqlInsertTDMBind.append(", ?, ?, ?, ?) ");
            //log.info("sqlInsertTDM: " + sqlInsertTDM);
            if(!paramCoupling){
                ciTDM.execute(sqlInsertTDM.toString() + sqlInsertTDMBind.toString() + sqlUpdateTDM.toString(), finParams);
            }
            //TDM 8.1 - The LU Param Table will hold all the fields in one Json field
            String paramsJson = fabric().fetch("broadway TDM.CreateJson fieldsNames=?, fieldsValues=?", luColsArray, params).firstValue().toString();
            fabric().execute(stringInsertFabricLuParam, rootLuName, rootIid,  sourceEnv, entityId, paramsJson);
        } else {//no parameters defined - inserting only key fields
            Object[] bind_for_no_params = new Object[4];
            bind_for_no_params[0] = entityId;
            bind_for_no_params[1] = sourceEnv;
            bind_for_no_params[2] = rootIid;
            bind_for_no_params[3] = rootLuName;
            // TALI- fix- 2-Dec-18- add on conflict on constraint do nothing to avoid a violation of a PK if the entity already exists in the params table
            if(!paramCoupling){
            ciTDM.execute("insert into " + TDMDB_SCHEMA + "." + tblName + " (ENTITY_ID, SOURCE_ENVIRONMENT, ROOT_IID, ROOT_LU_NAME) values (?,?,?,?)" +
                    " ON CONFLICT ON CONSTRAINT " + tblName + "_pkey DO NOTHING", bind_for_no_params);
            }
            // Tali- TDM 5.5- fix- add concatenate the lu_name to the table name
            fabric().execute("insert or replace into " + tblNameFabric + " (ENTITY_ID, SOURCE_ENVIRONMENT, ROOT_IID, ROOT_LU_NAME) values (?,?,?,?)", bind_for_no_params);
        }

        ciTDM.commit();

        //log.info("fnEnrichmentLuParams IS DONE");
    
	}
    
    public static Map<String,Map<String,Object>> fnUpdateDistinctFieldData(String columnName,String columnType, Map<String,Map<String,Object>> distinctTable,
																		   HashSet<String> newValuesSet) {
		Long maxNumOfValues = Long.parseLong(getGlobal("COMBO_MAX_COUNT", "TDM"));
        Map<String, Object> currFieldData = new HashMap<>();
		if (distinctTable.containsKey("\"" + columnName + "\"")) {
			//log.info("fnUpdateDistinctFieldData - Found: " + columnName);
			currFieldData = distinctTable.get("\"" + columnName + "\"");
			if (Long.parseLong(currFieldData.get("numberOfValues").toString()) <= maxNumOfValues) {
				HashSet <String> curreValues = (HashSet <String>)currFieldData.get("fieldValues");
				if (newValuesSet.size() +  curreValues.size() >= maxNumOfValues + 1) {
					currFieldData.put("numberOfValues", maxNumOfValues + 1);
					currFieldData.put("fieldValues", new HashSet<String>());
				} else {
					if (newValuesSet != null && newValuesSet.size() > 0) {
						curreValues.addAll(newValuesSet);
					}

					currFieldData.put("numberOfValues", curreValues.size());
					currFieldData.put("fieldValues", curreValues);
				}

			}

			if(Boolean.parseBoolean(currFieldData.get("isNumeric").toString())) {
				Map<String, String> currMinMax = new HashMap<>();
				currMinMax.put("MIN",currFieldData.get("minValue").toString());
				currMinMax.put("MAX",currFieldData.get("maxValue").toString());

				currMinMax = fnGetMinMaxValues(newValuesSet, currMinMax);

				currFieldData.put("minValue", currMinMax.get("MIN"));
				currFieldData.put("maxValue", currMinMax.get("MAX"));
				if ("\\N".equals(currMinMax.get("MIN"))) {
					currFieldData.put("isNumeric", "false");
				}
			}
		} else {
			//log.info("fnUpdateDistinctFieldData - Not Found: " + columnName);
			currFieldData = new HashMap<>();
			if (newValuesSet == null || newValuesSet.size() == 0) {
				currFieldData.put("numberOfValues", 0);
				currFieldData.put("fieldValues", new HashSet<String>());
				currFieldData.put("isNumeric", true);
				currFieldData.put("minValue", "\\N");
				currFieldData.put("maxValue", "\\N");
			} else {
				if (newValuesSet.size() >= maxNumOfValues + 1) {
					currFieldData.put("numberOfValues", maxNumOfValues + 1);
					currFieldData.put("fieldValues", new HashSet<String>());
				} else {

					currFieldData.put("numberOfValues", newValuesSet.size());
					currFieldData.put("fieldValues", newValuesSet);
				}
				currFieldData.put("isNumeric", true);
				Map<String, String> currMinMax = new HashMap<>();

				currMinMax.put("MIN", "\\N");
				currMinMax.put("MAX","\\N");
				currMinMax = fnGetMinMaxValues(newValuesSet, currMinMax);

				currFieldData.put("minValue", currMinMax.get("MIN"));
				currFieldData.put("maxValue", currMinMax.get("MAX"));
				if ("\\N".equals(currMinMax.get("MIN"))) {
					currFieldData.put("isNumeric", "false");
				}
			}
			currFieldData.put("newField", "true");

		}
        if("".equalsIgnoreCase(columnType)){
            if("true".equalsIgnoreCase(currFieldData.get("isNumeric").toString())){
                columnType="INTEGER";
            }else{
                columnType="TEXT";
            }
        }
        currFieldData.put("fieldType", columnType);
		distinctTable.put("\"" + columnName + "\"", currFieldData);
		//log.info("Finished fnUpdateDistinctFieldData");
		return distinctTable;
	}

	private static Map<String, String> fnGetMinMaxValues(HashSet<String> columnDistinctValues, Map<String, String> currMinMax) {

		String min = (currMinMax == null || "\\N".equals(currMinMax.get("MIN"))) ? null : currMinMax.get("MIN");
		String max = (currMinMax == null || "\\N".equals(currMinMax.get("MAX"))) ? null : currMinMax.get("MAX");

		for (String value : columnDistinctValues) {
			Integer intValue = null;
			Long longValue = null;
			Double doubleValue = null;
			value = value.replace("\"", "");
 			if (value.startsWith("0")) {
                currMinMax.put("MIN", "\\N");
                currMinMax.put("MAX", "\\N");
                return currMinMax;
            }
			try {
				intValue = Integer.parseInt(value);
				if (min == null || intValue < Integer.parseInt(min)) {
					min = value;
				}
				if (max == null || intValue > Integer.parseInt(max)) {
					max = value;
				}
			} catch (NumberFormatException e) {
				// Do nothing
			}
			if (intValue == null) {
				try {
					longValue = Long.parseLong(value);
					if (min == null || longValue <Long.parseLong(min)) {
						min = value;
					}
					if (max == null || longValue > Long.parseLong(max)) {
						max = value;
					}

				} catch (NumberFormatException e) {
					// Do nothing
				}
			}

			if (intValue == null && longValue == null) {
				try {
					doubleValue = Double.parseDouble(value);
					if (min == null || doubleValue < Double.parseDouble(min)) {
						min = value;
					}
					if (max == null || doubleValue > Double.parseDouble(max)) {
						max = value;
					}
				} catch (NumberFormatException e) {
					// Do nothing
				}
				if (intValue == null && longValue == null && doubleValue == null) {
					currMinMax.put("MIN", "\\N");
					currMinMax.put("MAX", "\\N");
					return currMinMax;
				}
			}
		}

		currMinMax.put("MIN", min);
		currMinMax.put("MAX", max);

		return currMinMax;
	}

	public static void fnEnrichmentChildLink() throws Exception {
		//TDM 8.1 trnChildLink is moved to TDMDB
		Db ciTDM = db(TDM);
		String uid = getInstanceID();
		//log.info("Running - fnEnrichmentChildLink for instance: " + uid);
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        
        // If running from Studio, the debug will be enabled
        if ("true".equalsIgnoreCase(getGlobal("TDM_DEBUG_MODE", TDM) )) {
            fabric().execute("set DEBUG_MODE=true");
        }
		//TDM 7.2 - divide the uid to instance Id and Parameters if the parameters exist.
		Object[] splitCloneId = uid.split("#params#");

		uid = "" + splitCloneId[0];
		String params = "";

		Object[] splitUID = fnSplitUID(uid);
		String instanceId = "" + splitUID[0];
		String srcEnv = "" + splitUID[1];
		//TDM 6.0 - VERSION_NAME and VERSION_DATETIME are part of the new Primary key
		// TDM 9.0 - VERSION EXECUTION ID is used instead of datetime and name
		String verExeID = "" + splitUID[2];
		String taskExecID = "" + fabric().fetch("set TDM_TASK_EXE_ID").firstValue();

		//String versionExeID = "" + ciTDM.fetch("select version_task_execution_id from " +TDMDB_SCHEMA +".task_execution_list where task_execution_id=?", taskExecID).firstValue();
        String versionExeID = "" + fabric().fetch("set TDM_VERSION_TASK_EXECUTION_ID").firstValue();

		//log.info("fnEnrichmentChildLink - srcEnv: " + srcEnv + ", instanceId: "  + instanceId + ", verName: " + verName + ", verDateTime: " + verDateTime);
		String parentLU = getLuType().luName;
		Db.Rows childEIDs = null;
		Db.Rows childTarEIDs = null;
		//Get the LU Name, as the tdm_lu_type_relation_eid is part of more than one LU
		String tableName = parentLU + ".tdm_lu_type_relation_eid";
		String tableNameTar = parentLU + ".tdm_lu_type_rel_tar_eid";
		//log.info("fnEnrichmentChildLink - Fabric Table: " + tableName);

        Db.Rows rows = null;
		try {
			ciTDM.beginTransaction();
			//TALI- Fix ticket #9523- delete the parent IID records, if exist, before the insert
			// TDM 6.0 - VERSION_NAME and VERSION_DATETIME are part of the new Primary key and are added to the where clause
			String DELETE_SQL = "delete from " + TDMDB_SCHEMA + ".tdm_lu_type_relation_eid where source_env = ? and lu_type_1 = ? and lu_type1_eid = ? and lu_type_2 = ? and version_task_execution_id = ? " ;

			//TDM 7 - Handle TDM_LU_TYPE_REL_TAR_EID table
			String DELETE_TAR_SQL = "delete from " + TDMDB_SCHEMA + ".tdm_lu_type_rel_tar_eid where target_env = ? and lu_type_1 = ? and lu_type1_eid = ? and lu_type_2 = ?";
			String targetEnv = "" + ludb().fetch("SET " + parentLU + ".TDM_TAR_ENV_NAME").firstValue();

			Map<String,Object> childLuInputs = new HashMap<>();
			childLuInputs.put("parent_lu",parentLU);
			List<Map<String, Object>> childLink=  MtableLookup("ChildLink",childLuInputs, MTable.Feature.caseInsensitive);
			for (Map<String, Object> r : childLink) {
                String key = "" + r.get("child_lu");
                String sqlSrc = "" + r.get("child_lu_eid_sql");
                String sqlTar = "" + r.get("child_lu_tar_eid_sql");

                // TDM 7.3 - 17/01/22 - Check if the child LU is part of the task, if it is not part of the task no need to populate its data

                String validateLuSql = "SELECT count(1) " +
                        "FROM " + TDMDB_SCHEMA + ".task_execution_list t, " +
                        TDMDB_SCHEMA + ".product_logical_units parent, " + TDMDB_SCHEMA + ".product_logical_units child " +
                        "WHERE t.task_Execution_id = ? and t.be_id  = parent.be_id and parent.lu_id = t.parent_lu_id " +
                        "and parent.lu_name = ? and t.be_id = child.be_id and child.lu_id = t.lu_id and child.lu_name = ?";

                Long cntLu = (Long) ciTDM.fetch(validateLuSql, taskExecID, parentLU, key).firstValue();
                // The child LU (key) is not part of the task, therefore continue to the next child LU
                if (cntLu == 0) {
                    continue;
                }
                //TDM 7.2 - The tdm_lu_type_relation_eid table should be handled (delete old data and load new data) only if handling one instance (no cloning)
                // or handling the first clone only - in case of cloning there is no need to delete and reinsert the same data per clone, it should
                // be done only once.
                // For tdm_lu_type_rel_tar_eid table, the data should be handled for each clone as it is based on target values.
                ciTDM.execute(DELETE_SQL, srcEnv, parentLU, instanceId, key, versionExeID);

                childEIDs = ludb().fetch(sqlSrc);

                //TDM 7.2 - The tdm_lu_type_relation_eid table should be handled (delete old data and load new data) only if handling one instance (no cloning)
                // or handling the first clone only - in case of cloning there is no need to delete and reinsert the same data per clone, it should
                // be done only once.
                // For tdm_lu_type_rel_tar_eid table, the data should be handled for each clone as it is based on target values.

                for (Db.Row row : childEIDs) {
                    String childId = row.cell(0).toString();

                    //log.info("Adding child record for instance: " + uid + " and LU Type: " + key + ". child instance: " + row.cell(0));

                    // TDM 6.0 - VERSION_NAME and VERSION_DATETIME are added to the table
                    Object[] values;
                    Object[] valuesLUDB;
                    values = new Object[]{srcEnv, parentLU, key, instanceId, childId, "now()", versionExeID, "now()"};

                    String currDatetime = "" + fabric().fetch("select datetime()").firstValue();
                    // TDM 6.0 - VERSION_NAME and VERSION_DATETIME are added to the table
                    String query = "";
                    valuesLUDB = new Object[]{srcEnv, parentLU, key, instanceId, childId, currDatetime, versionExeID};
                    query = "insert or replace into " + tableName + "(source_env,lu_type_1,lu_type_2,lu_type1_eid,lu_type2_eid,creation_date,version_task_execution_id) values(?,?,?,?,?,?,?)";

                    // TDM 6.0 - VERSION_NAME and VERSION_DATETIME are added to the table
                    //log.info("Inserting into Fabric to tdm_lu_type_relation_eid table for lu type: " + key);
                    //TDM9.0 replacing VERSION_NAME and VERSION_DATETIME with VERSION_TASK_EXECUTION_ID
                    ludb().execute(query, valuesLUDB);

                    // TDM 6.0 - VERSION_NAME and VERSION_DATETIME are added to the table

                    //log.info("Inserting into TDM - tdm_lu_type_relation_eid table with version Data for lu type: " + key);
                    ciTDM.execute("insert into " + TDMDB_SCHEMA + ".tdm_lu_type_relation_eid(source_env,lu_type_1,lu_type_2,lu_type1_eid,lu_type2_eid,creation_date,version_task_execution_id) " +
                            "values(?,?,?,?,?,?,?) ON CONFLICT ON CONSTRAINT tdm_lu_type_relation_eid_pk DO update set creation_date = ?", values);
                    
                }
                //TDM 7 - In case of delete from target, the TDM_LU_TYPE_REL_TAR_EID table should be updated
                if (fnDecisionDeleteFromTarget()) {
                    //log.info("TEST- deleting tdm_lu_type_rel_Tar_eid TDM table for parent LU: " + parentLU+ ", Parent ID: " +instanceId + ", and child LU: " + key );
                    ciTDM.execute(DELETE_TAR_SQL, targetEnv, parentLU, instanceId, key);
                    childTarEIDs = ludb().fetch(sqlTar);
                    for (Db.Row row : childTarEIDs) {
                        String currDatetime = "" + fabric().fetch("select datetime()").firstValue();
                        Object[] values = new Object[]{targetEnv, parentLU, key, instanceId, row.cell(0), "now()"};
                        Object[] valuesLUDB = new Object[]{targetEnv, parentLU, key, instanceId, row.cell(0), currDatetime};
                        ludb().execute("insert or replace into " + tableNameTar + "(target_env,lu_type_1,lu_type_2,lu_type1_eid,lu_type2_eid,creation_date) values(?,?,?,?,?,?)", valuesLUDB);
                        ciTDM.execute("insert into " + TDMDB_SCHEMA + ".tdm_lu_type_rel_tar_eid(target_env,lu_type_1,lu_type_2,lu_type1_eid,lu_type2_eid,creation_date) values(?,?,?,?,?,?)", values);
                    }
                }
            }

			if (childEIDs != null) {
				childEIDs.close();
			}

			if (childTarEIDs != null) {
				childTarEIDs.close();
			}
			ciTDM.commit();
			
		} finally {
			if (rows != null) rows.close();
			//log.info("fnEnrichmentChildLink IS DONE");
		}
	}


    @out(name = "res", type = Object[].class, desc = "")
	public static Object[] fnSplitUID(String uid) throws Exception {
		// TDM 5.1- fix the function to support also dataflux mode when the instance id also has version name  +datetime
		// In addition- remove the open and close separators
		String instanceId = "";
		String envName = "";
		String iidOpenSeparator = "";
		String iidCloseSeparator = "";
		// TDM 6.0 - The version name (task name) and version datetime should be returned
		String versionExeID = "";
		String iidSeparator = "";
		String broadwayCommand= "Broadway TDM.getTDMSeparators RESULT_STRUCTURE=ROW";
		Db.Rows rows = fabric().fetch(broadwayCommand);
		for(Db.Row row:rows){
			if ("IID_SEPARATOR".equals(row.get("column"))) {
				iidSeparator = "" + row.get("value");
			}
			if ("IID_OPEN_SEPARATOR".equals(row.get("column"))) {
				iidOpenSeparator = "" + row.get("value");
			}
			if ("IID_CLOSE_SEPARATOR".equals(row.get("column"))) {
				iidCloseSeparator = "" + row.get("value");
			}
		}
        //log.info("fnSplitUID - uid: " + uid);
		if (uid.startsWith("_dev")) {
			uid = uid.replaceFirst("_dev_", "dev_");
			envName = "_dev";
		}


		try {
		
			String[] split_uid = uid.split(iidSeparator);
		
			if(envName.equals(""))
				envName = split_uid[0].toString();
		
			// Check if the open and close separators are populated
			if(iidOpenSeparator.equals("") || uid.indexOf(iidOpenSeparator)== -1 || iidCloseSeparator.equals("") || uid.indexOf(iidCloseSeparator)== -1) // the separators for the IID are not defined in the TDM DB or in the input UID
			{
				//log.info("fnSplitUID - No Separators");
				instanceId = split_uid[1].toString();
				//log.info("fnSplitUID - instanceId: " + instanceId);
				//TDM 6.0, get the version name and datetime
				if (split_uid.length==3) {  //entity in the format of <environment>_<entity_id>_<task_name>_<timestamp>
					versionExeID = split_uid[2].toString();
				}
			}
			else // the open and close separators are populated. Both of them have to be poplated. You cannot populate just one of them
			{
				//log.info("fnSplitUID - There are separators");
				Object[] IID_info = fnRemoveSeparatorsFromIID(uid,  iidOpenSeparator, iidCloseSeparator);
				instanceId = IID_info[0].toString();
				//TDM 6.0, get the version name and datetime
				int pos = Integer.valueOf("" + IID_info[1]);
				// if the uid is longer than the end position of the instance id including the close separator
				if (uid.length() > pos) {
					// Add 1 to jump to the beginning of the task name
					String[] split_version = (uid.substring(pos + 1)).split(iidSeparator);
					versionExeID = split_version[0];
				}
			}
		} catch (Exception e) {
			if (e.getMessage().toString().contains("String index out of range"))
				throw new Exception("Environment Name Is Missing, String index out of range");
			else
				throw new Exception(e.getMessage());
		}
		finally
		{
			// If the input uid does not have _ separator
			if(uid.indexOf(iidSeparator) == -1)
				throw new Exception("Environment Name Is Missing, Underscore not found");
		
		}
		
		//log.info("fnSplitUID - Output: instanceID: " + instanceId + ", envName: " + envName + ", versionName: " + versionName + ", versionDateTime: " + versionDateTime);
		return new Object[]{instanceId, envName, versionExeID};
	}

	@type(RootFunction)
	@out(name = "dummy_output", type = void.class, desc = "")
	public static void fnPop_TDM_LU_TYPE_RELATION_EID(String dummy_input) throws Exception {
		if(1 == 2)UserCode.yield(new Object[] {null});
	}


	@out(name = "refSummaryStats", type = Map.class, desc = "")
	public static Map<String,Object> fnGetReferenceSummaryData(String refTaskExecutionId) throws Exception {
		String selectRefTablesStats = "Select count(*) as cnt, to_char(min(start_time), 'YYYY-MM-DD HH24:MI:SS') as start_time, " +
				"to_char(max(end_time), 'YYYY-MM-DD HH24:MI:SS') as end_time, execution_status, lu_name from " +
				TDMDB_SCHEMA + ".TASK_REF_EXE_STATS es, " + TDMDB_SCHEMA + ".TASK_REF_TABLES rt where task_execution_id = ? " +
				"and es.task_id = rt.task_id and es.task_ref_table_id = rt.task_ref_table_id group by execution_status, lu_name order by lu_name";

		Integer tot_num_tables_to_process=0;
		Integer num_of_processed_ref_tables= 0;
		Integer num_of_copied_ref_tables = 0;
		Integer num_of_failed_ref_tables= 0;
		Integer num_of_processing_tables=0;
		Integer num_of_not_started_tables= 0;

		Integer noOfRecords=0;

		Map <String, Object> refSummaryStatsBuf = new LinkedHashMap<>();

		Db.Rows rows = db(TDM).fetch(selectRefTablesStats, refTaskExecutionId);


		Date calcMinDate = null;
		Date calcMaxDate = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String luName = "";
		String prevLuName = "";

		for (Db.Row refRec : rows) {

			Map <String, Object> refSummaryStats = new LinkedHashMap<>();
			String refStatus = "";
			String min_start_time = "";
			String max_end_time = "";

			if (refRec.get("execution_status") != null)
				refStatus = refRec.get("execution_status").toString();

			if (refRec.get("start_time") != null)
				min_start_time = refRec.get("start_time").toString();

			if (refRec.get("end_time") != null)
				max_end_time = refRec.get("end_time").toString();


			luName = "" + refRec.get("lu_name");

			noOfRecords = Integer.parseInt(refRec.get("cnt").toString());

			//log.info("fnGetReferenceSummaryData - refStatus: " + refStatus + ", noOfRecords: " + noOfRecords);
			if (!prevLuName.equals(luName)) {
				tot_num_tables_to_process=0;
				num_of_processed_ref_tables= 0;
				num_of_copied_ref_tables = 0;
				num_of_failed_ref_tables= 0;
				num_of_processing_tables=0;
				num_of_not_started_tables= 0;
				calcMinDate = null;
				calcMaxDate = null;
				prevLuName = luName;
			}

			Date minDate = null;
			Date maxDate = null;

			if (!min_start_time.equals("")) {
				minDate = formatter.parse(min_start_time);
			}

			if (!max_end_time.equals("")) {
				maxDate = formatter.parse(max_end_time);
			}

			if (calcMinDate == null || (minDate != null && calcMinDate.after(minDate))) // if the minimum date is null or is later the the minimum date of the record- get the minimum date of the record
			{
				calcMinDate = minDate;
			}

			if (calcMaxDate == null || (maxDate != null && calcMaxDate.before(maxDate))) // If the max date is null or earlier than the max date of the record- get the max date of the record
			{
				calcMaxDate = maxDate;
			}

			tot_num_tables_to_process += noOfRecords;

			switch (refStatus) {
				case "completed":
					num_of_copied_ref_tables += noOfRecords;
					num_of_processed_ref_tables += noOfRecords;
					break;
				case "waiting":
				case "pending":
					num_of_not_started_tables += noOfRecords;
					break;

				case "running":
					num_of_processing_tables += noOfRecords;
					break;

				default:
					num_of_failed_ref_tables += noOfRecords;
					num_of_processed_ref_tables += noOfRecords;
			}

			if(calcMinDate != null) {
				refSummaryStats.put("minStartExecutionDate", calcMinDate);
			}
			else
			{
				refSummaryStats.put("minStartExecutionDate", "");
			}

			if(calcMaxDate != null) {
				refSummaryStats.put("maxEndExecutionDate", calcMaxDate);
				//log.info("calcMaxDate: " + calcMaxDate);
			}
			else
			{
				refSummaryStats.put("maxEndExecutionDate", "");
			}

			refSummaryStats.put("totNumOfTablesToProcess", tot_num_tables_to_process);
			refSummaryStats.put("numOfProcessedRefTables", num_of_processed_ref_tables);
			refSummaryStats.put("numOfCopiedRefTables", num_of_copied_ref_tables);
			refSummaryStats.put("numOfFailedRefTables", num_of_failed_ref_tables);
			refSummaryStats.put("numOfProcessingRefTables", num_of_processing_tables);
			refSummaryStats.put("numberOfNotStartedRefTables", num_of_not_started_tables);

			refSummaryStatsBuf.put(luName, refSummaryStats);

		} // end of for loop on the reference tables
		//}// end of if query returns records

		if (rows != null) {
			rows.close();
		}
		return refSummaryStatsBuf;
	}



	@desc("Get the information about the copied and failed entities.\r\n" +
			"This function needs to be used by the new WS- wsGetDetailedListForExtractTask - which replaces wsGetIIDFromK2migrate")
	@out(name = "result", type = Object.class, desc = "")
	public static Object fnGetIIDListForMigration(String fabricExecutionId, Integer entitiesArrarySize) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		Map <String, Map> Map_Outer = new LinkedHashMap<String, Map>();
		int totNumOfCopiedEntities= 0;
		int totNumOfFailedEntities= 0;
		String limiStr = "";
		// Taha - 10-Sep-19 - TDM v6.0 - Check if the given entitiesArrarySize is null, if so it should be ignored and all records returned
		if(entitiesArrarySize == null || entitiesArrarySize == 0)
		{
			entitiesArrarySize = -1;
		} else {
			limiStr = " limit " + entitiesArrarySize;
		}

		List <Object> Copied_entities_list =  new ArrayList<Object>();
		List <Object> Copied_UID_list =  new ArrayList<Object>();

		List <Object> Failed_entities_list =  new ArrayList<Object>();
		List <Object> Failed_UID_list =  new ArrayList<Object>();

		Db.Rows batch_details_completed  = fabric().fetch("batch_details '" + fabricExecutionId + "' lIMIT = " + TDM_BATCH_LIMIT);
		for (Db.Row row : batch_details_completed) {
			Map <Object, Object> innerCopiedEntitiesMap = new HashMap <Object, Object>();
			Map <Object, Object> innerCopiedUIDMap = new HashMap <Object, Object>();
			Map <Object, Object> innerFailedEntitiesMap = new HashMap <Object, Object>();
			Map <Object, Object> innerFailedUIDMap = new HashMap <Object, Object>();

			// TDM 5.1- fix the split of the iid to get the entityId and support configurable separators for entityId
			String IID = "" + row.get("Entity ID");
			String UID[] = IID.split("#params#");
			//log.info("fnGetIIDListForMigration - UID[0]: " + UID[0]);
			Object[] split_iid = fnSplitUID(UID[0]);
			String entityId = split_iid[0].toString();
			//log.info("fnGetIIDListForMigration - entityId: " + entityId);

			String status = "" + row.get("Status");

			if ("COMPLETED".equals(status)) {//Get copied entities ( statuses ADDED/UNCHANGED/UPDATED are considered copied successfully
				totNumOfCopiedEntities++;
				if(entitiesArrarySize == -1 || totNumOfCopiedEntities <= entitiesArrarySize)
				{
					innerCopiedEntitiesMap.put(entityId, entityId);
					innerCopiedUIDMap.put(entityId, UID[0]);
					Copied_entities_list.add((Object)innerCopiedEntitiesMap);
					Copied_UID_list.add((Object)innerCopiedUIDMap);
				}
			} else if ("FAILED".equals(status)) {// Get failed entities
				totNumOfFailedEntities++;
				if(entitiesArrarySize == -1 || totNumOfFailedEntities <= entitiesArrarySize)
				{
					innerFailedEntitiesMap.put(entityId, entityId);
					innerFailedUIDMap.put(entityId, UID[0]);
					Failed_entities_list.add((Object)innerFailedEntitiesMap);
					Failed_UID_list.add((Object)innerFailedUIDMap);
				}
			}
		}

		if (batch_details_completed != null) {
			batch_details_completed.close();
		}

		//add copied entities results to Map_Outer
		LinkedHashMap<String,Object> m1 = new LinkedHashMap<String,Object>();
		//log.info("fnGetIIDListForMigration - Num of Copied Entities: " + totNumOfCopiedEntities);
		m1.put("numOfEntities",totNumOfCopiedEntities);
		m1.put("entitiesList",Copied_entities_list);
		m1.put("UIDList", Copied_UID_list);
		Map_Outer.put("Copied entities per execution",m1);

		//add failed entities results to Map_Outer
		LinkedHashMap<String,Object> m2 = new LinkedHashMap<String,Object>();
		//log.info("fnGetIIDListForMigration - Num of Failed Entities: " + totNumOfFailedEntities);
		m2.put("numOfEntities",totNumOfFailedEntities);
		m2.put("entitiesList",Failed_entities_list);
		m2.put("UIDList",Failed_UID_list);
		Map_Outer.put("Failed entities per execution",m2);

		return Map_Outer;
	}


	@out(name = "refDetailedStats", type = Object.class, desc = "")
	public static Object fnGetReferenceDetailedData(String refTaskExecutionId) throws Exception {
		//ResultSetWrapper rs =null;
		Db.Rows rows = null;

		// Calculate the estimated remaining time for running tasks using the following formula:
		// ((Current time (UTC) – start_time (UTC) )/ number_of_processed_records) * (number_of_records_to_process- number_of_processed_records)

		String selectDetailedRefTablesStats = "SELECT rt.lu_name, es.ref_table_name, es.execution_status, es.start_time, es.end_time, " +
				"CASE WHEN execution_status = 'running' and number_of_processed_records > 0 and coalesce(number_of_records_to_process, 0) > 0 THEN " +
				"to_char(((CURRENT_TIMESTAMP AT TIME ZONE 'UTC' - start_time )/number_of_processed_records) * " +
				"(number_of_records_to_process - number_of_processed_records), 'HH24:MI:SS') " +
				"ELSE '0' END estimated_remaining_duration, coalesce(number_of_records_to_process, 0) as number_of_records_to_process, " +
				"coalesce(number_of_processed_records, 0) as number_of_processed_records, coalesce(error_msg, '') as error_msg " +
				"FROM " + TDMDB_SCHEMA + ".TASK_REF_EXE_STATS es, " + TDMDB_SCHEMA + ".task_ref_tables rt where task_execution_id = ? and es.task_id = rt.task_id " +
				"and es.task_ref_table_id = rt.task_ref_table_id and lower(es.execution_status) != 'pending'";

		//rs = DBQuery("TDM", selectDetailedRefTablesStats, new Object[]{refTaskExecutionId});
		rows = db(TDM).fetch(selectDetailedRefTablesStats, refTaskExecutionId);

		return rows;
	}

	@out(name = "res", type = Object[].class, desc = "")
	public static Object[] fnGetIIdSeparatorsFromTDM() throws Exception {
		String iidOpenSeparator = "";
		String iidCloseSeparator = "";


		// TDM 5.1- get open and close separators for the instanceId. If they exist- get the instanceId according the open and close separators
		//Set the SQL parameter

		String sql = "SELECT UPPER(param_name) as param_name, param_value FROM " + TDMDB_SCHEMA + ".tdm_general_parameters where UPPER(param_name) in ('IID_OPEN_SEPARATOR', 'IID_CLOSE_SEPARATOR')";
		Db.Rows rows = db(TDM).fetch(sql);
		for (Db.Row row:rows){
			if(row.cells()[0].toString().equals("IID_OPEN_SEPARATOR")&& row.cells()[1]!= null && !row.cells()[1].toString().isEmpty() )
			{
				iidOpenSeparator= row.cells()[1].toString();
			}
			else if (row.cells()[0].toString().equals("IID_CLOSE_SEPARATOR") && row.cells()[1]!= null && !row.cells()[1].toString().isEmpty() )
			{
				iidCloseSeparator = row.cells()[1].toString();
			}
		}

		if(rows != null) {
			rows.close();
		}
		return new Object[]{iidOpenSeparator, iidCloseSeparator};
	}

	@out(name = "instanceID", type = String.class, desc = "")
	@out(name = "envName", type = String.class, desc = "")
	@out(name = "versionExeID", type = String.class, desc = "")
	public static Object fnSplitUID2(String uid) throws Exception {
		// TDM 5.1- fix the function to support also dataflux mode when the instance id also has version name  +datetime
		// In addition- remove the open and close separators
		String instanceId = "";
		String envName = "";
		
		String iidOpenSeparator = "";
		String iidCloseSeparator = "";
		// TDM 6.0 - The version name (task name) and version datetime should be returned
		String versionExeID = "";
		String iidSeparator = "";
		String broadwayCommand= "Broadway TDM.getTDMSeparators RESULT_STRUCTURE=ROW";
		Db.Rows rows = fabric().fetch(broadwayCommand);
		for(Db.Row row:rows){
			if ("IID_SEPARATOR".equals(row.get("column"))) {
				iidSeparator = "" + row.get("value");
			}
			if ("IID_OPEN_SEPARATOR".equals(row.get("column"))) {
				iidOpenSeparator = "" + row.get("value");
			}
			if ("IID_CLOSE_SEPARATOR".equals(row.get("column"))) {
				iidCloseSeparator = "" + row.get("value");
			}
		}
        //log.info("fnSplitUID2 - uid: " + uid);
		if (uid.startsWith("_dev")) {
			uid = uid.replaceFirst("_dev_", "dev_");
			envName = "_dev";
		}
		
		try {
		
			String[] split_uid = uid.split(iidSeparator);
		
			if(envName.equals(""))
				envName = split_uid[0].toString();
		
			// Check if the open and close separators are populated
			if(iidOpenSeparator.equals("") || uid.indexOf(iidOpenSeparator)== -1 || iidCloseSeparator.equals("") || uid.indexOf(iidCloseSeparator)== -1) // the separators for the IID are not defined in the TDM DB or in the input UID
			{
				//log.info("fnSplitUID - No Separators");
				instanceId = split_uid[1].toString();
		
				//TDM 6.0, get the version name and datetime
				if (split_uid.length==3) {  //entity in the format of <environment>_<entity_id>_<task_name>_<timestamp>
					versionExeID = split_uid[2].toString();
				}
			}
			else // the open and close separators are populated. Both of them have to be poplated. You cannot populate just one of them
			{
				//log.info("fnSplitUID - There are separators");
				Object[] IID_info = fnRemoveSeparatorsFromIID(uid,  iidOpenSeparator, iidCloseSeparator);
				instanceId = IID_info[0].toString();
				//TDM 6.0, get the version name and datetime
				int pos = Integer.valueOf("" + IID_info[1]);
				// if the uid is longer than the end position of the instance id including the close separator
				if (uid.length() > pos) {
					// Add 1 to jump to the beginning of the task name
					String[] split_version = (uid.substring(pos + 1)).split(iidSeparator);
					versionExeID = split_version[0];
				}
			}
		
		} catch (Exception e) {
			if (e.getMessage().toString().contains("String index out of range"))
				throw new Exception("Environment Name Is Missing, String index out of range");
			else
				throw new Exception(e.getMessage());
		}
		finally
		{
			// If the input uid does not have _ separator
		
			if(uid.indexOf(iidSeparator) == -1)
				throw new Exception("Environment Name Is Missing, Underscore not found");
		
		}
		
		//log.info("fnSplitUID - Output: instanceID: " + instanceId + ", envName: " + envName + ", versionName: " + versionName + "versionDateTime: " + versionDateTime);
		return new Object[]{instanceId, envName, versionExeID};
	}

	@out(name = "luType", type = String.class, desc = "")
	public static String fnGetLuType() throws Exception {
		return getLuType().luName;
	}

	@type(DecisionFunction)
	@out(name = "decision", type = Boolean.class, desc = "")
	public static Boolean fnDecisionInsertToTarget() throws Exception {
		String luName = getLuType().luName;
		if(("" + ludb().fetch("SET " + luName + ".TDM_INSERT_TO_TARGET").firstValue()).equals("true"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@type(DecisionFunction)
	@out(name = "decision", type = Boolean.class, desc = "")
	public static Boolean fnDecisionDeleteFromTarget() throws Exception {
		String luName = getLuType().luName;
		if(("" + ludb().fetch("SET " + luName + ".TDM_DELETE_BEFORE_LOAD").firstValue()).equals("true"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@out(name = "firstSyncInd", type = Boolean.class, desc = "")
	public static Boolean fnIsFirstSync() throws Exception {
		return isFirstSync();
	}

    @type(DecisionFunction)
	@out(name = "decision", type = Boolean.class, desc = "")
    public static Boolean fnDecisionSyncBeIIDS(){
        try {
            Boolean paramsCoupling = isParamsCoupling();
            if(paramsCoupling){
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e.getMessage());
        }    
    }

	@desc("Dummy Root function")
	@type(RootFunction)
	@out(name = "dummy_output", type = void.class, desc = "")
	public static void fnDummyPop(String dummy_input) throws Exception {
		if(1 == 2) UserCode.yield(new Object[] {null});
	}

	public static void fnCheckInsFound() throws Exception {
		// Fix- TDM 7.0.1 - Check the main source LU tables only if the TDM_INSERT_TO_TARGET is true
		String luName = getLuType().luName;

		if (("" + ludb().fetch("SET " + luName + ".TDM_INSERT_TO_TARGET").firstValue()).equals("true")) {

			// Get the list of root tables from the Global
			String[] rootTables = ("" + ludb().fetch("SET " + luName + ".ROOT_TABLE_NAME").firstValue()).split(",");

			// Indicates if any of the root tables have values in it
			boolean instanceExists = false;

			// For every possible root table
			for (String rootTable : rootTables) {
				// If that table has data
				if (!ludb().fetch(String.format("SELECT 1 FROM %s LIMIT 1", rootTable.trim())).firstRow().isEmpty())
					// Indicate the LU is found
					instanceExists = true;
			}


			if (!instanceExists) {
				LogEntry lg = new LogEntry("INSTANCE NOT FOUND!", MsgId.INSTANCE_MISSING);
				lg.luInstance = SharedLogic.fnValidateNdGetInstance()[0] + "";
				lg.luType = getLuType().luName;
				throw new InstanceNotFoundException(lg, null);
			}
		}
	}

	public static void setGlobals(String globals) throws Exception {
		if (!Util.isEmpty(globals)) {
			// Replace gson with k2view's Json
			//Gson gson = new Gson();
			//Map statusData = gson.fromJson(globals, Map.class);
			Map statusData = Json.get().fromJson(globals, Map.class);
			if (!(statusData.isEmpty())) {
				statusData.forEach((key, value) -> {
					try {
						//log.info("setGlobals - setting "+key+"='"+value+ "'");
						fabric().execute("set " + key + "='" + value + "'");
						// TDM 7.1 - Handle Masking and Sequence Broadway Actors flags
						//setBroadwayActorFlags("" + key, "" + value);
					} catch (SQLException e) {
                        log.error("Failed to set Globals due to: " + e.getMessage());
						e.printStackTrace();
					}
				});
			}
		}
	}

	@out(name = "result", type = Integer.class, desc = "")
	public static Integer getRetention(String retentionPeriodType, Float retentionPeriodValue) throws Exception {
		Integer retention_in_seconds ;

		switch (retentionPeriodType) {
			case "Minutes":
				retention_in_seconds = Math.round(retentionPeriodValue * 60);
				break;
			case "Hours":
				retention_in_seconds = Math.round(retentionPeriodValue * 60 * 60);
				break;
			case "Days":
				retention_in_seconds = Math.round(retentionPeriodValue * 60 * 60 * 24);
				break;
			case "Weeks":
				retention_in_seconds = Math.round(retentionPeriodValue * 60 * 60 * 24 * 7);
				break;
			case "Years":
				retention_in_seconds = Math.round(retentionPeriodValue * 60 * 60 * 24 * 365);
				break;
			case "Do Not Delete" :
				retention_in_seconds = -1;
				break;
			default :
				retention_in_seconds = 0;
				break;
		}
		return retention_in_seconds;
	}

	@out(name = "result", type = HashMap.class, desc = "")
	public static HashMap<String,Object> fnReleaseReservedEntity(String entityID, String envID, String beID, String userName) throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		String ErrorCode = "SUCCESS";
		String ErrorMessage = "";

		//log.info("Start fnReleaseReservedEntity for user: " + userName);
		if (userName == null || "".equals(userName)) {
			userName = sessionUser().name();
		}
		String permissionGroup = fnGetUserPermissionGroup(userName);

		String deleteSql = "DELETE FROM " + TDMDB_SCHEMA + ".TDM_RESERVED_ENTITIES WHERE entity_id=? AND be_id =? AND env_id =? ";

		//Sort input list based on Environment
		//log.info("listOfEntities: " + listOfEntities);

		Boolean isOwner = false;
		Boolean isTester = false;

		String returnClause = " returning entity_id";
		//log.info("permissionGroup: " + permissionGroup);

		//New Environment ID, check if the user is allowed to update it
		if ("owner".equalsIgnoreCase(permissionGroup)) {
			//log.info("The user is an owner");
			//Check if the user is an owner of the environment, if not treat the user as tester
			if(fnIsOwner(envID)) {
				isOwner = true;
			} else {
				isOwner = false;
			}
		}
		//If the user is not an admin or owner, then add a condition 
		//to check if the user is the owner of the reservation
		if (!isOwner && !"admin".equalsIgnoreCase(permissionGroup)) {
			//log.info("The user is a tester");
			deleteSql += " AND reserve_owner =? ";
			isTester = true;
		} else {
			isTester = false;
		}

		//log.info("fnReleaseReservedEntity - deleteSql: " + deleteSql + returnClause);
		//Delete record
		String deleteEntityID = "";
		 if (userName.contains("##")) {
            String[] userData = userName.split("##");
            userName = userData[0]; 
        }
		if (isTester) {
			deleteEntityID = "" + db(TDM).fetch(deleteSql + returnClause, entityID, beID, envID, userName).firstValue();
		} else {
			deleteEntityID = "" + db(TDM).fetch(deleteSql + returnClause, entityID, beID, envID).firstValue();
		}

		//if record was not deleted
		if(!entityID.equals(deleteEntityID)) {
			//log.info("fnReleaseReservedEntity - entity is not deleted");
			//In case of a tester, check if the entity is reserved for a different user
			String reserveOwner = "" + db(TDM).fetch(
					"SELECT reserve_owner FROM " + TDMDB_SCHEMA + ".TDM_RESERVED_ENTITIES WHERE entity_id=? AND be_id =? AND env_id =?",
					entityID, beID, envID).firstValue();
			if (reserveOwner == null || "".equals(reserveOwner) ||"null".equals(reserveOwner) ) {
				ErrorMessage = "Entity already Released";
				//log.info("fnReleaseReservedEntity - Entity already Released");
				ErrorCode = "Warning";
			} else if (!reserveOwner.equals(userName)) {
				ErrorMessage = "Entity is reserved to user: " + reserveOwner;
				//log.info("fnReleaseReservedEntity - Entity is reserved to user: " + reserveOwner);
				ErrorCode = "ERROR";
			}
		}
		result.put("id", entityID);
		result.put("ErrorCode", ErrorCode);
		result.put("ErrorMessage", ErrorMessage);

		return result;
	}
	public static Map<String, String> fnGetQueryFormatForMigarteList(
			String ig_sql,String interface_name, String sourceEnvName,String versionInd,String separator, String openSeparator, String closeSeparator ) throws Exception {
		Map<String,String> result = new HashMap<>();
		String sql = ig_sql.replaceAll("\\s+", " ");
		String splitSQL[] = sql.toLowerCase().split("\\s+");
		String qry_entity_col = "";
		for (int i = 0; i < splitSQL.length; i++) {
			if (splitSQL[i].equals("from")) {
				qry_entity_col = splitSQL[i - 1].replaceAll("\\s+", "");
				break;
			}
		}
		// get original SQL statement "select" including the next SQL command like "distinct"
		String select = sql.toLowerCase().substring(0, sql.toLowerCase().indexOf(qry_entity_col));
        
		String sql_part2 = sql.substring(sql.toLowerCase().indexOf(" from ")).replace("'", "''");

		//Using trnMigrateListQueryFormats to support DBs that don't accept || as concatenation operator

		String interface_type = null;
		DbInterface dbObj = com.k2view.cdbms.lut.InterfacesManager.getInstance().getTypedInterface(interface_name, sourceEnvName);
		if (dbObj != null) {
			interface_type = dbObj.jdbcDriver;
		}
		Map<String, Object> migrateListQueryFormatsInput = new HashMap<>();
		migrateListQueryFormatsInput.put("interface_type", interface_type);
		migrateListQueryFormatsInput.put("version_ind", versionInd);
		List<Map<String, Object>> migrateListQueryFormats = MtableLookup("MigrateListQueryFormats", migrateListQueryFormatsInput, MTable.Feature.caseInsensitive);
		String query_format = null;
		for (Map<String, Object> t : migrateListQueryFormats) {
			query_format = "" + t.get("query_format");

		}
		if (!(query_format == null || query_format.isEmpty() || "null".equalsIgnoreCase(query_format))) {
			// TDM 5.1- add the handle of configurable separator for special formats- the separator may need to be added to the trnMigrateListQueryFormats
			String sql_part1 = sql.toLowerCase().substring(0, sql.toLowerCase().indexOf(qry_entity_col));

			if (!openSeparator.equals("") && !closeSeparator.equals("")) // if the open and close separators for the entity id are populated
			{
				StringBuffer sqlStr = new StringBuffer(query_format);
				// Get the substring between source env and entity id

				String formatSeparator = query_format.substring(query_format.indexOf("<source_env_name>") + "<source_env_named>".length(), query_format.indexOf("<entity_id>"));
				formatSeparator = formatSeparator.replaceFirst("'" + separator + "'", "");
				String insertOpenStr = "'" + openSeparator + "'" + formatSeparator;
				String insertCloseStr = formatSeparator + "'" + closeSeparator + "'";
				sqlStr.insert(sqlStr.indexOf("<entity_id>"), insertOpenStr);
				sqlStr.insert(sqlStr.indexOf("<entity_id>") + "<entity_id>".length(), insertCloseStr);
				sql_part1 = select + " " + sqlStr.toString();
			}
			result.put("sql_part1", sql_part1);
			result.put("qry_entity_col", qry_entity_col);
			result.put("sql_part2", sql_part2);
			result.put("list","migrateListFormat");
		}else{
			result.put("sql_part1",select);
			result.put("qry_entity_col", qry_entity_col);
			result.put("sql_part2", sql_part2);
			result.put("list","migrateList");

		}
		return result;
	}
	@out(name = "result", type = Object.class, desc = "")
	public static List<Map<String,Object>> MtableLookup(String name, Map<String,Object> key, MTable.Feature... features) throws Exception {
        MTable mtable;
        try {
		    mtable = MTables.get(name);
        } catch (Exception e) {
            return null;
        }
		return mtable.mapsByKey(key,features);
	}

	public static void MtableRemove(String name) throws Exception {
		MTables.remove(name);
	}

    @out(name = "taskStatus", type = String.class, desc = "")
	public static String fnCheckMedoidTaskStatus(String interface_name, String task_id,String task_type) throws Exception {
        String k2systemSchema = "k2system";
        // Object clusterId = fabric().fetch("clusterid").firstValue();
        // if (clusterId != null && !"".equals(clusterId)) {
        //     k2systemSchema = k2systemSchema + "_" + clusterId;
        // }
		String sql = "SELECT distinct status FROM " + k2systemSchema + ".task_executions where id=? AND task_type=?";
		String taskStatus = "";
		Boolean taskFinish = false;
		
		while(!taskFinish) { 
			taskStatus = (String) db(interface_name).fetch(sql, task_id,task_type).firstValue();
			
			if(taskStatus.equals("DONE") || taskStatus.equals("FAILED") || taskStatus.equals("STOPPED")) {
				taskFinish = true;
			}
            
			Thread.sleep(5000);
		}
		if(taskStatus.equals("DONE")){
			return "COMPLETED";
		}
		return taskStatus;
	}
	@out(name = "taskStatus", type = String.class, desc = "")
	public static Map<String,String> fnUpdateAIProcess( String task_exe_id , String process_id) throws Exception {
		String sql = "Select Distinct num_of_processed_entities as total, num_of_copied_entities as copied, " +
				"num_of_failed_entities as failed from " + TDMDB_SCHEMA + ".task_execution_list where task_execution_id=? and process_id=?";
		Map<String, String> num;
		try {
			num = new HashMap<>();
			Db.Rows rows = db(TDM).fetch(sql, task_exe_id,process_id);
			for (Db.Row row : rows) {
				num.put("total", "" + row.get("total"));
				num.put("copied", "" + row.get("copied"));
				num.put("failed", "" + row.get("failed"));
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return num;
	}

    public static void fnCreateUpdateLUParams(String luName) throws Exception{
        Db ciTDM = db(TDM);
		Set<String> pgColsList = new HashSet<>();
		Set<String> luColsList = new HashSet<>();
		String tblName = luName.toLowerCase() + "_params";
		String tblNameFabric = luName + ".LU_PARAMS";
		String tblInfoSql = "select column_name from INFORMATION_SCHEMA.COLUMNS where table_name = ? and table_schema = ?";
		StringBuilder sbCreStmt = new StringBuilder().append("CREATE TABLE IF NOT EXISTS " + TDMDB_SCHEMA + "." + tblName + "(");
		StringBuilder sbAltStmtAdd = new StringBuilder().append("ALTER TABLE " + TDMDB_SCHEMA + "." + tblName + " ADD COLUMN IF NOT EXISTS ");
		StringBuilder sbAltStmtRem = new StringBuilder().append("ALTER TABLE " + TDMDB_SCHEMA + "." + tblName + " DROP COLUMN IF EXISTS ");
		String prefix = "";
		
		String deleteDistinctValuesSql = "DELETE FROM " + TDMDB_SCHEMA + ".TDM_PARAMS_DISTINCT_VALUES " +
				"WHERE lu_name = ? AND field_name = ANY (string_to_array(?, ','))";
		//log.info("fnCreateUpdateLUParams is Starting - " +  luName);

		//TDM 8.1 transaltion trnLuParams is coverted to Mtable {LuName}LuParams
		// Reading the mTable data and gettig the queries
		String broadwayCommand = "broadway TDM.luParamsLookup" + " luName=" + luName + " RESULT_STRUCTURE=CURSOR";
		Db.Rows paramData = fabric().fetch(broadwayCommand);

		Db.Rows cols = ciTDM.fetch(tblInfoSql, tblName, TDMDB_SCHEMA);
		for (Db.Row row : cols) {
			if (row.get("column_name") != null)
				pgColsList.add((row.get("column_name") + ""));
		}

		if (cols != null) {
			cols.close();
		}

		sbCreStmt.append("ROOT_LU_NAME TEXT,ROOT_IID TEXT,ENTITY_ID TEXT,SOURCE_ENVIRONMENT TEXT");
		ArrayList<String> luColsArray = new ArrayList<>();

        if (paramData.resultSet() != null) {

            prefix = "";
            int i = 0;
            for (Db.Row indx : paramData) {
                String luParamColName = indx.get("COLUMN_NAME").toString().trim();
                String s = luName.toUpperCase() + "." + luParamColName.toUpperCase();
                luColsList.add(s);
                luColsArray.add(i, s);
                String columnName = "\"" + luName.toUpperCase() + "." + luParamColName.toUpperCase() + "\"";
                sbCreStmt.append("," + columnName + " TEXT[] ");

                i++;
				prefix = ",";
            }
        }
        sbCreStmt.append(", CONSTRAINT " + tblName + "_pkey PRIMARY KEY (root_lu_name, root_iid, entity_id, source_environment))");

        if (paramData != null) {
            paramData.close();
        }

        if (pgColsList.size() == 0) {//If its first time
            ciTDM.beginTransaction();// Use transaction to make sure the table is created before trying to load data into it
            ciTDM.execute(sbCreStmt.toString());
            ciTDM.commit();

        } else {
            //Check if PG params table has all the params table columns  - if not --> ADD columns to the PG params table
            prefix = "";
            boolean runCmnd = false;
            for (String mapEnt : luColsList) {
                if (!pgColsList.contains(mapEnt)) {
                    sbAltStmtAdd.append(prefix + "\"" + mapEnt + "\" TEXT[]");
                    prefix = ", ADD COLUMN IF NOT EXISTS ";
                    runCmnd = true;
                }
            }

            if (runCmnd) {
                ciTDM.beginTransaction();// Use transaction to make sure the table is created before trying to load data into it
                ciTDM.execute(sbAltStmtAdd.toString());
                ciTDM.commit();
            }
            //Check if params table is missing columns that PG params table has  - if yes --> drop those PG columns
            prefix = "";
            runCmnd = false;
            String dropFields = "";
            for (String mapEnt : pgColsList) {
                if (!luColsList.contains(mapEnt) && !"root_lu_name".equals(mapEnt) && !"root_iid".equals(mapEnt)
                        && !"entity_id".equals(mapEnt) && !"source_environment".equals(mapEnt)) {
                    sbAltStmtRem.append(prefix + "\"" + mapEnt + "\"");
                    dropFields = dropFields + (prefix == "" ? "" : ",") + "\"" + mapEnt + "\"";
                    prefix = ", DROP COLUMN IF EXISTS ";
                    runCmnd = true;
                }
            }

            if (runCmnd) {
                ciTDM.beginTransaction();// Use transaction to make sure the table is created before trying to load data into it
                ciTDM.execute(sbAltStmtRem.toString());
                //log.info("deleteDistinctValuesSql: " + deleteDistinctValuesSql + ", dropFields: " + dropFields);
                ciTDM.execute(deleteDistinctValuesSql, luName, dropFields);
                ciTDM.commit();
            }
        }
    }
    public static Boolean isParamsCoupling(){
        Boolean paramCoupling = false ; 
        try{
            Object paramsCouplingGlobal = fabric().fetch("set PARAMS_COUPLING").firstValue();
            if(paramsCouplingGlobal==null){
                paramCoupling = Boolean.parseBoolean(db(TDM).fetch("SELECT PARAM_VALUE FROM " +
                TDMDB_SCHEMA + ".TDM_GENERAL_PARAMETERS WHERE PARAM_NAME = 'PARAMS_COUPLING'").firstValue().toString());
            }else{
                paramCoupling=Boolean.parseBoolean(paramsCouplingGlobal.toString());
            }

        }catch(Exception e){
            log.error(e);
            throw new RuntimeException(e.getMessage());
        }
        return paramCoupling ;
    }

   public static void fnRunVerticalChildren(String taskExecutionId, String iid, String syncMode) throws Exception {
        String parentLU = getLuType().luName;

        // TDM 9.2 - support Vertical Execution
        String taskAction = getGlobal("TASK_TYPE", parentLU);
        String versionInd = getGlobal("TDM_DATAFLUX_TASK", parentLU);
        String couplingInd = getGlobal("PARAMS_COUPLING", parentLU);
    
        Object[] insRs = fnSplitUID(iid);
        Object entityId = insRs[0];
        String srcEnv = "" + insRs[1];
    
        Map<String, String> luChildrenMap = new HashMap<>();
    
        Db.Rows rows;
    
        String separator = TDM_PARAMETERS_SEPARATOR;
            
		String sql = "select distinct lu_name, iid from " + TDMDB_SCHEMA + ".task_execution_entities " +
		"where task_execution_id = ? and  parent_lu_name = ? and parent_entity_id = ? order by lu_name";
        rows = db(TDM).fetch(sql, Integer.parseInt(taskExecutionId), parentLU, entityId);
        processRows(rows, luChildrenMap, separator);//Build a map with current child and its IIDS
    
        if (rows != null) {
            rows.close();
        }
    
    
        // Execute the broadwayCommand for each luName and its associated childrenList Loop over the map
        for (Map.Entry<String, String> entry : luChildrenMap.entrySet()) {
            String luName = entry.getKey();
            String childrenList = entry.getValue();
    
            if (!childrenList.isEmpty()) {
                if ("false".equalsIgnoreCase(getGlobal("CHILD_LU_IND").toString())) {
                    fabric().execute("set root_lu_name = " + parentLU);
                    fabric().execute("set root_iid = " + entityId);
                }
                //log.info("Calling ExecuteChildInstances flow");
                String broadwayCommand = "broadway " + luName + ".ExecuteChildInstances instanceList='" + childrenList +
                        "', luName=" + luName + ", taskAction='" + taskAction + "', syncMode='" + syncMode + "', srcEnv=" + srcEnv +
                        ", versionInd=" + versionInd + ", isParamCoupling=" + couplingInd +
                        ", separator='" + separator + "', taskExecutionId=" + taskExecutionId;
                //log.info("Calling ExecuteChildInstances flow - " + broadwayCommand) ;
                fabric().execute(broadwayCommand);
            }
        }
    }
    
    private static void processRows(Db.Rows rows, Map<String, String> luChildrenMap, String separator) throws Exception {
        String currentLuName = "";
		StringBuilder childrenListBuilder = new StringBuilder();

		for (Db.Row row : rows) {
            String luName = row.get("lu_name").toString();
    
            if (!luName.equals(currentLuName)) {
                // Store the current list and start a new one if the child name is different
                if (!currentLuName.isEmpty()) {
                    luChildrenMap.put(currentLuName, childrenListBuilder.toString());
                }
                currentLuName = luName;
                childrenListBuilder.setLength(0); 
            }
            childrenListBuilder.append(row.get("iid")).append(separator);
        }
    
        if (!currentLuName.isEmpty() && childrenListBuilder.length() > 0) {
            // trim
            childrenListBuilder.setLength(childrenListBuilder.length() - separator.length());
            luChildrenMap.put(currentLuName, childrenListBuilder.toString());
        }
    }
}
