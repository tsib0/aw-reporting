var gae2App = angular.module("gae2App", [ "ngRoute", "ngCookies", 'ui.bootstrap', 'duScroll' ]);

var IAMVERYSUPER = "iamverysuper";

gae2App.config(function ($routeProvider) {
    $routeProvider
        .when("/", { redirectTo: "/accounts" })
        .when("/not/very/secret/public/template/enabler", { controller: "NVSPTEController", templateUrl: "/app/partials/nvspte_partial.html" })
        .when("/accounts", { controller: "HomeAccountController", templateUrl: "/app/partials/home_account_partial.html" })
        .when("/reports", { controller: "HomeReportController", templateUrl: "/app/partials/home_report_partial.html" })
        .when("/template/:template_id", { controller: "TemplateController", templateUrl: "/app/partials/template_partial.html" })
        .when("/template", { controller: "TemplateController", templateUrl: "/app/partials/template_partial.html" })
        .when("/template_help", { controller: "StaticContentController", templateUrl: "/app/partials/template_help_partial.html" })
        .when("/account/:mccid", { controller: "AccountController", templateUrl: "/app/partials/account_partial.html" })
        .when("/account/:mccid/export", { controller: "DocExportController", templateUrl: "/app/partials/doc_export_partial.html" })
        .when("/account/:mccid/rawdata", { controller: "RawDataController", templateUrl: "/app/partials/raw_data_partial.html" })
        .when("/new_mcc/:mccid", { controller: "OnboardingController", templateUrl: "/app/partials/onboarding_partial.html" })
        .when("/error/:error_code", { controller: "ErrorController", templateUrl: "/app/partials/error_partial.html" })
        .when("/404_page", { controller: "Controller404", templateUrl: "/app/partials/404_page_partial.html" })
        .otherwise( { redirectTo: "/" });
});



gae2App.directive("gaeTextFileReader", function () {
    return {
        link: function (scope, el, attr) {
            el.bind("change", function (e) {
                var file = (e.srcElement || e.target).files[0];
                var reader = new FileReader();
                var dest = attr.gaeTextFileReader;
                reader.onload = function () {
                    var template = this.result;
                    scope.$apply(function () {
                        if (scope[dest] !== undefined) {
                            scope[dest] = template;
                        }
                    });
                };

                reader.readAsText(file);
            });
        }
    }
});


gae2App.directive('datetimez', function() {
    return {
        restrict: 'A',
        require : 'ngModel',
        link: function(scope, element, attrs, ngModelCtrl) {
            element.datetimepicker({
                format: "MM-yyyy",
                viewMode: "months", 
                minViewMode: "months",
                pickTime: false,
            }).on('changeDate', function(e) {
                ngModelCtrl.$setViewValue(e.date);
                scope.$apply();
            });
        }
    };
});

