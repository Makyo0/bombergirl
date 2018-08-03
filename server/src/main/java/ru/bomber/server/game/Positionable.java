package ru.bomber.server.game;

public abstract class Positionable {

    protected int id;
    protected String type;
    protected Point position;

    public int getId() {
        return id;
    }

    public void setX(double x) {
        this.position.setX(x);
    }

    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
    }

    public void setY(double y) {
        this.position.setY(y);
    }
}
