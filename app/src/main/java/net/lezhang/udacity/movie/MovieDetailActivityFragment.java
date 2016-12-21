package net.lezhang.udacity.movie;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.lezhang.udacity.movie.data.MovieDataContract;

import java.net.URL;

public class MovieDetailActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();

    private static final int MOVIE_LOADER_ID = 0;

    public static final String DETAIL_URI = "URI";

    private View mRootView = null;
    private TextView mTitleTextView = null;
    private ImageView mPosterImageView = null;
    private TextView mReleaseDateTextView = null;
    private TextView mRatingTextView = null;
    private TextView mPlotTextView = null;

    private Uri mUri = null;

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null) {
            mUri = args.getParcelable(DETAIL_URI);
        }

        Movie movie = null;
        Intent intent = getActivity().getIntent();
        if(intent != null) {
            // movie = (Movie) intent.getParcelableExtra("movie");

            Log.e(LOG_TAG, "zhale: detail: " + intent.getDataString());

            if(movie == null) {
                Log.e(LOG_TAG, "null movie from intent");
                movie = new Movie();
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        mTitleTextView = (TextView) rootView.findViewById(R.id.text_view_movie_title);
        mPosterImageView = (ImageView) rootView.findViewById(R.id.image_view_movie_poster);
        mReleaseDateTextView = (TextView) rootView.findViewById(R.id.text_view_movie_release_date);
        mRatingTextView = (TextView) rootView.findViewById(R.id.text_view_movie_rating);
        mPlotTextView = (TextView) rootView.findViewById(R.id.text_view_movie_plot);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // LoaderManager implementations
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.e(LOG_TAG, "zhale: detail - onCreateLoader()");
        /*
        Intent intent = getActivity().getIntent();
        if (intent == null || intent.getData() == null) {
            return null;
        }
        */
        Uri movieUri = mUri; // (intent == null) ? null : intent.getData();
        if(mUri == null) {
            Log.e(LOG_TAG, "zhale: null mUri at Detail Fragment");
            return null;
        }
        Log.e(LOG_TAG, "zhale: movieUri: " + movieUri);
        return new CursorLoader(getActivity(), movieUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // data is ready, update UI
        Log.e(LOG_TAG, "zhale: detail - onLoadFinished()");
        if(!cursor.moveToFirst()) {
            Log.e(LOG_TAG, "zhale: detail: empty cursor returned");
            return;
        }

        String title = cursor.getString(2);
        String releaseDate = cursor.getString(7);
        double rating = cursor.getDouble(6);
        String plot = cursor.getString(5);

        String posterPath = cursor.getString(4);

        Log.e(LOG_TAG, "zhale: detail: " + posterPath);

        mTitleTextView.setText(title);
        mReleaseDateTextView.setText(getString(R.string.detail_release) + "\n" + releaseDate);
        mRatingTextView.setText(getString(R.string.detail_rating) + "\n" + rating);
        mPlotTextView.setText(plot);

        URL posterUrl = MovieDataRetriever.getPosterUrl(posterPath);
        Picasso.with(getActivity()).load(posterUrl.toString()).into(mPosterImageView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // do nothing
    }

}
