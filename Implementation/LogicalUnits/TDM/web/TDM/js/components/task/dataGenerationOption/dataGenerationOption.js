function generateData(){return{restrict:"EA",templateUrl:"js/components/task/dataGenerationOption/dataGenerationOption.html",scope:{chosenItems:"=",data:"=",mandatoryFields:"=",form:"="},controller:function($scope,$timeout){const dataGenerationOptionCtrl=this,editorsData=[];$scope.chosenItems.forEach(editorName=>{editorsData.push($scope.data[editorName].editor)}),dataGenerationOptionCtrl.editorsData=editorsData,$scope.$watch("chosenItems",(newValue,oldValue)=>{if(newValue.length>oldValue.length){for(let i=0;i<newValue.length;i++)if(oldValue.indexOf(newValue[i])<0){dataGenerationOptionCtrl.addEditor(newValue[i]);break}}else if(newValue.length<oldValue.length)for(let i=0;i<oldValue.length;i++)if(newValue.indexOf(oldValue[i])<0){dataGenerationOptionCtrl.deleteEditor(oldValue[i]);break}},!0,!0),dataGenerationOptionCtrl.addEditor=name=>{$scope.data.refData.addPlugins([$scope.data[name].editor]);const requestedEntitiesTab=$("html");requestedEntitiesTab&&requestedEntitiesTab.length>0&&$timeout(()=>{requestedEntitiesTab[0].scrollTo(0,requestedEntitiesTab[0].scrollHeight)},150)},dataGenerationOptionCtrl.deleteEditor=name=>{$scope.data.refData.removePluginByName(name)}},controllerAs:"dataGenerationOptionCtrl"}}angular.module("TDM-FE").directive("generateData",generateData);