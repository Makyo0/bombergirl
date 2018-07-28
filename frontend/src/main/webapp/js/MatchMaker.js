var MatchMaker = function (clusterSetting) {
    this.settings = {
        url: clusterSetting.matchMakerUrl(),
        method: "POST",
        crossDomain: true,
        async: false
    };
};

MatchMaker.prototype.connect = function (gameId) {
    this.serverProxy = new ServerProxy();
    this.serverProxy.connectToGameServer(gameId);

}
MatchMaker.prototype.getSessionId = function () {
    var name = Math.floor((1 + Math.random()) * 0x10000)
        .toString(16)
        .substring(1);

    this.settings.data = name;
    var matchMakerUrl = "localhost:8080/matchmaker/join";

    $.ajax({
        contentType: 'application/x-www-form-urlencoded',
        data: {
            'name': name
        },
        body: {},
        dataType: 'text',
        processData: true,
        type: 'POST',
        url: "http://" + matchMakerUrl,
        success: function (data) {
            console.log("Joined in matchmaker gameId=" + data);
            gMatchMaker.connect(data);
        },
        error: function () {
            alert("Matchmaker request failed");
            console.log("Matchmaker request failed");
        },
    });
};

gMatchMaker = new MatchMaker(gClusterSettings);