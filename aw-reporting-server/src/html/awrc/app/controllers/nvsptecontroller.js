(function () {

    function NVSPTEController ($scope, $cookieStore) {
        $cookieStore.put(IAMVERYSUPER, true);
    }

    awrcApp.controller("NVSPTEController", NVSPTEController);

})();
