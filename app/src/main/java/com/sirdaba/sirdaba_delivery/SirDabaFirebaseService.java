package com.sirdaba.sirdaba_delivery;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

/**
 * SirDabaFirebaseService
 *
 * ⚠️ مهم: السيرفر لازم يرسل data-only messages (بدون notification payload)
 * حتى يشتغل onMessageReceived في جميع الحالات (foreground/background/killed)
 *
 * FCM payload مطلوب من السيرفر:
 * {
 *   "to": "/topics/city_agadir",
 *   "data": {
 *     "title": "طلب توصيل جديد 🚀",
 *     "body": "طلب جديد في مدينة أكادير",
 *     "type": "new_order",
 *     "city": "agadir",
 *     "order_id": "123",
 *     "url": "https://sirdaba.delivery/order/123"
 *   }
 * }
 */
public class SirDabaFirebaseService extends FirebaseMessagingService {

    private static final String TAG = "SirDabaFCM";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
        getSharedPreferences("sirdaba", MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        Map<String, String> data = message.getData();

        // Support both data payload and notification payload
        String title   = data.containsKey("title")    ? data.get("title")    : null;
        String body    = data.containsKey("body")      ? data.get("body")     : null;
        String type    = data.containsKey("type")      ? data.get("type")     : "general";
        String url     = data.containsKey("url")       ? data.get("url")      : "";
        String orderId = data.containsKey("order_id")  ? data.get("order_id") : "";
        String city    = data.containsKey("city")      ? data.get("city")     : "";

        // Fallback to notification payload
        if (message.getNotification() != null) {
            if (title == null) title = message.getNotification().getTitle();
            if (body  == null) body  = message.getNotification().getBody();
        }

        if (title == null) title = "SirDaba Delivery";
        if (body  == null) body  = "إشعار جديد";

        Log.d(TAG, "Message received | type=" + type + " city=" + city + " order=" + orderId);

        showNotification(title, body, url, type, orderId);
    }

    private void showNotification(String title, String body, String url,
                                   String type, String orderId) {

        String channelId = "new_order".equals(type)
            ? SirDabaApp.CHANNEL_ORDERS
            : SirDabaApp.CHANNEL_GENERAL;

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (url != null && !url.isEmpty())         mainIntent.putExtra("url", url);
        if (orderId != null && !orderId.isEmpty()) mainIntent.putExtra("order_id", orderId);

        int reqCode = new Random().nextInt(100000);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(mainIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
            reqCode,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_legacy))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setPriority("new_order".equals(type)
                ? NotificationCompat.PRIORITY_MAX
                : NotificationCompat.PRIORITY_DEFAULT)
            .setColor(ContextCompat.getColor(this, R.color.orange_primary))
            .setContentIntent(pendingIntent);

        if ("new_order".equals(type)) {
            builder.setVibrate(new long[]{0, 400, 200, 400, 200, 400});
        }

        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.notify(reqCode, builder.build());
    }
}
