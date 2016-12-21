package net.lezhang.udacity.movie.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import net.lezhang.udacity.movie.Movie;
import net.lezhang.udacity.movie.MovieDataRetriever;
import net.lezhang.udacity.movie.data.MovieDataContract;

import java.net.URL;

public class MovieService extends IntentService {
    private static final String LOG_TAG = MovieService.class.getSimpleName();

    public static final String SORTORDER_EXTRA = "sort_order_extra";

    public MovieService() {
        super("MovieService");
        Log.e(LOG_TAG, "zhale: MovieService()");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(LOG_TAG, "zhale: MovieService: onHandleIntent()");

        int sortOrderExtra = intent.getIntExtra(SORTORDER_EXTRA, 0);
        Log.e(LOG_TAG, "zhale: sortOrder: " + sortOrderExtra);

        // validate required params
        if (sortOrderExtra == 0) {
            Log.e(LOG_TAG, "Invalid sortOrderExtra passed into MovieServide: " + sortOrderExtra);
            return;
        }

        URL url = MovieDataRetriever.getListQueryUrl(sortOrderExtra);
        String movieListJsonStr = MovieDataRetriever.getJsonStr(url);

        if(movieListJsonStr == null) {
            Log.e(LOG_TAG, "null JSON string returned");
            return;
        }

        Movie[] result;
        try {
            result = MovieDataRetriever.getMovieListFromJsonStr(movieListJsonStr);

            for(Movie m : result) {
                addMovie(m);
            }

        } catch(Exception e) {
            Log.e(LOG_TAG, "error parsing JSON to movie list", e);
        }

        return;
    }

    private long addMovie(Movie m) {
        Log.e(LOG_TAG, "zhale: try adding movie " + m + " to DB");
        long movieRowId = 0;

        // First, check if the movie exists in the db
        ContentResolver resolver = this.getContentResolver();
        Cursor movieCursor = resolver.query(
                MovieDataContract.MovieEntry.CONTENT_URI,
                new String[]{MovieDataContract.MovieEntry._ID},
                MovieDataContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{Integer.toString(m.getId())},
                null);

        if (movieCursor.moveToFirst()) {
            Log.e(LOG_TAG, "zhale: movie existing, do nothing");
            // the movie exists in our database
            //int movieIdIndex = movieCursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID);
            //movieRowId = movieCursor.getInt(movieIdIndex);
        } else {
            Log.e(LOG_TAG, "actually adding to DB");
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues movieValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            movieValues.put(MovieDataContract.MovieEntry.COLUMN_MOVIE_ID,
                    m.getId());
            movieValues.put(MovieDataContract.MovieEntry.COLUMN_TITLE,
                    m.getTitle());
            movieValues.put(MovieDataContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
                    m.getOriginalTitle());
            movieValues.put(MovieDataContract.MovieEntry.COLUMN_RELEASE_DATE,
                    m.getReleaseDate());
            movieValues.put(MovieDataContract.MovieEntry.COLUMN_POSTER_PATH,
                    m.getPosterPath());
            movieValues.put(MovieDataContract.MovieEntry.COLUMN_PLOT_OVERVIEW,
                    m.getPlotOverview());
            movieValues.put(MovieDataContract.MovieEntry.COLUMN_RATING,
                    m.getRating());

            // Finally, insert movie data into the database.
            Uri insertedUri = resolver.insert(
                    MovieDataContract.MovieEntry.CONTENT_URI,
                    movieValues
            );

            Log.e(LOG_TAG, "zhale: insertedUri: " + insertedUri);
            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            movieRowId = ContentUris.parseId(insertedUri);
            Log.e(LOG_TAG, "zhale: movieRowId returned from insertion: " + movieRowId);
        }

        movieCursor.close();
        return movieRowId;
    }

}
