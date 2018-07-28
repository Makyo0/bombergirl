package ru.bomber.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.bomber.server.game.Pawn;
import ru.bomber.server.game.Point;
import java.util.concurrent.atomic.AtomicInteger;

public class GameMechanics {

    private static AtomicInteger pawnGenerator = new AtomicInteger();

    public void initGame(String gameId) {

    }
}
