package ru.bomber.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.bomber.server.game.Bomb;
import ru.bomber.server.game.Pawn;
import ru.bomber.server.game.Point;
import ru.bomber.server.game.Tickable;
import ru.bomber.server.message.Direction;
import ru.bomber.server.message.InputQueueMessage;
import ru.bomber.server.message.Topic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GameMechanics {

    private int gameId;

    private static AtomicInteger objectIdGenerator = new AtomicInteger();
    private static AtomicInteger replicaIdGenerator = new AtomicInteger();
    private LinkedBlockingQueue<InputQueueMessage> inputQueue = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<Tickable> tickables = new LinkedBlockingQueue<>();

    public GameMechanics(int gameId) {
        this.gameId = gameId;
    }

    public void initGame(String gameId) {
        System.out.println("Starting new game");
        Pawn pawn1 = new Pawn(objectIdGenerator.getAndIncrement(), new Point(20, 10));
        pawn1.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(0).getPlayerId());
        Pawn pawn2 = new Pawn(objectIdGenerator.getAndIncrement(), new Point(20, 90));
        pawn2.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(1).getPlayerId());
        ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(gameId);
        replica.put(replicaIdGenerator.getAndIncrement(), pawn1);
        replica.put(replicaIdGenerator.getAndIncrement(), pawn2);
    }

    public synchronized void doMechanics() {
        while (!inputQueue.isEmpty()) {
            try {
                InputQueueMessage queueMessage = inputQueue.take();
                JsonNode message = JsonHelper.getJsonNode(queueMessage.getPayload());
                Topic topic = Topic.valueOf(message.findValue("topic").asText());
                if (topic == Topic.MOVE) {
                    Direction direction = Direction.valueOf(message.findValue("direction").asText());
                    int pawnPlayerId = Integer.valueOf(queueMessage.getPlayerId());
                    ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(String.valueOf(gameId));
                    for (Object object :
                            replica.values()) {
                        if (object instanceof Pawn) {
                            Pawn pawn = (Pawn) object;
                            if (pawn.getPlayerId() == pawnPlayerId) {
                                if (direction == Direction.UP) {
                                    pawn.setY(pawn.getY() + 1);
                                    pawn.setDirection(Direction.UP.toString());
                                }
                                if (direction == Direction.DOWN) {
                                    pawn.setY(pawn.getY() - 1);
                                    pawn.setDirection(Direction.DOWN.toString());
                                }
                                if (direction == Direction.RIGHT) {
                                    pawn.setX(pawn.getX() + 1);
                                    pawn.setDirection(Direction.RIGHT.toString());
                                }
                                if (direction == Direction.LEFT) {
                                    pawn.setX(pawn.getX() - 1);
                                    pawn.setDirection(Direction.LEFT.toString());
                                }
                            }
                        }
                    }
                }
                if (topic == Topic.PLANT_BOMB) {
                    int pawnPlayerId = Integer.valueOf(queueMessage.getPlayerId()); //по реализации сервера sessionId == playerId
                    ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(String.valueOf(gameId));
                    for (Object object :
                            replica.values()) {
                        if (object instanceof Pawn) {
                            Pawn pawn = (Pawn) object;
                            if (pawn.getPlayerId() == pawnPlayerId) {
                                Bomb bomb = new Bomb(objectIdGenerator.getAndIncrement(), pawn.getY(), pawn.getX());
                                replica.put(replicaIdGenerator.getAndIncrement(), bomb);
                                tickables.offer(bomb);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public LinkedBlockingQueue<InputQueueMessage> getInputQueue() {
        return inputQueue;
    }

    public LinkedBlockingQueue<Tickable> getTickables() {
        return tickables;
    }
}
