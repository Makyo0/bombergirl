package ru.bomber.matchmaker.service;

public class StartThread implements Runnable {

    private String gameId;
    private static final int MAX_PLAYER_IN_GAME = 4;
    private boolean isRunning;

    public StartThread(String gameId) {
        this.gameId = gameId;
        this.isRunning = true;
    }

    @Override
    public void run() {

        try {
            long startTime = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            while ((currentTime - startTime) < 60000 && isRunning) {
                int numberOfPlayersJoined = Integer.valueOf(MmRequester.checkStatus(gameId).body().string());
                if (numberOfPlayersJoined == MAX_PLAYER_IN_GAME) {
                    synchronized (this) {
                        MmRequester.start(gameId);
                        System.out.println("Starting game " + gameId);
                        ConnectionQueue.getInstance().clear();
                        isRunning = false;
                    }
                } else {
                    currentTime = System.currentTimeMillis();
                }
            }
            if (isRunning) {
                synchronized (this) {
                    System.out.println("Not enough players connected, please try again later");
                    ConnectionQueue.getInstance().clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
