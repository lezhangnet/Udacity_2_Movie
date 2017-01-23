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
        Log.d(LOG_TAG, "getting JSON with url: " + url);
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
                Log.e(LOG_TAG, "Empty buffer got from JSON Stream");
                return null;
            }

            jsonString = buffer.toString();
            return jsonString;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error retrieving JSON response: " + e.toString());
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

    public static String getMovieVideoLinksFromJsonStr(String movieVideoJsonString) {
        try {
            JSONObject movieVideoJsonObject = new JSONObject(movieVideoJsonString);
            JSONArray movieVideoJsonArray = movieVideoJsonObject.getJSONArray("results");
            int movieVideoCount = movieVideoJsonArray.length();
            if(movieVideoCount == 0) {
                return "";
            }
            String[] videoKeys = new String[movieVideoCount];
            for(int i = 0; i < movieVideoCount; i++) {
                JSONObject videoJsonObject = movieVideoJsonArray.getJSONObject(i);
                String videoKey = videoJsonObject.getString("key");
                videoKeys[i] = videoKey;
            }
            return videoKeys[0];
        } catch(Exception e) {
            Log.e(LOG_TAG, "Error parsing movieVideoJsonString: " + e.toString());
            return "";
        }
    }

    public static String getMovieReviewFromJsonStr(String movieReviewJsonString) {
        String result = "";
        try {
            JSONObject movieReviewJsonObject = new JSONObject(movieReviewJsonString);
            JSONArray movieReviewJsonArray = movieReviewJsonObject.getJSONArray("results");
            int movieReviewCount = movieReviewJsonArray.length();
            if(movieReviewCount == 0) {
                return result;
            }
            String[] reviews = new String[movieReviewCount];
            for(int i = 0; i < movieReviewCount; i++) {
                JSONObject reviewJsonObject = movieReviewJsonArray.getJSONObject(i);
                String author = reviewJsonObject.getString("author");
                String review = reviewJsonObject.getString("content");
                reviews[i] = review;
                result += "Review " + (i+1) + " by " + author + ":\n\n" + review + "\n\n";
            }
            return result;
        } catch(Exception e) {
            Log.e(LOG_TAG, "Error parsing movieReviewJsonString: " + e.toString());
            return "";
        }
    }

    // following parts are for URL building

    private static final String BASE_URL =       "https://api.themoviedb.org/3/movie/";
    private static final String BASE_URL_IMAGE = "http://image.tmdb.org/t/p/w185/";
    private static final String LIST_POPULAR =   "popular";
    private static final String LIST_TOPRATED =  "top_rated";
    private static final String PATH_VIDEOS =    "/videos";
    private static final String PATH_REVIEWS =   "/reviews";
    private static final String API_KEY =        "api_key";
    private static String API_KEY_VALUE;

    public static URL getListQueryUrl(int sortOrderInt) {
        Uri uri = null;
        if(sortOrderInt == MainActivityFragment.SORT_ORDER_POPULAR) {
            uri = Uri.parse(BASE_URL + LIST_POPULAR).buildUpon()
                    .appendQueryParameter(API_KEY, API_KEY_VALUE)
                    .build();
        } else if(sortOrderInt == MainActivityFragment.SORT_ORDER_TOPRATED) {
            uri = Uri.parse(BASE_URL + LIST_TOPRATED).buildUpon()
                    .appendQueryParameter(API_KEY, API_KEY_VALUE)
                    .build();
        }
        try {
            return new URL(uri.toString());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error building list query url", e);
            return null;
        }
    }

    public static URL getPosterUrl(String posterPath) {
        // posterPath example: "/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg"
        try {
            return new URL(BASE_URL_IMAGE + posterPath);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error building poster url", e);
            return null;
        }
    }

    public static URL getCommentsUrl(Movie movie) {
        try {
            Uri uri = Uri.parse(BASE_URL + movie.getId() + PATH_REVIEWS).buildUpon()
                    .appendQueryParameter(API_KEY, API_KEY_VALUE)
                    .build();
            return new URL(uri.toString());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error building comments url", e);
            return null;
        }
    }

    public static URL getVideosUrl(Movie movie) {
        try {
            Uri uri = Uri.parse(BASE_URL + movie.getId() + PATH_VIDEOS).buildUpon()
                    .appendQueryParameter(API_KEY, API_KEY_VALUE)
                    .build();
            return new URL(uri.toString());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error building videos url", e);
            return null;
        }
    }

}
