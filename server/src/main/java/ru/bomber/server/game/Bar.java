package ru.bomber.server.game;

public class Bar {

    public final int leftX;
    public final int rightX;
    public final int bottomY;
    public final int topY;

    public Bar(int x1, int x2, int y1, int y2) {
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
