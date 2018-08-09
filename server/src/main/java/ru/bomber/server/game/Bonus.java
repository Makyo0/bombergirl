package ru.bomber.server.game;

import ru.bomber.server.message.BonusType;

// Вид объекта в реплике
// {"id":2,"type":"Bonus","position":{"y":20,"x":10},"bonusType":"BOMBS"}
// {"id":3,"type":"Bonus","position":{"y":20,"x":10},"bonusType":"SPEED"}
// {"id":4,"type":"Bonus","position":{"y":20,"x":10},"bonusType":"RANGE"}
public class Bonus extends Positionable {

    private BonusType bonusType;

    public Bonus(int id, double y, double x, BonusType bonusType) {
        this.id = id;
        this.position = new Point(y, x);
        this.type = "Bonus";
        this.bonusType = bonusType;
    }

    public BonusType getBonusType() {
        return bonusType;
    }
}
