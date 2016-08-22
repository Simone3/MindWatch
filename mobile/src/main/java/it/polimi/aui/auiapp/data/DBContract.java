package it.polimi.aui.auiapp.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Contains general data about the SQLite DB
 */
public class DBContract
{
    public static final String DATABASE_NAME = "games.db";
    public static final int DATABASE_VERSION = 1;

    public static SQLiteDatabase getWritableDatabase(Context context)
    {
        return new DBHelper(context).getWritableDatabase();
    }

    public static SQLiteDatabase getReadableDatabase(Context context)
    {
        return new DBHelper(context).getReadableDatabase();
    }

    /**
     * Describes the "Game" table
     */
    public static abstract class GameEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "game";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_LOCATION_TYPE = "location";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_SOLUTION = "solution";
        public static final String COLUMN_WRONG1 = "wrong1";
        public static final String COLUMN_WRONG2 = "wrong2";
        public static final String COLUMN_WRONG3 = "wrong3";
        public static final String COLUMN_DIFFICULTY = "difficulty";
        public static final String COLUMN_SOLVED = "solved";
    }

    private static class DBHelper extends SQLiteAssetHelper
    {
        Context context;

        public DBHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }
    }
}
