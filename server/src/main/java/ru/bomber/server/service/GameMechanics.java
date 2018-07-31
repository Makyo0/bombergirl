package ru.bomber.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.bomber.server.game.*;
import ru.bomber.server.message.Direction;
import ru.bomber.server.message.InputQueueMessage;
import ru.bomber.server.message.Topic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GameMechanics {

    private String gameId;

    private AtomicInteger objectIdGenerator = new AtomicInteger();
    private LinkedBlockingQueue<InputQueueMessage> inputQueue = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<Tickable> tickables = new LinkedBlockingQueue<>();

    public GameMechanics(String gameId) {
        this.gameId = gameId;
    }

    public void initGame(String gameId) {
        System.out.println("Starting new game");
        Pawn pawn1 = new Pawn(objectIdGenerator.getAndIncrement(), 20, 40);
        Pawn pawn2 = new Pawn(objectIdGenerator.getAndIncrement(), 20, 90);
        pawn1.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(0).getPlayerId());
        pawn2.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(1).getPlayerId());
        ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(gameId);
        replica.put(pawn1.getPlayerId(), pawn1);
        replica.put(pawn2.getPlayerId(), pawn2);
        for (int i = 0; i <= 384; i = i + 32) {
            Wall leftWall = new Wall(objectIdGenerator.getAndIncrement(), i, 0);
            Wall rightWall = new Wall(objectIdGenerator.getAndIncrement(), i, 512);
            replica.put(leftWall.getId(), leftWall);
            replica.put(rightWall.getId(), rightWall);
        }
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
                                replica.put(bomb.getId(), bomb);
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
