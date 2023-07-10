/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.TDM_Extract;


import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.out;
import com.k2view.fabric.common.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnGetIIdSeparatorsFromTDM;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.getRetention;
import static com.k2view.cdbms.usercode.lu.TDM.Globals.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.Logic.getCommandForAll;

@SuppressWarnings({"DefaultAnnotationParam"})
public class Logic extends UserCode {
		public static final String TDM = "TDM";
	
	@out(name = "result", type = Map.class, desc = "")
	public static Map<String,String> fnMExtractEntities(String luName, String dcName, String sourceEnvName, String taskName, String versionInd, String entitiesList, String retentionPeriodType, Float retentionPeriodValue, String taskExecutionId, String parentLuName, String versionDateTime, String syncMode, String selectionMethod, Long luId) throws Exception {
		if (!"ON".equalsIgnoreCase(syncMode)) {
			fabric().execute("SET SYNC " + syncMode);
		}
		
		//Set Fabric's active environment to sourceEnvName + Execute batch + get batch_id
		if (sourceEnvName != null && !sourceEnvName.isEmpty()) {
			ludb().execute("set environment='" + sourceEnvName + "'");
		}
		
		String batchCommand = "";
		String entityList = "";
		String migrationInfo = "";
		Long unixTime = System.currentTimeMillis();
		Long unixTime_plus_retention;
		String versionName = "";
		
		//setGlobals(globals);
		String iidSeparator = "" + db(TDM).fetch("Select param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'iid_separator'").firstValue();
		String separator = "";
		if (!Util.isEmpty(iidSeparator) && !"null".equals(iidSeparator)) {
			separator = iidSeparator;
		} else {
			separator = "_";
		}
		// TALI- set version_exp_date to null. We cannot set an empty string for timestamp type of PG
		String version_exp_date = null;
		Db.Rows rs_mig_id = null;
		
		// Tali- return the results in a map
		Map<String, String> migInfo = new LinkedHashMap<>();
		
		// Tali- init timestamp by null
		String timeStamp = null;
		
		// TDM 5.1- support TTL also for regular extract tasks (versioning is false)
		// Check if retention parameters are populated instead of checking the versioning parameter
		
		//Calculate retention date + set TTL
		if (retentionPeriodType != null && !retentionPeriodType.isEmpty() && retentionPeriodValue != null && retentionPeriodValue >= 0) {
			// Tali- set the datetime only when versionInd is true (backup tasks)
			// TDM 6.0, in case of versionInd = true, the version_datetime will be received as input
			Integer retention_in_seconds = getRetention(retentionPeriodType, retentionPeriodValue);
			if (versionDateTime != null && !versionDateTime.isEmpty()) {
				timeStamp = versionDateTime;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				Date timeStampDate = sdf.parse(timeStamp);
				long millis = timeStampDate.getTime();
				unixTime_plus_retention = (millis / 1000L + retention_in_seconds) * 1000;
				version_exp_date = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTime_plus_retention);
			} else {
				timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTime);
				unixTime_plus_retention = (unixTime / 1000L + retention_in_seconds) * 1000;
				version_exp_date = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTime_plus_retention);
			}
			//Set TTL
		          if(-1 != retention_in_seconds) {
				ludb().execute("SET INSTANCE_TTL = " + retention_in_seconds);
			}
		}
		// end of if input retention parmeters are populated
		// TDM 7.3 - Set version globals in case of VERSION_IND is true
		if (versionInd.equals("true")) {
			versionName = taskName;
			//versionDateTime = timeStamp;
			
			fabric().execute("set TDM_VERSION_NAME = '" + versionName + "'");
			fabric().execute("set TDM_VERSION_DATETIME = " + versionDateTime);
		}
		
		//Execute Migrate
		
		// TDM 5.1- add a support of configurable separator for IID
		
		Object[] iidSeparators = fnGetIIdSeparatorsFromTDM();
		String openSeparator = iidSeparators[0].toString();
		String closeSeparator = iidSeparators[1].toString();
		
		if (entitiesList != null && !entitiesList.isEmpty() && !entitiesList.equals("")) { //Extract task is for listed entities
			entityList = entitiesList;
			if ("L".equals(selectionMethod)) {
				if (dcName != null && !dcName.isEmpty()) {
					batchCommand = "batch " + luName + ".(?) FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH AFFINITY='" + dcName + "' ASYNC=true";
				} else { // input DC is empty or null
					batchCommand = "batch " + luName + ".(?) FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH ASYNC=true";
				}
			} else { // in case of Custom Logic
				if (dcName != null && !dcName.isEmpty()) {
					batchCommand = "batch " + luName + " from DB_CASSANDRA using (?) FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH AFFINITY='" + dcName + "' ASYNC=true";
				} else { // input DC is empty or null
					batchCommand = "batch " + luName + " from DB_CASSANDRA using (?) FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH ASYNC=true";
				}
			}
		} else { //Extract task is for all the LU ( according to provided query in translation )
			// TDM 5.1 - add an additional input to the translation- source env
		
			// TDM 6.0 - if parentLuName is populated, then this is a child LU,
			// and the list of entities for migration will be created based on the Parent LU
			if (parentLuName != null && !parentLuName.equals("")) {
				//Get the timestamp from the parent record in task_execution_list
				String sqlGetVersionDateTime = "select to_char(version_datetime, 'yyyyMMddHH24miss') " +
						"from " + TDMDB_SCHEMA + ".task_execution_list tel, " + TDMDB_SCHEMA + ".tasks_logical_units tlu " +
						"where tel.task_execution_id = ? and tel.lu_id = tlu.lu_id and tlu.lu_name = ? limit 1";
				
				//log.info("fnMExtractEntities - taskExecutionId: " + taskExecutionId + ", parentLuName: " + parentLuName);
				timeStamp = "" + db(TDM).fetch(sqlGetVersionDateTime, taskExecutionId, parentLuName).firstValue();
		
				String entityIdSelectChildID = "rel.source_env||''" + separator + "''||rel.lu_type2_eid";
		
				if (!openSeparator.equals("")) {
					entityIdSelectChildID = "rel.source_env||''" + separator + "''||''" + openSeparator + "''||rel.lu_type2_eid";
				}
		
				if (!closeSeparator.equals("")) {
					entityIdSelectChildID += "||''" + closeSeparator + "''";
				}
				//log.info("fnMExtractEntities - versionInd: " + versionInd);
				if (versionInd.equals("true")) {
					
					//log.info("fnMExtractEntities - taskName: " + taskName + ", timeStamp: " + timeStamp);
					entityIdSelectChildID += "||''" + separator + taskName + separator + "''||''" + timeStamp + "''";
				}
		
				entityIdSelectChildID += "||'#params#{\"root_entity_id\" : '|| t.root_entity_id ||'}'";
				//log.info("entityIdSelectChildID: " + entityIdSelectChildID);
				String selSql = "";
		
				if (versionInd.equals("true")) {
		
					selSql = "SELECT ''''||" + entityIdSelectChildID + "||'''' child_entity_id FROM " + TDMDB_SCHEMA + ".task_execution_entities t, " +
							TDMDB_SCHEMA + ".tdm_lu_type_relation_eid rel where t.task_execution_id = ''" + taskExecutionId +
							"'' and t.execution_status = ''completed'' and t.lu_name = ''" + parentLuName +
							"'' and t.lu_name = rel.lu_type_1 and rel.lu_type_2 = ''" + luName +
							"'' and rel.version_name = ''" + versionName +
							"'' and to_char(rel.version_datetime, ''yyyyMMddHH24miss'') = ''" + versionDateTime +
							"'' and t.source_env = rel.source_env and t.iid = rel.lu_type1_eid";
				} else {
					selSql = "SELECT ''''||" + entityIdSelectChildID + "||'''' child_entity_id FROM " + TDMDB_SCHEMA + ".task_execution_entities t, " +
							TDMDB_SCHEMA + ".tdm_lu_type_relation_eid rel where t.task_execution_id = ''" + taskExecutionId +
							"'' and t.execution_status = ''completed'' and t.lu_name = ''" + parentLuName +
							"'' and t.lu_name = rel.lu_type_1 and rel.lu_type_2 = ''" + luName +
							"'' and rel.version_name = '''' and t.source_env = rel.source_env and t.iid = rel.lu_type1_eid";
				}
				if (dcName != null && !dcName.isEmpty()) {
					batchCommand = "batch " + luName + " from TDM using (?) FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH AFFINITY='" + dcName + "' ASYNC=true";
					entityList = selSql;
				} else // input DC is empty or null
				{
					batchCommand = "batch " + luName + " from TDM using (?) FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH ASYNC=true";
					entityList = selSql;
				}
		
			} else {
		
				Map <String, String> batchStrings = getCommandForAll(luName, taskExecutionId, sourceEnvName, versionInd, 
														separator, openSeparator, closeSeparator, taskName, timeStamp, dcName, luId);
				batchCommand = batchStrings.get("batchCommand");
				entityList = batchStrings.get("usingClause");
			}
		}
		
		rs_mig_id = ludb().fetch(batchCommand, entityList);
		migrationInfo = "" + rs_mig_id.firstValue();
		
		rs_mig_id.close();
		// Tali- populate the migInfo map
		migInfo.put("fabric_execution_id", migrationInfo);
		
		// TDM 5.1- set the version_datetime and version_expiration_date only of the input version ind is true. This check is required since a retention period can be also set for extract without versioning tasks
		if (versionInd.equals("true")) {
			migInfo.put("version_datetime", timeStamp);
			migInfo.put("version_expiration_date", version_exp_date);
		} else {
			migInfo.put("version_datetime", null);
			migInfo.put("version_expiration_date", null);
		}
		
		return migInfo;
	}
    
}
