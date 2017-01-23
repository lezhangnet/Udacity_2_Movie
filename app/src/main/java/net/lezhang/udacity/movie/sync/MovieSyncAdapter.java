package net.lezhang.udacity.movie.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

/*
import com.example.android.sunshine.app.BuildConfig;
import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;
*/

import net.lezhang.udacity.movie.MainActivity;
import net.lezhang.udacity.movie.MainActivityFragment;
import net.lezhang.udacity.movie.Movie;
import net.lezhang.udacity.movie.MovieDataRetriever;
import net.lezhang.udacity.movie.R;
import net.lezhang.udacity.movie.data.MovieDataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the server, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3; // 1 hour

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.d(LOG_TAG, "constructor");
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync()");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sortOrderPref = prefs.getString(getContext().getString(R.string.pref_key_sort_order), "0");
        int sortOrder = Integer.valueOf(sortOrderPref);

        Log.d(LOG_TAG, "sortOrder: " + sortOrder);

        URL url = MovieDataRetriever.getListQueryUrl(sortOrder);
        String movieListJsonStr = MovieDataRetriever.getJsonStr(url);

        if(movieListJsonStr == null) {
            Log.e(LOG_TAG, "Null JSON string on sync");
            return;
        }

        Movie[] result;
        try {
            result = MovieDataRetriever.getMovieListFromJsonStr(movieListJsonStr);

            // add movie to movie db
            Log.d(LOG_TAG, "updating movie DB");
            for(Movie m : result) {
                if(!movieExistsInDb(m)) {
                    queryReviewsAndVideos(m);
                    addMovie(m);
                }
                // do nothing for existing movie
            }

            // update the list db table
            updateListDb(result, sortOrder);
        } catch(Exception e) {
            Log.e(LOG_TAG, "Error parsing JSON to movie list", e);
        }

        return;
    }

    private boolean movieExistsInDb(Movie m) {
        Log.d(LOG_TAG, "movieExistsInDb(): " + m);
        // First, check if the movie exists in the db
        ContentResolver resolver = getContext().getContentResolver();
        Cursor movieCursor = resolver.query(
                MovieDataContract.MovieEntry.CONTENT_URI,
                new String[]{MovieDataContract.MovieEntry._ID},
                MovieDataContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{Integer.toString(m.getId())},
                null);

        boolean result = movieCursor.moveToFirst();
        Log.e(LOG_TAG, "movieExistsInDb: " + result);

        movieCursor.close();
        return result;
    }

    private long addMovie(Movie m) {
        Log.d(LOG_TAG, "addMovie(): actually adding to DB");
        // Now that the content provider is set up, inserting rows of data is pretty simple.
        // First create a ContentValues object to hold the data you want to insert.
        ContentValues movieValues = new ContentValues();

        // Then add the data, along with the corresponding name of the data type,
        // so the content provider knows what kind of value is  being inserted.
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
        movieValues.put(MovieDataContract.MovieEntry.COLUMN_REVIEW_JSON,
                m.getReviewJson());
        movieValues.put(MovieDataContract.MovieEntry.COLUMN_VIDEO_JSON,
                m.getVideoJson());

        ContentResolver resolver = getContext().getContentResolver();
        Uri insertedUri = resolver.insert(
                MovieDataContract.MovieEntry.CONTENT_URI,
                movieValues
        );

        Log.e(LOG_TAG, "zhale: insertedUri: " + insertedUri);
        long movieRowId = ContentUris.parseId(insertedUri);
        Log.e(LOG_TAG, "zhale: movieRowId returned from insertion: " + movieRowId);

        return movieRowId;
    }

    private void queryReviewsAndVideos(Movie m) {
        Log.d(LOG_TAG, "queryReviewsAndVideos()");
        URL url = MovieDataRetriever.getCommentsUrl(m);
        String reviewJsonStr = MovieDataRetriever.getJsonStr(url);
        m.setReviewJson(reviewJsonStr);

        url = MovieDataRetriever.getVideosUrl(m);
        String videoJsonStr = MovieDataRetriever.getJsonStr(url);
        m.setVideoJson(videoJsonStr);
    }

    private void updateListDb(Movie[] movies, int sortOrder) {
        Log.d(LOG_TAG, "updateListDb, sortOrder: " + sortOrder);
        Uri listDbUri = null;
        String dbKey = null;
        if(sortOrder == MainActivityFragment.SORT_ORDER_POPULAR) {
            Log.d(LOG_TAG, "updating popular table");
            listDbUri = MovieDataContract.PopularEntry.CONTENT_URI_POPULAR;
            dbKey = MovieDataContract.PopularEntry.COLUMN_MOVIE_ID;
        } else if (sortOrder == MainActivityFragment.SORT_ORDER_TOPRATED) {
            Log.d(LOG_TAG, "updating toprated table");
            listDbUri = MovieDataContract.TopRatedEntry.CONTENT_URI_TOPRATED;
            dbKey = MovieDataContract.TopRatedEntry.COLUMN_MOVIE_ID;
        }

        // delete all records in the table first
        ContentResolver resolver = getContext().getContentResolver();
        if(listDbUri != null) {
            resolver.delete(listDbUri, null, null);
        } else {
            Log.e(LOG_TAG, "listDbUri is not matched for update from sortOrder: " + sortOrder);
            return;
        }

        for(Movie m : movies) {
            ContentValues movieValues = new ContentValues();
            movieValues.put(dbKey, m.getId());
            Uri insertedUri = resolver.insert(listDbUri, movieValues);
            Log.d(LOG_TAG, "inserted " + listDbUri + " as " + insertedUri);
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d(LOG_TAG, "syncImmediately()");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        Log.d(LOG_TAG, "onAccountCreated()");
        /*
         * Since we've created an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}