package ru.bomber.server.game;

// {"id":6,"type":"Wall","position":{"y":20,"x":10}}

public class Wall extends Positionable {

    public Wall(int id, double y, double x) {
        this.id = id;
        this.position = new Point(y, x);
        this.type = "Wall";
    }
}
