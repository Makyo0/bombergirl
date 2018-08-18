package ru.bomber.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.TextMessage;
import ru.bomber.server.game.*;
import ru.bomber.server.message.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GameMechanics {

    private String gameId;
    private AtomicInteger objectIdGenerator = new AtomicInteger();
    private LinkedBlockingQueue<InputQueueMessage> inputQueue = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<Tickable> tickable = new LinkedBlockingQueue<>();
    private ConcurrentHashMap<Integer, Object> replica;
    private final static int impassableTileSize = 29;
    private final static int playerSize = 24;
    public final static int renderTileSize = 32;
    private int bombsBonusCount = 4;
    private int speedBonusCount = 4;
    private int rangeBonusCount = 4;

    public GameMechanics(String gameId) {
        this.gameId = gameId;
        this.replica = GameService.getReplica(gameId);
    }

    public void initGame(String gameId) {

        System.out.println("Starting new game");

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

        //filling game field with boxes
        for (int i = renderTileSize * 3; i <= renderTileSize * 14; i += renderTileSize * 2) {
            for (int j = renderTileSize; j < renderTileSize * 12; j += renderTileSize) {
                Wood wood = new Wood(objectIdGenerator.getAndIncrement(), j, i);
                replica.put(wood.getId(), wood);
            }
        }

        //some more boxes to prevent early contact
        for (int i = renderTileSize * 4; i <= renderTileSize * 8; i += renderTileSize) {
            Wood leftWood = new Wood(objectIdGenerator.getAndIncrement(), i, renderTileSize);
            replica.put(leftWood.getId(), leftWood);
            Wood rightWood = new Wood(objectIdGenerator.getAndIncrement(), i, renderTileSize * 15);
            replica.put(rightWood.getId(), rightWood);
        }

        //generating bonuses
        for (Object object :
                replica.values()) {
            if (object instanceof Wood) {
                getRandomBonus(((Wood) object).getY(), ((Wood) object).getX());
            }
        }

        //generating player Pawn's
        Pawn pawn1 = new Pawn(objectIdGenerator.getAndIncrement(), renderTileSize, renderTileSize);
        Pawn pawn2 = new Pawn(objectIdGenerator.getAndIncrement(), renderTileSize * 11, renderTileSize);
        Pawn pawn3 = new Pawn(objectIdGenerator.getAndIncrement(), renderTileSize, renderTileSize * 15);
        Pawn pawn4 = new Pawn(objectIdGenerator.getAndIncrement(), renderTileSize * 11, renderTileSize * 15);
        pawn1.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(0).getPlayerId());
        pawn2.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(1).getPlayerId());
        pawn3.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(2).getPlayerId());
        pawn4.setPlayerId(GameService.getGameMap().get(Integer.valueOf(gameId)).getPlayersList().get(3).getPlayerId());
        replica.put(pawn1.getId(), pawn1);
        replica.put(pawn2.getId(), pawn2);
        replica.put(pawn3.getId(), pawn3);
        replica.put(pawn4.getId(), pawn4);
    }

    private void getRandomBonus(double y, double x) {
        //for each wood 15% chance to generate bonus of random type
        if (Math.random() < 0.15) {
            int bonusType = (int) Math.round(Math.random() * 10);
            if (bonusType >= 0 && bonusType < 3 && bombsBonusCount > 0) {
                Bonus bonus = new Bonus(objectIdGenerator.getAndIncrement(), y, x, BonusType.BOMBS);
                replica.put(bonus.getId(), bonus);
                bombsBonusCount--;
            } else if (bonusType >= 3 && bonusType <= 6 && speedBonusCount > 0) {
                Bonus bonus2 = new Bonus(objectIdGenerator.getAndIncrement(), y, x, BonusType.SPEED);
                replica.put(bonus2.getId(), bonus2);
                speedBonusCount--;
            } else if (rangeBonusCount > 0) {
                Bonus bonus3 = new Bonus(objectIdGenerator.getAndIncrement(), y, x, BonusType.RANGE);
                replica.put(bonus3.getId(), bonus3);
                rangeBonusCount--;
            }
        }
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
                    replica.remove(((Fire) object).getId());
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
        int bombRange = bomb.getBombRange();
        int bombY = (int) bomb.getY();
        int bombX = (int) bomb.getX();
        replica.remove(bomb.getId());
        tickable.remove(bomb);
        Pawn bombOwner = bomb.getPawn();
        bombOwner.setAvailableBombs(bombOwner.getAvailableBombs() + 1);

        //4 loop's for each direction of explosion
        //fire in bomb tile is generated in first loop
        for (int i = bombX; i <= bombX + renderTileSize * bombRange; i += renderTileSize) {
            Fire fire = new Fire(objectIdGenerator.getAndIncrement(), bombY, i);
            boolean isCollided = placeFire(fire);
            if (isCollided) break;
        }
        for (int i = bombX - renderTileSize; i >= bombX - renderTileSize * bombRange; i -= renderTileSize) {
            Fire fire = new Fire(objectIdGenerator.getAndIncrement(), bombY, i);
            boolean isCollided = placeFire(fire);
            if (isCollided) break;
        }
        for (int i = bombY + renderTileSize; i <= bombY + renderTileSize * bombRange; i += renderTileSize) {
            Fire fire = new Fire(objectIdGenerator.getAndIncrement(), i, bombX);
            boolean isCollided = placeFire(fire);
            if (isCollided) break;
        }
        for (int i = bombY - renderTileSize; i >= bombY - renderTileSize * bombRange; i -= renderTileSize) {
            Fire fire = new Fire(objectIdGenerator.getAndIncrement(), i, bombX);
            boolean isCollided = placeFire(fire);
            if (isCollided) break;
        }
    }

    public boolean placeFire(Fire fire) {
        boolean isCollided = false;
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
        return isCollided;
    }

    public void handleInputQueue() {
        try {
            InputQueueMessage queueMessage = inputQueue.take();
            JsonNode message = JsonHelper.getJsonNode(queueMessage.getPayload());
            Topic topic = Topic.valueOf(message.findValue("topic").asText());

            if (topic == Topic.MOVE) {

                Direction direction = Direction.valueOf(message.findValue("direction").asText());
                String pawnPlayerId = queueMessage.getPlayerId();

                for (Object object :
                        replica.values()) {

                    if (object instanceof Pawn) {
                        Pawn pawn = (Pawn) object;

                        if (pawn.getPlayerId().equals(pawnPlayerId)) {

                            if (direction == Direction.UP) {
                                if (!isPlayerColliding(pawn.getY() + pawn.getVelocity(), pawn.getX())) {
                                    pawn.setY(pawn.getY() + pawn.getVelocity());
                                    pawn.setDirection(Direction.UP.toString());
                                    checkBonus(pawn);
                                }
                            }
                            if (direction == Direction.DOWN) {
                                if (!isPlayerColliding(pawn.getY() - pawn.getVelocity(), pawn.getX())) {
                                    pawn.setY(pawn.getY() - pawn.getVelocity());
                                    pawn.setDirection(Direction.DOWN.toString());
                                    checkBonus(pawn);
                                }
                            }
                            if (direction == Direction.RIGHT) {
                                if (!isPlayerColliding(pawn.getY(), pawn.getX() + pawn.getVelocity())) {
                                    pawn.setX(pawn.getX() + pawn.getVelocity());
                                    pawn.setDirection(Direction.RIGHT.toString());
                                    checkBonus(pawn);
                                }
                            }
                            if (direction == Direction.LEFT) {
                                if (!isPlayerColliding(pawn.getY(), pawn.getX() - pawn.getVelocity())) {
                                    pawn.setX(pawn.getX() - pawn.getVelocity());
                                    pawn.setDirection(Direction.LEFT.toString());
                                    checkBonus(pawn);

                                }
                            }
                            if (direction == Direction.IDLE) {
                                pawn.setDirection("");
                            }
                        }
                    }
                }
            }
            if (topic == Topic.PLANT_BOMB) {
                //by server implementation sessionId == playerId
                String pawnPlayerId = queueMessage.getPlayerId();

                for (Object object :
                        replica.values()) {

                    if (object instanceof Pawn) {
                        Pawn pawn = (Pawn) object;

                        if (pawn.getPlayerId().equals(pawnPlayerId)) {
                            if (pawn.getAvailableBombs() > 0) {
                                Bomb bomb = new Bomb(objectIdGenerator.getAndIncrement(), pawn);
                                pawn.setAvailableBombs(pawn.getAvailableBombs() - 1);
                                replica.put(bomb.getId(), bomb);
                                tickable.offer(bomb);
                            }
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
                replica.values()) {
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

    public void checkBonus(Pawn pawn) {


        Bar playerBar = new Bar(pawn.getX(), pawn.getX() + playerSize, pawn.getY(), pawn.getY() + playerSize);

        for (Object object :
                replica.values()) {
            if (object instanceof Bonus) {
                Bonus bonus = (Bonus) object;
                Bar bonusBar = new Bar(bonus.getX(), bonus.getX() + impassableTileSize,
                        bonus.getY(), bonus.getY() + impassableTileSize);
                if (bonusBar.collideCheck(playerBar)) {
                    BonusType bonusType = bonus.getBonusType();
                    if (bonusType == BonusType.BOMBS) {
                        pawn.setAvailableBombs(pawn.getAvailableBombs() + 1);
                        replica.remove(bonus.getId());
                    }
                    if (bonusType == BonusType.RANGE) {
                        pawn.setBombRange(pawn.getBombRange() + 1);
                        replica.remove(bonus.getId());
                    }
                    if (bonusType == BonusType.SPEED) {
                        pawn.setVelocity(pawn.getVelocity() + 0.2);
                        replica.remove(bonus.getId());
                    }
                }
            }
        }
    }

    public void render() {
        Message msg = new Message(Topic.REPLICA, JsonHelper.toJson(replica.values()));
        TextMessage message = new TextMessage(JsonHelper.toJson(msg));
        //System.out.println("Sending message " + message.getPayload() + "to gameId=" + gameId);
        GameService.broadcast(Integer.parseInt(gameId), message);
    }

    public LinkedBlockingQueue<InputQueueMessage> getInputQueue() {
        return inputQueue;
    }
}
