package ru.bomber.server.game;

// {"id":1,"type":"Bomb","position":{"y":20,"x":10}}
public class Bomb extends Positionable implements Tickable {

    // Lifetime = number of tick's in loop. Value in seconds is lifetime * (1 sec / FPS) = 1.6 sec
    private long lifetime = 100;

    public Bomb(int id, int y, int x) {
        this.id = id;
        this.position = new Point(y, x);
        this.type = "Bomb";
    }

    @Override
    public void tick() {
        lifetime--;
    }

    public long getLifetime() {
        return lifetime;
    }

}
