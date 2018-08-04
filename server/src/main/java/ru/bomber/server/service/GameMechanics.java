package ru.bomber.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.TextMessage;
import ru.bomber.server.game.*;
import ru.bomber.server.message.Direction;
import ru.bomber.server.message.InputQueueMessage;
import ru.bomber.server.message.Message;
import ru.bomber.server.message.Topic;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GameMechanics {

    private String gameId;
    private AtomicInteger objectIdGenerator = new AtomicInteger();
    private LinkedBlockingQueue<InputQueueMessage> inputQueue = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<Tickable> tickable = new LinkedBlockingQueue<>();
    private final static int tileSize = 29;
    private final static int playerSize = 24;

    public GameMechanics(String gameId) {
        this.gameId = gameId;
    }

    public void initGame(String gameId) {

        System.out.println("Starting new game");

        ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(gameId);

        //generating left/right borders of game field
        for (int i = 0; i <= 384; i += 32) {
            Wall leftWall = new Wall(objectIdGenerator.getAndIncrement(), i, 0);
            Wall rightWall = new Wall(objectIdGenerator.getAndIncrement(), i, 512);
            replica.put(leftWall.getId(), leftWall);
            replica.put(rightWall.getId(), rightWall);
        }

        //generating top/bottom borders of game field
        for (int i = 32; i <= 480; i += 32) {
            Wall topWall = new Wall(objectIdGenerator.getAndIncrement(), 0, i);
            Wall bottomWall = new Wall(objectIdGenerator.getAndIncrement(), 384, i);
            replica.put(topWall.getId(), topWall);
            replica.put(bottomWall.getId(), bottomWall);
        }

        //generating walls across game field
        for (int i = 64; i <= 480; i += 32 * 2) {
            for (int j = 64; j < 480; j += 32 * 2) {
                Wall wall = new Wall(objectIdGenerator.getAndIncrement(), i, j);
                replica.put(wall.getId(), wall);
            }
        }

        //generating player Pawn's
        Pawn pawn1 = new Pawn(objectIdGenerator.getAndIncrement(), 30, 33);
        Pawn pawn2 = new Pawn(objectIdGenerator.getAndIncrement(), 352, 33);
        pawn1.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(0).getPlayerId());
        pawn2.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(1).getPlayerId());
        replica.put(pawn1.getId(), pawn1);
        replica.put(pawn2.getId(), pawn2);
        tickable.offer(pawn1);
        tickable.offer(pawn2);
    }

    public void doMechanics() {
        while (!inputQueue.isEmpty()) {
            handleInputQueue();
        }
        for (Object object :
                GameService.getReplica(gameId).values()) {
            //using object iteration to check bomb's status
            if (object instanceof Bomb) {
                if (((Bomb) object).getLifetime() <= 0) {
                    GameService.getReplica(gameId).remove(((Bomb) object).getId());
                }
            }
        }
    }

    public void handleInputQueue() {
        try {
            InputQueueMessage queueMessage = inputQueue.take();
            JsonNode message = JsonHelper.getJsonNode(queueMessage.getPayload());
            Topic topic = Topic.valueOf(message.findValue("topic").asText());

            if (topic == Topic.MOVE) {

                Direction direction = Direction.valueOf(message.findValue("direction").asText());
                String pawnPlayerId = queueMessage.getPlayerId();
                ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(gameId);

                for (Object object :
                        replica.values()) {

                    if (object instanceof Pawn) {
                        Pawn pawn = (Pawn) object;

                        if (pawn.getPlayerId().equals(pawnPlayerId)) {

                            if (direction == Direction.UP) {
                                if (!isColliding(pawn.getY() + pawn.getVelocity(), pawn.getX())) {
                                    pawn.setY(pawn.getY() + pawn.getVelocity());
                                    pawn.setDirection(Direction.UP.toString());
                                }
                            }
                            if (direction == Direction.DOWN) {
                                if (!isColliding(pawn.getY() - pawn.getVelocity(), pawn.getX())) {
                                    pawn.setY(pawn.getY() - pawn.getVelocity());
                                    pawn.setDirection(Direction.DOWN.toString());
                                }
                            }
                            if (direction == Direction.RIGHT) {
                                if (!isColliding(pawn.getY(), pawn.getX() + pawn.getVelocity())) {
                                    pawn.setX(pawn.getX() + pawn.getVelocity());
                                    pawn.setDirection(Direction.RIGHT.toString());
                                }
                            }
                            if (direction == Direction.LEFT) {
                                if (!isColliding(pawn.getY(), pawn.getX() - pawn.getVelocity())) {
                                    pawn.setX(pawn.getX() - pawn.getVelocity());
                                    pawn.setDirection(Direction.LEFT.toString());
                                }
                            }
                        }
                    }
                }
            }
            if (topic == Topic.PLANT_BOMB) {
                //by server implementation sessionId == playerId
                String pawnPlayerId = queueMessage.getPlayerId();
                ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(String.valueOf(gameId));

                for (Object object :
                        replica.values()) {

                    if (object instanceof Pawn) {
                        Pawn pawn = (Pawn) object;

                        if (pawn.getPlayerId().equals(pawnPlayerId)) {
                            //bomb have to be placed in the center of nearest tile
                            long bombX = Math.round(pawn.getX() / 32) * 32;
                            long bombY = Math.round(pawn.getY() / 32) * 32;
                            Bomb bomb = new Bomb(objectIdGenerator.getAndIncrement(), bombY, bombX);
                            replica.put(bomb.getId(), bomb);
                            tickable.offer(bomb);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isColliding(double pawnY, double pawnX) {

        Bar playerBar = new Bar(pawnX, pawnX + playerSize, pawnY, pawnY + playerSize);

        for (Object object :
                GameService.getReplica(gameId).values()) {

            if (object instanceof Wall || object instanceof Wood) {
                Positionable obstacle = (Positionable) object;
                Bar obstacleBar = new Bar(obstacle.getX(), obstacle.getX() + tileSize,
                        obstacle.getY(), obstacle.getY() + tileSize);
                if (obstacleBar.collideCheck(playerBar)) return true;
            }
        }
        return false;
    }

    public void tick() {
        tickable.forEach(Tickable::tick);
        doMechanics();
        //object check

    }

    public void render() {
        Collection replicaToSend = GameService.getReplica(gameId).values();
        Message msg = new Message(Topic.REPLICA, JsonHelper.toJson(replicaToSend));
        TextMessage message = new TextMessage(JsonHelper.toJson(msg));
        //System.out.println("Sending message " + message.getPayload() + "to gameId=" + gameId);
        GameService.broadcast(Integer.parseInt(gameId), message);
    }

    public LinkedBlockingQueue<InputQueueMessage> getInputQueue() {
        return inputQueue;
    }
}
