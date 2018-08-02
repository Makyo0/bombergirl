package ru.bomber.server.game;

public abstract class Positionable {

    protected int id;
    protected String type;
    protected Point position;

    public int getId() {
        return id;
    }

    public void setX(int x) {
        this.position.setX(x);
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }

    public void setY(int y) {
        this.position.setY(y);
    }
}
