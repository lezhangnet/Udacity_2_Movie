package net.lezhang.udacity.movie;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Movie implements Parcelable {
    private int id;
    private String title;
    private String originalTitle;
    private String posterPath;
    private String plotOverview;
    private double rating;
    private String releaseDate;

    public Movie() {
        id =            0;
        title =         "testTitle";
        originalTitle = "testOritinalTitle";
        posterPath =    "testPosterPath";
        plotOverview =  "testPlotOverview";
        rating =        4.9;
        releaseDate =   "testReleaseDate";
    }

    public Movie(JSONObject movieJsonObject)
        throws JSONException {
        id =            movieJsonObject.getInt("id");
        title =         movieJsonObject.getString("title");
        originalTitle = movieJsonObject.getString("original_title");
        posterPath =    movieJsonObject.getString("poster_path");
        plotOverview =  movieJsonObject.getString("overview");
        rating =        movieJsonObject.getDouble("vote_average");
        releaseDate =   movieJsonObject.getString("release_date");
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getPlotOverview() {
        return plotOverview;
    }

    public double getRating() {
        return rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    @Override
    public String toString() {
        return id + " " + title;
    }

    // The following part is Parcelable implementation

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(title);
        out.writeString(originalTitle);
        out.writeString(posterPath);
        out.writeString(plotOverview);
        out.writeDouble(rating);
        out.writeString(releaseDate);
    }

    private Movie(Parcel in) {
        id =            in.readInt();
        title =         in.readString();
        originalTitle = in.readString();
        posterPath =    in.readString();
        plotOverview =  in.readString();
        rating =        in.readDouble();
        releaseDate =   in.readString();
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
