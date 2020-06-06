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
import com.google.firebase.database.FirebaseDatabase;
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
    //private Member member;
    private GameSetting gameSetting;
    private Button sendDt,backmenu;
    private TextView txtMines;
    private TextView txtKeys;
    private SeekBar seekBarMines;
    private SeekBar seekBarKeys;
    private DatabaseReference mDatabase;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initlayouts);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        priviteInitButton();
        //TODO - move into privtInitButton the following field
        //backmenu return to main menu
        backmenu= (Button) findViewById(R.id.backendMenu);
        backmenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                changeScreen(MenuActivity.class);
            }
        });
        seekBarShots.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtShots.setText("" + String.valueOf(progress));
                gameSetting.setNumberShots(String.valueOf(progress));
                //member.setNumberShot(String.valueOf(progress));
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
                gameSetting.setDuration(String.valueOf(progress));
                //member.setTime(String.valueOf(progress));
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
                gameSetting.setType(String.valueOf(progress+1));
                //member.setGameType(String.valueOf(progress+1));
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
                gameSetting.setMine(String.valueOf(progress));
                 //member.setMines(String.valueOf(progress));
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
                gameSetting.setKeys(String.valueOf(progress));
                //member.setKeys(String.valueOf(progress));

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
    //associate fields and buttons from XML
   private void priviteInitButton(){
       //member = new Member();
       gameSetting = new GameSetting();
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
        //TODO - Miki - do we need this toast? It interferes with "someone already started a game" toast
  //      Toast.makeText(getApplicationContext(), "insert data",Toast.LENGTH_LONG ).show();
        //TODO - implement CheckIfDataValid - it should check if all compents are ready to play - tanks are right pos
        //TODO check - maybe lower barrier down
        if(checkIfDataValid()){
            // check if game can be started from firebase
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
                        Log.v("MENU-CLASS",valid_join);
                        Toast.makeText(InitGameActivity.this, "SOMEONE ALREADY STARTED A GAME",
                                Toast.LENGTH_SHORT).show();
                    }
                    // you can start a game
                    else  if(valid_join.equals("GAME-NOT-READY")){
                        //update all game setting AND uid of player 1
                        //GameSetting gameSetting = new GameSetting((String) member.getTime(),member.getKeys(),member.getMines(),(String) member.getNumberShot(),member.getType());
                       // mDatabase.child("GameSettings").setValue(gameSetting);
                        mDatabase.child("GameSettings").updateChildren(gameSetting.toHashMap());

                        // update "gameReady" field
                        HttpHelper httpHelper = new HttpHelper();
                        httpHelper.HttpRequestForLooby("GAME-READY","https://us-central1-arduino-a5968.cloudfunctions.net/setGameReady");

                        //
                        Intent intent = new Intent(getApplicationContext(),LobyActivity.class);
                        intent.putExtra("Classifier", "Init");
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
