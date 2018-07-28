package ru.bomber.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.bomber.server.game.Pawn;
import ru.bomber.server.game.GameSession;
import ru.bomber.server.message.Topic;
import ru.bomber.server.network.Player;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameService {

    private static final ConcurrentHashMap<Integer, GameSession> gameMap = new ConcurrentHashMap<>();
    private static AtomicInteger gameIdGenerator = new AtomicInteger();

    public static String create(int numOfPlayers) {
        int newGameId = gameIdGenerator.getAndIncrement();
        GameSession gameSession = new GameSession(newGameId, numOfPlayers);
        gameMap.put(newGameId, gameSession);
        System.out.println("Created gameId=" + newGameId);
        return String.valueOf(newGameId);
    }


    public static String start(String gameId) {
        GameThread gameThread = new GameThread();
        gameThread.setGameId(gameId);
        Thread newGameThread = new Thread(gameThread, "gameThread:" + gameId);
        newGameThread.start();
        return gameId;
    }

    public static void send(WebSocketSession session, TextMessage msg) {
        if (session.isOpen()) {
            try {
                session.sendMessage(msg);
            } catch (IOException ignored) {
            }
        }
    }

    public static void connect(int gameId, Player player) {
        gameMap.get(gameId).addPlayer(player);
        System.out.println("PlayerId=" + player.getPlayerId() + " connected to gameId=" + gameId);
        System.out.println("Game:" + gameId + gameMap.get(gameId).getPlayersList());
    }

    public static ArrayList<WebSocketSession> getGameConnections(int gameId) {
        ArrayList<WebSocketSession> sessionList = new ArrayList<>();
        ArrayList<Player> playersList = gameMap.get(gameId).getPlayersList();
        for (Player player :
                playersList) {
            sessionList.add(player.getWebSocketSession());
        }
        return sessionList;
    }

    public static void shutdown(int gameId) {
        gameMap.get(gameId).getPlayerSessions().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.close();
                } catch (IOException ignored) {
                }
            }
        });
    }

    public static synchronized void broadcast(int gameId, TextMessage msg) {
        getGameConnections(gameId).forEach(session -> send(session, msg));
    }


    //Recieved message from StandardWebSocketSession[id=0, uri=/game/connect?gameId=0] message:{"topic":"PLANT_BOMB","data":{}}
    //Recieved message from StandardWebSocketSession[id=0, uri=/game/connect?gameId=0] message:{"topic":"MOVE","data":{"direction":"UP"}}
    public static void handleMessage(WebSocketSession session, TextMessage msg) {
        List<NameValuePair> nameValuePairList = URLEncodedUtils.parse(session.getUri(), StandardCharsets.UTF_8);
        String gameId = nameValuePairList.get(0).getValue();
        JsonNode message = JsonHelper.getJsonNode(msg.getPayload());
        String topic = message.findValue("topic").asText();
        //Message message = JsonHelper.fromJson(msg.getPayload(), Message.class);
        if (topic.equals(Topic.MOVE.toString())) {
            String direction = message.findValue("direction").asText();
            int pawnPlayerId = Integer.valueOf(session.getId()); //по реализации сервера sessionId == playerId
            ConcurrentHashMap<Integer, Object> replica = getReplica(gameId);
            for (Object object:
                 replica.values()) {
                if (object.getClass().equals(Pawn.class)) {
                    Pawn pawn = (Pawn) object;
                    if (pawn.getPlayerId() == pawnPlayerId) {
                        pawn.setX(pawn.getX() + 1);
                    }
                }
            }
        }
    }

    public static ConcurrentHashMap<Integer, Object> getReplica(String gameId) {
        return gameMap.get(Integer.valueOf(gameId)).getReplica();
    }

    public static ConcurrentHashMap<Integer, GameSession> getGameMap() {
        return gameMap;
    }
}
