//console.log('hello!');
//console.log(app);
app.controller('MainController', function ($scope, $timeout, RestService) {
    // RestService.translationRoot().success(function (data) {
    //             $scope.greeting = data;
    //         });
    $scope.go = go;
    $scope.edit = edit;

    $timeout(function () {
        go(0);
    }, 500);

    function go(page, search, approved, hasFarsi) {
        $scope.filter = {
            page: page,
            search: search,
            approved: approved,
            hasFarsi: hasFarsi
        };
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


app.controller('TemplateMappingController', function ($scope, $timeout, RestService) {
   $scope.go = go;
   $scope.save = save;
   $scope.revert = revert;

   //$scope.loadClasses = function (query) {
   //    return RestService.ontologyClassSearch(0, 100, query);
   //};

   $scope.autoCompleteOptions = {
      minimumChars: 1,
      data: function (term) {
         return RestService.ontologyClassSearch(0, 100, term);
      }
   };

   $timeout(function () {
      go(0);
   }, 500);

   function go(page, search, approved, hasFarsi) {
      $scope.filter = {
         page: page,
         search: search,
         approved: approved,
         hasFarsi: hasFarsi
      };
      RestService.templateMappingSearch($scope.filter.page, 40, $scope.filter.search, true, $scope.filter.approved, $scope.filter.hasFarsi)
          .success(function (data) {
             $scope.data = data;
             $scope.pages = [];
             for (var i = 0; i < data.pageCount; i++) $scope.pages.push(i + 1);
          });
   }

   function save(index, item) {
      RestService.templateMappingSave(item).success(function (data) {
         $scope.data.data[index].ontologyClass = data.ontologyClass;
         $scope.data.data[index].approved = data.approved;
      });
   }

   function revert(index) {
      $scope.data.data[index] = $scope.copy.data[index];
   }
});


app.controller('PropertyMappingController', function ($scope, $timeout, RestService) {
   $scope.go = go;
   $scope.save = save;
   $scope.revert = revert;

   $scope.autoCompleteOptions = {
      minimumChars: 1,
      data: function (term) {
         return RestService.ontologyPropertyNameSearch(0, 100, term);
      }
   };

   $scope.statusList = ['Approved', 'NearlyApproved', 'NotApproved', 'Multiple', 'Translated', 'NotMapped'];

   $timeout(function () {
      go(0);
   }, 500);

   function go(page, search, approved, hasFarsi) {
      $scope.filter = {
         page: page,
         search: search,
         approved: approved,
         hasFarsi: hasFarsi
      };
      RestService.propertyMappingSearch($scope.filter.page, 40, $scope.filter.search, true, $scope.filter.approved, $scope.filter.hasFarsi)
          .success(function (data) {
             $scope.copy = angular.copy(data);
             $scope.data = data;
             $scope.data.pageNo = $scope.data.page + 1;
          });
   }

   function save(index, item) {
      RestService.propertyMappingSave(item).success(function (data) {
         $.extend($scope.data.data[index], data);
      });
   }

   function revert(index) {
      $scope.data.data[index] = $scope.copy.data[index];
   }
});

