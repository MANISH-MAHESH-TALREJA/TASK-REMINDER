package net.manish.shopping.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import net.manish.shopping.R;
import net.manish.shopping.activities.MainActivity;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;

import java.util.Objects;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();

        TodoModel todoModel = Objects.requireNonNull(bundle).getParcelable(Constants.KEY_ID);

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Task Rider")
                .setContentText("Tap to manage your task/reminders")
                .setSmallIcon(R.drawable.logo_transparent)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(Constants.FOREGROUND_NOTIFICATION_ID, notification);
        //do heavy work on a background thread

        if (todoModel != null) {
            CommonUtils.scheduleAlarm(this, todoModel);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_NONE
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public static void clearNotification(Context context) {
        NotificationManagerCompat.from(context).cancelAll();
    }
}