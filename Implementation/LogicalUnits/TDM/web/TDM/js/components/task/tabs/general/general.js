function generalTab(){return{restrict:"E",templateUrl:"js/components/task/tabs/general/general.html",scope:{content:"=",generalForm:"=",disabled:"=",copyTask:"="},controller:function($scope,TDMService,toastr,$timeout,AuthService,$state,DTOptionsBuilder,DTColumnBuilder,$q,$compile){var generalTabCtrl=this;$scope.currentYear=(new Date).getFullYear(),TDMService.getTDMVersion().then(response=>{$scope.version=response.result}),$scope.blockBackButton=!0,generalTabCtrl.disabled=$scope.disabled,generalTabCtrl.taskData=$scope.content,generalTabCtrl.extract=!1,generalTabCtrl.load=!1,generalTabCtrl.delete=!1,generalTabCtrl.reserve=!1,generalTabCtrl.generate=!1,generalTabCtrl.sourceEnvType="",generalTabCtrl.taskTitlePattern="^((?!_).)*$",generalTabCtrl.TaskTypeHints={1e4:["Extract the data from source environment into the TDM warehouse"],10100:["Refresh data from source and load (provision) it to target environment"],10101:["Extract the data from source environment","Provision (load) the data to the target environment and mark the entities as reserved"],10110:["Extract the data from source environment","Delete and reprovision (reload) the data to the target environment"],10111:["Extract data from source environment","Delete and reload (reprovision) data to target environment","Mark entities as reserved"],"00100":["Provision data to the target environment"],"00101":["Get data from TDM warehouse and load (provision) it to target environment","Mark entities as reserved"],"00110":["Get the data from the TDM warehouse","Delete and reprovision (reload) it to the target environment"],"00111":["Get data from TDM warehouse","Delete and reload (reprovision) data to target environment","Mark entities as reserved"],"00010":["Delete (clean) entities from target environment"],"00001":["Reserve entities in the target environment."],"01000":["Generate synthetic entities and save them into the TDM warehouse."],"01100":["Generate synthetic entities and save them into the TDM warehouse.","Load the synthetic entities to the target environment"],11100:["Generate synthetic entities and save them into the TDM warehouse.","Load the synthetic entities to the target environment."],"01101":["Generate synthetic entities and save them into the TDM warehouse.","Load the synthetic entities to the target environment and mark them as reserved."],11101:["Generate synthetic entities and save them into the TDM warehouse.","Load the synthetic entities to the target environment and mark them as reserved."],"01101":["Generate synthetic entities and save them into the TDM warehouse.","Load the synthetic entities to the target environment and mark them as reserved."],11101:["Generate synthetic entities and save them into the TDM warehouse.","Load the synthetic entities to the target environment and mark them as reserved."]},TDMService.getActiveBusinessentitiesAPI().then(response=>{"SUCCESS"===response.errorCode?generalTabCtrl.businessEnttites=response.result:generalTabCtrl.businessEnttites=[],generalTabCtrl.taskData.be_id&&generalTabCtrl.businessEntityChange(!0)}),generalTabCtrl.taskTitleChange=()=>{$scope.generalForm.taskTitle.$setValidity("alreadyExist",!0)},generalTabCtrl.environmentChange=(init,type)=>{if(generalTabCtrl.generate){const synthetic_source_env=_.find(generalTabCtrl.sourceEnvironments,{synthetic_indicator:!0});if(!synthetic_source_env)return;generalTabCtrl.taskData.source_environment_id=synthetic_source_env.environment_id}if(!generalTabCtrl.taskData.be_id||"EXTRACT"===generalTabCtrl.taskData.task_type&&!generalTabCtrl.taskData.source_environment_id||"RESERVE"===generalTabCtrl.taskData.task_type&&!generalTabCtrl.taskData.environment_id||"LOAD"===generalTabCtrl.taskData.task_type&&generalTabCtrl.taskData.delete_before_load&&generalTabCtrl.load&&!generalTabCtrl.taskData.environment_id||"LOAD"===generalTabCtrl.taskData.task_type&&generalTabCtrl.load&&(!generalTabCtrl.taskData.environment_id||!generalTabCtrl.taskData.source_environment_id))return;if(init){const source_env_check=_.find(generalTabCtrl.sourceEnvironments,{environment_id:generalTabCtrl.taskData.source_environment_id});source_env_check.synthetic_indicator&&source_env_check.synthetic_indicator&&null===generalTabCtrl.taskData.sync_mode&&(generalTabCtrl.generate=!0,generalTabCtrl.extract=!1,generalTabCtrl.taskData.generateTask=!0)}else"src"===type&&generalTabCtrl.taskData.source_env_name&&(generalTabCtrl.srcEnvChanged=!0),"target"===type&&generalTabCtrl.taskData.environment_name&&(generalTabCtrl.targetEnvChanged=!0),generalTabCtrl.taskData.source_environment_id||(generalTabCtrl.srcEnvChanged=!1),generalTabCtrl.taskData.environment_id||(generalTabCtrl.targetEnvChanged=!1);if(generalTabCtrl.taskData.source_environment_id){const source_env=_.find(generalTabCtrl.sourceEnvironments,{environment_id:generalTabCtrl.taskData.source_environment_id});source_env&&(generalTabCtrl.taskData.source_env_name=source_env.environment_name,source_env.synthetic_indicator?generalTabCtrl.taskData.source_synthetic=!0:generalTabCtrl.taskData.source_synthetic=!1,source_env.mask_sensitive_data?(generalTabCtrl.taskData.env_mask_data=source_env.mask_sensitive_data,generalTabCtrl.taskData.mask_sensitive_data=!0):(generalTabCtrl.taskData.env_mask_data=!1,generalTabCtrl.taskData.mask_sensitive_data=!1))}if(generalTabCtrl.taskData.environment_id){const target_env=_.find(generalTabCtrl.targetEnvironments,{environment_id:generalTabCtrl.taskData.environment_id});target_env&&(generalTabCtrl.taskData.environment_name=target_env.environment_name,target_env.synthetic_indicator?generalTabCtrl.targetIsSynthetic=!0:generalTabCtrl.targetIsSynthetic=!1)}const promises=[];generalTabCtrl.taskData.source_environment_id&&promises.push(TDMService.getLogicalUnitsForBusinessEntityAndEnv(generalTabCtrl.taskData.be_id,generalTabCtrl.taskData.source_environment_id)),generalTabCtrl.taskData.environment_id&&promises.push(TDMService.getLogicalUnitsForBusinessEntityAndEnv(generalTabCtrl.taskData.be_id,generalTabCtrl.taskData.environment_id)),Promise.all(promises).then(responses=>{"SUCCESS"==responses[0].errorCode?(generalTabCtrl.taskData.allLogicalUnits=responses[0].result,responses[1]&&"SUCCESS"==responses[1].errorCode&&(generalTabCtrl.taskData.allLogicalUnits=generalTabCtrl.taskData.allLogicalUnits.concat(responses[1].result)),generalTabCtrl.taskData.allLogicalUnits=_.unique(generalTabCtrl.taskData.allLogicalUnits,"lu_name"),init||(generalTabCtrl.taskData.selectedLogicalUnits=generalTabCtrl.taskData.allLogicalUnits)):toastr.error("Business entity # "+generalTabCtrl.taskData.be_id,"Failed to get Logical units")})},generalTabCtrl.businessEntityChange=init=>{const foundBE=generalTabCtrl.businessEnttites.find(it=>it.be_id===generalTabCtrl.taskData.be_id);foundBE?TDMService.getEnvironmentsByUserandBe(foundBE.be_name).then(response=>{if("FAILURE"!==response.errorCode?(generalTabCtrl.sourceEnvironments=_.filter(angular.copy(response.result),(function(env){return"SOURCE"===env.environment_type||"BOTH"===env.environment_type})),generalTabCtrl.targetEnvironments=_.filter(angular.copy(response.result),(function(env){return"TARGET"===env.environment_type||"BOTH"===env.environment_type}))):(generalTabCtrl.sourceEnvironments=[],generalTabCtrl.targetEnvironments=[]),init){if(generalTabCtrl.taskData.source_environment_id&&"EXTRACT"!==generalTabCtrl.taskData.task_type){_.find(generalTabCtrl.sourceEnvironments,{environment_id:generalTabCtrl.taskData.source_environment_id}).synthetic_indicator&&null===generalTabCtrl.taskData.sync_mode&&(generalTabCtrl.generate=!0,generalTabCtrl.extract=!1,generalTabCtrl.taskData.generateTask=!0)}generalTabCtrl.environmentChange(!0)}else if(generalTabCtrl.taskData.source_environment_id=null,generalTabCtrl.taskData.environment_id=null,generalTabCtrl.generate){const synthetic_source_env=_.find(generalTabCtrl.sourceEnvironments,{synthetic_indicator:!0});if(!synthetic_source_env)return;generalTabCtrl.taskData.source_environment_id=synthetic_source_env.environment_id,generalTabCtrl.load||generalTabCtrl.reserve||generalTabCtrl.environmentChange()}}):console.error("cannot find be name")},generalTabCtrl.taskTypeChange=()=>{if(!(generalTabCtrl.extract||generalTabCtrl.load||generalTabCtrl.reserve||generalTabCtrl.generate||generalTabCtrl.delete))return generalTabCtrl.taskData.task_type="",void(generalTabCtrl.sourceEnvType="Extract from");generalTabCtrl.taskData.extractSelected=!1,generalTabCtrl.extract&&(generalTabCtrl.taskData.extractSelected=!0),generalTabCtrl.extract&&!generalTabCtrl.load?(generalTabCtrl.taskData.task_type="EXTRACT",generalTabCtrl.taskData.load_entity=!1,generalTabCtrl.taskData.delete_before_load=!1,generalTabCtrl.taskData.reserve_ind=!1,generalTabCtrl.sourceEnvType="Extract from"):generalTabCtrl.reserve&&!generalTabCtrl.load?(generalTabCtrl.taskData.task_type="RESERVE",generalTabCtrl.taskData.load_entity=!1,generalTabCtrl.taskData.delete_before_load=!1,generalTabCtrl.taskData.reserve_ind=!0,generalTabCtrl.sourceEnvType="Reseve in"):generalTabCtrl.delete&&!generalTabCtrl.load?(generalTabCtrl.taskData.task_type="LOAD",generalTabCtrl.taskData.load_entity=!1,generalTabCtrl.taskData.delete_before_load=!0,generalTabCtrl.taskData.reserve_ind=!1,generalTabCtrl.sourceEnvType="Delete from"):generalTabCtrl.generate&&!generalTabCtrl.load?(generalTabCtrl.taskData.task_type="GENERATE",generalTabCtrl.taskData.sync_mode=null,generalTabCtrl.taskData.reserve_ind=!1,generalTabCtrl.taskData.generateTask=!0,generalTabCtrl.extract=!1):(generalTabCtrl.sourceEnvType="Load (provision) to",generalTabCtrl.taskData.task_type="LOAD",generalTabCtrl.taskData.load_entity=!0,generalTabCtrl.generate?(generalTabCtrl.taskData.generateTask=!0,generalTabCtrl.taskData.sync_mode=null,generalTabCtrl.load||generalTabCtrl.reserve||(generalTabCtrl.taskData.environment_id=null)):generalTabCtrl.taskData.generateTask=!1,generalTabCtrl.delete?generalTabCtrl.taskData.delete_before_load=!0:generalTabCtrl.taskData.delete_before_load=!1,generalTabCtrl.reserve?generalTabCtrl.taskData.reserve_ind=!0:generalTabCtrl.taskData.reserve_ind=!1),generalTabCtrl.businessEnttites&&generalTabCtrl.businessEnttites.length>0&&generalTabCtrl.businessEntityChange()},generalTabCtrl.closeAdvanedOptions=()=>{generalTabCtrl.advancedOptionsToggle=!1},generalTabCtrl.toggleAdvanedOptions=()=>{!generalTabCtrl.disableChange&&(generalTabCtrl.extract||generalTabCtrl.load||generalTabCtrl.delete||generalTabCtrl.generate||generalTabCtrl.reserve)?generalTabCtrl.advancedOptionsToggle=!generalTabCtrl.advancedOptionsToggle:generalTabCtrl.advancedOptionsToggle=!1},generalTabCtrl.updateLogicalUnits=selectedLUs=>{generalTabCtrl.taskData.selectedLogicalUnits=angular.copy(selectedLUs)},"EXTRACT"===generalTabCtrl.taskData.task_type?(generalTabCtrl.extract=!0,generalTabCtrl.taskData.extractSelected=!0):"RESERVE"===generalTabCtrl.taskData.task_type?generalTabCtrl.reserve=!0:"GENERATE"===generalTabCtrl.taskData.task_type?(generalTabCtrl.generate=!0,generalTabCtrl.taskData.generateTask=!0):"LOAD"===generalTabCtrl.taskData.task_type&&(generalTabCtrl.load=!0,generalTabCtrl.taskData.load_entity||"refernceOnly"===generalTabCtrl.taskData.reference?("refernceOnly"===generalTabCtrl.taskData.reference||"FORCE"!==generalTabCtrl.taskData.sync_mode&&generalTabCtrl.taskData.sync_mode)&&("refernceOnly"!==generalTabCtrl.taskData.reference||generalTabCtrl.taskData.version_ind)||(generalTabCtrl.extract=!0,generalTabCtrl.taskData.extractSelected=!0):generalTabCtrl.load=!1,generalTabCtrl.taskData.delete_before_load&&(generalTabCtrl.delete=!0),generalTabCtrl.taskData.reserve_ind&&(generalTabCtrl.reserve=!0)),generalTabCtrl.envChangeNoteDisplay=()=>{},generalTabCtrl.taskTypeChange(),generalTabCtrl.taskData.task_id&&(generalTabCtrl.editMode=!0,generalTabCtrl.taskData.selectedLogicalUnits||TDMService.getTaskLogicalUnits(generalTabCtrl.taskData.task_id).then((function(response){"SUCCESS"==response.errorCode?generalTabCtrl.taskData.selectedLogicalUnits=response.result:toastr.error("Systems # "+generalTabCtrl.taskData.task_id,"Failed to get Task Systems")})))},controllerAs:"generalTabCtrl"}}angular.module("TDM-FE").directive("generalTab",generalTab);