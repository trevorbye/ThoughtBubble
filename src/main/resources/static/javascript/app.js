var thoughtBubbleApp = angular.module('thoughtBubbleApp', ['ngRoute']);

var halHeader = {
    headers : {
        'Content-type': 'application/hal+json',
        'Connection' : 'close'
    }
};

thoughtBubbleApp.config(function ($routeProvider, $httpProvider) {
    $routeProvider.when('/', {
        templateUrl : 'home.html',
        controller : 'home',
        controllerAs : 'controller'
    }).when('/login', {
        templateUrl : 'login.html',
        controller : 'login',
        controllerAs : 'controller'
    }).when('/register', {
        templateUrl : 'register.html',
        controller : 'register',
        controllerAs : 'controller'
    });
});

thoughtBubbleApp.service('STOMPService', ['$q', '$timeout', '$rootScope', function ($q, $timeout, $rootScope) {

    var authHeaders = {};
    if ($rootScope.authHeaders == null) {
        authHeaders = {authorization : "Basic "
        + btoa("Null user." + ":" + "Null user.")
        };
    } else {
        authHeaders = $rootScope.authHeaders;
    }

    var service = {}, listener = $q.defer(), socket = {
        client: null,
        stomp: null
    }, messageIds = [];

    service.RECONNECT_TIMEOUT = 30000;
    service.SOCKET_URL = "/application-socket-conn";
    service.CHAT_TOPIC = "/main-page-feed/thought-queue";
    service.CHAT_BROKER = "/thought-bubble/push-to-queue";

    service.receive = function () {
        return listener.promise;
    };

    service.send = function(payload) {
        var id = Math.floor(Math.random() * 1000000);
        socket.stomp.send(service.CHAT_BROKER, {
            priority: 9
        }, JSON.stringify(
            payload
        ));
        messageIds.push(id);
    };

    service.resetConnection = function () {
        socket.client.close();
        initialize();
    };

    var reconnect = function() {
        $timeout(function () {
            initialize();
        }, this.RECONNECT_TIMEOUT);
    };

    var getMessage = function (data) {
        return JSON.parse(data);
    };

    var startListener = function () {
        socket.stomp.subscribe(service.CHAT_TOPIC, function (data) {
            listener.notify(getMessage(data.body));
        });
    };

    var initialize = function () {
        socket.client = new SockJS(service.SOCKET_URL);
        socket.stomp = Stomp.over(socket.client);

        //auth headers for spring security
        socket.stomp.connect(authHeaders, startListener);
        /*
        socket.stomp.onclose = reconnect;
        */

    };

    initialize();
    return service;
}]);

thoughtBubbleApp.controller('home', ['$http', '$location', '$scope', '$rootScope', 'STOMPService', function ($http, $location, $scope, $rootScope, STOMPService) {

    var socketConnection = false;
    $scope.showCount = false;
    $scope.bodyLengthCount = undefined;

    $scope.payload = {};
    $scope.payload.body = undefined;
    $scope.payload.username = $rootScope.username;

    $scope.displayArray = [];
    $scope.errorResponses = [];



    if (!socketConnection) {
        $http.get('getLatestPost', halHeader).then(function(response) {
            $scope.displayArray = response.data.entityList;
        });
    }

    if ($rootScope.authHeaders != null) {
        STOMPService.resetConnection();
    }

    //TODO persist thought with POST request to /persistThought first, then send over STOMP if it's valid upon return. This will allow using principal without STOMP errors
    $scope.sendMessage = function () {
        $http.post('persistThought', $scope.payload, halHeader).then(function (response) {
            //todo returned object doesn't map, must use setters to change scope object
            STOMPService.send($scope.payload);
        }, function (response) {
            if (response.status == 400) {
                $scope.errorResponses = response.data.fieldErrors;
            }
        });
    };

    STOMPService.receive().then(null,null, function (response) {
        //change socket connection variable to change view from latest post to most recent post pushed from SockJs connection
        socketConnection = true;
        if ($scope.displayArray.length >= 5){
            $scope.displayArray.pop();
        }

        $scope.displayArray.unshift(response);
    });

    $scope.goToLogin = function () {
        $location.path("/login");
    };

    $scope.goToRegister = function () {
        $location.path("/register");
    };
}]);

thoughtBubbleApp.controller('login', function($rootScope, $http, $location, $scope) {
    $rootScope.authHeaders = {};

    var authenticate = function(credentials, callback) {

        var headers = credentials ? {authorization : "Basic "
        + btoa(credentials.username + ":" + credentials.password)
        } : {};

        $http.get('user', {headers : headers}).then(function(response) {

            console.log(headers.toString());

            if (response.data.principal.name) {
                $rootScope.authenticated = true;
                $rootScope.username = response.data.principal.name;
                //save auth headers in rootScope to use in websocket connection
                $rootScope.authHeaders = headers;
                authorities = response.data.authorities;
            } else {
                $rootScope.authenticated = false;
            }
            callback && callback();
        }, function() {
            $rootScope.authenticated = false;
            callback && callback();
        });
    };

    authenticate();
    $scope.error = false;
    $scope.credentials = {};
    $scope.login = function() {
        authenticate($scope.credentials, function() {
            if ($rootScope.authenticated) {
                $location.path("/");
            } else {
                $scope.credentials = {};
                $scope.error = true;
            }
        });
    };
});

thoughtBubbleApp.controller('register', function($rootScope, $http, $location, $scope) {
    $scope.userProfile = {};
    $scope.passConfirm = {};
    $scope.errorMessageArray = [];
    var errorObject = {};

    var clearInputs = function () {
        $scope.userProfile = {};
        $scope.passConfirm = {};
    };


    $scope.registerUser = function() {
        if ($scope.userProfile.password == $scope.passConfirm.password) {
            $http.post('register-user', $scope.userProfile, halHeader).then(function (response) {
                $rootScope.authenticated = true;
                $location.path("/");
            }, function (response) {
                if (response.status == 409) {
                    errorObject.message = response.data.error;
                    $scope.errorMessageArray.push(errorObject);
                    clearInputs;
                } else if (response.status == 400) {
                    $scope.errorMessageArray = response.data.fieldErrors;
                    clearInputs;
                }
            });
        } else {
            errorObject.message = "Passwords do not match.";
            $scope.errorMessageArray.push(errorObject);
            clearInputs;
        }
    }
});

