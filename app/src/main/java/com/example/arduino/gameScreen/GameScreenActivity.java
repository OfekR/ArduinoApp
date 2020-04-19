package com.example.arduino.gameScreen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.arduino.R;
import com.example.arduino.initGame.Member;
import com.example.arduino.loby.PopWindow;
import com.example.arduino.stats.PlayerStats;
import com.example.arduino.utilities.HttpHelper;
import com.example.arduino.utilities.MediaPlayerWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Locale;

import io.github.controlwear.virtual.joystick.android.JoystickView;


/*
TODO : we need to reorganize the class.
TODO : add the auth with the game for change the field in the database
 */

public class GameScreenActivity extends AppCompatActivity {
    private static final long START_TIME_IN_MILLIS = 600000 ;
    private Game game;
    private ProgressBar pbLife;
    private TextView txtLife;
    private TextView txtAmmo;
    private TextView txtScore;
    private  TextView txtKeys;
    private TextView txtMines;
    private TextView txtDefuse;
    private Button btnShot;
    private TextView txtCountDown;
    private CountDownTimer countDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMils = START_TIME_IN_MILLIS;
    private MediaPlayerWrapper mySong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);
        findAllView();
        getDataFromSetting();
        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // do whatever you want
            }
        });

        btnShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer num =Integer.parseInt(game.getAmmuo());
                num = num -1 ;
                if(num > 0 ){
                    mySong = new MediaPlayerWrapper(R.raw.goodgunshot,getApplicationContext());
                    mySong.StartOrResume();
                    game.setAmmuo(num.toString());
                    txtAmmo.setText("Ammuo: - " + game.getAmmuo());
                    if(game.getPlayerID().equals("1")){
                        HttpHelper httpHelper = new HttpHelper();
                        httpHelper.HttpRequestForLooby("2","https://us-central1-arduino-a5968.cloudfunctions.net/chnageLifeInGame");

                    }
                    // player 2
                    else{
                        HttpHelper httpHelper = new HttpHelper();
                        httpHelper.HttpRequestForLooby("1","https://us-central1-arduino-a5968.cloudfunctions.net/chnageLifeInGame");
                    }
                    //hit sound
                    checkForHit();


                }
                else{
                    txtAmmo.setText("NO- Ammuo - !!!!!!!!: - ");
                }


            }
        });

        startTimer();
        HttpHelper httpHelper = new HttpHelper();
        httpHelper.HttpRequestForLooby(game.getPoint().toString(),"https://us-central1-arduino-a5968.cloudfunctions.net/endOfGameSender");
        ListnerForChangeInGame();

    }

    private void checkForHit() {

        boolean arduinoSensorLaser =true; //TODO check in the sensor if we hit the other player
        if(arduinoSensorLaser){
            mySong = new MediaPlayerWrapper(R.raw.boom,getApplicationContext());
            mySong.StartOrResume();
            Integer val = game.getPoint();
            val= val+10;
            game.setPoint(val);
            txtScore.setText("SCORE -- "+ val.toString());
        }

    }

    private void findAllView(){
        btnShot = (Button) findViewById(R.id.btGameShoot);
        pbLife = (ProgressBar) findViewById(R.id.progressBar2);
        txtLife = (TextView) findViewById(R.id.txtLife);
        txtAmmo = (TextView) findViewById(R.id.txtAmmouLeft);
        txtScore = (TextView) findViewById(R.id.txtGamePoint);
        txtKeys = (TextView) findViewById(R.id.txtGameKeys);
        txtMines = (TextView) findViewById(R.id.txtGameMines);
        txtDefuse = (TextView) findViewById(R.id.txtGameDefuse);
        txtCountDown = (TextView) findViewById(R.id.txtCountdown);
    }
    private  void startTimer(){
       countDownTimer = new CountDownTimer(mTimeLeftInMils*Integer.parseInt(game.getTime())/10, 1000) {
           @Override
           public void onTick(long millisUntilFinished) {
               mTimeLeftInMils = millisUntilFinished;
               updateCountDownText();
           }

           @Override
           public void onFinish() {
           }
       }.start();
       mTimerRunning =true;
    }

    private void checkForWin() {
        mySong.Pause();
        mySong.Destroy();
       HttpHelper httpHelper = new HttpHelper();
       httpHelper.HttpRequestForLooby(game.getPoint().toString(),"https://us-central1-arduino-a5968.cloudfunctions.net/endOfGameSendder");
       gameOver();
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMils/1000)/60;
        int seconds = (int) (mTimeLeftInMils/1000)%60;

        if (seconds == 0 && minutes == 0){
            checkForWin();
        }
        else if(seconds < 10 && minutes == 0){

            mySong = new MediaPlayerWrapper(R.raw.countdownbeep,getApplicationContext());
            mySong.StartOrResume();
            String timeLeftFromat = String.format(Locale.getDefault(), "%01d",seconds);
            txtCountDown.setText(timeLeftFromat);
        }
        else {
            String timeLeftFromat = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            txtCountDown.setText(timeLeftFromat);
        }
    }


    /**
     * get the data from the user that set the game
     */
    private void getDataFromSetting(){
        Bundle data = getIntent().getExtras();
        assert data != null;
        Member member = (Member) data.getParcelable("MyMember");
        assert member != null;
        game = new Game(member);
        game.setPoint(0);
        txtAmmo.setText("Ammuo Left:" + game.getAmmuo());
        txtLife.setText("LIFE- 3"); // TODO set to 100
        txtScore.setText("SCORE- 0");
        txtKeys.setText("KEYS - "+ game.getKeys());
        txtMines.setText("MINES - " + game.getMines());
        txtDefuse.setText("DEFUSE - 0");

    }

    private void ListnerForChangeInGame() {
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        final DocumentReference documentReference = fstore.collection("Game").document("firstgame");
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                String life_left = documentSnapshot.getString("LifePlayer"+game.getPlayerID());
                assert life_left != null;
                if(life_left.equals("0")){
                    checkForWin();
                }
                pbLife.setProgress(Integer.parseInt(life_left));
                txtLife.setText("LIFE: - " +life_left);
                txtScore.setText(("SCORE- ")+game.getPoint());

            }
        });
    }

    /*
    TODO we should make here the win or lose by the type of the game the played
        1. capture the flag
        2. points
        3. life
     */

    private void gameOver() {
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        final DocumentReference documentReference = fstore.collection("Game").document("endgame");
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                String valid_player1 = documentSnapshot.getString("valid1");
                final String valid_player2 = documentSnapshot.getString("valid2");
                if (valid_player1.equals("1") && valid_player2.equals("1")) {
                    Log.v("GAME-CLASS", "GAME------------FINISHED");
                    resetValue(); //reset game setting value
                    sendDataTodataBase(); //TODO
                    Intent intent = new Intent(getApplicationContext(), PopWindowGameOver.class); // Todo change for the right screen
                    startActivity(intent);
                }
            }
        });
    }

    private void sendDataTodataBase() {
        //create document to send the values at end of the game
    }

    public void resetValue(){
        HttpHelper httpHelper = new HttpHelper();
        HttpHelper httpHelper1 = new HttpHelper();
        httpHelper.HttpRequestForLooby("NO-ONE-IS-WAITING", "https://us-central1-arduino-a5968.cloudfunctions.net/addJoin");
        httpHelper1.HttpRequestForLooby("GAME-NOT-READY","https://us-central1-arduino-a5968.cloudfunctions.net/setGameReady");

    }
}
