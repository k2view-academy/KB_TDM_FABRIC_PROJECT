/////////////////////////////////////////////////////////////////////////
// Project Shared Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common.TDM;

import com.google.gson.Gson;
import com.k2view.cdbms.lut.DbInterface;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.exceptions.InstanceNotFoundException;
import com.k2view.cdbms.shared.logging.LogEntry;
import com.k2view.cdbms.shared.logging.MsgId;
import com.k2view.cdbms.shared.ResultSetWrapper;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.out;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.type;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.RootFunction;
import java.sql.*;
import java.math.*;
import java.io.*;
import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class SharedLogic {

	public static final String TDM = "TDM";
	public static final String REF = "REF";
	public static final String TASKS = "TASKS";
	public static final String TASK_EXECUTION_LIST = "task_execution_list";
	public static final String TASK_REF_TABLES = "TASK_REF_TABLES";
	public static final String PRODUCT_LOGICAL_UNITS = "product_logical_units";
	public static final String TASK_REF_EXE_STATS = "TASK_REF_EXE_STATS";
	public static final String TASKS_LOGICAL_UNITS = "tasks_logical_units";
	public static final String TDM_COPY_REFERENCE = "fnTdmCopyReference";
	public static final String PENDING = "pending";
	public static final String RUNNING = "running";
	public static final String WAITING = "waiting";
	public static final String STOPPED = "stopped";
	public static final String RESUME = "resume";

	public static final String FAILED = "failed";
    public  static final String COMPLETED = "completed";
	public static final String GET_CHILDREN_SQL = "WITH RECURSIVE children AS ( " +
			"SELECT lu_name,lu_id,lu_parent_id,lu_parent_name FROM product_logical_units WHERE lu_name=? and be_id=? " +
			"UNION ALL SELECT a.lu_name, a.lu_id, a.lu_parent_id,a.lu_parent_name " +
			"FROM product_logical_units a " +
			"INNER JOIN children b ON a.lu_parent_id = b.lu_id) " +
			"SELECT  string_agg('''' ||  unnest || '''' , ',') FROM children ,unnest(string_to_array(children.lu_name, ',')); ";
	public static final String PARENTS_SQL = "SELECT lu_name  FROM product_logical_units WHERE be_id=? AND lu_parent_id is null";

	@desc("New function for TDM 5.1- this function is called if the open and close separators are populated for the IID.\r\nThe function returns the IID without the separators + the end position of the IID (including the separators) in the UID")
	@out(name = "res", type = Object[].class, desc = "")
	public static Object[] fnRemoveSeparatorsFromIID(String i_UID, String openSeparator, String closeSeprator) throws Exception {
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
			"position 3 - task name ( in case of versioning )\r\n" +
			"position 4 - timestamp ( in case of versioning )\r\n" +
			"The instance id may be in the following formats : \r\n" +
			"_dev_<entity_id>\r\n" +
			"_dev_<entity_id>_<task_name>_<timestamp>\r\n" +
			"<environment>_<entity_id>\r\n" +
			"<environment>_<entity_id>_<task_name>_<timestamp>")
	@out(name = "rs", type = Object[].class, desc = "")
	public static Object[] fnValidateNdGetInstance() throws Exception {
		String origUid = getInstanceID();
		
		// TDM 7.1 - fix- get out the clone id before the split. For example: SRC_66#params#{"clone_id"=1}|//
		
		Object[] splitCloneId = origUid.split("#params#");
		origUid = "" + splitCloneId[0];
		
		// end of Fix
		
		Object[] splitIID = fnSplitUID(origUid);
		
		return new Object[]{origUid, splitIID[1], splitIID[0], splitIID[2], splitIID[3]};
	}


	@type(RootFunction)
	@out(name = "k2_tdm_eid", type = String.class, desc = "")
	@out(name = "source_env", type = String.class, desc = "")
	@out(name = "IID", type = String.class, desc = "")
	@out(name = "task_name", type = String.class, desc = "")
	@out(name = "timestamp", type = String.class, desc = "")
	public static void fnRtK2TDMRoot(String TDM_INSTANCE_ID) throws Exception {
		yield(fnValidateNdGetInstance());
	}


    @type(RootFunction)
    @out(name = "ENTITY_ID", type = String.class, desc = "")
    public static void fnRootLuParams(String ENTITY_ID) throws Exception {
        if (1 == 2) yield(new Object[]{null});

    }


	public static void fnEnrichmentLuParams() throws Exception {
		//*****************************************************//
		//    Function used to update the table LU_PARAMS      //
		//                                                     //
		//This function reads the parameters                   //
		//configured in the translator trnLuParams -           //
		//execute the query associated -                       //
		//and finally run and update statement into LU_PARAMS  //
		//                                                     //
		//*****************************************************//
		
		Db ciTDM = db("TDM");
		//ResultSetWrapper rs = null;
		Set<String> pgColsList = new HashSet<>();
		Set<String> luColsList = new HashSet<>();
		String tblName = getLuType().luName.toLowerCase() + "_params";
		String tblNameFabric = getLuType().luName + ".LU_PARAMS";
		String tblInfoSql = "select column_name from INFORMATION_SCHEMA.COLUMNS where table_name = ?";
		String luParamTblSql = "DESCRIBE TABLE " + tblNameFabric;
		StringBuilder sbCreStmt = new StringBuilder().append("CREATE TABLE IF NOT EXISTS " + tblName + "(");
		StringBuilder sbAltStmtAdd = new StringBuilder().append("ALTER TABLE " + tblName + " ADD COLUMN IF NOT EXISTS ");
		StringBuilder sbAltStmtRem = new StringBuilder().append("ALTER TABLE " + tblName + " DROP COLUMN IF EXISTS ");
		String LuName = ("" + getLuType().luName).toUpperCase();
		String prefix = "";
		
		Object[] insRs = fnValidateNdGetInstance();
		Db.Rows rows= null;
		
		boolean tdmLuParamAltered = false;
		if (!inDebugMode()) {
			ciTDM.beginTransaction();
		}
		
		//add a drop of the view of the related BEs when the <LU_NAME>_PARAMS TDMDB table is altered
		try{
		    if (!inDebugMode())
		    {
				rows= ciTDM.fetch(tblInfoSql, tblName);
				for (Db.Row row:rows) {
					if(row.cell(0) != null)
						pgColsList.add((row.cell(0) + ""));
				}
				
				rows = ludb().fetch(luParamTblSql);
				for (Db.Row row : rows)
		    	{
					String columnName = "";
					columnName= row.cell(2) +"";
			        if(columnName.toLowerCase().equalsIgnoreCase("ENTITY_ID") ||
			                columnName.toLowerCase().equalsIgnoreCase("SOURCE_ENVIRONMENT"))
			        {
			            sbCreStmt.append(prefix + columnName.toLowerCase() + " TEXT");
			        }else if (!columnName.equals(""))
			        {
			            sbCreStmt.append(prefix + "\"" + LuName + "." + columnName.toUpperCase() + "\" TEXT ");
		        	}
		        	luColsList.add(columnName.toUpperCase());
			        prefix = ",";
		    	}
		    } else //in Debug Mode
			{
				rows = fabric().fetch(luParamTblSql);
			    
				for (Db.Row row : rows)
			    {
					String columnName = "";
					columnName= row.cell(2) + "";
			        if(columnName.toLowerCase().equalsIgnoreCase("ENTITY_ID") ||
			                columnName.toLowerCase().equalsIgnoreCase("SOURCE_ENVIRONMENT"))
			        {
			            sbCreStmt.append(prefix + columnName.toLowerCase() + " TEXT");
			        }else if (!columnName.equals(""))
			        {
			            sbCreStmt.append(prefix + "\"" + LuName + "." + columnName.toUpperCase() + "\" TEXT ");
			        }
			        luColsList.add(columnName.toUpperCase());
			        prefix = ",";
			    }
			}
				
		    sbCreStmt.append(", CONSTRAINT " + tblName + "_pkey PRIMARY KEY (entity_id, source_environment))");
		
		}finally
		{
		   if(rows != null) rows.close();
			
		}
		if(pgColsList.size() == 0){//If its first time
		    if(!inDebugMode()){
				try {
					//log.info("Running Create table: " + sbCreStmt.toString());
					ciTDM.execute(sbCreStmt.toString());
				}catch (Exception e) {
					log.error("fnEnrichmentLuParams - Error Message: " + e.getMessage());
					//if (e.getMessage().toString().contains("duplicate key value violates unique constraint")) {
					//	log.warn("fnEnrichmentLuParams - Paramaters table " + tblName + " already exists, no need to create it");
					//	ciTDM.execute("rollback");
					//}
		    	}
		    }
		}else{
		    //Check if PG params table has all the LUDB params table columns  - if not --> ADD columns to the PG params table
		    prefix = "";
		    boolean runCmnd = false;
		
		    for(String mapEnt : luColsList){
		        if( !mapEnt.equals("ENTITY_ID") && !mapEnt.equals("SOURCE_ENVIRONMENT") ){
		            mapEnt = LuName + "." + mapEnt ;
		        }
		
		        if(!pgColsList.contains(mapEnt) && !"ENTITY_ID".equals(mapEnt) && !"SOURCE_ENVIRONMENT".equals(mapEnt)){
		
		            sbAltStmtAdd.append(prefix + "\"" + mapEnt + "\" TEXT");
		            prefix = ", ADD COLUMN IF NOT EXISTS ";
		            runCmnd = true;
		
		            //set the tdmLuParamAltered indicator to true
		            tdmLuParamAltered= true;
		
		        }
		    }
		
		    if(runCmnd && !inDebugMode()) {
				ciTDM.execute(sbAltStmtAdd.toString());
			}
		    //Check if LUDB params table is missing columns that PG params table has  - if yes --> drop those PG columns
		    prefix = "";
		    runCmnd = false;
		    for(String mapEnt : pgColsList){
		        mapEnt=mapEnt.replace(LuName+".","");
		
		        if(!luColsList.contains(mapEnt) && !"entity_id".equals(mapEnt) && !"source_environment".equals(mapEnt)){
		
		            sbAltStmtRem.append(prefix + "\"" + LuName + "." + mapEnt + "\"");
		            prefix = ", DROP COLUMN IF EXISTS ";
		            runCmnd = true;
		
		            //set the tdmLuParamAltered indicator to true
		            tdmLuParamAltered= true;
		        }
		    }
		
		    if(runCmnd && !inDebugMode()) {
				ciTDM.execute(sbAltStmtRem.toString());
			}
		}
		
		//drop the view from the TDMDB if that LU table was altered
		if(tdmLuParamAltered)
		{
		    // Get the related BEs of the logical unit
		    String sql_be_list = "SELECT be.be_name FROM product_logical_units lu, business_entities be " +
		            "WHERE lower(lu.lu_name) = ? and lu.be_id = be.be_id and be.be_status = 'Active'";
		
		    if (!inDebugMode()) {
		        String env_name = ""+ insRs[1];
		
				Db.Rows rsBeList;
				rsBeList = ciTDM.fetch(sql_be_list, getLuType().luName.toLowerCase());
				for (Db.Row beRow : rsBeList) {
		            // DROP the view for the BE if exists
					String viewName = "lu_relations_" + beRow.cell(0) + "_" + env_name;
					ciTDM.execute("DROP MATERIALIZED VIEW IF EXISTS \"" + viewName + "\"");
		        }
			
				if (rsBeList != null) rsBeList.close();
		    }
		
		} // end  if(tdmLuParamAltered)
		
		// TDM 6.0 - In case of versioning extact, the paramters are not supported, 
		//therefore the LU_PARAM will be populated only if the VERSOIN_NAME from instanceID will be empty
		if (insRs[2] != "") // If version_name is not null then populate the LU_PARAM tables
		{
			// Reading the translation data and gettig th queries
			Map<String,Map<String,String>> data = getTranslationsData("trnLuParams");
			StringBuilder sqlUpdateTDM = new StringBuilder().append(" ON CONFLICT ON CONSTRAINT " + tblName + "_pkey Do update set ");
			StringBuilder sqlInsertTDM = new StringBuilder().append("insert into " + tblName + "(");
			StringBuilder sqlInsertTDMBind = new StringBuilder().append(" values (");
			
			// Tali- 29-Nov-18- add a stringInsertFabricLuParam to insert the columns without the concatenation of the lu name
			StringBuilder stringInsertFabricLuParam = new StringBuilder().append("insert into " + tblName + "(");
			
			//Check if we have elements in the Translator
			if(data.size() > 0){
			
			    //Parameters that will be used for Update
			    Object[] params = new Object[data.size()];
			    //Counter to insert the parameters in the correct position
			    int i=0;
			
			    //Getting the values from the LU
			    prefix = "";
			    for(String index: data.keySet()){
			
			        //String which contains the values returned by the query
			        StringBuilder values = new StringBuilder();
			
			        Map<String,String> valMap = data.get(index);
			        // Tali- 29-Nov-18- add luParamColName for the insert into the ludb
			        String luParamColName = valMap.get("COLUMN_NAME");
			
			        String columnName = "\"" + LuName + "."+ (valMap.get("COLUMN_NAME")).toUpperCase() + "\"";
			        String sql = valMap.get("SQL");
			
			        //Check if SQL query contains distinct and add it if not
			        if (!sql.contains("distinct")){
			            sql = sql.replace("select","select distinct");
			        }
			
			        // Tali- 29-Nov-18- building the insert intot he ludb (fabric)
			        stringInsertFabricLuParam.append(prefix + luParamColName );
			
			        //Building the update statement
			        sqlUpdateTDM.append(prefix + columnName+" = ?");
			        sqlInsertTDM.append(prefix + columnName );
			        //Execution of the query
			        Object[] valuesArr = null;
					Db.Rows rs1 = null;
					rs1 = ludb().fetch(sql);
			        values.append("{");
		
					for (Db.Row row : rs1) {
						//Skip null values
						if (row.cell(0) != null) {
							values.append("\""+row.cell(0) +"\",");
			            }
			        }
			
			        //Check if the last element is a comma and remove it
			        if (values.lastIndexOf(",") == values.length()-1){
			        	values.deleteCharAt(values.lastIndexOf(","));
			        }
			        values.append("}");
			
			        //If no values, set NULL
			        if (values.toString().equals("{}")){
				        params[i] = null;
			        }else{
			            params[i] = values.toString();
			        }
			        sqlInsertTDMBind.append(prefix + " ? ");
			        i++;
		
			        prefix = ",";
					if(rs1 != null) rs1.close();
			    }
			
			    Object[] finParams = new Object[params.length * 2 + 2];
			    Object[] finParamsLu = new Object[params.length + 2];
			    for (i = 0; i < params.length; i++) {
			        finParams[i] = params[i];
			        finParamsLu[i] = params[i];
			    }
			
			    finParams[data.size()] = insRs[2];
			    finParams[data.size() + 1] = insRs[1];
			
			    finParamsLu[data.size()] = insRs[2];
			    finParamsLu[data.size() + 1] = insRs[1];
			
			    for (i = 0; i < params.length; i++) {
			        finParams[params.length + 2 + i] = params[i];
			    }
			
			    // Tali- 29-Nov-18- add a stringInsertFabricLuParam to insert the columns without the concatenation of the lu name
			    stringInsertFabricLuParam.append(", entity_id, source_environment) ");
			
			    sqlInsertTDM.append(", entity_id, source_environment) ");
			    sqlInsertTDMBind.append(", ?, ?) ");
			
			
			    //log.info("Insert into LU params- TDMDB :"+ sqlInsertTDM.toString() + sqlInsertTDMBind.toString() + sqlUpdateTDM.toString());
			
			    if(!inDebugMode()){
					ciTDM.execute(sqlInsertTDM.toString() + sqlInsertTDMBind.toString() + sqlUpdateTDM.toString(),finParams);
			
			    }
				
				// Tali- TDM 5.5- fix- add concatenate the lu_name to the table name
				ludb().execute(stringInsertFabricLuParam.toString().replace(tblName, tblNameFabric).replace("insert", "insert or replace ") + sqlInsertTDMBind.toString(), finParamsLu);
			}
			else {//no parameters defined - inserting only entity_id and source_environment values
			    Object[] bind_for_no_params = new Object[2];
			    bind_for_no_params[0] = insRs[2];
			    bind_for_no_params[1] = insRs[1];
			
			    // TALI- fix- 2-Dec-18- add on conflit on constraint do nothing to avoid a violation of a PK if the entity already exists in the params table
			    if(!inDebugMode()){
					ciTDM.execute("insert into " + tblName + " (ENTITY_ID, SOURCE_ENVIRONMENT) values (?,?)" + " ON CONFLICT ON CONSTRAINT " + tblName + "_pkey DO NOTHING", bind_for_no_params);
			    }
				
				// Tali- TDM 5.5- fix- add concatenate the lu_name to the table name
				ludb().execute("insert or replace into "+ tblNameFabric + " (ENTITY_ID, SOURCE_ENVIRONMENT) values (?,?)", bind_for_no_params);
			}
			
			if (!inDebugMode()) {
				ciTDM.commit();
			}
			// In case JSON is needed
			//Create a stream to hold the output
			//ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//PrintStream ps = new PrintStream(baos);
			// Save the old output
			//PrintStream old = System.out;
			// Use the special stream
			//System.setOut(ps);
			//Convert the ResultSetWrapper to JSON string
			//com.k2view.cdbms.shared.Utils.writeJSON(rs,System.out,true);
			//System.out.flush();
			//System.setOut(old);
			//Get the value from the new ByteArrayOutputStream
		
		}
	}


	public static void fnEnrichmentChildLink() throws Exception {
		/*
		This function will populate the parent-child link table in postgress
		added by: sereenm
		*/
		//trnChildLink translation will include the child LU Name and the sql query which will return the child entities
		Map<String, Map<String, String>> trnChildLinkVals = getTranslationsData("trnChildLink");
		Db ciTDM = db("TDM");
		String uid = getInstanceID();
		//log.info("Running - fnEnrichmentChildLink for instance: " + uid);
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
		
		//TDM 7.2 - divide the uid to instance Id and Parameters if the parameters exist.
		Object[] splitCloneId = uid.split("#params#");
		
		uid = "" + splitCloneId[0];
		String params = "";
		String cloneId = "";
		String cloneIdPattern = "\\{\"clone_id\"=(.*?)\\}";
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(cloneIdPattern);
		
		if (splitCloneId.length > 1) {
			params = "" + splitCloneId[1];
		
			//log.info("fnEnrichmentChildLink - params: " + params);
			
			java.util.regex.Matcher matcher = pattern.matcher(params);
			if (matcher.find()) {
				cloneId = matcher.group(1);
			}
		}
		//log.info("fnEnrichmentChildLink after removing params: " + uid);
		//log.info("fnEnrichmentChildLink - cloneId: " + cloneId);
		//TDM 7.2 - The relation tables should be handled (delete old data and load new data) only if handling one instance (no cloning) 
		// or handling the first clone only - in case of cloning there is no need to delete and reinsert the same data per clone, it should
		// be done only once.
		
		if ("".equals(cloneId) || "1".equals(cloneId)) {
			Object[] splitUID = fnSplitUID(uid);
			String instanceId = "" + splitUID[0];
			String srcEnv = "" + splitUID[1];
			//TDM 6.0 - VERSION_NAME and VERSION_DATETIME are part of the new Primary key
			String verName = "" + splitUID[2];
			String verDateTime = "" + splitUID[3];
			Date verDateTimeDate = null;
			
			if (!"".equals(verDateTime)) {
				verDateTimeDate = formatter.parse(verDateTime);
				verDateTime = sdf.format(verDateTimeDate);
			}
			
			//log.info("fnEnrichmentChildLink - srcEnv: " + srcEnv + ", instanceId: "  + instanceId + ", verName: " + verName + ", verDateTime: " + verDateTime);
			String parentLU = getLuType().luName;
			
			Set<String> keyset = trnChildLinkVals.keySet();
			
			Db.Rows childEIDs = null;
			Db.Rows childTarEIDs = null;
			//Get the LU Name, as the tdm_lu_type_relation_eid is part of more than one LU
			String tableName = parentLU + ".tdm_lu_type_relation_eid";
			String tableNameTar = parentLU + ".tdm_lu_type_rel_tar_eid";
			//log.info("fnEnrichmentChildLink - Fabric Table: " + tableName);
			if (!inDebugMode()) {
				ciTDM.beginTransaction();
			}
			//TALI- Fix ticket #9523- delete the parent IID records, if exist, before the insert
			//log.info("before delete");
			//Add the LU Name to the table name
			// TDM 6.0 - VERSION_NAME and VERSION_DATETIME are part of the new Primary key and are added to the where clause
			//String DELETE_SQL = "delete from tdm_lu_type_relation_eid where source_env = ? and lu_type_1 = ? and lu_type1_eid = ? ";
			String verAddition = verName.equals("") ? "" : "and version_name = ? and version_datetime = ?";
			String DELETE_SQL = "delete from tdm_lu_type_relation_eid where source_env = ? and lu_type_1 = ? and lu_type1_eid = ? " + 
						verAddition;
			
			//TDM 7 - Handle TDM_LU_TYPE_REL_TAR_EID table 
				// Fix the query- add the child LU to the condition
			String DELETE_TAR_SQL = "delete from tdm_lu_type_rel_tar_eid where target_env = ? and lu_type_1 = ? and lu_type1_eid = ? and lu_type_2 = ?";
			String targetEnv = "" + ludb().fetch("SET " + parentLU + ".TDM_TAR_ENV_NAME").firstValue();
			
			String currDate = sdf.format(new java.util.Date());
			
			//log.info("DELETE_SQL: " + DELETE_SQL + " for instance: " + uid);
			if (verName.equals("")) {
				ciTDM.execute(DELETE_SQL,srcEnv, parentLU, instanceId);
			} else {
				ciTDM.execute(DELETE_SQL,srcEnv, parentLU, instanceId, verName, verDateTime);
			}
			
			//log.info("after the delete");
				
			for (String key : keyset) {
				//log.info ("fnEnrichmentChildLink - key: " + key + " for instance: " + uid);
				Map<String, String> mapVal = trnChildLinkVals.get(key);
				String sql = mapVal.get("child_lu_eid_sql");
				String sqlTar = mapVal.get("child_lu_tar_eid_sql");
			    
				//log.info("Getting child EIDS for key: " + key + ", with sql: " + sql);
				childEIDs = ludb().fetch(sql);
				
				for (Db.Row row : childEIDs) {
				
					//log.info("Adding child record for instance: " + uid + " and LU Type: " + key + ". child instance: " + row.cell(0));
			    
					// TDM 6.0 - VERSION_NAME and VERSION_DATETIME are added to the table
					Object[] values;
					Object[] valuesLUDB;
					if (verName.equals("")) {
						values = new Object[]{srcEnv, parentLU, key, instanceId, row.cell(0), currDate, currDate};
					} else {
						values = new Object[]{srcEnv, parentLU, key, instanceId, row.cell(0), currDate, verName, verDateTime, currDate};
					}
			
					// TDM 6.0 - VERSION_NAME and VERSION_DATETIME are added to the table
					if (verName.equals("")) {
						valuesLUDB = new Object[]{srcEnv, parentLU, key, instanceId, row.cell(0), currDate, "''", "''"};
					} else {
						valuesLUDB = new Object[]{srcEnv, parentLU, key, instanceId, row.cell(0), currDate, verName, verDateTime};
					}
						
					// TDM 6.0 - VERSION_NAME and VERSION_DATETIME are added to the table
					//log.info("Inserting into Fabric to tdm_lu_type_relation_eid table for lu type: " + key);
					ludb().execute("insert or replace into " + tableName + "(source_env,lu_type_1,lu_type_2,lu_type1_eid,lu_type2_eid,creation_date,version_name,version_datetime) values(?,?,?,?,?,?,?,?)",valuesLUDB);
			
					if (!inDebugMode()) {
						// TDM 6.0 - VERSION_NAME and VERSION_DATETIME are added to the table
						if (verName.equals("")) {
							//log.info("Inserting into TDM - tdm_lu_type_relation_eid table for lu type: " + key);
							ciTDM.execute("insert into tdm_lu_type_relation_eid(source_env,lu_type_1,lu_type_2,lu_type1_eid,lu_type2_eid,creation_date) values(?,?,?,?,?,?) ON CONFLICT ON CONSTRAINT tdm_lu_type_relation_eid_pk DO update set creation_date = ?", values);
						} else {
							//log.info("Inserting into TDM - tdm_lu_type_relation_eid table with version Data for lu type: " + key);
							ciTDM.execute("insert into tdm_lu_type_relation_eid(source_env,lu_type_1,lu_type_2,lu_type1_eid,lu_type2_eid,creation_date,version_name,version_datetime) " +
								"values(?,?,?,?,?,?,?,?) ON CONFLICT ON CONSTRAINT tdm_lu_type_relation_eid_pk DO update set creation_date = ?", values);
						}
					}
				}
				
				//TDM 7 - In case of delete from target, the TDM_LU_TYPE_REL_TAR_EID table should be updated 
				if (fnDecisionDeleteFromTarget()) {
					//log.info("TEST- deleting tdm_lu_type_rel_Tar_eid TDM table for parent LU: " + parentLU+ ", Parent ID: " +instanceId + ", and child LU: " + key );
					ciTDM.execute(DELETE_TAR_SQL, targetEnv, parentLU, instanceId, key);
					
					childTarEIDs = ludb().fetch(sqlTar);
					for (Db.Row row : childTarEIDs) {
						
			    		Object[] values =  new Object[]{targetEnv, parentLU, key, instanceId, row.cell(0), currDate};
				
						ludb().execute("insert or replace into " + tableNameTar + "(target_env,lu_type_1,lu_type_2,lu_type1_eid,lu_type2_eid,creation_date) values(?,?,?,?,?,?)",values);
			
						if (!inDebugMode()) {
							ciTDM.execute("insert into tdm_lu_type_rel_tar_eid(target_env,lu_type_1,lu_type_2,lu_type1_eid,lu_type2_eid,creation_date) values(?,?,?,?,?,?)", values);
						}
					}
				}
			
			}
		
			if (childEIDs != null) {
				childEIDs.close();	
			}
			if (!inDebugMode()) {
				ciTDM.commit();
			}
		}
	}


	@out(name = "res", type = Object[].class, desc = "")
	public static Object[] fnSplitUID(String uid) throws Exception {
		// TDM 5.1- fix the function to support also dataflux mode when the instance id also has version name  +datetime
		// In addition- remove the open and close separators
		String instanceId = "";
		String envName = "";
		String separator = "";
		String iidOpenSeparator = "";
		String iidCloseSeparator = "";
		// TDM 6.0 - The version name (task name) and version datetime should be returned
		String versionName = "";
		String versionDateTime = "";
				
		String iidSeparator = "" + db("TDM").fetch("Select param_value from tdm_general_parameters where param_name = 'iid_separator'").firstValue();
        //separator = !Util.isEmpty(iidSeparator) ? iidSeparator : "_";
		if ( !Util.isEmpty(iidSeparator) && !"null".equals(iidSeparator) ) {
			separator = iidSeparator;
		} else {
			separator = "_";
		}
		//log.info("fnSplitUID: separator: " + separator);
		// TDM 5.1- get open and close separators for the instanceId. If they exist- get the instanceId according the open and close separators
		//Set the SQL parameter
		
		Object[] separators = fnGetIIdSeparatorsFromTDM();
		//log.info("fnSplitUID - Input UID: " + uid);
		//log.info("fnSplitUID - separators[0]: " + separators[0] + ", separators[1]: " + separators[1]);
		if(separators[0] != null && separators[0].toString() != null && !separators[0].toString().isEmpty() )
			iidOpenSeparator = separators[0].toString();
		
		if(separators[1] != null && separators[1].toString() != null && !separators[1].toString().isEmpty())
			iidCloseSeparator = separators[1].toString();
		
		if (uid.startsWith("_dev")) {
			uid = uid.replaceFirst("_dev_", "dev_");
			envName = "_dev";
		}
			
		try {
		 	
				String[] split_uid = uid.split(separator);
			
				if(envName.equals(""))
		        	envName = split_uid[0].toString();
			
				// Check if the open and close separators are populated
				if(iidOpenSeparator.equals("") || uid.indexOf(iidOpenSeparator)== -1 || iidCloseSeparator.equals("") || uid.indexOf(iidCloseSeparator)== -1) // the separators for the IID are not defined in the TDM DB or in the input UID
				{
					//log.info("fnSplitUID - No Separators");
					instanceId = split_uid[1].toString();
					//log.info("fnSplitUID - instanceId: " + instanceId);
					//TDM 6.0, get the version name and datetime
					if (split_uid.length==4) {  //entity in the format of <environment>_<entity_id>_<task_name>_<timestamp>
						versionName = split_uid[2].toString();
						versionDateTime = split_uid[3].toString();
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
						String[] split_version = (uid.substring(pos + 1)).split(separator);
						versionName = split_version[0];
						versionDateTime = split_version[1];
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
			if(uid.indexOf(separator) == -1)
				 throw new Exception("Environment Name Is Missing, Underscore not found");
		
		}
		
		//log.info("fnSplitUID - Output: instanceID: " + instanceId + ", envName: " + envName + ", versionName: " + versionName + ", versionDateTime: " + versionDateTime);
		return new Object[]{instanceId, envName, versionName, versionDateTime};
	}


	public static void fnPopINSTANCE_TABLE_COUNT() throws Exception {
		String tableName = getLuType().luName + "._k2_objects_info";
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String currDate = sdf.format(new Date());
		String countTable = getLuType().luName + ".INSTANCE_TABLE_COUNT";
		
		// Fetching all LU tables
		Db.Rows luTableList = null;
		try {
			luTableList = fabric().fetch("SELECT distinct table_name FROM " + tableName + " where table_name != ? and table_name != ?",
					"INSTANCE_TABLE_COUNT", "LU_PARAMS");
		
			for (Db.Row row : luTableList) {
		        // Get table's count
				Integer luTableCnt = (Integer) fabric().fetch("select count(*) from " + row.get("table_name")).firstValue();
		
				fabric().execute("insert or replace into " + countTable + " values (?, ?, ?)", getInstanceID(), row.get("table_name"), luTableCnt);
		        if (!inDebugMode()) {
		
					db("TDM").execute("insert into instance_table_count values (?, ?, ?, ?, ?) ON CONFLICT ON CONSTRAINT instance_table_count_pkey DO " +
						"update set sys_update_date = ? , table_count = ? ",
						getLuType().luName, getInstanceID(), currDate, row.cell(0), luTableCnt, currDate, luTableCnt);
				}
		    }
		} finally {
		
			if (luTableList != null) luTableList.close();
		}
	}


    @type(RootFunction)
    @out(name = "output", type = String.class, desc = "")
    public static void fnRootINSTANCE_TABLE_COUNT(String input) throws Exception {
        if (1 == 2) {
            yield(new Object[]{null});
        }

    }


	@type(RootFunction)
	@out(name = "dummy_output", type = String.class, desc = "")
	public static void fnPop_TDM_LU_TYPE_RELATION_EID(String dummy_input) throws Exception {
		if(1 == 2)yield(new Object[] {null});
	}


	@out(name = "refSummaryStats", type = Map.class, desc = "")
	public static Map<String,Object> fnGetReferenceSummaryData(String refTaskExecutionId) throws Exception {
		String selectRefTablesStats = "Select count(*), to_char(min(start_time), 'YYYY-MM-DD HH24:MI:SS'), to_char(max(end_time), 'YYYY-MM-DD HH24:MI:SS'), " +
			"execution_status, lu_name from TASK_REF_EXE_STATS es, TASK_REF_TABLES rt where task_execution_id = ? " + 
			"and es.task_id = rt.task_id and es.task_ref_table_id = rt.task_ref_table_id group by execution_status, lu_name order by lu_name";
		
		//ResultSetWrapper taskExecutionRefTables = null;
		Integer tot_num_tables_to_process=0;
		Integer num_of_processed_ref_tables= 0;
		Integer num_of_copied_ref_tables = 0;
		Integer num_of_failed_ref_tables= 0;
		Integer num_of_processing_tables=0;
		Integer num_of_not_started_tables= 0;
		
		Integer noOfRecords=0;
		
		Map <String, Object> refSummaryStatsBuf = new LinkedHashMap<>();
		
		Db.Rows rows = db("TDM").fetch(selectRefTablesStats, refTaskExecutionId);
		
		
		Date calcMinDate = null;
		Date calcMaxDate = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String luName = "";
		String prevLuName = "";
		//reportUserMessage("in fnGetReferenceSummaryData");
		for (Db.Row refRec : rows) {
		
			Map <String, Object> refSummaryStats = new LinkedHashMap<>();
			String refStatus = "";
			String min_start_time = "";
			String max_end_time = "";
				
			calcMinDate = null;
			calcMaxDate = null;
		
			if (refRec.cells()[3] != null)
				refStatus = refRec.cells()[3].toString();
		
			if (refRec.cells()[1] != null)
				min_start_time = refRec.cells()[1].toString();
		
			if (refRec.cells()[2] != null)
				max_end_time = refRec.cells()[2].toString();
		
		
			luName = "" + refRec.cells()[4];
			
			if (!prevLuName.equals(luName)) {
				tot_num_tables_to_process=0;
				num_of_processed_ref_tables= 0;
				num_of_copied_ref_tables = 0;
				num_of_failed_ref_tables= 0;
				num_of_processing_tables=0;
				num_of_not_started_tables= 0;
				
				prevLuName = luName;
			}
			//reportUserMessage("in fnGetReferenceSummaryData- in the loop2");
		
			Date minDate = null;
			Date maxDate = null;
		
			if (!min_start_time.equals("")) {
				minDate = formatter.parse(min_start_time);
				//reportUserMessage("min start date: " + min_start_time + " minDate: " + minDate);
			}
		
			if (!max_end_time.equals("")) {
				maxDate = formatter.parse(max_end_time);
				//log.info("max_end_time: " + max_end_time);
				//log.info("maxDate: " + maxDate);
			}
			
			if (calcMinDate == null || (minDate != null && calcMinDate.after(minDate))) // if the minimum date is null or is later the the minimum date of the record- get the minimum date of the record
			{
				calcMinDate = minDate;
				//reportUserMessage("minDate " + minDate);
			}
			
			if (calcMaxDate == null || (maxDate != null && calcMaxDate.before(maxDate))) // If the max date is null or earlier than the max date of the record- get the max date of the record
			{
				calcMaxDate = maxDate;
			}
			
			//reportUserMessage("type of  refRec[0]: " + refRec[0].getClass() );
			noOfRecords = Integer.parseInt(refRec.cells()[0].toString());
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
		
		//get entities with COMPLETED status
		
		Db.Rows mig_details = null;
		//Db.Rows mig_details_completed  = fabric().fetch("migrate_details '" + fabricExecutionId + "' STATUS='COMPLETED'");
		// if the cluster ID is define, add it to the name of the keyspace.
		String clusterID = "" + ludb().fetch("clusterid").firstValue();
		
		if (clusterID == null || clusterID.isEmpty()) {
			String getMigDetSql = "select entityid, status from k2batchprocess.batchprocess_entities_info where bid = ? " + limiStr + " ALLOW FILTERING";
			mig_details = db("DB_CASSANDRA").fetch(getMigDetSql, fabricExecutionId);
		} else {
			String getMigDetSql = "select entityid, status from k2batchprocess_" + clusterID + ".batchprocess_entities_info where bid = ? " + limiStr + " ALLOW FILTERING";
			mig_details = db("DB_CASSANDRA").fetch(getMigDetSql, fabricExecutionId);
		}
			
		for (Db.Row row:mig_details) {
			Map <Object, Object> innerCopiedEntitiesMap = new HashMap <Object, Object>();
			Map <Object, Object> innerCopiedUIDMap = new HashMap <Object, Object>();
			Map <Object, Object> innerFailedEntitiesMap = new HashMap <Object, Object>();
			Map <Object, Object> innerFailedUIDMap = new HashMap <Object, Object>();
			
			// TDM 5.1- fix the split of the iid to get the entityId and support configurable separators for entityId
			String IID = "" + row.cell(0);
			String UID[] = IID.split("#params#");
			//log.info("fnGetIIDListForMigration - UID[0]: " + UID[0]);
			Object[] split_iid = fnSplitUID(UID[0]);
			String entityId = split_iid[0].toString();
			//log.info("fnGetIIDListForMigration - entityId: " + entityId);
			
			String status = "" + row.cell(1);
			
			if ("COMPLETED".equals(status)) {//Get copied entities ( statuses ADDED/UNCHANGED/UPDATED are considered copied successfully
				totNumOfCopiedEntities++;
				if(entitiesArrarySize == -1 || totNumOfCopiedEntities <= entitiesArrarySize)
				{
					innerCopiedEntitiesMap.put(entityId, entityId);
					innerCopiedUIDMap.put(entityId, row.cell(0));
					Copied_entities_list.add((Object)innerCopiedEntitiesMap);
					Copied_UID_list.add((Object)innerCopiedUIDMap);
				}
			} else if ("FAILED".equals(status)) {// Get failed entities
				totNumOfFailedEntities++;
				if(entitiesArrarySize == -1 || totNumOfFailedEntities <= entitiesArrarySize)
				{
					innerFailedEntitiesMap.put(entityId, entityId);
					innerFailedUIDMap.put(entityId, row.cell(0));
					Failed_entities_list.add((Object)innerFailedEntitiesMap);
					Failed_UID_list.add((Object)innerFailedUIDMap);
				}
			}
		}
		
		if (mig_details != null) {
			mig_details.close();
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
			"CASE WHEN execution_status = 'running' THEN " +
			"to_char(((CURRENT_TIMESTAMP AT TIME ZONE 'UTC' - start_time )/number_of_processed_records) * (number_of_records_to_process- number_of_processed_records), 'HH24:MI:SS') " + 
			"ELSE '0' END estimated_remaining_duration, number_of_records_to_process, coalesce(number_of_processed_records, 0), coalesce(error_msg, '') " +
			"FROM TASK_REF_EXE_STATS es, task_ref_tables rt where task_execution_id = ? and es.task_id = rt.task_id " +
			"and es.task_ref_table_id = rt.task_ref_table_id"; 
			
			//rs = DBQuery("TDM", selectDetailedRefTablesStats, new Object[]{refTaskExecutionId});
			rows = db("TDM").fetch(selectDetailedRefTablesStats, refTaskExecutionId);
		
		return rows;
	}

	public static void setGlobals(String globals) throws Exception {
		if(!Util.isEmpty(globals)){
			Gson gson = new Gson();
			Map statusData = gson.fromJson(globals, Map.class);
			if(!(statusData.isEmpty())){
				statusData.forEach((key, value) -> {
					try {
						//log.info("setGlobals - setting "+key+"='"+value+ "'");
						fabric().execute( "set "+key+"='"+value+ "'");
						// TDM 7.1 - Handle Masking and Sequence Broadway Actors flags
						setBroadawayActorFlags("" + key, "" + value);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				});
			}
		}
	}


	private static void setBroadawayActorFlags(String key, String value) throws SQLException {
		//TDM 7.1 - Masking and Sequence Broadway Actors have special flags to enable/disable them, and they need
		// to be set based on the input globals of the task

		// Masking Actor - If MASK_FLAG is set to false set the indicator of masking actor to false to suppress the masking in
		// both Load and Extract tasks. If MASK_FLAG is not set or set to true then nothing to do as the Masking is enabled by default.
		if (key.contains("MASK_FLAG") && "0".equals(value)) {
			//log.info("setBroadawayActorFlags - Disabling Masking");
			fabric().execute("set enable_masking = false");
		}
		// Sequence Actor - If TDM_REPLACE_SEQUENCES is set, used its value to disable/enable the sequence actor
		if (key.contains("TDM_REPLACE_SEQUENCES") ) {
			//log.info("setBroadawayActorFlags - Setting Sequence Actor to: " + value);
			fabric().execute("set enable_sequences = ?", value);
		}
	}
	@out(name = "res", type = Object[].class, desc = "")
	public static Object[] fnGetIIdSeparatorsFromTDM() throws Exception {
		String iidOpenSeparator = "";
		String iidCloseSeparator = "";
		
		
		// TDM 5.1- get open and close separators for the instanceId. If they exist- get the instanceId according the open and close separators
		//Set the SQL parameter
		
		String sql = "SELECT UPPER(param_name) as param_name, param_value FROM public.tdm_general_parameters where UPPER(param_name) in ('IID_OPEN_SEPARATOR', 'IID_CLOSE_SEPARATOR')";
		Db.Rows rows = db("TDM").fetch(sql);
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


	public static Integer getRetention(String retentionPeriodType, Float retentionPeriodValue) {
		Integer retention_in_seconds = 0;

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
				retention_in_seconds = Math.round(retentionPeriodValue * 60 * 60 * 24 * 7 * 365);
				break;
		}
		return retention_in_seconds;
	}

	@desc("This function returns the details of the child records of the given entity. It is a recursive function that iterates till the leaves and go back, each child record will be added to the parent's children list.")
	@out(name = "o_childHirarchyData", type = Map.class, desc = "")
	public static Map<String,Object> fnGetChildHierarchy(String i_luName, String i_targetEntityID) throws Exception {
		String entityStatus = "completed";
		LinkedHashMap<String,Object> o_childHirarchyData =  new LinkedHashMap<>();
		LinkedHashMap<String,Object> innerChild =  new LinkedHashMap<>();
		List<Object> childData = new ArrayList<>();
		
		//log.info("fnGetChildHierarchy inputs - Lu Name: " + i_luName + ", target ID: " + i_targetEntityID);
		
		String sqlGetEntityData = "select lu_name luName, target_entity_id targetId, entity_id sourceId, " +
			"execution_status luStatus, parent_lu_name parentLuName, TARGET_PARENT_ID parentTargetId, root_entity_status luStatus from TDM.task_Execution_link_entities " +
			"where lu_name= ? and target_entity_id = ?";
		
		String sqlGetChildren = "select lu_name, target_entity_id from TDM.task_Execution_link_entities " +
			"where parent_lu_name= ? and target_parent_id = ?";
		
		Db.Rows childRecs = fabric().fetch(sqlGetChildren, i_luName, i_targetEntityID);
		
		ArrayList<String[]> childrenRecs = new ArrayList<>();
		
		//Get the list of direct children of the current (input) entity if such exists
		for (Db.Row childRec : childRecs) {
			String[] childInfo = {"" + childRec.cell(0), "" + childRec.cell(1)};
			childrenRecs.add(childInfo);
		}
		
		if (childRecs != null) {
			childRecs.close();
		}
		//log.info("fnGetChildHierarchy - Number of child Recs: " + childrenRecs.size());
		//Loop over the direct children entities and call this function to get for each child it own children
		for (int i=0; i< childrenRecs.size(); i++) {
			
			String[] child = childrenRecs.get(i);
			String childLuName = child[0];
			String childTargetId = child[1];
			
			//log.info("fnGetChildHierarchy - Child Rec: Lu Name: " + childLuName + ", target ID: " + childTargetId);
			if (childLuName != null && !childLuName.isEmpty()) {
				//a recursive call to this function until we get to a leaf entity
				innerChild = (LinkedHashMap<String, Object>)fnGetChildHierarchy(childLuName, childTargetId);
						
				childData.add(innerChild);
			}
		}
		// This point is reached, either the current entity has no children (leaf) or all it direct children (and their own children) were
		// already processed.
		Db.Row entityRec = (fabric().fetch(sqlGetEntityData, i_luName, i_targetEntityID)).firstRow();
		
		o_childHirarchyData.put("luName", "" + entityRec.cell(0));
		o_childHirarchyData.put("targetId", "" + entityRec.cell(1));
		
		//Get instance ID from entity id
		//log.info("fnGetChildHierarchy - entity_id: " + entityRec.cell(2));
		Object[] splitId = fnSplitUID("" + entityRec.cell(2));
		String instanceId = "" + splitId[0];
		o_childHirarchyData.put("sourceId", "" + instanceId);
		
		o_childHirarchyData.put("entityStatus", "" + entityRec.cell(3));
		o_childHirarchyData.put("parentLuName", "" + entityRec.cell(4));
		o_childHirarchyData.put("parentTargetId", "" + entityRec.cell(5));
		
		//log.info("fnGetChildHierarchy - Adding children Data, size: " + childData.size());
		if (childData.size() > 0){
			o_childHirarchyData.put("children", childData);
		}
		
		//log.info("fnGetChildHierarchy - LU status: " + entityStatus);
		o_childHirarchyData.put("luStatus",  "" + entityRec.cell(6));
		
		return o_childHirarchyData;
	}


	@desc("This function returns the details of the ancestors records of the given entity, if such exists. It is a recursive function, that travels until  it reaches the root and goes back. Each Ancestor will get the data of its children and will added it to its record as children data, The final data prepared for the root entity, will be eventually returned from this function")
	@out(name = "o_parentHirarchyData", type = Map.class, desc = "")
	public static Map<String,Object> fnGetParentHierarchy(String i_luName, String i_targetEntityID, Object i_children) throws Exception {
		String entityStatus = "completed";
		LinkedHashMap<String,Object> currentEntity =  new LinkedHashMap<>();
		LinkedHashMap<String,Object> upperParent =  new LinkedHashMap<>();
		
		//LinkedHashMap<String,Object> childrenRecs = new LinkedHashMap<>();
		List<Object> childrenRecs = new ArrayList<>();
		
		if (i_children != null){
			childrenRecs.add(i_children);
		}
		
		//log.info("fnGetParentHierarchy inputs - Lu Name: " + i_luName + ", target ID: " + i_targetEntityID);
		
		String sqlGetEntityData = "select lu_name luName, target_entity_id targetId, entity_id sourceId, " +
			"execution_status entityStatus, parent_lu_name parentLuName, TARGET_PARENT_ID parentTargetId, root_entity_status luStatus " +
			"from TDM.task_Execution_link_entities where lu_name= ? and target_entity_id = ?";
		
		String sqlGetParent = "select parent_lu_name, target_parent_id from TDM.task_Execution_link_entities " +
			"where lu_name= ? and target_entity_id = ? limit 1";
		
		//Get the data of the current (input) entity
		Db.Row entityRec = (fabric().fetch(sqlGetEntityData, i_luName, i_targetEntityID)).firstRow();
		
		currentEntity.put("luName", "" + entityRec.cell(0));
		currentEntity.put("targetId", "" + entityRec.cell(1));
		
		//Get instance ID from entity id
		//log.info("fnGetParentHierarchy - entity_id: " + entityRec.cell(2));
		Object[] splitId = fnSplitUID("" + entityRec.cell(2));
		String instanceId = "" + splitId[0];
		currentEntity.put("sourceId", "" + instanceId);
		
		currentEntity.put("entityStatus", "" + entityRec.cell(3));
		currentEntity.put("parentLuName", "" + entityRec.cell(4));
		currentEntity.put("parentTargetId", "" + entityRec.cell(5));
			
		currentEntity.put("children",childrenRecs);
		
		//log.info("fnGetParentHierarchy - LU status: " + entityStatus);
		currentEntity.put("luStatus", "" + entityRec.cell(6));
		
		String parentLuName = "";
		String parentTargetId = "";
		
		//Get the parent record, as each entity can have only one parent, only one row wil be returned
		Db.Row parentRec = fabric().fetch(sqlGetParent, i_luName, i_targetEntityID).firstRow();
		if (!parentRec.isEmpty()) {
				parentLuName = "" + parentRec.cell(0);
				parentTargetId = "" + parentRec.cell(1);
		}
		//log.info("fnGetParentHierarchy - parent Rec: Lu Name: " + parentLuName + ", Parent target ID: " + parentTargetId);
		
		if ( parentLuName != null && !"".equals(parentLuName) ) {
			
			//log.info("fnGetParentHierarchy - parent Rec: Lu Name: " + parentLuName + ", Parent target ID: " + parentTargetId);
			//a recursive call to this function until we get to a leaf entity
			upperParent = (LinkedHashMap<String, Object>)fnGetParentHierarchy(parentLuName, parentTargetId, currentEntity);
			//log.info("fnGetParentHierarchy - parent Status: " + upperParent.get("luStatus"));
			
		}
		
		//No parent, therefore a root, and return the root data, as it already includes the data of all the children
		if (upperParent == null || upperParent.isEmpty()){
			return currentEntity;
		}
		
		//Return the parent, so eventually return the root record with the whole hierarchy data
		return upperParent;
	}

	@out(name = "result", type = Map.class, desc = "")
	public static Map<String,String> fnMigrateEntitiesForTdmExtract(String luName, String dcName, String sourceEnvName, String taskName, String versionInd, String entitiesList, String retentionPeriodType, Float retentionPeriodValue, String globals, String taskExecutionId, String parentLuName, String versionDateTime, String syncMode) throws Exception {
		if (syncMode != "ON") {
			fabric().execute("SET SYNC " + syncMode);
		}
		
		String entities_list_for_migrate ="";
		String [] entities_list_array = {};
		if (entitiesList != null) {
			entities_list_array = entitiesList.split(",");
		}
		
		//log.info("wsMigrateEntitiesForTdmExtract - lu name: " + luName + ", parent lu name: " + parentLuName);
		String migrateCommand="";
		String migrationInfo ="";
		Long unixTime = System.currentTimeMillis() ;
		Long unixTime_plus_retention;
		
		setGlobals(globals);
		String iidSeparator = "" + db("TDM").fetch("Select param_value from tdm_general_parameters where param_name = 'iid_separator'").firstValue();
		String separator = "";
		if ( !Util.isEmpty(iidSeparator) && !"null".equals(iidSeparator) ) {
					separator = iidSeparator;
			} else {
				separator = "_";
			}
		// TALI- set version_exp_date to null. We cannot set an empty string for timestamp type of PG
		String version_exp_date =null;
		//ResultSetWrapper rs_mig_id = null;
		Db.Rows rs_mig_id = null;
		
		// Tali- return the results in a map
		Map <String, String> migInfo = new LinkedHashMap<String, String>();
		
		// Tali- init timestamp by null
		String timeStamp =null;
		
		// TDM 5.1- support TTL also for regular extract tasks (versioning is false)
		// Check if retention parameters are populated instead of checking the versioning parameter
		
		//Calculate retention date + set TTL
		//if (versionInd.equals("true")) {
		if(retentionPeriodType !=  null && !retentionPeriodType.isEmpty() && retentionPeriodValue != null && retentionPeriodValue > 0)
		{
			//log.info("calculate TTL");
			// Tali- set the datetime only when versionInd is true (backup tasks)
			// TDM 6.0, in case of versionInd = true, the version_datetime will be received as input
			Integer retention_in_seconds = getRetention(retentionPeriodType, retentionPeriodValue);
			if (versionDateTime != null && !versionDateTime.isEmpty()) {
				timeStamp = versionDateTime;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				Date timeStampDate = sdf.parse(timeStamp);
				long millis = timeStampDate.getTime();
				unixTime_plus_retention = ( millis/1000L + retention_in_seconds )*1000;
				version_exp_date = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTime_plus_retention);
			} else {
				timeStamp= new SimpleDateFormat("yyyyMMddHHmmss").format(unixTime);
				unixTime_plus_retention = ( unixTime/1000L + retention_in_seconds )*1000;
				version_exp_date = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTime_plus_retention);
			}
		
		
		
			//log.info("retention_in_seconds: "+ retention_in_seconds + ", version_exp_date: "+ version_exp_date);
			//Set TTL
			//DBExecute(DB_FABRIC,"SET INSTANCE_TTL = " + retention_in_seconds,null);
			ludb().execute("SET INSTANCE_TTL = " + retention_in_seconds);
		
		}
		// end of if input retention parmeters are populated
		
		
		//Execute Migrate
		
		// TDM 5.1- add a support of configurable separator for IID
		
		Object[] iidSeparators = fnGetIIdSeparatorsFromTDM();
		String openSeparator = iidSeparators[0].toString();
		String closeSeparator = iidSeparators[1].toString();
		
		if(entitiesList != null && !entitiesList.isEmpty() && !entitiesList.equals("")) { //Extract task is for listed entities
			for (int i=0; i< entities_list_array.length; i++) {
				String entityId = entities_list_array[i].toString();
				if(!openSeparator.equals(""))
					entityId = openSeparator + entityId;
		
				if(!closeSeparator.equals(""))
					entityId += closeSeparator;
		
				if (versionInd.equals("true")) { //Modify entities to be in the format of <source_env>_<entity_id>_<task_name>_<timestamp>
					entities_list_for_migrate += "'" + sourceEnvName+separator+entityId+separator+taskName+separator+timeStamp+"',";
					//entities_list_for_migrate += "'" + sourceEnvName+"_"+entities_list_array[i]+"_"+taskName+"_"+timeStamp+"',";
				}
				else { // no Versioning needed, entities format :<source_env>_<entity_id>
					entities_list_for_migrate +=  "'" +sourceEnvName+separator+entityId + "',";
					//entities_list_for_migrate +=  "'" +sourceEnvName+"_"+entities_list_array[i] + "',";
				}
			} // end of loop on entity array
		
			//remove last ,
			entities_list_for_migrate = entities_list_for_migrate.substring(0,entities_list_for_migrate.length()-1);
		
			//log.info("open separator: " + openSeparator + ", close separator: " + closeSeparator + " , entities_list_for_migrate : " + entities_list_for_migrate);
		
			// TDM 5.1- add the check of the input DC. Run the migrate with affinity only if the input DC is populated to support a migrate without affinity
			if(dcName!= null && !dcName.isEmpty())
			{
				migrateCommand = "batch "+luName+".("+ entities_list_for_migrate + ") FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH AFFINITY='"+dcName+"' ASYNC=true";
			}
			else // input DC is empty or null
			{
				migrateCommand = "batch "+luName+".("+ entities_list_for_migrate + ") FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH ASYNC=true";
			}
			//log.info("ENTITIES LIST task : " + migrateCommand);
		
		}
		
		else { //Extract task is for all the LU ( according to provided query in translation )
			// TDM 5.1 - add an additional input to the translation- source env
		
			// TDM 6.0 - if parentLuName is populated, then this is a child LU,
			// and the list of entities for migration will be created based on the Parent LU
			//log.info("fnMigrateEntitiesForTdmExtract - parentLuName: " + parentLuName);
			if (parentLuName != null && !parentLuName.equals(""))
			{
				//log.info("fnMigrateEntitiesForTdmExtract, handling child LU: " + luName + " of Parent LU: " +  parentLuName);
				//Get the timestamp from the parent record in task_execution_list
				String sqlGetVersionDateTime = "select to_char(version_datetime, 'yyyyMMddHH24miss') from task_execution_list tel, tasks_logical_units tlu " +
						"where tel.task_execution_id = ? and tel.lu_id = tlu.lu_id and tlu.lu_name = ?";
		
				timeStamp = "" + db("TDM").fetch(sqlGetVersionDateTime, taskExecutionId, parentLuName).firstValue();
		
				String entityIdSelectChildID= "rel.source_env||''" + separator + "''||rel.lu_type2_eid";
				//String entityIdSelectParID = "rel.source_env||''_''||rel.lu_type1_eid";
		
				if (!openSeparator.equals(""))
				{
					entityIdSelectChildID = "rel.source_env||''" + separator + "''||''" + openSeparator + "''||rel.lu_type2_eid";
					//entityIdSelectParID = "rel.source_env||''_''||''" + openSeparator + "''||rel.lu_type1_eid";
				}
		
				if(!closeSeparator.equals(""))
				{
					entityIdSelectChildID += "||''" + closeSeparator + "''";
					//entityIdSelectParID += "||'" + closeSeparator + "'";
				}
		
				if (versionInd.equals("true")) {
					entityIdSelectChildID += "||''" + separator + taskName + separator + "''||''" + timeStamp + "''";
					//entityIdSelectParID += "||''_" + taskName + "_''||''" + timeStamp + "''";
				}
		
				String versionName = "";
				String selSql = "";
		
				//log.info("timeStamp: " + timeStamp);
				if (versionInd.equals("true")) {
					versionName = taskName;
					versionDateTime = timeStamp;
		
					selSql = "SELECT ''''||" +  entityIdSelectChildID + "||'''' child_entity_id FROM task_execution_entities t, " +
							"tdm_lu_type_relation_eid rel where t.task_execution_id = ''" + taskExecutionId +
							"'' and t.execution_status = ''completed'' and t.lu_name = ''" + parentLuName +
							"'' and t.lu_name = rel.lu_type_1 and rel.lu_type_2 = ''" + luName +
							"'' and rel.version_name = ''" + versionName +
							"'' and to_char(rel.version_datetime, ''yyyyMMddHH24miss'') = ''" + versionDateTime +
							"'' and t.source_env = rel.source_env and t.iid = rel.lu_type1_eid" ;
				} else {
					selSql = "SELECT ''''||" + entityIdSelectChildID + "||'''' child_entity_id FROM task_execution_entities t, " +
							"tdm_lu_type_relation_eid rel where t.task_execution_id = ''" + taskExecutionId +
							"'' and t.execution_status = ''completed'' and t.lu_name = ''" + parentLuName +
							"'' and t.lu_name = rel.lu_type_1 and rel.lu_type_2 = ''" + luName +
							"'' and rel.version_name = '''' and t.source_env = rel.source_env and t.iid = rel.lu_type1_eid" ;
				}
				//log.info("selSql: " + selSql);
				if(dcName!= null && !dcName.isEmpty())
				{
					migrateCommand = "batch " + luName + " from TDM using ('" + selSql + "') FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH AFFINITY='"+dcName+"' ASYNC=true";
				}
				else // input DC is empty or null
				{
					migrateCommand = "batch " + luName + " from TDM using ('" + selSql + "') FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH ASYNC=true";
				}
				//log.info("ENTITIES LIST task : " + migrateCommand);
		
			} else
		
			{
				Object[] trnMigrateList_input = {luName,sourceEnvName};
		
				//log.info("Get trnMigrateList records by lu name + source env " );
				Map<String,String> trnMigrateList_values = getTranslationValues("trnMigrateList", trnMigrateList_input);
		
				// TDM 5.1- If no translation record was found for the combination of lu name + source env- get the translation with null value of source env as input
				if(trnMigrateList_values.get("interface_name")== null)
				{
					//log.info("trnMigrateList is empty for lu name: "+ luName + " and source env name: " + sourceEnvName + ". Get the translation records by the lu name with empty source env name ");
					Object[] trnMigrateList_input2 = {luName, ""};
					trnMigrateList_values = getTranslationValues("trnMigrateList", trnMigrateList_input2);
				}
				if (trnMigrateList_values.get("interface_name")== null){
					throw new RuntimeException("No entry found for LU_NAME: " + luName + " in translation trnMigrateList");
				}
		
				//log.info("Get values from the translation map: " + trnMigrateList_values.toString());
				String interface_name = ""+ trnMigrateList_values.get("interface_name");
				String sql = ""+ trnMigrateList_values.get("ig_sql").replaceAll("\n"," ");
		
		
				String splitSQL[] = StringUtils.split(sql.toLowerCase());
				String qry_entity_col="";
				for (int i = 0; i < splitSQL.length; i++)
				{
					if (splitSQL[i].equals("from"))
					{
						qry_entity_col = splitSQL[i - 1].replaceAll("\\s+","");
						break;
					}
				}
		
				// get original SQL statement "select" including the next SQL command like "distinct"
				String select = StringUtils.substringBefore(sql.toLowerCase(), qry_entity_col);
				String sql_part2 = sql.substring(sql.toLowerCase().indexOf(" from ")).replace("'","''");
				String modified_sql ="";
		
				//log.info("Check special formats for interface name: " + interface_name);
		
				//Using trnMigrateListQueryFormats to support DBs that don't accept || as concatenation operator
		
				// 1-May-19- Fix the function- remove the casting (Miron's fix)
				DbInterface dbObj = com.k2view.cdbms.lut.InterfacesManager.getInstance().getTypedInterface(interface_name,sourceEnvName);
		
				String interface_type = dbObj.jdbcDriver;
				Object[] trnMigrateListQueryFormats_input = {interface_type,versionInd};
				Map<String,String> trnMigrateListQueryFormats_values = getTranslationValues("trnMigrateListQueryFormats", trnMigrateListQueryFormats_input);
				String query_format = trnMigrateListQueryFormats_values.get("query_format");
		
				//Query format is supplied --> modify the query acording to the given query format in trnMigrateListQueryFormats
				if (query_format != null && !query_format.isEmpty()) {
					// TDM 5.1- add the handle of configurable separator for special formats- the separator may need to be added to the trnMigrateListQueryFormats
					String sql_part1 = StringUtils.substringBefore(sql.toLowerCase(), qry_entity_col)  + query_format;
		
					if(!openSeparator.equals("") && !closeSeparator.equals("")) // if the open and close separators for the entity id are populated
					{
						StringBuffer sqlStr = new StringBuffer(query_format);
						// Get the substring between source env and entity id
		
						String formatSeparator = query_format.substring(query_format.indexOf("<source_env_name>")+ "<source_env_named>".length(), query_format.indexOf("<entity_id>"));
						//log.info("query_format: " + query_format + ", formatSeparator1: " + formatSeparator + " check");
						formatSeparator = formatSeparator.replaceFirst("'" + separator + "'", "");
						//log.info("formatSeparator2: " + formatSeparator);
						String insertOpenStr = "'" + openSeparator + "'" + formatSeparator;
						String insertCloseStr = formatSeparator +"'" + closeSeparator + "'";
						sqlStr.insert(sqlStr.indexOf("<entity_id>"), insertOpenStr);
						sqlStr.insert(sqlStr.indexOf("<entity_id>") + "<entity_id>".length(), insertCloseStr);
						sql_part1 = select + " " + sqlStr.toString();
					}
		
					//log.info("sql_part1: " + sql_part1 );
		
		
					//String sql_part1 = "select " + query_format;
		
					if (versionInd.equals("true")) { //Modify entities to be in the format of <source_env>_<entity_id>_<task_name>_<timestamp> according to supplied query format
						sql_part1 = sql_part1.replace("<source_env_name>","'" + sourceEnvName + "'");
						sql_part1 = sql_part1.replace("<entity_id>", qry_entity_col );
						sql_part1 = sql_part1.replace("<task_name>","'" + taskName + "'");
						sql_part1 = sql_part1.replace("<timestamp>","'" + timeStamp + "'");
						modified_sql = sql_part1.replace("'","''") + sql_part2;
						//log.info("BACKUP ALL task, With query format, With version: migrate "+luName+" from "+ interface_name +" using ('" + modified_sql + "') WITH AFFINITY='"+dcName+"' ASYNC=true");
					}
					else { //Modify entities to be in the format of <source_env>_<entity_id>  according to supplied query format
						sql_part1 = sql_part1.replace("<source_env_name>","'" + sourceEnvName + "'");
						sql_part1 = sql_part1.replace("<entity_id>" , qry_entity_col );
						modified_sql = sql_part1.replace("'","''") + sql_part2;
						//log.info("BACKUP ALL task, With query format, No version: migrate "+luName+" from "+ interface_name +" using ('" + modified_sql + "') WITH AFFINITY='"+dcName+"' ASYNC=true");
					}
				}
				//No query format --> modfiy query by using || concatenation operator
				else {
					// TDM 5.1- concatenate the open and close separators to the qry_entity_col variables
		
					if(!openSeparator.equals(""))
						qry_entity_col = "''"+ openSeparator + "''||"+ qry_entity_col;
		
					if(!closeSeparator.equals(""))
						qry_entity_col  = qry_entity_col + "||''" + closeSeparator + "''";
		
					if (versionInd.equals("true")) { //Modify entities to be in the format of <source_env>_<entity_id>_<task_name>_<timestamp>
						modified_sql = select + " ''"+ sourceEnvName + separator + "''||"+ qry_entity_col + "||''" + separator + taskName + separator + timeStamp + "''" + sql_part2;
						//log.info("BACKUP ALL task, No query format, with version: migrate "+luName+" from "+ interface_name +" using ('" + modified_sql + "') WITH AFFINITY='"+dcName+"' ASYNC=true");
					}
					else { ////Modify entities to be in the format of <source_env>_<entity_id>
						modified_sql = select + " ''"+ sourceEnvName + separator + "''||"+ qry_entity_col + sql_part2;
						//log.info("BACKUP ALL task, No query format, no version: migrate "+luName+" from "+ interface_name +" using ('" + modified_sql + "') WITH AFFINITY='"+dcName+"' ASYNC=true");
					}
				}
		
				// TDM 5.1- add the check of the input DC. Run the migrate with affinity only if the input DC is populated to support a migrate without affinity
		
				// Tali- add a replace of '' by '
				//modified_sql = modified_sql.replace("''","'");
		
				//log.info("modified_sql: " + modified_sql);
				if(dcName!= null && !dcName.isEmpty())
				{
					migrateCommand= "batch "+luName+" from "+ interface_name +" using ('" + modified_sql + "') FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH AFFINITY='"+dcName+"' ASYNC=true";
				}
				else // input DC is empty
				{
					migrateCommand= "batch "+luName+" from "+ interface_name +" using ('" + modified_sql + "') FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH ASYNC=true";
				}
		
				//log.info("BACKUP ALL task- migrate command: "+ migrateCommand);
			}
		}
		
		//Set Fabric's active environment to sourceEnvName + Execute migrate + get migration_id
		if(sourceEnvName != null && !sourceEnvName.isEmpty())
		{
			//DBExecute(DB_FABRIC, "set environment='"+sourceEnvName+ "'", null);
			ludb().execute("set environment='" + sourceEnvName + "'");
		}
		
		//log.info("Running the following Migrate Command: " + migrateCommand);
		rs_mig_id = ludb().fetch(migrateCommand);
		migrationInfo = "" + rs_mig_id.firstValue();
		
		//rs_mig_id.closeStmt();
		rs_mig_id.close();
		// Tali- populate the migInfo map
		migInfo.put("fabric_execution_id", migrationInfo);
		
		
		// TDM 5.1- set the version_datetime and version_expiration_date only of the input version ind is true. This check is required since a retention period can be also set for extract without versioning tasks
		if (versionInd.equals("true")) {
			migInfo.put("version_datetime", timeStamp );
			migInfo.put("version_expiration_date", version_exp_date );
		}
		else
		{
			migInfo.put("version_datetime", null );
			migInfo.put("version_expiration_date", null );
		}
		
		//return new String[]{migration_id,timeStamp,version_exp_date};
		return migInfo;
	}

	public static Object fnBatchStats(String i_batchId, String i_runMode) throws Exception {
		Object response;
		switch (i_runMode) {
			case "S":
				response = getFabricResponse("batch_summary '" + i_batchId + "'");
				break;
			case "D":
				response = getFabricResponse("batch_details '" + i_batchId + "'");
				break;
			case "H":
				ResultSetWrapper rs = null;
				String migCommand = null;
				String sql1 = null;
				String migrateDesc = null;
				Map <String, String> migHeader = new LinkedHashMap<>();
				String clusterID = (String) ludb().fetch("clusterid").firstValue();

				if (clusterID == null || clusterID.isEmpty()) {
					sql1="select command from k2batchprocess.batchprocess_list where bid='" + i_batchId + "'";
				} else {
					sql1="select command from k2batchprocess_" + clusterID + ".batchprocess_list where bid='" + i_batchId + "'";
				}

				migrateDesc = (String) db("DB_CASSANDRA").fetch(sql1).firstValue();
				migHeader.put("Migration Command", migrateDesc );
				return migHeader;
			default:
				response = new HashMap() {{
					put("errorCode", "FAIL");
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

	public static String generateListOfMatchingEntitiesQuery(Long beID, String whereStmt, String sourceEnv) throws SQLException {

		final String where = !Util.isEmpty(whereStmt) ? "where " + whereStmt : "";
		Db tdmDB = db("TDM");
		Db.Rows rows = tdmDB.fetch("SELECT be_name FROM public.business_entities WHERE be_id=?;", beID);
		String beName = (String) rows.firstValue();
		if(Util.isEmpty(beName)){
			throw new RuntimeException("business entity does not exist");
		}
		String rootLu = "";
		String luRelationsView = "lu_relations_" + beName + "_" + sourceEnv;

		AtomicInteger parentsCount = new AtomicInteger();

		Db.Rows parents = tdmDB.fetch(PARENTS_SQL, beID);
		Map<String,String> setTypesSql = new HashMap<>();
		Map<String,String> getParamsSqlMap = new HashMap<>();
		List<String> getEntitiesSqlList = new ArrayList<>();
		StringBuilder setTypesQuery = new StringBuilder();
		for(Db.Row luRow : parents){
			ResultSet resultSet = luRow.resultSet();
			rootLu = resultSet.getString("lu_name");
			String rootLuStrL = rootLu.toLowerCase();
			String jsonTypeName = sourceEnv + "_" + beName + "_" + rootLu + "_json_type";
			parentsCount.getAndIncrement();

			Db.Rows children = tdmDB.fetch(GET_CHILDREN_SQL, rootLu, beID);

			String childrenList = (String) children.firstValue();
			setTypesQuery.append(getSetTypesQuery(sourceEnv, rootLu, jsonTypeName, childrenList));


			String paramsSql = getParamsSql(sourceEnv, rootLu, rootLuStrL, jsonTypeName, childrenList);

			getParamsSqlMap.put(rootLu, paramsSql);
			getEntitiesSqlList.add(" SELECT DISTINCT " + rootLuStrL + "_root_id as entity_id FROM \"" + luRelationsView + "\" " + where);
		}

		String getParamsSql = "";

		int count = parentsCount.get();
		if(count > 0){
			if(count == 1){
				getParamsSql = getParamsSqlMap.get(rootLu); //todo check key
			}else {
				final String firstLu = getParamsSqlMap.entrySet().stream().findFirst().get().getKey();
				String sql = getParamsSqlMap.remove(firstLu);
				getParamsSql += "(" + sql + ") as " + firstLu + "_final " + getParamsSqlMap.entrySet().stream().map((entry) -> " FULL JOIN (" + entry.getValue() + ") as " + entry.getKey() + "_final" + " ON "  + firstLu + "_final." + firstLu + "_root_id=" + entry.getKey().toLowerCase() + "_final." + entry.getKey().toLowerCase() + "_root_id ")
						.collect(Collectors.joining(" "));
			}
		}

		String createViewSql = createViewSql(luRelationsView, setTypesQuery.toString(), getParamsSql);
		tdmDB.execute(createViewSql);
		return getEntitiesSqlList.stream().map(Object::toString).collect(Collectors.joining(" UNION "));

	}


	@out(name = "instanceID", type = String.class, desc = "")
	@out(name = "envName", type = String.class, desc = "")
	@out(name = "versionName", type = String.class, desc = "")
	@out(name = "versiionDateTime", type = String.class, desc = "")
	public static Object fnSplitUID2(String uid) throws Exception {
		// TDM 5.1- fix the function to support also dataflux mode when the instance id also has version name  +datetime
		// In addition- remove the open and close separators
		String instanceId = "";
		String envName = "";
		
		String separator = "";
		String iidOpenSeparator = "";
		String iidCloseSeparator = "";
		// TDM 6.0 - The version name (task name) and version datetime should be returned
		String versionName = "";
		String versionDateTime = "";
		
		String iidSeparator = "" + db("TDM").fetch("Select param_value from tdm_general_parameters where param_name = 'iid_separator'").firstValue();
        //separator = !Util.isEmpty(iidSeparator) ? iidSeparator : "_";
		if ( !Util.isEmpty(iidSeparator) && !"null".equals(iidSeparator) ) {
			separator = iidSeparator;
		} else {
			separator = "_";
		}
		// TDM 5.1- get open and close separators for the instanceId. If they exist- get the instanceId according the open and close separators
		//Set the SQL parameter
		
		Object[] separators = fnGetIIdSeparatorsFromTDM();
		//log.info("fnSplitUID - Input UID: " + uid);
		//log.info("fnSplitUID - separators[0]: " + separators[0] + ", separators[1]: " + separators[1]);
		if(separators[0] != null && separators[0].toString() != null && !separators[0].toString().isEmpty() )
			iidOpenSeparator = separators[0].toString();
		
		if(separators[1] != null && separators[1].toString() != null && !separators[1].toString().isEmpty())
			iidCloseSeparator = separators[1].toString();
		
		if (uid.startsWith("_dev")) {
			uid = uid.replaceFirst("_dev_", "dev_");
			envName = "_dev";
		}
			
		try {
		 	
				String[] split_uid = uid.split(separator);
			
				if(envName.equals(""))
		        	envName = split_uid[0].toString();
			
				// Check if the open and close separators are populated
				if(iidOpenSeparator.equals("") || uid.indexOf(iidOpenSeparator)== -1 || iidCloseSeparator.equals("") || uid.indexOf(iidCloseSeparator)== -1) // the separators for the IID are not defined in the TDM DB or in the input UID
				{
					//log.info("fnSplitUID - No Separators");
					instanceId = split_uid[1].toString();
					
					//TDM 6.0, get the version name and datetime
					if (split_uid.length==4) {  //entity in the format of <environment>_<entity_id>_<task_name>_<timestamp>
						versionName = split_uid[2].toString();
						versionDateTime = split_uid[3].toString();
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
						String[] split_version = (uid.substring(pos + 1)).split(separator);
						versionName = split_version[0];
						versionDateTime = split_version[1];
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
		
			if(uid.indexOf(separator) == -1)
				 throw new Exception("Environment Name Is Missing, Underscore not found");
		
		}
		
		//log.info("fnSplitUID - Output: instanceID: " + instanceId + ", envName: " + envName + ", versionName: " + versionName + "versionDateTime: " + versionDateTime);
		return new Object[]{instanceId, envName, versionName, versionDateTime};
	}

	@NotNull
	private static String createViewSql(String luRelationsView, String setTypesQuery, String getParamsSql) {
		return "DO $$ " +
				"DECLARE "+
				"type_var text; "+
				"BEGIN "+
				"IF NOT EXISTS (SELECT 1 FROM pg_matviews WHERE matviewname =  '" + luRelationsView + "'" +
				") THEN " + setTypesQuery +
				"CREATE MATERIALIZED VIEW \"" + luRelationsView + "\" AS SELECT * FROM  ( "  + getParamsSql + " ) AS mergedTables;" +
				"END IF; " +
				"END $$ ";
	}

	@NotNull
private static String getParamsSql(String sourceEnv, String rootLu, String rootLuStrL, String jsonTypeName, String childrenList) {

    String str = "IN(" + childrenList.replace("'", "''") + ")";
    return "WITH RECURSIVE relations AS(" +
            "SELECT lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid, entity_id as root_id," +
            " param_values('" + rootLu +"',entity_id,lower(base.lu_type_1) || '_params','" + sourceEnv + "'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(base.lu_type_1) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment'),'" + str + "', 'lu_type1_eid', '-1')::text as p1," +
            " param_values('" + rootLu + "',entity_id,lower(base.lu_type_2) || '_params','" + sourceEnv + "'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(base.lu_type_2) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment')," +
            "'" + str + "', 'lu_type2_eid'" +
            ",base.lu_type_2)::text as p2 " +
            "FROM ( " +
            "SELECT entity_id, '" + rootLu + "'::text as lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid " +
            "FROM " + rootLu + "_params " +
            " LEFT JOIN (SELECT * FROM tdm_lu_type_relation_eid WHERE lu_type_1= '" + rootLu + "' AND source_env='" + sourceEnv +"' AND lu_type_2 IN(" + childrenList + ") AND version_name='')" +
            " AS rel_base ON " + rootLu + "_params.entity_id=rel_base.lu_type1_eid" +
            " WHERE source_environment='" + sourceEnv + "' ) AS base " +
            " UNION ALL" +
            " SELECT a.lu_type_1, a.lu_type_2, a.lu_type1_eid, a.lu_type2_eid, root_id," +
            " param_values('" + rootLu + "',a.lu_type1_eid,lower(a.lu_type_1) || '_params','" + sourceEnv +"'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(a.lu_type_1) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment'), '" + str + "', 'lu_type1_eid', '-1')::text as p1," +
            " param_values('" + rootLu + "',a.lu_type1_eid,lower(a.lu_type_2) || '_params','" + sourceEnv + "'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(a.lu_type_2) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment')," +
            "'" + str + "', 'lu_type2_eid'" +
            ",a.lu_type_2)::text as p2 " +
            "FROM tdm_lu_type_relation_eid a " +
            "INNER JOIN relations b ON b.lu_type2_eid = a.lu_type1_eid  AND b.lu_type_2 = a.lu_type_1 AND source_env='" + sourceEnv + "' AND a.lu_type_2 IN("+ childrenList +")  AND b.lu_type_2 IN(" + childrenList + ") AND a.lu_type_1 IN(" + childrenList + ") AND b.lu_type_1 IN("+ childrenList +"))" +
            " SELECT * FROM ( " +
            " SELECT root_id AS " + rootLuStrL + "_root_id, (json_populate_recordset(null::\"" + jsonTypeName +"\", json_agg(path))).*" +
            " FROM (" +
            " SELECT root_id,  json_append(p1::json, (replace(string_agg(p2, ', '), '}, {', ','))::json, '{}') AS path from relations GROUP BY root_id, p1) colsMerged GROUP BY root_id) AS final";
}
		
	@NotNull
	private static String getSetTypesQuery(String sourceEnv, String rootLu, String json_type_name, String childrenList) {
		return " "+
				"DROP TYPE IF EXISTS \"" +  json_type_name + "\";" +
				"WITH RECURSIVE cols AS " +
				"(SELECT lu_type as lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid, concat(table_params1, table_params2) AS table_params FROM (" +
				"SELECT * FROM (SELECT '" + rootLu + "'::text as lu_type, (SELECT string_agg(lu_param_col::text, ',') AS param FROM (select '\"' || column_name || '\"' AS lu_param_col FROM information_schema.columns WHERE table_name=concat(LOWER('" + rootLu + "'),'_params') AND column_name <> 'source_environment' AND column_name <> 'be_id' AND column_name <> 'entity_id') AS params) AS table_params1) AS t1 " +
				"LEFT JOIN (" +
				"SELECT lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid," +
				"(SELECT CASE WHEN lu_type_2 IN(" + childrenList + ") THEN ',' || string_agg(lu_param_col::text, ',')  ELSE '' END AS param FROM (SELECT '\"' || column_name || '\"' AS lu_param_col FROM information_schema.columns WHERE table_name=concat(LOWER(lu_type_2),'_params') AND column_name <> 'source_environment' AND column_name <> 'be_id' AND column_name <> 'entity_id') AS params) AS table_params2 " +

				"FROM tdm_lu_type_relation_eid WHERE lu_type_1= '" + rootLu + "' AND source_env='" + sourceEnv + "' AND lu_type_2 IN(" + childrenList + ")) AS rel_base ON t1.lu_type=rel_base.lu_type_1 ) as base " +

				"UNION ALL SELECT a.lu_type_1, a.lu_type_2, a.lu_type1_eid, a.lu_type2_eid," +
				"concat(table_params,',',(SELECT string_agg(lu_param_col::text, ',') as param from (select '\"' || column_name || '\"' AS lu_param_col from information_schema.columns where table_name=concat(LOWER(a.lu_type_1),'_params') AND column_name <> 'source_environment' AND column_name <> 'be_id' AND column_name <> 'entity_id') AS params),','," +
				"(SELECT string_agg(lu_param_col::text, ',') AS param from (select '\"' || column_name || '\"' AS lu_param_col from information_schema.columns where table_name=concat(LOWER(a.lu_type_2),'_params') AND column_name <> 'source_environment' AND column_name <> 'be_id' AND column_name <> 'entity_id') as params))" +

				"FROM tdm_lu_type_relation_eid a " +
				"INNER JOIN cols b ON b.lu_type2_eid = a.lu_type1_eid  AND b.lu_type_2 = a.lu_type_1 AND a.lu_type_2 IN(" + childrenList + "))" +

				"select ' CREATE type \"" + json_type_name + "\" AS (' || string_agg(distinct unnest || ' text[] ' , ',') || ');' into type_var from cols ,unnest(string_to_array(cols.table_params, ','));" +

				"IF type_var IS NOT NULL " +
				"THEN EXECUTE type_var;" +
				"ELSE EXECUTE ' CREATE type \"" + json_type_name + "\" AS (" + rootLu + "_dummy text);';" +
				"END IF; ";
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


	public static void fnCheckInsFound() throws Exception {
		// Fix- TDM 7.0.1 - Check the main source LU tables only if the TDM_INSERT_TO_TARGET is true
		String luName = getLuType().luName;
		
		if(("" + ludb().fetch("SET " + luName + ".TDM_INSERT_TO_TARGET").firstValue()).equals("true"))
		{
		
			// Get the list of root tables from the Global
			String[] rootTables = ("" + ludb().fetch("SET " + luName + ".ROOT_TABLE_NAME").firstValue()).split(",");
		
			// Indicates if any of the root tables have values in it
			boolean instanceExists = false;
		
			// For every possible root table
			for(String rootTable : rootTables){
		                // If that table has data
		                if(!ludb().fetch(String.format("SELECT 1 FROM %s LIMIT 1",rootTable.trim())).firstRow().isEmpty())
		                                // Indicate the LU is found
		                                instanceExists = true;
			}
		
		
			if(!instanceExists){
		                LogEntry lg = new LogEntry("INSTANCE NOT FOUND!", MsgId.INSTANCE_MISSING);
		                lg.luInstance = fnValidateNdGetInstance()[0] + "";
		                lg.luType = getLuType().luName;
		                throw new InstanceNotFoundException(lg, null);
			}
		}
	}



	@out(name = "firstSyncInd", type = Boolean.class, desc = "")
	public static Boolean fnIsFirstSync() throws Exception {
		return isFirstSync();
	}


	@out(name = "refTableName", type = String.class, desc = "")
	@out(name = "schemaName", type = String.class, desc = "")
	@out(name = "interfaceName", type = String.class, desc = "")
	@out(name = "targetSchemaName", type = String.class, desc = "")
	@out(name = "targetInterfaceName", type = String.class, desc = "")
	public static Object fnGetRefTableData(String luName, String refTableName) throws Exception {
		Map<String,Map<String, String>> trnRefListValues = getTranslationsData("trnRefList");
		
		String trnLuName = "";
		String trnTableName = "";
		String schemaName = "";
		String interfaceName = "";
		String targetSchemaName = "";
		String targetInterfaceName = "";
		//log.info("fnGetRefTableData - luName: " + luName + ", refTableName: " + refTableName);
		
		for(String index: trnRefListValues.keySet()){
			Map<String, String> valMap = trnRefListValues.get(index);
			
			trnLuName = index.substring(0, index.indexOf("@") );
			trnTableName = valMap.get("reference_table_name");
			//log.info("fnGetRefTableData - trnLuName: " + trnLuName + ", trnTableName: " + trnTableName);
			if (trnTableName.equalsIgnoreCase(refTableName)  && trnLuName.equalsIgnoreCase(luName)) {
				//log.info("fnGetRefTableData - Found table in translation");
				schemaName = valMap.get("schema_name");
				interfaceName = valMap.get("interface_name");
				targetSchemaName = valMap.get("target_schema_name");
				targetInterfaceName = valMap.get("target_interface_name");
				break;
			}
				
		}
		//log.info("fnGetRefTableData - Returning: refTableName: " + refTableName + ", schemaName: " + schemaName + ", interfaceName: " + interfaceName +
		//		", targetSchemaName: " + targetSchemaName + ", targetInterfaceName: " + targetInterfaceName);
		return new Object[]{refTableName, schemaName, interfaceName, targetSchemaName, targetInterfaceName};
	}
	
	public static void fnTdmUpdateTaskExecutionEntities(String taskExecutionId, Long luId, String luName) throws Exception {
		// TALI- 5-May-20- add a select of selection_method  + fabric_Execution_uid columns. 
		//Remove the condition of fabric_execution_id is not null to support reference only task
		
		//String taskExeListSql = "SELECT L.FABRIC_EXECUTION_ID, L.SOURCE_ENV_NAME, L.CREATION_DATE, L.START_EXECUTION_TIME, " +
		//	"L.END_EXECUTION_TIME, L.ENVIRONMENT_ID, T.VERSION_IND, T.TASK_TITLE, L.VERSION_DATETIME, T.SELECTION_METHOD, COALESCE(FABRIC_EXECUTION_ID, '') AS FABRIC_EXECUTION_ID " + 
		//    "FROM TASK_EXECUTION_LIST L, TASKS T " +
		//	"WHERE TASK_EXECUTION_ID = ? AND LU_ID = ? AND L.TASK_ID = T.TASK_ID";
		
		String taskExeListSql = "SELECT L.FABRIC_EXECUTION_ID, L.SOURCE_ENV_NAME, L.CREATION_DATE, L.START_EXECUTION_TIME, " +
						"L.END_EXECUTION_TIME, L.ENVIRONMENT_ID, T.VERSION_IND, T.TASK_TITLE, to_char(L.VERSION_DATETIME,'YYYY-MM-DD HH24:MI:SS') AS VERSION_DATETIME, " +
						"T.SELECTION_METHOD, COALESCE(FABRIC_EXECUTION_ID, '') AS FABRIC_EXECUTION_ID " +
						"FROM TASK_EXECUTION_LIST L, TASKS T " +
						"WHERE TASK_EXECUTION_ID = ? AND LU_ID = ? AND L.TASK_ID = T.TASK_ID";
		
		String fabricExecID = "";
		String srcEnvName = "";
		String creationDate = "";
		String startExecDate = "";
		String endExecDate = "";
		String envID = "";
		String entityID = "";
		String targetEntityID = "";
		String execStatus = "";
		String idType = "ENTITY";
		String IID = "";
		String versionInd = "";
		String versionName = "";
		String verstionDateTime = "";
		// Add selectionMethod
		String selectionMethod = "";
		String fabricExecutionId="";
		
		final String UIDLIST = "UIDList";
		
		String insertSql = "INSERT INTO TASK_EXECUTION_ENTITIES(" +
				"TASK_EXECUTION_ID, LU_NAME, ENTITY_ID, TARGET_ENTITY_ID, ENV_ID, EXECUTION_STATUS, ID_TYPE, " +
				"FABRIC_EXECUTION_ID, IID, SOURCE_ENV";
		String insertBinding = "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
		
		Db.Row taskData = db("TDM").fetch(taskExeListSql, taskExecutionId, luId).firstRow();
		
		
		//log.info("tdmUpdateTaskExecutionEntities: TASK_EXECUTION_ID: " + taskExecutionId + ", LU_ID: " + luId + ", LU_NAME: " + luName);
		if(!taskData.isEmpty()) {
			fabricExecID = "" + taskData.cell(0);
			srcEnvName = "" + taskData.cell(1);
			creationDate = "" + taskData.cell(2);
			startExecDate = "" + taskData.cell(3);
			endExecDate = "" + taskData.cell(4);
			envID = "" + taskData.cell(5);
			versionInd = "" + taskData.cell(6);
			versionName = "" + taskData.cell(7);
			verstionDateTime = "" + taskData.cell(8);
		
			// Add selection method and fabric_execution_id
			selectionMethod = "" + taskData.cell(9);
			fabricExecutionId = "" + taskData.cell(10);
			
			//log.info("creationDate: " + creationDate + ", startExecDate: " + startExecDate + ", endExecDate: " + endExecDate + ", SELECTION METHOD: " + selectionMethod);
			 
			if(!"null".equals(creationDate) && !"".equals(creationDate)) {
				insertSql += ", CREATION_DATE";
				creationDate = creationDate.substring(1);
				insertBinding += ", ?";
			} 
			
			if(!"null".equals(startExecDate) && !"".equals(startExecDate)) {
				insertSql += ", ENTITY_START_TIME";
				insertBinding += ", ?";
			}
			
			if(!"null".equals(endExecDate) && !"".equals(endExecDate)) {
				insertSql += ", ENTITY_END_TIME";
				insertBinding += ", ?";
			}
			
			if ("true".equals(versionInd)) {
				insertSql += ", VERSION_NAME,VERSION_DATETIME";
				insertBinding += ", ?, ?";
			}
			
			insertBinding += ")";
			insertSql += ") " + insertBinding;
			insertSql += " ON CONFLICT ON CONSTRAINT task_execution_entities_pkey Do update set execution_status = ?";
		
			// TALI- 5-May-20 - add a check of the sectionMethod. Do not get the list of IIDs for reference only task
		
			Map<String, Map> migrationList = new LinkedHashMap<String, Map>();
			//Map<String, Map> migrationList = (Map<String, Map>) fnGetIIDListForMigration(fabricExecID, null);
			if(!selectionMethod.equals("REF") && !fabricExecutionId.equals("")) {
				migrationList = (Map<String, Map>) fnGetIIDListForMigration(fabricExecID, null);
			}
			
			//log.info ("tdmUpdateTaskExecutionEntities - insertSql: " + insertSql);
			if (migrationList.containsKey("Copied entities per execution")) {
				LinkedHashMap<String, Object> m1 = (LinkedHashMap<String, Object>) migrationList.get("Copied entities per execution");
				
				if (m1.containsKey(UIDLIST)) {
					List<Object> copied_UID_list = (List<Object>) m1.get(UIDLIST);
					//log.info("Size of copied_UID_list: " + copied_UID_list.size());
					for (Object UID : copied_UID_list) {
						Map<Object, Object> innerCopiedUIDMap = (Map<java.lang.Object, java.lang.Object>) UID;
			
						for (Map.Entry<Object, Object> copiedUID : innerCopiedUIDMap.entrySet()) {
							targetEntityID = (String) copiedUID.getKey();
							IID = (String) copiedUID.getKey();
							entityID = (String) copiedUID.getValue();
							execStatus = COMPLETED;
							
							ArrayList<String> paramList = new ArrayList<>();
							
							paramList.add(taskExecutionId);
							paramList.add(luName);
							paramList.add(entityID);
							paramList.add(targetEntityID);
							paramList.add(envID);
							paramList.add(execStatus);
							paramList.add(idType);
							paramList.add(fabricExecID);
							paramList.add(IID);
							paramList.add(srcEnvName);
							
							//log.info("Inserting Copied: LU_NAME: " + LU_NAME + ", TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", entityID: " + entityID);
							//In postgres, timestamp fields cannot be set to empty string,
							//therefore date fields should be insterted only if they have value		
							//log.info("Inserting: TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", LU_NAME: " + LU_NAME + ", entityID: " + entityID);			
							if(!"null".equals(creationDate) && !"".equals(creationDate)) paramList.add(creationDate);
							if(!"null".equals(startExecDate) && !"".equals(startExecDate)) paramList.add(startExecDate);
							if(!"null".equals(endExecDate) && !"".equals(endExecDate)) paramList.add(endExecDate);
							if ("true".equals(versionInd)) {
								paramList.add(versionName);
								paramList.add(verstionDateTime);
							}
							
							//Adding additional parameter for execution_status, in case the insert failed on primary key constraint,
							//in that case only the status will be updated,such case can happen in case of cancel resume
							paramList.add(execStatus);
							
							Object[] params = paramList.toArray();
			
							//log.info ("insertSql - Copied Entities: " + insertSql);
							db("TDM").execute(insertSql, params);
						}
			
					}
				}
			}
			
			if (migrationList.containsKey("Failed entities per execution")) {
				LinkedHashMap<String, Object> m2 = (LinkedHashMap<String, Object>) migrationList.get("Failed entities per execution");
				if (m2.containsKey(UIDLIST)) {
					List<Object> failed_UID_list = (List<Object>) m2.get(UIDLIST);
					for (Object UID : failed_UID_list) {
						Map<Object, Object> innerFailedUIDMap = (Map<java.lang.Object, java.lang.Object>) UID;
			
						for (Map.Entry<Object, Object> failedUID : innerFailedUIDMap.entrySet()) {
							targetEntityID = (String) failedUID.getKey();
							IID = (String) failedUID.getKey();
							entityID = (String) failedUID.getValue();
							execStatus = FAILED;
							
							ArrayList<String> paramList = new ArrayList<>();
							
							paramList.add(taskExecutionId);
							paramList.add(luName);
							paramList.add(entityID);
							paramList.add(targetEntityID);
							paramList.add(envID);
							paramList.add(execStatus);
							paramList.add(idType);
							paramList.add(fabricExecID);
							paramList.add(IID);
							paramList.add(srcEnvName);
							
							//log.info("Inserting Failed: TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", LU_NAME: " + LU_NAME + ", entityID: " + entityID);
							if(!"null".equals(creationDate) && !"".equals(creationDate)) paramList.add(creationDate);
							if(!"null".equals(startExecDate) && !"".equals(startExecDate)) paramList.add(startExecDate);
							if(!"null".equals(endExecDate) && !"".equals(endExecDate)) paramList.add(endExecDate);
							if ("true".equals(versionInd)) {
								paramList.add(versionName);
								paramList.add(verstionDateTime);
							}
							//Adding additional parameter for execution_status, in case the insert failed on primary key constraint,
							//in that case only the status will be updated,such case can happen in case of cancel resume
							paramList.add(execStatus);
							
							Object[] params = paramList.toArray();
			
							//log.info ("insertSql - Failed Entities: " + insertSql);
							db("TDM").execute(insertSql, params);
						}
			
					}
				}
			}
			
			//Add reference Entities to TASK_EXECUTION_ENTITIES table
			String refListSql = "SELECT REF_TABLE_NAME, EXECUTION_STATUS FROM TASK_REF_EXE_STATS ES WHERE " +
					"TASK_EXECUTION_ID = ? AND TASK_REF_TABLE_ID IN (SELECT TASK_REF_TABLE_ID FROM TASK_REF_TABLES RT " +
						"WHERE RT.TASK_ID = ES.TASK_ID AND RT.TASK_REF_TABLE_ID = ES.TASK_REF_TABLE_ID AND RT.LU_NAME = ?)";
			
			idType = "REFERENCE";
			
			Db.Rows refList = db("TDM").fetch(refListSql, taskExecutionId, luName);
			
			for (Db.Row refTable : refList) {
				entityID = "" + refTable.cell(0);
				targetEntityID = entityID;
				execStatus = "" + refTable.cell(1);
				IID = entityID;
							
				ArrayList<String> paramList = new ArrayList<>();
							
				paramList.add(taskExecutionId);
				paramList.add(luName);
				paramList.add(entityID);
				paramList.add(targetEntityID);
				paramList.add(envID);
				paramList.add(execStatus);
				paramList.add(idType);
				paramList.add(fabricExecID);
				paramList.add(IID);
				paramList.add(srcEnvName);
				
				if(!"null".equals(creationDate) && !"".equals(creationDate)) paramList.add(creationDate);
				if(!"null".equals(startExecDate) && !"".equals(startExecDate)) paramList.add(startExecDate);
				if(!"null".equals(endExecDate) && !"".equals(endExecDate)) paramList.add(endExecDate);
				if ("true".equals(versionInd)) {
					paramList.add(versionName);
					paramList.add(verstionDateTime);
				}
				//Adding additional parameter for execution_status, in case the insert failed on primary key constraint,
				//in that case only the status will be updated,such case can happen in case of cancel resume
				paramList.add(execStatus);
				
				Object[] params = paramList.toArray();
				
				//log.info("Inserting Reference: TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", LU_NAME: " + LU_NAME + ", entityID: " + entityID);
				//log.info ("insertSql - Reference Entity: " + insertSql);
				db("TDM").execute(insertSql, params);
			}
			
			if (refList != null) {
				refList.close();
			}
		}
	}


	@desc("Dummy Root function")
	@type(RootFunction)
	@out(name = "dummy_output", type = void.class, desc = "")
	public static void fnDummyPop(String dummy_input) throws Exception {
		if(1 == 2)yield(new Object[] {null});
	}


	@out(name = "globalValue", type = String.class, desc = "")
	public static String getGlobal(String globalName) throws Exception {
		return "" + fabric().fetch("set " + globalName).firstValue();
	}

	public static Map<String, Object> wrapWebServiceResults(String errorCode, String message, Object result) {
		HashMap<String, Object> response = new HashMap<>();
		response.put("errorCode", errorCode);
		response.put("message", message);
		response.put("result", result);
		return response;
	}

	public static void fnTdmCopyReference(String taskExecutionID, String taskType) throws Exception {
		//log.info("-- START Reference JOB for Task Type: " + taskType + " Task Execution ID: " + taskExecutionID + "---");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();
		
		Db.Rows refTabLst = null;
		
		String refQuery ="Select ES.* from TASK_REF_EXE_STATS ES, TASKS T where execution_status  in ('"+WAITING+"', '"+PENDING+"', '"+RESUME+"', '"+STOPPED+"') " + 
			"and ES.TASK_ID = T.TASK_ID and lower(T.TASK_TYPE) = ? and ES.TASK_EXECUTION_ID = ? order by task_execution_id, task_id";
		
		refTabLst = db(TDM).fetch(refQuery, taskType, taskExecutionID);
		
		int refCount = 0;
		
		for (Db.Row row : refTabLst) {
			refCount++;
			String taskRefTableID = "" + row.get("task_ref_table_id");
			String refTableName = "" + row.get("ref_table_name");
			//log.info("fnTdmCopyReference - refTableName: " + refTableName);
				
			if (!StringUtils.isEmpty(taskRefTableID) && !StringUtils.isEmpty(taskExecutionID)) {
				
				String luName = "" + db(TDM).fetch("Select lu_name from " + TASK_REF_TABLES + " where task_ref_table_id = ?", taskRefTableID).firstValue();
		
				String taskID = "" + row.get("task_id");
		
				String execStatus = "" + row.get("execution_status");
				
				//log.info("fnTdmCopyReference - execution_status: " + execStatus);
		
				Db.Row taskParams = db(TDM).fetch("Select source_env_name, version_ind, retention_period_type, retention_period_value, " +
					"selection_method, selected_ref_version_task_name, selected_ref_version_datetime, selected_ref_version_task_exe_id from " +
					TASKS + " where task_id = ?", taskID).firstRow();
		
				String versionInd = "false";
				if(taskParams.get("version_ind") != null) {
					versionInd = "" + taskParams.get("version_ind");
				}
				Object retPeriodType = taskParams.get("retention_period_type");
				String retType = (retPeriodType != null) ? "" + retPeriodType : "";
		
				Object retPeriodVal = taskParams.get("retention_period_value");
				Float retVal = (retPeriodVal != null) ? Float.valueOf(retPeriodVal.toString()) : 0;
				Integer ttl = getRetention(retType, retVal);
		
				Db.Row luData = db(TDM).fetch("select p.lu_id, p.lu_dc_name from " + TASKS_LOGICAL_UNITS + " t, "
					 + PRODUCT_LOGICAL_UNITS + " p where t.lu_name = ? and t.task_id = ? and t.lu_name = p.lu_name and t.lu_id = p.lu_id", 
					luName, taskID).firstRow();
				
				String luID = "" + luData.get("lu_id");
				String luDCName = "" + luData.get("lu_dc_name");
			
				String affinity =  "";
				
				if (luDCName != null && !"".equals(luDCName) && !"null".equals(luDCName)) {
					affinity = " AFFINITY='" + luDCName + "'";
				}
				
				String uid = "" + row.get("job_uid");
				
				Object selectionMethod = "" + taskParams.get("selection_method");
				
				if(selectionMethod != null && selectionMethod.toString().equals(REF)){
					Long unixTime = System.currentTimeMillis() ;
					Long unixTime_plus_retention;
		
					String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
					Integer retention_in_seconds = getRetention(retType, retVal);
		
					unixTime_plus_retention = ( unixTime/1000L + retention_in_seconds )*1000;
					String versionExpirationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(unixTime_plus_retention);
		
					Object[] param;
					if (versionInd.equals("true")) {
						param = new Object[]{RUNNING, timeStamp, versionExpirationDate, taskExecutionID, taskID, luID};
					} else {
						param = new Object[]{RUNNING, null, null, taskExecutionID, taskID, luID};
					}
					
					//log.info("fnTdmCopyReference - Updating task to running for - task_execution_id: " + taskExecutionID + 
					//		", task_id: " + taskID + ", lu_id: " + luID);
				
					db(TDM).execute("update " + TASK_EXECUTION_LIST + " set execution_status = ?, " +
									"version_datetime = ?, version_expiration_date = ? " +
									"where task_execution_id = ? and task_id = ? and lu_id = ?", param);
				}
				//int count = 0;
				//int retries = 5;
				
				String sourceEnv;
		
				sourceEnv = "" + taskParams.get("source_env_name");
				
				if (taskType.equalsIgnoreCase("extract")) {
				
					String args = " ARGS='{\"sourceEnv\":\"" + sourceEnv + "\",\"versionID\":\"" + versionInd +
									"\",\"ttl\":\"" + ttl + "\", \"taskExecID\":\"" + taskExecutionID +
									"\",\"taskRefTableID\":\"" + taskRefTableID + "\",\"luName\":\"" + luName + "\"}'";
						
					//while(!Thread.currentThread().isInterrupted()) {
						try{
							switch (execStatus) {
								case WAITING:
								case PENDING:
									//log.info("fnTdmCopyReference -inside PENDING/WAITING status: " + execStatus);
				
									fabric().execute("startjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + taskExecutionID + "_" + taskRefTableID + "' " + affinity + args + ";");
									break;
								case STOPPED:
									//log.info("fnTdmCopyReference -inside STOPPED status: " + execStatus);
									fabric().execute("stopjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "';");
									break;
								case RESUME:
									//log.info("fnTdmCopyReference -inside RESUME status: " + execStatus);
									//log.info("fnTdmCopyReference - UID: " + uid + ", ARGS: " + args + ", affinity: " + affinity);
									if(Util.isEmpty(uid)){
										//log.info("fnTdmCopyReference - Creating UID");
										uid = taskExecutionID + "_" + taskRefTableID;
										fabric().execute("startjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "' " + affinity + args + ";");
									}else{
										//log.info("fnTdmCopyReference - Resuming UID");
										/*db(TDM).execute("update " + TASK_EXECUTION_LIST + " set execution_status = ? " +
											"where task_execution_id = ? and task_id = ? and lu_id = ?; ",
											RUNNING, taskExecutionID, taskID,luID);*/
										fabric().execute("resumejob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "'"  + ";");
										//log.info("------------------------------------- After resume case ------------------------------------");
									}
									break;
							}
						} catch (Exception e) {
							if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
								e.printStackTrace();
								throw e;
							}
							//if (++count >= retries) {
							db(TDM).execute("update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?,  updated_by = ?" +
								"where task_execution_id = ? and task_ref_table_id = ?; ",
								FAILED, e.getMessage(), TDM_COPY_REFERENCE, taskExecutionID, taskRefTableID);
							fabric().execute("stopjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "';");
								//retries = 0;
								//break;
							//} else {
								//log.info(e.getMessage() + "; this is retry number " + count);
								//Thread.sleep(5000);
							//}
						}
					//}
				} else {// In case of Load Task
				//	while(!Thread.currentThread().isInterrupted()) {
						//log.info("fnTdmCopyReference - Handling Load Task");
					try{
						switch (execStatus) {
							case WAITING:
							case PENDING:
								String selectedRefVersionTaskName = "''";
								String selectedRefVersionDateTime = "19700101000000";
								String selectedRefVersionTaskExeId = "''";
								if (versionInd.equals("true")) {
									selectedRefVersionTaskName = "" + taskParams.get("selected_ref_version_task_name");
									selectedRefVersionDateTime = "" + taskParams.get("selected_ref_version_datetime");
									selectedRefVersionTaskExeId = "" + taskParams.get("selected_ref_version_task_exe_id");
								}
								String loadRefCmd = "BATCH " + luName + ".('" + taskExecutionID + "_" + refTableName + "') fabric_command=\"broadway " + luName + ".TDMReferenceLoader " +
		                            "iid=?, taskExecutionID="+ taskExecutionID + ", luName=" + luName + ", refTableName=" + refTableName + ", sourceEnvName=" + sourceEnv + 
									", selectedRefVersionTaskName='" + selectedRefVersionTaskName +"', selectedRefVersionDateTime=" +
									selectedRefVersionDateTime + ", selectedRefVersionTaskExeId=" + selectedRefVersionTaskExeId +"\" with async='true'";
									
								//log.info("fnTdmCopyReference - Running batch command: " + loadRefCmd);
								String batchID = Util.rte(() -> "" + fabric().fetch(loadRefCmd).firstValue());
								
								//log.info("fnTdmCopyReference - Updating batch ID to: " + batchID);
								db(TDM).execute("update " + TASK_REF_EXE_STATS + " set job_uid= ?, execution_status = ?, " +
									"start_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', updated_by = ?" +
									"where task_execution_id = ? and task_ref_table_id = ?; ",
									batchID, RUNNING, "TDMReferenceLoader", taskExecutionID, taskRefTableID);
								break;
							case STOPPED:
								fabric().execute("batch_pause '" + uid + "';");
								break;
							case RESUME:
								fabric().execute("batch_retry '" + uid + "'"  + ";");
								//log.info("------------------------------------- After resume case ------------------------------------");
								break;
						}
						//break;
					} catch (Exception e) {
						log.error("Reference Table handling had an exception: " + e);
						if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
							throw e;
						}
						//if (++count >= retries) {
						db(TDM).execute("update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?,  updated_by = ?" +
							"where task_execution_id = ? and task_ref_table_id = ?; ",
							FAILED, e.getMessage(), TDM_COPY_REFERENCE, taskExecutionID, taskRefTableID);
						//log.info("uid to cancel: " + uid);
						if (!"null".equals(uid)) {
							fabric().execute("cancel batch '" + uid + "';");
						}
							//	retries = 0;
							//break;
							//} else {
								//log.info(e.getMessage() + "; this is retry number " + count);
							//	Thread.sleep(5000);
							//}
					}
				}
				
			}
			//log.info("fnTdmCopyReference - Handling next Ref table #" + refCount);
		}
		//log.info("fnTdmCopyReference - After the loop on ref tables, refCount: " + refCount);
		//In case the task type was REF, but included LUs that do not have reference,or none of their reference were chosen, update the status to COMPLETE
		String lusWithoutRefSql = "select l.task_id, l.task_execution_id, l.lu_id from task_execution_list l, tasks t, tasks_logical_units lu " +
			"where l.task_id = t.task_id and t.selection_method = 'REF' and l.task_id = lu.task_id and l.lu_id = lu.lu_id and " +
			"lower(l.execution_status) in ('pending', 'stopped') and " +
			"not exists (select 1 from task_ref_tables r, task_ref_exe_stats s where r.task_id = lu.task_id and r.lu_name = lu.lu_name " +
			"and r.task_ref_table_id = s.task_ref_table_id and s.task_execution_id = l.task_execution_id " +
			"and lower(s.execution_status) not in ('completed', 'failed'))";
		
		String updateLuWithoutRefSql = "UPDATE task_execution_list SET execution_status = ?, num_of_processed_entities = 0, " +
					"start_execution_time = current_timestamp at time zone 'utc', " +
					"end_execution_time = current_timestamp at time zone 'utc', num_of_copied_entities = 0, num_of_failed_entities = 0, " +
					"num_of_processed_ref_tables = 0, num_of_copied_ref_tables = 0, num_of_failed_ref_tables = 0 " +
					"WHERE  task_id = ? AND task_execution_id = ? and lu_id = ?";
		
		Db.Rows lusWithoutRef = db(TDM).fetch(lusWithoutRefSql);
		
		for (Db.Row luWithoutRef : lusWithoutRef) {
			String taskId = "" + luWithoutRef.cell(0);
			String taskExecID = "" + luWithoutRef.cell(1);
			String luId = "" + luWithoutRef.cell(2);
			//log.info("tdmCopyReferenceListener - Setting status to completed for LU without reference to be handled. Task Execution ID: " + taskExecID + ", lu id: " + luId);
			db(TDM).execute(updateLuWithoutRefSql, COMPLETED, taskId, taskExecID,luId);
		}
		
		if (refTabLst != null) {
			refTabLst.close();
		}
		
		if (lusWithoutRef != null) {
			lusWithoutRef.close();
		}
					
	}
}
