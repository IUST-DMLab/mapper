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
        $scope.filter = {
            page: page,
            search: search,
            approved: approved,
            hasFarsi: hasFarsi
        }
        RestService.translationSearch($scope.filter.page, 40, $scope.filter.search, true, $scope.filter.approved, $scope.filter.hasFarsi)
            .success(function (data) {
                $scope.data = data;
                $scope.pages = [];
                for (var i = 0; i < data.pageCount; i++) $scope.pages.push(i + 1);
            });
    }

    function edit(index, edit) {
        var name = $scope.data.data[index].ontologyClass;

        if (edit.faLabel == undefined) edit.faLabel = $scope.data.data[index].faLabel;
        if (edit.faOtherLabels == undefined) edit.faOtherLabels = $scope.data.data[index].faOtherLabels;
        if (edit.note == undefined) edit.note = $scope.data.data[index].note;
        if (edit.approved == undefined) edit.approved = $scope.data.data[index].approved;

        RestService.translationTranslate(name, edit.faLabel,
            edit.faOtherLabels, edit.note, edit.approved).success(function (data) {
            $scope.data.data[index].faLabel = edit.faLabel;
            $scope.data.data[index].faOtherLabels = edit.faOtherLabels;
            $scope.data.data[index].note = edit.note;
            $scope.data.data[index].approved = edit.approved;
        });
    }
});