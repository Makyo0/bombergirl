package ru.bomber.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.bomber.server.service.GameService;


@Controller
@RequestMapping("/game")
public class GameController {

    @Autowired
    GameService gameService;

    /**
     * curl -i localhost:8090/game/create
     */

    @RequestMapping(
            path = "/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> create(@RequestParam("playerCount") String playerCount) {
        String gameId = gameService.create(Integer.parseInt(playerCount));
        return ResponseEntity.ok().body(gameId); //return gameId
    }

    @RequestMapping(
            path = "/start",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public synchronized ResponseEntity<String> start(@RequestParam("gameId") String gameId) {
        String startedGameId = gameService.start(gameId);
        return ResponseEntity.ok().body(startedGameId); //return gameId
    }

}

