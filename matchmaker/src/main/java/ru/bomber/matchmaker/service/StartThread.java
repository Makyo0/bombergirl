package ru.bomber.matchmaker.service;

public class StartThread implements Runnable {

    private String gameId;
    private int counter;
    public static final int MAX_PLAYER_IN_GAME = 2;

    public StartThread(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public void run() {

        try {
            while (counter < 1000) {
                if (ConnectionQueue.getInstance().size() >= MAX_PLAYER_IN_GAME) {
                    synchronized (this) {
                        MmRequester.start(gameId);
                        System.out.println("Starting game " + gameId);
                        for (int i = 0; i < MAX_PLAYER_IN_GAME; i++) {
                            ConnectionQueue.getInstance().take();
                        }
                        Thread.currentThread().interrupt();
                    }
                } else {
                    Thread.sleep(100);
                    counter++;
                }
            }
            System.out.println("Not enough players");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
