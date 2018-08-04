package ru.bomber.server.game;

// {"id":1,"type":"Bomb","position":{"y":20,"x":10}}
public class Bomb extends Positionable implements Tickable {

    // Lifetime = number of tick's in loop. Value in seconds is lifetime / 60 = 2 sec
    private int lifetime = 120;

    public Bomb(int id, double y, double x) {
        this.id = id;
        this.position = new Point(y, x);
        this.type = "Bomb";
    }

    @Override
    public void tick() {
        lifetime--;
    }

    public int getLifetime() {
        return lifetime;
    }

}
