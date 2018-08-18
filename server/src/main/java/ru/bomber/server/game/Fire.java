package ru.bomber.server.game;

import java.util.Objects;

// {"id":5,"type":"Fire","position":{"y":20,"x":10}}
public class Fire extends Positionable implements Tickable {

    private int lifetime = 60;

    public Fire(int id, double y, double x) {
        this.id = id;
        this.position = new Point(y, x);
        this.type = "Fire";
    }

    @Override
    public void tick() {
        lifetime--;
    }

    public int getLifetime() {
        return lifetime;
    }
}
