/*!
 * File:        jquery.dataTables.rowReordering.js
 * Version:     1.2.3 / Datatables 1.10 hack
 * Author:      Jovan Popovic
 *
 * Copyright 2013 Jovan Popovic, all rights reserved.
 *
 * This source file is free software, under either the GPL v2 license or a
 * BSD style license, as supplied with this software.
 */
!function($){"use strict";($.fn.rowReordering=function(options){function fnCancelSorting(oTable,tbody,properties,iLogLevel,sMessage){tbody.sortable("cancel"),iLogLevel<=properties.iLogLevel&&(null!=sMessage?properties.fnAlert(sMessage,""):properties.fnAlert("Row cannot be moved","")),properties.fnEndProcessingMode(oTable)}function fnMoveRows(oTable,sSelector,iCurrentPosition,iNewPosition,sDirection,id,sGroup){var iStart=iCurrentPosition,iEnd=iNewPosition;"back"==sDirection&&(iStart=iNewPosition,iEnd=iCurrentPosition),$(oTable.fnGetNodes()).each((function(){if(""==sGroup||$(this).attr("data-group")==sGroup){var tr=this,iRowPosition=parseInt(oTable.fnGetData(tr,properties.iIndexColumn),10);iStart<=iRowPosition&&iRowPosition<=iEnd&&(tr.id==id?oTable.fnUpdate(iNewPosition,oTable.fnGetPosition(tr),properties.iIndexColumn,!1):"back"==sDirection?oTable.fnUpdate(iRowPosition+1,oTable.fnGetPosition(tr),properties.iIndexColumn,!1):oTable.fnUpdate(iRowPosition-1,oTable.fnGetPosition(tr),properties.iIndexColumn,!1))}}));var oSettings=oTable.fnSettings();if(!1===oSettings.oFeatures.bServerSide){var before=oSettings._iDisplayStart;oSettings.oApi._fnReDraw(oSettings),oSettings._iDisplayStart=before,oSettings.oApi._fnCalculateEnd(oSettings)}oSettings.oApi._fnDraw(oSettings)}var tables,defaults={iIndexColumn:0,iStartPosition:1,sURL:null,sRequestType:"POST",iGroupingLevel:0,fnAlert:function(message,type){alert(message)},fnSuccess:jQuery.noop,iLogLevel:1,sDataGroupAttribute:"data-group",fnStartProcessingMode:function(oTable){oTable.fnSettings().oFeatures.bProcessing&&$(".dataTables_processing").css("visibility","visible")},fnEndProcessingMode:function(oTable){oTable.fnSettings().oFeatures.bProcessing&&$(".dataTables_processing").css("visibility","hidden")},fnUpdateAjaxRequest:jQuery.noop},properties=$.extend(defaults,options),tableFixHelper=function(e,tr){var $originals=tr.children(),$helper=tr.clone();return $helper.children().each((function(index){$(this).width($originals.eq(index).width())})),$helper};return tables=this instanceof jQuery?this:this.context,$.each(tables,(function(){var oTable,aaSortingFixed=null==(oTable=void 0!==this.nodeType?$(this).dataTable():$(this.nTable).dataTable()).fnSettings().aaSortingFixed?new Array:oTable.fnSettings().aaSortingFixed;aaSortingFixed.push([properties.iIndexColumn,"asc"]),oTable.fnSettings().aaSortingFixed=aaSortingFixed;for(var i=0;i<oTable.fnSettings().aoColumns.length;i++)oTable.fnSettings().aoColumns[i].bSortable=!1;oTable.fnDraw(),$("tbody",oTable).disableSelection().sortable({cursor:"move",helper:tableFixHelper,update:function(event,ui){var $dataTable=oTable,tbody=$(this),sSelector="tbody tr",sGroup="";if(properties.bGroupingUsed){if(null==(sGroup=$(ui.item).attr(properties.sDataGroupAttribute))||null==sGroup)return void fnCancelSorting($dataTable,tbody,properties,3,"Grouping row cannot be moved");sSelector="tbody tr["+properties.sDataGroupAttribute+" ='"+sGroup+"']"}var tr=$(ui.item.context),oState=function(oTable,sSelector,tr){var iCurrentPosition=parseInt(oTable.fnGetData(tr[0],properties.iIndexColumn),10),iNewPosition=-1,trPrevious=tr.prev(sSelector);if(trPrevious.length>0)(iNewPosition=parseInt(oTable.fnGetData(trPrevious[0],properties.iIndexColumn),10))<iCurrentPosition&&(iNewPosition+=1);else{var trNext=tr.next(sSelector);trNext.length>0&&(iNewPosition=parseInt(oTable.fnGetData(trNext[0],properties.iIndexColumn),10))>iCurrentPosition&&(iNewPosition-=1)}return{sDirection:iNewPosition<iCurrentPosition?"back":"forward",iCurrentPosition:iCurrentPosition,iNewPosition:iNewPosition}}($dataTable,sSelector,tr);if(-1!=oState.iNewPosition){var sRequestData={id:ui.item.context.id,fromPosition:oState.iCurrentPosition,toPosition:oState.iNewPosition,direction:oState.sDirection,group:sGroup,data:properties.sData};if(null!=properties.sURL){properties.fnStartProcessingMode($dataTable);var oAjaxRequest={url:properties.sURL,type:properties.sRequestType,data:sRequestData,success:function(data){properties.fnSuccess(data),properties.avoidMovingRows||fnMoveRows($dataTable,0,oState.iCurrentPosition,oState.iNewPosition,oState.sDirection,ui.item.context.id,sGroup),properties.fnEndProcessingMode($dataTable),properties.fnUpdateCallback&&"function"==typeof properties.fnUpdateCallback&&properties.fnUpdateCallback(sRequestData)},error:function(jqXHR){var err=""!=jqXHR.responseText?jqXHR.responseText:jqXHR.statusText;fnCancelSorting($dataTable,tbody,properties,1,err)}};properties.fnUpdateAjaxRequest(oAjaxRequest,properties,$dataTable),$.ajax(oAjaxRequest)}else fnMoveRows($dataTable,0,oState.iCurrentPosition,oState.iNewPosition,oState.sDirection,ui.item.context.id,sGroup),properties.fnUpdateCallback&&"function"==typeof properties.fnUpdateCallback&&properties.fnUpdateCallback(sRequestData)}else fnCancelSorting($dataTable,tbody,properties,2)}})})),this},$.fn.dataTable.rowReordering=$.fn.rowReordering,$.fn.DataTable.rowReordering=$.fn.rowReordering,$.fn.dataTable.Api)&&$.fn.dataTable.Api.register("rowReordering()",$.fn.rowReordering)}(jQuery);
//# sourceMappingURL=data:application/json;charset=utf8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbInBsdWdpbnMvZGF0YVRhYmxlcy9qcXVlcnkuZGF0YVRhYmxlcy5yb3dSZW9yZGVyaW5nLmpzIl0sIm5hbWVzIjpbIiQiLCJmbiIsInJvd1Jlb3JkZXJpbmciLCJvcHRpb25zIiwiZm5DYW5jZWxTb3J0aW5nIiwib1RhYmxlIiwidGJvZHkiLCJwcm9wZXJ0aWVzIiwiaUxvZ0xldmVsIiwic01lc3NhZ2UiLCJzb3J0YWJsZSIsInVuZGVmaW5lZCIsImZuQWxlcnQiLCJmbkVuZFByb2Nlc3NpbmdNb2RlIiwiZm5Nb3ZlUm93cyIsInNTZWxlY3RvciIsImlDdXJyZW50UG9zaXRpb24iLCJpTmV3UG9zaXRpb24iLCJzRGlyZWN0aW9uIiwiaWQiLCJzR3JvdXAiLCJpU3RhcnQiLCJpRW5kIiwiZm5HZXROb2RlcyIsImVhY2giLCJ0aGlzIiwiYXR0ciIsInRyIiwiaVJvd1Bvc2l0aW9uIiwicGFyc2VJbnQiLCJmbkdldERhdGEiLCJpSW5kZXhDb2x1bW4iLCJmblVwZGF0ZSIsImZuR2V0UG9zaXRpb24iLCJvU2V0dGluZ3MiLCJmblNldHRpbmdzIiwib0ZlYXR1cmVzIiwiYlNlcnZlclNpZGUiLCJiZWZvcmUiLCJfaURpc3BsYXlTdGFydCIsIm9BcGkiLCJfZm5SZURyYXciLCJfZm5DYWxjdWxhdGVFbmQiLCJfZm5EcmF3IiwidGFibGVzIiwiZGVmYXVsdHMiLCJpU3RhcnRQb3NpdGlvbiIsInNVUkwiLCJzUmVxdWVzdFR5cGUiLCJpR3JvdXBpbmdMZXZlbCIsIm1lc3NhZ2UiLCJ0eXBlIiwiYWxlcnQiLCJmblN1Y2Nlc3MiLCJqUXVlcnkiLCJub29wIiwic0RhdGFHcm91cEF0dHJpYnV0ZSIsImZuU3RhcnRQcm9jZXNzaW5nTW9kZSIsImJQcm9jZXNzaW5nIiwiY3NzIiwiZm5VcGRhdGVBamF4UmVxdWVzdCIsImV4dGVuZCIsInRhYmxlRml4SGVscGVyIiwiZSIsIiRvcmlnaW5hbHMiLCJjaGlsZHJlbiIsIiRoZWxwZXIiLCJjbG9uZSIsImluZGV4Iiwid2lkdGgiLCJlcSIsImNvbnRleHQiLCJhYVNvcnRpbmdGaXhlZCIsIm5vZGVUeXBlIiwiZGF0YVRhYmxlIiwiblRhYmxlIiwiQXJyYXkiLCJwdXNoIiwiaSIsImFvQ29sdW1ucyIsImxlbmd0aCIsImJTb3J0YWJsZSIsImZuRHJhdyIsImRpc2FibGVTZWxlY3Rpb24iLCJjdXJzb3IiLCJoZWxwZXIiLCJ1cGRhdGUiLCJldmVudCIsInVpIiwiJGRhdGFUYWJsZSIsImJHcm91cGluZ1VzZWQiLCJpdGVtIiwib1N0YXRlIiwidHJQcmV2aW91cyIsInByZXYiLCJ0ck5leHQiLCJuZXh0IiwiZm5HZXRTdGF0ZSIsInNSZXF1ZXN0RGF0YSIsImZyb21Qb3NpdGlvbiIsInRvUG9zaXRpb24iLCJkaXJlY3Rpb24iLCJncm91cCIsImRhdGEiLCJzRGF0YSIsIm9BamF4UmVxdWVzdCIsInVybCIsInN1Y2Nlc3MiLCJhdm9pZE1vdmluZ1Jvd3MiLCJmblVwZGF0ZUNhbGxiYWNrIiwiZXJyb3IiLCJqcVhIUiIsImVyciIsInJlc3BvbnNlVGV4dCIsInN0YXR1c1RleHQiLCJhamF4IiwiRGF0YVRhYmxlIiwiQXBpIiwicmVnaXN0ZXIiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7Q0E0Q0EsU0FBV0EsR0FFUCxjQUNBQSxFQUFFQyxHQUFHQyxjQUFnQixTQUFVQyxTQWlDM0IsU0FBU0MsZ0JBQWdCQyxPQUFRQyxNQUFPQyxXQUFZQyxVQUFXQyxVQUMzREgsTUFBTUksU0FBUyxVQUNaRixXQUFXRCxXQUFXQyxZQUNQRyxNQUFYRixTQUNDRixXQUFXSyxRQUFRSCxTQUFVLElBRTdCRixXQUFXSyxRQUFRLHNCQUF1QixLQUdsREwsV0FBV00sb0JBQW9CUixRQWtDbkMsU0FBU1MsV0FBV1QsT0FBUVUsVUFBV0MsaUJBQWtCQyxhQUFjQyxXQUFZQyxHQUFJQyxRQUNuRixJQUFJQyxPQUFTTCxpQkFDVE0sS0FBT0wsYUFDTyxRQUFkQyxhQUNBRyxPQUFTSixhQUNUSyxLQUFPTixrQkFHWGhCLEVBQUVLLE9BQU9rQixjQUFjQyxNQUFLLFdB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QUFXOEMsVUFBVWtELE1BR2hCaEcsV0FBV3FHLGlCQUNaOUYsV0FBVzJFLFdBQVkxRSxFQUFXNkUsT0FBTzVFLGlCQUFrQjRFLE9BQU8zRSxhQUFjMkUsT0FBTzFFLFdBQVlzRSxHQUFHRyxLQUFLcEIsUUFBUXBELEdBQUlDLFFBRTNIYixXQUFXTSxvQkFBb0I0RSxZQU01QmxGLFdBQVdzRyxrQkFBNEQsbUJBQWpDdEcsV0FBMkIsa0JBQ2hFQSxXQUFXc0csaUJBQWlCWCxlQUlwQ1ksTUFBTyxTQUFVQyxPQUdiLElBQUlDLElBQTZCLElBQXRCRCxNQUFNRSxhQUFxQkYsTUFBTUUsYUFBZUYsTUFBTUcsV0FDakU5RyxnQkFBZ0JxRixXQUFZbkYsTUFBT0MsV0FBWSxFQUFHeUcsT0FJMUR6RyxXQUFXcUQsb0JBQW9CNkMsYUFBY2xHLFdBQVlrRixZQUN6RHpGLEVBQUVtSCxLQUFLVixtQkFFUDNGLFdBQVcyRSxXQUFZMUUsRUFBVzZFLE9BQU81RSxpQkFBa0I0RSxPQUFPM0UsYUFBYzJFLE9BQU8xRSxXQUFZc0UsR0FBR0csS0FBS3BCLFFBQVFwRCxHQUFJQyxRQU1wSGIsV0FBV3NHLGtCQUE0RCxtQkFBakN0RyxXQUEyQixrQkFDaEVBLFdBQVdzRyxpQkFBaUJYLG1CQXpEaEM5RixnQkFBZ0JxRixXQUFZbkYsTUFBT0MsV0FBVyxTQWlFdkRrQixNQUlYekIsRUFBRUMsR0FBR3lFLFVBQVV4RSxjQUFnQkYsRUFBRUMsR0FBR0MsY0FDcENGLEVBQUVDLEdBQUdtSCxVQUFVbEgsY0FBZ0JGLEVBQUVDLEdBQUdDLGNBRy9CRixFQUFFQyxHQUFHeUUsVUFBVTJDLE1BQ05ySCxFQUFFQyxHQUFHeUUsVUFBVTJDLElBQ3JCQyxTQUFVLGtCQUFtQnRILEVBQUVDLEdBQUdDLGVBdlM5QyxDQXlTR29EIiwiZmlsZSI6InBsdWdpbnMvZGF0YVRhYmxlcy9qcXVlcnkuZGF0YVRhYmxlcy5yb3dSZW9yZGVyaW5nLmpzIiwic291cmNlc0NvbnRlbnQiOlsiLyohXG4gKiBGaWxlOiAgICAgICAganF1ZXJ5LmRhdGFUYWJsZXMucm93UmVvcmRlcmluZy5qc1xuICogVmVyc2lvbjogICAgIDEuMi4zIC8gRGF0YXRhYmxlcyAxLjEwIGhhY2tcbiAqIEF1dGhvcjogICAgICBKb3ZhbiBQb3BvdmljXG4gKlxuICogQ29weXJpZ2h0IDIwMTMgSm92YW4gUG9wb3ZpYywgYWxsIHJpZ2h0cyByZXNlcnZlZC5cbiAqXG4gKiBUaGlzIHNvdXJjZSBmaWxlIGlzIGZyZWUgc29mdHdhcmUsIHVuZGVyIGVpdGhlciB0aGUgR1BMIHYyIGxpY2Vuc2Ugb3IgYVxuICogQlNEIHN0eWxlIGxpY2Vuc2UsIGFzIHN1cHBsaWVkIHdpdGggdGhpcyBzb2Z0d2FyZS5cbiAqL1xuLypcbiAqIE5PVEVTOlxuICpcbiAqIFRoaXMgc291cmNlIGZpbGUgaXMgZGlzdHJpYnV0ZWQgaW4gdGhlIGhvcGUgdGhhdCBpdCB3aWxsIGJlIHVzZWZ1bCwgYnV0XG4gKiBXSVRIT1VUIEFOWSBXQVJSQU5UWTsgd2l0aG91dCBldmVuIHRoZSBpbXBsaWVkIHdhcnJhbnR5IG9mIE1FUkNIQU5UQUJJTElUWVxuICogb3IgRklUTkVTUyBGT1IgQSBQQVJUSUNVTEFSIFBVUlBPU0UuXG4gKlxuICpcbiAqIE1vZGlmaWVkIGJ5IEplcmVtaWUgTGVncmFuZCAoS09NT1JJLUNIQU1CT04gU0FTKTpcbiAqICAtIGZhc3QgYW5kIHVnbHkgbW9kaWZpY2F0aW9uIGZvciBEYXRhdGFibGVzIDEuMTAgY29tcGF0aWJpbGl0eSAoYXQgbGVhc3QsIG1vdmUgcm93cyBldmVuIGlmIGl0J3MgcmVhbGx5IHNsb3cuLi4pXG4gKiAgLSBjYW4gcHJldmVudCBkYXRhdGFibGUgdG8gYWN0dWFsbHkgRE8gdGhlIHJlb3JkZXJpbmcgYXQgdGhlIGVuZCAoYWZ0ZXIgdGhlIHN1Y2Nlc3MgYWpheCBjYWxsKSwgZm9yIGV4YW1wbGVcbiAqICAgICAgaWYgd2Ugd2FudCB0byByZWxvYWQgdGhlIHdob2xlIHRhYmxlIGluIGFqYXgganVzdCBhZnRlcjpcbiAqICAgICAgICAgICAgICBvVGFibGUucm93UmVvcmRlcmluZyh7XG4gKiAgICAgICAgICAgICAgICAgICAgICBzVVJMOiAnVXBkYXRlUm93T3JkZXIucGhwJyxcbiAqICAgICAgICAgICAgICAgICAgICAgIGF2b2lkTW92aW5nUm93czogdHJ1ZVxuICogICAgICAgICAgICAgIH0pO1xuICogIC0gY2FuIGNhbGwgYSBmblVwZGF0ZUNhbGxiYWNrKCkgZnVuY3Rpb24gd2hlbiBkcm9wIGlzIGZpbmlzaGVkXG4gKiAgICAgIChJbnRlZ3JhdGlvbiBvZiBhIGZyZWUgY29tbWVudCBpbjogaHR0cHM6Ly9jb2RlLmdvb2dsZS5jb20vcC9qcXVlcnktZGF0YXRhYmxlcy1yb3ctcmVvcmRlcmluZy93aWtpL0luZGV4KVxuICogICAgICAgICAgICAgIEF1dGhvcjogXCJDb21tZW50IGJ5IHJhLi4uQHdlYnJ1bi5jYSwgTWFyIDE2LCAyMDEzXCJcbiAqICAtIGNhbiBwYXNzIGFkZGl0aW9uYWwgZGF0YSBpbiBQT1NULCBsaWtlIHRoaXM6XG4gKiAgICAgICAgICAgICAgb1RhYmxlLnJvd1Jlb3JkZXJpbmcoe1xuICogICAgICAgICAgICAgICAgICAgICAgIHNVUkw6ICdVcGRhdGVSb3dPcmRlci5waHAnLFxuICogICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzRGF0YTogeyd2YXJfbmFtZSc6ICdiaWcgY29udGVudCBoZXJlJ31cbiAqICAgICAgICAgICAgICAgICAgICAgIH0pO1xuICogLSBvbiBBamF4IGVycm9yIHJldHVybiBjb2RlLCBnaXZlIHRvIGZuRXJyb3IgdGhlIHJlc3BvbnNlIHRleHQgaW5zdGVhZCBvZiB4aHIuc3RhdHVzVGV4dCwgaWYgYW55LFxuICogLSBGSVggYSBjcmFzaCB3aGVuICd0cicgaW4gJ3Rib2R5JyBkaWRuJ3QgaGF2ZSBhbiBJRCAodGhlIGZ1bmN0aW9uIGZuR2V0U3RhdGUoKSBtYWRlXG4gKiAgICAgICAgICAgICAgYSAkKFwiI1wiICsgaWQsIG9UYWJsZSkgdG8gZ2V0IHRoZSAndHInIGluc3RlYWQgb2YganVzdCBnZXQgaXQgZnJvbSB0aGUgY29udGV4dClcbiAqIC1cbiAqXG4gKiBQYXJhbWV0ZXJzOlxuICogQGlJbmRleENvbHVtbiAgICAgaW50ICAgICAgICAgUG9zaXRpb24gb2YgdGhlIGluZGV4aW5nIGNvbHVtblxuICogQHNVUkwgICAgICAgICAgICAgU3RyaW5nICAgICAgU2VydmVyIHNpZGUgcGFnZSB0YXQgd2lsbCBiZSBub3RpZmllZCB0aGF0IG9yZGVyIGlzIGNoYW5nZWRcbiAqIEBpR3JvdXBpbmdMZXZlbCAgIGludCAgICAgICAgIERlZmluZXMgdGhhdCBncm91cGluZyBpcyB1c2VkXG4gKi9cbihmdW5jdGlvbiAoJCkge1xuXG4gICAgXCJ1c2Ugc3RyaWN0XCI7XG4gICAgJC5mbi5yb3dSZW9yZGVyaW5nID0gZnVuY3Rpb24gKG9wdGlvbnMpIHtcblxuICAgICAgICBmdW5jdGlvbiBfZm5TdGFydFByb2Nlc3NpbmdNb2RlKG9UYWJsZSkge1xuICAgICAgICAgICAgLy8vPHN1bW1hcnk+XG4gICAgICAgICAgICAvLy9GdW5jdGlvbiB0aGF0IHN0YXJ0cyBcIlByb2Nlc3NpbmdcIiBtb2RlIGkuZS4gc2hvd3MgXCJQcm9jZXNzaW5nLi4uXCIgZGlhbG9nIHdoaWxlIHNvbWUgYWN0aW9uIGlzIGV4ZWN1dGluZyhEZWZhdWx0IGZ1bmN0aW9uKVxuICAgICAgICAgICAgLy8vPC9zdW1tYXJ5PlxuXG4gICAgICAgICAgICBpZiAob1RhYmxlLmZuU2V0dGluZ3MoKS5vRmVhdHVyZXMuYlByb2Nlc3NpbmcpIHtcbiAgICAgICAgICAgICAgICAkKFwiLmRhdGFUYWJsZXNfcHJvY2Vzc2luZ1wiKS5jc3MoJ3Zpc2liaWxpdHknLCAndmlzaWJsZScpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9XG5cbiAgICAgICAgZnVuY3Rpb24gX2ZuRW5kUHJvY2Vzc2luZ01vZGUob1RhYmxlKSB7XG4gICAgICAgICAgICAvLy88c3VtbWFyeT5cbiAgICAgICAgICAgIC8vL0Z1bmN0aW9uIHRoYXQgZW5kcyB0aGUgXCJQcm9jZXNzaW5nXCIgbW9kZSBhbmQgcmV0dXJucyB0aGUgdGFibGUgaW4gdGhlIG5vcm1hbCBzdGF0ZShEZWZhdWx0IGZ1bmN0aW9uKVxuICAgICAgICAgICAgLy8vPC9zdW1tYXJ5PlxuXG4gICAgICAgICAgICBpZiAob1RhYmxlLmZuU2V0dGluZ3MoKS5vRmVhdHVyZXMuYlByb2Nlc3NpbmcpIHtcbiAgICAgICAgICAgICAgICAkKFwiLmRhdGFUYWJsZXNfcHJvY2Vzc2luZ1wiKS5jc3MoJ3Zpc2liaWxpdHknLCAnaGlkZGVuJyk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cblxuICAgICAgICAvLy9Ob3QgdXNlZFxuICAgICAgICBmdW5jdGlvbiBmbkdldFN0YXJ0UG9zaXRpb24ob1RhYmxlLCBzU2VsZWN0b3IpIHtcbiAgICAgICAgICAgIHZhciBpU3RhcnQgPSAxMDAwMDAwO1xuICAgICAgICAgICAgJChzU2VsZWN0b3IsIG9UYWJsZSkuZWFjaChmdW5jdGlvbiAoKSB7XG4gICAgICAgICAgICAgICAgdmFyIGlQb3NpdGlvbiA9IHBhcnNlSW50KG9UYWJsZS5mbkdldERhdGEodGhpcywgcHJvcGVydGllcy5pSW5kZXhDb2x1bW4pLCAxMCk7XG4gICAgICAgICAgICAgICAgaWYgKGlQb3NpdGlvbiA8IGlTdGFydClcbiAgICAgICAgICAgICAgICAgICAgaVN0YXJ0ID0gaVBvc2l0aW9uO1xuICAgICAgICAgICAgfSk7XG4gICAgICAgICAgICByZXR1cm4gaVN0YXJ0O1xuICAgICAgICB9XG5cbiAgICAgICAgZnVuY3Rpb24gZm5DYW5jZWxTb3J0aW5nKG9UYWJsZSwgdGJvZHksIHByb3BlcnRpZXMsIGlMb2dMZXZlbCwgc01lc3NhZ2UpIHtcbiAgICAgICAgICAgIHRib2R5LnNvcnRhYmxlKCdjYW5jZWwnKTtcbiAgICAgICAgICAgIGlmKGlMb2dMZXZlbDw9cHJvcGVydGllcy5pTG9nTGV2ZWwpe1xuICAgICAgICAgICAgICAgIGlmKHNNZXNzYWdlIT0gdW5kZWZpbmVkKXtcbiAgICAgICAgICAgICAgICAgICAgcHJvcGVydGllcy5mbkFsZXJ0KHNNZXNzYWdlLCBcIlwiKTtcbiAgICAgICAgICAgICAgICB9ZWxzZXtcbiAgICAgICAgICAgICAgICAgICAgcHJvcGVydGllcy5mbkFsZXJ0KFwiUm93IGNhbm5vdCBiZSBtb3ZlZFwiLCBcIlwiKTtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgICAgICBwcm9wZXJ0aWVzLmZuRW5kUHJvY2Vzc2luZ01vZGUob1RhYmxlKTtcbiAgICAgICAgfVxuXG4gICAgICAgIC8vICMjIyBLQ00gIyMjIEdldCAkKCd0cicpIGluc3RlYWQgb2YgJ3RyIGlkJywgdG8gYXZvaWQgcmUtbWFwcGluZyBpbiBqUXVlcnkgb2JqZWN0IGZyb20gaXQncyBpZCAod2hpY2ggY2FuIGJlIG51bGwpXG4gICAgICAgIC8vICNmdW5jdGlvbiBmbkdldFN0YXRlKG9UYWJsZSwgc1NlbGVjdG9yLCBpZCkge1xuICAgICAgICBmdW5jdGlvbiBmbkdldFN0YXRlKG9UYWJsZSwgc1NlbGVjdG9yLCB0cikge1xuICAgICAgICAgICAgLy92YXIgdHIgPSAkKFwiI1wiICsgaWQsIG9UYWJsZSk7XG4gICAgICAgICAgICAvLyAjIyMgRU5EICMjI1xuICAgICAgICAgICAgdmFyIGlDdXJyZW50UG9zaXRpb24gPSBwYXJzZUludChvVGFibGUuZm5HZXREYXRhKHRyWzBdLCBwcm9wZXJ0aWVzLmlJbmRleENvbHVtbiksIDEwKTtcbiAgICAgICAgICAgIHZhciBpTmV3UG9zaXRpb24gPSAtMTsgLy8gZm5HZXRTdGFydFBvc2l0aW9uKHNTZWxlY3Rvcik7XG4gICAgICAgICAgICB2YXIgc0RpcmVjdGlvbjtcbiAgICAgICAgICAgIHZhciB0clByZXZpb3VzID0gdHIucHJldihzU2VsZWN0b3IpO1xuICAgICAgICAgICAgaWYgKHRyUHJldmlvdXMubGVuZ3RoID4gMCkge1xuICAgICAgICAgICAgICAgIGlOZXdQb3NpdGlvbiA9IHBhcnNlSW50KG9UYWJsZS5mbkdldERhdGEodHJQcmV2aW91c1swXSwgcHJvcGVydGllcy5pSW5kZXhDb2x1bW4pLCAxMCk7XG4gICAgICAgICAgICAgICAgaWYgKGlOZXdQb3NpdGlvbiA8IGlDdXJyZW50UG9zaXRpb24pIHtcbiAgICAgICAgICAgICAgICAgICAgaU5ld1Bvc2l0aW9uID0gaU5ld1Bvc2l0aW9uICsgMTtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgICAgIHZhciB0ck5leHQgPSB0ci5uZXh0KHNTZWxlY3Rvcik7XG4gICAgICAgICAgICAgICAgaWYgKHRyTmV4dC5sZW5ndGggPiAwKSB7XG4gICAgICAgICAgICAgICAgICAgIGlOZXdQb3NpdGlvbiA9IHBhcnNlSW50KG9UYWJsZS5mbkdldERhdGEodHJOZXh0WzBdLCBwcm9wZXJ0aWVzLmlJbmRleENvbHVtbiksIDEwKTtcbiAgICAgICAgICAgICAgICAgICAgaWYgKGlOZXdQb3NpdGlvbiA+IGlDdXJyZW50UG9zaXRpb24pLy9tb3ZlZCBiYWNrXG4gICAgICAgICAgICAgICAgICAgICAgICBpTmV3UG9zaXRpb24gPSBpTmV3UG9zaXRpb24gLSAxO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgICAgIGlmIChpTmV3UG9zaXRpb24gPCBpQ3VycmVudFBvc2l0aW9uKVxuICAgICAgICAgICAgICAgIHNEaXJlY3Rpb24gPSBcImJhY2tcIjtcbiAgICAgICAgICAgIGVsc2VcbiAgICAgICAgICAgICAgICBzRGlyZWN0aW9uID0gXCJmb3J3YXJkXCI7XG5cbiAgICAgICAgICAgIHJldHVybiB7IHNEaXJlY3Rpb246IHNEaXJlY3Rpb24sIGlDdXJyZW50UG9zaXRpb246IGlDdXJyZW50UG9zaXRpb24sIGlOZXdQb3NpdGlvbjogaU5ld1Bvc2l0aW9uIH07XG5cbiAgICAgICAgfVxuXG4gICAgICAgIGZ1bmN0aW9uIGZuTW92ZVJvd3Mob1RhYmxlLCBzU2VsZWN0b3IsIGlDdXJyZW50UG9zaXRpb24sIGlOZXdQb3NpdGlvbiwgc0RpcmVjdGlvbiwgaWQsIHNHcm91cCkge1xuICAgICAgICAgICAgdmFyIGlTdGFydCA9IGlDdXJyZW50UG9zaXRpb247XG4gICAgICAgICAgICB2YXIgaUVuZCA9IGlOZXdQb3NpdGlvbjtcbiAgICAgICAgICAgIGlmIChzRGlyZWN0aW9uID09IFwiYmFja1wiKSB7XG4gICAgICAgICAgICAgICAgaVN0YXJ0ID0gaU5ld1Bvc2l0aW9uO1xuICAgICAgICAgICAgICAgIGlFbmQgPSBpQ3VycmVudFBvc2l0aW9uO1xuICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAkKG9UYWJsZS5mbkdldE5vZGVzKCkpLmVhY2goZnVuY3Rpb24gKCkge1xuICAgICAgICAgICAgICAgIGlmIChzR3JvdXAgIT0gXCJcIiAmJiAkKHRoaXMpLmF0dHIoXCJkYXRhLWdyb3VwXCIpICE9IHNHcm91cClcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICAgICAgICAgIHZhciB0ciA9IHRoaXM7XG4gICAgICAgICAgICAgICAgdmFyIGlSb3dQb3NpdGlvbiA9IHBhcnNlSW50KG9UYWJsZS5mbkdldERhdGEodHIsIHByb3BlcnRpZXMuaUluZGV4Q29sdW1uKSwgMTApO1xuICAgICAgICAgICAgICAgIGlmIChpU3RhcnQgPD0gaVJvd1Bvc2l0aW9uICYmIGlSb3dQb3NpdGlvbiA8PSBpRW5kKSB7XG4gICAgICAgICAgICAgICAgICAgIGlmICh0ci5pZCA9PSBpZCkge1xuICAgICAgICAgICAgICAgICAgICAgICAgb1RhYmxlLmZuVXBkYXRlKGlOZXdQb3NpdGlvbixcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBvVGFibGUuZm5HZXRQb3NpdGlvbih0ciksIC8vIGdldCByb3cgcG9zaXRpb24gaW4gY3VycmVudCBtb2RlbFxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHByb3BlcnRpZXMuaUluZGV4Q29sdW1uLFxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGZhbHNlKTsgLy8gZmFsc2UgPSBkZWZlciByZWRyYXcgdW50aWwgYWxsIHJvdyB1cGRhdGVzIGFyZSBkb25lXG4gICAgICAgICAgICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICAgICAgICAgICAgICBpZiAoc0RpcmVjdGlvbiA9PSBcImJhY2tcIikge1xuICAgICAgICAgICAgICAgICAgICAgICAgICAgIG9UYWJsZS5mblVwZGF0ZShpUm93UG9zaXRpb24gKyAxLFxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBvVGFibGUuZm5HZXRQb3NpdGlvbih0ciksIC8vIGdldCByb3cgcG9zaXRpb24gaW4gY3VycmVudCBtb2RlbFxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBwcm9wZXJ0aWVzLmlJbmRleENvbHVtbixcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZmFsc2UpOyAvLyBmYWxzZSA9IGRlZmVyIHJlZHJhdyB1bnRpbCBhbGwgcm93IHVwZGF0ZXMgYXJlIGRvbmVcbiAgICAgICAgICAgICAgICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgb1RhYmxlLmZuVXBkYXRlKGlSb3dQb3NpdGlvbiAtIDEsXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG9UYWJsZS5mbkdldFBvc2l0aW9uKHRyKSwgLy8gZ2V0IHJvdyBwb3NpdGlvbiBpbiBjdXJyZW50IG1vZGVsXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHByb3BlcnRpZXMuaUluZGV4Q29sdW1uLFxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBmYWxzZSk7IC8vIGZhbHNlID0gZGVmZXIgcmVkcmF3IHVudGlsIGFsbCByb3cgdXBkYXRlcyBhcmUgZG9uZVxuICAgICAgICAgICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfSk7XG5cbiAgICAgICAgICAgIHZhciBvU2V0dGluZ3MgPSBvVGFibGUuZm5TZXR0aW5ncygpO1xuXG4gICAgICAgICAgICAvL1N0YW5kaW5nIFJlZHJhdyBFeHRlbnNpb25cbiAgICAgICAgICAgIC8vQXV0aG9yOiAgIEpvbmF0aGFuIEhvZ3VldFxuICAgICAgICAgICAgLy9odHRwOi8vZGF0YXRhYmxlcy5uZXQvcGx1Zy1pbnMvYXBpI2ZuU3RhbmRpbmdSZWRyYXdcbiAgICAgICAgICAgIGlmIChvU2V0dGluZ3Mub0ZlYXR1cmVzLmJTZXJ2ZXJTaWRlID09PSBmYWxzZSkge1xuICAgICAgICAgICAgICAgIHZhciBiZWZvcmUgPSBvU2V0dGluZ3MuX2lEaXNwbGF5U3RhcnQ7XG4gICAgICAgICAgICAgICAgb1NldHRpbmdzLm9BcGkuX2ZuUmVEcmF3KG9TZXR0aW5ncyk7XG4gICAgICAgICAgICAgICAgLy9pRGlzcGxheVN0YXJ0IGhhcyBiZWVuIHJlc2V0IHRvIHplcm8gLSBzbyBsZXRzIGNoYW5nZSBpdCBiYWNrXG4gICAgICAgICAgICAgICAgb1NldHRpbmdzLl9pRGlzcGxheVN0YXJ0ID0gYmVmb3JlO1xuICAgICAgICAgICAgICAgIG9TZXR0aW5ncy5vQXBpLl9mbkNhbGN1bGF0ZUVuZChvU2V0dGluZ3MpO1xuICAgICAgICAgICAgfVxuICAgICAgICAgICAgLy9kcmF3IHRoZSAnY3VycmVudCcgcGFnZVxuICAgICAgICAgICAgb1NldHRpbmdzLm9BcGkuX2ZuRHJhdyhvU2V0dGluZ3MpO1xuICAgICAgICB9XG5cbiAgICAgICAgZnVuY3Rpb24gX2ZuQWxlcnQobWVzc2FnZSwgdHlwZSkgeyBhbGVydChtZXNzYWdlKTsgfVxuXG4gICAgICAgIHZhciBkZWZhdWx0cyA9IHtcbiAgICAgICAgICAgIGlJbmRleENvbHVtbjogMCxcbiAgICAgICAgICAgIGlTdGFydFBvc2l0aW9uOiAxLFxuICAgICAgICAgICAgc1VSTDogbnVsbCxcbiAgICAgICAgICAgIHNSZXF1ZXN0VHlwZTogXCJQT1NUXCIsXG4gICAgICAgICAgICBpR3JvdXBpbmdMZXZlbDogMCxcbiAgICAgICAgICAgIGZuQWxlcnQ6IF9mbkFsZXJ0LFxuICAgICAgICAgICAgZm5TdWNjZXNzOiBqUXVlcnkubm9vcCxcbiAgICAgICAgICAgIGlMb2dMZXZlbDogMSxcbiAgICAgICAgICAgIHNEYXRhR3JvdXBBdHRyaWJ1dGU6IFwiZGF0YS1ncm91cFwiLFxuICAgICAgICAgICAgZm5TdGFydFByb2Nlc3NpbmdNb2RlOiBfZm5TdGFydFByb2Nlc3NpbmdNb2RlLFxuICAgICAgICAgICAgZm5FbmRQcm9jZXNzaW5nTW9kZTogX2ZuRW5kUHJvY2Vzc2luZ01vZGUsXG4gICAgICAgICAgICBmblVwZGF0ZUFqYXhSZXF1ZXN0OiBqUXVlcnkubm9vcFxuICAgICAgICB9O1xuXG4gICAgICAgIHZhciBwcm9wZXJ0aWVzID0gJC5leHRlbmQoZGVmYXVsdHMsIG9wdGlvbnMpO1xuXG4gICAgICAgIHZhciBpRnJvbSwgaVRvO1xuXG4gICAgICAgIC8vIFJldHVybiBhIGhlbHBlciB3aXRoIHByZXNlcnZlZCB3aWR0aCBvZiBjZWxscyAoc2VlIElzc3VlIDkpXG4gICAgICAgIHZhciB0YWJsZUZpeEhlbHBlciA9IGZ1bmN0aW9uKGUsIHRyKVxuICAgICAgICB7XG4gICAgICAgICAgICB2YXIgJG9yaWdpbmFscyA9IHRyLmNoaWxkcmVuKCk7XG4gICAgICAgICAgICB2YXIgJGhlbHBlciA9IHRyLmNsb25lKCk7XG4gICAgICAgICAgICAkaGVscGVyLmNoaWxkcmVuKCkuZWFjaChmdW5jdGlvbihpbmRleCl7XG4gICAgICAgICAgICAgICAgLy8gU2V0IGhlbHBlciBjZWxsIHNpemVzIHRvIG1hdGNoIHRoZSBvcmlnaW5hbCBzaXplc1xuICAgICAgICAgICAgICAgICQodGhpcykud2lkdGgoJG9yaWdpbmFscy5lcShpbmRleCkud2lkdGgoKSk7XG4gICAgICAgICAgICB9KTtcbiAgICAgICAgICAgIHJldHVybiAkaGVscGVyO1xuICAgICAgICB9O1xuXG4gICAgICAgIC8vICMjIyBLQ00gIyMjIFVnbHkgYW5kIGZhc3QgbWV0aG9kIHRvIGdldCBkYXRhVGFibGUgb2JqZWN0XG4gICAgICAgIHZhciB0YWJsZXM7XG4gICAgICAgIGlmKHRoaXMgaW5zdGFuY2VvZiBqUXVlcnkpe1xuICAgICAgICAgICAgdGFibGVzID0gdGhpcztcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIHRhYmxlcyA9IHRoaXMuY29udGV4dDtcbiAgICAgICAgfVxuXG4gICAgICAgICQuZWFjaCh0YWJsZXMsIGZ1bmN0aW9uICgpIHtcbiAgICAgICAgICAgIHZhciBvVGFibGU7XG5cbiAgICAgICAgICAgIGlmICh0eXBlb2YgdGhpcy5ub2RlVHlwZSAhPT0gJ3VuZGVmaW5lZCcpe1xuICAgICAgICAgICAgICAgIG9UYWJsZSA9ICQodGhpcykuZGF0YVRhYmxlKCk7XG4gICAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgICAgIG9UYWJsZSA9ICQodGhpcy5uVGFibGUpLmRhdGFUYWJsZSgpO1xuICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICB2YXIgYWFTb3J0aW5nRml4ZWQgPSAob1RhYmxlLmZuU2V0dGluZ3MoKS5hYVNvcnRpbmdGaXhlZCA9PSBudWxsID8gbmV3IEFycmF5KCkgOiBvVGFibGUuZm5TZXR0aW5ncygpLmFhU29ydGluZ0ZpeGVkKTtcbiAgICAgICAgICAgIGFhU29ydGluZ0ZpeGVkLnB1c2goW3Byb3BlcnRpZXMuaUluZGV4Q29sdW1uLCBcImFzY1wiXSk7XG5cbiAgICAgICAgICAgIG9UYWJsZS5mblNldHRpbmdzKCkuYWFTb3J0aW5nRml4ZWQgPSBhYVNvcnRpbmdGaXhlZDtcblxuXG4gICAgICAgICAgICBmb3IgKHZhciBpID0gMDsgaSA8IG9UYWJsZS5mblNldHRpbmdzKCkuYW9Db2x1bW5zLmxlbmd0aDsgaSsrKSB7XG4gICAgICAgICAgICAgICAgb1RhYmxlLmZuU2V0dGluZ3MoKS5hb0NvbHVtbnNbaV0uYlNvcnRhYmxlID0gZmFsc2U7XG4gICAgICAgICAgICAgICAgLypmb3IodmFyIGo9MDsgajxhYVNvcnRpbmdGaXhlZC5sZW5ndGg7IGorKylcbiAgICAgICAgICAgICAgICAge1xuICAgICAgICAgICAgICAgICBpZiggaSA9PSBhYVNvcnRpbmdGaXhlZFtqXVswXSApXG4gICAgICAgICAgICAgICAgIHNldHRpbmdzLmFvQ29sdW1uc1tpXS5iU29ydGFibGUgPSBmYWxzZTtcbiAgICAgICAgICAgICAgICAgfSovXG4gICAgICAgICAgICB9XG4gICAgICAgICAgICBvVGFibGUuZm5EcmF3KCk7XG5cbiAgICAgICAgICAgICQoXCJ0Ym9keVwiLCBvVGFibGUpLmRpc2FibGVTZWxlY3Rpb24oKS5zb3J0YWJsZSh7XG4gICAgICAgICAgICAgICAgY3Vyc29yOiBcIm1vdmVcIixcbiAgICAgICAgICAgICAgICBoZWxwZXI6IHRhYmxlRml4SGVscGVyLFxuICAgICAgICAgICAgICAgIHVwZGF0ZTogZnVuY3Rpb24gKGV2ZW50LCB1aSkge1xuICAgICAgICAgICAgICAgICAgICB2YXIgJGRhdGFUYWJsZSA9IG9UYWJsZTtcbiAgICAgICAgICAgICAgICAgICAgdmFyIHRib2R5ID0gJCh0aGlzKTtcbiAgICAgICAgICAgICAgICAgICAgdmFyIHNTZWxlY3RvciA9IFwidGJvZHkgdHJcIjtcbiAgICAgICAgICAgICAgICAgICAgdmFyIHNHcm91cCA9IFwiXCI7XG4gICAgICAgICAgICAgICAgICAgIGlmIChwcm9wZXJ0aWVzLmJHcm91cGluZ1VzZWQpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgIHNHcm91cCA9ICQodWkuaXRlbSkuYXR0cihwcm9wZXJ0aWVzLnNEYXRhR3JvdXBBdHRyaWJ1dGUpO1xuICAgICAgICAgICAgICAgICAgICAgICAgaWYoc0dyb3VwPT1udWxsIHx8IHNHcm91cD09dW5kZWZpbmVkKXtcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBmbkNhbmNlbFNvcnRpbmcoJGRhdGFUYWJsZSwgdGJvZHksIHByb3BlcnRpZXMsIDMsIFwiR3JvdXBpbmcgcm93IGNhbm5vdCBiZSBtb3ZlZFwiKTtcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm47XG4gICAgICAgICAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICAgICAgICAgICAgICBzU2VsZWN0b3IgPSBcInRib2R5IHRyW1wiICsgcHJvcGVydGllcy5zRGF0YUdyb3VwQXR0cmlidXRlICsgXCIgPSdcIiArIHNHcm91cCArIFwiJ11cIjtcbiAgICAgICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgICAgIC8vICMjIyBLQ00gIyMjXG4gICAgICAgICAgICAgICAgICAgIC8vICAgICAgcGFzcyAndHInIGRpcmVjdGx5LCBpbnN0ZWFkIG9mIGdpdmluZyBpZCB0aGVuIHJlZG8gYSAkKCcjJyArIGlkKSBpbiB0aGUgZnVuY3Rpb24uLi5cbiAgICAgICAgICAgICAgICAgICAgLy8gI3ZhciBvU3RhdGUgPSBmbkdldFN0YXRlKCRkYXRhVGFibGUsIHNTZWxlY3RvciwgdWkuaXRlbS5jb250ZXh0LmlkKTtcbiAgICAgICAgICAgICAgICAgICAgdmFyIHRyID0gJCggdWkuaXRlbS5jb250ZXh0ICk7XG4gICAgICAgICAgICAgICAgICAgIHZhciBvU3RhdGUgPSBmbkdldFN0YXRlKCRkYXRhVGFibGUsIHNTZWxlY3RvciwgdHIpO1xuICAgICAgICAgICAgICAgICAgICAvLy8gIyMjIEVORCAjIyNcbiAgICAgICAgICAgICAgICAgICAgaWYob1N0YXRlLmlOZXdQb3NpdGlvbiA9PSAtMSkge1xuICAgICAgICAgICAgICAgICAgICAgICAgZm5DYW5jZWxTb3J0aW5nKCRkYXRhVGFibGUsIHRib2R5LCBwcm9wZXJ0aWVzLDIpO1xuICAgICAgICAgICAgICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICAgICAgdmFyIHNSZXF1ZXN0RGF0YSA9IHtcbiAgICAgICAgICAgICAgICAgICAgICAgIGlkOiB1aS5pdGVtLmNvbnRleHQuaWQsXG4gICAgICAgICAgICAgICAgICAgICAgICBmcm9tUG9zaXRpb246IG9TdGF0ZS5pQ3VycmVudFBvc2l0aW9uLFxuICAgICAgICAgICAgICAgICAgICAgICAgdG9Qb3NpdGlvbjogb1N0YXRlLmlOZXdQb3NpdGlvbixcbiAgICAgICAgICAgICAgICAgICAgICAgIGRpcmVjdGlvbjogb1N0YXRlLnNEaXJlY3Rpb24sXG4gICAgICAgICAgICAgICAgICAgICAgICBncm91cDogc0dyb3VwLFxuICAgICAgICAgICAgICAgICAgICAgICAgLy8gIyMjIEtDTSAjIyMgQ2FuIHBhc3MgYWRkaXRpb25hbCBkYXRhIGluIFBPU1RcbiAgICAgICAgICAgICAgICAgICAgICAgIGRhdGE6IHByb3BlcnRpZXMuc0RhdGFcbiAgICAgICAgICAgICAgICAgICAgICAgIC8vICMjIyBFTkQgIyMjXG4gICAgICAgICAgICAgICAgICAgIH07XG5cbiAgICAgICAgICAgICAgICAgICAgaWYgKHByb3BlcnRpZXMuc1VSTCAhPSBudWxsKSB7XG4gICAgICAgICAgICAgICAgICAgICAgICBwcm9wZXJ0aWVzLmZuU3RhcnRQcm9jZXNzaW5nTW9kZSgkZGF0YVRhYmxlKTtcbiAgICAgICAgICAgICAgICAgICAgICAgIHZhciBvQWpheFJlcXVlc3QgPSB7XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdXJsOiBwcm9wZXJ0aWVzLnNVUkwsXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdHlwZTogcHJvcGVydGllcy5zUmVxdWVzdFR5cGUsXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZGF0YTogc1JlcXVlc3REYXRhLFxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHN1Y2Nlc3M6IGZ1bmN0aW9uIChkYXRhKSB7XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHByb3BlcnRpZXMuZm5TdWNjZXNzKGRhdGEpO1xuXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIC8vICMjI0tDTSMjIyBDYW4gYXZvaWQgbW92aW5nIHJvd3MgaWYgd2Ugd2FudCAoZm9yIGV4YW1wbGUgaWYgd2UgcmVsb2FkIGFsbCB0aGUgdGFibGUgaW4gYWpheCBqdXN0ZSBhZnRlcilcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWYoISBwcm9wZXJ0aWVzLmF2b2lkTW92aW5nUm93cylcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGZuTW92ZVJvd3MoJGRhdGFUYWJsZSwgc1NlbGVjdG9yLCBvU3RhdGUuaUN1cnJlbnRQb3NpdGlvbiwgb1N0YXRlLmlOZXdQb3NpdGlvbiwgb1N0YXRlLnNEaXJlY3Rpb24sIHVpLml0ZW0uY29udGV4dC5pZCwgc0dyb3VwKTtcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgLy8gIyMjIEVORCAjIyNcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcHJvcGVydGllcy5mbkVuZFByb2Nlc3NpbmdNb2RlKCRkYXRhVGFibGUpO1xuXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIC8vICMjI0tDTSMjIyBDYW4gaGF2ZSBhIGNhbGxiYWNrIHdoZW4gZHJvcCBpcyBmaW5pc2hlZFxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAvLyBTb3VyY2U6XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIC8vICAgICAgaHR0cHM6Ly9jb2RlLmdvb2dsZS5jb20vcC9qcXVlcnktZGF0YXRhYmxlcy1yb3ctcmVvcmRlcmluZy93aWtpL0luZGV4LFxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAvLyAgICAgIC0tPiBGcmVlIGNvbW1lbnQgb2YgXCJDb21tZW50IGJ5IHJhLi4uQHdlYnJ1bi5jYSwgTWFyIDE2LCAyMDEzXCJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWYocHJvcGVydGllcy5mblVwZGF0ZUNhbGxiYWNrICYmIHR5cGVvZihwcm9wZXJ0aWVzLmZuVXBkYXRlQ2FsbGJhY2spID09PSAnZnVuY3Rpb24nKXtcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHByb3BlcnRpZXMuZm5VcGRhdGVDYWxsYmFjayhzUmVxdWVzdERhdGEpO1xuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIC8vICMjI0VORCMjI1xuICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZXJyb3I6IGZ1bmN0aW9uIChqcVhIUikge1xuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAvLyMjIyBLQ00gIyMjIEdldCByZXNwb25zZSB0ZXh0IGluc3RlYWQgb2Ygc3RhdHVzVGV4dCBpZiBhbnlcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgLy8gI2ZuQ2FuY2VsU29ydGluZygkZGF0YVRhYmxlLCB0Ym9keSwgcHJvcGVydGllcywgMSwganFYSFIuc3RhdHVzVGV4dCk7XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHZhciBlcnIgPSAoanFYSFIucmVzcG9uc2VUZXh0ICE9IFwiXCIgPyBqcVhIUi5yZXNwb25zZVRleHQgOiBqcVhIUi5zdGF0dXNUZXh0KTtcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZm5DYW5jZWxTb3J0aW5nKCRkYXRhVGFibGUsIHRib2R5LCBwcm9wZXJ0aWVzLCAxLCBlcnIpO1xuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAvLyAjIyMgRU5EICMjI1xuICAgICAgICAgICAgICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICAgICAgICAgIH07XG4gICAgICAgICAgICAgICAgICAgICAgICBwcm9wZXJ0aWVzLmZuVXBkYXRlQWpheFJlcXVlc3Qob0FqYXhSZXF1ZXN0LCBwcm9wZXJ0aWVzLCAkZGF0YVRhYmxlKTtcbiAgICAgICAgICAgICAgICAgICAgICAgICQuYWpheChvQWpheFJlcXVlc3QpO1xuICAgICAgICAgICAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgICAgICAgICAgICAgZm5Nb3ZlUm93cygkZGF0YVRhYmxlLCBzU2VsZWN0b3IsIG9TdGF0ZS5pQ3VycmVudFBvc2l0aW9uLCBvU3RhdGUuaU5ld1Bvc2l0aW9uLCBvU3RhdGUuc0RpcmVjdGlvbiwgdWkuaXRlbS5jb250ZXh0LmlkLCBzR3JvdXApO1xuXG4gICAgICAgICAgICAgICAgICAgICAgICAvLyAjIyNLQ00jIyMgQ2FuIGhhdmUgYSBjYWxsYmFjayB3aGVuIGRyb3AgaXMgZmluaXNoZWRcbiAgICAgICAgICAgICAgICAgICAgICAgIC8vIFNvdXJjZTpcbiAgICAgICAgICAgICAgICAgICAgICAgIC8vICAgICAgaHR0cHM6Ly9jb2RlLmdvb2dsZS5jb20vcC9qcXVlcnktZGF0YXRhYmxlcy1yb3ctcmVvcmRlcmluZy93aWtpL0luZGV4LFxuICAgICAgICAgICAgICAgICAgICAgICAgLy8gICAgICAtLT4gRnJlZSBjb21tZW50IG9mIFwiQ29tbWVudCBieSByYS4uLkB3ZWJydW4uY2EsIE1hciAxNiwgMjAxM1wiXG4gICAgICAgICAgICAgICAgICAgICAgICBpZihwcm9wZXJ0aWVzLmZuVXBkYXRlQ2FsbGJhY2sgJiYgdHlwZW9mKHByb3BlcnRpZXMuZm5VcGRhdGVDYWxsYmFjaykgPT09ICdmdW5jdGlvbicpe1xuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHByb3BlcnRpZXMuZm5VcGRhdGVDYWxsYmFjayhzUmVxdWVzdERhdGEpO1xuICAgICAgICAgICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9KTtcbiAgICAgICAgfSk7XG5cbiAgICAgICAgcmV0dXJuIHRoaXM7XG4gICAgfTtcblxuICAgIC8vIEF0dGFjaCBSb3dSZW9yZGVyaW5nIHRvIERhdGFUYWJsZXMgc28gaXQgY2FuIGJlIGFjY2Vzc2VkIGFzIGFuICdleHRyYSdcbiAgICAkLmZuLmRhdGFUYWJsZS5yb3dSZW9yZGVyaW5nID0gJC5mbi5yb3dSZW9yZGVyaW5nO1xuICAgICQuZm4uRGF0YVRhYmxlLnJvd1Jlb3JkZXJpbmcgPSAkLmZuLnJvd1Jlb3JkZXJpbmc7XG5cbiAgICAvLyBEYXRhVGFibGVzIDEuMTAgQVBJIG1ldGhvZCBhbGlhc2VzXG4gICAgaWYgKCAkLmZuLmRhdGFUYWJsZS5BcGkgKSB7XG4gICAgICAgIHZhciBBcGkgPSAkLmZuLmRhdGFUYWJsZS5BcGk7XG4gICAgICAgIEFwaS5yZWdpc3RlciggJ3Jvd1Jlb3JkZXJpbmcoKScsICQuZm4ucm93UmVvcmRlcmluZyApO1xuICAgIH1cbn0pKGpRdWVyeSk7Il19
