var app = angular.module('poll-app', []);
var socket = io.connect({transports:['polling']});

app.controller('statsCtrl', function($scope){
  var updateScores = function(){
    socket.on('scores', function (json) {
       data = JSON.parse(json);
       var user_id = data[0].user_id;
       var first_name = data[0].first_name;
       var last_name = data[0].last_name;
       $scope.$apply(function () {
         $scope.user_id = user_id;
         $scope.first_name = first_name;
         $scope.last_name = last_name;
       });
    });
  };

  var init = function(){
    updateScores();
  };
  socket.on('message',function(data){
    init();
  });
});
