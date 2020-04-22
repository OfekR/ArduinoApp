package com.example.arduino.initGame;

import androidx.annotation.NonNull;
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
import com.example.arduino.menu.MenuActivity;
import com.example.arduino.menu.PlayerId;
import com.example.arduino.utilities.HttpHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.arduino.R;

import java.util.Objects;

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
    private TextView txtMines;
    private TextView txtKeys;
    private SeekBar seekBarMines;
    private SeekBar seekBarKeys;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initlayouts);
        priviteInitButton();
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
                if(progress == 0)  textLevel.setText("CAPTURE-THE-FLAG");
                if(progress == 1)  textLevel.setText("HIGH-SCORE");
                if(progress == 2)  textLevel.setText("LAST-TANK-REMAINING");
                member.setGameType(String.valueOf(progress+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarMines.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtMines.setText("Mines - " + String.valueOf(progress));
                member.setMines(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarKeys.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtKeys.setText("Keys - " + String.valueOf(progress));
                member.setKeys(String.valueOf(progress));
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
   private void priviteInitButton(){
       member = new Member();
       db = FirebaseFirestore.getInstance();
       sendDt = (Button) findViewById(R.id.sendData);
       textView =(TextView) findViewById(R.id.txtMin);
       textLevel =(TextView) findViewById(R.id.txtLevel);
       txtShots = (TextView) findViewById(R.id.txtShots);
       seekBarTime = (SeekBar) findViewById(R.id.seekBar);
       seekBarShots = (SeekBar) findViewById(R.id.seekBar2);
       seekBarLevel = (SeekBar) findViewById(R.id.seekBar3);
       txtMines = (TextView) findViewById(R.id.txtInitMines);
       txtKeys = (TextView) findViewById(R.id.txtInitKeys);
       seekBarMines = (SeekBar) findViewById(R.id.seekBarMines);
       seekBarKeys = (SeekBar) findViewById(R.id.seekBarKeys);
       seekBarLevel.setProgress(0);
       seekBarShots.setProgress(0);
       seekBarTime.setProgress(0);
    }

    private void sendData(){
        //db.collection("GameSettings").document("doucment1").update(member.getMap());
        Toast.makeText(getApplicationContext(), "insert data",Toast.LENGTH_LONG ).show();
        if(checkIfDataValid()){
            checkForGameReady();
        }
        else{
            Toast.makeText(getApplicationContext(), "WRONG-DATA",Toast.LENGTH_LONG ).show();
        }
    }

    private void checkForGameReady() {
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        DocumentReference docRef = fstore.collection("Appending").document("Append1");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot= task.getResult();
                    String valid_join = snapshot.getString("gameReady");
                    // another player is already started a game
                    if (valid_join.equals("GAME-READY")) {
                        Log.v("MENU-CLASS", "SOMEONE IS ALREADY START A GAME");
                        Toast.makeText(InitGameActivity.this, "SOMEONE IS ALREADY START A GAME",
                                Toast.LENGTH_SHORT).show();


                    }
                    // you can start a game
                    else {
                        HttpHelper httpHelper1 = new HttpHelper();
                        String args = "?keys="+member.getKeys()+"&mine="+member.getMines()+"&type="+member.getType()+"&shots="+member.getNumberShot()+"&time="+member.getTime();
                        String url = " https://us-central1-arduino-a5968.cloudfunctions.net/setfirstGame"+args;
                        httpHelper1.HttpRequest(url);

                        HttpHelper httpHelper = new HttpHelper();
                        httpHelper.HttpRequestForLooby("GAME-READY","https://us-central1-arduino-a5968.cloudfunctions.net/setGameReady");
                        Intent intent = new Intent(getApplicationContext(),LobyActivity.class);

                        HttpHelper httpHelper2 = new HttpHelper();
                        String str = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        httpHelper2.HttpRequestForLooby(str,"https://us-central1-arduino-a5968.cloudfunctions.net/setPlayerId1");

                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
                    }
                }
            }
        });
    }

    private boolean checkIfDataValid() {
        return true;
    }

    public void changeScreen(Class screen){
        Intent intent = new Intent(this, screen);
        startActivity(intent);
    }

}
