package com.example.arduino.gameScreen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
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
    private static final double START_TIME_IN_MILLIS = 600000 ;
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
    private double mTimeLeftInMils = START_TIME_IN_MILLIS;
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
        // Listen to the shot button and update val
        btnShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer num =game.getAmmuo() -1;
                game.raiseBy1TotalData("Shots");
                if(num > 0 ){
                    mySong = new MediaPlayerWrapper(R.raw.goodgunshot,getApplicationContext());
                    mySong.StartOrResume();
                    game.setAmmuo(num);
                    txtAmmo.setText("Ammuo: - " + (game.getAmmuo().toString()));
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

    /**
     * check if the other player hit by getting data from arduino sensor
     * need to impalement
     */
    private void checkForHit() {

        boolean arduinoSensorLaser =true; //TODO check in the sensor if we hit the other player
        if(arduinoSensorLaser){
            mySong = new MediaPlayerWrapper(R.raw.boom,getApplicationContext());
            mySong.StartOrResume();
            Integer val = game.getPoint();
            val= val+10;
            game.setPoint(val);
            txtScore.setText("SCORE -- "+ val.toString());
            game.raiseBy1TotalData("Hits");
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
        if(game.getType() == 3){
            txtCountDown.setText("NO-TIME-UNTIL-DEATH");
        }
        else {
            countDownTimer = new CountDownTimer((long) (mTimeLeftInMils * game.getTime() / 10), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mTimeLeftInMils = (long) millisUntilFinished;
                    updateCountDownText();
                }

                @Override
                public void onFinish() {
                }
            }.start();
            mTimerRunning = true;
        }
    }

    /**
     * check for win is wrap function for game over  close all the relvent audio and things that are opened
     * TODO need to send the data of what happened we gat in the  arggs
     */
    private void checkForWin(EndOfGameReason endOfGameReason , StatusGame statusGame) {
        if(mySong != null){
            mySong.Pause();
            mySong.Destroy();
        }

       HttpHelper httpHelper = new HttpHelper();
        String points = game.getPoint().toString();
        String life = game.getLife().toString();
        String flag = game.getFlag().toString();
        String arggsfield = "?token="+game.getPlayerID()+"&flag="+flag+"&points="+points+"&life="+life+"&valid=1";
        String url = "https://us-central1-arduino-a5968.cloudfunctions.net/endOfGameSendder"+arggsfield;
       httpHelper.HttpRequest(url);
       gameOver(endOfGameReason ,statusGame);
    }

    /**
     * write to data base when the game is ended Log of the game for statics
     */

    private void writeDataToCloud(StatusGame statusGame) {
        FirebaseFirestore db =FirebaseFirestore.getInstance();
        String uid = game.getFirebaseId();
        db.collection("Logs").document(uid).set(game.toMap(statusGame,mTimeLeftInMils));
    }

    /**
     * update the clock and check if the game is ended by time
     */

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMils/1000)/60;
        int seconds = (int) (mTimeLeftInMils/1000)%60;

        if (seconds == 0 && minutes == 0){
            checkForWin(EndOfGameReason.TIME, StatusGame.DONTKNOW);
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
        txtAmmo.setText("Ammuo Left:" + game.getAmmuo().toString());
        txtLife.setText("LIFE- 100"); // TODO set to 100
        txtScore.setText("SCORE- 0");
        txtKeys.setText("KEYS - "+ game.getKeys().toString());
        txtMines.setText("MINES - " + game.getMines().toString());
        txtDefuse.setText("DEFUSE - 0");

    }

    private void ListnerForChangeInGame() {
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        final DocumentReference documentReference = fstore.collection("Game").document("firstgame");
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                String my_life_left = documentSnapshot.getString("LifePlayer"+game.getPlayerID());
                String opp_life_left = documentSnapshot.getString("LifePlayer"+game.getOppID());
                Long my_flag = (Long) documentSnapshot.get("flag"+game.getPlayerID());
                Long opp_flag = (Long) documentSnapshot.get("flag"+game.getOppID());
                assert my_life_left != null;
                assert opp_flag !=null;
                assert my_flag != null;
                assert opp_flag != null;
                game.setFlag(my_flag);
                // check if someone lose
                if(my_life_left.equals("0") || opp_life_left.equals("0")){
                    if(my_life_left.equals("0")){  // I lost ):
                        checkForWin(EndOfGameReason.LIFE,StatusGame.LOSE);
                    }
                    else{  // I Won (:
                        checkForWin(EndOfGameReason.LIFE, StatusGame.WIN);
                    }
                }
                else if( my_flag == 1 || opp_flag == 1){
                    if(opp_flag == 1){  // I lost ):
                        checkForWin(EndOfGameReason.FLAG,StatusGame.LOSE);
                    }
                    else{  // I Won (:
                        checkForWin(EndOfGameReason.FLAG, StatusGame.WIN);
                    }

                }
                pbLife.setProgress(Integer.parseInt(my_life_left));
                txtLife.setText("LIFE: - " +my_life_left);
                txtScore.setText(("SCORE- ")+game.getPoint().toString());

            }
        });
    }

    /*
    TODO we should make here the win or lose by the type of the game the played
        1. capture the flag
        2. points
        3. life
     */

    private void gameOver(EndOfGameReason endOfGameReason , StatusGame statusGame) {
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        final DocumentReference documentReference = fstore.collection("Game").document("endgame");
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                String valid_player1 = documentSnapshot.getString("valid1");
                final String valid_player2 = documentSnapshot.getString("valid2");
                String my_points =  documentSnapshot.getString("points"+game.getPlayerID());
                String opp_points =  documentSnapshot.getString("points"+game.getOppID());
                String my_flag =  documentSnapshot.getString("flag"+game.getPlayerID());
                String opp_flag =  documentSnapshot.getString("flag"+game.getOppID());
                String my_life =  documentSnapshot.getString("life"+game.getPlayerID());
                String opp_life =   documentSnapshot.getString("life"+game.getOppID());

                if (valid_player1.equals("1") && valid_player2.equals("1")) {  //TODO check that we reset those values
                    resetValue(); //reset game setting value
                    Log.v("GAME-CLASS", "GAME------------FINISHED");
                    if(game.getType() == 1 ) // capture the flag
                    {
                        assert (my_flag != null);
                        assert (opp_flag != null);
                        if(my_flag.equals("1")){  // I Won
                            writeDataToCloud(StatusGame.WIN); //TODO in this postion all lose
                            Intent intent = new Intent(getApplicationContext(), PopWindowWin.class); // Todo change for the right screen
                            startActivity(intent);
                        }
                        else{   // Lost
                            writeDataToCloud(StatusGame.LOSE); //TODO in this postion all lose
                            Intent intent = new Intent(getApplicationContext(), PopWindowGameOver.class); // Todo change for the right screen
                            startActivity(intent);
                        }
                    }
                    else if(game.getType() == 2)  // High score game
                    {
                        assert (my_points != null);
                        assert (opp_points != null);
                        if(my_points.equals(opp_points)){  // draw
                            writeDataToCloud(StatusGame.DRAW); //TODO in this postion all lose
                            Intent intent = new Intent(getApplicationContext(), PopWindowWin.class); // Todo change for the right screen
                            startActivity(intent);
                        }
                        else if (Integer.parseInt(my_points) > Integer.parseInt(opp_points)){   // won
                            writeDataToCloud(StatusGame.WIN); //TODO in this postion all lose
                            Intent intent = new Intent(getApplicationContext(), PopWindowWin.class); // Todo change for the right screen
                            startActivity(intent);
                        }
                        else{  // lost
                            writeDataToCloud(StatusGame.LOSE); //TODO in this postion all lose
                            Intent intent = new Intent(getApplicationContext(), PopWindowGameOver.class); // Todo change for the right screen
                            startActivity(intent);
                        }

                    }

                    else{      // life last tank stand game
                        assert (my_life != null);
                        assert (opp_life != null);
                        if(Integer.parseInt(my_life) > Integer.parseInt(opp_life)){  // I Won
                            writeDataToCloud(StatusGame.WIN); //TODO in this postion all lose
                            Intent intent = new Intent(getApplicationContext(), PopWindowWin.class); // Todo change for the right screen
                            startActivity(intent);
                        }
                        else{   // Lost
                            writeDataToCloud(StatusGame.LOSE); //TODO in this postion all lose
                            Intent intent = new Intent(getApplicationContext(), PopWindowGameOver.class); // Todo change for the right screen
                            startActivity(intent);
                        }
                    }

                }
            }
        });
    }

    private void sendDataTodataBase() {
        //create document to send the values at end of the game
    }

    /**
     * RESET input data in appending collection in firestore
     */


    //TODO NEED to reset the flag in frst game
    public void resetValue(){
        HttpHelper httpHelper = new HttpHelper();
        HttpHelper httpHelper1 = new HttpHelper();
        HttpHelper httpHelper2 = new HttpHelper();
        httpHelper2.HttpRequest("https://us-central1-arduino-a5968.cloudfunctions.net/resetvalidend?token="+game.getPlayerID());
        httpHelper.HttpRequestForLooby("NO-ONE-IS-WAITING", "https://us-central1-arduino-a5968.cloudfunctions.net/addJoin");
        httpHelper1.HttpRequestForLooby("GAME-NOT-READY","https://us-central1-arduino-a5968.cloudfunctions.net/setGameReady");

    }
}
