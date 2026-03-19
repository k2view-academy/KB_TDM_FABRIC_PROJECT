/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.ParentChildLink;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;


@SuppressWarnings({"DefaultAnnotationParam"})
public class Logic extends UserCode {

	public static final String TDM = "TDM";

	public static void fnEnrParentChildLink() throws Exception {
		// Tali- 3-Dec-18- fix the query- add a join with task_execution_entities to get only the relevant records
		//log.info("fnEnrParentChildLink - Start");
		// TDM 5.1- fix this function to support configurable separator for entity id
		//Object[] res = fnGetIIdSeparatorsFromTDM();
		//TDM 6.0 - Get version_name and version_datetime from TASK_EXECUTION_LIST LU table
		Db.Row taskInfo = ludb().fetch("SELECT task_execution_id FROM TASK_EXECUTION_LIST LIMIT 1").firstRow();

		if (!taskInfo.isEmpty()) {
			String taskExecId = "" + taskInfo.get("task_execution_id");

			// TDM 7.1- performance improvement- remove the to_char from the version_datetime in the WHERE statement
			String sql = "SELECT rel.source_env, rel.lu_type_1, rel.lu_type_2, rel.lu_type1_eid, rel.lu_type2_eid, rel.creation_date, rel.version_task_execution_id, " +
					"parent.target_entity_id as parent_id, child.target_entity_id as child_id " +
					"FROM " + TDMDB_SCHEMA + ".tdm_lu_type_relation_eid rel, " + TDMDB_SCHEMA + ".task_execution_entities parent, " +
					TDMDB_SCHEMA + ".task_execution_entities child " +
					"where parent.task_execution_id = ? and parent.lu_name = rel.lu_type_1 " +
					"and parent.source_env = rel.source_env and parent.iid = rel.lu_type1_eid " +
					"and parent.version_task_execution_id = rel.version_task_execution_id " +
					"and child.task_execution_id = parent.task_execution_id and child.clone_no = parent.clone_no and child.lu_name= rel.lu_type_2 " +
					"and child.source_env = rel.source_env and child.iid = rel.lu_type2_eid " +
					"and child.version_task_execution_id = parent.version_task_execution_id";


			Db.Rows rows = null;
			try {

				String insertSql = "insert into TDM.tdm_lu_type_relation_eid (source_env, lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid, creation_date, version_task_execution_id)" +
						"values(?,?,?,?,?,?,?)";

				//TDM 7.0 - table tdm_lu_type_rel_tar_eid for replace sequence/Cloning will be populated in this function
				Db.Row taskRow = ludb().fetch("SELECT TARGET_ENV, LOAD_ENTITY, DELETE_BEFORE_LOAD, TASK_TYPE, REPLACE_SEQUENCES, CLONE_IND FROM TASKS").firstRow();
				String targetEnv = "" + taskRow.get("TARGET_ENV");
        		int loadEntity = (int) taskRow.get("LOAD_ENTITY");
				String taskType = "" + taskRow.get("TASK_TYPE");
				String replaceSequences = "" + taskRow.get("REPLACE_SEQUENCES");
                String cloneInd = "" + taskRow.get("CLONE_IND");
				boolean loadRelTar = false;
				//log.info("fnEnrParentChildLink - cloneInd: " + cloneInd + ", loadEntity: " + loadEntity + ", taskType: " + taskType);
				String insertSqlTar = "insert into TDM.tdm_lu_type_rel_tar_eid (target_env, lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid, creation_date) " +
						"values(?,?,?,?,?,?)";
				if (loadEntity == 1 && "load".equalsIgnoreCase(taskType) &&
						("true".equalsIgnoreCase(cloneInd) || "true".equalsIgnoreCase(replaceSequences))) {
					loadRelTar = true;
				}
				//log.info("fnEnrParentChildLink - sql: " + sql);
				rows = db(TDM).fetch(sql, taskExecId);

				//log.info("fnEnrParentChildLink - Loading data to Relation tables");
				for (Db.Row row : rows) {
					//log.info("fnEnrParentChildLink - insertSql: " + insertSql);
					String sourceEnv = "" + row.get("source_env");
					String luType1 = "" + row.get("lu_type_1");
					String luType2 = "" + row.get("lu_type_2");
					String luTypeEid1 = "" + row.get("lu_type1_eid");
					String luTypeEid2 = "" + row.get("lu_type2_eid");
					String createDate = "" + row.get("creation_date");
					String version_task_execution_id = "" + row.get("version_task_execution_id");
					fabric().execute(insertSql, sourceEnv, luType1, luType2, luTypeEid1, luTypeEid2, createDate, version_task_execution_id);
					if (loadRelTar) {
						luTypeEid1 = "" + row.get("parent_id");
						luTypeEid2 = "" + row.get("child_id");

						//log.info("fnEnrParentChildLink - Loading record to tdm_lu_type_rel_tar_eid");

						fabric().execute(insertSqlTar, targetEnv, luType1, luType2, luTypeEid1, luTypeEid2, createDate);
					}
				}
			} finally {
				if (rows != null) {
					rows.close();
				}
			}

		}
	}


	@desc("This function populates table TASK_EXECUTION_LINK_ENTITIES, based on tables TASK_EXECUTION_ENTITIES and TDM_LU_TYPE_RELATION_EID (for mosttasks) or TDM_LU_TYPE_REL_TAR_EID (for load tasks with special requirements)")
	public static void fnEncTaskExecutionLinkEntities() throws Exception {
		/* The function has 3 steps:
			1. Get and insert the entities with parent from TDM_LU_TYPE_RELATION_EID/TDM_LU_TYPE_REL_TAR_EID (Root entities will not be added at this step)
				a. The details of the parent will be used as root also
				b. For direct children of root, the root info will be correct, for lower levels the root data will be incorrect
			2. For all entities with incorrect root data (the root lu name has a parent lu id in PRODUCT_LOGICAL_UNITS table)
				a. The root info will be replaced by the root data of the parent from the entries added in the first step
				b. The root info of children of parents with correct root info, will have now correct root info
				c. Step 2 will be performed again until there are not more entries with incorrect root info
			3. Add the root entities to the table from TASK_EXECUTION_ENTITIES where the lu name has no parent lu id in PRODUCT_LOGICAL_UNITS
		
		*/

		//Get the task type in order to know if it is a load or extract task
		String sql = "SELECT TASK_TYPE, REPLACE_SEQUENCES, CLONE_IND FROM TASKS";
		String taskType = "";
		String replaceSeq = "false";
		Db.Rows rootList = null;
        String cloneInd = "";

		Db.Row taskRow = ludb().fetch(sql).firstRow();
		if (!taskRow.isEmpty()) {
			taskType = "" + taskRow.get("TASK_TYPE");
			replaceSeq = "" + taskRow.get("REPLACE_SEQUENCES");
            cloneInd = "" + taskRow.get("CLONE_IND");

			String insertChildSql = "";
			//log.info("fnEncTaskExecutionLinkEntities - taskType: " + taskType + ", replaceSeq: " + replaceSeq + ", cloneInd: " + cloneInd);

			//if it is a load task, and it is one of the following cases, the data will be taken from tdm_lu_type_rel_tar_eid:
			// 1. Clone Data
			// 2. Replace Sequences
			// 3. Delete only task
			try {
				if (("load".equalsIgnoreCase(taskType) && ("true".equalsIgnoreCase(cloneInd) || "true".equalsIgnoreCase(replaceSeq))) ||
						"delete".equalsIgnoreCase(taskType)) {

					insertChildSql = "insert or replace into TASK_EXECUTION_LINK_ENTITIES (LU_NAME,PARENT_LU_NAME,TARGET_ENTITY_ID,ENTITY_ID, " +
							"PARENT_ENTITY_ID,BE_ROOT_ENTITY_ID,TARGET_ROOT_ENTITY_ID,EXECUTION_STATUS,TARGET_PARENT_ID,ROOT_LU_NAME," +
							"VERSION_TASK_EXECUTION_ID,IID)" +
							"select distinct t.lu_name, r.lu_type_1, t.target_entity_id, t.entity_id, t2.iid, t.root_entity_id, t2.target_entity_id," +
							"t.execution_status,r.lu_type1_eid,t.root_lu_name,t.version_task_execution_id,t.iid " +
							"from task_execution_entities t, tdm_lu_type_rel_tar_eid r, task_execution_entities t2, task_execution_list l " +
							"where t.lu_name = r.lu_type_2 and t.target_entity_id = r.lu_type2_eid and " +
							"t2.lu_name = r.lu_type_1 and t2.target_entity_id = r.lu_type1_eid and " +
							"t.lu_name = l.lu_name and l.lu_name = r.lu_type_2 and l.parent_lu_name = r.lu_type_1 and l.lu_id != 0";

				} else {// in case of extract task or load task not including the above cases
					insertChildSql = "insert or replace into TASK_EXECUTION_LINK_ENTITIES (LU_NAME,PARENT_LU_NAME,TARGET_ENTITY_ID,ENTITY_ID, " +
							"PARENT_ENTITY_ID,BE_ROOT_ENTITY_ID,TARGET_ROOT_ENTITY_ID,EXECUTION_STATUS,TARGET_PARENT_ID,ROOT_LU_NAME," +
							"VERSION_TASK_EXECUTION_ID,IID)" +
							"select distinct t.lu_name, r.lu_type_1, t.target_entity_id, t.entity_id,r.lu_type1_eid,t.root_entity_id,t.root_entity_id," +
							"t.execution_status,r.lu_type1_eid,t.root_lu_name,t.version_task_execution_id,t.iid " +
							"from task_execution_entities t, tdm_lu_type_relation_eid r, task_execution_list l " +
							"where t.lu_name = r.lu_type_2 and t.iid = r.lu_type2_eid and t.source_env = r.source_env and " +
							"t.version_task_execution_id = r.version_task_execution_id and " +
							"t.lu_name = l.lu_name and l.lu_name = r.lu_type_2 and l.parent_lu_name = r.lu_type_1 and l.lu_id != 0";
				}
				//Step 1 - Get all child entities (non-root) and load them into target table
				//log.info("fnEncTaskExecutionLinkEntities - Running Step 1 query: " + insertChildSql);
				//log.info("fnEncTaskExecutionLinkEntities - Before execute insertChildSql: " + insertChildSql);
				fabric().execute(insertChildSql);
				String getRootLus = "select lu_name from " + TDMDB_SCHEMA + ".task_execution_list l, " +
						TDMDB_SCHEMA + ".product_logical_units p where l.lu_id = p.lu_id and l.task_execution_id = ? " +
						"and parent_lu_id is null and l.lu_id != 0";
				rootList = db(TDM).fetch(getRootLus, ludb().fetch("SELECT IID('TDM')").firstValue());

				String rootListIn = "";
				String comma = "";
				for(Db.Row root : rootList) {
					rootListIn += comma + "'" + root.get("lu_name") + "'";
					comma = ", ";
				}
				String insertRootSql = "insert or replace into task_execution_link_entities (LU_NAME,TARGET_ENTITY_ID,ENTITY_ID,BE_ROOT_ENTITY_ID, " +
						"TARGET_ROOT_ENTITY_ID,EXECUTION_STATUS,ROOT_LU_NAME,VERSION_TASK_EXECUTION_ID,IID,PARENT_LU_NAME,PARENT_ENTITY_ID,TARGET_PARENT_ID) " +
						"select t.lu_name, t.target_entity_id, t.entity_id, t.iid,t.target_entity_id, t.execution_status, t.lu_name, " +
						"t.version_task_execution_id, t.iid, '', '','' " +
						"from task_execution_entities t " +
						"where t.id_type = 'ENTITY' " +
						"and lu_name in (" + rootListIn + ")";

				//Step 3 - Add root entities to target table
				//log.info("fnEncTaskExecutionLinkEntities - Running step 3 query : " + insertRootSql);
				//log.info("fnEncTaskExecutionLinkEntities - Before execute insertRootSql: " + insertRootSql);
				fabric().execute(insertRootSql);
				//log.info("fnEncTaskExecutionLinkEntities - after execute insertChildSql");
			} catch (Exception e) {
				log.error("fnEncTaskExecutionLinkEntities failed with exception: " + e.getMessage(), e);
				throw new Exception(e.getMessage());
			}
		}
	}
}