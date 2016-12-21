package net.lezhang.udacity.movie.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

import net.lezhang.udacity.movie.MainActivity;
import net.lezhang.udacity.movie.R;

public class MovieDataContract {

    /*
       This seems to be not working as this is needed much earlier than the set().

    private static Context context;
    public static void setContext(Context c) {
        context = c;
        CONTENT_AUTHORITY = context.getString(R.string.package_name);
    }
*/

    public static final String CONTENT_AUTHORITY = "net.lezhang.udacity.movie";
    public static final Uri    CONTENT_URI_BASE = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String CONTENT_PATH_MOVIE = "movie";

    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movie";

        // Column with the foreign key into the location table.
        //public static final String COLUMN_LOC_KEY = "location_id";

        // Movie id of movie db, stored as int
        public static final String COLUMN_MOVIE_ID = "movie_id";             // column 1
        public static final String COLUMN_TITLE = "title";                   // column 2
        public static final String COLUMN_ORIGINAL_TITLE = "original_title"; // column 3
        public static final String COLUMN_POSTER_PATH = "poster_path";       // column 4
        public static final String COLUMN_PLOT_OVERVIEW = "plot_overview";   // column 5
        // Rating, stored as double
        public static final String COLUMN_RATING = "rating";                 // column 6
        // Release date, stored as string
        public static final String COLUMN_RELEASE_DATE = "date";             // column 7

        public static final Uri CONTENT_URI =
                CONTENT_URI_BASE.buildUpon().appendPath(CONTENT_PATH_MOVIE).build();

        // "vnd.android.cursor.dir/net.lezhang.udacity.movie/movie" - not used
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + CONTENT_PATH_MOVIE;

        // "vnd.android.cursor.item/net.lezhang.udacity.movie/movie"
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + CONTENT_PATH_MOVIE;

        /*
            id is the row id returned by inserting the value to db
         */
        public static Uri buildMovieUri(long id) {
            //String movieIdStr = Integer.toString(movieId);
            //return CONTENT_URI.buildUpon().appendPath(movieIdStr).build();
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // m
        public static Uri buildMovieIdUri(int movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

        public static int getMovieIdFromUri(Uri uri) {
            String s = uri.getPathSegments().get(1);
            return Integer.parseInt(s);
        }
    }
}
