package micheal.must.signuplogin.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import micheal.must.signuplogin.DashboardActivity;
import micheal.must.signuplogin.R;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import micheal.must.signuplogin.DashboardActivity;
import micheal.must.signuplogin.R;
import micheal.must.signuplogin.utils.FirebaseManager;

public class NotificationService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "mindmate_channel";
    private static final String CHANNEL_NAME = "MindMate Notifications";
    private static final String CHANNEL_DESC = "Notifications from MindMate app";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            sendNotification(title, body);
        }

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            String type = remoteMessage.getData().get("type");
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");

            if (type != null) {
                switch (type) {
                    case "check_in":
                        sendCheckInReminder(title, message);
                        break;
                    case "meditation":
                        sendMeditationReminder(title, message);
                        break;
                    case "journal":
                        sendJournalReminder(title, message);
                        break;
                    default:
                        sendNotification(title, message);
                        break;
                }
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        // Update the token in Firestore
        String userId = FirebaseManager.getInstance().getCurrentUserId();
        if (userId != null) {
            FirebaseManager.getInstance().getUserReference()
                    .update("fcmToken", token);
        }
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        createNotificationChannel();

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notifications)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendCheckInReminder(String title, String message) {
        // Create an intent for the check-in activity
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("openCheckIn", true);
        sendNotification(title, message);
    }

    private void sendMeditationReminder(String title, String message) {
        // Create an intent for the meditation activity
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("openMeditation", true);
        sendNotification(title, message);
    }

    private void sendJournalReminder(String title, String message) {
        // Create an intent for the journal activity
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("openJournal", true);
        sendNotification(title, message);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
