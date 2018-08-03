package ru.bomber.server.game;

public class Bar {

    public final double leftX;
    public final double rightX;
    public final double bottomY;
    public final double topY;

    public Bar(double x1, double x2, double y1, double y2) {
        leftX = Math.min(x1, x2);
        bottomY = Math.min(y1, y2);
        rightX = Math.max(x1, x2);
        topY = Math.max(y1, y2);
    }

    public boolean collideCheck(Bar barToCheck) {

        boolean x1 = ((barToCheck.leftX >= leftX) && (barToCheck.leftX <= rightX));
        boolean x2 = ((barToCheck.rightX >= leftX) && (barToCheck.rightX <= rightX));
        boolean y1 = ((barToCheck.bottomY >= bottomY) && (barToCheck.bottomY <= topY));
        boolean y2 = ((barToCheck.topY >= bottomY) && (barToCheck.topY <= topY));
        return (x1 || x2) && (y1 || y2);
    }
}
