package it.polimi.aui.auiapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import it.polimi.aui.auiapp.places.GamePlace;

import static it.polimi.aui.auiapp.data.DBContract.GameEntry;
import static it.polimi.aui.auiapp.data.DBContract.getWritableDatabase;

/**
 * Manages the SQLite DB at low level: actual queries that return game cursors
 */
public class GameSQLiteRepository
{
    private SQLiteDatabase db;

    protected GameSQLiteRepository(Context context)
    {
        db = getWritableDatabase(context);
    }

    /**
     * @see it.polimi.aui.auiapp.data.GameManager#getUnsolvedRandomGame(int)
     */
    protected GameCursor getUnsolvedRandomGame(GamePlace place, int difficulty)
    {
        return new GameCursor(db.rawQuery(
                " SELECT * FROM "+GameEntry.TABLE_NAME+
                " WHERE "+GameEntry.COLUMN_LOCATION_TYPE+" = ?"+
                " AND "+GameEntry.COLUMN_DIFFICULTY+" = ?"+
                " AND "+GameEntry.COLUMN_SOLVED+" = 0"+
                " ORDER BY RANDOM()"+
                " LIMIT 1",
                new String[]{place.getValue(), ""+difficulty}));
    }

    /**
     * @see it.polimi.aui.auiapp.data.GameManager#getGameById(int)
     */
    protected GameCursor findById(int id)
    {
        return new GameCursor(db.rawQuery(
                " SELECT * FROM " + GameEntry.TABLE_NAME +
                " WHERE " + GameEntry.COLUMN_ID + " = ?",
                new String[]{""+id}));
    }

    /**
     * @see it.polimi.aui.auiapp.data.GameManager#setGameAsSolved(int)
     */
    protected void setGameAsSolved(int id)
    {
        ContentValues values = new ContentValues();
        values.put(GameEntry.COLUMN_SOLVED, 1);
        db.update(GameEntry.TABLE_NAME, values, GameEntry.COLUMN_ID+" = ?", new String[]{id+""});
    }

    /**
     * @see it.polimi.aui.auiapp.data.GameManager#getSolvedGamesPercentage(int)
     */
    protected int getSolvedGamesPercentage(int difficulty)
    {
        Cursor cursor =
                    db.rawQuery(
                    " SELECT ROUND(100*COUNT(*)/"+
                        "(SELECT COUNT(*) FROM "+GameEntry.TABLE_NAME+" WHERE "+GameEntry.COLUMN_DIFFICULTY+" = ?))" +
                    " AS percentage FROM "+GameEntry.TABLE_NAME+
                    " WHERE "+GameEntry.COLUMN_SOLVED+" = 1" +
                    " AND "+GameEntry.COLUMN_DIFFICULTY+" = ?",
                    new String[]{difficulty+"", difficulty+""});

        int percentage = 0;
        if(cursor!=null)
        {
            if(cursor.getCount()>0)
            {
                cursor.moveToNext();
                percentage = cursor.getInt(cursor.getColumnIndexOrThrow("percentage"));
            }
            cursor.close();
        }
        return percentage;
    }
}
