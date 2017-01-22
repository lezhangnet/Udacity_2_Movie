package net.lezhang.udacity.movie;

import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import net.lezhang.udacity.movie.data.MovieDataContract;

import org.w3c.dom.Text;

import java.net.URL;

public class MovieDetailActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();

    private static final int MOVIE_LOADER_ID = 0;

    public static final String DETAIL_URI = "URI";

    private int mMovieId = 0;

    private View mRootView = null;
    private TextView mTitleTextView = null;
    private ImageView mPosterImageView = null;
    private TextView mReleaseDateTextView = null;
    private TextView mRatingTextView = null;
    private TextView mPlotTextView = null;
    private TextView mReviewTextView = null;
    private TextView mVideoTextView = null;
    private VideoView mVideoView = null;
    private Uri mVideoUri = null;
    private Button mVideoButton = null;
    private ToggleButton mToggleFavorite = null;

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
        mReviewTextView = (TextView) rootView.findViewById(R.id.text_view_movie_review);
        mVideoTextView = (TextView) rootView.findViewById(R.id.text_view_movie_video);
        //mVideoView = (VideoView) rootView.findViewById(R.id.video_view_movie_trailer);

        mVideoButton = (Button) rootView.findViewById(R.id.button_trailer);
        mVideoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View buttonView) {
                Log.d(LOG_TAG, "watching video: ");
                Intent trailerIntent = new Intent(Intent.ACTION_VIEW);
                trailerIntent.setData(mVideoUri);
                if (trailerIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(trailerIntent);
                } else {
                    Log.e(LOG_TAG, "Error starting video activity: no video app installed");
                }
            }
        });

        mToggleFavorite = (ToggleButton) rootView.findViewById(R.id.toggle_button_favorite);
        mToggleFavorite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View buttonView) {
                boolean isChecked = mToggleFavorite.isChecked();
                if (isChecked) {
                    // The toggle is enabled / turned on
                    Log.d(LOG_TAG, "favoriting movie: ");
                    favoriteMovie();
                } else {
                    // The toggle is disabled / turned off
                    Log.d(LOG_TAG, "unfavoriting movie: ");
                    unfavoriteMovie();
                }
            }
        });

        return rootView;
    }

    private void favoriteMovie() {
        Log.d(LOG_TAG, "favoriteMovie()");
        Uri favoriteUri = MovieDataContract.FavoriteEntry.CONTENT_URI_FAVORITE;
        String dbKey = MovieDataContract.FavoriteEntry.COLUMN_MOVIE_ID;
        ContentValues movieValues = new ContentValues();
        movieValues.put(dbKey, mMovieId);
        Uri insertedUri = getContext().getContentResolver().insert(favoriteUri, movieValues);
        Log.d(LOG_TAG, "inserted " + favoriteUri + " as " + insertedUri);
    }

    private void unfavoriteMovie() {
        Log.d(LOG_TAG, "unfavoriteMovie()");
        Uri favoriteUri = MovieDataContract.FavoriteEntry.CONTENT_URI_FAVORITE;
        String dbKey = MovieDataContract.FavoriteEntry.COLUMN_MOVIE_ID;
        ContentValues movieValues = new ContentValues();
        movieValues.put(dbKey, mMovieId);
        int deleteCount = getContext().getContentResolver().delete(favoriteUri,
                MovieDataContract.FavoriteEntry.COLUMN_MOVIE_ID + "= ?",
                new String[]{Integer.toString(mMovieId)});
        Log.d(LOG_TAG, "deleted " + favoriteUri + " as " + deleteCount);
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
        Log.d(LOG_TAG, "onLoadFinished() - detail");
        if(!cursor.moveToFirst()) {
            Log.e(LOG_TAG, "zhale: detail: empty cursor returned");
            return;
        }

        int movieId = cursor.getInt(1);
        String title = cursor.getString(2);
        String posterPath = cursor.getString(4);
        String plot = cursor.getString(5);
        double rating = cursor.getDouble(6);
        String releaseDate = cursor.getString(7);

        String reviewJson = cursor.getString(8);
        String reviewText = MovieDataRetriever.getMovieReviewFromJsonStr(reviewJson);
        String videoJson = cursor.getString(9);
        String videoText = MovieDataRetriever.getMovieVideoLinksFromJsonStr(videoJson);
        String videoUrl = "https://www.youtube.com/watch?v=" + videoText;
        Log.e(LOG_TAG, "videoUrl: " + videoUrl);
        Uri videoUri = Uri.parse(videoUrl);
        Log.e(LOG_TAG, "videoUri: " + videoUri);
        mVideoUri = videoUri;

        mMovieId = movieId;
        mTitleTextView.setText(title);
        mReleaseDateTextView.setText(getString(R.string.detail_release) + "\n" + releaseDate);
        mRatingTextView.setText(getString(R.string.detail_rating) + "\n" + rating);
        mPlotTextView.setText(getString(R.string.detail_plot) + "\n" + plot);
        mReviewTextView.setText(reviewText);
        //mVideoTextView.setText(getString(R.string.detail_video) + "\n" + "www.youtube.com/watch?v=" + videoText);
        //mVideoView.setVideoURI(videoUri);
        //mVideoView.start();

        URL posterUrl = MovieDataRetriever.getPosterUrl(posterPath);
        Picasso.with(getActivity()).load(posterUrl.toString()).into(mPosterImageView);

        setToggleState();
    }

    private void setToggleState() {
        Log.d(LOG_TAG, "setToggleState()");

        ContentResolver resolver = getContext().getContentResolver();
        Cursor movieCursor = resolver.query(
                MovieDataContract.FavoriteEntry.CONTENT_URI_FAVORITE,
                null,
                MovieDataContract.FavoriteEntry.TABLE_NAME + "." + MovieDataContract.FavoriteEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{Integer.toString(mMovieId)},
                null);

        if (movieCursor.moveToFirst()) {
            mToggleFavorite.setChecked(true);
        } else {
            mToggleFavorite.setChecked(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // do nothing
    }

}
