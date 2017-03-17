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
        controllerAs : 'controller',
        title : 'Thought Bubble | Home'
    }).when('/login', {
        templateUrl : 'login.html',
        controller : 'login',
        controllerAs : 'controller',
        title : 'Thought Bubble | Login'
    }).when('/register', {
        templateUrl : 'register.html',
        controller : 'register',
        controllerAs : 'controller',
        title : 'Thought Bubble | Register'
    }).when('/user-profile/:username', {
        templateUrl : 'profile.html',
        controller : 'profile',
        controllerAs : 'controller',
        title : 'Thought Bubble | Profile'
    });

    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    $httpProvider.defaults.headers.common['Connection'] = 'close';
});

thoughtBubbleApp.run(['$rootScope', '$route', function ($rootScope, $route) {
    $rootScope.$on('$routeChangeSuccess', function () {
        $rootScope.pageTitle = $route.current.title;
    });
}]);

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

thoughtBubbleApp.controller('nav', function ($http, $location, $scope, $rootScope) {

    $scope.goHome = function () {
        $location.path("/");
    };

    $scope.goToLogin = function () {
        $location.path("/login");
    };

    $scope.goToRegister = function () {
        $location.path("/register");
    };

    $scope.goToProfile = function () {
        $location.path("/user-profile/" + $rootScope.username);
    };

    $scope.goToLogout = function () {

    };
});

thoughtBubbleApp.controller('home', ['$http', '$location', '$scope', '$rootScope', 'STOMPService', '$timeout', '$route', function ($http, $location, $scope, $rootScope, STOMPService, $timeout, $route) {
    $http.get('user', {headers : {}}).then(function(response) {

        if (response.data.principal.name) {

            //use principal response object to build Basic auth headers
            $rootScope.authHeaders = {authorization : "Basic "
            + btoa(response.data.principal.name + ":" + response.data.principal.password)
            };

            $rootScope.authenticated = true;
            $rootScope.username = response.data.principal.name;
            authorities = response.data.authorities;

        } else {
            $rootScope.authenticated = false;
        }

    }, function() {
        $rootScope.authenticated = false;
    });

    var socketConnection = false;
    $scope.showCount = false;

    $scope.authUser = $rootScope.username;
    console.log($scope.authUser);

    $scope.payload = {
        postId: undefined,
        postDate: undefined,
        body: undefined,
        username: $rootScope.username
    };

    var clearPayload = function () {
        $scope.payload.postId = undefined;
        $scope.payload.postDate = undefined;
        $scope.payload.body = undefined;
    };

    var errorObject = {
        message: undefined
    };

    $scope.displayArray = [];
    $scope.errorResponses = [];
    var clearErrors = function () {
        $scope.errorResponses = [];
    };

    if (!socketConnection) {
        $http.get('getLatestPost', halHeader).then(function(response) {
            $scope.displayArray = response.data.entityList;
        });
    }

    if ($rootScope.authHeaders != null) {
        STOMPService.resetConnection();
    }

    $scope.sendMessage = function () {
        $scope.payload.username = $rootScope.username;
        var error = {};

        if ($scope.payload.body == undefined) {
            error = {
                error: "Thought cannot be blank."
            };
            console.log("thought is blank");
            clearErrors();
            $scope.errorResponses.push(error);
            
            $timeout(function () {
                clearErrors();
            }, 3000);
            clearPayload();
        } else {
            if ($scope.payload.body.length > 80) {
                error = {
                    error: "Thought is too long."
                };
                clearErrors();
                $scope.errorResponses.push(error);

                $timeout(function () {
                    clearErrors();
                }, 3000);
                clearPayload();
            } else {
                $http.post('persistThought', $scope.payload, halHeader).then(function (response) {
                    $scope.errorResponses = [];
                    $scope.payload.postId = response.data.postId;
                    $scope.payload.postDate = response.data.postDate;
                    STOMPService.send($scope.payload);
                    clearPayload();
                }, function (response) {
                    if (response.status == 400) {
                        $scope.errorResponses = response.data.fieldErrors;

                        $timeout(function () {
                            clearErrors();
                        }, 3000);
                    }
                });
            }
        }
    };

    $scope.favoritePost = function (postId, currentScope) {
        if ($rootScope.authenticated) {
            $http.get('incrementFavoriteCount?postId=' + postId).then(function (response) {
                currentScope.favoriteCount++;
            }, function (response) {
                errorObject.message = response.data.error;
                $scope.errorResponses.unshift(errorObject);

                $timeout(function () {
                    $scope.errorResponses = [];
                }, 3000);
            });
        }
    };

    $scope.deletePost = function (postId, currentObject) {
        $http.get('deleteThought?postId=' + postId).then(function (response) {
            $scope.displayArray.splice($scope.displayArray.indexOf(currentObject),1);
        }, function (response) {

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
}]);

thoughtBubbleApp.controller('login', function($rootScope, $http, $location, $scope, $timeout) {
    $rootScope.authHeaders = {};
    $scope.errorResponse = {
        error: null
    };
    $scope.credentials = {};

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

    $scope.login = function() {
        if ($scope.credentials.username == undefined || $scope.credentials.password == undefined) {
            $scope.errorResponse.error = "Username or password is blank.";
            $timeout(function () {
                $scope.errorResponse.error = null;
            }, 3000);
        } else {
            authenticate($scope.credentials, function () {
                if ($rootScope.authenticated) {
                    $location.path("/");
                } else {
                    $scope.credentials = {};
                    $scope.errorResponse.error = "Username or password is incorrect.";
                    $timeout(function () {
                        $scope.errorResponse.error = null;
                    }, 3000);
                }
            });
        }
    };
});

thoughtBubbleApp.controller('register', function($rootScope, $http, $location, $scope, $timeout) {
    $scope.userProfile = {};
    $scope.passConfirm = {};
    $scope.errorMessageArray = [];
    var errorObject = {};

    var clearInputs = function () {
        $scope.userProfile = {};
        $scope.passConfirm = {};
    };

    var headers = {authorization : "Basic "
    + btoa($scope.userProfile.username + ":" + $scope.userProfile.password)
    };

    $scope.registerUser = function() {
        if ($scope.userProfile.password == $scope.passConfirm.password) {
            $http.post('register-user', $scope.userProfile, halHeader).then(function (response) {
                $rootScope.authenticated = true;
                $rootScope.username = $scope.userProfile.username;
                $rootScope.authHeaders = headers;
                $location.path("/");
            }, function (response) {
                if (response.status == 409) {
                    errorObject.message = response.data.error;
                    $scope.errorMessageArray.push(errorObject);
                    $timeout(function () {
                        $scope.errorMessageArray = [];
                    }, 3000);
                    clearInputs();
                } else if (response.status == 400) {
                    $scope.errorMessageArray = response.data.fieldErrors;
                    $timeout(function () {
                        $scope.errorMessageArray = [];
                    }, 3000);
                    clearInputs();
                }
            });
        } else {
            errorObject.message = "Passwords do not match.";
            $scope.errorMessageArray.push(errorObject);
            $timeout(function () {
                $scope.errorMessageArray = [];
            }, 3000);
            clearInputs();
        }
    }
});

thoughtBubbleApp.controller('profile', function ($rootScope, $http, $location, $scope) {
    $scope.displayArray = [];

    if (!$rootScope.authenticated) {
        $location.path('/')
    }

    $http.get('getProfileData?username=' + $rootScope.username, halHeader).then(function (response) {
        $scope.displayArray = response.data.entityList;
        }, function (response) {
    });

    $scope.deletePost = function (postId, currentObject) {
        $http.get('deleteThought?postId=' + postId).then(function (response) {
            $scope.displayArray.splice($scope.displayArray.indexOf(currentObject),1);
        }, function (response) {

        });
    };
});

