package com.example.yearproject12;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
Button button;
Button toHome;
TextView username;
TextView password;
FirebaseDatabase database = FirebaseDatabase.getInstance();
DatabaseReference myRef = database.getReference().child("User");

String user;
String pass;

//this is a simple reminder page to act as a form of security to prevent incidents with geofence creation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);;
        toHome = findViewById(R.id.To_Home);
        button = findViewById(R.id.loginBut);
        username = findViewById(R.id.editTextTextPersonName);
        password = findViewById(R.id.editTextTextPassword);



        myRef.addValueEventListener(new ValueEventListener() {

            public void onDataChange(@NonNull DataSnapshot snapshot) {
               user = snapshot.child("Username").getValue().toString();
               pass = snapshot.child("Password").getValue().toString();
            }


            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        toHome.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        });

        button.setOnClickListener(v -> {
            String textuser = username.getText().toString();
            String textpass = password.getText().toString();
            if (textuser.equals(user) && textpass.equals(pass)){
                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(this, "Username or password is incorrect, please try again", Toast.LENGTH_LONG).show();
            }
        });
    }
}