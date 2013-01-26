package com.shockwave.clockproj.free;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import com.jakewharton.notificationcompat2.NotificationCompat2;

public class TimerService extends Service {
    final static String START_ACTION = "START_ACTION_TIMER";

    int cdHour, cdMin, cdSec, cdMillis;
    boolean timerRunning = false;
    boolean stoppedBefore = false;
    CountDownTimer cdTimer;

    SharedPreferences timerPrefs;

    @Override
    public void onCreate() {
        timerPrefs = getSharedPreferences("TimerServicePrefs", 0);
        timerPrefs.getBoolean("timerSaves", true);
        stoppedBefore = timerPrefs.getBoolean("stopped", false);
        cdHour = timerPrefs.getInt("cdHour", 0);
        cdMin = timerPrefs.getInt("cdMin", 0);
        cdSec = timerPrefs.getInt("cdSec", 0);
        cdMillis = timerPrefs.getInt("cdMillis", 0);
        super.onCreate();
    }

    boolean timeChanged;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(getApplicationContext(), ClockMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        NotificationCompat2.Builder builder = new NotificationCompat2.Builder(getApplicationContext());
        builder.setContentTitle("Clock Project Timer");
        builder.setContentText("Timer is Running.");
        builder.setSmallIcon(R.drawable.ic_stat_ic_timer_running);
        builder.setContentIntent(pendingIntent);
        final Notification timerRunningNote = builder.build();
        startForeground(71, timerRunningNote);

        timeChanged = intent.getBooleanExtra("timeChanged", false);
        if (timeChanged) {
            cdHour = intent.getIntExtra("cdHour", 0);
            cdMin = intent.getIntExtra("cdMin", 0);
            cdSec = intent.getIntExtra("cdSec", 0);
        }
        if (!timerRunning) {
            if ((cdHour != 0) || (cdSec != 0) || (cdMin != 0) || (cdMillis != 0))
                timerRunning = true;
        }
        timeChanged = false;
        cdMin += cdHour * 60;
        cdSec += cdMin * 60;
        cdMillis += cdSec * 1000;

        cdTimer = new CountDownTimer(cdMillis, 100) {
            Intent broadcastIntent = new Intent();

            @Override
            public void onTick(long millisUntilFinished) {
                cdSec = ((int) millisUntilFinished / 1000);
                cdMin = (cdSec / 60);
                cdHour = (cdMin / 60);
                cdMin %= 60;
                cdSec %= 60;
                cdHour %= 24;
                cdMillis = ((int) millisUntilFinished % 1000);

                broadcastIntent.setAction(TimerService.START_ACTION);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra("timerMain", String.format("%d : %02d : %02d", cdHour, cdMin,
                        cdSec));
                broadcastIntent.putExtra("timerMillis", String.format(". %03d", cdMillis));
                sendBroadcast(broadcastIntent);
            }

            @Override
            public void onFinish() {
                cdMillis = 0;
                broadcastIntent.setAction(TimerService.START_ACTION);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra("timerMain", String.format("%d : %02d : %02d", 0, 0, 0));
                broadcastIntent.putExtra("timerMillis", String.format(". %03d", cdMillis));
                timerRunning = false;
                broadcastIntent.putExtra("timerRunning", timerRunning);
                sendBroadcast(broadcastIntent);

                SharedPreferences.Editor editor = timerPrefs.edit();
                editor.clear();
                editor.commit();

                stopForeground(true);

                Uri note;
                try {
                    note = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                } catch (NullPointerException e) {
                    note = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
                final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), note);
                try {
                    r.setStreamType(RingtoneManager.TYPE_ALARM);
                } catch (NullPointerException e) {
                    r.setStreamType(RingtoneManager.TYPE_NOTIFICATION);
                }
                r.play();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        r.stop();
                    }
                }, 3000);

                Intent notificationIntent = new Intent(getApplicationContext(), ClockMain.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                        notificationIntent, 0);
                NotificationCompat2.Builder builder = new NotificationCompat2.Builder(getApplicationContext());
                builder.setContentTitle("Clock Project Timer");
                builder.setContentText("Time is up.");
                builder.setSmallIcon(R.drawable.ic_stat_timer_up);
                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                Notification notification = builder.build();
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context
                        .NOTIFICATION_SERVICE);
                notificationManager.notify(0, notification);

                Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                v.vibrate(1000);
            }
        }.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        cdTimer.cancel();
        timerRunning = false;
        SharedPreferences.Editor editor = timerPrefs.edit();
        editor.putInt("cdHour", cdHour);
        editor.putInt("cdMin", cdMin);
        editor.putInt("cdSec", cdSec);
        editor.putInt("cdMillis", cdMillis);
        editor.putBoolean("stopped", true);
        editor.commit();

        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
