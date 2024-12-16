angular.module("TDM-FE").factory("ExcelService",(function(){var generalInfoStructure=[{name:"Task Name",field:"task_name",task_type:["all"]},{name:"Task ID",field:"task_id",task_type:["all"]},{name:"Task Execution ID",field:"task_execution_id",task_type:["all"]},{name:"Created by",field:"created_by",task_type:["all"]},{name:"Executed By",field:"executed_by",task_type:["all"]},{name:"Execution Time (UTC)",field:"start_execution",task_type:["]all"]},{name:"Execution Status",field:"execution_status",task_type:["all"]},{name:"Execution Mode",field:"execution_mode",task_type:["all"]},{name:"Source Environment",field:"source_env",task_type:["all"]},{name:"Source Environment Systems",field:"source_env_products",task_type:["all"]},{name:"Target Environment",field:"target_env",task_type:["load","reserve"]},{name:"Target Environment Systems",field:"target_env_products",task_type:["load","reserve"]},{name:"Business Entity Name",field:"be_name",task_type:["all"]},{name:"",field:"",task_type:["all"]},{name:"Task Properties:",field:"",task_type:["all"]},{name:"Task Type:",field:"task_type",task_type:["all"]},{name:"Fabric Execution Id:",field:"fabric_execution_id",task_type:["all"]},{name:"Selection Method:",field:"selection_method",task_type:["load","reserve"]},{name:"Task Sync Mode:",field:"task_sync_mode",task_type:["load","reserve"]},{name:"Env Sync Mode:",field:"env_sync_mode",task_type:["load"]},{name:"Operation Mode:",field:"operation_mode",task_type:["all"]},{name:"Replace Sequences:",field:"replace_sequences",task_type:["load"]},{name:"Task Versioning:",field:"version_ind",task_type:["all"]},{name:"Selected data generation execution id:",field:"selected_subset_task_exe_id",task_type:["load"]},{name:"Selected data snapshot execution id:",field:"selected_version_task_exe_id",task_type:["load"]},{name:"Scheduling Parameters:",field:"version_ind",task_type:["load","reserve"]},{name:"Schedule Expiration Date:",field:"schedule_expiration_date",task_type:["load","reserve"]},{name:"Data generation execution id:",field:"subset_task_execution_id",task_type:["extract"]},{name:"Data snapshot execution id:",field:"version_task_execution_id",task_type:["extract"]},{name:"Retention Period Type:",field:"retention_period_type",task_type:["extract"]},{name:"Retention Period Value:",field:"retention_period_value",task_type:["extract"]},{name:"Reservation Indicator:",field:"reserve_ind",task_type:["all"]},{name:"Reservation Period Type:",field:"reserve_retention_period_type",task_type:["all"]},{name:"Reservation Period Value:",field:"reserve_retention_period_value",task_type:["all"]}],generalInfo={},srcProducts=[],targetProducts=[],buildGeneralInfoTab=function(worksheet){var task_type=generalInfo.task_type;if(generalInfoStructure.forEach((function(field){var value=generalInfo[field.field]||"";("fabric_execution_id"!==field.field||generalInfo[field.field])&&(field.task_type.indexOf("all")>=0||field.task_type.indexOf(task_type.toLowerCase())>=0)&&("source_env_products"===field.field?(worksheet.addRow([field.name,""]),srcProducts.forEach(srcProd=>{worksheet.addRow(["",`${srcProd.product_name}, ${srcProd.source_product_version}`])})):"target_env_products"===field.field?(worksheet.addRow([field.name,""]),targetProducts.forEach(targetProd=>{worksheet.addRow(["",`${targetProd.product_name}, ${targetProd.target_product_version}`])})):worksheet.addRow([field.name,""+value]))})),generalInfo.override_parameters)try{const override_parameters=JSON.parse(generalInfo.override_parameters),overrideParametersKeys=Object.keys(override_parameters);if(overrideParametersKeys.length>0){addEmptyLine(worksheet),worksheet.addRow(["Override Parameters"]);let taskGlobals=null,reserveRetentionPeriod=null,retentionPeriod=null;overrideParametersKeys.forEach(key=>{"TASK_GLOBALS"===key?taskGlobals=override_parameters[key]:"RESERVE_RETENTION_PARAMS"===key?reserveRetentionPeriod=override_parameters[key]:"DATAFLUX_RETENTION_PARAMS"===key?retentionPeriod=override_parameters[key]:worksheet.addRow([key,""+override_parameters[key]])}),taskGlobals&&(addEmptyLine(worksheet),worksheet.addRow(["Task Globals"]),Object.keys(taskGlobals).forEach(key=>{worksheet.addRow([key,""+taskGlobals[key]])})),reserveRetentionPeriod&&(addEmptyLine(worksheet),worksheet.addRow(["RESERVE RETENTION PARAMS"]),Object.keys(reserveRetentionPeriod).forEach(key=>{worksheet.addRow([key,""+reserveRetentionPeriod[key]])})),retentionPeriod&&(addEmptyLine(worksheet),worksheet.addRow(["RETENTION PERIOD PARAMS"]),Object.keys(retentionPeriod).forEach(key=>{worksheet.addRow([key,""+retentionPeriod[key]])}))}}catch(err){console.log("unable to parse override parameters"),console.error(err)}},addEmptyLine=function(worksheet){worksheet.addRow([""])},getTableModel=function(data,worksheet,tableName){if(!data||!data.length)return{};var tableCols=[],tableRows=[],currentRow=worksheet.addRow([""]);Object.keys(data[0]).forEach((function(key){tableCols.push({name:key,key:key,width:"77"})}));for(var i=0;i<data.length;i++){var tableRow=[],row=data[i];Object.keys(row).forEach((function(key){tableRow.push(row[key])})),tableRows.push(tableRow)}return{name:tableName,ref:currentRow._cells[0]._address,columns:tableCols,rows:tableRows,style:{}}},addCustomizedRow=function(text,worksheet,customStyle){var row=worksheet.addRow([text]);Object.keys(customStyle).forEach((function(styleAttr){row[styleAttr]=customStyle[styleAttr]}))},addWorksheetTitle=function(titleName,worksheet){addCustomizedRow(titleName,worksheet,{font:{bold:!0,size:14}})},addWorksheetWarning=function(message,worksheet){addCustomizedRow(message,worksheet,{font:{bold:!0,color:{argb:"FF0000"}}})},buildListOfRootEntitiesTab=function(worksheet,tabData){var numOfCopiedEntities=tabData["Number of Copied Entities"][0].number_of_copied_root_entities,listOfCopiedEntities=tabData["List of Copied Entities"],numOfFailedEntities=tabData["Number of Failed Entities"][0].number_of_failed_root_entities,listOfFailedEntities=tabData["List of Failed Entities"];!function(numOfCopiedEntities,listOfCopiedEntities){addWorksheetTitle("Number of "+generalInfo.be_name+" Copied entities:"+numOfCopiedEntities,worksheet),addEmptyLine(worksheet),numOfCopiedEntities>0&&(worksheet.addTable(getTableModel(listOfCopiedEntities,worksheet,"copiedEntitiesTable")),addEmptyLine(worksheet))}(numOfCopiedEntities,listOfCopiedEntities),function(numOfFailedEntities,listOfFailedEntities){addWorksheetTitle("Number of "+generalInfo.be_name+" Failed entities:"+numOfFailedEntities,worksheet),addEmptyLine(worksheet),numOfFailedEntities>0&&(worksheet.addTable(getTableModel(listOfFailedEntities,worksheet,"failedEntitiesTable")),addEmptyLine(worksheet))}(numOfFailedEntities,listOfFailedEntities),addEmptyLine(worksheet)},buildListOfReferenceTables=function(worksheet,tabData){var numOfCopiedEntities=tabData["Number of Copied Tables"][0].count,listOfCopiedEntities=tabData["List of Copied Tables"],numOfFailedEntities=tabData["Number of Failed Tables"][0].count,listOfFailedEntities=tabData["List of Failed Tables"];!function(numOfCopiedEntities,listOfCopiedEntities){addWorksheetTitle("Number of "+generalInfo.be_name+" Copied Tables: "+numOfCopiedEntities,worksheet),addEmptyLine(worksheet),numOfCopiedEntities>0&&(worksheet.addTable(getTableModel(listOfCopiedEntities,worksheet,"copiedRefTable")),addEmptyLine(worksheet))}(numOfCopiedEntities,listOfCopiedEntities),function(numOfFailedEntities,listOfFailedEntities){addWorksheetTitle("Number of "+generalInfo.be_name+" Failed Tables: "+numOfFailedEntities,worksheet),addEmptyLine(worksheet),numOfFailedEntities>0&&(worksheet.addTable(getTableModel(listOfFailedEntities,worksheet,"failedRefTable")),addEmptyLine(worksheet))}(numOfFailedEntities,listOfFailedEntities)},buildTaskParametersTab=function(worksheet,tabData){tabData["Selection Method"]&&(tabData["Selection Method"].parameters?worksheet.addRow([tabData["Selection Method"].parameters.param_name,tabData["Selection Method"].parameters.param_value]):tabData["Selection Method"]["custom logic"]&&tabData["Selection Method"]["custom logic"].param_name?Object.keys(tabData["Selection Method"]["custom logic"].param_name).forEach(param_Index=>{worksheet.addRow([tabData["Selection Method"]["custom logic"].param_name[param_Index],tabData["Selection Method"]["custom logic"].param_value[param_Index]])}):tabData["Selection Method"]["Generate Params"]&&tabData["Selection Method"]["Generate Params"].forEach&&tabData["Selection Method"]["Generate Params"].forEach(param=>{worksheet.addRow([param.param_name,param.param_value])})),addEmptyLine(worksheet)},buildErrorSummaryTab=function(worksheet,tabData){tabData&&tabData.length?(worksheet.addTable(getTableModel(tabData,worksheet,"errorSummaryTable")),addEmptyLine(worksheet)):addWorksheetWarning("No Error Summary Report Data",worksheet)},buildExtractTab=function(tabName,tabData,worksheet){switch(tabName){case"General Info":buildGeneralInfoTab(worksheet);break;case"Task Execution Summary":!function(worksheet,tabData){var migrateTableData=[],luExtractSummary=[];let lusData=[],pepsData=[],prepepsData=[];tabData.forEach(item=>{item&&item.LUs&&(lusData=lusData.concat(item.LUs)),item&&item.LUs&&item.LUs["LU Extract Summary"]&&(luExtractSummary=luExtractSummary.concat(item.LUs["LU Extract Summary"])),item&&item.Processes&&item.Processes["Post Execution Processes"]&&(pepsData=pepsData.concat(item.Processes["Post Execution Processes"])),item&&item.Processes&&item.Processes["Pre Execution Processes"]&&(prepepsData=prepepsData.concat(item.Processes["Pre Execution Processes"]))});for(var i=0;i<lusData.length;i++){var luData=lusData[i],luName=luData["LU Extract Summary"][0].lu_name;luData["Extract Summary"]&&(migrateTableData=migrateTableData.concat(luData["Extract Summary"].map((function(data){return(data=Object.assign({"LU Name":luName},data)).added=data.added||0,data.updated=data.updated||0,data.unchanged=data.unchanged||0,data}))))}let pepsTableData=[];for(i=0;i<pepsData.length;i++){var pepName=(pepData=pepsData[i])["Post Execution Processes Summary"][0].process_name;pepData["Process Summary"]&&(pepsTableData=pepsTableData.concat(pepData["Process Summary"].map((function(data){return data=Object.assign({"Process Name":pepName},data)}))))}let prepepsTableData=[];for(i=0;i<prepepsData.length;i++){var pepData;pepName=(pepData=prepepsData[i])["Pre Execution Processes Summary"][0].process_name;pepData["Process Summary"]&&(prepepsTableData=prepepsTableData.concat(pepData["Process Summary"].map((function(data){return data=Object.assign({"Process Name":pepName},data)}))))}var addPostExecutionTable=function(pepData,worksheet,type){if(addWorksheetTitle(type+" Execution Processes Summary",worksheet),addEmptyLine(worksheet),!pepData||!pepData.length)return void addWorksheetWarning(`No ${type} Execution Processes Tables`,worksheet);const tableData=getTableModel(pepData,worksheet,type+"pepTableSummary");tableData&&tableData.columns&&tableData.columns.forEach(column=>{"end_time"===column.name?column.name="end_time (UTC)":"start_time"===column.name&&(column.name="start_time (UTC)")}),worksheet.addTable(tableData),console.log(tableData),addEmptyLine(worksheet)};if(luExtractSummary&&luExtractSummary.length>0){addWorksheetTitle("LU Summary",worksheet),addEmptyLine(worksheet);let tableData=getTableModel(luExtractSummary,worksheet,"loadExecutionSummary");tableData&&tableData.columns&&tableData.columns.forEach(column=>{"start_execution_time"===column.name?column.name="start_execution_time (UTC)":"end_execution_time"===column.name&&(column.name="end_execution_time (UTC)")}),worksheet.addTable(tableData),addEmptyLine(worksheet)}!function(migrateData,worksheet){if(addWorksheetTitle("Extract summary report for Task Execution ID: "+generalInfo.task_execution_id,worksheet),addEmptyLine(worksheet),migrateData&&migrateData.length){var table=getTableModel(migrateData,worksheet,"migrateTable");if(table.columns&&table.columns.length){for(var i=0;i<table.rows[0];i++)table.rows[0][i].unshift(luName);table.columns.forEach(column=>{"start time"===column.name?column.name="start time (UTC)":"end time"===column.name&&(column.name="end time (UTC)")})}worksheet.addTable(table),addEmptyLine(worksheet)}else addWorksheetWarning("No entities were migrated",worksheet)}(migrateTableData,worksheet),addEmptyLine(worksheet),addEmptyLine(worksheet),addPostExecutionTable(prepepsTableData,worksheet,"Pre"),addEmptyLine(worksheet),addPostExecutionTable(pepsTableData,worksheet,"Post")}(worksheet,tabData);break;case"List of Root Entities":buildListOfRootEntitiesTab(worksheet,tabData);break;case"List of Tables":buildListOfReferenceTables(worksheet,tabData);break;case"Error Summary":buildErrorSummaryTab(worksheet,tabData);break;case"Task Parameters":buildTaskParametersTab(worksheet,tabData)}},buildLoadTab=function(tabName,tabData,worksheet){switch(tabName){case"General Info":buildGeneralInfoTab(worksheet);break;case"Task Execution Summary":!function(worksheet,tabData){if(!tabData||!tabData.length)return void addEmptyLine(worksheet);let luTableData=[],postExecutionProcessData=[];tabData.forEach(item=>{item&&item.LUs&&item.LUs["LU Load Summary"]?luTableData=luTableData.concat(item.LUs["LU Load Summary"]):item&&item["Post Execution Processes"]&&item["Post Execution Processes"]["Post Execution Processes Summary"]&&(postExecutionProcessData=postExecutionProcessData.concat(item["Post Execution Processes"]["Post Execution Processes Summary"]))}),addWorksheetTitle("LU Summary",worksheet),addEmptyLine(worksheet);let tableData=getTableModel(luTableData,worksheet,"loadExecutionSummary");tableData&&tableData.columns&&tableData.columns.forEach(column=>{"start_execution_time"===column.name?column.name="start_execution_time (UTC)":"end_execution_time"===column.name&&(column.name="end_execution_time (UTC)")}),worksheet.addTable(tableData),addEmptyLine(worksheet),postExecutionProcessData&&postExecutionProcessData.length>0&&(addWorksheetTitle("Post Execution Processes Summary",worksheet),addEmptyLine(worksheet),tableData=getTableModel(postExecutionProcessData,worksheet,"postExecutionSummary"),tableData&&tableData.columns&&tableData.columns.forEach(column=>{"start_execution_time"===column.name?column.name="start_execution_time (UTC)":"end_execution_time"===column.name&&(column.name="end_execution_time (UTC)")}),worksheet.addTable(tableData),addEmptyLine(worksheet))}(worksheet,tabData);break;case"List of Root Entities":buildListOfRootEntitiesTab(worksheet,tabData);break;case"List of Tables":buildListOfReferenceTables(worksheet,tabData);break;case"Error Summary":buildErrorSummaryTab(worksheet,tabData);break;case"Statistics Report":!function(worksheet,tabData){tabData&&tabData.length?(worksheet.addTable(getTableModel(tabData,worksheet,"statisticsReportTable")),addEmptyLine(worksheet)):addWorksheetWarning("No Statistics Report Data",worksheet)}(worksheet,tabData);break;case"Replace Sequence Summary Report":!function(worksheet,tabData){tabData&&tabData.length?(worksheet.addTable(getTableModel(tabData,worksheet,"sequenceSummaryTable")),addEmptyLine(worksheet)):addWorksheetWarning("No Sequence Summary Report Data",worksheet)}(worksheet,tabData);break;case"Error Details":!function(worksheet,tabData){tabData&&tabData.length?(worksheet.addTable(getTableModel(tabData,worksheet,"errorDetailsTable")),addEmptyLine(worksheet)):addWorksheetWarning("No Error Details Report Data",worksheet)}(worksheet,tabData);break;case"Task Parameters":buildTaskParametersTab(worksheet,tabData)}};var buildExcel=function(workbook,data){var taskType=generalInfo.task_type.toLowerCase();Object.keys(data).forEach((function(tabName){var worksheet=workbook.addWorksheet(tabName),tabData=data[tabName];"extract"===taskType||"generate"===taskType||"training"===taskType?buildExtractTab(tabName,tabData||{},worksheet):buildLoadTab(tabName,tabData||{},worksheet),(worksheet=>{if(!worksheet.columns)return;const tableNames=Object.keys(worksheet.tables);if(tableNames.length>0){const worksheetColumn=worksheet.columns;tableNames.forEach(key=>{const table=worksheet.tables[key];table.model&&table.model.columns&&table.model.columns.length>0&&table.model.columns.forEach((column,index)=>{worksheetColumn[index].width=column.width,worksheetColumn[index].width<10?worksheetColumn[index].width=10:worksheetColumn[index].width>150?worksheetColumn[index].width=150:worksheetColumn[index].width=worksheetColumn[index].width+20})})}for(let i=0;i<worksheet.columns.length;i+=1){let dataMax=0;const column=worksheet.columns[i],valuesLength=tableNames.length>0?10:column.values.length;for(let j=1;column.values[j]&&j<valuesLength;j+=1){const columnLength=column.values[j].length;columnLength>dataMax&&(dataMax=columnLength)}column.width<dataMax?column.width=dataMax+20:column.width=dataMax<10?20:dataMax+20}})(worksheet)}))};return{buildSummaryExcel:function(data){var workbook=new ExcelJS.Workbook;const srcEnv=data["Source Environment"]&&data["Source Environment"]["Source Environment Products"]||[],tarEnv=data["Target Environment"]&&data["Target Environment"]["Target Environment Products"]||[];var info;return delete data["Source Environment"],delete data["Target Environment"],info=data["General Info"][0],generalInfo=info,srcProducts=srcEnv,targetProducts=tarEnv,buildExcel(workbook,data),workbook}}}));