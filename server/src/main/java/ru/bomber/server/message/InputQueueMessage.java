package ru.bomber.server.message;

public class InputQueueMessage {

    private String playerId;
    private String payload;

    public InputQueueMessage(String playerId, String payload) {
        this.playerId = playerId;
        this.payload = payload;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPayload() {
        return payload;
    }
}
