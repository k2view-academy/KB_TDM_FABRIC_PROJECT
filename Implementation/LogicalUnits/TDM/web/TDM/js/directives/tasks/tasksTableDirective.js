function tasksTableDirective($interval,TASK){return{restrict:"E",templateUrl:"views/tasks/tasksTable.html",scope:{content:"="},link:function(scope,element){var updateData=$interval((function(){scope.getRunningTasks()}),TASK.timeInterval);element.on("$destroy",(function(){$interval.cancel(updateData)}))},controller:function($scope,$compile,TDMService,$sessionStorage,DTColumnBuilder,DTOptionsBuilder,DTColumnDefBuilder,$q,BreadCrumbsService,AuthService,toastr,SweetAlert,$timeout,ExcelService){var tasksTableCtrl=this;tasksTableCtrl.loadingTable=!0,tasksTableCtrl.userRole=AuthService.getRole(),tasksTableCtrl.username=AuthService.getUsername(),tasksTableCtrl.prevRunningTasksId="",TDMService.getFabricRolesforUser(tasksTableCtrl.username).then(response=>{tasksTableCtrl.userFabricRoles=response.result||[]}),TDMService.getTasks().then((function(response){if("SUCCESS"!=response.errorCode)return;if(tasksTableCtrl.tasksData=_.sortBy(response.result,(function(value){return new Date(value.task_last_updated_date)})),$scope.content.taskForExecution){const index=tasksTableCtrl.tasksData.findIndex(it=>it.task_id===$scope.content.taskForExecution);index>=0&&tasksTableCtrl.executeTask(index)}tasksTableCtrl.tasksData.reverse(),tasksTableCtrl.tasksData=_.map(tasksTableCtrl.tasksData,task=>{task.selection_method2=task.selection_method;const found=_.find([{value:"C",label:"Custom Logic"},{value:"PR",label:"Parameters with Random Entity Selection"},{value:"P",label:"Parameters"},{value:"R",label:"Random selection"},{value:"GENERATE",label:"Generate Synthetic Entities"},{value:"CLONE",label:"Entity Clone"},{value:"CLONE_PARAM",label:"Parameters based entity clone"},{value:"L",label:"Entity List"},{value:"ALL",label:"ALL"},{value:"REF",label:"Reference Tables"}],{value:task.selection_method});return found&&(task.selection_method2=found.label,"ALL"===task.selection_method&&"EXTRACT"===task.task_type?task.selection_method2="Predefined Entity List":"ALL"===task.selection_method&&"LOAD"===task.task_type&&(task.selection_method2="Selected Version's Entities")),task}),tasksTableCtrl.dtInstance={},tasksTableCtrl.dtColumns=[],tasksTableCtrl.dtColumnDefs=[],tasksTableCtrl.headers=[{column:"task_id",name:"Task ID",clickAble:!0,visible:!0},{column:"task_title",name:"Task name",clickAble:!0,visible:!0},{column:"source_env_name",name:"Source",clickAble:!1,visible:!0},{column:"environment_name",name:"Target",clickAble:!1,visible:!0},{column:"be_name",name:"Business Entity",clickAble:!1,visible:!0},{column:"task_type2",name:"Action",clickAble:!1,visible:!0},{column:"operation_mode",name:"Operations",clickAble:!1,visible:!1},{column:"reserve_ind",name:"Reserved Entities",clickAble:!1,visible:!0},{column:"version_ind",name:"Versioning",clickAble:!1,visible:!0},{column:"data_type",name:"Data Type",clickAble:!1,visible:!0},{column:"selection_method",name:"Subset Method",clickAble:!1,visible:!0},{column:"num_of_entities",name:"No. Of Requested Entities",clickAble:!1,visible:!0},{column:"task_status",name:"Status",clickAble:!1,visible:!0},{column:"task_execution_status",name:"Task Execution Status",clickAble:!1,visible:!0},{column:"task_last_updated_by",name:"Updated By",clickAble:!1,visible:!0},{column:"task_last_updated_date",name:"Update Date",clickAble:!1,type:"date",visible:!0},{column:"scheduler",name:"Execution Timing",clickAble:!1,visible:!0},{column:"scheduling_end_date",name:"Scheduling End Date",clickAble:!1,visible:!0},{column:"retention_period_type",name:"Retention period type",clickAble:!1,visible:!0},{column:"retention_period_value",name:"Retention period value",clickAble:!1,visible:!0},{column:"refresh_reference_data",name:"Select Tables",clickAble:!1,visible:!1},{column:"sync_mode",name:"Override Sync Mode",clickAble:!1,visible:!1},{column:"replace_sequences",name:"Replace Sequence",clickAble:!1,visible:!1},{column:"processnames",name:" Pre & Post Execution Processes",clickAble:!1,visible:!1}],$sessionStorage.taskTableHideColumns?tasksTableCtrl.hideColumns=$sessionStorage.taskTableHideColumns:tasksTableCtrl.hideColumns=[14,15,16,17,18,19,20,21,22,23,24];for(var i=0;i<tasksTableCtrl.hideColumns.length;i++){var hideColumn=DTColumnDefBuilder.newColumnDef(tasksTableCtrl.hideColumns[i]).withOption("visible",!1);tasksTableCtrl.dtColumnDefs.push(hideColumn)}var clickAbleColumn=function(data,type,full,meta){return"<a ng-click=\"tasksTableCtrl.openTask('"+full.task_id+"')\">"+data+"</a>"};var taskTitleColumn=function(data,type,full,meta){return`<div class="taskTitle" style="display: flex;justify-content: space-between;position: relative" title="${data}">\n                        <a style="width: 86%;white-space: nowrap; overflow: hidden;text-overflow: ellipsis;" \n                            ng-click="tasksTableCtrl.openTask('${full.task_id}')">${data}</a>\n                        ${function(full){return full.task_description?`<div>\n                                <img src="icons/info-icon.svg" tooltip-class="task_description"\n                                    uib-tooltip="${full.task_description}" tooltip-append-to-body="true" tooltip-placement="right" />\n                            </div>`:""}(full)}\n                    </div>`},selectionMethodColumn=function(data,type,full,meta){switch(data){case"L":return"Entity List";case"P":return"Parameters";case"PR":return"Parameters with Random Entity Selection";case"R":return"Random selection";case"CLONE":return"Entity Clone";case"GENERATE":return"Generate Synthetic Entities";case"CLONE_PARAM":return"Parameters based entity clone";case"ALL":return"EXTRACT"===full.task_type?"Predefined Entity List":"LOAD"===full.task_type?"Selected Version's Entities":"All";case"REF":return"Reference Tables";case"C":return"Custom Logic";default:return"none"}},changeToLocalDate=function(data,type,full,meta){return moment(data).format("DD MMM YYYY, HH:mm")},changeToLocalDateToChar=function(data,type,full,meta){var ans=moment(data,"YYYYMMDDHHmmss").format("DD MMM YYYY, HH:mm");return"Invalid date"==ans?"":ans},checkMarkColumn=function(data){return data?'<span style="display: flex; align-items: center; justify-content: center;" class="fa fa-check"></span>':""};tasksTableCtrl.chooseHoldActivate=function(row){1==tasksTableCtrl.tasksData[row].onHold?tasksTableCtrl.activateTask(row):tasksTableCtrl.holdTask(row)};var defauleColumn=function(data){return data?`<span title="${data}">${data}</span>`:""};for(i=0;i<tasksTableCtrl.headers.length;i++)"task_title"===tasksTableCtrl.headers[i].column?tasksTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(tasksTableCtrl.headers[i].column).withTitle(tasksTableCtrl.headers[i].name).renderWith(taskTitleColumn)):1==tasksTableCtrl.headers[i].clickAble?tasksTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(tasksTableCtrl.headers[i].column).withTitle(tasksTableCtrl.headers[i].name).renderWith(clickAbleColumn)):"date"==tasksTableCtrl.headers[i].type?tasksTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(tasksTableCtrl.headers[i].column).withTitle(tasksTableCtrl.headers[i].name).renderWith(changeToLocalDate)):"to_char_date"==tasksTableCtrl.headers[i].type?tasksTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(tasksTableCtrl.headers[i].column).withTitle(tasksTableCtrl.headers[i].name).renderWith(changeToLocalDateToChar)):"selection_method"==tasksTableCtrl.headers[i].column?tasksTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(tasksTableCtrl.headers[i].column).withTitle(tasksTableCtrl.headers[i].name).renderWith(selectionMethodColumn)):"task_status"==tasksTableCtrl.headers[i].column?tasksTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(tasksTableCtrl.headers[i].column).withTitle(tasksTableCtrl.headers[i].name).withOption("select","Active")):"reserve_ind"===tasksTableCtrl.headers[i].column||"version_ind"===tasksTableCtrl.headers[i].column?tasksTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(tasksTableCtrl.headers[i].column).withTitle(tasksTableCtrl.headers[i].name).renderWith(checkMarkColumn)):tasksTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(tasksTableCtrl.headers[i].column).withTitle(tasksTableCtrl.headers[i].name).renderWith(defauleColumn));tasksTableCtrl.dtColumns.unshift(DTColumnBuilder.newColumn("taskActions").withTitle("").renderWith((function(data,type,full,meta){let arraysEqual=(arr1,arr2)=>{if(arr1.length!==arr2.length)return!1;for(var i=arr1.length;i--;)if(arr1[i]!==arr2[i])return!1;return!0};var canByRunByOtherTester=!1,createdTester=_.find(full.testers,{tester:full.task_created_by});if(createdTester){var currentTester=_.find(full.testers,{tester:tasksTableCtrl.username});currentTester&&arraysEqual(currentTester.role_id,createdTester.role_id)?canByRunByOtherTester=!0:tasksTableCtrl.userFabricRoles.forEach(role=>{var currentTester=_.find(full.testers,{tester_type:"GROUP",tester:role});currentTester&&arraysEqual(currentTester.role_id,createdTester.role_id)&&(canByRunByOtherTester=!0)})}else full.creatorRoles&&full.creatorRoles.forEach(createdUserRole=>{tasksTableCtrl.userFabricRoles.indexOf(createdUserRole)>=0&&(canByRunByOtherTester=!0)});let ownerByfabricRole=!1;if(!canByRunByOtherTester)for(let i=0;i<tasksTableCtrl.userFabricRoles.length;i++){const role=tasksTableCtrl.userFabricRoles[i];if(_.findIndex(full.owners,{owner:role,owner_type:"GROUP"})>=0){ownerByfabricRole=!0;break}}var taskActions="";return"Active"==full.task_status&&("admin"==tasksTableCtrl.userRole.type||_.findIndex(full.owners,{owner:tasksTableCtrl.username})>=0||ownerByfabricRole||tasksTableCtrl.username==full.task_created_by||canByRunByOtherTester)&&(tasksTableCtrl.tasksData[meta.row].disabled=!1,tasksTableCtrl.tasksData[meta.row].onHold="onHold"==full.task_execution_status,taskActions+=`\n                    <span style="margin-left: 3px; cursor: pointer;" title="Execute Task" >\n                        <img ng-show="(tasksTableCtrl.tasksData[${meta.row}].disabled == true \n                            || (tasksTableCtrl.tasksData[${meta.row}].onHold == true || \n                            tasksTableCtrl.tasksData[${meta.row}].executioncount !== 0))" \n                            src="icons/play-off.svg">\n                        </img>\n                        <img  ng-click="tasksTableCtrl.executeTask(${meta.row})" ng-show="!(tasksTableCtrl.tasksData[${meta.row}].disabled == true \n                        || (tasksTableCtrl.tasksData[${meta.row}].onHold == true || \n                        tasksTableCtrl.tasksData[${meta.row}].executioncount !== 0))"\n                            src="icons/play.svg">\n                        </img>\n                        \n                    </span>\n                `,taskActions+=`\n                    <span title="{{tasksTableCtrl.tasksData[${meta.row}].onHold == false ?'Hold Task':'Activate Task'}}"  \n                        style="margin-left: 3px;cursor: pointer;" >\n                        <img ng-show="tasksTableCtrl.tasksData[${meta.row}].onHold == true"\n                        ng-click="tasksTableCtrl.chooseHoldActivate(${meta.row})"\n                            src="icons/play-red.svg">\n                        </img>\n                        <img ng-show="tasksTableCtrl.tasksData[${meta.row}].onHold==false" ng-click="tasksTableCtrl.chooseHoldActivate(${meta.row})"\n                            src="icons/puse.svg">\n                        </img>\n                        \n                    </span>\n                `),taskActions+=` \n                <span\n                    title="Task Execution History" \n                    style="margin-left: 3px;\n                    cursor: pointer;\n                    " \n                    ng-click="tasksTableCtrl.taskExecutionSummary(${full.task_id})">\n                    <img src="icons/history.svg"> </img>\n                </span>`,"Active"==full.task_status&&(taskActions+=` \n                    <span\n                     title="Save As"  \n                     style="margin-left: 3px;\n                     cursor: pointer;\n                     " \n                    ng-click="tasksTableCtrl.saveAs(${full.task_id})"  \n                 >\n                    <img src="icons/floppy.svg"> </img>\n                </span>`),"Active"===full.task_status&&(taskActions+=`<span style="margin-left: 3px;\n                    cursor: pointer;\n                    " >\n                    <img id="task_delete_${meta.row}"\n                    style="cursor:pointer"\n                    uib-tooltip="Delete Task" \n                    tooltip-placement="right" \n                    tooltip-append-to-body="true"\n                    mwl-confirm="" \n                    message="Are you sure you want to delete the task?" \n                    confirm-text="Yes <i class='glyphicon glyphicon-ok'</i>" \n                    cancel-text="No <i class='glyphicon glyphicon-remove'></i>" \n                    placement="right" \n                    on-confirm="tasksTableCtrl.deleteTask(${full.task_id})" \n                    on-cancel="cancelClicked = true" \n                    confirm-button-type="danger" \n                    cancel-button-type="default" \n                    role-handler="" \n                    role="0"\n                    src="icons/delete-icon.svg"\n                    alt="delete"\n                        </img>\n                    </span>`),taskActions})));var getTableData=function(){var deferred=$q.defer();return angular.forEach(tasksTableCtrl.tasksData,(function(task){task.task_type2=task.task_type,"TRAINING"===task.task_type?task.operation_mode="AI Training":"AI_GENERATED"===task.task_type?task.operation_mode="Generate entities based on AI":-1===task.be_id?"EXTRACT"===task.task_type?task.operation_mode="Extract tables":task.operation_mode="Load tables":"LOAD"===task.task_type&&-1===task.source_environment_id?task.operation_mode="Generate and load entity":"GENERATE"===task.task_type?task.operation_mode="Generate entity":task.load_entity&&task.delete_before_load?task.operation_mode="Delete and load entity":task.load_entity&&!task.delete_before_load?task.operation_mode="Load entity":!task.load_entity&&task.delete_before_load?(task.operation_mode="Delete entity",task.task_type2="DELETE"):"RESERVE"===task.task_type?task.operation_mode="Reserve entity":"EXTRACT"===task.task_type?task.operation_mode="Extract entity":task.operation_mode="",0===task.refcount?task.data_type="Entities":"REF"===task.selection_method?task.data_type="Reference":task.data_type="Entities and Reference"})),deferred.resolve(tasksTableCtrl.tasksData),deferred.promise};if(tasksTableCtrl.dtOptions=DTOptionsBuilder.fromFnPromise((function(){return getTableData()})).withDOM('<"html5buttons"B>lTfgitp').withOption("createdRow",(function(row){$compile(angular.element(row).contents())($scope)})).withOption("pageLength",$sessionStorage.taskPageLength||10).withOption("length",(function(e,settings,len){console.log("xxx")})).withOption("scrollX",!1).withOption("aaSorting",[[13,"asc"]]).withButtons([{extend:"colvis",text:"Show/Hide columns",columns:[14,15,16,17,18,19,20,21,22,23,24],callback:function(columnIdx,visible){var index;1==visible?(index=tasksTableCtrl.hideColumns.indexOf(columnIdx))>=0&&tasksTableCtrl.hideColumns.splice(index,1):(index=tasksTableCtrl.hideColumns.indexOf(columnIdx))<0&&tasksTableCtrl.hideColumns.push(columnIdx);$sessionStorage.taskTableHideColumns=tasksTableCtrl.hideColumns}}]).withOption("caseInsensitive",!0).withOption("search",{caseInsensitive:!1}).withOption("fixedColumns",!1),tasksTableCtrl.tasksData&&tasksTableCtrl.tasksData.length>0){const columns=[{type:"text"},{type:"text"},{type:"select",values:_.map(_.unique(_.map(tasksTableCtrl.tasksData,"source_env_name")),(function(el){return{value:el,label:el}}))},{type:"select",values:_.map(_.unique(_.map(tasksTableCtrl.tasksData,"environment_name")),(function(el){return{value:el,label:el}}))},{type:"select",values:_.map(_.unique(_.map(tasksTableCtrl.tasksData,"be_name")),(function(el){return{value:el,label:el}}))},{type:"select",values:[{value:"LOAD",label:"LOAD"},{value:"EXTRACT",label:"EXTRACT"},{value:"RESERVE",label:"RESERVE"},{value:"DELETE",label:"DELETE"}]},{type:"select",values:[{value:"Load entity",label:"Load entity"},{value:"Generate and load entity",label:"Generate and load entity"},{value:"Generate entity",label:"Generate entity"},{value:"Delete and load entity",label:"Delete and load entity"},{value:"Delete entity",label:"Delete entity"},{value:"Reserve entity",label:"Reserve entity"},{value:"Extract entity",label:"Extract entity"},{value:"AI Training",label:"AI Training"},{value:"Generate entities based on AI",label:"Generate entities based on AI"},{value:"Extract tables",label:"Extract tables"},{value:"Load tables",label:"Load tables"}]},{type:"select",values:[{value:!0,label:"true"},{value:!1,label:"false"}]},{type:"select",values:_.map(_.unique(_.map(tasksTableCtrl.tasksData,"version_ind")),(function(el){return{value:el,label:el}}))},{type:"select",values:[{value:"Entities",label:"Entities"},{value:"Reference",label:"Reference"},{value:"Entities and Reference",label:"Entities and Reference"}]},{type:"select",values:[{value:"Predefined Entity List",label:"Predefined Entity List"},{value:"Selected Version's Entities",label:"Selected Version's Entities"},{value:"Reference Tables",label:"Reference Tables"},{value:"Custom Logic",label:"Custom Logic"},{value:"Entity Clone",label:"Entity Clone"},{value:"Entity List",label:"Entity List"},{value:"Parameters with Random Entity Selection",label:"Parameters with Random Entity Selection"},{value:"Parameters",label:"Parameters"},{value:"Random selection",label:"Random selection"}]},{type:"text"},{type:"select",defaultValue:"Active",values:[{value:"Inactive",label:"Inactive"},{value:"Active",label:"Active"}]},{type:"select",values:[{value:"Active",label:"Active"},{value:"onHold",label:"onHold"}]},{type:"select",values:_.map(_.unique(_.map(tasksTableCtrl.tasksData,"task_last_updated_by")),(function(el){return{value:el,label:el}}))},{type:"text"},{type:"text"},{type:"text"},{type:"select",values:_.map(_.unique(_.map(tasksTableCtrl.tasksData,"retention_period_type")),(function(el){return{value:el,label:el}}))},{type:"text"},{type:"select",values:[{value:"true",label:"true"},{value:"false",label:"false"}]},{type:"select",values:_.map(_.unique(_.map(tasksTableCtrl.tasksData,"sync_mode")),(function(el){return{value:el,label:el}}))},{type:"select",values:[{value:"true",label:"true"},{value:"false",label:"false"}]},{type:"text"}],lightColumnFilter={};columns.forEach((column,index)=>{let temp=angular.copy(column);temp.hidden=tasksTableCtrl.hideColumns.indexOf(index+1)>=0,lightColumnFilter[index+1]=temp}),tasksTableCtrl.dtOptions.withLightColumnFilter(lightColumnFilter)}tasksTableCtrl.dtInstanceCallback=function(dtInstance){dtInstance.DataTable.on("length.dt",(function(e,settings,len){$sessionStorage.taskPageLength=len})),angular.isFunction(tasksTableCtrl.dtInstance)?tasksTableCtrl.dtInstance(dtInstance):angular.isDefined(tasksTableCtrl.dtInstance)&&(tasksTableCtrl.dtInstance=dtInstance)},null!=tasksTableCtrl.dtInstance.changeData&&tasksTableCtrl.dtInstance.changeData(getTableData()),$timeout(()=>{tasksTableCtrl.loadingTable=!1})})),tasksTableCtrl.openTask=function(taskId,copy){if($scope.content.openTask){var taskData=_.find(tasksTableCtrl.tasksData,{task_id:parseInt(taskId)});if(taskData)return void $scope.content.openTask(taskData,copy,tasksTableCtrl.tasksData.filter(it=>"Active"===it.task_status))}},tasksTableCtrl.openNewTask=function(){$scope.content.openNewTask&&$scope.content.openNewTask(tasksTableCtrl.tasksData.filter(it=>"Active"===it.task_status))},tasksTableCtrl.taskExecutionHistory=function(taskId){if($scope.content.openTaskHistory){var taskData=_.find(tasksTableCtrl.tasksData,{task_id:parseInt(taskId)});if(taskData)return $scope.content.openTaskHistory(taskData),void BreadCrumbsService.push({title:taskData.task_title},"TASK_EXECUTION_HISTORY",(function(){$scope.content.openTaskHistory(taskData)}))}},tasksTableCtrl.taskExecutionSummary=function(taskId){if($scope.content.openTaskSummary){var taskData=_.find(tasksTableCtrl.tasksData,{task_id:parseInt(taskId)});if(taskData)return $scope.content.openTaskSummary(taskData),void BreadCrumbsService.push({title:taskData.task_title},"TASK_EXECUTION_SUMMARY",(function(){$scope.content.openTaskSummary(taskData)}))}},tasksTableCtrl.executeTask=function(index){let task=tasksTableCtrl.tasksData[index],taskId=task.task_id,testers=task.testers,roles=task.roles||[],owners=task.owners,task_title=task.task_title,ownerByfabricRole=(task.task_created_by,!1);for(let i=0;i<tasksTableCtrl.userFabricRoles.length;i++){const role=tasksTableCtrl.userFabricRoles[i];if(_.findIndex(owners,{owner:role,owner_type:"GROUP"})>=0){ownerByfabricRole=!0;break}}let forced=!!("admin"==tasksTableCtrl.userRole.type||_.findIndex(owners,{owner:tasksTableCtrl.username})>=0||ownerByfabricRole||(()=>{let canRun=!1;if(!testers||!roles)return canRun;let loggedUser=tasksTableCtrl.username;for(i of testers)if((i.tester===loggedUser||"ALL"===i.tester||"GROUP"===i.tester_type&&tasksTableCtrl.userFabricRoles.indexOf(i.tester)>=0)&&angular.isArray(i.role_id))for(id of i.role_id){let foundRole=_.find(roles[0],{role_id:parseInt(id)});foundRole&&foundRole.allowed_test_conn_failure&&(canRun=!0)}return canRun})());tasksTableCtrl.tasksData[index].disabled=!0;const getErrorMessage=response=>{let message="";return Array.isArray(response.result)?Object.keys(response.result[0]).forEach((key,index)=>{message=`${message}${response.result[0][key]}\n                        `}):"object"==typeof response.result?Object.keys(response.result).forEach((key,index)=>{message=`${message}${response.result[key]}\n                        `}):message=response.result||response.message,message};TDMService.executeTask(taskId).then((function(response){if("SUCCESS"==response.errorCode)tasksTableCtrl.tasksData[index].executioncount=1,toastr.success("Task # "+task_title," Successfully started"),tasksTableCtrl.dtInstance.reloadData((function(data){}),!0),tasksTableCtrl.taskExecutionSummary(taskId);else if(forced&&"FAILED"!==response.errorCode){const message=getErrorMessage(response);SweetAlert.swal({title:message+". Do you want to proceed with the task execution?",text:"",type:"warning",showCancelButton:!0,confirmButtonColor:"#DD6B55",confirmButtonText:"No",cancelButtonText:"Yes",closeOnConfirm:!0,closeOnCancel:!0,animation:"false",customClass:"animated fadeInUp"},(function(isConfirm){isConfirm||TDMService.executeTask(taskId,forced).then((function(response){if("SUCCESS"==response.errorCode)tasksTableCtrl.tasksData[index].executioncount=1,toastr.success("Task # "+task_title," Successfully started"),tasksTableCtrl.dtInstance.reloadData((function(data){}),!0),tasksTableCtrl.taskExecutionSummary(taskId);else{const message=getErrorMessage(response);SweetAlert.swal({title:message,text:"",type:"warning",closeOnCancel:!0,animation:"false",customClass:"animated fadeInUp start-task-error"},(function(isConfirm){}))}}))}))}else{const message=getErrorMessage(response);SweetAlert.swal({title:message,text:"",type:"warning",closeOnCancel:!0,animation:"false",customClass:"animated fadeInUp start-task-error"},(function(isConfirm){}))}tasksTableCtrl.tasksData[index].disabled=!1})).catch(err=>{toastr.error(response.message,"The task cannot be started.")})},tasksTableCtrl.holdTask=function(index){TDMService.holdTask(tasksTableCtrl.tasksData[index].task_id).then((function(response){"SUCCESS"==response.errorCode?(tasksTableCtrl.tasksData[index].task_execution_status="onHold",toastr.success("Task # "+tasksTableCtrl.tasksData[index].task_title," was Holded"),tasksTableCtrl.tasksData[index].onHold=!0,tasksTableCtrl.dtInstance.reloadData((function(data){}),!0)):toastr.error("Task # "+task_title,"Failed to hold")}))},$scope.getRunningTasks=function(){TDMService.getRunningTasks().then((function(response){if("SUCCESS"==response.errorCode){tasksTableCtrl.tasksData=_.map(tasksTableCtrl.tasksData,(function(task){return task.executioncount=0,task}));for(var i=0;i<response.result.length;i++){var task=_.find(tasksTableCtrl.tasksData,{task_id:parseInt(response.result[i])});task&&(task.executioncount=1)}response.result.toString()!==tasksTableCtrl.prevRunningTasksId&&(tasksTableCtrl.prevRunningTasksId=response.result.toString(),tasksTableCtrl.dtInstance&&($(".tooltip").remove(),$timeout(()=>{tasksTableCtrl.dtInstance.reloadData((function(data){}),!1)},300)))}}))},tasksTableCtrl.activateTask=function(index){TDMService.activateTask(tasksTableCtrl.tasksData[index].task_id).then((function(response){"SUCCESS"==response.errorCode?(tasksTableCtrl.tasksData[index].task_execution_status="Active",toastr.success("Task # "+tasksTableCtrl.tasksData[index].task_title," was activated"),tasksTableCtrl.tasksData[index].onHold=!1,tasksTableCtrl.dtInstance.reloadData((function(data){}),!0)):toastr.error("Task # "+task_title,"Failed to activate")}))},tasksTableCtrl.deleteTask=(task_id,task_title)=>{TDMService.deleteTask({task_id:task_id,task_title:task_title}).then((function(response){"SUCCESS"==response.errorCode?(toastr.success("Task # "+task_id,"deleted Successfully"),$timeout((function(){$scope.content.openTasks(!0)}),400)):toastr.error("Task # "+task_id,"failed to delete")}))},tasksTableCtrl.saveAs=function(task_id){console.log("Save As check");var taskData=_.find(tasksTableCtrl.tasksData,{task_id:parseInt(task_id)});taskData&&TDMService.postGenericAPI("checkSaveTaskAs",{task_id:taskData.task_id,environment_id:taskData.environment_id,source_environment_id:taskData.source_environment_id,taskData:taskData}).then((function(response){"FAILED"!=response.errorCode?tasksTableCtrl.openTask(taskData.task_id,!0):toastr.error(response.message,"The task cannot be copied.")}))}},controllerAs:"tasksTableCtrl"}}angular.module("TDM-FE").directive("tasksTableDirective",tasksTableDirective);