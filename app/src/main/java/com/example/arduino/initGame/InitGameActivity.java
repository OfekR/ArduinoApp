package com.example.arduino.initGame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arduino.loby.LobyActivity;
import com.example.arduino.utilities.HttpHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.arduino.R;

public class InitGameActivity extends AppCompatActivity {
    private TextView textView;
    private TextView textLevel;
    private TextView txtShots;
    private SeekBar seekBarTime;
    private SeekBar seekBarShots;
    private SeekBar seekBarLevel;
    private FirebaseFirestore db;
    private Member member;
    private Button sendDt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_game);
        member = new Member();
        db = FirebaseFirestore.getInstance();


        sendDt = (Button) findViewById(R.id.sendData);
        textView =(TextView) findViewById(R.id.txtMin);
        textLevel =(TextView) findViewById(R.id.txtLevel);
        txtShots = (TextView) findViewById(R.id.txtShots);
        seekBarTime = (SeekBar) findViewById(R.id.seekBar);
        seekBarShots = (SeekBar) findViewById(R.id.seekBar2);
        seekBarLevel = (SeekBar) findViewById(R.id.seekBar3);
        seekBarLevel.setProgress(0);
        seekBarShots.setProgress(0);
        seekBarTime.setProgress(0);
        seekBarShots.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtShots.setText("" + String.valueOf(progress));
                member.setNumberShot(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        seekBarLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0)  textLevel.setText("Level -" + String.valueOf(progress) + " Easy");
                if(progress == 1)  textLevel.setText("Level - " + String.valueOf(progress) + " Hard");
                if(progress == 2)  textLevel.setText("Level - " + String.valueOf(progress) + " Hell");
                member.setGameType(String.valueOf(progress));
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
        db.collection("GameSettings").document("doucment1").update(member.getMap());
        Toast.makeText(getApplicationContext(), "insert data",Toast.LENGTH_LONG ).show();
        if(checkIfDataValid()){
            HttpHelper httpHelper =new HttpHelper();
            httpHelper.HttpRequestForLooby("GAME-READY","https://us-central1-arduino-a5968.cloudfunctions.net/setGameReady");
            changeScreen(LobyActivity.class);
        }
        else{
            Toast.makeText(getApplicationContext(), "WRONG-DATA",Toast.LENGTH_LONG ).show();
        }
    }

    private boolean checkIfDataValid() {
        return true;
    }

    public void changeScreen(Class screen){
        Intent intent = new Intent(this, screen);
        startActivity(intent);
    }

}
