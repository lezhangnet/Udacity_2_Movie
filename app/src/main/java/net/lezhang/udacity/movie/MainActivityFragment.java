package net.lezhang.udacity.movie;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import net.lezhang.udacity.movie.data.MovieDataContract;
import net.lezhang.udacity.movie.sync.MovieSyncAdapter;

public class MainActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private static final int SORT_ORDER_INITIAL = 0;
    public static final int SORT_ORDER_POPULAR = 1;
    public static final int SORT_ORDER_TOPRATED = 2;
    public static final int SORT_ORDER_FAVORITE = 3;
    private int currentSortOrder = -1;

    private MovieCursorAdapter movieCursorAdapter;

    private GridView mMovieGridView = null;
    private int mSelectedPosition = ListView.INVALID_POSITION;
    private String SELECTED_POSITION_KEY = "selected_position";

    private static final int MOVIE_LOADER_ID = 0;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_movie, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected()");
        int id = item.getItemId();
        if (id == R.id.sort_mostpopular) {
            updateList(SORT_ORDER_POPULAR);
            return true;
        } else if (id == R.id.sort_toprated) {
            updateList(SORT_ORDER_TOPRATED);
            return true;
        } else if (id == R.id.sort_favorite) {
            updateList(SORT_ORDER_FAVORITE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieCursorAdapter = new MovieCursorAdapter(getActivity(), null, 0);

        GridView movieGridView = (GridView) rootView.findViewById(R.id.grid_view_movie);
        mMovieGridView = movieGridView;
        movieGridView.setAdapter(movieCursorAdapter);
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(LOG_TAG, "zhale: item clicked at position: " + position);
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    int movieId = cursor.getInt(1);
                    Uri movieUri = MovieDataContract.MovieEntry.buildMovieIdUri(movieId);
                    ((MovieCallback) getActivity()).onMovieItemSelected(movieUri);
                }
                mSelectedPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_POSITION_KEY)) {
            mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION_KEY);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onActivityCreated(): initializing Loader");

        // reading sort order preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrderPref = prefs.getString(getString(R.string.pref_key_sort_order), "0");
        int sortOrder = Integer.valueOf(sortOrderPref);
        currentSortOrder = sortOrder;
        Log.d(LOG_TAG, "sortOrder read from pref: " + sortOrder);

        getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "onStart()");
        super.onStart();
        updateList(SORT_ORDER_INITIAL);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSelectedPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_POSITION_KEY, mSelectedPosition);
        }
        super.onSaveInstanceState(outState);
    }

    private void updateList(int sortOrder) {
        // on initial call, currentSortOrder is already set from pref at this point
        Log.d(LOG_TAG, "updateList(): sortOrder: " + sortOrder + " currentSortOrder: " + currentSortOrder);
        if(sortOrder != currentSortOrder) {
            if (sortOrder == SORT_ORDER_POPULAR || sortOrder == SORT_ORDER_TOPRATED) {
                // store the newly selected sortOrder to preference
                // note: initial (0) or favorite (3) is not being persisted
                Log.d(LOG_TAG, "saving sort order to pref: " + sortOrder);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                prefs.edit()
                        .putString(getString(R.string.pref_key_sort_order), Integer.toString(sortOrder))
                        .apply();
            }

            // on initial, sortOrder is 0 and currentSortOrder is 1 or 2, so will trigger here
            if(sortOrder != SORT_ORDER_INITIAL) {
                currentSortOrder = sortOrder;
            }

            if(sortOrder != SORT_ORDER_FAVORITE) {
                Log.d(LOG_TAG, "force syncing");
                MovieSyncAdapter.syncImmediately(getActivity());
            }

            Log.e(LOG_TAG, "refreshing loader");
            getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
        }

    }


    /// LoaderManager implementations
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(LOG_TAG, "onCreateLoader(): currentSortOrder: " + currentSortOrder);
        Uri movieUri = null;
        String cursorSortOrder = null;

        if(currentSortOrder == SORT_ORDER_POPULAR) {
            movieUri = MovieDataContract.PopularEntry.CONTENT_URI_POPULAR;
        } else if (currentSortOrder == SORT_ORDER_TOPRATED) {
            movieUri = MovieDataContract.TopRatedEntry.CONTENT_URI_TOPRATED;
        } else if (currentSortOrder == SORT_ORDER_FAVORITE) {
            movieUri = MovieDataContract.FavoriteEntry.CONTENT_URI_FAVORITE;
        } else { // list all movies
            movieUri = MovieDataContract.MovieEntry.CONTENT_URI;
            cursorSortOrder = MovieDataContract.MovieEntry.COLUMN_MOVIE_ID + " ASC";
        }
        Log.d(LOG_TAG, "movieUri: " + movieUri);
        return new CursorLoader(getActivity(), movieUri, null, null, null, cursorSortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // data is ready, update UI
        Log.d(LOG_TAG, "onLoadFinished(): swapping cursor into CursorAdaptor. " +
                "cursor count: " + cursor.getCount());
        movieCursorAdapter.swapCursor(cursor);

        // restore the selected position
        if (mSelectedPosition != ListView.INVALID_POSITION) {
            mMovieGridView.smoothScrollToPosition(mSelectedPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(LOG_TAG, "onLoaderReset()");
        movieCursorAdapter.swapCursor(null);
    }


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface MovieCallback {
        /**
         * Callback for when an item has been selected.
         */
        public void onMovieItemSelected(Uri movieUri);
    }
}
