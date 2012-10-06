package com.shockwave.clockproj;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

public class TimerWidgetService extends Service {

    ComponentName widget;
    AppWidgetManager awManager;
    RemoteViews remoteViews;

    int cdHour, cdMin, cdSec, cdMillis;
    boolean timerRunning = false;
    CountDownTimer countDownTimer;

    SharedPreferences prefs;

    @Override
    public void onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.getBoolean("timersaves", true);
        cdHour = prefs.getInt("cdHour", 0);
        cdMin = prefs.getInt("cdMin", 0);
        cdSec = prefs.getInt("cdSec", 0);
        cdMillis = prefs.getInt("cdMillis", 0);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.timer_widget_layout);
        widget = new ComponentName(getApplicationContext(), TimerWidgetProvider.class);
        awManager = AppWidgetManager.getInstance(getApplicationContext());
        boolean timeChanged = intent.getBooleanExtra("timeChanged", false);
        if (timeChanged) {
            cdHour = intent.getIntExtra("cdHour", 0);
            cdMin = intent.getIntExtra("cdMin", 0);
            cdSec = intent.getIntExtra("cdSec", 0);
        } else {
            showStopButtonTimerWidget();
            if (!timerRunning) {
                if ((cdHour != 0) || (cdSec != 0) || (cdMin != 0) || (cdMillis != 0))
                    timerRunning = true;
            }
            cdMin += cdHour * 60;
            cdSec += cdMin * 60;
            cdMillis += cdSec * 1000;
            countDownTimer = new CountDownTimer(cdMillis, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    cdSec = ((int) millisUntilFinished / 1000);
                    cdMin = (cdSec / 60);
                    cdHour = (cdMin / 60);
                    cdMin %= 60;
                    cdSec %= 60;
                    cdHour %= 24;
                    cdMillis = ((int) millisUntilFinished % 1000);
                    updateTimerWidgetTextView();
                }

                @Override
                public void onFinish() {
                    hideStopButtonTimerWidget();
                    cdMillis = 0;
                    updateTimerWidgetTextView();
                    timerRunning = false;

                    String ns = Context.NOTIFICATION_SERVICE;
                    NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
                    Notification.Builder builder = new Notification.Builder(getApplicationContext());

                    Intent notificationIntent = new Intent(getApplicationContext(), TimerWidgetProvider.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
                    builder
                            .setContentTitle("Clock Project Timer Widget")
                            .setContentText("Time is up!")
                            .setTicker("Time Up")
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL);

                    Notification notification = builder.build();
                    notificationManager.notify(R.drawable.ic_launcher, notification);
                }
            }.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("cdHour", cdHour);
        editor.putInt("cdMin", cdMin);
        editor.putInt("cdSec", cdSec);
        editor.putInt("cdMillis", cdMillis);
        editor.commit();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateTimerWidgetTextView() {
        remoteViews.setTextViewText(R.id.tvTimerWidget, String.format("%d : %02d : %02d", cdHour, cdMin,
                cdSec));
        remoteViews.setTextViewText(R.id.tvTimerMillisWidget, String.format(". %03d", cdMillis));
        awManager.updateAppWidget(widget, remoteViews);
    }

    private void showStopButtonTimerWidget() {
        remoteViews.setViewVisibility(R.id.bStopTimerWidget, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.bStartTimerWidget, View.GONE);
        remoteViews.setViewVisibility(R.id.bResetTimerWidget, View.GONE);
    }

    private void hideStopButtonTimerWidget() {
        remoteViews.setViewVisibility(R.id.bStopTimerWidget, View.GONE);
        remoteViews.setViewVisibility(R.id.bStartTimerWidget, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.bResetTimerWidget, View.VISIBLE);
    }
}
