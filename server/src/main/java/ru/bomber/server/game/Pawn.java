package ru.bomber.server.game;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Pawn extends Positionable {

    @JsonIgnore
    private int playerId;

    private boolean alive = true;
    private String direction = "";

    public Pawn(int id, int y, int x) {
        this.id = id;
        this.position = new Point(y, x);
        this.type = "Pawn";
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
