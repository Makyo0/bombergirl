package ru.bomber.server.service;

import org.springframework.web.socket.WebSocketSession;
import ru.bomber.server.network.Player;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession {

    private int gameId;
    private int numOfPlayers;
    private ArrayList<Player> playersList = new ArrayList<>();
    private ConcurrentHashMap<Integer, Object> replica = new ConcurrentHashMap<>();

    public GameSession(int newGameId, int numOfPlayers) {
        this.gameId = newGameId;
        this.numOfPlayers = numOfPlayers;
    }

    public int getGameId() {
        return gameId;
    }

    public int getNumOfPlayers() {
        return numOfPlayers;
    }

    public void addPlayer(Player player) {
        playersList.add(player);
    }

    public ArrayList<Player> getPlayersList() {
        return playersList;
    }

    public ArrayList<WebSocketSession> getPlayerSessions() {
        ArrayList<WebSocketSession> sessions = new ArrayList<>();
        for (Player player:
             playersList) {
            sessions.add(player.getWebSocketSession());
        }
        return sessions;
    }

    public ConcurrentHashMap<Integer, Object> getReplica() {
        return replica;
    }

}
