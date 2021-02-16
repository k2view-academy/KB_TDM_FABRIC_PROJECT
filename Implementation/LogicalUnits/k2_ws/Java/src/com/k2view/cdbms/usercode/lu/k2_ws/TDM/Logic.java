/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.k2view.cdbms.FabricEncryption.FabricEncryption;
import com.k2view.cdbms.lut.DbInterface;
import com.k2view.cdbms.lut.InterfacesManager;
import com.k2view.cdbms.shared.Db;


import com.k2view.cdbms.shared.ResultSetWrapper;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;

import com.k2view.cdbms.shared.utils.UserCodeDescribe.out;
import com.k2view.fabric.api.endpoint.Endpoint.MethodType;
import com.k2view.fabric.api.endpoint.Endpoint.Produce;
import com.k2view.fabric.api.endpoint.Endpoint.webService;
import com.k2view.fabric.api.writers.Writers;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;
import org.jetbrains.annotations.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


import static com.k2view.cdbms.usercode.common.SharedGlobals.ORACLE8_DB_TYPE;
import static com.k2view.cdbms.usercode.common.SharedGlobals.COMBO_MAX_COUNT;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
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
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;


@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
//@legacy
public class Logic extends WebServiceUserCode {

	public static final String DB_FABRIC = "fabric";
	public static final String BE_ID = "BE_ID";
	public static final String LU_NAME = "LU_NAME";
	public static final String PARAM_NAME = "PARAM_NAME";
	public static final String PARAM_TYPE = "PARAM_TYPE";
	public static final String VALID_VALUES = "VALID_VALUES";
	public static final String LU_PARAMS_TABLE_NAME = "LU_PARAMS_TABLE_NAME";
	public static final String MAX_VALUE = "MAX_VALUE";
	public static final String MIN_VALUE = "MIN_VALUE";
	public static final String LU_SQL = "SELECT product_id as productID, product_name as productName, lu_id as logicalUnitID, lu_name as logicalUnitName FROM product_logical_units WHERE be_id = ? ORDER BY lu_id";

	public enum PARAM_TYPES{
		COMBO, NUMBER, TEXT;

		public String getName(){
			return this.toString().toLowerCase();
		}
	}

	private static Map<String, Object> wrapWebServiceResults(String errorCode, String message, Object result) {
		HashMap<String, Object> response = new HashMap<>();
		response.put("errorCode", errorCode);
		response.put("message", message);
		response.put("result", result);
		return response;
	}

	@out(name = "envList", type = String.class, desc = "")
	public static Object wsGetAllEnvs() throws Exception {
		try {
			Set<String> envList = InterfacesManager.getInstance().getAllEnvironments();
			envList.remove("_dev");
			return wrapWebServiceResults("SUCCESS", null, envList);
		} catch (Exception e) {
			return wrapWebServiceResults("FAIL", e.getMessage(), null);
		}
						 
							  
	}

	@out(name = "res", type = Object.class, desc = "")
	public static Object wsGetTaskExecSeqVal(String task_execution_id) throws Exception {
		//Sereen - fix : tdm_seq_mapping PG table is deleted so we fetch the data from tdm_seq_mapping fabric table
		//DBExecute(DB_FABRIC, "set sync off", null);
		//DBExecute(DB_FABRIC, "get TDM." + task_execution_id, null);
		ludb().execute( "get TDM." + task_execution_id);
		String sql = "SELECT entity_target_id , lu_type, source_env, table_name, column_name, source_id, target_id, is_instance_id FROM tdm_seq_mapping";
		Db.Rows rows = db(DB_FABRIC).fetch(sql);
		return rows;
	}

	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsPopTDMTaskExecutionStats(String Task_execution_id) throws Exception {
		//DBExecute(DB_FABRIC, "get TDM." + Task_execution_id, null);
		fabric().execute("get TDM.?", Task_execution_id);
		
		//Object[] ValueArr=null;
		
						 
  
		//Tot_copied_entities_per_exe
		String sql1="Select count(distinct entity_id)  from TDM.TASK_EXECUTION_ENTITIES t " +
					"where t.ID_Type = 'entity' and t.Execution_Status = 'completed' and " +
					"not exists (select 1 from TDM.TASK_EXECUTION_ENTITIES t2 where " +
					"t2.entity_id = t.entity_id  and (t2.Execution_Status <> 'completed'" +
					"or t2.Execution_Status is null))";
		//Object rs1 = DBSelectValue(DB_FABRIC,sql1,ValueArr);
		Object rs1 = fabric().fetch(sql1).firstValue();
		
		//Tot_failed_entities_per_exe
		String sql2="Select count(distinct entity_id)  from TDM.TASK_EXECUTION_ENTITIES t " +
					"where t.ID_Type = 'entity' and (t.Execution_Status <> 'completed' " +
					"or t.Execution_Status is null)";
		//Object rs2 = DBSelectValue(DB_FABRIC,sql2,ValueArr);
		Object rs2 = fabric().fetch(sql2).firstValue();
		//Tot_process_ref_tables_per_exe
		String sql3="Select count(distinct entity_id) from TDM.TASK_EXECUTION_ENTITIES t " +
					"where t.ID_Type = 'ref'";
		//Object rs3 = DBSelectValue(DB_FABRIC,sql3,ValueArr);
		Object rs3 = fabric().fetch(sql3).firstValue();
		
		//Tot_copied_ref_tables_per_exe
		String sql4="Select count(distinct entity_id) from TDM.TASK_EXECUTION_ENTITIES t " + 
					"where t.ID_Type = 'ref' and t.Execution_Status='completed' and not exists " +
					"(select 1 from TDM.TASK_EXECUTION_ENTITIES t2 where t2.entity_id = t.entity_id " +  
					"and (t2.Execution_Status <> 'completed' or t2.Execution_Status is null))";
		//Object rs4 = DBSelectValue(DB_FABRIC,sql4,ValueArr);
		Object rs4 = fabric().fetch(sql4).firstValue();
		
		//Tot_failed_ref_tables_per_exe
		String sql5="Select count(distinct entity_id) from TDM.TASK_EXECUTION_ENTITIES t " +
					"where t.ID_Type = 'ref' and t.Execution_Status = 'completed' and not exists " +
					"(select 1 from TDM.TASK_EXECUTION_ENTITIES t2 where t2.entity_id = t.entity_id " +
					"and (t2.Execution_Status <> 'completed' or t2.Execution_Status is null))";
		//Object rs5 = DBSelectValue(DB_FABRIC,sql5,ValueArr);
		Object rs5 = fabric().fetch(sql5).firstValue();
		
		 Object[] arr = new Object[] {
			 rs1,
			 rs2,
			 rs3,
			 rs4,
			 rs5,	
		};
		
		
		Map <String, Object> values = new LinkedHashMap<String, Object>();
		values.put("Tot_copied_entities_per_exe",rs1);
		values.put("Tot_failed_entities_per_exe",rs2);
		values.put("Tot_process_ref_tables_per_exe",rs3);
		values.put("Tot_copied_ref_tables_per_exe",rs4);
		values.put("Tot_failed_ref_tables_per_exe",rs5);
		
		return values;
	}
	
	@desc("Checks each entity in provided entity list whether it got migrated\r\n" +
			"successfully for the provided task execution id and list of LUs\r\n" +
			"( valid for extracts with versioning only )")
	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Map<String,Object> wsCheckMigrateStatusForEntitiesList(String entities_list, String task_execution_id, String lu_list) throws Exception {
		Map <String, Object> entity_mig_status = new LinkedHashMap<String, Object>();
		String [] entities_list_arr = entities_list.split(",");
		String [] lu_list_arr = lu_list.split(",");
		String entities_list_for_qry = "";
		String lu_list_for_qry = "";
		
		//create a string with added single quotes to each entity in the entities list + preliminary mark every entity in the list as exists,
		//since the entity was already validated against the root LU in the GUI
		for (int i=0; i< entities_list_arr.length; i++) {
			entity_mig_status.put(entities_list_arr[i].trim(),"true");
			entities_list_for_qry += "'" + entities_list_arr[i].trim() + "',";
		}
		// remove last ,
		entities_list_for_qry = entities_list_for_qry.substring(0,entities_list_for_qry.length()-1);
		
		for (int i=0; i< lu_list_arr.length; i++) {
			lu_list_for_qry += "'" + lu_list_arr[i].trim() + "',";
		}
		// remove last ,
		lu_list_for_qry = lu_list_for_qry.substring(0,lu_list_for_qry.length()-1);
		
		fabric().execute("GET TDM.?", task_execution_id);
		
		String getStatusesSql = "select distinct be_root_entity_id from task_execution_link_entities " + 
			"where be_root_entity_id in (" + entities_list_for_qry + ") and lu_name in (" + lu_list_for_qry + ") and execution_status <> 'completed'";
		
		//log.info("wsCheckMigrateStatusForEntitiesList - Query: " + getStatusesSql);
		
		Db.Rows getStatuses = fabric().fetch(getStatusesSql);
		//Only entities that failed on any of the LUs will be returned, therefore for all the returned entities set the status to false
		for (Db.Row entityStatus : getStatuses) {
			entity_mig_status.put("" + entityStatus.cell(0), "false");
		}

		return wrapWebServiceResults("SUCCESS", null, entity_mig_status);
	}

	@desc("WS to return connected details")
	@out(name = "Host", type = String.class, desc = "")
	@out(name = "Port", type = String.class, desc = "")
	@out(name = "DbSchema", type = String.class, desc = "")
	@out(name = "User", type = String.class, desc = "")
	@out(name = "EncryptPass", type = String.class, desc = "")
	public static Object wsConnectionDetails(String interfaceName, String environmentName) throws Exception {
		//DbInterface dbObj = (DbInterface)com.k2view.cdbms.lut.InterfacesManager.getInstance().getInterface(interfaceName,environmentName);
		
		// 1-May-19- fix the call to the interface manager (Miron's fix)
		DbInterface dbObj = com.k2view.cdbms.lut.InterfacesManager.getInstance().getTypedInterface(interfaceName,environmentName);
		
		// Tali- Fix- Add the check if dbObj is null
		if(dbObj == null)
		{
			//log.info("Interface name "+ interfaceName+ " was not found for environment " + environmentName); 
			return null;
		}
				
		String Host = dbObj.dbHost;
		int    Port = dbObj.dbPort;
		String User = dbObj.dbUser;
		String EncryptPass = FabricEncryption.encrypt(dbObj.getPassword());
		String DbSchema = dbObj.dbScheme;
		
		return wrapWebServiceResults("SUCCESS", null, new String[] {Host, Integer.toString(Port), DbSchema, User, EncryptPass});

	}

	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsMigrateEntitiesForTdmExtract(String luName, String dcName, String sourceEnvName, String taskName, String versionInd, String entitiesList, String retentionPeriodType, Float retentionPeriodValue, String globals, String taskExecutionId, String parentLuName, String versionDateTime, String syncMode) throws Exception {
		return fnMigrateEntitiesForTdmExtract(luName, dcName, sourceEnvName, taskName, versionInd, entitiesList, retentionPeriodType, retentionPeriodValue, globals, taskExecutionId, parentLuName, versionDateTime, syncMode);
	}

	@desc("Add a reference handling for TDM 5.1")
	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsStopTaskExecution(Long task_execution_id) throws Exception {
		Db.Rows batchIdList = null;
		try {
			//TDM 6.0 - Get the list of migration IDs based on task execution ID, instead of getting one migrate_id as input
			batchIdList = db("TDM").fetch("select fabric_execution_id, execution_status, l.lu_id, lu_name from task_execution_list l, tasks_logical_units u where task_execution_id = ? " +
						"and fabric_execution_id is not null and UPPER(execution_status) IN " +
						"('RUNNING','EXECUTING','STARTED','PENDING','PAUSED','STARTEXECUTIONREQUESTED') " +
						"and l.task_id = u.task_id and l.lu_id = u.lu_id" , task_execution_id);
			
			db("TDM").execute("UPDATE task_execution_list SET execution_status='stopped' where task_execution_id = ? and execution_status != 'completed'",
						task_execution_id);
			// TDM 5.1- add a reference handling- update the status of the reference tables to 'stopped'.
			// The cancellation of the jobs for the tables will be handled by the new fabric listener user job for the reference copy.
			
			db("TDM").execute("UPDATE task_ref_exe_stats set execution_status='stopped', number_of_processed_records = 0 where task_execution_id = ? " +
						"and execution_status != 'completed'", task_execution_id);
			
			// TDM 7, set the execution summary to stopped also
			db("TDM").execute("UPDATE task_execution_summary SET execution_status='stopped' where task_execution_id = ? and execution_status != 'completed'",
						task_execution_id);	

			// TDM 5.1- cancel the migrate only if the input migration id is not null
			//TDM 6.0 - Loop over the list of migrate IDs
			for (Db.Row batchInfo : batchIdList)
			{
				String fabricExecID = "" + batchInfo.get("fabric_execution_id");
				Long luID = (Long) batchInfo.get("lu_id");
				String luName = "" + batchInfo.get("lu_name");
				String taskExecutionID = "" + task_execution_id;
				ludb().execute("cancel batch '" + fabricExecID +"'");
				
				if (luID > 0) {
					fnTdmUpdateTaskExecutionEntities(taskExecutionID, luID, luName);
				}
			}
			return wrapWebServiceResults("SUCCESS", null, null);
		}
		catch (Exception e){
			log.error("wsStopTaskExecution", e);
			return wrapWebServiceResults("FAIL", null, null);

		} finally {
			if(batchIdList != null) {
				batchIdList.close();
			}
		}
	}

	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsResumeTaskExecution(Long task_execution_id) throws Exception {
		Boolean success_ind = true;
		Db.Rows batchIdList = null;
		try {
			//TDM 6.0 - Get the list of migration IDs based on task execution ID, instead of getting one migrate_id as input
			batchIdList = db("TDM").fetch("select fabric_execution_id, execution_status, selection_method from task_execution_list l, tasks t " +
						"where task_execution_id = ? and l.task_id = t.task_id and selection_method <> 'REF'" +
						"and fabric_execution_id is not null", task_execution_id);
				
			db("TDM").execute("UPDATE task_execution_list SET execution_status='running' where fabric_execution_id is not null " +
				"and lower(execution_status) = 'stopped' and task_execution_id = ?",
				task_execution_id);
			
			// TDM 7, set the status in execution summary to running
			db("TDM").execute("UPDATE task_execution_summary SET execution_status='running' where task_execution_id = ? and execution_status != 'stopped'",
						task_execution_id);
			db("TDM").execute("UPDATE task_execution_list SET execution_status='pending' where fabric_execution_id is null and task_execution_id = ? " +
				"and lower(execution_status) = 'stopped' and task_id in (select task_id from tasks where lower(selection_method) <>'ref')",
				task_execution_id);
			
			// TDM 5.1- add a reference handling- update the status of the reference tables to 'resume'.
			// The resume of the jobs for the tables will be handled by the new fabric listener user job for the reference copy.
			
			db("TDM").execute("UPDATE task_execution_list l SET execution_status='pending' where fabric_execution_id is null and task_execution_id = ? " +
				"and (task_execution_id, lu_id) in (select task_execution_id, lu_id from task_ref_exe_stats r,  tasks_logical_units u, task_ref_tables s " +
				"where l.task_execution_id = r.task_execution_id and l.lu_id = u.lu_id " +
				"and l.task_id = u.task_id and u.lu_name = s.lu_name and s.task_ref_table_id = r.task_ref_table_id and s.task_id = l.task_id " + 
				"and lower(r.execution_status) = 'stopped')",
				task_execution_id);
			db("TDM").execute("UPDATE task_ref_exe_stats set execution_status= 'resume' where task_execution_id = ? and lower(execution_status) = 'stopped'", task_execution_id);
			
			
			// TDM 5.1- cancel the migrate only if the input migration id is not null
			//TDM 6.0 - Loop over the list of migrate IDs
			for (Db.Row batchInfo : batchIdList)
			{
				ludb().execute("batch_retry '" + batchInfo.cell(0) +"'");
			}
		}
		catch (Exception e){
			success_ind = false;
			log.error("wsResumeTaskExecution: " + e);
										   
		} finally {
			if (batchIdList != null) {
				batchIdList.close();
			}
		}

		return wrapWebServiceResults((success_ind ? "SUCCESS" : "FAIL"), null, success_ind);
	}


	@desc("WebService to return all successfull entities from given migration ID")
	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsGetIIDFromK2migrate(String fabricExecutionId, Integer entitiesArrarySize) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		
		Map <String, Map> Map_Outer = new LinkedHashMap<String, Map>();
		int totNumOfCopiedEntities= 0;
		int totNumOfFailedEntities= 0;
		
		
		//Get copied entities ( statuses ADDED/UNCHANGED/UPDATED are considered copied successfully
		List <Object> Copied_entities_list =  new ArrayList<Object>();
		
		Db.Rows mig_details_completed  = fabric().fetch("migrate_details '" + fabricExecutionId + "' STATUS='COMPLETED'");
		for (Db.Row row:mig_details_completed) {
			Map <Object, Object> innerCopiedEntitiesMap = new HashMap <Object, Object>();
			Map <Object, Object> innerCopiedUIDMap = new HashMap <Object, Object>();
			totNumOfCopiedEntities++;
			if(entitiesArrarySize == -1 || totNumOfCopiedEntities <= entitiesArrarySize)
			{
				// TDM 5.1- fix the split of the iid to get the entityId and support configurable separators for entityId
				Object[] split_iid = fnSplitUID("" + row.cell(0));
				String entityId = split_iid[0].toString();
				//log.info("UID: " + row[1].toString() + " , entityID: " + entityId);
			
				innerCopiedEntitiesMap.put(entityId, entityId);
				innerCopiedUIDMap.put(entityId, row.cell(0));
				Copied_entities_list.add((Object)innerCopiedEntitiesMap);
			}	
		}
		if (mig_details_completed != null) {
			mig_details_completed.close();
		}
		
		//add copied entities results to Map_Outer
		LinkedHashMap<String,Object> m1 = new LinkedHashMap<String,Object>();
		//log.info("fnGetIIDListForMigration - Num of Copied Entities: " + totNumOfCopiedEntities);
		m1.put("numOfEntities",totNumOfCopiedEntities);
		m1.put("entitiesList",Copied_entities_list);
		Map_Outer.put("Copied entities per execution",m1);
		
		// Get failed entities
		List <Object> Failed_entities_list =  new ArrayList<Object>();
		Db.Rows mig_details_failed  = fabric().fetch("migrate_details '" + fabricExecutionId + "' STATUS='FAILED'");
		for (Db.Row row:mig_details_failed) {
			Map <Object, Object> innerFailedEntitiesMap = new HashMap <Object, Object>();
			Map <Object, Object> innerFailedUIDMap = new HashMap <Object, Object>();
			totNumOfFailedEntities++;
			if(entitiesArrarySize == -1 || totNumOfFailedEntities <= entitiesArrarySize)
			{
				// TDM 5.1- fix the split of the iid to get the entityId and support configurable separators for entityId
				Object[] split_iid = fnSplitUID("" + row.cell(0));
				String entityId = split_iid[0].toString();
				//log.info("UID: " + row[1].toString() + " , entityID: " + entityId);
			
				innerFailedEntitiesMap.put(entityId, entityId);
				innerFailedUIDMap.put(entityId, row.cell(0));
				Failed_entities_list.add((Object)innerFailedEntitiesMap);
			}	
		}
		if (mig_details_failed != null) {
			mig_details_failed.close();
		}
		
		//add failed entities results to Map_Outer
		LinkedHashMap<String,Object> m2 = new LinkedHashMap<String,Object>();
		//log.info("fnGetIIDListForMigration - Num of Failed Entities: " + totNumOfFailedEntities);
		m2.put("numOfEntities",totNumOfFailedEntities);
		m2.put("entitiesList",Failed_entities_list);
		Map_Outer.put("Failed entities per execution",m2);
		
		return Map_Outer;
	}


	@out(name = "refTablesList", type = Object.class, desc = "")
	public static Object wsGetRefTablesByLu(String luArray) throws Exception {
		//Map <String, String> refTablesList = new LinkedHashMap<String, String>();
		List<Object> refTablesList = new ArrayList<Object>();
				
		Map<String,Map<String, String>> trnRefListValues = getTranslationsData("trnRefList");
		
		List<String> luNamesList = new ArrayList<String>();
				
		// Parse the input array of LUs
		
		try {
		
			JSONArray jArray = new JSONArray(luArray);
			 for (int i=0;i< jArray.length(); i++){
					//DEBUG
				    //log.info("Input LU Name: " + jArray.get(i).toString());
		            luNamesList.add(jArray.get(i).toString());
		        }
			 
		} catch (JSONException e) {
		log.error(e.getMessage());
		}
		
		// Scan the translation  by its keys. The key contains the LU name + ID
		Set<String> keys = trnRefListValues.keySet();
		String luName;
		String prevLuName="";
		for(String trnLuKey:keys){
			// Get the LU name from the key. Key example- PATIENT_LU@:@1
			luName = trnLuKey.substring(0, trnLuKey.indexOf("@") );
		    
			// DEBUG
			//log.info("Key: "+ trnLuKey + " lu name of the translation key: " + luName);
		
			if(luNamesList.contains(luName)) {
				//DEBUG 
		  	 // log.info("luNamesList contains " + luName );

				//StringBuffer refInfoStr = new StringBuffer();
				JSONObject refInfo=null;
				//refInfoStr.put("logical_unit_name: " + luName + "," );
				refInfo = new JSONObject();
				refInfo.put("logical_unit_name", luName);

				Set<String> internalKeys = trnRefListValues.get(trnLuKey).keySet();
				String paramValue;

				for(String paramKey:internalKeys) {
					paramValue = trnRefListValues.get(trnLuKey).get(paramKey);
					//log.info(paramKey + ": " + paramValue);
					refInfo.put(paramKey, paramValue);
					//refInfoStr.append(paramKey + ","  + paramValue);

				}

				//log.info( "refInfo JSON: " + refInfo.toString());
				//refTablesList.add(refInfoStr);
		        //refTablesList.add("logical_unit_name: " + luName);
		        //refTablesList.add(trnRefListValues.get(trnLuKey));
				 refTablesList.add(refInfo.toString());
		
			  }
			// DEBUG
			// log.info(trnLuKey+" -- "+trnRefListValues.get(trnLuKey));
					
		}
		
		return wrapWebServiceResults("SUCCESS", null, refTablesList);
	}

	//This WS is deprecated and wsGetTDMTaskExecutionStats should be used instead
	@Deprecated
	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
													  
	public static Object wsGetDetailedListForExtractTask(String fabricExecutionId, String refTaskExecutionId, Integer entitiesArrarySize) throws Exception {
		// Check if the fabricExecutionId is populated. 
		// If yes- call the wsGetIIDFromK2migrate WS to get information about the copied and fauiled entities
		
		Object migrateIIDList= null;
		//ResultSetWrapper migrateRefList= null;
		Db.Rows migrateRefList= null;
		Map <String, Map> extractTaskStats= new LinkedHashMap <String, Map>();
		Map <String, Map> innerRefTablesList= new LinkedHashMap <String, Map>();
		
		// Get the list of entities
		if((fabricExecutionId != null) && !fabricExecutionId.isEmpty())
		{
			migrateIIDList = fnGetIIDListForMigration(fabricExecutionId, entitiesArrarySize);
		    extractTaskStats.putAll((Map)migrateIIDList);
		}
		else // input fabric execution id is null (reference only task)
		{
			Map <String, Object> innerMap = new LinkedHashMap <String, Object>();
			List entitiesList = new ArrayList();
		
		    innerMap.put("numOfEntities",0);	
			innerMap.put("entitiesList",entitiesList);
			
			extractTaskStats.put("Failed entities per execution", innerMap);
			extractTaskStats.put("Copied entities per execution", innerMap);
		
		 }
		
		// Get the list of reference tables
		if(refTaskExecutionId!= null && !refTaskExecutionId.isEmpty())
		{
			//migrateRefList = (ResultSetWrapper)fnGetReferenceDetailedData(refTaskExecutionId);
			migrateRefList= (Db.Rows)fnGetReferenceDetailedData(refTaskExecutionId);
			List <Object> copiedTables = new ArrayList<Object>();
			List <Object> failedTables = new ArrayList <Object>();
			Integer no_of_copied_tables=0;
			Integer no_of_failed_tables = 0;
		
			//for (Object[] row : migrateRefList) {
			for (Db.Row row : migrateRefList) {
				String refStatus = "" + row.cell(1);
				String refTableName = "" + row.cell(0);
		
				if(refStatus.equals("completed"))
		              {
		                  no_of_copied_tables++;
		                  if(no_of_copied_tables <= entitiesArrarySize)
		                      copiedTables.add(refTableName);
		              }
		              else
		              {
		                  no_of_failed_tables++;
		                  if(no_of_failed_tables <= entitiesArrarySize)
		                      failedTables.add(refTableName);
		              }
		
			}// end of for loop
			if (migrateRefList != null) {
				//migrateRefList.closeStmt();
				migrateRefList.close();
			}
			
		    Map <String, Object> failedMap = new LinkedHashMap<String, Object>();
			Map <String, Object> copiedMap = new LinkedHashMap<String, Object>();
		
			failedMap.put("numOfEntities", no_of_failed_tables);
		    failedMap.put("entitiesList", failedTables);
		    copiedMap.put("numOfEntities", no_of_copied_tables);
		    copiedMap.put("entitiesList", copiedTables);
		
		    innerRefTablesList.put("Copied Reference per execution", copiedMap);
		    innerRefTablesList.put("Failed Reference per execution", failedMap);
		
		    extractTaskStats.putAll(innerRefTablesList);
		
		}// end of if
		else // if the task does not have reference tables
		{
			Map <String, Object> innerMap = new LinkedHashMap <String, Object>();
			List refList = new ArrayList();
			innerMap.put("numOfEntities",0);	
			innerMap.put("entitiesList",refList);
			extractTaskStats.put("Copied Reference per execution", innerMap);
			extractTaskStats.put("Failed Reference per execution", innerMap);
			
		}
		
		return extractTaskStats;
	}




	@out(name = "res", type = String.class, desc = "")
	public static String wsDecryptPwd(String passTodDcrypt) throws Exception {
		//log.info("TEST-SHAI-Input pwd: "+ passTodDcrypt);
		String decryptPwd = FabricEncryption.decrypt(passTodDcrypt);
		
		//log.info("TEST-SHAI-after calling Fabric: "+ decryptPwd);
		
		return decryptPwd;
		//FabricEncryption.decrypt(passTodDcrypt);
	}

	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsTestConnectionForEnv(String env) throws Exception {
		Log log = Log.a(Logic.class);
		
		if(Util.isEmpty(env)){
			env = "_dev";
		}
		fabric().execute("set environment='" + env + "';");
		
		Map<String,String> connResMap= new HashMap<>();
		Db.Rows dbSources = fabric().fetch("list DB_SOURCES");
		
		for (Db.Row source : dbSources) {
			Boolean active = Boolean.valueOf("" + source.cell(1));
			String iface = "" + source.cell(0);
			if(active){

				String connRes = "" + fabric().fetch( "test_connection DbInterface=?", iface).firstRow().cell(2);
				connResMap.put(iface, connRes);
			}else{
				connResMap.put(iface, ", this interface is not active");
			}
			
		}
		if (dbSources != null) dbSources.close();
		
		return wrapWebServiceResults("SUCCESS", null, connResMap);
	}








	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsExtractRefStats(String i_taskExecutionId, String i_runMode) throws Exception {
		Object rs =null;
		
		if (i_runMode.equals("S")) {
   
			//log.info("call to fnGetReferenceSummaryData");
			Map <String, Object> refSummaryStatsBuf = fnGetReferenceSummaryData(i_taskExecutionId);
			
			// Convert the dates to strings
			refSummaryStatsBuf.forEach((key, value) -> {
				String minStartDate ="";
				String maxEndDate="";
				Map <String, Object> refSummaryStats = (HashMap) value;
			
				if (refSummaryStats.get("minStartExecutionDate")!=null)
					minStartDate = refSummaryStats.get("minStartExecutionDate").toString();
			
				if (refSummaryStats.get("maxEndExecutionDate")!= null)
			 		maxEndDate = refSummaryStats.get("maxEndExecutionDate").toString();
			
				refSummaryStats.put("minStartExecutionDate", minStartDate);
				refSummaryStats.put("maxEndExecutionDate", maxEndDate);
			
				//log.info("after calling fnGetReferenceSummaryData");
				});
			rs = refSummaryStatsBuf;
		} else if (i_runMode.equals("D")) {
 			rs = fnGetReferenceDetailedData(i_taskExecutionId);
		}

		// convert iterable to serializable object
		if (rs instanceof Db.Rows) {
			ArrayList<Map> rows = new ArrayList<>();
			((Db.Rows) rs).forEach(row -> {
				HashMap copy = new HashMap();
				copy.putAll(row);
				rows.add(copy);
			});
			rs = rows;
		}

		return wrapWebServiceResults("SUCCESS", null, rs);
	}

	@desc("This WS gets the hierarchy of the given entity. It gets first the hierarchies of its children and then for the hierarchy of the ancestors if they exists")
	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsGetTaskExeStatsForEntity(String taskExecutionId, String luName, String targetId) throws Exception {
		String sqlGetEntityData = "select lu_name luName, target_entity_id targetId, entity_id sourceId, " +
			"execution_status luStatus from TDM.task_Execution_link_entities  " +
			"where lu_name <> ? and target_entity_id = ? and entity_id = ?";
		
		String sqlGetParent = "select parent_lu_name, target_parent_id from TDM.task_Execution_link_entities " +
			"where lu_name= ? and target_entity_id = ? and parent_lu_name <> ''";
		
		Map <String, Object> mainOutput = new HashMap<>();
		Map <String, Object> childHierarchyDetails = new HashMap<>();
		Map <String, Object> parentHierarchyDetails = new HashMap<>();
			
		Db.Row entityDetails = null;
		Boolean countChildren = false;
				
		fabric().execute( "get TDM." + taskExecutionId);
		
		//Get the Hierarchy starting from the given entity and below
		childHierarchyDetails = fnGetChildHierarchy(luName, targetId);
		
		String parentLuName = "";
		String parentTargetId = "";
				
		// Get the parent of the given LU, to see if there is a reason to get the ancestors or not
		Db.Row parentRec = fabric().fetch(sqlGetParent, luName, targetId).firstRow();
		
		if (!parentRec.isEmpty()) {
			//log.info("There is a parent: " + parentRec.cell(0));
			parentLuName = "" + parentRec.cell(0);
			parentTargetId = "" + parentRec.cell(1);
		}
		//If the the input entity has parents get the hierarchy above it
		if (parentLuName != null && !"".equals(parentLuName)) {
			//log.info("wsGetTaskExeStatsForEntity - parent Rec: Lu Name: " + parentLuName + ", Parent target ID: " + parentTargetId);
			//Starting for the parent as the details of the input entity is already included in the children part
			//Sending the chilren hierarchy in order to add it to the ancestors as child hierarchy
			parentHierarchyDetails = fnGetParentHierarchy(parentLuName, parentTargetId, childHierarchyDetails);
		} else {// Given inputs are of a root entity
			//log.info("The given LU is a root");
			parentHierarchyDetails =  childHierarchyDetails;
		}
		
		String rootLUName = "" + parentHierarchyDetails.get("luName");
		String rootTargetID = "" + parentHierarchyDetails.get("targetId");
		String rootSourceID = "" + parentHierarchyDetails.get("sourceId");
		//log.info ("wsGetTaskExeStatsForEntity - rootLUName: " + rootLUName + ", rootTargetID: " + rootTargetID + ", rootSourceID: " + rootSourceID);
		
		mainOutput.put(rootLUName, parentHierarchyDetails);
		//If there are other root entities with the same root entity ID get them, 
		//they will be added to output as standalone (even if they have their own hierarchy)
		Db.Rows otherRootRecs = fabric().fetch(sqlGetEntityData, rootLUName, rootTargetID, rootSourceID);
		for (Db.Row rootRec : otherRootRecs) {
			Map <String, Object> rootDetails = new HashMap<>();
			String currRootLuName = "" + rootRec.cell(0);
			rootDetails.put("luName", currRootLuName);
			rootDetails.put("targetId", "" + rootRec.cell(1));
			
			//Get instance ID from entity id
			Object[] splitId = fnSplitUID("" + rootRec.cell(2));
			String instanceId = "" + splitId[0];
			rootDetails.put("sourceId", "" + instanceId);
			
			rootDetails.put("entityStatus", "" + rootRec.cell(3));
			
			mainOutput.put(currRootLuName, rootDetails);
		}
		if (otherRootRecs != null) {
			otherRootRecs.close();
		}
		return wrapWebServiceResults("SUCCESS", null, mainOutput);
					
	}




	@desc("This WS is invoked by the ADI WS when the test connection fails using the ADI linux server. For example- Informix DB.\r\n" +
			"URL-\r\n" +
			" $url .= \"ws?methodName=wsTestConnection&token=\" . $wsToken . \"&dbType=\" . $this->dbType . \"&dbHost=\" . $this->dbHost. \"&dbPort=\" . $this->dbPort . \"&dbUser=\" . $this->dbUser . \"&dbPass=\" . $this->dbPassword . \"&dbScheme=\" . $this->dbScheme ;")
	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Boolean wsTestConnection(String dbType, String dbHost, String dbPort, String dbUser, String dbPass, String dbScheme) throws Exception {
		//Decrypt the pwd
		          
		String decryptPwd = FabricEncryption.decrypt(dbPass);
		          
		Boolean testConn=false; 
		//log.info(" dbType= " + dbType +", dbHost= " + dbHost + " dbPort= +" + dbPort + " dbUser= " +dbUser + " dbPass= " + dbPass + " dbScheme= " + dbScheme);
		//  log.info("TEST2- after calling Fabric: "+ decryptPwd);
		          
		// Build the JDBC connection string
		          
		String connStr="";
		switch(dbType.toLowerCase())
		{
		       case "informix":
		             connStr= "jdbc:informix-sqli://"+ dbHost + ":" + dbPort + "/" + dbScheme + ":DELIMIDENT=Y";
		            break;
		                                          
		       case "teradata":
		             connStr= "jdbc:teradata://"+dbHost+"/DBS_PORT="+dbPort+",DATABASE="+dbUser+",CHARSET=UTF8";
		            break;
		                          
		       case "db2":
		             connStr="jdbc:db2://"+dbHost+":"+dbPort+"/"+dbScheme;
		            break;
		 
		// 8-9-19- add oracle string for Oracle 8 as well as other Oracle which are not validated
		// from GUI
		case "oracle":
			connStr = "jdbc:oracle:thin:@"+dbHost+":"+dbPort+"/"+dbScheme;
		  break;
		 
		// 8-8-19- add Sybase_ASE
		case "sybase_ase":
		// Sybase_ASE example
		// jdbc:sybase:Tds:10.21.3.4:5000/TALI
		connStr="jdbc:sybase:Tds:"+dbHost+":"+dbPort+"/"+dbScheme;
		  break;
			
		// 8-8- add sqlServer
		case "sqlserver":
		//jdbc:sqlserver://vpn.k2view.com:49623;database=k2vtest
		connStr="jdbc:sqlserver://"+dbHost+":"+dbPort+";database="+dbScheme;
		 break;
		                                                                                                          
		       default:
		 // Handle Sybase_IQ or Sybase Anywhere
		             if(dbType.toLowerCase().contains("sybase"))
		                    connStr="jdbc:sybase:Tds:"+dbHost+":"+dbPort+"/";
		}
		          
		//log.info("Test- connection string: "+ connStr);
		//log.info("Test- connection string: "+ connStr);
		
		// Fix-19-Aug-19- add try and catch
		try
		{
			// test connection
			if(!connStr.equals(""))
			{
				 Object[] params = new Object[]{"testDB", dbType, connStr, dbUser, dbPass};
				 //String connRes = DBQuery(DB_FABRIC, "test_connection InterfaceName=? DatabaseType=? ConnectionString =? DatabaseUserId=? DatabasePassword=?", params).getFirstRow()[0].toString();
				 String testCmd = "test_connection InterfaceName='testDB' DatabaseType='" + dbType + "' ConnectionString = '" + connStr + "' DatabaseUserId= '"+ dbUser + "' DatabasePassword='"+ dbPass + "'";
				 // log.info("test connection command: " + testCmd);
				 //   log.info("test connection command: " + testCmd + "  -SHAI-   ");
				
				//String connRes = DBQuery(DB_FABRIC, testCmd, null).getFirstRow()[0].toString();
				String connRes = "" + fabric().fetch(testCmd).firstRow();
				 //   log.info("conn status: " + connRes);
				 //    log.info("conn status: " + connRes);
				
				// Tali- 13-Aug- if the test connection fails and tghe dbType is Oracle- rebuild the connection string to support Oracle 8
				
				if(connRes.equals("true"))
				{
					testConn=true;
				}
				else  
				{
					if(dbType.toLowerCase().equals("oracle"))
					{	// Example for Oracle 8:  jdbc:oracle:thin:@135.21.217.92:1521:aens1
						connStr = "jdbc:oracle:thin:@"+dbHost+":"+dbPort+":"+dbScheme;
						
						// 2-Sep-19- set the dbType to the global value for Oracle8
						dbType = ORACLE8_DB_TYPE;
						testCmd = "test_connection InterfaceName='testDB' DatabaseType='" + dbType + "' ConnectionString = '" + connStr + "' DatabaseUserId= '"+ dbUser + "' DatabasePassword='"+ dbPass + "'";
				
						//   log.info("test connection command2: " + testCmd + "     ");
						//connRes = DBQuery(DB_FABRIC, testCmd, null).getFirstRow()[0].toString();
						connRes = "" + fabric().fetch(testCmd).firstRow();
				
						//    log.info("conn status2: " + connRes);
						if(connRes.equals("true"))
							testConn=true;
				
					}// end of if(dbType.toLowerCase().equals("oracle"))
				
				} // end of else 
		}// end of if(!connStr.equals(""))
		}catch (Exception e){
			log.error("wsTestConnection", e);
			testConn=false;
		}            
		   return testConn; 
	}

	@desc("This WS returns the statistics of the given task execution id and given LU name and other parameters")
	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsGetTDMTaskExecutionStats(String taskExecutionId, String luName, String luEntityId, String luIdType, String luParentId, Integer entitiesArraySize, String displayErrorPath) throws Exception {
		//Tali- 4-Dec-18- fix the queries- select distinct the combination of be_root_entity_id + TARGET_ROOT_ENTITY_ID when selecting the number of copied/failed entities. This is required to support the clone of one entity X times by a task
		
		//log.info("wsGetTDMTaskExecutionStats - Inputs:");
		//log.info("taskExecutionId: <" + taskExecutionId + ">, luName: <" + luName + ">, luEntityId: <" + luEntityId + ">, luIdType: <" +
		//		luIdType + ">, luParentId: <" + luParentId + ">, entitiesArraySize: <" + entitiesArraySize + ">, displayErrorPath: <" + displayErrorPath + ">");
		boolean isRootLu = true;
		Map <String, Map> Map_Outer = new LinkedHashMap<>();
		
		List <Object> copiedEntitiesList =  new ArrayList<>();
		List <Object> failedEntitiesList =  new ArrayList<>();
		List <Object> copiedRefEntitiesList =  new ArrayList<>();
		List <Object> failedRefEntitiesList =  new ArrayList<>();
		
		Map <String, Object> mapInnerCopiedBuf = new HashMap <>();
		Map <String, Object> mapInnerFailedBuf = new HashMap <>();
		Map <String, Object> mapInnerCopiedRefBuf = new HashMap <>();
		Map <String, Object> mapInnerFailedRefBuf = new HashMap <>();
		Map <String, String> mapRootsStatus = new HashMap <>();
		
		String sqlSelect = "select ENTITY_ID as sourceId, TARGET_ENTITY_ID as targetId, BE_ROOT_ENTITY_ID as rootSourceId, " +
					"TARGET_ROOT_ENTITY_ID as rootTargetId, PARENT_LU_NAME as parentLuName, PARENT_ENTITY_ID as parentSourceId, " +
					"TARGET_PARENT_ID as parentTargetId, " +
					"Case when EXECUTION_STATUS ='completed' then 'Copied' else 'Failed' end as copyEntityStatus, " +
					"Case when ROOT_ENTITY_STATUS <> 'completed' then 'Failed' else 'Copied' end as copyHierarchyStatus, " +
					"LU_NAME as luName " +
					"from TDM.TASK_EXECUTION_LINK_ENTITIES t1 where ";
		
		String sqlSelectOrder = " order by TARGET_ENTITY_ID";
		String sqlSelectCnt = "select count(1) from TDM.TASK_EXECUTION_LINK_ENTITIES t1 where ";
		
		fabric().execute("get TDM.?", taskExecutionId);
		String entity_id_alone ="";
		if (entitiesArraySize == null) entitiesArraySize = 100;
		//Check if the given LU is a root LU
		String sqlCheckParent = "SELECT PARENT_LU_NAME FROM TDM.TASK_EXECUTION_LINK_ENTITIES WHERE LU_NAME = ? LIMIT 1";
		String parentLuName = "" + fabric().fetch(sqlCheckParent,luName).firstValue();
		if(!("".equals(parentLuName))) {
		    isRootLu = false;
		}
		//log.info("wsGetTDMTaskExecutionStats - isRootLu: " + isRootLu + ", luIdType: " + luIdType);
		//***** Entities lists *****//
		//
		
		String sqlCompEntities = "";
		Db.Rows compEntitiesBuf = null;
		String sqlCompCount = "";
		String compEntitiesCnt = "";
		
		String sqlFailedEntities = "";
		Db.Rows failedEntitiesBuf = null;
		String sqlFailedCount = "";
		String failedEntitiesCnt = "";
		
		List<String> luEntityIdList = new ArrayList<>();
		
		// TDM 6.0 - Check if the parent LU name is given or not
		// Get Completed entities
		
		if ("ENTITY".equals(luIdType)) {//If looking only for reference fo directly to reference section
			if (isRootLu) { //If root LU
				
				//log.info("wsGetTDMTaskExecutionStats - Handling Root Entity");
					
				//lu Entity ID of the root is not given then set the query to get all entities of the given root LU
				if ( (luEntityId == null || luEntityId.isEmpty())) {
					
					//log.info("wsGetTDMTaskExecutionStats - luEntityId of the root is not provided, retrieving it");
					
					sqlCompEntities = sqlSelect + 
						"lu_name = ? and root_entity_status = 'completed' " +
						sqlSelectOrder + " limit ?";
		
					compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, entitiesArraySize);
					//log.info("wsGetTDMTaskExecutionStats - After getting the copied root entities");
					sqlCompCount = sqlSelectCnt +
						"lu_name = ? and root_entity_status = 'completed'";
					
					compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName).firstValue();
					//log.info("wsGetTDMTaskExecutionStats - After getting the count of copied root entities");
					
					sqlFailedEntities = sqlSelect +
						"lu_name = ? and root_entity_status <> 'completed' " +
						sqlSelectOrder + " limit ?";
			
					failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, entitiesArraySize);
					//log.info("wsGetTDMTaskExecutionStats - After getting the failed root entities");
					sqlFailedCount = sqlSelectCnt +
						"lu_name = ? and root_entity_status <> 'completed'";
					
					failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName).firstValue();
					//log.info("wsGetTDMTaskExecutionStats - After getting the count of failed root entities");
				} else {// luEntityId is given
					
					sqlCompEntities = sqlSelect + 
					"lu_name = ? and root_entity_status = 'completed' and target_entity_id = ? " +
					sqlSelectOrder + " limit ?";
					
					compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, luEntityId, entitiesArraySize);
			
					sqlCompCount = sqlSelectCnt +
						"lu_name = ? and root_entity_status = 'completed' and target_entity_id = ?";
					
					compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName, luEntityId).firstValue();
					
					sqlFailedEntities = sqlSelect +
						"lu_name = ? and target_entity_id = ? and root_entity_status <> 'completed' " +
						sqlSelectOrder + " limit ?";
			
					failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, luEntityId, entitiesArraySize);
					
					sqlFailedCount = sqlSelectCnt +
						"lu_name = ? and target_entity_id = ? and root_entity_status <> 'completed' ";
					
					failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName, luEntityId).firstValue();
				}		
			} else {//if not a root LU
				//log.info("The LU is not a root LU");
				if ( !(luEntityId == null || luEntityId.isEmpty()) ) {//If entity ID is given and looking for entities and not reference
					//log.info("luEntityId provided: " + luEntityId);
					sqlCompEntities = sqlSelect +
						"LU_NAME = ? and target_entity_id = ? and execution_status = 'completed' and root_entity_status = 'completed' " +
						sqlSelectOrder;
					
					compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, luEntityId);
					
					sqlCompCount = sqlSelectCnt +
						"LU_NAME = ? and target_entity_id = ? and execution_status = 'completed' and root_entity_status = 'completed' ";
					
					compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName, luEntityId).firstValue();
			
					sqlFailedEntities = sqlSelect +
						"LU_NAME = ? and target_entity_id = ? and (ifNull(execution_status, 'failed') <> 'completed' " +
						"or ifNull(root_entity_status, 'failed') <> 'completed') " +
						sqlSelectOrder;
			
					failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, luEntityId);
					
					sqlFailedCount = sqlSelectCnt +
						"LU_NAME = ? and target_entity_id = ? and (ifNull(execution_status, 'failed') <> 'completed' " +
						"or ifNull(root_entity_status, 'failed') <> 'completed') ";
					
					failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName, luEntityId).firstValue();
		
				} else {//lu Entity ID is not given
					//log.info("luEntityId is not provided");
					if(luParentId == null || luParentId.isEmpty()) {// if parent LU ID is not given
						//log.info("luParentId is not provided");
						sqlCompEntities = sqlSelect +
							"LU_NAME = ? and execution_status = 'completed' and root_entity_status = 'completed' " +
							sqlSelectOrder + " limit ?";
						//log.info("wsGetTDMTaskExecutionStats - sqlCompEntities for non Root without ID: " + sqlCompEntities);
						compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, entitiesArraySize);
						
						sqlCompCount = sqlSelectCnt +
							"LU_NAME = ? and execution_status = 'completed' and root_entity_status = 'completed'";
					
						compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName).firstValue();
			
						sqlFailedEntities = sqlSelect +
							"LU_NAME = ? and (ifNull(execution_status, 'failed') <> 'completed' or root_entity_status <> 'completed') " +
							sqlSelectOrder + " limit ?";
			
						failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, entitiesArraySize);
						
						sqlFailedCount = sqlSelectCnt +
							"LU_NAME = ? and (ifNull(execution_status, 'failed') <> 'completed' or root_entity_status <> 'completed')";
					
						failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName).firstValue();
						
					} else {// Parent LU ID is given
						//log.info("luParentId: " + luParentId);
						sqlCompEntities = sqlSelect +
							"LU_NAME = ? and target_parent_id = ? and execution_status = 'completed' and root_entity_status = 'completed' " + 
							sqlSelectOrder + " limit ? ";
			
						compEntitiesBuf = fabric().fetch(sqlCompEntities, luName,luParentId, entitiesArraySize);
						
						sqlCompCount = sqlSelectCnt +
							"LU_NAME = ? and target_parent_id = ? and execution_status = 'completed' and root_entity_status = 'completed' ";
					
						compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName, luParentId).firstValue();
			
						sqlFailedEntities = sqlSelect +
							"LU_NAME = ? and target_parent_id = ? and (ifNull(execution_status, 'failed') <> 'completed' or " + 
							"root_entity_status <> 'completed') " +
							sqlSelectOrder + " limit ?";
			
						failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, luParentId, entitiesArraySize);
						
						sqlFailedCount = sqlSelectCnt +
							"LU_NAME = ? and target_parent_id = ? and (ifNull(execution_status, 'failed') <> 'completed' or " + 
							"root_entity_status <> 'completed')";
					
						failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName, luParentId).firstValue();
			
					}
				}
			}
		
			String prevTargetID = "";
			//log.info("wsGetTDMTaskExecutionStats - Looping over copied entities");
			for (Db.Row copiedEnt : compEntitiesBuf) {
				Map <String, Object> mapInnerCopiedEnt = new HashMap <>();
		
				mapInnerCopiedEnt.put("luName", copiedEnt.cell(9));
		
				//Get instance ID from entity id
				Object[] splitId = fnSplitUID("" +  copiedEnt.cell(0));
				String instanceId = "" + splitId[0];
				mapInnerCopiedEnt.put("sourceId", instanceId);
				
				String targetID = "" + copiedEnt.cell(1);
				
				mapInnerCopiedEnt.put("targetId", targetID);
				mapInnerCopiedEnt.put("rootSourceId", copiedEnt.cell(2));
				mapInnerCopiedEnt.put("rootTargetId", copiedEnt.cell(3));
				mapInnerCopiedEnt.put("parentLuName", copiedEnt.cell(4));
				mapInnerCopiedEnt.put("parentSourceId", copiedEnt.cell(5));
				mapInnerCopiedEnt.put("parentTargetId", copiedEnt.cell(6));
				mapInnerCopiedEnt.put("copyEntityStatus", copiedEnt.cell(7));
				mapInnerCopiedEnt.put("copyHierarchyStatus", copiedEnt.cell(8));
				
				if (!prevTargetID.equals(targetID)) {
					prevTargetID = targetID;
				}
				copiedEntitiesList.add(mapInnerCopiedEnt);
			}
			if (compEntitiesBuf != null) {
				compEntitiesBuf.close();
			}
			mapInnerCopiedBuf.put("NoOfEntities",compEntitiesCnt);
			mapInnerCopiedBuf.put("entitiesList", copiedEntitiesList);
			
			prevTargetID = "";
			
			//log.info("wsGetTDMTaskExecutionStats - looping over failed entities");
			for (Db.Row failedEnt : failedEntitiesBuf) {
				Map <String, Object> mapInnerFailedEnt = new HashMap <>();
				
				mapInnerFailedEnt.put("luName", failedEnt.cell(9));
		
				//Get instance ID from entity id
				Object[] splitId = fnSplitUID("" +  failedEnt.cell(0));
				String instanceId = "" + splitId[0];
				mapInnerFailedEnt.put("sourceId", instanceId);
				
				String targetID = "" + failedEnt.cell(1);
				String copyEntityStatus = "" + failedEnt.cell(7);
				
				mapInnerFailedEnt.put("targetId", targetID);
				mapInnerFailedEnt.put("rootSourceId", failedEnt.cell(2));
				mapInnerFailedEnt.put("rootTargetId", failedEnt.cell(3));
				mapInnerFailedEnt.put("parentLuName", failedEnt.cell(4));
				mapInnerFailedEnt.put("parentSourceId", failedEnt.cell(5));
				mapInnerFailedEnt.put("parentTargetId", failedEnt.cell(6));
				mapInnerFailedEnt.put("copyEntityStatus", copyEntityStatus);
				mapInnerFailedEnt.put("copyHierarchyStatus", failedEnt.cell(8));
				//log.info ("Failed - luName: " + failedEnt.cell(9) + ", rootSourceId: " + failedEnt.cell(2));
				// TDM 6.1.1 - 20-may-20, add the error msg that casued the failure
				String errorMsgSql = "select error_message from task_exe_error_detailed where " +
					"task_execution_id = ? and lu_name = ? and target_entity_id = ?  ORDER BY ERROR_CATEGORY LIMIT 5";
				Db.Rows errorMsgs = fabric().fetch(errorMsgSql, taskExecutionId, failedEnt.cell(9), targetID);
				List<String> entityErrMsgs  = new ArrayList<>();
				for (Db.Row errorMsg : errorMsgs) {
					entityErrMsgs.add("" + errorMsg.cell(0));
				}
				mapInnerFailedEnt.put("errorMsg", entityErrMsgs);
				if (!prevTargetID.equals(targetID)) {
					prevTargetID = targetID;
				}
				
				if ("true".equals(displayErrorPath)) {
					LinkedList <Object> failedErrPathList =  new LinkedList<>();
					//Get the failures error path
					if (!("Failed".equals(copyEntityStatus))) {
					
						String sqlFirst = "select LU_NAME, TARGET_ENTITY_ID, EXECUTION_STATUS, PARENT_LU_NAME, TARGET_PARENT_ID, " +
							"1 as row_number FROM TASK_EXECUTION_LINK_ENTITIES where PARENT_LU_NAME = ? and TARGET_PARENT_ID = ? ";
					
						String sqlSecond = "select a.LU_NAME, a.TARGET_ENTITY_ID, a.EXECUTION_STATUS, a.PARENT_LU_NAME, a.TARGET_PARENT_ID," +
							"row_number+1 FROM TASK_EXECUTION_LINK_ENTITIES a INNER JOIN relations b ON " +
							"b.lu_name = a.parent_lu_name and b.target_entity_id = a.TARGET_PARENT_ID ";
					
						String sqlRecursiveGetChildren = "WITH RECURSIVE relations AS (" + sqlFirst + " UNION " + sqlSecond + 
							") select LU_NAME, TARGET_ENTITY_ID, EXECUTION_STATUS, PARENT_LU_NAME, TARGET_PARENT_ID, row_number " +
							" from relations order by row_number DESC";
		
						//log.info("wsGetTDMTaskExecutionStats - Calling RECURSIVE sql for parent lu: " + failedEnt.cell(9) + ", and target ID: " + targetID);
						Db.Rows childrenBuf = fabric().fetch(sqlRecursiveGetChildren, failedEnt.cell(9), targetID);
					
						Boolean errorLevelFound = false;
						int rowNum = 0;
						String lookupParentLuName = "";
						String lookupParentID = "";
		
						for (Db.Row childRec : childrenBuf) {
							Map <String, Object> mapInnerErrorPath = new HashMap <>();
							String childluName = "" + childRec.cell(0);
							String childEnityID = "" + childRec.cell(1);
							String childStatus = "" + childRec.cell(2);
							String childParenLuName = "" + childRec.cell(3);
							String childParentID = "" + childRec.cell(4);
							String entityStatus = "";
						
							int currRowNum = Integer.parseInt("" + childRec.cell(5));
							//log.info("wsGetTDMTaskExecutionStats - Failed - luName: " + childluName + ", childTargetId: " + childEnityID +
							//	", parentLuName: " + childParenLuName + ", childParentID: " + childParentID + ", childStatus: " + childStatus);
						
							if(!errorLevelFound && "completed".equals(childStatus)) {
								//The input is coming from bottom up, therefore looking for the lowest LU that failed, and until it is found continue to the next one
								continue;
							}
							
							//This point will be reached for the first time when the lowest erred lu is found
							if (!errorLevelFound) {
								//log.info("wsGetTDMTaskExecutionStats - Failed Child Found");
								//log.info("wsGetTDMTaskExecutionStats - setting error level to true");
								errorLevelFound = true;
								if ("completed".equals(childStatus)) {
									entityStatus = "Copied";
								} else {
									entityStatus = "Failed";
								}
								mapInnerErrorPath.put("luName", childluName);
								mapInnerErrorPath.put("entityStatus", entityStatus);
								mapInnerErrorPath.put("parentLuName", childParenLuName);
								mapInnerErrorPath.put("luStatus", "Failed");
		
								failedErrPathList.addFirst(mapInnerErrorPath);
		
								//log.info("wsGetTDMTaskExecutionStats - Adding to Error Path: luName: " + childluName + ", ParentLuName: " + childParenLuName);
								lookupParentLuName = "" + childParenLuName;
								lookupParentID = "" + childParentID;
								
								//log.info("wsGetTDMTaskExecutionStats - Looking for Parent: ID: " + lookupParentID + ", Parent LU Name: " + lookupParentLuName);
								continue;
							}
						
							// This point will be reached only if the first failed record was found
							// From this point we are looking for the ancenstor LUs of the failed record
							//log.info("wsGetTDMTaskExecutionStats - Comparing - lookupParentLuName: " + lookupParentLuName + " with childluName: " + childluName +
							//	" and lookupParentID: " + lookupParentID + " with childEnityID: " + childEnityID);
							if (lookupParentLuName.equals(childluName) && lookupParentID.equals(childEnityID)) {
								//log.info("wsGetTDMTaskExecutionStats - Parent Record Found - luName: " + childluName + ", TargetId: " + childEnityID);
								if ("completed".equals(childStatus)) {
									entityStatus = "Copied";
								} else {
									entityStatus = "Failed";
								}
								mapInnerErrorPath.put("luName", childluName);
								mapInnerErrorPath.put("entityStatus", entityStatus);
								mapInnerErrorPath.put("parentLuName", childParenLuName);
								mapInnerErrorPath.put("luStatus", "Failed");
								
								failedErrPathList.addFirst(mapInnerErrorPath);
		
								lookupParentLuName = "" + childParenLuName;
								lookupParentID = "" + childParentID;
								continue;
							}
						}
						if (childrenBuf != null) {
							childrenBuf.close();
						}
					}
					Map <String, Object> mapInputLu = new HashMap <>();
					mapInputLu.put("luName", failedEnt.cell(9));
					mapInputLu.put("entityStatus", copyEntityStatus);
					mapInputLu.put("parentLuName", failedEnt.cell(4));
					mapInputLu.put("luStatus", "Failed");
					
		
					failedErrPathList.addFirst(mapInputLu);
					//log.info("Adding luName: " + failedEnt.cell(9) + ", entityStatus: " + copyEntityStatus + " to erro path");
					
					mapInnerFailedEnt.put("Full Error Path", failedErrPathList);
				}
				
				failedEntitiesList.add(mapInnerFailedEnt);
			}
			if(failedEntitiesBuf != null) {
				failedEntitiesBuf.close();
			}
			
			//log.info("wsGetTDMTaskExecutionStats - finished failed entities");
			mapInnerFailedBuf.put("NoOfEntities", failedEntitiesCnt);
			mapInnerFailedBuf.put("entitiesList", failedEntitiesList);
			
			Map_Outer.put("Copied entities per execution", mapInnerCopiedBuf);
			Map_Outer.put("Failed entities per execution", mapInnerFailedBuf);
		}
		
		//
		//Get Reference Info
		
		String sqlCopiedRefTabBuf = "";
		String sqlFailedRefRabBuf = "";
		Db.Rows copiedRefTabBuf  = null;
		Db.Rows failedRefTabBuf = null;
		
		//log.info("wsGetTDMTaskExecutionStats - start handling reference");
		if (!(luEntityId == null || luEntityId.isEmpty()) && "REFERENCE".equals(luIdType) ) {
			sqlCopiedRefTabBuf = "select distinct entity_id from TDM.task_execution_entities t where t.ID_Type = 'REFERENCE' " +
				"and entity_id = ? and t.Execution_Status = 'completed' and Lu_Name = ?";
			copiedRefTabBuf = fabric().fetch(sqlCopiedRefTabBuf, luEntityId, luName);
			
			sqlFailedRefRabBuf = "select distinct entity_id from TDM.task_execution_entities t where t.ID_Type = 'REFERENCE' " +
				"and entity_id = ? and ifNull(t.Execution_Status, 'failed') <> 'completed' and Lu_Name = ?";
			failedRefTabBuf = fabric().fetch(sqlCopiedRefTabBuf, luEntityId, luName);
			//log.info("wsGetTDMTaskExecutionStats - got failed reference");
		} else {
			sqlCopiedRefTabBuf = "select distinct entity_id from TDM.task_execution_entities t where t.ID_Type = 'REFERENCE' " +
				"and t.Execution_Status = 'completed' and Lu_Name = ?";
			copiedRefTabBuf = fabric().fetch(sqlCopiedRefTabBuf, luName);
		
			sqlFailedRefRabBuf = "select distinct entity_id from TDM.task_execution_entities t where t.ID_Type = 'REFERENCE' " +
				"and ifNull(t.Execution_Status, 'failed') <> 'completed' and Lu_Name = ?";
			failedRefTabBuf = fabric().fetch(sqlFailedRefRabBuf, luName);
		}
			//log.info("wsGetTDMTaskExecutionStats - Got reference Data");
		int totalCount = 0;
		for (Db.Row copiedRefEnt : copiedRefTabBuf) {
			Map <String, Object> mapInnerCopiedRefEnt = new HashMap <>();
			
			totalCount++;
			
			mapInnerCopiedRefEnt.put("RerernceTableName", copiedRefEnt.cell(0));
			copiedRefEntitiesList.add(mapInnerCopiedRefEnt);
		}
		if (copiedRefTabBuf != null) {
			copiedRefTabBuf.close();
		}
		
		mapInnerCopiedRefBuf.put("NoOfEntities", totalCount);
		mapInnerCopiedRefBuf.put("entitiesList", copiedRefEntitiesList);
		
		totalCount = 0;
		for (Db.Row failedRefEnt : failedRefTabBuf) {
			Map <String, Object> mapInnerFailedRefEnt = new HashMap <>();
			
			totalCount++;
			String reTableName = "" + failedRefEnt.cell(0);
			mapInnerFailedRefEnt.put("RerernceTableName", reTableName);
			
			// TDM 6.1.1 - 20-may-20, add the error msg that casued the failure
			String errorMsgSql = "select error_message from task_exe_error_detailed where " +
				"task_execution_id = ? and lu_name = ? and target_entity_id = ? LIMIT 5";
			Db.Rows errorMsgs = fabric().fetch(errorMsgSql, taskExecutionId, luName, reTableName);
			List<String> entityErrMsgs  = new ArrayList<>();
			for (Db.Row errorMsg : errorMsgs) {
				entityErrMsgs.add("" + errorMsg.cell(0));
			}
			mapInnerFailedRefEnt.put("errorMsg", entityErrMsgs);
			
			failedRefEntitiesList.add(mapInnerFailedRefEnt);
		}
		if (failedRefTabBuf != null) {
			failedRefTabBuf.close();
		}
		mapInnerFailedRefBuf.put("NoOfEntities", totalCount);
		mapInnerFailedRefBuf.put("entitiesList", failedRefEntitiesList);
		
		Map_Outer.put("Copied Reference per execution", mapInnerCopiedRefBuf);
		Map_Outer.put("Failed Reference per execution", mapInnerFailedRefBuf);
		
		//Get the status of all the hierachies of the task, for each hierarchy mark if it has failures or not, 
		// and if it has failures, where they on the root level or not
		String rootsStatusSql = "select ROOT_LU_NAME, max(root_status) from ( " +
			"select distinct root_lu_name, case when root_entity_status = 'completed' and EXECUTION_STATUS = 'completed' then 1 " +
			"when root_entity_status = 'failed' and  EXECUTION_STATUS = 'completed' then 2 " +
			"else 3 end as  root_status from task_execution_link_entities where parent_lu_name = '') group by root_lu_name";
		
		Db.Rows rootsStatuses = fabric().fetch(rootsStatusSql);
		
		for (Db.Row rootStatus : rootsStatuses) {
				String rootName = "" + rootStatus.cell(0);
				int rootStatusInd =  (int) rootStatus.cell(1);
				String rootSts = "";
				switch (rootStatusInd) {
					case 1 :
						rootSts = "completed";
						break;
					case 2 :
						rootSts = "child failed";
						break;
					default: 
						rootSts = "root failed";
						break;
				}
					
				mapRootsStatus.put(rootName, rootSts);
		}
		
		Map_Outer.put("Roots Status", mapRootsStatus);
		if (rootsStatuses != null) {
			rootsStatuses.close();
		}
		
		return wrapWebServiceResults("SUCCESS", null, Map_Outer);
	}

	@webService(path = "", verb = {MethodType.GET, MethodType.POST}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsBatchStats(String i_batchId, String i_runMode) throws Exception {
		return wrapWebServiceResults("SUCCESS", null, fnBatchStats(i_batchId, i_runMode));
	}

	@webService(path = "", verb = {MethodType.GET, MethodType.POST}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsGetLuList() throws Exception {
		List luList = (List) getFabricResponse("list lut;");
		for(Object e : luList) {
			Object luName = ((Map) e).get("LU_NAME");
			if ("TDM".equals(luName)) {
				luList.remove(e);
				break;
			}
		}
		return wrapWebServiceResults("SUCCESS", null, luList);
	}

	@webService(path = "", verb = {MethodType.GET, MethodType.POST}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsGetDcList() throws Exception {
		return wrapWebServiceResults("SUCCESS", null, getFabricResponse("clusterstatus;"));
								   
	}

	@webService(path = "", verb = {MethodType.GET, MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsGetAllGlobals() throws Exception {
		Map<String, Object> result = new HashMap<>();
		
		try {
			Map<String, Map<String, Object>> globalsPerLu = new HashMap<>();
		
			((List) getFabricResponse("set")).forEach(var -> {
				String[] keyParts = ((String) ((Map) var).get("key")).split("\\.");
				if (keyParts.length == 3 && "Global".equals(keyParts[0])) {
					if (!"k2_ws".equals(keyParts[1]) && !"k2_ref".equals(keyParts[1]) && !"TDM".equals(keyParts[1])) {
						Map<String, Object> luGlobals = globalsPerLu.computeIfAbsent(keyParts[1], k -> new HashMap<>());
						if (!"TDM_DELETE_BEFORE_LOAD".equals(keyParts[2]) &&
								!"TDM_INSERT_TO_TARGET".equals(keyParts[2]) &&
								!"TDM_SYNC_SOURCE_DATA".equals(keyParts[2]) &&
								!"ROOT_TABLE_NAME".equals(keyParts[2]) &&
								!"ROOT_COLUMN_NAME".equals(keyParts[2]) &&
								!"TDM_REPLACE_SEQUENCES".equals(keyParts[2]) &&
								!"TDM_TASK_EXE_ID".equals(keyParts[2]) &&
								!"TDM_SOURCE_ENVIRONMENT_NAME".equals(keyParts[2]) &&
								!"TDM_TAR_ENV_NAME".equals(keyParts[2]) &&
								!"TDM_TASK_ID".equals(keyParts[2]) &&
								!"TDM_VERSION_DATETIME".equals(keyParts[2]) &&
								!"TDM_VERSION_NAME".equals(keyParts[2]) &&
								!"ORACLE8_DB_TYPE".equals(keyParts[2]) &&
								!"COMBO_MAX_COUNT".equals(keyParts[2])
							) {
									luGlobals.put(keyParts[2], ((Map<String, Object>) var).get("value"));
						}
					}
				}
			});
			return wrapWebServiceResults("SUCCESS", null, globalsPerLu);
		} catch (Exception e) {
			return wrapWebServiceResults("FAIL", e.getMessage(), null);
		}
	}
	@webService(path = "", verb = {MethodType.GET, MethodType.POST}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsGetNumberOfMatchingEntities(String whereStmt, String sourceEnvName, Long beID) throws Exception {
		String sourceEnv = !Util.isEmpty(sourceEnvName) ? sourceEnvName : "_dev";
		
		String getEntitiesSql = generateListOfMatchingEntitiesQuery(beID, whereStmt, sourceEnv);
		
		Db tdmDB = db("TDM");
		Db.Rows rows = tdmDB.fetch("SELECT COUNT(entity_id) FROM (" + getEntitiesSql + " ) AS final_count");
		return  wrapWebServiceResults("SUCCESS", null, rows.firstValue());
	}

	@webService(path = "", verb = {MethodType.GET, MethodType.POST}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsGetListOfParamsForBE(String beID, String sourceEnvName) throws Exception {
		final String env = Util.isEmpty(sourceEnvName) ? "_dev" : sourceEnvName;
		
		Db tdmDB = db("TDM");
		Db.Rows luRes = tdmDB.fetch(LU_SQL, beID);
		
		Map<String, Iterable<Db.Row>> metaDataMap = new HashMap<>();
		for(Db.Row luRow : luRes){
			ResultSet resultSet = luRow.resultSet();
			String logicalUnitName = resultSet.getString("logicalunitname");
			Db.Rows luParamsRes = tdmDB.fetch("SELECT distinct column_name  FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ?", logicalUnitName.toLowerCase() + "_params");
			metaDataMap.put(logicalUnitName, luParamsRes.getResults());
		}
		Map<String, Map<String, Object>> beParametersColumnTypes = new HashMap<>();

		metaDataMap.forEach((luName, colNames) -> {
			StreamSupport.stream(colNames.spliterator(), false).map(col -> Util.rte(() -> col.resultSet().getString("column_name"))).filter(col -> !col.equals("be_id")).filter(col -> !col.equals("entity_id")).filter(col -> !col.equals("source_environment")).forEach(colName -> {
				String[] columnNameArr = colName.split("\\.");
				String logicalUnitName = columnNameArr[0].toUpperCase();
				String beTableName = logicalUnitName + "_PARAMS";
				String colNameUpper = colName.toUpperCase();
		
				String columnDistinctValueSQL = "SELECT COUNT(DISTINCT \"" + colName + "\") AS \"" + colName + "\" FROM (" +
						"SELECT UNNEST(\"" + colName + "\"::text[]) AS \"" + colName + "\" FROM " + beTableName + " WHERE source_environment='" + env + "') tempTable";
		
				long columnDistinctValue = Util.rte(() -> (Long) tdmDB.fetch(columnDistinctValueSQL).firstValue());

				// Initiate the parameter that will hold the distinct list of values for the column
				List<String> columnDistinctValues = new ArrayList<>();
				if (columnDistinctValue > 0 && columnDistinctValue <= Integer.parseInt(COMBO_MAX_COUNT)) {
					// Prepare the SQL Statement to retrieve the distinct list of values for that column
					String columnDistinctListSQL = "SELECT DISTINCT UNNEST(\"" + colName + "\"::text[]) AS \"" + colName + "\" FROM " + beTableName + " WHERE source_environment='" + env + "'";
		
					Iterator<Db.Row> iter = Util.rte(() -> tdmDB.fetch(columnDistinctListSQL).getResults().iterator());
					while (iter.hasNext()) {
						Util.rte(() -> columnDistinctValues.add(iter.next().resultSet().getString(colName)));
					}
					columnDistinctValues.sort(String::compareTo);
					beParametersColumnTypes.put(colNameUpper, Util.map(BE_ID, beID, LU_NAME, luName, PARAM_NAME, colNameUpper, PARAM_TYPE, PARAM_TYPES.COMBO.getName(), VALID_VALUES, columnDistinctValues, MIN_VALUE, "\\N", MAX_VALUE, "\\N", LU_PARAMS_TABLE_NAME, luName.toLowerCase() + "_params"
					));
		
				} else {
					int colNum = getColNumber(env, tdmDB, colName, beTableName);

					if (colNum == 1) {
						// In case it is, prepare the query to bring the column's min and max values
						String columnMinMaxSQL = getColumnMinMaxSQL(env, colName, beTableName);

						Db.Row columnMinMaxRow = Util.rte(() -> tdmDB.fetch(columnMinMaxSQL).firstRow());
						beParametersColumnTypes.put(colNameUpper, Util.map(BE_ID, beID, LU_NAME, luName, PARAM_NAME, colNameUpper, PARAM_TYPE, PARAM_TYPES.NUMBER.getName(), VALID_VALUES, "\\N", MIN_VALUE, Util.rte(() -> columnMinMaxRow.cell(0)), MAX_VALUE, Util.rte(() -> columnMinMaxRow.cell(1)), LU_PARAMS_TABLE_NAME, luName.toLowerCase() + "_params"
						));
					} else {
						// Mark it as text
						beParametersColumnTypes.put(colNameUpper, Util.map(BE_ID, beID, LU_NAME, luName, PARAM_NAME, colNameUpper, PARAM_TYPE, PARAM_TYPES.TEXT.getName(), VALID_VALUES, "\\N", MIN_VALUE, "\\N", MAX_VALUE, "\\N", LU_PARAMS_TABLE_NAME, luName.toLowerCase() + "_params"
						));
					}
				}
			});
		});
		return wrapWebServiceResults("SUCCESS", null, beParametersColumnTypes);
	}


	@webService(path = "", verb = {MethodType.GET, MethodType.POST}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsGetPostExecutionProcess() throws Exception {
		Map<String, Map<String, String>> translations = getTranslationsData("trnPostProcessList");
		return wrapWebServiceResults("SUCCESS", null, translations);
	}

	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsExecutionSummaryReport(String i_taskExecutionId, String i_luName) throws Exception {
		fabric().execute("get TDM.?", i_taskExecutionId);
		
		String taskType = "" + fabric().fetch("select task_type from tasks limit 1").firstValue();
		
		Object response;
		
		if ("LOAD".equals(taskType)) {
			if ("ALL".equalsIgnoreCase(i_luName)) {
				//log.info("Creating report for Load Task");
				response = graphit("LoadSummaryReport.graphit", i_taskExecutionId);
			} else {
				response = graphit("LoadSummaryReportPerLu.graphit", i_taskExecutionId, i_luName);
			}
			
		} else {
			//log.info("Creating report for Extract Task");
			response = graphit("ExtractSummaryReport.graphit", i_taskExecutionId);
		}
			
		return wrapWebServiceResults("SUCCESS", null, response);
	}

	@NotNull
	private static String getColumnMinMaxSQL(String env, String colName, String beTableName) {
		return "SELECT MIN(CAST(\"" + colName + "\" AS DOUBLE PRECISION)) AS \"MIN_" + colName + "\", MAX(CAST(\"" + colName + "\" AS DOUBLE PRECISION)) AS \"MAX_" + colName + "\" FROM (SELECT DISTINCT UNNEST(\"" + colName + "\"::text[]) AS \"" + colName + "\" FROM " + beTableName + " WHERE source_environment='" + env + "') tempTable";
	}

	private static Integer getColNumber(String env, Db tdmDB, String colName, String beTableName) {
		return Util.rte(() -> {
			Object value = tdmDB.fetch("SELECT DISTINCT CASE WHEN CAST(\"" + colName + "\" AS TEXT) ~ '^[0-9.]*$' THEN 1 ELSE 0 END isNumber FROM (SELECT DISTINCT UNNEST(\"" + colName + "\"::text[]) AS \"" + colName + "\" FROM " + beTableName + " WHERE source_environment='" + env + "') tempTable ORDER BY  1 ASC LIMIT 1").firstValue();
			return value != null ? (int) value: 0;
		});
	}

}
