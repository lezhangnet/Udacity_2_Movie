package net.lezhang.udacity.movie.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.lezhang.udacity.movie.Movie;
import net.lezhang.udacity.movie.data.MovieDataContract.MovieEntry;
import net.lezhang.udacity.movie.data.MovieDataContract.PopularEntry;

public class MovieDBHelper extends SQLiteOpenHelper {
    private final String LOG_TAG = MovieDBHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    // private static final int DATABASE_VERSION = 1; // only movie table
    // private static final int DATABASE_VERSION = 3; // movie table; popular table
    private static final int DATABASE_VERSION = 4; // movie table; popular table; toprated table

    static final String DATABASE_NAME = "movie.db";

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(LOG_TAG, "onCreate()");

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID +                   " INTEGER PRIMARY KEY AUTOINCREMENT," +

                MovieEntry.COLUMN_MOVIE_ID +       " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_TITLE +          " TEXT NOT NULL, " +
                MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH +    " TEXT NOT NULL, " +
                MovieEntry.COLUMN_PLOT_OVERVIEW +  " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RATING +         " REAL NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE +   " TEXT NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                //" FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                //LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +

                // To assure the application have just one movie entry
                " UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_POPULAR_TABLE = "CREATE TABLE " + PopularEntry.TABLE_NAME + " (" +
                PopularEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PopularEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL" +
                ");";

        final String SQL_CREATE_TOPRATED_TABLE = "CREATE TABLE " + MovieDataContract.TopRatedEntry.TABLE_NAME + " (" +
                PopularEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PopularEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL" +
                ");";

        //sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        //sqLiteDatabase.execSQL(SQL_CREATE_POPULAR_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TOPRATED_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
