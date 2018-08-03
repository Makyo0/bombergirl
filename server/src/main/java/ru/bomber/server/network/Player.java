package ru.bomber.server.network;

import org.springframework.web.socket.WebSocketSession;

public class Player {

    private String playerId;
    private int gameId;
    private WebSocketSession webSocketSession;

    public Player(int gameId, WebSocketSession session) {
        this.playerId = session.getId();
        this.gameId = gameId;
        this.webSocketSession = session;
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public String getPlayerId() {
        return playerId;
    }
}
