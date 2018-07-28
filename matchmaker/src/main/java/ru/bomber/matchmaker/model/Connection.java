package ru.bomber.matchmaker.model;

public class Connection {

    private Integer playerId = 0;
    private final String name;

    public Connection(String name) {
        this.playerId++;
        this.name = name;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "playerId=" + playerId +
                ", name='" + name + '\'' +
                '}';
    }
}
