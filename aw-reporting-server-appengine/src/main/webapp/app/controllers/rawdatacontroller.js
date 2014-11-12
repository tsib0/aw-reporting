/*
accountId: 1834866136
avgCpc: 0.15
avgCpm: 0.83
clicks: 784
conversions: 0
conversionsManyPerClick: 0
cost: 121.38
ctr: 0.53
impressions: 146558
*/

(function () {

    var month_names = [ "January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December" ];

    function RawDataController ($scope, $location, $routeParams, $timeout, coreProvider) {

        $scope.mccid = $routeParams.mccid;

        // make sure we've verified we have data ready to go.
        if (!coreProvider.get_value_for_key("verified_loaded_" + $scope.mccid)) {
            $location.path("/account/" + $scope.mccid);
            return;
        }

        $scope.TOTAL_LOADING_TASKS = 4;
        $scope.loading_task_count = 4;
        $scope.load_error_text = '';
        $scope.client_accounts = [];
        $scope.mcc_info = null;

        $scope.accounts = [];
        $scope.source_accounts = [];
        $scope.selected_accounts = [];

        $scope.report_selectedIndex = -1;
        $scope.account_selectedIndexes = [];
        $scope.selectedAccount_selectedIndexes = [];

        $scope.processed_reports = null;
        $scope.charts = null;

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

                // 2. see if it has any reports downloaded, if not, abort.
                $scope.loading_task_count--;
                emsg = "Unable to load accounts for this MCC";
                coreProvider.getAccountsForMcc($scope.mccid, true, cb);
            },
            function (accounts, cb) {
                // make them more searchable!
                if (Array.isArray(accounts)) {
                    for (var i = 0; i < accounts.length; i++) {
                        accounts[i]._accountId = format_ccid(accounts[i].accountId);
                    }
                }

                $scope.total_number_items = accounts.length;
                $scope.max_visible_pages = 10;
                $scope.current_page = 0;
                $scope.loading_task_count--;
                $scope.accounts = accounts;
                $scope.source_accounts = JSON.parse(JSON.stringify(accounts));

                // dates don't work currently with this API :(
                coreProvider.getReportXYZ(
                    "account",
                    $scope.mccid,
                    "2014-01-01",
                    "2014-06-01",
                    cb
                );
            },
            function (reports, cb) {
                $scope.loading_task_count--;
                $scope.processed_reports = __process(reports);
                cb(null);
            }
        ], function (err, reports) {
            if (err) {
                $scope.loading_task_count = 0;
                $scope.load_error = err.error;
                $scope.load_error_text = emsg + "(" + err.error + "): " + err.message + ")";
            } else {
                $scope.loading_task_count = 0;
                $timeout(function () {
                    $scope.current_page = 1;
                    $scope.pageChanged();
//                    $scope.setPage(1);
                }, 100);
            }
        });

        $scope.setPage = function (page) {
            $scope.charts = $scope.processed_reports.slice(0, 10);
        }


        $scope.pageChanged = function () {
            var pg = $scope.current_page;
            var start = (pg - 1) * 10
            $scope.charts = $scope.processed_reports.slice(start, start + 10); 


            /**
             * this is pretty UN-angular -- this should probably go in a 
             * directive somewhere. CLEAN ME UP YA LAZY ARSE!
             */

            /**
             * I'm not 100% positive this is necessary, but I instinctively
             * feel it is because I suspect need to let the elements settle
             * down from the ng-repeat before we start dumping crap into
             * them.
             */
            $timeout(function () {
                for (var i = 0; i < $scope.charts.length; i++) {
                    var c = $scope.charts[i];
                    var el, ctx;

                    el = document.getElementById("chart1_" + c.chart_data.accountid);
                    ctx = el.getContext("2d");
                    var data1 = {
	                labels : ["","","","",""],
	                datasets : [
		            {
			        fillColor : "rgba(220,220,220,0.5)",
			        strokeColor : "rgba(220,220,220,1)",
			        pointColor : "rgba(220,220,220,1)",
			        pointStrokeColor : "#fff",
			        data : c.chart_data.impressions
		            },
	                ]
                    };
                    new Chart(ctx).Line(data1, {scaleShowLabels: false});
                    el = document.getElementById("chart2_" + c.chart_data.accountid);
                    ctx = el.getContext("2d");
                    var data2 = {
	                labels : ["","","","",""],
	                datasets : [
		            {
			        fillColor : "rgba(220,220,220,0.5)",
			        strokeColor : "rgba(220,220,220,1)",
			        pointColor : "rgba(220,220,220,1)",
			        pointStrokeColor : "#fff",
			        data : c.chart_data.clicks
		            },
	                ]
                    };
                    new Chart(ctx).Line(data2, {scaleShowLabels: false});

                    el = document.getElementById("chart3_" + c.chart_data.accountid);
                    ctx = el.getContext("2d");
                    var data3 = {
	                labels : ["","","","",""],
	                datasets : [
		            {
			        fillColor : "rgba(220,220,220,0.5)",
			        strokeColor : "rgba(220,220,220,1)",
			        pointColor : "rgba(220,220,220,1)",
			        pointStrokeColor : "#fff",
			        data : c.chart_data.ctr
		            },
		            {
			        fillColor : "rgba(151,187,205,0.5)",
			        strokeColor : "rgba(151,187,205,1)",
			        pointColor : "rgba(151,187,205,1)",
			        pointStrokeColor : "#fff",
			        data : c.chart_data.avgCpc
		            }
	                ]
                    };
                    new Chart(ctx).Line(data3, {scaleShowLabels: false});

                    el = document.getElementById("chart4_" + c.chart_data.accountid);
                    ctx = el.getContext("2d");
                    var data4 = {
	                labels : ["","","","",""],
	                datasets : [
		            {
			        fillColor : "rgba(220,220,220,0.5)",
			        strokeColor : "rgba(220,220,220,1)",
			        pointColor : "rgba(220,220,220,1)",
			        pointStrokeColor : "#fff",
			        data : c.chart_data.cost
		            },
	                ]
                    };
                    new Chart(ctx).Line(data4, {scaleShowLabels: false});
                }
            }, 500);
        }

    }

 
    gae2App.controller("RawDataController", RawDataController);



    /**
     * Processing is in two parts,
     *
     * 1. sort and set up data arrays for the reports.
     * 2. set up the data that chartjs will need.
     */
    function __process(reports) {

        var new_reports = _extract_and_sort_data(reports);
        return _generate_chart_data(new_reports);
    }

    function _extract_and_sort_data(reports) {
        var out = {};
        for (var i = 0; i < reports.length; i++) {
            reports[i].accountId = format_ccid(reports[i].accountId);
            if (!out["" + reports[i].accountId])
                out["" + reports[i].accountId] = [];
            out["" + reports[i].accountId].push(reports[i]);
        }

        var k = Object.keys(out);
        for (var j = 0; j < k.length; j++) {
            out[k[j]].sort(function (a, b) { return a.dateStart > b.dateStart; });
        }


        // now sort and return the reports.
        k.sort();

        var results = [];
        for (i = 0; i < k.length; i++) {
            results.push(out[k[i]]);
        }

        return results;
    }


    function _generate_chart_data (reports) {
        var out = [];

        for (var i = 0; i < reports.length; i++) {
            if (!Array.isArray(reports[i])) {
                console.error("WAT, this should be an array of reports: " + reports[i]);
                continue;
            }

            // if we have more than 12 months, just show the last 12.
            var list = reports[i];
            if (list.length > 12) list = list.slice(list.length - 12);

            var obj = {
                reports: null,
                chart_data: {
                    accountid: list[0].accountId,
                    name: list[0].accountDescriptiveName,
                    labels: [],
                    impressions: [],
                    clicks: [],
                    ctr: [],
                    avgCpc: [],
                    cost: [],
                    avgCpm: [],
                }
            };



            for (var j = 0; j < list.length; j++) {
                var r = list[j];
                var label = month_names[new Date(r.month).getMonth()];
                obj.chart_data.labels.push(label);
                obj.chart_data.impressions.push(r.impressions);
                obj.chart_data.clicks.push(r.clicks);
                obj.chart_data.ctr.push(r.ctr);
                obj.chart_data.avgCpc.push(r.avgCpc);
                obj.chart_data.cost.push(r.cost);
                obj.chart_data.avgCpm.push(r.avgCpm);
            }

            obj.reports = reports[i];
            out.push(obj);
        }

        return out;
    }

})();
