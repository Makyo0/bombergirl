package ru.bomber.matchmaker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.bomber.matchmaker.model.Connection;
import ru.bomber.matchmaker.service.ConnectionQueue;
import ru.bomber.matchmaker.service.MmRequester;
import ru.bomber.matchmaker.service.StartThread;

import java.io.IOException;

@Controller
@RequestMapping("/matchmaker")
public class MmController {

    public volatile String gameId = null;
    public static final int MAX_PLAYER_IN_GAME = 2;

    @RequestMapping(
            path = "join",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> join(@RequestBody String data) throws IOException {
        String name = data.split("=")[1];
        if (ConnectionQueue.getInstance().isEmpty()) {
            System.out.println("Registering new player " + name);
            ConnectionQueue.getInstance().offer(new Connection(name));
            System.out.println("Requesting to create new game");
            gameId = MmRequester.create(MAX_PLAYER_IN_GAME).body().string();
            System.out.println("Response with gameId " + gameId);
            Thread startThread = new Thread(new StartThread(gameId));
            startThread.start();
        } else {
            System.out.println("Registering new player " + name);
            ConnectionQueue.getInstance().offer(new Connection(name));
        }
        return ResponseEntity.ok().body(gameId); //return gameId
    }
}
