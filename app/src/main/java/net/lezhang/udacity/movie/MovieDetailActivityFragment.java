package net.lezhang.udacity.movie;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {
    private final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Movie movie = null;
        Intent intent = getActivity().getIntent();
        if(intent != null) {
            movie = (Movie) intent.getParcelableExtra("movie");
            if(movie == null) {
                Log.e(LOG_TAG, "null movie from intent");
                movie = new Movie();
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        TextView textViewMovieTitle = (TextView) rootView.findViewById(R.id.text_view_movie_title);
        textViewMovieTitle.setText(movie.getTitle());

        ImageView imageViewMoviePoster = (ImageView) rootView.findViewById(R.id.image_view_movie_poster);
        URL posterUrl = MovieDataRetriever.getPosterUrl(movie.getPosterPath());
        Picasso.with(getActivity()).load(posterUrl.toString()).into(imageViewMoviePoster);

        TextView textViewMovieRelease = (TextView) rootView.findViewById(R.id.text_view_movie_release_date);
        textViewMovieRelease.setText(getString(R.string.detail_release) + "\n" + movie.getReleaseDate());

        TextView textViewMovieRating = (TextView) rootView.findViewById(R.id.text_view_movie_rating);
        textViewMovieRating.setText(getString(R.string.detail_rating) + "\n" + movie.getRating());

        TextView textViewMoviePlot = (TextView) rootView.findViewById(R.id.text_view_movie_plot);
        textViewMoviePlot.setText(movie.getPlotOverview());

        return rootView;
    }
}
