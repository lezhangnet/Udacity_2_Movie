package net.lezhang.udacity.movie.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import net.lezhang.udacity.movie.data.MovieDataContract.MovieEntry;
import net.lezhang.udacity.movie.data.MovieDataContract.PopularEntry;
import net.lezhang.udacity.movie.data.MovieDataContract.TopRatedEntry;
import net.lezhang.udacity.movie.data.MovieDataContract.FavoriteEntry;

public class MovieContentProvider extends ContentProvider {
    private final String LOG_TAG = MovieContentProvider.class.getSimpleName();

    private MovieDBHelper movieDBHelper;

    static final int MOVIE_LIST_CODE =     100;
    static final int MOVIE_POPULAR_CODE =  101;
    static final int MOVIE_TOPRATED_CODE = 102;
    static final int MOVIE_FAVORITE_CODE = 103;
    static final int MOVIE_DETAIL_CODE =   200;

    private static final UriMatcher movieUriMatcher = buildUriMatcher();
    /**
     * Match each URI to the integer constants defined above.
     */
    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // "net.lezhang.udacity.movie"
        final String authority = MovieDataContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieDataContract.CONTENT_PATH_MOVIE, MOVIE_LIST_CODE);
        matcher.addURI(authority, MovieDataContract.CONTENT_PATH_MOVIE + "/#", MOVIE_DETAIL_CODE);
        matcher.addURI(authority, MovieDataContract.CONTENT_PATH_POPULAR, MOVIE_POPULAR_CODE);
        matcher.addURI(authority, MovieDataContract.CONTENT_PATH_TOPRATED, MOVIE_TOPRATED_CODE);
        matcher.addURI(authority, MovieDataContract.CONTENT_PATH_FAVORITE, MOVIE_FAVORITE_CODE);
        return matcher;
    }


    private static final SQLiteQueryBuilder movieQueryBuilder;

    static {
        //MovieDataContract.setContext(getContext());

        movieQueryBuilder = new SQLiteQueryBuilder();
/*
        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);
*/
    }

    /*
    //location.location_setting = ?
    private static final String sLocationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    //location.location_setting = ? AND date >= ?
    private static final String sLocationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " >= ? ";

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ";

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


*/

    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate()");
        //MovieDataContract.setContext(getContext());
        movieDBHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(LOG_TAG, "getType()!!!!!!!!!!");
        final int match = movieUriMatcher.match(uri);

        switch (match) {
            case MOVIE_LIST_CODE:
                return MovieEntry.CONTENT_DIR_TYPE;
            case MOVIE_DETAIL_CODE:
                return MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri type: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String   selection,
                        String[] selectionArgs,
                        String   sortOrder) {
        Log.d(LOG_TAG, "query(): uri: " + uri + " " + sortOrder);
        Cursor retCursor = null;
        switch (movieUriMatcher.match(uri)) {
            case MOVIE_POPULAR_CODE:
            {
                Log.d(LOG_TAG, "querying popular movies");
                SQLiteDatabase db = movieDBHelper.getReadableDatabase();

                SQLiteQueryBuilder popularMovieQueryBuilder = new SQLiteQueryBuilder();
                popularMovieQueryBuilder.setTables(
                        PopularEntry.TABLE_NAME + " INNER JOIN " + MovieEntry.TABLE_NAME +
                                " ON " + PopularEntry.TABLE_NAME + "." + PopularEntry.COLUMN_MOVIE_ID +
                                " = " + MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID);

                retCursor = popularMovieQueryBuilder.query(db,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIE_TOPRATED_CODE: {
                Log.d(LOG_TAG, "querying top rated movies");
                SQLiteDatabase db = movieDBHelper.getReadableDatabase();

                SQLiteQueryBuilder topratedMovieQueryBuilder = new SQLiteQueryBuilder();
                topratedMovieQueryBuilder.setTables(
                        TopRatedEntry.TABLE_NAME + " INNER JOIN " + MovieEntry.TABLE_NAME +
                                " ON " + TopRatedEntry.TABLE_NAME + "." + TopRatedEntry.COLUMN_MOVIE_ID +
                                " = " + MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID);

                retCursor = topratedMovieQueryBuilder.query(db,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIE_FAVORITE_CODE: {
                Log.d(LOG_TAG, "querying favorite movies");
                SQLiteDatabase db = movieDBHelper.getReadableDatabase();

                SQLiteQueryBuilder favoriteMovieQueryBuilder = new SQLiteQueryBuilder();
                favoriteMovieQueryBuilder.setTables(
                        FavoriteEntry.TABLE_NAME + " INNER JOIN " + MovieEntry.TABLE_NAME +
                                " ON " + FavoriteEntry.TABLE_NAME + "." + FavoriteEntry.COLUMN_MOVIE_ID +
                                " = " + MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID);

                retCursor = favoriteMovieQueryBuilder.query(db,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIE_LIST_CODE: {
                Log.d(LOG_TAG, "listing all movies");
                SQLiteDatabase db = movieDBHelper.getReadableDatabase();
                retCursor = db.query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIE_DETAIL_CODE: {
                SQLiteDatabase db = movieDBHelper.getReadableDatabase();
                int movieId = MovieEntry.getMovieIdFromUri(uri);
                Log.d(LOG_TAG, "querying movie detail of id: " + movieId);

                retCursor = db.query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        MovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{ Integer.toString(movieId) },
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            /*
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            */

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        ContentResolver cr = getContext().getContentResolver();
        retCursor.setNotificationUri(cr, uri);
        return retCursor;
    }

    /*
        not sure how the returnUri is used?
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, "insert() for uri: " + uri);
        final SQLiteDatabase db = movieDBHelper.getWritableDatabase();
        final int match = movieUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case MOVIE_LIST_CODE: {
                /*
                normalizeDate(values);
                */
                long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert into: " + uri);
                break;
            }
            case MOVIE_POPULAR_CODE: {
                long _id = db.insert(PopularEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PopularEntry.buildPopularUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert into " + uri);
                break;
            }
            case MOVIE_TOPRATED_CODE: {
                long _id = db.insert(TopRatedEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TopRatedEntry.buildTopratedUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert into " + uri);
                break;
            }
            case MOVIE_FAVORITE_CODE: {
                long _id = db.insert(FavoriteEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = FavoriteEntry.buildFavoriteUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri for insert: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete() for uri: " + uri);
        final SQLiteDatabase db = movieDBHelper.getWritableDatabase();
        final int match = movieUriMatcher.match(uri);
        int rowsDeleted = 0;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIE_LIST_CODE:
                rowsDeleted = db.delete(
                        MovieDataContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_POPULAR_CODE:
                rowsDeleted = db.delete(
                        MovieDataContract.PopularEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_TOPRATED_CODE:
                rowsDeleted = db.delete(
                        MovieDataContract.TopRatedEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_FAVORITE_CODE:
                rowsDeleted = db.delete(
                        MovieDataContract.FavoriteEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri + "match: " + match);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            Log.d(LOG_TAG, "deleted " + rowsDeleted + " records");
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /*
    private void normalizeDate(ContentValues values) {
        // normalize the date value

        if (values.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
            values.put(WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.normalizeDate(dateValue));
        }

    }
    */

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.e(LOG_TAG, "zhale: updating at uri: " + uri);
        final SQLiteDatabase db = movieDBHelper.getWritableDatabase();
        final int match = movieUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case MOVIE_LIST_CODE:
                rowsUpdated = db.update(MovieDataContract.MovieEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            /*
            case LOCATION:
                rowsUpdated = db.update(WeatherContract.LocationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
                */
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.e(LOG_TAG, "zhale: bulkInserting at uri: " + uri);
        final SQLiteDatabase db = movieDBHelper.getWritableDatabase();
        final int match = movieUriMatcher.match(uri);
        switch (match) {
            case MOVIE_LIST_CODE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieDataContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        movieDBHelper.close();
        super.shutdown();
    }
}
