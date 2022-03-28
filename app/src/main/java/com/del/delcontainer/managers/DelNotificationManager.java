package com.del.delcontainer.managers;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.del.delcontainer.R;
import com.del.delcontainer.ui.login.LoginActivity;
import com.del.delcontainer.utils.Constants;

/**
 * Notification Manager - handles app notifications requested by
 * linked mini-apps.
 */
public class DelNotificationManager {

    private static final String TAG = "NotificationManager";

    private static DelNotificationManager notificationManager = null;

    private static Context applicationContext = null;
    private String CHANNEL_ID = "test_notif_channel";

    /**
     * Singleton class - create new instance if one doesn't already exist
     *
     * @return notificationManager instance
     */
    public static DelNotificationManager getInstance() {
        if (null == notificationManager) {
            notificationManager = new DelNotificationManager();
        }

        return notificationManager;
    }

    /**
     * Initialize the manager with the application context
     * and create a notification channel
     *
     * @param context Application context
     */
    public void initNotificationManager(Context context) {
        if(null == applicationContext) {
            applicationContext = context;
        }
        createNotificationChannel();
    }

    /**
     * Create notification channel on init to ensure notifications are
     * delivered on API level 26+
     * Can be called more than once - has no effect
     */
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = applicationContext.getString(R.string.notification_channel_name);
            String description = applicationContext.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = applicationContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Function called by micro-mHealth apps with the AppId and message.
     * App title is fetched from the AppId
     *
     * @param appId Micro App Id
     * @param notificationMessage notification message
     */
    public void createAppNotification(String appId, String notificationMessage) {

        String appName = DelAppManager.getInstance().getAppNameMap().get(appId);
        createNotification(appId, appName, notificationMessage);
    }

    /**
     * Get the app launcher intent - make sure we don't overwrite the
     * app back stack.
     *
     * @param appId Application id
     * @return intent
     */
    public Intent getLauncherIntent(String appId) {
        // LoginActivity -- because that's the first in the stack called by the OS
        // DO NOT add DelContainerActivity - else the app state will be lost
        final Intent intent = new Intent(applicationContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra(Constants.INTENT_APP_ID, appId);

        return intent;
    }

    /**
     * Create single notification with a given title and message
     *
     * @param notificationTitle Notification title
     * @param notificationMessage Notification message
     */
    public void createNotification(String appId, String notificationTitle, String notificationMessage) {
        PendingIntent pendingIntent = PendingIntent.getActivity(applicationContext, 0,
                getLauncherIntent(appId), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.del_image)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Fire the intent when the user taps the notification
                .setAutoCancel(true); // clear notification automatically

        // Display notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
        notificationManager.notify(1, notificationBuilder.build());
    }
}
