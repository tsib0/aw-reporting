(function () {

    function DocExportController ($scope, $location, $route, $routeParams, $document, $interval, coreProvider) {
        $scope.mccid = $routeParams.mccid;

        // make sure we've verified we have data ready to go.
        if (!coreProvider.get_value_for_key("verified_loaded_" + $scope.mccid)) {
            $location.path("/account/" + $scope.mccid);
            return;
        }

        $scope.mccid = $routeParams.mccid;

        $scope.step = 0;
        $scope.no_data_yet = false;

        $scope.TOTAL_LOADING_TASKS = 4;
        $scope.loading_task_count = 4;
        $scope.load_error_text = '';
        $scope.go_button = "";
        $scope.client_accounts = [];
        $scope.mcc_info = null;
        $scope.templates = [];

        $scope.accounts = [];
        $scope.source_accounts = [];
        $scope.selected_accounts = [];

        $scope.report_selectedIndex = -1;
        $scope.account_selectedIndexes = [];
        $scope.selectedAccount_selectedIndexes = [];

        $scope.mcc_tasks_remaining = -1;
        $scope.export_tasks_remaining = -1;
        $scope.select_all = 1;

        var emsg = "Unable to load account info";

        async.waterfall([
            function (cb) {
                // 1. get the core mcc info
                coreProvider.getMccById($scope.mccid, cb);
            },
            function (mcc, cb) {
                $scope.mcc_info = mcc;
                if (!$scope.mcc_info.name)
                    $scope.mcc_info.name = format_ccid($scope.mcc_info.topAccountId) + " (Unknown Name)";

                $scope.loading_task_count--;
                coreProvider.getTemplates(cb);
            },
            function (templates, cb) {
                $scope.templates = templates;
                $scope.loading_task_count--;
                coreProvider.getAccountsForMcc($scope.mccid, true, cb);
            },

            function (accounts, cb) {
                // make them more searchable!
                if (Array.isArray(accounts)) {
                    for (var i = 0; i < accounts.length; i++) {
                        accounts[i]._accountId = format_ccid(accounts[i].accountId);
                    }
                }
                $scope.loading_task_count--;
                $scope.accounts = accounts;
                $scope.source_accounts = JSON.parse(JSON.stringify(accounts));

                // 4. finally, get the available dates of data for this account
                emsg = "Unable to determine which account reports are available";
                coreProvider.dataAvailableForMcc($scope.mccid, cb);
            },

            function (available_data, cb) {
                if (available_data && available_data.reports_available
                    && Array.isArray(available_data.reports_available)) {
                    for (var i = 0; i < available_data.reports_available.length; i++) {
                        var rad = available_data.reports_available[i];
                        if (rad.ReportType == "ReportAccount") {
                            var d = new Date(rad.startMonth);
                            d.setTime(d.getTime() + d.getTimezoneOffset() * 60000);
                            $scope.min_date = d;

                            $scope.dts = new Date(rad.endMonth);
                            $scope.dts.setTime($scope.dts.getTime() + $scope.dts.getTimezoneOffset() * 60000);
                            $scope.dts.setMonth($scope.dts.getMonth() - 1);

                            $scope.dte = new Date(rad.endMonth);
                            $scope.dte.setTime($scope.dte.getTime() + $scope.dte.getTimezoneOffset() * 60000);
                            $scope.dte.setTime($scope.dte.getTime() - 1);
                            $scope.max_date = $scope.dte;
                            break;
                        }
                    }

                    $scope.$watch("dts", watch_dtsdte);
                    $scope.$watch("dte", watch_dtsdte);
                }

                $scope.loading_task_count--;
                cb(null);
            },

        ], function (err) {
            if (err) {
                $scope.loading_task_count = 0;
                $scope.load_error = err.error;
                $scope.load_error_text = "Unable to load account info (" + err.error + ": " + err.message +")";
                $scope.load_error_text = emsg + "(" + err.error + "): " + err.message + ")";
            } else {
                $scope.$apply();
            }
        });

        $scope.start_export_timer = function () {
            $scope.export_interval = $interval(function () {
                coreProvider.dataAvailableForMcc($scope.mccid, function (err, results) {
                    if (!err) {
                        $scope.export_tasks_remaining = results.pending_export_tasks;
                        if ($scope.export_tasks_remaining == 0) {
                            $interval.cancel($scope.export_interval);
                            $scope.export_interval = null;
                        }
                    }
                });
            }, 2000);
        };


        $scope.$on('$destroy', function() {
            if ($scope.export_interval) $interval.cancel($scope.data_interval);
        });

        $scope.selectReport = function (idx, name) {
            $scope.report_selectedIndex = idx;
            var section2 = angular.element(document.getElementById('pdf-step2'));
            $document.scrollTo(section2, 0, 700);
            $scope.step = 3;
        };

        $scope.showGenerateReports = function () {
            $scope.step = 1;
            $scope.report_selectedIndex = -1;
        };
        $scope.shiftMatches = function (items_to_shift) {
            var i, j;
            for (i = 0; i < items_to_shift.length; i++) {
                $scope.selected_accounts.push(JSON.parse(JSON.stringify(items_to_shift[i])));
            }

            var sources = [];
            for (i = 0; i < $scope.source_accounts.length; i++) {
                for (j = 0; j < items_to_shift.length; j++) {
                    if ($scope.source_accounts[i]._accountId == items_to_shift[j]._accountId) break;
                }

                if (j == items_to_shift.length) sources.push($scope.source_accounts[i]);
            }

            $scope.source_accounts = sources;
            $scope.filter_accounts = '';
        }


        $scope.selectAccount = function (index, name) {
            if ($scope.source_accounts[index].name) {
                $scope.filter_accounts = name;
            } else {
                $scope.filter_accounts = $scope.source_accounts[index]._accountId;
            }
        }

        $scope.resetMatches = function () {
            $scope.source_accounts = JSON.parse(JSON.stringify($scope.accounts));
            $scope.selected_accounts = [];
            $scope.filter_accounts = "";
        };


        $scope.go = function () {
            $scope.go_button = "disable";

console.log(                    _fix_date($scope.dts));
console.log(                    _fix_date($scope.dte));


            if ($scope.select_all || ($scope.selected_accounts.length == $scope.accounts.length)) {
                coreProvider.exportPDFReports(
                    $scope.mccid,
                    _fix_date($scope.dts),
                    _fix_date($scope.dte),
                    $scope.templates[$scope.report_selectedIndex].id,
                    function (err, results) { 
                        $scope.go_button = "hide";
                        $scope.start_export_timer();
                    }
                );
            } else {
                var ccids = []
                for (var i = 0; i < $scope.selected_accounts.length; i++) {
                    ccids.push($scope.selected_accounts[i]._accountId);
                }

                coreProvider.exportPDFReports(
                    $scope.mccid,
                    _fix_date($scope.dts),
                    _fix_date($scope.dte),
                    ccids,
                    $scope.templates[$scope.report_selectedIndex].id,
                    function (err, results) { 
                        $scope.go_button = "hide";
                        $scope.start_export_timer();
                    }
                );
            }
        };

        /**
          * I feel bad having the scrollTos in here. It doesn't feel very ... angular-y.
          */
        var watch_dtsdte = function () {
            if ($scope.dts && $scope.dte) {
                if ($scope.dte.getDate() == 1) {
                    $scope.dte.setTime($scope.dte.getTime() + $scope.dte.getTimezoneOffset() * 60000);
                    $scope.dte.setTime($scope.dte.getTime() + 32 * 86400000);
                    $scope.dte.setDate(1);
                    $scope.dte.setTime($scope.dte.getTime() - 86400000);
                }
                var section2 = angular.element(document.getElementById('pdf-step3'));
            }                
        };
    }

    gae2App.controller("DocExportController", DocExportController);


    function _fix_date (dt) {
        if (typeof d == 'string') {
            dt = new Date(dt);
        }

        var m = dt.getMonth() + 1;
        if (m < 10) m = "0" + m;
//        var day = dt.getDate();
//        if (day < 10) day = "0" + day;

        return dt.getFullYear() + m /* + day */;
    }



})();
