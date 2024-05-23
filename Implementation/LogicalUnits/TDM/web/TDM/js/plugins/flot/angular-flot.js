angular.module("angular-flot",[]).directive("flot",(function(){return{restrict:"EA",template:"<div></div>",scope:{dataset:"=",options:"=",callback:"="},link:function(scope,element,attributes){var height,init,onDatasetChanged,onOptionsChanged,plot,plotArea,width,_ref,_ref1;if(plot=null,width=attributes.width||"100%",height=attributes.height||"100%",(null!=(_ref=scope.options)&&null!=(_ref1=_ref.legend)?_ref1.container:void 0)instanceof jQuery)throw'Please use a jQuery expression string with the "legend.container" option.';return scope.dataset||(scope.dataset=[]),scope.options||(scope.options={legend:{show:!1}}),(plotArea=$(element.children()[0])).css({width:width,height:height}),init=function(){var plotObj;return plotObj=$.plot(plotArea,scope.dataset,scope.options),scope.callback&&scope.callback(plotObj),plotObj},onDatasetChanged=function(dataset){return plot?(plot.setData(dataset),plot.setupGrid(),plot.draw()):plot=init()},scope.$watch("dataset",onDatasetChanged,!0),onOptionsChanged=function(){return plot=init()},scope.$watch("options",onOptionsChanged,!0)}}}));