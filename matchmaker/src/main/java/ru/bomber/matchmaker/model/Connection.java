package ru.bomber.matchmaker.model;

public class Connection {

    private final String name;

    public Connection(String name) {
        this.name = name;
    }



    @Override
    public String toString() {
        return "Connection{" +
                "playerId=" +
                ", name='" + name + '\'' +
                '}';
    }
}
