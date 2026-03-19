/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.TDM_Stats;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;



@SuppressWarnings({"DefaultAnnotationParam"})
public class Logic extends UserCode {

    public static final String TDM = "TDM";

	@desc("Add JMX statistics per loaded table of the task")
	public static void fnTDMJMXStats() throws Exception {

        fnTableStats();
        fnBEStats();
		fnBEAndStatusStats();
        fnTotalExecutions();
        fnTotalStatusExecutions();

	}

	private static void fnTableStats() throws Exception {
        String sql = "Select lu_name, table_name, flow_name, Sum(CAST (target_count AS INTEGER)) as target_count" +
                "  from " + TDMDB_SCHEMA + ".task_exe_stats_detailed" +
                " group by lu_name, table_name, flow_name";

        Db.Rows rows = db(TDM).fetch(sql);

        for (Db.Row row : rows) {

            String luName = "" + row.get("lu_name");
            String tableName = "" + row.get("table_name");
            String flowName = "" + row.get("flow_name");
            Long RecCount = Long.valueOf("" + row.get("target_count")).longValue();

            //UserCode.statsCount(luName + "@" + tableName, flowName, RecCount);
            UserCode.statsCount("TotalLoadedRecordsPerLoadFlow", luName + "@" + tableName + "@" + flowName, RecCount);
        }
		
		if (rows != null) {
			rows.close();
		}
		
    }

    private static void fnBEStats() throws Exception {
        String sql = "select be.be_name, count(1) as execution_count" +
                " from " + TDMDB_SCHEMA + ".task_execution_summary s, " + TDMDB_SCHEMA + ".business_entities be" +
                " where s.be_id = be.be_id" +
                " group by be.be_name";

        Db.Rows rows = db(TDM).fetch(sql);

        for (Db.Row row : rows) {

            String beName = "" + row.get("be_name");
            Long RecCount = Long.valueOf("" + row.get("execution_count")).longValue();

            UserCode.statsCount("TaskExecutionPerBE", beName, RecCount);
        }
	
		if (rows != null) {
			rows.close();
		}
    }
	
	    private static void fnBEAndStatusStats() throws Exception {
        String sql = "select be.be_name, execution_status, count(1) as execution_count" +
                " from " + TDMDB_SCHEMA + ".task_execution_summary s, " + TDMDB_SCHEMA + ".business_entities be" +
                " where s.be_id = be.be_id" +
                " group by be.be_name, execution_status" +
				" order by be.be_name, execution_status";

        Db.Rows rows = db(TDM).fetch(sql);

        for (Db.Row row : rows) {

            String beName = "" + row.get("be_name");
			String status = "" + row.get("execution_status");
            Long RecCount = Long.valueOf("" + row.get("execution_count")).longValue();

            UserCode.statsCount("TaskExecutionPerBEAndStatus", beName + "#" + status, RecCount);
        }
		
		if (rows != null) {
			rows.close();
		}
    }


    private static void fnTotalExecutions() throws Exception {
        String sql = "select count(1) from " + TDMDB_SCHEMA + ".task_execution_summary";

        Object cnt = db(TDM).fetch(sql).firstValue();
        Long RecCount = Long.valueOf("" + cnt).longValue();

        UserCode.statsCount("TotalTaskExecutions", "TotalNumberOfExecutions", RecCount);
    }

    private static void fnTotalStatusExecutions() throws Exception {
	    String sql = "select execution_status, count(1) as status_count" +
                " from " + TDMDB_SCHEMA + ".task_execution_summary" +
                " group by execution_status" +
                " order by execution_status";

       Db.Rows rows = db(TDM).fetch(sql);
       for (Db.Row row : rows) {
           String status = "" + row.get("execution_status");
           Long RecCount = Long.valueOf("" + row.get("status_count")).longValue();

           UserCode.statsCount("TotalTaskExecutionsPerStatus", status, RecCount);
       }
		
		if (rows != null) {
			rows.close();
		}
    }
}
