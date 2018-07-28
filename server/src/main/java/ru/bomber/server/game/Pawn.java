package ru.bomber.server.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.bomber.server.message.Direction;

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

    public int getY() {
        return position.y;
    }

    public void setY(int y) {
        this.position.y = y;
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

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
