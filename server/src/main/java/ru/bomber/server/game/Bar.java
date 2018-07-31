package ru.bomber.server.game;

public class Bar {

    private Point barPoint1;
    private Point barPoint2;

    public Bar(int y1, int x1, int y2, int x2) {
        this.barPoint1 = new Point(y1, x1);
        this.barPoint2 = new Point(y2, x2);
    }

    public Point getBarPoint1() {
        return barPoint1;
    }

    public Point getBarPoint2() {
        return barPoint2;
    }
}
