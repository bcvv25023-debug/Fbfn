package com.sirdaba.sirdaba_delivery;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class SirDabaApp extends Application {

    // One Signal
    private static final String ONESIGNAL_APP_ID = "38ee4211-3aed-4351-81ad-7827497f6faf";



    public static final String CHANNEL_ORDERS   = "sirdaba_orders";
    public static final String CHANNEL_GENERAL  = "sirdaba_general";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();




        // Enable verbose logging to debug issues (remove in production)
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

        // Replace with your 36-character App ID from Dashboard > Settings > Keys & IDs
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        // Prompt user for push notification permission
        // In production, consider using an in-app message instead for better opt-in rates
        OneSignal.getNotifications().requestPermission(false, Continue.none());




    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);

            // Orders channel — HIGH importance for delivery alerts
            NotificationChannel orders = new NotificationChannel(
                CHANNEL_ORDERS,
                "طلبات التوصيل",
                NotificationManager.IMPORTANCE_HIGH
            );
            orders.setDescription("إشعارات طلبات التوصيل الجديدة في مدينتك");
            orders.enableVibration(true);
            orders.setShowBadge(true);
            nm.createNotificationChannel(orders);

            // General channel
            NotificationChannel general = new NotificationChannel(
                CHANNEL_GENERAL,
                "إشعارات عامة",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            general.setDescription("تحديثات وإشعارات عامة من SirDaba");
            nm.createNotificationChannel(general);
        }
    }





}
