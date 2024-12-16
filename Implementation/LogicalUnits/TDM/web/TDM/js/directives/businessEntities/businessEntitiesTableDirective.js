function businessEntitiesTableDirective(){return{restrict:"E",templateUrl:"views/businessEntities/businessEntitiesTable.html",scope:{content:"="},controller:function($scope,$compile,TDMService,DTColumnBuilder,DTOptionsBuilder,$q,$timeout){var businessEntitiesTableCtrl=this;businessEntitiesTableCtrl.loadingTable=!0,TDMService.getBusinessEntities().then((function(response){if("SUCCESS"==response.errorCode){businessEntitiesTableCtrl.businessEntitiesData=_.sortBy(response.result,(function(value){return new Date(value.be_creation_date)})),businessEntitiesTableCtrl.businessEntitiesData.reverse(),businessEntitiesTableCtrl.dtInstance={},businessEntitiesTableCtrl.dtColumns=[],businessEntitiesTableCtrl.dtColumnDefs=[],businessEntitiesTableCtrl.headers=[{column:"be_name",name:"Name",clickAble:!0},{column:"be_description",name:"Description",clickAble:!1},{column:"execution_mode",name:"Execution mode",clickAble:!1},{column:"be_creation_date",name:"Creation Date",clickAble:!1,type:"date"},{column:"be_created_by",name:"Created By",clickAble:!1},{column:"be_last_updated_date",name:"Last Update Date",clickAble:!1,type:"date"},{column:"be_last_updated_by",name:"Updated By",clickAble:!1},{column:"be_status",name:"Status",clickAble:!1}];for(var clickAbleColumn=function(data,type,full,meta){return`<a id="be_${data}" ng-click="businessEntitiesTableCtrl.openBusinessEntity(${full.be_id})">${data}</a>`},changeToLocalDate=function(data,type,full,meta){return moment(data).format("DD MMM YYYY, HH:mm")},descriptionColumn=function(data,type,full,meta){return`<span title="'${data}'">${data}</span>`},i=0;i<businessEntitiesTableCtrl.headers.length;i++)1==businessEntitiesTableCtrl.headers[i].clickAble?businessEntitiesTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(businessEntitiesTableCtrl.headers[i].column).withTitle(businessEntitiesTableCtrl.headers[i].name).renderWith(clickAbleColumn)):"be_description"==businessEntitiesTableCtrl.headers[i].column?businessEntitiesTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(businessEntitiesTableCtrl.headers[i].column).withTitle(businessEntitiesTableCtrl.headers[i].name).renderWith(descriptionColumn)):"date"==businessEntitiesTableCtrl.headers[i].type?businessEntitiesTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(businessEntitiesTableCtrl.headers[i].column).withTitle(businessEntitiesTableCtrl.headers[i].name).renderWith(changeToLocalDate)):businessEntitiesTableCtrl.dtColumns.push(DTColumnBuilder.newColumn(businessEntitiesTableCtrl.headers[i].column).withTitle(businessEntitiesTableCtrl.headers[i].name));var getTableData=function(){var deferred=$q.defer();return deferred.resolve(businessEntitiesTableCtrl.businessEntitiesData),deferred.promise};businessEntitiesTableCtrl.dtOptions=DTOptionsBuilder.fromFnPromise((function(){return getTableData()})).withDOM('<"html5buttons"B>lTfgitp').withOption("createdRow",(function(row){$compile(angular.element(row).contents())($scope)})).withOption("aaSorting",[7,"asc"]).withOption("scrollX",!1).withButtons([]).withOption("caseInsensitive",!0).withOption("search",{caseInsensitive:!1}),businessEntitiesTableCtrl.businessEntitiesData&&businessEntitiesTableCtrl.businessEntitiesData.length>0&&businessEntitiesTableCtrl.dtOptions.withLightColumnFilter({0:{type:"text"},1:{type:"text"},2:{type:"text"},3:{type:"text"},4:{type:"select",values:_.map(_.unique(_.map(businessEntitiesTableCtrl.businessEntitiesData,"be_created_by")),(function(el){return{value:el,label:el}}))},5:{type:"text"},6:{type:"select",values:_.map(_.unique(_.map(businessEntitiesTableCtrl.businessEntitiesData,"be_last_updated_by")),(function(el){return{value:el,label:el}}))},7:{type:"select",defaultValue:"Active",values:[{value:"Inactive",label:"Inactive"},{value:"Active",label:"Active"}]}}),businessEntitiesTableCtrl.dtInstanceCallback=function(dtInstance){angular.isFunction(businessEntitiesTableCtrl.dtInstance)?businessEntitiesTableCtrl.dtInstance(dtInstance):angular.isDefined(businessEntitiesTableCtrl.dtInstance)&&(businessEntitiesTableCtrl.dtInstance=dtInstance)},null!=businessEntitiesTableCtrl.dtInstance.changeData&&businessEntitiesTableCtrl.dtInstance.changeData(getTableData()),$timeout(()=>{businessEntitiesTableCtrl.loadingTable=!1})}})),businessEntitiesTableCtrl.openBusinessEntity=function(businessEntityId){if($scope.content.openBusinessEntity){var businessEntityData=_.find(businessEntitiesTableCtrl.businessEntitiesData,{be_id:businessEntityId});if(businessEntityData)return void $scope.content.openBusinessEntity(businessEntityData)}},businessEntitiesTableCtrl.openNewBusinessEntity=function(){$scope.content.openNewBusinessEntity&&$scope.content.openNewBusinessEntity(businessEntitiesTableCtrl.businessEntitiesData)}},controllerAs:"businessEntitiesTableCtrl"}}angular.module("TDM-FE").directive("businessEntitiesTableDirective",businessEntitiesTableDirective);