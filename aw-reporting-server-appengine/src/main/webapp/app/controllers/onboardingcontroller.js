(function () {

    function OnboardingController ($scope, $routeParams, $location, coreProvider) {

        $scope.generate_reports_error = "";
        $scope.page_load_error = "";
        $scope.done_loading = false;
        $scope.force_disable_generate_button = false;

        // get first day of this month, then set max date to 1ms before that
        // (ie 23:59:59.999 night before)
        var d = new Date();
        d.setTime(d.getTime() + d.getTimezoneOffset() * 60000);
        d.setDate(1);
        $scope.date_end = d;

        var d = new Date();
        d.setTime(d.getTime() + d.getTimezoneOffset() * 60000);
        d.setDate(1);
        d.setTime(d.getTime() - 86400000);
        $scope.fixed_date_end = d;


        d = new Date();
        d.setTime(d.getTime() + d.getTimezoneOffset() * 60000);
        d.setDate(1);
        d.setYear(d.getFullYear() - 1);
        $scope.date_start = d;

        coreProvider.dataAvailableForMcc($routeParams.mccid, function (err, data) {
            if (err != null) {
                $scope.page_load_error = "Error loading page (" + err.error + "): " + err.message;
                $scope.done_loading = true;
            } else {
                if (data && data.pending_export_tasks == 0
                    && data.pending_process_tasks == 0
                    && Array.isArray(data.reports_available)
                    && data.reports_available.length == 0) {
                    // it's copmletely new, woooow! let's offer them the chance to
                    // start pulling down some reports.
                    $scope.done_loading = true;
                } else {
                    // just redirect back to the accounts page for this bad boy.
                    $location.path("/account/" + normalise_ccid($routeParams.mccid));
                }
            }
        });

        $scope.generateReports = function () {
            $scope.force_disable_generate_button = true;
            $scope.generate_reports_error = '';

            var dts = _fix_date($scope.date_start);
            var dte = _fix_date($scope.date_end);
console.log(dts);
 console.log(dte);

            coreProvider.pullAccountReports($routeParams.mccid, dts, dte, function (err) {
                if (err) {
                    $scope.force_disable_generate_button = false;
                    if (err.error == "date_start_bigger")
                        $scope.generate_reports_error = "Start date must be less than end date";
                    else if (err.error == "no_future_dates")
                        $scope.generate_reports_error = "We don't know how to pull reports from the future (yet)";
                    else
                        $scope.generate_reports_error = "Unable to start report download (" + err.error + "): " + err.message;
                } else {
                    $scope.generate_reports_success = "Report download started, redirecting ... ";
                    setTimeout(function () {
                        // we should be using $location.path here, but because this
                        // is a callback
                        document.location = "/";
                        $location.path("/");
                    }, 2000);
                }
            });
        };
    }



    gae2App.controller("OnboardingController", OnboardingController);



    function _fix_date (dt) {
        if (typeof d == 'string') {
            dt = new Date(dt);
        }

        var m = dt.getMonth() + 1;
        if (m < 10) m = "0" + m;
        var day = dt.getDate();
        if (day < 10) day = "0" + day;

        return dt.getFullYear() + m + day;
    }


})();
