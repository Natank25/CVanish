var app = angular.module('poll-app', []);
var socket = io.connect({transports: ['polling']});


app.controller('appCtrl', function ($scope) {
    var updateScores = function () {
        socket.on('scores', function (json) {
            data = JSON.parse(json);

            $scope.$apply(function () {
                $scope.data = data;
            });
        });
    };

    var init = function () {
        updateScores();
    };
    socket.on('message', function (data) {
        init();
    });
});
