package it.polimi.aui.auiapp.data;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import it.polimi.aui.auiapp.data.DBContract.GameEntry;
import it.polimi.aui.auiapp.model.games.Game;

/**
 * Cursor to loop a game retrieved from the SQLite DB
 */
public class GameCursor extends CursorWrapper
{
    public GameCursor(Cursor cursor)
    {
        super(cursor);
    }

    /**
     * Get game in the cursor
     */
    public Game getGame()
    {
        try
        {
            // The "type" column of the DB contains the fully qualified name of the linked Java class: here we retrieve the class constructor
            Class<?> gameClass = Class.forName(getString(getColumnIndexOrThrow(GameEntry.COLUMN_TYPE)));
            Constructor<?> gameConstructor = gameClass.getConstructor(int.class, String.class, String.class, String[].class, String.class, int.class);

            // Create game instance from data using the given constructor and return it
            return (Game) gameConstructor.newInstance
            (
                getInt(getColumnIndexOrThrow(GameEntry.COLUMN_ID)),
                getString(getColumnIndexOrThrow(GameEntry.COLUMN_CONTENT)),
                getString(getColumnIndexOrThrow(GameEntry.COLUMN_SOLUTION)),
                new String[]{
                    getString(getColumnIndexOrThrow(GameEntry.COLUMN_WRONG1)),
                    getString(getColumnIndexOrThrow(GameEntry.COLUMN_WRONG2)),
                    getString(getColumnIndexOrThrow(GameEntry.COLUMN_WRONG3))},
                getString(getColumnIndexOrThrow(GameEntry.COLUMN_LOCATION_TYPE)),
                getInt(getColumnIndexOrThrow(GameEntry.COLUMN_DIFFICULTY))
            );
        }
        catch(InstantiationException|IllegalAccessException|ClassNotFoundException|NoSuchMethodException|InvocationTargetException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
