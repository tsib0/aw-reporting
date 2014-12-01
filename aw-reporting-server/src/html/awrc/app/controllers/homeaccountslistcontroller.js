(function () {

    function HomeAccountsListController ($scope, $location, $modal, $route, coreProvider) {

        $scope.loading_task_count = 1;
        $scope.load_mccs_error = "";
        $scope.mccs = [];

        coreProvider.getMccs(function (err, mccs) {
            if (!err) {
                $scope.mccs = mccs;
            } else {
                $scope.load_mccs_error = "Unable to load your MCCs (" + err.error + ": " + err.message + ")";
            }

            $scope.loading_task_count--;
        });

        $scope.authenticateMCC = function (mccid) {
            // this will redirect back to us, so it'll force a reload,
            // which means coreProvider will reload mccs. that's good!
            document.location = "/oauth/" + normalise_ccid(mccid);
        };

        $scope.selectMCC = function (mccid) {
            $location.path("/account/" + format_ccid(mccid));
        };


        $scope.deleteMCC = function (mccid, $event) {
            var mcc = mccid;

            var modalInstance = $modal.open({
                templateUrl: 'myYesNoDialog.html',
                controller: ModalInstanceController,
                size: "sm"
            });

            modalInstance.result.then(function (value) {
                if (value) {
                    // delete the mcc and then reload on success
                    coreProvider.deleteMCC(mcc, function (e) {
                        if (e) {
                            $scope.load_mccs_error = "Unable to delete that MCC. Weird.";
                        } else {
                            $route.reload();
                        }
                    });
                }
            }, function () {
                // ignore
            });
            return true;
        };
    }

    awrcApp.controller("HomeAccountsListController", HomeAccountsListController);

    var ModalInstanceController = function ($scope, $modalInstance) {

        $scope.ok = function () {
            $modalInstance.close(true);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };


})();
