package com.example.yearproject12;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ReminderActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    Calendar c = Calendar.getInstance();
    LocalTime newTime;
    String time;
    String resultTime;
    public AlarmManager alarmManager;
    Button noBut;
    Button yesBut;
    TextView reminderText;
    Handler handler = new Handler();
    Runnable runnable;

    long vibrationDuration = 1000;
    long vibrationInterval = 10000;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reminder = database.getReference().child("Reminders");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //create notification channel
        createNotificationChannel();
        showWhenLocked();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        //assign variables to the buttons and textview
        noBut = findViewById(R.id.noButton);
        yesBut = findViewById(R.id.yesButton);
        reminderText = findViewById(R.id.ReminderText);
        reminderText.setText(getIntent().getStringExtra("reminder"));

        //initialise text to speech
        textToSpeech =  new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.UK);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {

                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        noBut.setOnClickListener(view -> {
            Query query2 = reminder.orderByChild("title");

            query2.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Iterate over the ordered children
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String childOrder = childSnapshot.child("title").getValue(String.class);

                        if (Objects.equals(childOrder, getIntent().getStringExtra("reminder"))){
                            //add 15 minutes to the time string
                            time = childSnapshot.child("time").getValue(String.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                                newTime = LocalTime.now();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                                //adds 3 minutes to the time
                                newTime = newTime.plusMinutes(3);

                                resultTime = newTime.format(formatter);

                            }
                            //set the new time
                            childSnapshot.getRef().child("time").setValue(resultTime);

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
       // setAlarm();

        Intent i = new Intent(ReminderActivity.this, MainActivity.class);
        startActivity(i);
        });

        yesBut.setOnClickListener(view -> {
            Query query = reminder.orderByChild("title");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Iterate over the ordered children
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String childOrder = childSnapshot.child("title").getValue(String.class);


                        if (Objects.equals(childOrder, getIntent().getStringExtra("reminder"))) {
                            // Remove the child node
                            childSnapshot.getRef().removeValue();
                            break;  // Exit the loop after removing the child node
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            Intent i = new Intent(ReminderActivity.this, MainActivity.class);
            startActivity(i);
        });


    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "alarmsReminderChannel";
            String description = "Channel for Reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("alarms", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setAlarm() {
        Log.d("DEBUG", "setAlarm: " + getIntent().getStringExtra("reminder"));
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("Name" , getIntent().getStringExtra("reminder"));
        intent.putExtra("desc" ,"Don't forget to complete this task!");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "alarm created", Toast.LENGTH_SHORT).show();
        }

    }
    private void showWhenLocked(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

    }
    private void speak(String text){
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                textToSpeech.speak("Have you completed this reminder? " + getIntent().getStringExtra("reminder"), TextToSpeech.QUEUE_FLUSH, null);
                handler.postDelayed(this, 5000);
            }
        }, 0);


    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }
}