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
    private final static int impassableTileSize = 29;
    private final static int playerSize = 24;
    private final static int renderTileSize = 32;

    public GameMechanics(String gameId) {
        this.gameId = gameId;
    }

    public void initGame(String gameId) {

        System.out.println("Starting new game");

        ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(gameId);

        //generating left/right borders of game field
        for (int i = 0; i <= renderTileSize * 12; i += renderTileSize) {
            Wall leftWall = new Wall(objectIdGenerator.getAndIncrement(), i, 0);
            Wall rightWall = new Wall(objectIdGenerator.getAndIncrement(), i, renderTileSize * 16);
            replica.put(leftWall.getId(), leftWall);
            replica.put(rightWall.getId(), rightWall);
        }

        //generating top/bottom borders of game field
        for (int i = renderTileSize; i <= renderTileSize * 15; i += renderTileSize) {
            Wall topWall = new Wall(objectIdGenerator.getAndIncrement(), 0, i);
            Wall bottomWall = new Wall(objectIdGenerator.getAndIncrement(), renderTileSize * 12, i);
            replica.put(topWall.getId(), topWall);
            replica.put(bottomWall.getId(), bottomWall);
        }

        //generating walls and boxes across game field
        for (int i = renderTileSize * 2; i <= renderTileSize * 15; i += renderTileSize * 2) {
            for (int j = renderTileSize * 2; j < renderTileSize * 15; j += renderTileSize * 2) {
                Wall wall = new Wall(objectIdGenerator.getAndIncrement(), i, j);
                replica.put(wall.getId(), wall);
                Wood wood = new Wood(objectIdGenerator.getAndIncrement(), i - renderTileSize, j);
                replica.put(wood.getId(), wood);
            }
        }

        for (int i = renderTileSize * 3; i <= renderTileSize * 14; i += renderTileSize * 2) {
            for (int j = renderTileSize; j < renderTileSize * 12; j += renderTileSize) {
                Wood wood = new Wood(objectIdGenerator.getAndIncrement(), j, i);
                replica.put(wood.getId(), wood);
            }
        }

        for (int i = renderTileSize * 4; i <= renderTileSize * 8; i += renderTileSize) {
            Wood leftWood = new Wood(objectIdGenerator.getAndIncrement(), i, renderTileSize);
            replica.put(leftWood.getId(), leftWood);
            Wood rightWood = new Wood(objectIdGenerator.getAndIncrement(), i, renderTileSize * 15);
            replica.put(rightWood.getId(), rightWood);
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
        checkTickable();
    }

    public void checkTickable() {
        for (Tickable object :
                tickable) {
            //check fire status
            if (object instanceof Fire) {
                if (((Fire) object).getLifetime() == 0) {
                    GameService.getReplica(gameId).remove(((Fire) object).getId());
                    tickable.remove(object);
                }
            }
            //check bomb's status
            if (object instanceof Bomb) {
                if (((Bomb) object).getLifetime() == 0) {
                    explodeBomb((Bomb) object);
                }
            }
        }
    }

    public void explodeBomb(Bomb bomb) {
        int bombStrength = 1;
        int bombY = (int) bomb.getY();
        int bombX = (int) bomb.getX();
        ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(gameId);
        replica.remove(bomb.getId());
        tickable.remove(bomb);

        for (int i = bombX; i <= bombX + renderTileSize * bombStrength; i += renderTileSize) {
            Fire fire = new Fire(objectIdGenerator.getAndIncrement(), bombY, i);
            placeFire(fire);
        }
        for (int i = bombX - renderTileSize; i >= bombX - renderTileSize * bombStrength; i -= renderTileSize) {
            Fire fire = new Fire(objectIdGenerator.getAndIncrement(), bombY, i);
            placeFire(fire);
        }
        for (int i = bombY + renderTileSize; i <= bombY + renderTileSize * bombStrength; i += renderTileSize) {
            Fire fire = new Fire(objectIdGenerator.getAndIncrement(), i, bombX);
            placeFire(fire);
        }
        for (int i = bombY - renderTileSize; i >= bombY - renderTileSize * bombStrength; i -= renderTileSize) {
            Fire fire = new Fire(objectIdGenerator.getAndIncrement(), i, bombX);
            placeFire(fire);
        }
    }

    public void placeFire(Fire fire) {
        boolean isCollided = false;
        ConcurrentHashMap<Integer, Object> replica = GameService.getReplica(gameId);
        for (Object object :
                replica.values()) {
            if (object instanceof Wood) {
                if (isFireColliding(fire, object)) {
                    replica.replace(((Positionable) object).getId(), fire);
                    tickable.offer(fire);
                    isCollided = true;
                    break;
                }
            }
            if (object instanceof Pawn) {
                if (isFireColliding(fire, object)) {
                    GameService.gameOver(Integer.valueOf(gameId), ((Pawn) object).getPlayerId());
                    replica.replace(((Positionable) object).getId(), fire);
                    tickable.offer(fire);
                    isCollided = true;
                    break;
                }
            }
            if (object instanceof Wall) {
                if (isFireColliding(fire, object)) {
                    isCollided = true;
                    break;
                }
            }
            if (object instanceof Bomb) {
                if (isFireColliding(fire, object)) {
                    explodeBomb((Bomb) object);
                    isCollided = true;
                    break;
                }
            }
        }
        if (!isCollided) {
            replica.put(objectIdGenerator.getAndIncrement(), fire);
            tickable.offer(fire);
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
                                if (!isPlayerColliding(pawn.getY() + pawn.getVelocity(), pawn.getX())) {
                                    pawn.setY(pawn.getY() + pawn.getVelocity());
                                    pawn.setDirection(Direction.UP.toString());
                                }
                            }
                            if (direction == Direction.DOWN) {
                                if (!isPlayerColliding(pawn.getY() - pawn.getVelocity(), pawn.getX())) {
                                    pawn.setY(pawn.getY() - pawn.getVelocity());
                                    pawn.setDirection(Direction.DOWN.toString());
                                }
                            }
                            if (direction == Direction.RIGHT) {
                                if (!isPlayerColliding(pawn.getY(), pawn.getX() + pawn.getVelocity())) {
                                    pawn.setX(pawn.getX() + pawn.getVelocity());
                                    pawn.setDirection(Direction.RIGHT.toString());
                                }
                            }
                            if (direction == Direction.LEFT) {
                                if (!isPlayerColliding(pawn.getY(), pawn.getX() - pawn.getVelocity())) {
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
                            long bombY = Math.round(pawn.getY() / renderTileSize) * renderTileSize;
                            long bombX = Math.round(pawn.getX() / renderTileSize) * renderTileSize;
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

    public boolean isPlayerColliding(double pawnY, double pawnX) {

        Bar playerBar = new Bar(pawnX, pawnX + playerSize, pawnY, pawnY + playerSize);

        for (Object object :
                GameService.getReplica(gameId).values()) {
            if (object instanceof Wall || object instanceof Wood) {
                Positionable obstacle = (Positionable) object;
                Bar obstacleBar = new Bar(obstacle.getX(), obstacle.getX() + impassableTileSize,
                        obstacle.getY(), obstacle.getY() + impassableTileSize);
                if (obstacleBar.collideCheck(playerBar)) return true;
            }
        }
        return false;
    }

    public boolean isFireColliding(Fire fire, Object object) {
        Positionable objectToCheck = (Positionable) object;
        Bar fireBar = new Bar(fire.getX(), fire.getX() + impassableTileSize,
                fire.getY(), fire.getY() + impassableTileSize);
        Bar objectBar = new Bar(objectToCheck.getX(), objectToCheck.getX() + impassableTileSize,
                objectToCheck.getY(), objectToCheck.getY() + impassableTileSize);
        return fireBar.collideCheck(objectBar);
    }

    public void tick() {
        tickable.forEach(Tickable::tick);
        doMechanics();
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
