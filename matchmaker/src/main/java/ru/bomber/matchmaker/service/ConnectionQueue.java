package ru.bomber.matchmaker.service;

import ru.bomber.matchmaker.model.Connection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionQueue {

    private static BlockingQueue<Connection> instance = new LinkedBlockingQueue<>();

    public static BlockingQueue<Connection> getInstance() {
        return instance;
    }
}
