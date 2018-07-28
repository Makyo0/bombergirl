package ru.bomber.server.network;

import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.atomic.AtomicInteger;

public class Player {

    private static AtomicInteger atomicInteger = new AtomicInteger();
    private int playerId;
    private int gameId;
    private WebSocketSession webSocketSession;

    public Player(int gameId, WebSocketSession session) {
        this.playerId = atomicInteger.getAndIncrement();
        this.gameId = gameId;
        this.webSocketSession = session;
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public void setWebSocketSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getPlayerId() {
        return playerId;
    }
}
