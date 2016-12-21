package net.lezhang.udacity.movie.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import net.lezhang.udacity.movie.data.MovieDataContract.MovieEntry;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {
    static final String TEST_LOCATION = "99705";
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    /*
    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }
*/

    /**
     * Validate the record retrieved from test db is the same as the test value inserted.
     * @param valueCursor
     * @param expectedValues
     */
    static void validateCurrentRecord(Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found in Cursor.",
                    idx == -1);

            String expectedValue = entry.getValue().toString();
            String cursorValue = valueCursor.getString(idx);
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" + expectedValue + "'. ",
                    expectedValue, cursorValue);
        }
    }

    /**
     * Create some default movie values for database tests.
     */
    static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();

        movieValues.put(MovieEntry.COLUMN_MOVIE_ID, 123);
        movieValues.put(MovieEntry.COLUMN_TITLE, "Test Movie Title");
        movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, "Test Movie Original Title");
        movieValues.put(MovieEntry.COLUMN_POSTER_PATH, "/testposterpath");
        movieValues.put(MovieEntry.COLUMN_PLOT_OVERVIEW, "Test Movie Plot Overview");
        movieValues.put(MovieEntry.COLUMN_RATING, 4.5);
        movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, "2016/12/11");

        return movieValues;
    }

    /*
        Students: You can uncomment this helper function once you have finished creating the
        LocationEntry part of the WeatherContract.
    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION);
        testValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, "North Pole");
        testValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, 64.7488);
        testValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, -147.353);

        return testValues;
    }
     */

    /*
        Students: You can uncomment this function once you have finished creating the
        LocationEntry part of the WeatherContract as well as the WeatherDbHelper.
    static long insertNorthPoleLocationValues(Context context) {
        // insert our test records into the database
        WeatherDbHelper dbHelper = new WeatherDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

        long locationRowId;
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        return locationRowId;
    }
     */

    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
    */

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

}