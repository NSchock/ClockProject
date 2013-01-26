package com.shockwave.clockproj.free;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import com.jakewharton.notificationcompat2.NotificationCompat2;

public class StopwatchService extends Service {
    final static String START_ACTION = "START_ACTION";

    //Stopwatch Vars
    long customMillis, sStart, elapsedTime;
    boolean valueEntered = false;

    Intent broadcastIntent;

    private final int REFRESH_RATE = 1;
    private Handler sHandler = new Handler();
    private Runnable startStopwatch = new Runnable() {
        public void run() {
            final long start = sStart;
            elapsedTime = SystemClock.elapsedRealtime() - start;
            broadcastIntent.putExtra("elapsedTime", elapsedTime);
            sendBroadcast(broadcastIntent);
            sHandler.postDelayed(this, REFRESH_RATE);
        }
    };

    SharedPreferences preferences;
    boolean startedBefore = false;

    @Override
    public void onCreate() {
        broadcastIntent = new Intent();
        broadcastIntent.setAction(StopwatchService.START_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        preferences = getSharedPreferences("StopwatchServicePrefs", 0);
        preferences.getBoolean("stopwatchsaves", true);
        startedBefore = preferences.getBoolean("startedBefore", false);
        if (startedBefore) {
            sStart = preferences.getLong("sStart", 0);
            elapsedTime = preferences.getLong("elapsedTime", 0);
        } else {
            sStart = 0;
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

        valueEntered = intent.getBooleanExtra("valueEntered", false);
        customMillis = intent.getLongExtra("customMillis", 0);
        if (!valueEntered) {
            if (sStart == 0L) {
                sStart = SystemClock.elapsedRealtime();
            } else {
                sStart = SystemClock.elapsedRealtime() - elapsedTime;
            }
        } else {
            sStart = SystemClock.elapsedRealtime() - customMillis;
        }
        sHandler.removeCallbacks(startStopwatch);
        sHandler.postDelayed(startStopwatch, REFRESH_RATE);
        valueEntered = false;
        intent.removeExtra("valueEntered");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        startedBefore = true;
        stopForeground(true);
        sHandler.removeCallbacks(startStopwatch);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("elapsedTime", elapsedTime);
        editor.putLong("sStart", sStart);
        editor.putBoolean("startedBefore", startedBefore);
        editor.commit();

        super.onDestroy();
    }
}
