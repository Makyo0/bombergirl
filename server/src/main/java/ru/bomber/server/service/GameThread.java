package ru.bomber.server.service;

public class GameThread extends Thread {

    private String gameId;
    private boolean running = true;
    private GameMechanics gameMechanics;

    public GameThread(String gameId) {
        this.gameId = gameId;
        gameMechanics = new GameMechanics(gameId);
        gameMechanics.initGame(gameId);
    }

    public GameMechanics getGameMechanics() {
        return gameMechanics;
    }

    @Override
    public void run() {

        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;

        while (running) {

            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                gameMechanics.tick();
                gameMechanics.render();
                frames++;
                delta--;
            }
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
            }
        }
    }
}

