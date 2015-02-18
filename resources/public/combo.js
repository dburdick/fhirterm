var app = angular.module('combo', ['formstamp']);

app.controller('MainCtrl', ['$scope', '$http', '$sce', '$q', function($scope, $http, $sce, $q) {
  $scope.fhirServerUrl = 'http://localhost:3000';

  $scope.listValueSets = function() {
    var g = $http.get($scope.fhirServerUrl + '/ValueSet');

    g.success(function(data, status, headers, config) {
      $scope.valueSets = data.entry.map(function(e) {
        e.resource.text.div = $sce.trustAsHtml(e.resource.text.div);
        return e.resource;
      });
    });

    g.error(function(data, status, headers, config) {
      alert("HTTP error: " + status);
    });
  };

  $scope.showModal = function(vs) {
    $scope.currentVs = vs;
    $scope.selectedCoding = null;

    $scope.itemsFn = function(lookupText) {
      return $q(function(resolve, reject) {
        var g = $http.get("/ValueSet/" + vs.id + "/$expand",
                          {params: {filter: lookupText}});

        g.success(function(resource) {
          console.log("Received expansion for", vs.id);

          if (resource.resourceType == 'OperationOutcome') {
            reject(resource.issue[0].details);
          } else {
            resolve(resource.expansion.contains);
          }
        });

        g.error(function(data) {
          reject([]);
        })
      });
    };

    console.log(vs);
  };

  $scope.closeModal = function() {
    $scope.currentVs = null;
  }
}]);
