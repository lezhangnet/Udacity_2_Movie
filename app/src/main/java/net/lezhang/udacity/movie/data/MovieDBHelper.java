package net.lezhang.udacity.movie.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.lezhang.udacity.movie.data.MovieDataContract.MovieEntry;
import net.lezhang.udacity.movie.data.MovieDataContract.PopularEntry;
import net.lezhang.udacity.movie.data.MovieDataContract.TopRatedEntry;
import net.lezhang.udacity.movie.data.MovieDataContract.FavoriteEntry;

public class MovieDBHelper extends SQLiteOpenHelper {
    private final String LOG_TAG = MovieDBHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    // private static final int DATABASE_VERSION = 1; // only movie table
    // private static final int DATABASE_VERSION = 3; // movie table; popular table
    // private static final int DATABASE_VERSION = 4; // movie table; popular table; toprated table
    // private static final int DATABASE_VERSION = 5; // movie table; popular table; toprated table; favorite table
    private static final int DATABASE_VERSION = 8; // reviewJson and videoJson columns added in movie table

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
                MovieEntry.COLUMN_REVIEW_JSON +    " TEXT, " +
                MovieEntry.COLUMN_VIDEO_JSON +     " TEXT, " +

                // To assure the application have just one movie entry
                " UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_POPULAR_TABLE = "CREATE TABLE " + PopularEntry.TABLE_NAME + " (" +
                PopularEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PopularEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL" +
                ");";

        final String SQL_CREATE_TOPRATED_TABLE = "CREATE TABLE " + TopRatedEntry.TABLE_NAME + " (" +
                TopRatedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TopRatedEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL" +
                ");";

        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoriteEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_POPULAR_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TOPRATED_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PopularEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TopRatedEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        // the other tables stores movie id only, which is ok to keep
        onCreate(sqLiteDatabase);
    }

}
