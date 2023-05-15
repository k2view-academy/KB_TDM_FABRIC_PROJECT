package com.k2view.cdbms.usercode.lu.TDM;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.ZonedDateTime;

import static com.cronutils.model.CronType.QUARTZ;
import static com.k2view.cdbms.shared.user.UserCode.db;
import static com.k2view.cdbms.usercode.common.SharedGlobals.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TdmSharedUtils.SharedLogic.fnStartTask;

public class TdmTaskScheduler {
    public static final Log log = Log.a(TdmTaskScheduler.class);

    public static final String TDM = "TDM";
    public static void fnTdmTaskScheduler() throws IOException, SQLException {
        log.info("----------------- Starting tdmTaskScheduler -------------------");

        String activeTasksQuery = " WITH task_max_execution_id "+
                "AS(SELECT task_id AS max_task_id,lu_id AS max_lu_id,max(task_execution_id) AS max_task_execution_id " +
                "FROM " + TDMDB_SCHEMA + ".TASK_EXECUTION_LIST GROUP BY task_id,lu_id) " +
                "SELECT  task_id, scheduler, be_id, environment_id, num_of_entities, scheduling_end_date, "+
                "source_env_name, task_type, version_ind, source_environment_id, task_created_by " +
                "FROM " + TDMDB_SCHEMA + ".TASKS WHERE UPPER(task_status) = 'ACTIVE' "+
                "AND UPPER(task_execution_status) = 'ACTIVE' AND UPPER(scheduler) != 'IMMEDIATE' AND task_id NOT IN " +
                "(SELECT task_id FROM " + TDMDB_SCHEMA + ".TASK_EXECUTION_LIST,task_max_execution_id " +
                "WHERE task_id = max_task_id AND task_execution_id = max_task_execution_id AND lu_id = max_lu_id AND UPPER(execution_status) " +
                "IN ('RUNNING','EXECUTING','STARTED','PENDING','PAUSED','STARTEXECUTIONREQUESTED') OR " +
                "(end_execution_time >= (current_timestamp at time zone 'utc' + '-1 minute')));";

        db(TDM).fetch(activeTasksQuery).forEach(row -> {
            ResultSet resultSet = row.resultSet();
            Long taskID = Util.rte(() -> resultSet.getLong("task_id"));
            String cronExpression = Util.rte(() -> resultSet.getString("scheduler"));
            Timestamp schedulingEndDate = Util.rte(() -> resultSet.getTimestamp("scheduling_end_date"));
            Timestamp localTime = (Timestamp) Util.rte(() -> db(TDM).fetch("SELECT localtimestamp").firstValue());

            if(schedulingEndDate != null && localTime.compareTo(schedulingEndDate) > 0){
                log.info(" ----------------- updating task to immediate ----------------- ");

                Util.rte(() ->db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".TASKS SET scheduler = ?, scheduling_end_date = ?, task_last_updated_by = ? WHERE  task_id = ?", "immediate", null, "TDM scheduler", taskID));
                return;
            }

            ExecutionTime executionTime = ExecutionTime.forCron(new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ)).parse(cronExpression));
            ZonedDateTime now = ZonedDateTime.now();
            Duration timeToNextExecution = executionTime.timeToNextExecution(now).get();
            //log.info("time to next execution " + timeToNextExecution.getSeconds() + " to minutes " + timeToNextExecution.toMinutes());
            // Check if the task is due to run now
            if(executionTime.isMatch(now) || (timeToNextExecution.toMinutes() == 0 && timeToNextExecution.getSeconds() <= 10)){
                log.info(" ----------------- calling wsStartTask ----------------- ");
                try {
                    fnStartTask(taskID,true,null,null,null,null,null,null,null,null,null,null);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            }

        });
    }
}
