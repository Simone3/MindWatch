package it.polimi.aui.auiapp.data;

import android.content.Context;

import it.polimi.aui.auiapp.model.games.Game;
import it.polimi.aui.auiapp.places.GamePlace;
import it.polimi.aui.auiapp.places.PlacesManager;

/**
 * Manages the SQLite DB at high level: offers methods to get games from the database
 */
public class GameManager
{
    private static GameManager instance;
    private GameSQLiteRepository repository;
    private Context context;

    private GameManager(Context context)
    {
        this.context = context;
        repository = new GameSQLiteRepository(context);
    }

    /**
     * Singleton pattern
     */
    public static synchronized GameManager getInstance(Context context)
    {
        if(instance == null) instance = new GameManager(context);
        return instance;
    }

    /**
     * Get game from its ID
     * @param id the game ID
     * @return the game with the given ID
     */
    public Game getGameById(int id)
    {
        GameCursor cursor = repository.findById(id);
        Game game = null;
        if(cursor.moveToNext()) game = cursor.getGame();
        cursor.close();
        return game;
    }

    /**
     * Gets a random game that has never been solved by the user and has the given difficulty
     * @param difficulty the game difficulty
     * @return a random unsolved game
     */
    public Game getUnsolvedRandomGame(int difficulty)
    {
        // Get current position
        PlacesManager placesManager = new PlacesManager(context);
        GamePlace gamePlace = placesManager.getCurrentPlace();

        // Get game cursor
        GameCursor cursor = repository.getUnsolvedRandomGame(gamePlace, difficulty);

        // If we found nothing in the current position...
        if(cursor==null)
        {
            // Change to no position (if not already so)
            if(!gamePlace.equals(GamePlace.NONE))
            {
                gamePlace = GamePlace.NONE;
                cursor = repository.getUnsolvedRandomGame(gamePlace, difficulty);
            }
        }

        // If we still have nothing, there are no more generic games with the given settings
        if(cursor==null)
        {
            // TODO need to do something (e.g. inform user)
            return null;
        }

        // If we found a game, return it
        Game game = null;
        if(cursor.moveToNext()) game = cursor.getGame();
        cursor.close();
        return game;
    }

    /**
     * Sets a game as solved
     * @param id the game ID
     */
    public void setGameAsSolved(int id)
    {
        repository.setGameAsSolved(id);
    }

    /**
     * Gets the percentage (number 0-100) of solved games in the given difficulty level
     * @param difficulty the difficulty level
     * @return percentage, number 0-100
     */
    public int getSolvedGamesPercentage(int difficulty)
    {
        return repository.getSolvedGamesPercentage(difficulty);
    }
}
