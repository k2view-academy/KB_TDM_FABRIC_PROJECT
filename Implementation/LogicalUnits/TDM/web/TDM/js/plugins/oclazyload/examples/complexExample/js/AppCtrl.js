angular.module("app").controller("AppCtrl",(function($scope,$ocLazyLoad){$scope.$on("ocLazyLoad.moduleLoaded",(function(e,params){console.log("event module loaded",params)})),$scope.$on("ocLazyLoad.componentLoaded",(function(e,params){console.log("event component loaded",params)})),$scope.$on("ocLazyLoad.fileLoaded",(function(e,file){console.log("event file loaded",file)})),$scope.loadBootstrap=function(){var unbind=$scope.$on("ocLazyLoad.fileLoaded",(function(e,file){"js/plugins/bootstrap/dist/css/bootstrap.css"===file&&($scope.bootstrapLoaded=!0,unbind())}));$ocLazyLoad.load(["js/plugins/bootstrap/dist/js/bootstrap.js","js/plugins/bootstrap/dist/css/bootstrap.css"])}}));
//# sourceMappingURL=data:application/json;charset=utf8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbInBsdWdpbnMvb2NsYXp5bG9hZC9leGFtcGxlcy9jb21wbGV4RXhhbXBsZS9qcy9BcHBDdHJsLmpzIl0sIm5hbWVzIjpbImFuZ3VsYXIiLCJtb2R1bGUiLCJjb250cm9sbGVyIiwiJHNjb3BlIiwiJG9jTGF6eUxvYWQiLCIkb24iLCJlIiwicGFyYW1zIiwiY29uc29sZSIsImxvZyIsImZpbGUiLCJsb2FkQm9vdHN0cmFwIiwidW5iaW5kIiwiYm9vdHN0cmFwTG9hZGVkIiwibG9hZCJdLCJtYXBwaW5ncyI6IkFBQUFBLFFBQVFDLE9BQU8sT0FBT0MsV0FBVyxXQUFXLFNBQVNDLE9BQVFDLGFBQzNERCxPQUFPRSxJQUFJLDJCQUEyQixTQUFTQyxFQUFHQyxRQUNoREMsUUFBUUMsSUFBSSxzQkFBdUJGLFdBR3JDSixPQUFPRSxJQUFJLDhCQUE4QixTQUFTQyxFQUFHQyxRQUNuREMsUUFBUUMsSUFBSSx5QkFBMEJGLFdBR3hDSixPQUFPRSxJQUFJLHlCQUF5QixTQUFTQyxFQUFHSSxNQUM5Q0YsUUFBUUMsSUFBSSxvQkFBcUJDLFNBR25DUCxPQUFPUSxjQUFnQixXQUVyQixJQUFJQyxPQUFTVCxPQUFPRSxJQUFJLHlCQUF5QixTQUFTQyxFQUFHSSxNQUMvQyxnREFBVEEsT0FDRFAsT0FBT1UsaUJBQWtCLEVBQ3pCRCxhQUlKUixZQUFZVSxLQUFLLENBQ2YsNENBQ0EiLCJmaWxlIjoicGx1Z2lucy9vY2xhenlsb2FkL2V4YW1wbGVzL2NvbXBsZXhFeGFtcGxlL2pzL0FwcEN0cmwuanMiLCJzb3VyY2VzQ29udGVudCI6WyJhbmd1bGFyLm1vZHVsZSgnYXBwJykuY29udHJvbGxlcignQXBwQ3RybCcsIGZ1bmN0aW9uKCRzY29wZSwgJG9jTGF6eUxvYWQpIHtcbiAgJHNjb3BlLiRvbignb2NMYXp5TG9hZC5tb2R1bGVMb2FkZWQnLCBmdW5jdGlvbihlLCBwYXJhbXMpIHtcbiAgICBjb25zb2xlLmxvZygnZXZlbnQgbW9kdWxlIGxvYWRlZCcsIHBhcmFtcyk7XG4gIH0pO1xuXG4gICRzY29wZS4kb24oJ29jTGF6eUxvYWQuY29tcG9uZW50TG9hZGVkJywgZnVuY3Rpb24oZSwgcGFyYW1zKSB7XG4gICAgY29uc29sZS5sb2coJ2V2ZW50IGNvbXBvbmVudCBsb2FkZWQnLCBwYXJhbXMpO1xuICB9KTtcblxuICAkc2NvcGUuJG9uKCdvY0xhenlMb2FkLmZpbGVMb2FkZWQnLCBmdW5jdGlvbihlLCBmaWxlKSB7XG4gICAgY29uc29sZS5sb2coJ2V2ZW50IGZpbGUgbG9hZGVkJywgZmlsZSk7XG4gIH0pO1xuICBcbiAgJHNjb3BlLmxvYWRCb290c3RyYXAgPSBmdW5jdGlvbigpIHtcbiAgICAvLyB1c2UgZXZlbnRzIHRvIGtub3cgd2hlbiB0aGUgZmlsZXMgYXJlIGxvYWRlZFxuICAgIHZhciB1bmJpbmQgPSAkc2NvcGUuJG9uKCdvY0xhenlMb2FkLmZpbGVMb2FkZWQnLCBmdW5jdGlvbihlLCBmaWxlKSB7XG4gICAgICBpZihmaWxlID09PSAnanMvcGx1Z2lucy9ib290c3RyYXAvZGlzdC9jc3MvYm9vdHN0cmFwLmNzcycpIHtcbiAgICAgICAgJHNjb3BlLmJvb3RzdHJhcExvYWRlZCA9IHRydWU7XG4gICAgICAgIHVuYmluZCgpO1xuICAgICAgfVxuICAgIH0pO1xuICAgIC8vIHdlIGNvdWxkIHVzZSAudGhlbiBoZXJlIGluc3RlYWQgb2YgZXZlbnRzXG4gICAgJG9jTGF6eUxvYWQubG9hZChbXG4gICAgICAnanMvcGx1Z2lucy9ib290c3RyYXAvZGlzdC9qcy9ib290c3RyYXAuanMnLFxuICAgICAgJ2pzL3BsdWdpbnMvYm9vdHN0cmFwL2Rpc3QvY3NzL2Jvb3RzdHJhcC5jc3MnXG4gICAgXSk7XG4gIH07XG59KTtcbiJdfQ==
