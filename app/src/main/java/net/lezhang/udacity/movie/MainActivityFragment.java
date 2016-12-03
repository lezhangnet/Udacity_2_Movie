package net.lezhang.udacity.movie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivityFragment extends Fragment {
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private SortOrder currentSortOrder = null;
    private ArrayList<Movie> movies = new ArrayList<>();
    private MovieArrayAdapter movieArrayAdapter;

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
        int id = item.getItemId();
        if (id == R.id.sort_mostpopular) {
            updateList(SortOrder.POPULAR);
            return true;
        } else if (id == R.id.sort_toprated) {
            updateList(SortOrder.TOP_RATED);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieArrayAdapter = new MovieArrayAdapter(getActivity(), movies);
        GridView movieGridView = (GridView) rootView.findViewById(R.id.grid_view_movie);
        movieGridView.setAdapter(movieArrayAdapter);
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movieArrayAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class)
                        .putExtra("movie", movie);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateList(null);
    }

    private void updateList(SortOrder sortOrder) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(sortOrder == null) {
            // try to get sortOrder from preference
            String sortOrderPref = prefs.getString(getString(R.string.pref_key_sort_order), "0");
            int sortOrderInt = Integer.valueOf(sortOrderPref);
            sortOrder = SortOrder.fromInt(sortOrderInt);
        } else {
            // store the sortOrder to preference
            prefs.edit().putString(getString(R.string.pref_key_sort_order), Integer.toString(sortOrder.getValue())).apply();
        }
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder;
            new MovieListTask().execute(sortOrder);
        }
    }

    public class MovieListTask extends AsyncTask<SortOrder, Void, Movie[]> {
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
                return result;
            } catch(Exception e) {
                Log.e(LOG_TAG, "error parsing JSON to movie list", e);
            }

            return null;
        }

        @Override
        public void onPostExecute(Movie[] result) {
            if (result != null) {
                movies.clear();
                movies.addAll(Arrays.asList(result));
                movieArrayAdapter.notifyDataSetChanged();
            } else {
                String text = "Failed to retrieve movie data from cloud. Please double check the API key, the internet connection, and try again later.";
                Log.e(LOG_TAG, text);
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(getActivity(), text, duration);
                toast.show();
            }
        }
    }
}

enum SortOrder {
    POPULAR(1), TOP_RATED(2);

    private static final String LOG_TAG = SortOrder.class.getSimpleName();

    private final int value;

    SortOrder(int value) {
        this.value = value;
    }

    public int getValue() {
        return  value;
    }

    private static final Map<Integer, SortOrder> intToSortOrderMap = new HashMap<>();
    static {
        for (SortOrder sortOrder : SortOrder.values()) {
            intToSortOrderMap.put(sortOrder.value, sortOrder);
        }
    }

    public static SortOrder fromInt(int i) {
        SortOrder sortOrder = intToSortOrderMap.get(i);
        if (sortOrder == null) {
            Log.w(LOG_TAG, "invalid sort order, using default");
            return SortOrder.POPULAR;
        }
        return sortOrder;
    }
}
