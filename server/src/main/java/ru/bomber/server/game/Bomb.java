package ru.bomber.server.game;

import static ru.bomber.server.service.GameMechanics.renderTileSize;

// {"id":1,"type":"Bomb","position":{"y":20,"x":10}}
public class Bomb extends Positionable implements Tickable {

    // Lifetime = number of tick's in loop. Value in seconds is lifetime / 60 = 2 sec
    private int lifetime = 120;
    private int bombRange;
    private Pawn pawn;

    public Bomb(int id, Pawn pawn) {
        this.id = id;
        this.type = "Bomb";
        this.pawn = pawn;

        //bomb have to be placed in the center of nearest tile
        double bombY = Math.round(pawn.getY() / renderTileSize) * renderTileSize;
        double bombX = Math.round(pawn.getX() / renderTileSize) * renderTileSize;
        this.position = new Point(bombY, bombX);
        this.bombRange = pawn.getBombRange();
    }

    @Override
    public void tick() {
        lifetime--;
    }

    public int getLifetime() {
        return lifetime;
    }

    public int getBombRange() {
        return bombRange;
    }

    public Pawn getPawn() {
        return pawn;
    }
}
