package com.shockwave.clockproj;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import com.jakewharton.notificationcompat2.NotificationCompat2;

/**
 * Program: ClockProject
 * Author: Nolan Schock (Shockwave)
 * Created: 1/11/13 at 7:56 PM
 * Version:
 * Description:
 * Last Updated:
 * Recent Changes:
 * Future Additions:
 * Known Errors:
 */
public class StopwatchLoopService extends Service {
    final static String LOOP_START_ACTION = "LOOP_START_ACTION";

    //Stopwatch Vars
    private long loopStart, elapsedLoopTime;
    private boolean valueEntered = false, newLoop = true;

    private final int REFRESH_RATE = 1;
    private Handler sHandler = new Handler();
    private Runnable startLoopStopwatch = new Runnable() {
        public void run() {
            final long start = loopStart;
            elapsedLoopTime = SystemClock.elapsedRealtime() - start;
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(StopwatchLoopService.LOOP_START_ACTION);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("elapsedLoopTime", elapsedLoopTime);
            sendBroadcast(broadcastIntent);
            sHandler.postDelayed(this, REFRESH_RATE);
        }
    };

    SharedPreferences preferences;
    private boolean startedLoopBefore = false;

    @Override
    public void onCreate() {
        preferences = getSharedPreferences("StopwatchLoopServicePrefs", 0);
        preferences.getBoolean("stopwatchloopsaves", true);
        newLoop = preferences.getBoolean("newLoop", true);
        if (newLoop) {
            loopStart = 0;
            elapsedLoopTime = 0;
        } else {
            loopStart = preferences.getLong("loopStart", 0);
            elapsedLoopTime = preferences.getLong("elapsedLoopTime", 0);
        }
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //set up notification to start foreground service
        Intent notificationIntent = new Intent(getApplicationContext(), ClockMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        NotificationCompat2.Builder builder = new NotificationCompat2.Builder(getApplicationContext());
        builder.setContentTitle("Clock Project Stopwatch");
        builder.setContentText("Stopwatch is Running.");
        builder.setSmallIcon(R.drawable.ic_stat_stopwatch_running);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(51, notification);

        newLoop = preferences.getBoolean("newLoop", true);
        if (newLoop) {
            loopStart = 0;
            elapsedLoopTime = 0;
        } else {
            loopStart = preferences.getLong("loopStart", 0);
            elapsedLoopTime = preferences.getLong("elapsedLoopTime", 0);
        }

        if (loopStart == 0L) {
            loopStart = SystemClock.elapsedRealtime();
        } else {
            loopStart = SystemClock.elapsedRealtime() - elapsedLoopTime;
        }
        sHandler.removeCallbacks(startLoopStopwatch);
        sHandler.postDelayed(startLoopStopwatch, REFRESH_RATE);
        valueEntered = false;
        intent.removeExtra("valueEntered");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        startedLoopBefore = true;
        sHandler.removeCallbacks(startLoopStopwatch);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("loopStart", loopStart);
        editor.putLong("elapsedLoopTime", elapsedLoopTime);
        editor.putBoolean("startedLoopBefore", startedLoopBefore);
        editor.commit();

        stopForeground(true);
        super.onDestroy();
    }
}
