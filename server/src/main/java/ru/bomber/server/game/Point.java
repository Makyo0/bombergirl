package ru.bomber.server.game;

import java.util.Objects;

public class Point {

    private double y;
    private double x;

    public Point(double y, double x) {
        this.y = y;
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }
}
