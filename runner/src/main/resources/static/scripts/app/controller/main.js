console.log('hello!')
console.log(app)
app.controller('MainController', function ($scope, $timeout, RestService) {
    // RestService.translationRoot().success(function (data) {
    //             $scope.greeting = data;
    //         });
    $scope.go = go;
    $scope.edit = edit;

    $timeout(function () {
        go(0);
    }, 500)

    function go(page, search, approved, hasFarsi) {
        RestService.translationSearch(page, 40, search, true, approved, hasFarsi).success(function (data) {
            $scope.data = data;
            $scope.pages = [];
            for (var i = 0; i < data.pageCount; i++) $scope.pages.push(i + 1);
        });
    }

    function edit(index, edit) {
        var name = $scope.data.data[index].ontologyClass;
        console.log(name)
        RestService.translationTranslate(name, edit.faLabel,
            edit.faOtherLabels, edit.note, edit.approved).success(function (data) {
            $scope.data.data[index].faLabel = edit.faLabel;
            $scope.data.data[index].faOtherLabels = edit.faOtherLabels;
            $scope.data.data[index].note = edit.note;
            $scope.data.data[index].approved = edit.approved;
        });
    }
});