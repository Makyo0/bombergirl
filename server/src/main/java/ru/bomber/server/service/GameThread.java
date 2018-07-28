package ru.bomber.server.service;

import org.springframework.web.socket.TextMessage;
import ru.bomber.server.game.Pawn;
import ru.bomber.server.game.Point;
import ru.bomber.server.message.Message;
import ru.bomber.server.message.Topic;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameThread implements Runnable {

    private String gameId;
    private AtomicInteger pawnGenerator = new AtomicInteger();
    private AtomicInteger replicaGenerator = new AtomicInteger();

    public GameThread(String gameId) {
        this.gameId = gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public void run() {

        System.out.println("Starting new game");
        Pawn pawn1 = new Pawn(pawnGenerator.getAndIncrement(), new Point(20, 10));
        pawn1.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(0).getPlayerId());
        Pawn pawn2 = new Pawn(pawnGenerator.getAndIncrement(), new Point(20, 90));
        pawn2.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(1).getPlayerId());

        ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(gameId);

        replica.put(replicaGenerator.getAndIncrement(), pawn1);
        replica.put(replicaGenerator.getAndIncrement(), pawn2);

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
                Collection replicaToSend = replica.values();
                Message msg = new Message(Topic.REPLICA, JsonHelper.toJson(replicaToSend));
                TextMessage message = new TextMessage(JsonHelper.toJson(msg));
//                System.out.println("Sending message " + message.getPayload());
                GameService.broadcast(Integer.parseInt(gameId), message);
            }
            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
            }
        }
        Thread.currentThread().interrupt();
    }
}
