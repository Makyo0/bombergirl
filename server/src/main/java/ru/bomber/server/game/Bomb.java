package ru.bomber.server.game;


// Вид объекта в реплике
// {"id":1,"type":"Bomb","position":{"y":20,"x":10}}
public class Bomb extends Positionable implements Tickable {

    private long lifetime = 100;

    public Bomb(int id, int y, int x) {
        this.id = id;
        this.position = new Point(y, x);
        this.type = "Bomb";
    }

    @Override
    public void tick(long elapsed) {
        lifetime -= elapsed;
    }

    public long getLifetime() {
        return lifetime;
    }

}
