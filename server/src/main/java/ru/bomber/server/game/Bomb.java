package ru.bomber.server.game;


// Вид объекта в реплике
// {"id":1,"type":"Bomb","position":{"y":20,"x":10}}
public class Bomb implements Tickable {
    private int id;
    private String type = "Bomb";
    private Point position;
    private long lifetime = 100;

    public Bomb(int id, int y, int x) {
        this.id = id;
        this.position = new Point(y, x);
    }

    @Override
    public void tick(long elapsed) {
        lifetime -= elapsed;
    }

    public long getLifetime() {
        return lifetime;
    }

    public int getId() {
        return id;
    }
}
