package net.lezhang.udacity.movie;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MovieArrayAdapter extends ArrayAdapter<Movie> {
    private final String LOG_TAG = MovieArrayAdapter.class.getSimpleName();

    public MovieArrayAdapter(Activity context, List<Movie> movieList) {
        super(context, 0, movieList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie, parent, false);
        ImageView posterImageView = (ImageView) rootView.findViewById(R.id.list_item_movie_imageview);

        Movie movie = getItem(position);
        if(movie == null) {
            movie = new Movie();
        }
        URL posterUrl = MovieDataRetriever.getPosterUrl(movie.getPosterPath());
        Picasso.with(getContext()).load(posterUrl.toString()).into(posterImageView);

        return rootView;
    }
}
