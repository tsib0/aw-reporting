(function () {

    function AccountController ($scope, $location, $route, $routeParams, $document, $interval, $filter, $window, coreProvider) {

        $scope.mccid = $routeParams.mccid;
        $scope.load_error = ''; // the error itself
        $scope.no_data_yet = false; // data failed to load sets this to true
        $scope.show_choices = false; // display list of tools
        $scope.mcc_tasks_remaining = -1; // irrelevant
        $scope.show_date_picker = false; // show / hide date picker
        
        $scope.kratu_no_data = false;
        $scope.reports_no_data = false;
        
        $scope.TOTAL_LOADING_TASKS = 4;
        $scope.loading_task_count = 4;

        coreProvider.getMccById($scope.mccid, function (err, results) {
            if (err) {
                $scope.load_error = err;
            } else {
            	var kratus_available_loaded = false;
            	var reports_available_loaded = false;
            	
            	// Get daily available data for Kratu
                coreProvider.dataAvailableForMcc($scope.mccid, 'day', function (err, available) {
                    if (err) {
                        $scope.load_error = err;
                        $scope.show_choices = false;
                        return;
                    }
                    kratus_available_loaded = (available.kratus_available != null);
                    $scope.kratu_no_data =	kratus_available_loaded && ! available.kratus_available.endDay;

                    setKratuDateRange(available);
                    if(kratus_available_loaded && reports_available_loaded) {
                    	coreProvider.store_value_for_key("verified_loaded_" + $scope.mccid, true);
                    	$scope.show_choices = true;
                    }
                });
                
                // Get monthly available data for Reporting functions
                coreProvider.dataAvailableForMcc($scope.mccid, 'monthly', function (err, available) {
                    if (err) {
                        $scope.load_error = err;
                        $scope.show_choices = false;
                        return;
                    }
                    reports_available_loaded = Array.isArray(available.reports_available);
                    $scope.reports_no_data = reports_available_loaded && available.reports_available.length <= 0;
                    setReportsDateRange(available);
                    
                    if(kratus_available_loaded && reports_available_loaded) {
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
        }),
        
        setReportsDateRange = function(available_data) {
            if (available_data && available_data.reports_available
                && Array.isArray(available_data.reports_available)) {
                for (var i = 0; i < available_data.reports_available.length; i++) {
                    var rad = available_data.reports_available[i];
                    if (rad.ReportType == "ReportAccount") {
                    	$scope.reports_min_date = new Date(rad.startMonth);
                    	$scope.reports_max_date = new Date(rad.endMonth);
                        break;
                    }
                }
            }
        };
        
        setKratuDateRange = function(available_data) {
            if (available_data && available_data.kratus_available) {
            	$scope.kratu_min_date = new Date(available_data.kratus_available.startDay);
            	$scope.kratu_max_date = new Date(available_data.kratus_available.endDay);
            }
        };
        
        $scope.scroll_to_date_selection = function () {
        	//$scope.show_date_picker = true;
        	var date_section = angular.element(document.getElementById('date_section'));
    		$document.scrollTo(date_section, 0, 700);
        }
    }

    awrcApp.controller("AccountController", AccountController);

})();
