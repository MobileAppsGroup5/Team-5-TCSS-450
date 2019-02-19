package tcss450.uw.edu.chapp.utils;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import me.pushy.sdk.Pushy;
import tcss450.uw.edu.chapp.MainActivity;
import tcss450.uw.edu.chapp.R;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

public class PushReceiver extends BroadcastReceiver {

    public static final String RECEIVED_NEW_MESSAGE = "new message from pushy";

    private static final String CHANNEL_ID = "1";

    @Override
    public void onReceive(Context context, Intent intent) {

        //the following variables are used to store the information sent from Pushy
        //In the WS, you define what gets sent. You can change it there to suit your needs
        //Then here on the Android side, decide what to do with the message you got

        //for the lab, the WS is only sending chat messages so the type will always be msg
        //for your project, the WS need to send different types of push messages.
        //perform so logic/routing based on the "type"
        //feel free to change the key or type of values. You could use numbers like HTTP: 404 etc
        String typeOfMessage = intent.getStringExtra("type");

        //The WS sent us the name of the sender
        String sender = intent.getStringExtra("sender");

        String messageText = intent.getStringExtra("message");

        String chatid = intent.getStringExtra("chatid");

        Log.e("INTENTS", intent.getExtras().toString());

        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);

        if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
            //app is in the foreground so send the message to the active Activities
            Log.d("PhishApp", "Message received in foreground: " + messageText);

            //create an Intent to broadcast a message to other parts of the app.
            Intent i = new Intent(RECEIVED_NEW_MESSAGE);
            i.putExtra("SENDER", sender);
            i.putExtra("MESSAGE", messageText);
            i.putExtra("CHATID", chatid);
            i.putExtras(intent.getExtras());

            context.sendBroadcast(i);

        } else {
            //app is in the background so create and post a notification
            Log.d("PhishApp", "Message received in background: " + messageText);

            Intent i = new Intent(context, MainActivity.class);
            i.putExtras(intent.getExtras());

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    i, PendingIntent.FLAG_UPDATE_CURRENT);

            //research more on notifications the how to display them
            //https://developer.android.com/guide/topics/ui/notifiers/notifications
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setAutoCancel(true)
//                    .setSmallIcon(R.)
                    .setContentTitle("Message from: " + sender)
                    .setContentText(messageText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(R.drawable.ic_menu_chat)
                    .setContentIntent(pendingIntent);

            // Automatically configure a Notification Channel for devices running Android O+
            Pushy.setNotificationChannel(builder, context);

            // Get an instance of the NotificationManager service
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

            // Build the notification and display it
            notificationManager.notify(1, builder.build());
        }

    }
}
