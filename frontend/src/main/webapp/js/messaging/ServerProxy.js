var ServerProxy = function () {
    this.handler = {
        'REPLICA': gMessageBroker.handleReplica,
        'POSSESS': gMessageBroker.handlePossess,
        'GAME_OVER': gMessageBroker.handleGameOver
    };
};

ServerProxy.prototype.setupMessaging = function() {
    var self = this;
    gInputEngine.subscribe('up', function () {
        self.socket.send(gMessageBroker.move('up'))
    });
    gInputEngine.subscribe('down', function () {
        self.socket.send(gMessageBroker.move('down'))
    });
    gInputEngine.subscribe('left', function () {
        self.socket.send(gMessageBroker.move('left'))
    });
    gInputEngine.subscribe('right', function () {
        self.socket.send(gMessageBroker.move('right'))
    });
    gInputEngine.subscribe('bomb', function () {
        self.socket.send(gMessageBroker.plantBomb());
    });
    gInputEngine.subscribe('jump', function () {
        self.socket.send(gMessageBroker.jump());
    });
};

// Присоединяемся к серверу по сокету
// у WebSocketSession в Java имеется функция getURI() с помощью которого этот "Хвост" получается
ServerProxy.prototype.connectToGameServer = function(gameId) {

    var self = this;

    this.socket = new WebSocket(gClusterSettings.gameServerUrl() + "?gameId=" + gameId);

    this.socket.onmessage = function (event) {
        console.log('Had a message from server');
        var msg = JSON.parse(event.data);
        console.log('Message topic:' + msg.topic + ' payload:' + msg.data);
        if (self.handler[msg.topic] === undefined) {
            console.log('Undefined topic of message');
            return;
        }
        self.handler[msg.topic](msg);
    };

    this.socket.onopen = function () {
        console.log('WebSocket connection established');
    };

    this.socket.onclose = function (event) {
        console.log('Code: ' + event.code + ' cause: ' + event.reason);
    };

    this.socket.onerror = function (error) {
        console.log("Error " + error.message);
    };

    this.setupMessaging();
};
