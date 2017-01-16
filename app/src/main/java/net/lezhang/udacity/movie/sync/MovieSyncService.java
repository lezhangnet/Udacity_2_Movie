package net.lezhang.udacity.movie.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MovieSyncService extends Service {
    private static final String LOG_TAG = MovieSyncService.class.getSimpleName();

    private static final Object syncAdapterLock = new Object();
    private static MovieSyncAdapter movieSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate()");
        synchronized (syncAdapterLock) {
            if (movieSyncAdapter == null) {
                movieSyncAdapter = new MovieSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind()");
        return movieSyncAdapter.getSyncAdapterBinder();
    }
}
