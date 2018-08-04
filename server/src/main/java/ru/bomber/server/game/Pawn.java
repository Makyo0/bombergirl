package ru.bomber.server.game;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Pawn extends Positionable implements Tickable {

    @JsonIgnore
    private String playerId;

    private boolean alive = true;
    private String direction = "";
    private double velocity = 1.00;

    public Pawn(int id, double y, double x) {
        this.id = id;
        this.position = new Point(y, x);
        this.type = "Pawn";
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    @Override
    public void tick() {
        direction = "";
    }
}
