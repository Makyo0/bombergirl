package ru.bomber.server.service;

import org.springframework.web.socket.TextMessage;
import ru.bomber.server.message.Message;
import ru.bomber.server.message.Topic;
import java.util.Collection;

public class GameThread implements Runnable {

    private String gameId;

    public GameThread(String gameId) {
        this.gameId = gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public void run() {

        GameMechanics gameMechanics = GameService.getGameMechanics(gameId);
        gameMechanics.initGame(gameId);

        double FPS = 60.0;
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        boolean running = true;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                //Tickable tick();
                delta--;
            }
            if (running) {
                gameMechanics.doMechanics();
                Collection replicaToSend = GameService.getReplica(gameId).values();
                Message msg = new Message(Topic.REPLICA, JsonHelper.toJson(replicaToSend));
                TextMessage message = new TextMessage(JsonHelper.toJson(msg));
//              System.out.println("Sending message " + message.getPayload());
                GameService.broadcast(Integer.parseInt(gameId), message);
            }
            frames++;
            try {
                Thread.sleep(Math.round(1000 / FPS));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
            }
        }
        Thread.currentThread().interrupt();
    }
}
