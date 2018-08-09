package ru.bomber.server.game;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Pawn extends Positionable implements Tickable {

    @JsonIgnore
    private String playerId;

    private boolean alive = true;
    private String direction = "";
    private double velocity = 0.8;
    private int availableBombs = 1;
    private int bombRange = 1;

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

    public int getAvailableBombs() {
        return availableBombs;
    }

    public void setAvailableBombs(int availableBombs) {
        this.availableBombs = availableBombs;
    }

    public int getBombRange() {
        return bombRange;
    }

    public void setBombRange(int bombRange) {
        this.bombRange = bombRange;
    }

    @Override
    public void tick() {
        direction = "";
    }
}
