package ru.bomber.server.game;

/**
 * Any game object that changes with time
 */
public interface Tickable {
    /**
     * Applies changes to game objects that happen after elapsed time
     */
    void tick();
}
