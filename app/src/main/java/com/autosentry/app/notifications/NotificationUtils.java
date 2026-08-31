package com.autosentry.app.notifications;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.autosentry.app.ui.AlertsActivity;

public class NotificationUtils {
    public static final String CHANNEL_ID = "autosentry_alerts";

    public static void createChannels(Context ctx) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "AutoSentry Alerts", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Notifications for AutoSentry alerts and diagnostics");
        nm.createNotificationChannel(ch);
    }

    // POST_NOTIFICATIONS (API 33+) is declared in the manifest and requested at runtime
    // by PermissionFlow; callers post alerts only after the user has been prompted.
    @SuppressLint("MissingPermission")
    public static void sendAlert(Context ctx, int notifId, String title, String text) {
        createChannels(ctx);
        Intent intent = new Intent(ctx, AlertsActivity.class);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentIntent(pi)
                .setAutoCancel(true);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(notifId, b.build());
    }
}
