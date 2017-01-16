package net.lezhang.udacity.movie;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.lezhang.udacity.movie.data.MovieContentProvider;

import java.net.URL;

/**
 * {@link MovieCursorAdapter} exposes a list of movies
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class MovieCursorAdapter extends CursorAdapter {
    private final String LOG_TAG = MovieCursorAdapter.class.getSimpleName();

    /**
     * Cache of the children views for a movie list item.
     * So no need to find by id for recycled view.
     */
    public static class ViewHolder {
        public final ImageView posterView;
        public final TextView  dateView;
        public final TextView  titleView;
        public final TextView  ratingView;
        public final TextView  plotView;

        public final ImageView listPosterView;
        //public final TextView listTitleView;

        public ViewHolder(View view) {
            posterView = (ImageView) view.findViewById(R.id.image_view_movie_poster);
            dateView =   (TextView)  view.findViewById(R.id.text_view_movie_release_date);
            titleView =  (TextView)  view.findViewById(R.id.text_view_movie_title);
            ratingView = (TextView)  view.findViewById(R.id.text_view_movie_rating);
            plotView =   (TextView)  view.findViewById(R.id.text_view_movie_plot);

            listPosterView = (ImageView) view.findViewById(R.id.list_item_movie_imageview);
            //listTitleView = (TextView) view.findViewById(R.id.list_item_movie_textview);
        }
    }

    public MovieCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(LOG_TAG, "bindView(): cursor: " + cursor.getColumnNames());

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //String posterPath = cursor.getString(4); // for movie list
        String posterPath = cursor.getString(6); // for popular list
        //Log.d(LOG_TAG, "posterPath from cursor: " + posterPath);
        //Log.d(LOG_TAG, "cursor test 2: " + cursor.getString(2));
        //Log.d(LOG_TAG, "cursor test 3: " + cursor.getString(3));
        //Log.d(LOG_TAG, "cursor test 5: " + cursor.getString(5));
        //Log.d(LOG_TAG, "cursor test 6: " + cursor.getString(6));

        URL posterUrl = MovieDataRetriever.getPosterUrl(posterPath);
        Picasso.with(view.getContext()).load(posterUrl.toString()).into(viewHolder.listPosterView);

        // for debug only
        //String title = cursor.getString(2);
        //viewHolder.listTitleView.setText(title);
    }
}