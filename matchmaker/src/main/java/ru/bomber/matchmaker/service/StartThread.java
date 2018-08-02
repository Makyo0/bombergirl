package ru.bomber.matchmaker.service;

public class StartThread implements Runnable {

    private String gameId;
    private int counter;
    private static final int MAX_PLAYER_IN_GAME = 2;
    private boolean isRunning = true;

    public StartThread(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public void run() {

        try {
            while (counter < 1000 && isRunning) {
                int numberOfPlayersJoined = Integer.valueOf(MmRequester.checkStatus(gameId).body().string());
                if (numberOfPlayersJoined == MAX_PLAYER_IN_GAME) {
                    synchronized (this) {
                        MmRequester.start(gameId);
                        System.out.println("Starting game " + gameId);
                        for (int i = 0; i < MAX_PLAYER_IN_GAME; i++) {
                            ConnectionQueue.getInstance().take();
                        }
                        isRunning = false;
                    }
                } else {
                    Thread.sleep(100);
                    counter++;
                }
            }
            if (isRunning) {
                System.out.println("Not enough players");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
