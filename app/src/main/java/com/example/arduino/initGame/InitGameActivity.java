package com.example.arduino.initGame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.arduino.R;

import java.util.HashMap;
import java.util.Map;

public class InitGameActivity extends AppCompatActivity {
    private TextView textView;
    private SeekBar seekBar;
    private DatabaseReference reff;
    private FirebaseFirestore db;
    private Member member;
    private Button sendDt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_game);
        member = new Member();
        db = FirebaseFirestore.getInstance();
        textView =(TextView) findViewById(R.id.txtMin);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        sendDt = (Button) findViewById(R.id.sendData);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("" + String.valueOf(progress) + " Min");
                member.setTime(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sendDt.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                    sendData();
            }
        });
    }

    private void sendData(){
        if(db.collection("GameSettings").)
        db.collection("GameSettings").add(member);
        Toast.makeText(getApplicationContext(), "insert data",Toast.LENGTH_LONG ).show();
            }




}
