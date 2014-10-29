(function () {

    function NVSPTEController ($scope, $cookieStore) {
        $cookieStore.put(IAMVERYSUPER, true);
    }

    gae2App.controller("NVSPTEController", NVSPTEController);

})();
