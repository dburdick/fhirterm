var bench = angular.module('bench',[]);

bench.controller('BenchCtrl', ['$scope', '$http', '$sce', function($scope, $http, $sce) {
    $scope.avg = function(d){
        var sum = 0;
        for (var i = 0; i < d.length; i++) { sum += d[i]; }

        return Math.round(sum / d.length);
    };

    var extractErrorMessage = function(d) {
        var re = /<div class="message">([^<]+)<\/div>/;
        var errorMsg = d.match(re)[1] || "Unknown error";

        return $sce.trustAsHtml("<strong>ERROR:</strong> " + errorMsg);
    };

    $scope.fhirServerUrl = 'http://localhost:3000';
    $scope.expansionsPerVs = 3;
    $scope.results = null;
    $scope.started = false;

    $scope.displayTimes = function(times) {
        if (!times || times.length == 0) { return ""; }

        var s = times.map(function (t) {
            return String(t) + "ms"
        }).join(" / ");

        return $sce.trustAsHtml("<strong>" + $scope.avg(times) + "ms</strong> / <span class='text-muted'>" + s + "</span>");
    };

    $scope.performExpands = function() {
        var current = $scope.results.filter(function(e) {
            return e.status == 'pending';
        })[0];

        if (!current) {
            return;
        }

        current.status = 'started';
        current.message = $sce.trustAsHtml('Expanding...');
        current.times = [];

        var justDoIt = function() {
            var startedAt = new Date();
            var g = $http.get("/ValueSet/" + current.id + "/$expand");

            g.success(function(data) {
                var time = new Date() - startedAt;

                if (data.resourceType == "OperationOutcome") {
                    current.status = 'error';
                    current.message = $sce.trustAsHtml("<strong>" + data.issue[0].type.code + ":</strong> " + data.issue[0].details);
                    $scope.performExpands();
                } else {
                    current.times.push(time);
                    current.size = data.expansion.contains.length;

                    if (current.times.length == $scope.expansionsPerVs) {
                        current.status = 'finished';
                        current.message = $sce.trustAsHtml("OK");

                        $scope.performExpands();
                    } else {
                        justDoIt();
                    }
                }
            });

            g.error(function(data) {
                current.status = 'error';
                current.message = extractErrorMessage(data);
                $scope.performExpands();
            });
        };

        justDoIt();
    };

    $scope.start = function() {
        $scope.started = true;
        var g = $http.get($scope.fhirServerUrl + '/ValueSet');

        g.success(function(data, status, headers, config) {
            $scope.results = data.entry.map(function(e) {
                return {
                    "id": e.resource.id,
                    "status": "pending"
                };
            });

            $scope.performExpands();
        });

        g.error(function(data, status, headers, config) {
            alert("HTTP error: " + status);
        });
    };
}]);
