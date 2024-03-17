(function () {
  "use-strict";
  // Basic setup from: http://txt.fliglio.com/2013/05/angularjs-state-management-with-ui-router/

  angular.module("ussdClient", []).controller("MainController", [
    "$scope",
    "$http",
    "ussdDjango",
    function ($scope, $http, django) {
      var initial_data = function () {
        return {
          url: window.location.origin + "/ussd/",
          serviceCode: "SECRET",
          phoneNumber: "+SECRET",
          sessionId:
            django.transport + "AT_____" + Math.floor(Math.random() * 999999),
          text: "",
        };
      };
      if (django.transport == "OsmoCom") {
        initial_data = function () {
          return {
            url: window.location.origin + "/ussd/",
            serviceCode: "*100",
            phoneNumber: "3000",
            sessionId: Math.floor(Math.random() * 99),
            text: "",
          };
        };
      }
      if (django.transport == "Niafikra") {
        initial_data = function () {
          return {
            url: window.location.origin + "/ussd/",
            serviceCode: "*149*26#",
            phoneNumber: "255000000000",
            sessionId: Math.floor(Math.random() * 99999999999),
            text: "",
          };
        };
      }

      $scope.input_append = django.transport == "AfricasTalking" ? true : false;

      angular.extend($scope, {
        session: "start",
        input: "",
        response: "",
        input_history: [],
        data: initial_data(),

        send: function ($event) {
          console.log("Send", $scope.input_history);
          Array.prototype.push.apply(
            $scope.input_history,
            $scope.input.split("*")
          );
          if ($scope.input_append == true) {
            $scope.data.text = $scope.input_history.join("*");
          } else {
            $scope.data.text = $scope.input;
          }
          $scope.input = "";
          $scope.post();
        },

        call: function () {
          console.log(
            "Calling",
            $scope.data.serviceCode,
            "on",
            $scope.data.url
          );
          var initial_data = $scope.data.text.split("*");
          if (initial_data.length >= 1 && initial_data[0] != "") {
            $scope.input_history = initial_data;
          }
          $scope.session = "ongoing";
          $scope.post();
        },

        end: function () {
          $scope.session = "start";
          // Reset all variables
          $scope.data = initial_data();
          $scope.data.text = "";
          $scope.response = "";
          $scope.input_history = [];
          $scope.input = "";
        },

        post: function () {
          console.log("Posting", $scope.data);
          $http
            .post(window.location.href, $scope.data, { responseType: "json" })
            .then(function (response) {
              console.log("Post Response", response);
              $scope.textValid = response.data.text.substr(0, 160);
              $scope.textOverflow = response.data.text.substr(160);

              if (response.data.action.toLowerCase() !== "con") {
                console.log("END");
                $scope.session = "end";
              }
            });
        },

        keypress: function (evt) {
          if (evt.key == "Enter") {
            $scope.send();
          }
        },
      }); //end extend
    },
  ]);
})();
