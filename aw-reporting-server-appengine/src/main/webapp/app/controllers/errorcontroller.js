(function () {

    function ErrorController ($scope, $routeParams) {

        $scope.error = $routeParams.error_code;
    }

    gae2App.controller("ErrorController", ErrorController);


})();
