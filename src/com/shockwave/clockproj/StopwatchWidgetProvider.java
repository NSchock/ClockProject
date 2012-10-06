package com.shockwave.clockproj;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class StopwatchWidgetProvider extends AppWidgetProvider {
    public static String ACTION_STOPWATCH_WIDGET_RESET = "ActionReceiverReset";
    public static String ACTION_STOPWATCH_WIDGET_START = "ActionReceiverStart";
    public static String ACTION_STOPWATCH_WIDGET_STOP = "ActionReceiverStop";

    ComponentName widget;
    AppWidgetManager awManager;

    RemoteViews remoteViews;
    int[] allWidgetIds;
    int appWidgetId;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        allWidgetIds = appWidgetIds;
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            appWidgetId = appWidgetIds[i];
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.stopwatch_widget_layout);

            Intent btnStartIntent = new Intent(context, StopwatchWidgetProvider.class);
            btnStartIntent.setAction(ACTION_STOPWATCH_WIDGET_START);
            PendingIntent piStart = PendingIntent.getBroadcast(context, 0, btnStartIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.bStartWidget, piStart);

            Intent btnStopIntent = new Intent(context, StopwatchWidgetProvider.class);
            btnStopIntent.setAction(ACTION_STOPWATCH_WIDGET_STOP);
            PendingIntent piStop = PendingIntent.getBroadcast(context, 0, btnStopIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.bStopWidget, piStop);

            Intent btnResetIntent = new Intent(context, StopwatchWidgetProvider.class);
            btnResetIntent.setAction(ACTION_STOPWATCH_WIDGET_RESET);
            PendingIntent piReset = PendingIntent.getBroadcast(context, 0, btnResetIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.bResetWidget, piReset);

            StopwatchWidgetService service = new StopwatchWidgetService();
            service.prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = service.prefs.edit();
            editor.clear();
            editor.commit();

            Intent serviceIntent = new Intent(context, StopwatchWidgetService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            Intent thisIntent = new Intent(context, StopwatchWidgetProvider.class);
            thisIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        widget = new ComponentName(context, StopwatchWidgetProvider.class);
        awManager = AppWidgetManager.getInstance(context);
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.stopwatch_widget_layout);
        Intent serviceIntent = new Intent(context, StopwatchWidgetService.class);
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        Uri data = Uri.withAppendedPath(Uri.parse("stopwatchwidget://widget/id/" + appWidgetId), String.valueOf(appWidgetId));
        serviceIntent.setData(data);
        if (intent.getAction().equals(ACTION_STOPWATCH_WIDGET_START)) {
            context.startService(serviceIntent);
        }
        if (intent.getAction().equals(ACTION_STOPWATCH_WIDGET_STOP)) {
            context.stopService(serviceIntent);
        }
        if (intent.getAction().equals(ACTION_STOPWATCH_WIDGET_RESET)) {
            remoteViews.setTextViewText(R.id.tvStopwatchWidget, "0 : 00 : 00");
            remoteViews.setTextViewText(R.id.tvStopwatchMillisWidget, ". 000");
            StopwatchWidgetService service = new StopwatchWidgetService();
            service.prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = service.prefs.edit();
            editor.clear();
            editor.commit();
        }
        awManager.updateAppWidget(appWidgetId, remoteViews);
        super.onReceive(context, intent);
    }

}
