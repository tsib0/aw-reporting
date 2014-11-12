(function () {

    function AccountController ($scope, $location, $route, $routeParams, $document, $interval, coreProvider) {

        $scope.mccid = $routeParams.mccid;

        $scope.load_error = '';
        $scope.no_data_yet = false;
        $scope.show_choices = false;
        $scope.mcc_tasks_remaining = -1;

        coreProvider.getMccById($scope.mccid, function (err, results) {
            if (err) {
                $scope.load_error = err;
            } else {
                coreProvider.dataAvailableForMcc($scope.mccid, function (err, available) {
                    if (err) {
                        $scope.load_error = err;
                        $scope.show_choices = true;
                        return;
                    }

                    var ready =  (Array.isArray(available.reports_available)
                                  && available.reports_available.length > 0
                                  && available.pending_process_tasks == 0);
                    if (!ready) {
                        $scope.no_data_yet = true;
                        $scope.start_data_timer();
                    } else {
                        coreProvider.store_value_for_key("verified_loaded_" + $scope.mccid, true);
                        $scope.show_choices = true;
                    }
                });
            }
        });

        // every few seconds, get the latest task count for this account.
        // DON'T FORGET TO CANCEL THIS IN $SCOPE.$ON$DESTROY
        $scope.start_data_timer = function () {
            $scope.data_interval = $interval(function () {
                coreProvider.dataAvailableForMcc($scope.mccid, function (err, results) {
                    if (!err) {
                        if (results.reports_available.length > 0) {
                            $scope.mcc_tasks_remaining = results.pending_process_tasks;
                            if ($scope.mcc_tasks_remaining == 0) {
                                // mark this mcc as verified loaded, and then
                                // reload this page
                                coreProvider.store_value_for_key("verified_loaded_" + $scope.mccid, true);
                                $route.reload();
                            }
                        }
                    }
                });
            }, 2000);
        };

        $scope.$on('$destroy', function() {
            if ($scope.data_interval) $interval.cancel($scope.data_interval);
        });
    }

    gae2App.controller("AccountController", AccountController);

})();
