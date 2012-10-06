package com.shockwave.clockproj;


import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RemoteViews;

public class TimerWidgetDialogActivity extends Activity implements NumberPicker.OnValueChangeListener, View.OnClickListener {

    NumberPicker npHourWidget, npMinWidget, npSecWidget;
    private int cdHour, cdMin, cdSec, cdMillis;
    Button btnEnterTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_widget_numberpicker_dialog);

        npHourWidget = (NumberPicker) findViewById(R.id.npHoursWidget);
        npMinWidget = (NumberPicker) findViewById(R.id.npMinutesWidget);
        npSecWidget = (NumberPicker) findViewById(R.id.npSecondsWidget);
        npHourWidget.setMaxValue(23);
        npHourWidget.setMinValue(0);
        npSecWidget.setMaxValue(59);
        npSecWidget.setMinValue(0);
        npMinWidget.setMaxValue(59);
        npMinWidget.setMinValue(0);
        npHourWidget.setOnValueChangedListener(this);
        npSecWidget.setOnValueChangedListener(this);
        npMinWidget.setOnValueChangedListener(this);

        btnEnterTimes = (Button) findViewById(R.id.bEnterTimes);
        btnEnterTimes.setOnClickListener(this);

    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {

    }

    @Override
    public void onClick(View view) {
        Intent timerIntent = new Intent(this, TimerWidgetService.class);
        timerIntent.putExtra("cdHour", npHourWidget.getValue());
        timerIntent.putExtra("cdMin", npMinWidget.getValue());
        timerIntent.putExtra("cdSec", npSecWidget.getValue());
        timerIntent.putExtra("timeChanged", true);
        sendBroadcast(timerIntent);
        startService(timerIntent);
        AppWidgetManager appWidgetManager;
        appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.timer_widget_layout);
        views.setTextViewText(R.id.tvTimerWidget, String.format("%d : %02d : %02d", npHourWidget.getValue(), npMinWidget.getValue(),
                npSecWidget.getValue()));
        appWidgetManager.updateAppWidget(new ComponentName(getApplicationContext(), TimerWidgetProvider.class), views);
        finish();
    }
}
