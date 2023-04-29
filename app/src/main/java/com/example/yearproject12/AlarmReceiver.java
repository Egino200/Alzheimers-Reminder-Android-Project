package com.example.yearproject12;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {

    String filePatch = "raw/test.mp3";
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {


        String reminderName = intent.getStringExtra("Name");
        Log.d("DEBUG", "onReceive: " + reminderName);

        try {
            Intent i = new Intent(context, ReminderActivity.class);
            i.putExtra("reminder", reminderName);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE);


//            Uri notificationSoundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.beaner);

            //builds the notification using data from the intent
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarms")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    //.setSound(notificationSoundUri)

                    .setContentTitle(reminderName)
                    .setContentText("Dont forget to complete this task!")
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent);


            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

            Notification noti = builder.build();


       //    noti.sound = Uri.parse("raw/samsung.mp3");
            //makes notifications insistent. meaning if they arent addressed they will continue to make noise
            noti.flags = noti.flags | Notification.FLAG_INSISTENT;
            notificationManagerCompat.notify(200, noti);

        }catch (Exception e) {
            Log.e("MainActivity", "onReceive: ", e);
        }
        try {
            //if the notification is clicked it will redirect to the reminder activity
            Intent reminderIntent = new Intent(context, ReminderActivity.class);
            reminderIntent.putExtra("reminder", reminderName);
            reminderIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(reminderIntent);
        }catch (Exception e) {
            Log.e("MainActivity", "onReceive: ", e);
        }



    }
}
