var app = angular.module('kgui', ['ui.bootstrap', 'ngTagsInput', 'autoCompleteModule', 'bw.paging']);
var OUC = {
    isEmpty: function (obj) {
        return obj == undefined || obj == null;
    }
};