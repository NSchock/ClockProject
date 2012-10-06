package com.shockwave.clockproj;


import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

public class StopwatchWidgetService extends Service {

    RemoteViews remoteViews;
    AppWidgetManager appWidgetManager;
    ComponentName widget;
    int appWidgetId;

    boolean startedBefore = false;

    long elapsedTime, sStart;
    private final int REFRESH_RATE = 100;
    private Handler sHandler = new Handler();
    private Runnable startStopwatch = new Runnable() {
        @Override
        public void run() {
            final long start = sStart;
            elapsedTime = System.currentTimeMillis() - start;
            updateStopwatch(elapsedTime);
            sHandler.postDelayed(this, REFRESH_RATE);
        }
    };

    private void updateStopwatch(long time) {
        int seconds = (int) time / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        long millis = time % 1000;
        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        remoteViews.setTextViewText(R.id.tvStopwatchWidget, String.format("%d : %02d : %02d", hours, minutes, seconds));
        remoteViews.setTextViewText(R.id.tvStopwatchMillisWidget, String.format(". %03d", millis));
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    SharedPreferences prefs;

    @Override
    public void onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.getBoolean("stopwatchsaves", true);
        startedBefore = prefs.getBoolean("startedBefore", false);
        if (startedBefore) {
            sStart = prefs.getLong("sStart", 0);
            elapsedTime = prefs.getLong("elapsedTime", 0);
        } else {
            sStart = 0;
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        appWidgetManager = AppWidgetManager.getInstance(this);
        widget = new ComponentName(this, StopwatchWidgetProvider.class);
        remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.stopwatch_widget_layout);
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        showStopButton();
        if (sStart == 0L) {
            sStart = System.currentTimeMillis();
        } else {
            sStart = System.currentTimeMillis() - elapsedTime;
        }
        sHandler.removeCallbacks(startStopwatch);
        sHandler.postDelayed(startStopwatch, REFRESH_RATE);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        startedBefore = true;
        sHandler.removeCallbacks(startStopwatch);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("sStart", sStart);
        editor.putLong("elapsedTime", elapsedTime);
        editor.putBoolean("startedBefore", startedBefore);
        editor.commit();
        hideStopButton();
        appWidgetManager.updateAppWidget(widget, remoteViews);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showStopButton() {
        remoteViews.setViewVisibility(R.id.bStopWidget, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.bStartWidget, View.GONE);
        remoteViews.setViewVisibility(R.id.bResetWidget, View.GONE);
    }

    private void hideStopButton() {
        remoteViews.setViewVisibility(R.id.bStopWidget, View.GONE);
        remoteViews.setViewVisibility(R.id.bStartWidget, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.bResetWidget, View.VISIBLE);
    }
}
