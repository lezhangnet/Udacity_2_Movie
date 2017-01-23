package net.lezhang.udacity.movie.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieDataContract {

    public static final String CONTENT_AUTHORITY = "net.lezhang.udacity.movie";
    public static final Uri    CONTENT_URI_BASE = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String CONTENT_PATH_MOVIE = "movie";
    public static final String CONTENT_PATH_POPULAR = "popular";
    public static final String CONTENT_PATH_TOPRATED = "toprated";
    public static final String CONTENT_PATH_FAVORITE = "favorite";

    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";

        // Movie id of movie db, stored as int; foreign key
        public static final String COLUMN_MOVIE_ID = "movie_id";             // column 1
        public static final String COLUMN_TITLE = "title";                   // column 2
        public static final String COLUMN_ORIGINAL_TITLE = "original_title"; // column 3
        public static final String COLUMN_POSTER_PATH = "poster_path";       // column 4
        public static final String COLUMN_PLOT_OVERVIEW = "plot_overview";   // column 5
        // Rating, stored as double
        public static final String COLUMN_RATING = "rating";                 // column 6
        // Release date, stored as string
        public static final String COLUMN_RELEASE_DATE = "date";             // column 7
        // Raw json string fro review and video
        public static final String COLUMN_REVIEW_JSON = "reviewjson";
        public static final String COLUMN_VIDEO_JSON = "videojson";


        // "content://net.lezhang.udacity.movie/movie"
        public static final Uri CONTENT_URI =
                CONTENT_URI_BASE.buildUpon().appendPath(CONTENT_PATH_MOVIE).build();

        // "vnd.android.cursor.dir/net.lezhang.udacity.movie/movie"
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + CONTENT_PATH_MOVIE;

        // "vnd.android.cursor.item/net.lezhang.udacity.movie/movie"
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + CONTENT_PATH_MOVIE;

        /*
            id is the row id returned by inserting the value to db
            used for building return uri for insertion ONLY?
        */
        public static Uri buildMovieUri(long id) {
            //String movieIdStr = Integer.toString(movieId);
            //return CONTENT_URI.buildUpon().appendPath(movieIdStr).build();
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // id is the movie id
        public static Uri buildMovieIdUri(int movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

        public static int getMovieIdFromUri(Uri uri) {
            String s = uri.getPathSegments().get(1);
            return Integer.parseInt(s);
        }
    }

    public static final class PopularEntry implements BaseColumns {
        public static final String TABLE_NAME = "popular";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        // "content://net.lezhang.udacity.movie/popular"
        public static final Uri CONTENT_URI_POPULAR =
                CONTENT_URI_BASE.buildUpon().appendPath(CONTENT_PATH_POPULAR).build();

        public static Uri buildPopularUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI_POPULAR, id);
        }
    }

    public static final class TopRatedEntry implements BaseColumns {
        public static final String TABLE_NAME = "toprated";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        // "content://net.lezhang.udacity.movie/toprated"
        public static  final Uri CONTENT_URI_TOPRATED =
                CONTENT_URI_BASE.buildUpon().appendPath(CONTENT_PATH_TOPRATED).build();

        public static Uri buildTopratedUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI_TOPRATED, id);
        }

    }

    public static final class FavoriteEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorite";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        // "content://net.lezhang.udacity.movie/favorite"
        public static  final Uri CONTENT_URI_FAVORITE =
                CONTENT_URI_BASE.buildUpon().appendPath(CONTENT_PATH_FAVORITE).build();

        public static Uri buildFavoriteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI_FAVORITE, id);
        }
    }
}
