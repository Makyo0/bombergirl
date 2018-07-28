package ru.bomber.server.game;

public class Pawn {

    private int id;
    private String type = "Pawn";
    private Point position;
    private boolean alive = true;

    public int getId() {
        return id;
    }

    private String direction = "";

    public Pawn(int id, Point position) {
        this.id = id;
        this.position = position;
    }

    public void setX(int x) {
        this.position.x = x;
    }

    public int getX() {
        return position.x;
    }
}
