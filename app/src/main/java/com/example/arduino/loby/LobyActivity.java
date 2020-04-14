package com.example.arduino.loby;

import androidx.appcompat.app.AppCompatActivity;
import com.example.arduino.R;
import com.example.arduino.gameScreen.GameScreenActivity;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

public class LobyActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button sendDt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loby);
        Map<String ,Integer> info = new HashMap<String, Integer>();
        info.put("User-Id", 1);
        info.put("NumberShot", 100);
        info.put("NumberOfWins" , 10);
        sendDt = (Button) findViewById(R.id.LobyButton);
        DatabaseReference dbReff = FirebaseDatabase.getInstance().getReference().child("Users");
        sendDt.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                changeScreen(GameScreenActivity.class);
            }
        });

    }
    public void changeScreen(Class screen){
        Intent intent = new Intent(this, screen);
        startActivity(intent);
    }
}
