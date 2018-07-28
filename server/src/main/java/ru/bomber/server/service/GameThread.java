package ru.bomber.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import ru.bomber.server.game.Pawn;
import ru.bomber.server.game.Point;
import ru.bomber.server.message.Message;
import ru.bomber.server.message.Topic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope("prototype")
public class GameThread implements Runnable {

    private String gameId;
    private static AtomicInteger pawnGenerator = new AtomicInteger();
    private static AtomicInteger replicaGenerator = new AtomicInteger();

    @Autowired
    private GameService gameService;

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public void run() {

        System.out.println("Starting new game");
        Pawn pawn1 = new Pawn(pawnGenerator.getAndIncrement(), new Point(20, 10));
        Pawn pawn2 = new Pawn(pawnGenerator.getAndIncrement(), new Point(20, 90));

        ConcurrentHashMap<Integer, Object> replica = gameService.getReplica(gameId);

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
                gameService.broadcast(Integer.parseInt(gameId), message);
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
