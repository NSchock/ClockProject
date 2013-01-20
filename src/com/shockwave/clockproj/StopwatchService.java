package com.shockwave.clockproj;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import com.jakewharton.notificationcompat2.NotificationCompat2;

public class StopwatchService extends Service {
    final static String START_ACTION = "START_ACTION";

    //Stopwatch Vars
    long customMillis, sStart, elapsedTime;
    boolean valueEntered = false;

    private final int REFRESH_RATE = 100;
    private Handler sHandler = new Handler();
    private Runnable startStopwatch = new Runnable() {
        public void run() {
            final long start = sStart;
            elapsedTime = SystemClock.elapsedRealtime() - start;
            updateStopwatch(elapsedTime);
            sHandler.postDelayed(this, REFRESH_RATE);
        }
    };

    public void updateStopwatch(long time) {
        int seconds = (int) time / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        long millis = time % 1000;
        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(StopwatchService.START_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("stopwatchMain", String.format("%d : %02d : %02d", hours, minutes,
                seconds));
        broadcastIntent.putExtra("stopwatchMillis", String.format(". %03d", millis));
        sendBroadcast(broadcastIntent);
    }

    SharedPreferences preferences;
    boolean startedBefore = false;

    @Override
    public void onCreate() {
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
        Log.d("Values", String.valueOf(valueEntered) + "," + String.valueOf("customMillis"));
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
        sHandler.removeCallbacks(startStopwatch);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("sStart", sStart);
        editor.putLong("elapsedTime", elapsedTime);
        editor.putBoolean("startedBefore", startedBefore);
        editor.commit();

        stopForeground(true);
        super.onDestroy();
    }
}
