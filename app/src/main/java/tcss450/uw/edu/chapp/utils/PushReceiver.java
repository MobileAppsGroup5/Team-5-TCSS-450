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
import tcss450.uw.edu.chapp.model.Credentials;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

public class PushReceiver extends BroadcastReceiver {

    public static final String RECEIVED_NEW_MESSAGE = "new message from pushy";
    private static final String CHANNEL_ID = "1";
    private static Credentials mCreds; //TODO how do we get credentials of user to sift through notifications?

    @Override
    public void onReceive(Context context, Intent intent) {

        String typeOfMessage = intent.getStringExtra("type");

        Log.e("INTENTS", intent.getExtras().toString());

        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);

        if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
            if ("msg".equals(typeOfMessage)) { //new message
                //app is in the foreground so send the message to the active Activities
                Log.d("ChappApp", "Message received in foreground: " + intent.getStringExtra("message"));

                //create an Intent to broadcast a message to other parts of the app.
                Intent i = new Intent(RECEIVED_NEW_MESSAGE);
                i.putExtra("SENDER", intent.getStringExtra("sender"));
                i.putExtra("MESSAGE", intent.getStringExtra("message"));
                i.putExtra("CHATID", intent.getStringExtra("chatid"));
                i.putExtras(intent.getExtras());

                context.sendBroadcast(i);
            } else if ("conn req".equals(typeOfMessage)) { //new connection request
                Log.d("ChappApp", "Conn req recieved in foreground: " + intent.getStringExtra("sender"));

                //create an Intent to broadcast a request to other parts of the app.
                Intent i = new Intent(RECEIVED_NEW_MESSAGE);
                i.putExtra("SENDER", intent.getStringExtra("sender"));
                i.putExtra("TO", intent.getStringExtra("to"));
                i.putExtra("MESSAGE", intent.getStringExtra("message"));
                i.putExtras(intent.getExtras());

                context.sendBroadcast(i);
            } else if ("convo req".equals(typeOfMessage)) { //new chat room request
                Log.d("ChappApp", "Convo req recieved in foreground: " + intent.getStringExtra("sender"));

                //create an Intent to broadcast a request to other parts of the app.
                Intent i = new Intent(RECEIVED_NEW_MESSAGE);
                i.putExtra("SENDER", intent.getStringExtra("sender"));
                i.putExtra("TO", intent.getStringExtra("to"));
                i.putExtra("CHATNAME", intent.getStringExtra("chatName"));
                i.putExtra("MESSAGE", intent.getStringExtra("message"));
                i.putExtras(intent.getExtras());

                context.sendBroadcast(i);
            }

        } else {
            //app is in the background so create and post a notification

            Log.d("ChappApp", "Message received in background: " + intent.getStringExtra("message"));
            Intent i = new Intent(context, MainActivity.class);
            i.putExtras(intent.getExtras());

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    i, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(R.mipmap.chapp_logo_trans_foreground)
                    .setContentIntent(pendingIntent);

            //Styling the notifications out of app
            if (typeOfMessage.equals("msg")) {
                builder.setContentTitle("Message from: " + intent.getStringExtra("sender"))
                        .setContentText(intent.getStringExtra("message"));
            } else if (typeOfMessage.equals("conn req")) {
                builder.setContentTitle("Connection request from: " + intent.getStringExtra("sender"))
                        .setContentText(intent.getStringExtra("message"));
            } else if (typeOfMessage.equals("convo req")) {
                builder.setContentTitle("Chat room invitation from: " + intent.getStringExtra("sender"))
                        .setContentText(intent.getStringExtra("message"));
            }

            // Automatically configure a Notification Channel for devices running Android O+
            Pushy.setNotificationChannel(builder, context);

            // Get an instance of the NotificationManager service
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

            // Build the notification and display it
            notificationManager.notify(1, builder.build());
        }

    }
}
