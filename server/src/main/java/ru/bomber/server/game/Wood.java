package ru.bomber.server.game;

// {"id":6,"type":"Wood","position":{"y":20,"x":10}}

public class Wood extends Positionable {

    public Wood(int id, int y, int x) {
        this.id = id;
        this.position = new Point(y, x);
        this.type = "Wood";
    }
}
