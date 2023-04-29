package com.example.yearproject12;


import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yearproject12.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    //create global variables
    Button testbutton;
    int value = 0;
    public static final int recognizer = 1;
    private TextToSpeech textToSpeech;
    public AlarmManager alarmManager;

    private ActivityMainBinding binding;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reminder = database.getReference().child("Reminders");
    private DatabaseReference reference = database.getReference().child("References");
    Button voiceButton;
    ArrayList<TestReminder> valuesForReminder;
    ArrayList<TestReminder> valuesForAlarms;
    ArrayList<TestReminder> valuesForReference;
    RecyclerView recycler;
    RecyclerView referenceRecycler;
    reminderRecyclerAdapter remindAdapter;
    referenceRecyclerAdapter referenceAdapter;
    ArrayList<String> parentIDs;
    ArrayList<String> words = new ArrayList<>();
    ImageButton imagebut;
    String tempVar;
    LocalTime hrTime24 = null;

    FloatingActionButton geoButton;
    Calendar calender = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //initialise values with their xml counterparts
        recycler = findViewById(R.id.recycler);
        referenceRecycler  = findViewById(R.id.referenceRecycler);

        //initialise arraylists
        valuesForReminder = new ArrayList<>();
        valuesForAlarms = new ArrayList<>();
        valuesForReference = new ArrayList<>();

        //initialise buttons
        voiceButton = findViewById(R.id.please);
        geoButton = findViewById(R.id.floatingActionButton);

        //initialise values for both recycleradapters
        remindAdapter = new reminderRecyclerAdapter(MainActivity.this, valuesForReminder, parentIDs);
        referenceAdapter = new referenceRecyclerAdapter(MainActivity.this, valuesForReference);
        recycler.setAdapter(remindAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        referenceRecycler.setAdapter(referenceAdapter);
        referenceRecycler.setLayoutManager(new LinearLayoutManager(this));

        //create notification channel for reminders
        createNotificationChannel();

        //Initialise text to speech object
        textToSpeech =  new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.UK);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        //if the text to speech object initialises properly it will play this line on application startup
                        speak("Welcome to your reminder application. To get started press the microphone button and say create a reminder or create a reference.");

                        voiceButton.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });




       // textToSpeech.synthesizeToFile("this is a test with high potential", null, new File(String.valueOf(R.raw.beaner)), "id1");
         //   int soundResId = R.raw.beaner;
           // MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResId);


      //  mediaPlayer.start();

        //add value event listener to references in the firebase
        reference.addValueEventListener(new ValueEventListener() {
            @Override

            //when the references in the database are changed it takes a snapshot of the database at that time
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                valuesForReference.clear();
                //loops through the snapshotand adds all references to an arraylist
                for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                    TestReminder fbReminders = datasnapshot.getValue(TestReminder.class);
                    Log.d("REF", "onDataChange: " + fbReminders.getTitle());
                    valuesForReference.add(fbReminders);
                }

                //refreshes the recyclerview so that the new list can be displayed
                referenceAdapter = new referenceRecyclerAdapter(MainActivity.this, valuesForReference );
                referenceRecycler.setAdapter(referenceAdapter);
                referenceRecycler.setLayoutManager(new LinearLayoutManager(MainActivity.this));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //adds value event listener to reminders in the firebase
        reminder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                valuesForReminder.clear();
                int i = 0 ;
                //iterates through each child of the reminder value in the firebase and adds them to a reminder arraylist
                for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                    TestReminder fbReminders = datasnapshot.getValue(TestReminder.class);
                    //adds all reminders to an arraylist
                    valuesForReminder.add(fbReminders);



                    Log.d("DEBUG", "onDataChange: " + fbReminders.getTitle());
                            valuesForAlarms.add(fbReminders);




                            if(alarmExists(i)){
                                cancelAlarm(i);
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                //creates a localtime variable out of the time string from the reminder
                                hrTime24 = LocalTime.parse(fbReminders.getTime());
                                Calendar tempCalendar = Calendar.getInstance();
//                                //sets the calendar variable to the 24hr time value
                                tempCalendar.set(Calendar.HOUR_OF_DAY, hrTime24.getHour());
                                tempCalendar.set(Calendar.MINUTE, hrTime24.getMinute());
                                tempCalendar.set(Calendar.SECOND, 0);
                                tempCalendar.set(Calendar.MILLISECOND, 0);
                                //if the time is less than the current time it will add a day to the calendar
                                if(tempCalendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() - 60 * 1000){
                                    tempCalendar.add(Calendar.DATE, 1);
                                    setAlarm(fbReminders.getTitle(), i, tempCalendar);
                                }
                                else{ setAlarm(fbReminders.getTitle(), i, tempCalendar);}


                              //  Toast.makeText(MainActivity.this, "This executed!", Toast.LENGTH_SHORT).show();
                                hrTime24 = null;
                            }
                            i++;
                }

                //refresh recyclerview to adapt to new changes
                remindAdapter = new reminderRecyclerAdapter(MainActivity.this, valuesForReminder, parentIDs);
                recycler.setAdapter(remindAdapter);
                recycler.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                //  adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        voiceButton.setText("Click to create reminder");

        geoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }catch (Exception e){
                    Log.d("mainactivity", "onClick: "+ e );                }

            }
        });

        //set on click listener
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //uses googles api to send users to a speech to text activity
                Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                //speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                Locale Locale24h = new Locale(Locale.getDefault().getLanguage(), "GB");
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale24h);
                startActivityForResult(speechIntent, 1);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

     //   createReminder(requestCode, resultCode, data);

        //if the value returned is okay continue with code
        if (requestCode == recognizer && resultCode == RESULT_OK) {

            //assigns the value to a string arraylist if the string equals create a reminder a toast is used to prompt users to input the next value
            words.add(String.valueOf(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)));
            // words = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            words.set(0, words.get(0).replaceAll("\\[", "").replaceAll("\\]", ""));
            try{
            if (words.get(0).contains("reminder") && words.size() <= 1) {
                speak("sure, now enter the name of a reminder");

            }

            //the next value inputted is added to the arraylist which will be used as the name value
            else if (words.size() == 2 && words.get(0).contains("reminder")){
                words.set(1, words.get(1).replaceAll("\\[", "").replaceAll("\\]", ""));
                speak("great, now what time would you like to be reminded?");
            }
            //the final value inputted is the time if it doesnt get a usable string to convert to a localtime variable it prints an error toast
            else if (words.size() == 3 && words.get(0).contains("reminder")) {
                //trims the string of extra characters

                    tempVar = words.get(2);
              //      removes all the extra characters from the string
                    tempVar = tempVar.replaceAll("[\\[\\]\\s\'\"]", "");
                    Pattern pattern = Pattern.compile("([1-9]|1[0-2]):([0-5][0-9])([apAP]\\.[mM]\\.)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(tempVar);


                    if (matcher.find()) {
                        //gets the hour and minute from the string
                        String hour = matcher.group(1);
                        String minute = matcher.group(2);
                        //gets the am or pm from the string
                        String ampm = Objects.requireNonNull(matcher.group(3)).charAt(0) + "";

                        //converts the strings to ints
                        assert hour != null;
                        int hourInt = Integer.parseInt(hour);
                        assert minute != null;
                        int minuteInt = Integer.parseInt(minute);

                        //if the time is pm and the hour is less than 12 add 12 to the hour
                        if (ampm.equals("p") && hourInt < 12) {
                            hourInt += 12;
                        } else if (ampm.equals("a") && hourInt == 12) {
                            hourInt = 0;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            //creates a local time variable from the hour and minute
                            hrTime24 = LocalTime.of(hourInt, minuteInt);
                            Log.d("MainActivity", "onActivityResult: " + hrTime24.toString());
                        }
                    }
                    else{
                        speak("Sorry, The time you inputted was not recognised. Please say create a reminder and try again");

                        clearList();
                    }


                    //adds the reminder to the database
                    reminder.push().setValue(new TestReminder(words.get(1), hrTime24.toString()));

                    //clears the user inputs for the next reminder
                    clearList();
                    words.clear();
//                    valuesForList.clear();
                    calender = null;
                    hrTime24 = null;
                    speak("Great, reminder created");
                }
            else if (words.get(0).contains("reference") ){
                referencePrompts();
            }
            //if the user inputs a reference
            else{
                speak("Sorry, command not recognised. Please say create a reminder or create a reference");
                words.clear();
                clearList();
            }

            }catch (Exception e) {
                Log.e("MainActivity", "An error occurred: " + e.getMessage());
//                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }

            }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //creates a notification channel for the reminders
            CharSequence name = "alarmsReminderChannel";
            String description = "Channel for Reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("alarms", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setAlarm(String name , int requestcode, Calendar tester) {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //creates an intent to send to the alarm receiver
        Intent intent = new Intent(this, AlarmReceiver.class);
        Log.d("DEBUG", "setAlarm: " + name);
        intent.putExtra("Name" , name);
        intent.putExtra("desc" ,"Dont forget to complete this task!");

        //creates a pending intent to send to the alarm receiver
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestcode, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //sets the alarm to the time in the calender which will wake the device if its idle
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tester.getTimeInMillis(), pendingIntent);
        }
        Log.d("MainActivity", "setAlarm: " + tester.toString());
    }

    private boolean alarmExists(int requestCode){
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendIntent= PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);
        Log.d("DEBUG", String.valueOf("alarmExists: "+ pendIntent != null));
        return pendIntent != null;

    }
    public void cancelAlarm(int requestcode){
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestcode, intent, PendingIntent.FLAG_IMMUTABLE);
        Log.d("DEBUG", "cancelAlarm: "+" deleted reminder " + requestcode);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    private void speak(String text){
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    public void stringToTime() {

        //removes all the extra characters from the string
        tempVar = tempVar.replaceAll("[\\[\\]\\s\'\"]", "");
        Pattern pattern = Pattern.compile("([1-9]|1[0-2]):([0-5][0-9])([apAP]\\.[mM]\\.)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tempVar);

        if (matcher.find()) {
            //gets the hour and minute from the string
            String hour = matcher.group(1);
            String minute = matcher.group(2);
            //gets the am or pm from the string
            String ampm = Objects.requireNonNull(matcher.group(3)).charAt(0) + "";

            //converts the strings to ints
            assert hour != null;
            int hourInt = Integer.parseInt(hour);
            assert minute != null;
            int minuteInt = Integer.parseInt(minute);

            //if the time is pm and the hour is less than 12 add 12 to the hour
            if (ampm.equals("p") && hourInt < 12) {
                hourInt += 12;
            } else if (ampm.equals("a") && hourInt == 12) {
                hourInt = 0;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //creates a local time variable from the hour and minute
                hrTime24 = LocalTime.of(hourInt, minuteInt);
                // Log.d("MainActivity", "onActivityResult: " + hrTime24.toString());
            }
        }
    }
    public void referencePrompts(){
        try {
            if (words.get(0).contains("reference") && words.size() <= 1) {
                speak("sure, please name the item you want to reference");
            }
            else if (words.size() == 2 && words.get(0).contains("reference")) {
                //trim the string of brackets and quotations
                words.set(1, words.get(1).replaceAll("[\\[\\]\'\"]", "").replaceAll("\\]", ""));

                speak("great, now where do you normally keep this item?");
            } else if (words.size() == 3 && words.get(0).contains("reference")) {
                words.set(2, words.get(2).replaceAll("[\\[\\]\'\"]", "").replaceAll("\\]", ""));

                reference.push().setValue(new TestReminder(words.get(1), words.get(2)));
                clearList();
                words.clear();
                calender = null;
                hrTime24 = null;
                speak("Great, reference created");
            }
            else{
                speak("Sorry, command not recognised, Please say create a reminder or create a reference instead, this is in the reference");
                words.clear();
            }


        } catch (Exception e) {
            Log.e("MainActivity", "An error occurred: " + e.getMessage());
//                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
        public void clearList() {
            //clears the list
            words.clear();
            valuesForReminder.clear();
            calender = null;
            hrTime24 = null;
        }

}