package ru.bomber.server.game;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Pawn {

    @JsonIgnore
    private int playerId;

    private int id;
    private String type = "Pawn";
    private Point position;
    private boolean alive = true;
    private String direction = "";

    public Pawn(int id, Point position) {
        this.id = id;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public void setX(int x) {
        this.position.x = x;
    }

    public int getX() {
        return position.x;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
}
