package com.example.arduino.loby;

import androidx.appcompat.app.AppCompatActivity;
import com.example.arduino.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

public class LobyActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loby);
        Map<String ,Integer> info = new HashMap<String, Integer>();
        info.put("User-Id", 1);
        info.put("NumberShot", 100);
        info.put("NumberOfWins" , 10);
        DatabaseReference dbReff = FirebaseDatabase.getInstance().getReference().child("Users");
        dbReff.setValue(info);
    }
}
