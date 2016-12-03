package net.lezhang.udacity.movie;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MovieDataRetriever {
    private static final String LOG_TAG = MovieDataRetriever.class.getSimpleName();

    private static Context context = null;
    public static void setContext(Context c) {
        context = c;
        API_KEY_VALUE = context.getString(R.string.api_key_movie);
    }

    public static String getJsonStr(URL url) {
        Log.v(LOG_TAG, "getting JSON with url: " + url);
        String jsonString = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }

            jsonString = buffer.toString();
            return jsonString;

        } catch (Exception e) {
            Log.e(LOG_TAG, "error retrieving JSON response", e);
            return null;

        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "error closing stream", e);
                }
            }
        }
    }

    public static Movie[] getMovieListFromJsonStr(String movieListJsonString)
            throws JSONException {
        JSONObject movieListJsonObject = new JSONObject(movieListJsonString);
        JSONArray movieJsonArray = movieListJsonObject.getJSONArray("results");
        int movieCount = movieJsonArray.length();
        Movie[] movieArray = new Movie[movieCount];
        for(int i = 0; i < movieCount; i++) {
            JSONObject movieJsonObject = movieJsonArray.getJSONObject(i);
            Movie movie = new Movie(movieJsonObject);
            movieArray[i] = movie;
        }
        return movieArray;
    }

    // following parts are for URL building

    private static final String BASE_URL =      "https://api.themoviedb.org/3/movie/";
    private static final String LIST_POPULAR =  "popular";
    private static final String LIST_TOPRATED = "top_rated";
    private static final String API_KEY =       "api_key";
    private static String API_KEY_VALUE;

    public static URL getListQueryUrl(SortOrder sortOrder) {
        Uri uri = null;
        if(sortOrder == SortOrder.POPULAR) {
            uri = Uri.parse(BASE_URL + LIST_POPULAR).buildUpon()
                    .appendQueryParameter(API_KEY, API_KEY_VALUE)
                    .build();
        } else if(sortOrder == SortOrder.TOP_RATED) {
            uri = Uri.parse(BASE_URL + LIST_TOPRATED).buildUpon()
                    .appendQueryParameter(API_KEY, API_KEY_VALUE)
                    .build();
        }
        try {
            return new URL(uri.toString());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error building list query URL", e);
            return null;
        }
    }

    private static final String BASE_URL_IMAGE = "http://image.tmdb.org/t/p/w185/";

    public static URL getPosterUrl(String posterPath) {
        // posterPath example: "/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg"
        try {
            return new URL(BASE_URL_IMAGE + posterPath);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error building poster URL", e);
            return null;
        }
    }

}
