package net.lezhang.udacity.movie;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import net.lezhang.udacity.movie.data.MovieDataContract;
import net.lezhang.udacity.movie.data.MovieDataContract.MovieEntry;

public class FetchMovieTask extends AsyncTask<SortOrder, Void, Movie[]> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private final Context mContext;
    private ArrayList<Movie> mMovies;
    private MovieArrayAdapter mMovieArrayAdapter;
    private MovieCursorAdapter mMovieCursorAdapter;

    public FetchMovieTask(Context context,
                          ArrayList<Movie> movies,
                          MovieArrayAdapter movieArrayAdapter) {
        mContext = context;
        mMovies = movies;
        mMovieArrayAdapter = movieArrayAdapter;
    }

    public FetchMovieTask(Context context,
                          ArrayList<Movie> movies,
                          MovieCursorAdapter movieCursorAdapter) {
        mContext = context;
        mMovies = movies;
        mMovieCursorAdapter = movieCursorAdapter;
    }

    @Override
    public Movie[] doInBackground(SortOrder... params) {
        // validate required params
        if (params.length == 0) {
            return null;
        }

        URL url = MovieDataRetriever.getListQueryUrl(params[0]);
        String movieListJsonStr = MovieDataRetriever.getJsonStr(url);

        if(movieListJsonStr == null) {
            Log.e(LOG_TAG, "null JSON string returned");
            return null;
        }

        Movie[] result;
        try {
            result = MovieDataRetriever.getMovieListFromJsonStr(movieListJsonStr);


            for(Movie m : result) {
                addMovie(m);
            }


            return result;
        } catch(Exception e) {
            Log.e(LOG_TAG, "error parsing JSON to movie list", e);
        }

        return null;
    }

    @Override
    public void onPostExecute(Movie[] result) {
        Log.e(LOG_TAG, "zhale: onPostExecute()");
        if (result != null) {
            mMovies.clear();
            mMovies.addAll(Arrays.asList(result));
            //mMovieArrayAdapter.notifyDataSetChanged();
            mMovieCursorAdapter.notifyDataSetChanged();
        } else {
            String text = "Failed to retrieve movie data from cloud. Please double check the API key, the internet connection, and try again later.";
            Log.e(LOG_TAG, text);
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(mContext, text, duration);
            toast.show();
        }
    }

    long addMovie(Movie m) {
        Log.e(LOG_TAG, "zhale !!!OLD IN TASK!!!: try adding movie " + m + " to DB");
        long movieRowId = 0;

        // First, check if the movie exists in the db
        ContentResolver resolver = mContext.getContentResolver();
        Cursor movieCursor = resolver.query(
                MovieEntry.CONTENT_URI,
                new String[]{MovieEntry._ID},
                MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{Integer.toString(m.getId())},
                null);

        if (movieCursor.moveToFirst()) {
            //Log.e(LOG_TAG, "zhale: movie existing, do nothing");
            // the movie exists in our database
            //int movieIdIndex = movieCursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID);
            //movieRowId = movieCursor.getInt(movieIdIndex);
        } else {
            //Log.e(LOG_TAG, "actually adding to DB");
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues movieValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            movieValues.put(MovieEntry.COLUMN_MOVIE_ID,
                    m.getId());
            movieValues.put(MovieEntry.COLUMN_TITLE,
                    m.getTitle());
            movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE,
                    m.getOriginalTitle());
            movieValues.put(MovieEntry.COLUMN_RELEASE_DATE,
                    m.getReleaseDate());
            movieValues.put(MovieEntry.COLUMN_POSTER_PATH,
                    m.getPosterPath());
            movieValues.put(MovieEntry.COLUMN_PLOT_OVERVIEW,
                    m.getPlotOverview());
            movieValues.put(MovieEntry.COLUMN_RATING,
                    m.getRating());

            // Finally, insert movie data into the database.
            Uri insertedUri = resolver.insert(
                    MovieEntry.CONTENT_URI,
                    movieValues
            );

            //Log.e(LOG_TAG, "zhale: insertedUri: " + insertedUri);
            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            movieRowId = ContentUris.parseId(insertedUri);
            //Log.e(LOG_TAG, "zhale: movieRowId returned from insertion: " + movieRowId);
        }

        movieCursor.close();
        return movieRowId;
    }
}
