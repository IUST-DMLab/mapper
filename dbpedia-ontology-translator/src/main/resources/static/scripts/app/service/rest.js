app.service('RestService', ['$http', function ($http) {
    var baseURl = 'http://localhost:8090/';
    var self = this;
    this.ingoing = 0;

    self.init = function (rootAddress) {
        baseURl = rootAddress;
    };

    function handelError(error) {
        self.ingoing--;
        console.log(error);
    }

    function handelSuccess(/*data, status, headers, config*/) {
        self.ingoing--;
    }

    function http(req) {
        if (OUC.isEmpty(req.params)) req.params = {};
        req.params.random = new Date().getTime();
        self.ingoing++;
        return $http(req).error(handelError).success(handelSuccess);
    }

    function post(url, data) {
        self.ingoing++;
        return $http.post(url, data).error(handelError).success(handelSuccess);
    }

    this.addMultipleTagToDisk = function (data) {
        return post(baseURl + 'rest/tags/addMultiple', data);
    };

    this.translationRoot = function () {
        var req = {
            method: 'GET',
            url: baseURl + 'translator/rest/v1/root',
            params: {}
        };
        return http(req);
    };

    this.translationSearch = function (page, pageSize, name, like, approved, hasFarsi) {
        var req = {
            method: 'GET',
            url: baseURl + 'translator/rest/v1/search',
            params: {
                page: page,
                pageSize: pageSize
            }
        };
        if (name != null) req.params.name = name;
        if (like != null) req.params.like = like;
        if (approved != null && approved) req.params.approved = approved;
        if (hasFarsi != null && hasFarsi) req.params.hasFarsi = hasFarsi;
        return http(req);
    };

    this.translationTranslate = function (name, faLabel, faOtherLabels, note, approved) {
        var req = {
            method: 'GET',
            url: baseURl + 'translator/rest/v1/translate',
            params: {
                name: name,
                faLabel: OUC.isEmpty(faLabel) ? "" : faLabel,
                faOtherLabels: OUC.isEmpty(faOtherLabels) ? "" : faOtherLabels,
                note: OUC.isEmpty(note) ? "" : note,
                approved: OUC.isEmpty(approved) ? false : approved
            }
        };
        return http(req);
    };

}]);