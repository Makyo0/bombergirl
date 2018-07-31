package ru.bomber.server.service;

import org.springframework.web.socket.TextMessage;
import ru.bomber.server.game.Bomb;
import ru.bomber.server.game.Pawn;
import ru.bomber.server.game.Tickable;
import ru.bomber.server.message.Message;
import ru.bomber.server.message.Topic;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class GameThread extends Thread {

    private String gameId;
    private boolean running = true;
    private GameMechanics gameMechanics;

    public GameThread(String gameId) {
        this.gameId = gameId;
        gameMechanics = new GameMechanics(gameId);
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public GameMechanics getGameMechanics() {
        return gameMechanics;
    }

    @Override
    public void run() {

        gameMechanics.initGame(gameId);

        double FPS = 60.0;
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
                for (Tickable tickable :
                        gameMechanics.getTickables()) {
                    tickable.tick((long) delta);
                }
                delta--;
            }

            if (running) {
                gameMechanics.doMechanics();
                ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(gameId);
                Collection replicaToSend = replica.values();
                Message msg = new Message(Topic.REPLICA, JsonHelper.toJson(replicaToSend));
                TextMessage message = new TextMessage(JsonHelper.toJson(msg));
                System.out.println("Sending message " + message.getPayload() + "to gameId=" + gameId);
                GameService.broadcast(Integer.parseInt(gameId), message);
                //object check
                for (Object object :
                        replica.values()) {
                    //resetting direction binding of Pawn's
                    if (object instanceof Pawn) {
                        ((Pawn) object).setDirection("");
                    }
                    //using object loop to check bomb's status
                    if (object instanceof Bomb) {
                        if (((Bomb) object).getLifetime() <= 0) {
                            replica.remove(((Bomb) object).getId());
                        }
                    }
                }
                frames++;
            }

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
            }

            try {
                Thread.sleep(Math.round(1000 / FPS));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
