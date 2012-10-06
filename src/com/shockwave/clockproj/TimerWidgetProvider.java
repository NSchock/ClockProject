package com.shockwave.clockproj;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

public class TimerWidgetProvider extends AppWidgetProvider {
    public static String ACTION_TIMER_WIDGET_RESET = "ActionReceiverResetTimer";
    public static String ACTION_TIMER_WIDGET_START = "ActionReceiverStartTimer";
    public static String ACTION_TIMER_WIDGET_STOP = "ActionReceiverStopTimer";
    public static String ACTION_TIMER_WIDGET_SET_TIME = "ActionReceiverSetTimer";

    ComponentName widget;
    AppWidgetManager awManager;
    RemoteViews remoteViews;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.timer_widget_layout);

            Intent btnStartTimerIntent = new Intent(context, TimerWidgetProvider.class);
            btnStartTimerIntent.setAction(ACTION_TIMER_WIDGET_START);
            PendingIntent piStart = PendingIntent.getBroadcast(context, 0, btnStartTimerIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.bStartTimerWidget, piStart);

            Intent btnStartIntent = new Intent(context, TimerWidgetProvider.class);
            btnStartIntent.setAction(ACTION_TIMER_WIDGET_STOP);
            PendingIntent piStop = PendingIntent.getBroadcast(context, 0, btnStartIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.bStopTimerWidget, piStop);

            Intent btnResetTimerIntent = new Intent(context, TimerWidgetProvider.class);
            btnResetTimerIntent.setAction(ACTION_TIMER_WIDGET_RESET);
            PendingIntent piReset = PendingIntent.getBroadcast(context, 0, btnResetTimerIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.bResetTimerWidget, piReset);

            Intent setTimesIntent = new Intent(context, TimerWidgetProvider.class);
            setTimesIntent.setAction(ACTION_TIMER_WIDGET_SET_TIME);
            PendingIntent piSetTimes = PendingIntent.getBroadcast(context, 0, setTimesIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.timer_relative_widget, piSetTimes);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.timer_widget_layout);
        widget = new ComponentName(context, TimerWidgetProvider.class);
        awManager = AppWidgetManager.getInstance(context);
        Intent myIntent = new Intent(context, TimerWidgetService.class);
        if (intent.getAction().equals(ACTION_TIMER_WIDGET_SET_TIME)) {
            Intent intent1 = new Intent(context, TimerWidgetDialogActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
        if (intent.getAction().equals(ACTION_TIMER_WIDGET_START)) {
            showStopButtonTimerWidget();
            context.startService(myIntent);
        }
        if (intent.getAction().equals(ACTION_TIMER_WIDGET_STOP)) {
            hideStopButtonTimerWidget();
            context.stopService(myIntent);
        }
        if (intent.getAction().equals(ACTION_TIMER_WIDGET_RESET)) {
            remoteViews.setTextViewText(R.id.tvTimerWidget, "0 : 00 : 00");
            remoteViews.setTextViewText(R.id.tvTimerMillisWidget, ". 000");
            TimerWidgetService service = new TimerWidgetService();
            service.prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = service.prefs.edit();
            editor.clear();
            editor.commit();
        }
        awManager.updateAppWidget(widget, remoteViews);
        super.onReceive(context, intent);
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
