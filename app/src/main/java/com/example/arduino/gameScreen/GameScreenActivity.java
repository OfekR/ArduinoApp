package com.example.arduino.gameScreen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arduino.R;
import com.example.arduino.defines.LogDefs;
import com.example.arduino.initGame.GameSetting;
import com.example.arduino.initGame.Member;
import com.example.arduino.loby.PopWindow;
import com.example.arduino.utilities.HttpHelper;
import com.example.arduino.utilities.MediaPlayerWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.controlwear.virtual.joystick.android.JoystickView;



/*
TODO : we need to reorganize the class.
TODO : add the auth with the game for change the field in the database
 */

public class GameScreenActivity extends AppCompatActivity {
    private static final String TAG = LogDefs.tagGameScreen;

    private static final long START_TIME_IN_MILLIS = 600000;
    private ValueEventListener registration;
    private ValueEventListener gameStartRegistration;
    private DatabaseReference mDatabase;
    private Game game;
    private DatabaseReference gamedocRef;
    private ProgressBar pbLife;
    private TextView txtLife;
    private TextView txtAmmo;
    private TextView txtScore;
    private TextView txtKeys;
    private TextView txtMines;
    private TextView txtDefuse;
    private TextView txtSpeicalKey;

    private Button btnShot;
    private Button btnKey;
    private Button btnMine;
    private TextView txtCountDown;
    private CountDownTimer countDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMils = START_TIME_IN_MILLIS;
    private MediaPlayerWrapper mySong;
    private JoystickView joystickCar;
    private JoystickView joystickServo;
    //BT variables
    //TODO - set the real ones, for now the first is the tank second is the car (for now set both to the same
    //private final String DEVICE_ADDRESS_P1 = "98:D3:51:FD:D9:45";
    private final String DEVICE_ADDRESS_P1 = "98:D3:61:F5:E7:3C";
    private final String DEVICE_ADDRESS_P2 = "98:D3:61:F5:E7:3C";
    private final int laserShootLengthMS = 2000;
    private static final String TAG_BT = LogDefs.tagBT;
    private static final int REQUEST_ENABLE_BT = 3; //just random value , 3 doesn't mean anything

    private String DEVICE_ADDRESS; //MAC Address of Bluetooth Module
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private BluetoothAdapter mBluetoothAdapter;

    //TODO bundle to struct
    private AtomicBoolean _isBtConnected;
    private AtomicBoolean _isInternetConnected;
    private AtomicBoolean _isCarInPlace;

    private Integer _playerId;

    private String command = "";
    private AtomicBoolean _isGameStarted;

    AlertDialog waitDialog;
    boolean doubleBackToExitPressedOnce = false;

    //Aux classes
    private RfidHandler _rfidHandler;
    private LiveGameInfo _liveGameInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_game_screen);

        findAllView();

        initPreGameVariables();
        initButtonsListeners();
        showWaitScreen();

        waitUntillGameReady();
        //TODO OFEK DEBUG
        /*
        initBluetoothConnection();
        initCarInPlace();
        initInternetConnected();
        */

    }

    /** ************************* General ************************* **/
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //super.onBackPressed();
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int optionChoosen) {
                    switch (optionChoosen){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            forfeitMatch();
                            dialog.dismiss();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked - nothing to do
                            dialog.dismiss();
                            break;
                    }
                }
            };

            final AlertDialog gameForfitDialog = new AlertDialog.Builder(this).setMessage("Are you sure you want to forfeit the match?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .create();
            //this attributes to avoid user be able to remove dialog
            gameForfitDialog.setCancelable(false);
            gameForfitDialog.setCanceledOnTouchOutside(false);
            gameForfitDialog.show();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to forfeit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private void forfeitMatch()
    {
        //TODO IMPORTANT - pass param to indicate this user lost
        checkForWin();
    }

    private void updateGameStartedField(boolean isReady)
    {
        String key;
        if (_playerId.equals(1)) {
            //player 1
            key = "P1_Ready";
        }
        else{
            //player 2
            //message.equals("Join")
            key = "P2_Ready";
        }
        //update firebase with the updated value
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put(key, isReady);
        mDatabase.child("GameStarted").updateChildren(hashMap);
    }
    /** ************************* PreGame Settings ************************* **/

    /**
     * wait screen , appear as long the game hasn't started yet
     */
    private void showWaitScreen() {
        Toast.makeText(getApplicationContext(),"player number -- "+game.getPlayerID(),Toast.LENGTH_LONG).show();
        waitDialog = new AlertDialog.Builder(this).setMessage("waiting for both player to be ready to play").create();
        //this attributes to avoid user be able to remove dialog
        waitDialog.setCancelable(false);
        waitDialog.setCanceledOnTouchOutside(false);
        waitDialog.show();
    }

    /**
     * starting game screen , appear for X seconds after both player are ready and game about to sttart
     */
    private void showStartingGameScreen()
    {
        final int waitingTimeSec = 3;
        final AlertDialog gameStartingDialog = new AlertDialog.Builder(this).setMessage("Game Starting in " + waitingTimeSec + " seconds.....").create();
        //this attributes to avoid user be able to remove dialog
        gameStartingDialog.setCancelable(false);
        gameStartingDialog.setCanceledOnTouchOutside(false);
        gameStartingDialog.show();

        // Hide after some seconds
        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (gameStartingDialog.isShowing()) {
                    gameStartingDialog.dismiss();
                }
            }
        };

        gameStartingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, waitingTimeSec * 1000);
    }

    /**
     * init all views
     */
    private void findAllView(){
        btnShot = (Button) findViewById(R.id.btGameShoot);
        btnKey = (Button) findViewById(R.id.btGameKeys);
        btnMine = (Button) findViewById(R.id.btGameMines);
        pbLife = (ProgressBar) findViewById(R.id.progressBar2);
        txtLife = (TextView) findViewById(R.id.txtLife);
        txtAmmo = (TextView) findViewById(R.id.txtAmmouLeft);
        txtScore = (TextView) findViewById(R.id.txtGamePoint);
        txtKeys = (TextView) findViewById(R.id.txtGameKeys);
        txtMines = (TextView) findViewById(R.id.txtGameMines);
        txtDefuse = (TextView) findViewById(R.id.txtGameDefuse);
        txtSpeicalKey =  (TextView) findViewById(R.id.txtGameSpecialKeys);
        txtCountDown = (TextView) findViewById(R.id.txtCountdown);
        joystickCar = (JoystickView) findViewById(R.id.joystickViewCarControl);
        joystickServo = (JoystickView) findViewById(R.id.joystickViewTurretControl);

    }

    /**
     * init all preGame variables
     */
    private void initPreGameVariables()
    {
        //disable buttons until game starting
        disableEnableButtons(false);
        getDataFromSetting();
        _isGameStarted = new AtomicBoolean(false);
        _isInternetConnected = new AtomicBoolean(false);
        _isCarInPlace = new AtomicBoolean(false);

        //check which player is it
        Bundle bundle = getIntent().getExtras();
        String message = bundle.getString("Classifier");
        _playerId = (message.equals("Init")) ? 1 : 2;
        _liveGameInfo = new LiveGameInfo(this, mDatabase, _playerId, btnShot, txtLife, txtAmmo, txtScore, txtKeys, txtMines, txtDefuse, txtSpeicalKey);
        _liveGameInfo.initLivePlayerInfoFromSettings();

        _rfidHandler = new RfidHandler(this, mDatabase ,_playerId,btnMine,btnKey,btnShot,joystickCar,joystickServo, _liveGameInfo);

    }

    /**
     * init all listeners
     */
    private void initButtonsListeners()
    {
        joystickCar.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                try {
                    char c = findCommandCar(angle,strength);
                    System.out.println("The-Car-JoyStick angle -------> " + angle+ "and the Char i send is ---> " + c);
                    outputStream.write(c); //transmits the value of command to the bluetooth

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("The-CarFail-JoyStick angle -------> " + angle+ "The- Strength--------->  "+strength);
                }
            }
        });
        // Listen to the shot button and update val
        joystickServo.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                try {
                    if(strength != 0)
                    {
                        char c = findCommandServo(angle,strength);
                        System.out.println("The-Servo-JoyStick angle -------> " + angle+ "and the Char i send is ---> " + c);
                        outputStream.write(c); //transmits the value of command to the bluetooth
                    }
                    //else - strength 0, nothing to move


                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("The-ServoFail-JoyStick angle -------> " + angle+ "The- Strength--------->  "+strength);
                }
            }
        });

        btnShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mySong !=  null) mySong.Destroy();
                mySong = new MediaPlayerWrapper(R.raw.goodgunshot,getApplicationContext());
                mySong.StartOrResume();

                _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.AMMO,-1);
                //TODO OFEK DEBUG
                //SendCommandShotLaser();

                //TODO - add delay , so can't shot multiple time (laser already on for X Sec)


/*
                Integer numOfAmmoLeft =game.getAmmuo();
                assert(numOfAmmoLeft >= 0);
                if(numOfAmmoLeft == 0)
                {
                    //TODO OFEK restore this line once you handled lootbox (that when more ammo added, back to be enabled
                    // Note - when bt lose connection and restore he enables all buttons
                    //        btnShot.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "No Ammo", Toast.LENGTH_LONG).show();
                    return;
                }
                //TODO:: Miki - uncomment
  //              SendCommandShotLaser();

                game.raiseBy1TotalData("Shots");
                if(mySong !=  null) mySong.Destroy();
                mySong = new MediaPlayerWrapper(R.raw.goodgunshot,getApplicationContext());
                mySong.StartOrResume();
                game.setAmmuo(numOfAmmoLeft - 1);
                txtAmmo.setText("Ammuo: - " + (game.getAmmuo().toString()));
                //hit sound
                // checkForHit();

                if(numOfAmmoLeft - 1 == 0 ) {
                    txtAmmo.setText("NO- Ammuo - !!!!!!!!: - ");
                    //TODO OFEK restore this line once you handled lootbox (that when more ammo added, back to be enabled
                    // OR - just listen to number of ammo and then update accordingly
                    // Note - when bt lose connection and restore he enables all buttons
                    //        btnShot.setEnabled(false);
                }*/
            }
        });

        /*final String playerIdStr = (_playerId.equals(1)) ? ""
        btnMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("Game").child(_playerIdStr).child("LootBox").child("PickedSpecialLootbox").setValue(0);



                Integer numOfAmmoLeft =game.getAmmuo();
                assert(numOfAmmoLeft >= 0);
                if(numOfAmmoLeft == 0)
                {
                    //TODO OFEK restore this line once you handled lootbox (that when more ammo added, back to be enabled
                    // Note - when bt lose connection and restore he enables all buttons
                    //        btnShot.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "No Ammo", Toast.LENGTH_LONG).show();
                    return;
                }
                //TODO:: Miki - uncomment
                //              SendCommandShotLaser();

                game.raiseBy1TotalData("Shots");
                if(mySong !=  null) mySong.Destroy();
                mySong = new MediaPlayerWrapper(R.raw.goodgunshot,getApplicationContext());
                mySong.StartOrResume();
                game.setAmmuo(numOfAmmoLeft - 1);
                txtAmmo.setText("Ammuo: - " + (game.getAmmuo().toString()));
                //hit sound
                // checkForHit();

                if(numOfAmmoLeft - 1 == 0 ) {
                    txtAmmo.setText("NO- Ammuo - !!!!!!!!: - ");
                    //TODO OFEK restore this line once you handled lootbox (that when more ammo added, back to be enabled
                    // OR - just listen to number of ammo and then update accordingly
                    // Note - when bt lose connection and restore he enables all buttons
                    //        btnShot.setEnabled(false);
                }
            }
        });*/
    }

    private void startRfidListeners()
    {
        _rfidHandler.startListeners();
    }

    /**
     * here game will be dispatched to start.
     * does it by listening to GameStarted db which contain two boolean, one for each player.
     * boolean are updated in checkIfPlayerReady function
     */
    private void waitUntillGameReady()
    {
        //wait untill all players joined
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        gamedocRef = database.getReference("GameStarted/");

        gameStartRegistration = gamedocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                {
                    System.out.println("GameStarted snapshot is empty");
                    Log.e(TAG,LogDefs.gameStartingListenerFailed);
                }
                //get both ready values from FB and if both true initalize start game
                boolean p1Ready = (boolean) dataSnapshot.child("P1_Ready").getValue();
                boolean p2Ready = (boolean) dataSnapshot.child("P2_Ready").getValue();
                System.out.println("P1 status is " + p1Ready + " P2 status is " + p2Ready  );
                if(p1Ready && p2Ready){
                    //Both players are all set, start the game
                    Log.d(TAG, LogDefs.gameStarting);
                    startGame();
                    gamedocRef.removeEventListener(this);

                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    /**
     * checks if user car is in the right place
     * does it by listening to /TODO/ db which contain boolean which indicate car in right place
     * boolean is updated by cloud function called by arduino which RFID tag
     */
    private void initCarInPlace()
    {
        //TODO IMPL
        _isCarInPlace.set(true);
        checkIfPlayerReady();
    }


    /**
     * called after every preGameBooleans is updated, and check if all of them were set
     * if all set, update firebase - which indicate this player is ready to play
     */
    private void checkIfPlayerReady()
    {
        if(_isInternetConnected.get() && _isCarInPlace.get() && _isBtConnected.get())
        {
            //get correct key according to current player
            updateGameStartedField(true);
        }
    }

    /**
     * called when both player are ready to play
     * initalize all listener for in game and initalize additional final things (timer, buttons etc)
     */
    private void startGame()
    {
        waitDialog.dismiss();
        showStartingGameScreen();

        _isGameStarted.set(true);
        disableEnableButtons(true);
        startRfidListeners();
        //TODO OFEK DEBUG#$#@
        _liveGameInfo.startListeners();
        //ListnerForChangeInGame();

        startTimer();
    }

    /** ************************* In Game Settings ************************* **/


    /**
     * check if the other player hit by getting data from arduino sensor
     * need to impalement
     */
    private void checkForHit() {

        boolean arduinoSensorLaser =true; //TODO check in the sensor if we hit the other player
        if(arduinoSensorLaser){
         //   mySong = new MediaPlayerWrapper(R.raw.boom,getApplicationContext());
        //    mySong.StartOrResume();
            Integer val = game.getPoint();
            val= val+10;
            game.setPoint(val);
            txtScore.setText("SCORE -- "+ val.toString());
            game.raiseBy1TotalData("Hits");
            mDatabase.child("Game").child("points"+game.getPlayerID()).setValue(val.toString());

        }

    }

    private  void gatHit(){
        Integer hit;
        if(game.getPlayerID().equals("1")){
            hit = game.getValuesShared().getLife1() -1;
        }
        // player 2
        else{
            hit = game.getValuesShared().getLife2()  -1;
        }
        if(mySong !=  null) mySong.Destroy();
        mySong = new MediaPlayerWrapper(R.raw.boom,getApplicationContext());
        mySong.StartOrResume();
        mDatabase.child("Game").child("life"+game.getPlayerID()).setValue(hit.toString());
    }



    private  void startTimer(){
        if(game.getType() == 3){
            txtCountDown.setText("NO-TIME-UNTIL-DEATH");
            txtCountDown.setTextSize(15);
            txtCountDown.getCompoundPaddingTop();
            txtCountDown.getPaddingRight();
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
                    checkForWin();
                }
            }.start();
            mTimerRunning = true;
        }
    }

    /**
     * check for win is wrap function for game over  close all the relvent audio and things that are opened
     * TODO need to send the data of what happened we gat in the  arggs
     */
    private void checkForWin() {
        //if(endOfGameReason.equals(EndOfGameReason.FLAG)) Log.d("GAME-REASON-END-->","We Lost Because -  FLAG");
        //if(endOfGameReason.equals(EndOfGameReason.LIFE)) Log.d("GAME-REASON-END-->","We Lost Because -  LIFE");
        //if(endOfGameReason.equals(EndOfGameReason.TIME)) Log.d("GAME-REASON-END-->","We Lost Because -  TIME");


        gamedocRef.removeEventListener(registration);
        countDownTimer.cancel();
       gameOver();
    }

    /**
     * write to data base when the game is ended Log of the game for statics (stats also update auto)
     */

    private void writeDataToCloud(final StatusGame statusGame) {
        FirebaseFirestore db =FirebaseFirestore.getInstance();
        String uid = game.getFirebaseId();
        db.collection("Logs").document(uid).set(game.toMap(statusGame,mTimeLeftInMils));
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        final DocumentReference documentReference = fstore.collection("PlayerStats").document(game.getFirebaseId());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                // before
                DocumentSnapshot documentSnapshot= task.getResult();
                assert documentSnapshot != null;
                Long _bestTime = (Long) documentSnapshot.get("bestTime");
                Long _gamesLost = (Long) documentSnapshot.get("gamesLost");
                Long _gamesPlayed = (Long) documentSnapshot.get("gamesPlayed");
                Long _gamesWon = (Long) documentSnapshot.get("gamesWon");
                Long _hitsPercentage = (Long) documentSnapshot.get("hitsPercentage");
                Long _mostBombHits = (Long) documentSnapshot.get("mostBombHits");
                Long _mostLaserHits = (Long) documentSnapshot.get("mostLaserHits");
                Long _totalBombHits = (Long) documentSnapshot.get("totalBombHits");
                Long _totalHits = (Long) documentSnapshot.get("totalHits");
                Long _totalPoints = (Long) documentSnapshot.get("totalPoints");
                Long _totalShots = (Long) documentSnapshot.get("totalShots");
                Long _flags = (Long) documentSnapshot.get("flags");
                Long _numPlayedflags = (Long) documentSnapshot.get("numPlayedflags");
                Long _numPlayedtime = (Long) documentSnapshot.get("numPlayedtime");
                Long _numPlayedhighscore = (Long) documentSnapshot.get("numPlayedhighscore");

                assert(_bestTime != null);
                assert _gamesLost != null;
                assert _gamesPlayed != null;
                assert _gamesWon != null;
                assert _hitsPercentage != null;
                assert _mostBombHits != null;
                assert _mostLaserHits != null;
                assert _totalBombHits != null;
                assert _totalHits != null;
                assert _totalPoints != null;
                assert _totalShots != null;
                assert(_flags != null);
                assert  _numPlayedflags != null;
                assert  _numPlayedtime != null;
                assert _numPlayedhighscore != null;
                Long totalshots = game.getTotalData()[0];
                Long totalbomb = game.getTotalData()[1];
                Long totalhits = game.getTotalData()[2];
                if (_bestTime < mTimeLeftInMils) {
                    _bestTime = mTimeLeftInMils;
                }
                if (_mostBombHits < totalbomb) {
                    _mostBombHits = totalbomb;
                }
                if (_mostLaserHits < totalhits) {
                    _mostLaserHits = totalhits;
                }
                _gamesPlayed = _gamesPlayed + 1;
                if (statusGame.equals(StatusGame.WIN)) {
                    _gamesWon = _gamesWon + 1;

                }
                if (statusGame.equals(StatusGame.LOSE)) {
                    _gamesLost = _gamesLost + 1;

                }
                if(game.getType() == 1){
                    _numPlayedflags = _numPlayedflags+1;
                }
                else if(game.getType() == 2){
                    _numPlayedhighscore = _numPlayedhighscore+1;
                }
                else{
                    _numPlayedtime = _numPlayedtime+1;
                }
                _totalBombHits = _totalBombHits + totalbomb;
                _totalHits = _totalHits + totalhits; //TODO fix it
                _totalPoints = _totalPoints + game.getPoint();
                _totalShots = _totalShots + totalshots;
                if (_totalShots != 0) {
                    _hitsPercentage = _totalHits / _totalShots;
                }
                DocumentMover documentMover = new DocumentMover(game.getFirebaseId(),_bestTime,_gamesLost,_gamesPlayed,_gamesWon,
                        _hitsPercentage,_mostBombHits,_mostLaserHits,_totalBombHits,_totalHits,_totalPoints,_totalShots,_flags,_numPlayedflags,_numPlayedhighscore,_numPlayedtime);
                Log.d("Writing to data cloud","-----> Moving to Pop window");
                if(statusGame.equals(StatusGame.WIN)){
                    Intent intent = new Intent(getApplicationContext(), PopWindowWin.class); // Todo change for the right screen
                    intent.putExtra("DocumentPusher",documentMover);
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
                }
                else if (statusGame.equals(StatusGame.LOSE)){
                    Intent intent = new Intent(getApplicationContext(), PopWindowGameOver.class); // Todo change for the right screen
                    intent.putExtra("DocumentPusher",documentMover);
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), PopWindowWin.class); // Todo change for the right screen
                    intent.putExtra("DocumentPusher",documentMover);
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
                }
            }
        });
       // registration.remove();
    }

    /**
     * update the clock and check if the game is ended by time
     */

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMils/1000)/60;
        int seconds = (int) (mTimeLeftInMils/1000)%60;

        if (seconds == 0 && minutes == 0){
           // mySong.Pause();
           // mySong.Destroy();
         //   checkForWin(EndOfGameReason.TIME, StatusGame.DONTKNOW);
        }
        else if(seconds < 10 && minutes == 0){

          //  mySong = new MediaPlayerWrapper(R.raw.countdownbeep,getApplicationContext());
          //  mySong.StartOrResume();
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
//        Member member = (Member) data.getParcelable("MyMember");
//        assert member != null;
//        game = new Game(member);
        GameSetting gameSetting = (GameSetting) data.getParcelable("GameSettings");
        assert gameSetting != null;
        game = new Game(gameSetting);
        game.setPoint(0);
        txtAmmo.setText("Ammuo Left:" + game.getAmmuo().toString());
        txtLife.setText("LIFE- 100"); // TODO set to 100
        txtScore.setText("SCORE- 0");
        txtKeys.setText("KEYS - "+ game.getKeys().toString());
        txtMines.setText("MINES - " + game.getMines().toString());
        txtDefuse.setText("DEFUSE - 0");

    }

    // All game manegement
    //TODO OFEK NOW - change this function
    private void ListnerForChangeInGame(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        gamedocRef = database.getReference("Game/");

        registration = gamedocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //TODO OFEK - update this to all values...
                Integer my_life_left,opp_flag,my_flag,opp_life_left,my_hit,opp_hit,my_points,opp_points,my_mine,my_keys,my_defuse,my_spkey,opp_spkey;
                ValuesShared values = dataSnapshot.getValue(ValuesShared.class);
                game.setValuesShared(values);
                assert values != null;
                if(game.getPlayerID().equals("1")){
                    my_life_left = values.getLife1();
                    my_flag = values.getFlag1();
                    opp_flag = values.getFlag2();
                    opp_life_left = values.getLife2();
                    my_hit = values.getValid1();
                    opp_hit = values.getValid2();
                    my_points =values.getPoints1();
                    opp_points =values.getPoints2();
                    my_mine =values.getMine1();
                    my_keys =values.getKeys1();
                    my_defuse =values.getDefuse1();
                    my_spkey =values.getDefuse1();

                }
                else{
                     my_life_left = values.getLife2();
                     my_flag = values.getFlag2();
                     opp_flag = values.getFlag1();
                     opp_life_left = values.getLife1();
                     opp_hit = values.getValid1();
                     my_hit = values.getValid2();
                    my_points =values.getPoints2();
                    opp_points =values.getPoints1();
                    my_mine =values.getMine2();
                    my_keys =values.getKeys2();
                    my_defuse =values.getDefuse2();
                    my_spkey =values.getDefuse1();
                }
                if(!game.getDefuse().equals(my_defuse)){
                    if(game.getDefuse() < my_defuse)
                        Toast.makeText(getApplicationContext(), "You gat from the lot box - Defuse ",Toast.LENGTH_LONG).show();
                    else{
                        Toast.makeText(getApplicationContext(), "Bomb has been defused ",Toast.LENGTH_LONG).show();
                    }
                }
                if(game.getPoint()+100 == my_points){
                    Toast.makeText(getApplicationContext(), "You gat from the lot box - Points ",Toast.LENGTH_LONG).show();
                }
                if(!game.getKeys().equals(my_keys)){
                    if(game.getKeys() < my_keys)
                        Toast.makeText(getApplicationContext(), "You gat from the lot box - Key ",Toast.LENGTH_LONG).show();
                    else{
                        Toast.makeText(getApplicationContext(), "Gate is now opening ",Toast.LENGTH_LONG).show();
                    }
                }
                if(!game.getMines().equals(my_mine)){
                    if(game.getMines() < my_mine)
                        Toast.makeText(getApplicationContext(), "You gat from the lot box -  Mine ",Toast.LENGTH_LONG).show();
                    else{
                        Toast.makeText(getApplicationContext(), "Mine has been set ",Toast.LENGTH_LONG).show();
                    }
                }
                if(my_life_left+5 == game.getLife()){
                    Toast.makeText(getApplicationContext(), "You gat hit from a mine  ",Toast.LENGTH_LONG).show();
                }
                game.setDefuse(my_defuse);
                game.setKeys(my_keys);
                game.setMines(my_mine);
                game.setFlag(my_flag);
                game.setLife(my_life_left);
                game.setPoint(my_points);
                //update number hit
                assert(my_hit >= game.getNum_hits());
                if(my_hit > game.getNum_hits()){
                    makeToast();
                    game.setNum_hits(game.getNum_hits()+1);
                    gatHit();
                    //TODO - add field to Game firbase field which indicate hit, and change the point calc to oppenet side
                    opp_points = opp_points+10;
                    mDatabase.child("Game").child("points"+game.getOppID()).setValue(opp_points.toString());
                }
                if(my_life_left <= 0 || opp_life_left <= 0 ||  my_flag ==  1 || opp_flag == 1 ) {
                    checkForWin();
                }
                pbLife.setProgress(my_life_left);
                txtLife.setText("LIFE: - " +my_life_left);
                txtScore.setText(("SCORE- ")+game.getPoint().toString());
                txtKeys.setText("KEYS - "+ game.getKeys().toString());
                txtMines.setText("MINES - " + game.getMines().toString());
                txtDefuse.setText("DEFUSE - " + game.getDefuse().toString());

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
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
        Integer my_life_left,opp_flag,my_flag,opp_life_left,my_points,opp_points;
        ValuesShared values = game.getValuesShared();   // reading the val for help
        if(game.getPlayerID().equals("1")){
            my_life_left = values.getLife1();
            my_flag = values.getFlag1();
            opp_flag = values.getFlag2();
            opp_life_left = values.getLife2();
            opp_points = values.getPoints2();
            my_points = values.getPoints1();
        }
        else{
            my_life_left = values.getLife2();
            my_flag = values.getFlag2();
            opp_flag = values.getFlag1();
            opp_life_left = values.getLife1();
            opp_points = values.getPoints1();
            my_points = values.getPoints2();
        }
        resetValue(); //reset game setting value
        Log.v("GAME-CLASS", "GAME------------FINISHED");
        if(game.getType() == 1 ){ // capture the flag
            if(my_life_left == 0 ){
                writeDataToCloud(StatusGame.LOSE);
            }
            if(my_flag == 0){  // I Won
                writeDataToCloud(StatusGame.WIN);
            }
            else{   // Lost
                writeDataToCloud(StatusGame.LOSE);
            }
        }
        else if(game.getType() == 2){  // High score game
            if(my_life_left == 0 ){
                writeDataToCloud(StatusGame.LOSE);
            }
            if(my_points.equals(opp_points)){  // draw
                writeDataToCloud(StatusGame.DRAW);
            }
            else if (my_points > opp_points){   // won
                writeDataToCloud(StatusGame.WIN);
            }
            else{  // lost
                writeDataToCloud(StatusGame.LOSE);
            }

        }
        else{      // life last tank stand game
            if(my_life_left >= opp_life_left){  // I Won
                writeDataToCloud(StatusGame.WIN);
            }
            else{   // Lost
                writeDataToCloud(StatusGame.LOSE);
            }
        }
    }


    private void sendDataTodataBase() {
        //create document to send the values at end of the game
    }

    /**
     * RESET input data in appending collection in firestore
     */


    //TODO NEED to reset the flag in frst game
    public void resetValue(){
        terminateBT();
        HttpHelper httpHelper = new HttpHelper();
        HttpHelper httpHelper1 = new HttpHelper();
        ValuesShared vs = new ValuesShared();
        mDatabase.child("Game").setValue(vs.resetObject());
        mDatabase.child("ValueForStart").child("create").setValue("0");
        mDatabase.child("ValueForStart").child("join").setValue("0");

       // httpHelper.HttpRequestForLooby("NO-ONE-IS-WAITING", "https://us-central1-arduino-a5968.cloudfunctions.net/addJoin");
        //httpHelper1.HttpRequestForLooby("GAME-NOT-READY","https://us-central1-arduino-a5968.cloudfunctions.net/setGameReady");

        //update GameStarted
        updateGameStartedField(false);
    }



    // used to toggle status of buttons, in order to disbale buttons once coneection to BT is lost or game hasn't started yet
    private void disableEnableButtons(boolean isEnable)
    {
        btnShot.setEnabled(isEnable);
        joystickCar.setEnabled(isEnable);
        joystickServo.setEnabled(isEnable);
    }

    /** ************************* Internet ************************* **/
    /**
     * checks if internet connection exist, if not prompt user to connect
     */
    private void initInternetConnected()
    {
        //TODO do a listener like bt maybe
        boolean isInternetConnected = isConnectionAvaliable();
        if(isInternetConnected)
        {
            _isInternetConnected.set(isInternetConnected);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int optionChoosen) {
                    switch (optionChoosen){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            initInternetConnected();
                            break;

                    }
                }
            };


            builder.setMessage("No internet connection, Please connect to internet").setPositiveButton("Retry", dialogClickListener)
                   .setCancelable(false).show();

            return;
        }
        checkIfPlayerReady();
    }



    public boolean isInternetAvailable() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        } catch (UnknownHostException e) {
            // Log error
        }
        return false;
    }

    public boolean isConnectionAvaliable()
    {
        return true;
        /*
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager == null) return false;
        if(Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;

        return connected;
         */
    }

/*
    private void checkInternetConnection() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wifiConnectionReceiver, intentFilter);
    }
    //TODO OFEK -maybe can merge this with the BT
    private final BroadcastReceiver wifiConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(ConnectivityManager.)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(getApplicationContext(), "lost connection to BT, trying to reconnect",Toast.LENGTH_LONG).show();
                        disableEnableButtons(false);
                        BtInitOfek();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };
*/

    /** ************************* Bluetooth ************************* **/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENABLE_BT)
        {
            if(resultCode == RESULT_OK)
            {
                Toast.makeText(this, "BT enabled successfully", Toast.LENGTH_LONG).show();
                Log.d(TAG_BT, LogDefs.btEnabledSuccessfully);
                BtInitOfek();
            }
            else
            {
                Toast.makeText(this, "BT enabled failed, please enable it manually", Toast.LENGTH_LONG).show();
                Log.d(TAG_BT, LogDefs.btEnabledFailed);
            }
        }
    }



    public boolean BtInitOfek()
    {
        if (mBluetoothAdapter == null )
        {
            Toast.makeText(this, "Device doesn't support BT", Toast.LENGTH_SHORT).show();
            Log.e(TAG_BT,LogDefs.btDeviceNotSupported);
            return false;
        }

        if (mBluetoothAdapter.isEnabled()) //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
        {

            BtInitAfterEnable();

        }
        else
        {
            Log.d(TAG_BT,LogDefs.btWasDisabled);
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        return true;
    }

    private void initBluetoothConnection() {
        _isBtConnected = new AtomicBoolean(false);
        setBtMacAddress();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);
        IntentFilter filterChange = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(btConnectionLostReceiver, filterChange);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BtInitOfek();
    }


    protected void BtInitAfterEnable()
    {
        if(queryPairedDevice())
        {
            Log.d(TAG_BT,LogDefs.btQueryPairedDeviceSuc);
            for(int numAttempts = 1; numAttempts < 4; ++numAttempts)
            {
                Toast.makeText(this, "Trying to connect to BT attempt: "
                        + numAttempts + "/" + "3", Toast.LENGTH_LONG).show();
                BTconnect();
                if(_isBtConnected.get())
                {
                    //connected successfully
                    Log.d(TAG_BT, LogDefs.btConnectedSuccessfully);
                    return;
                }
            }

            Log.d(TAG_BT, LogDefs.btConnectedFailed);
            //Toast.makeText(this, "Failed to connect to BT , please exit and try again", Toast.LENGTH_LONG).show();
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            BtInitOfek();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Fail to connect to BT, would you like to try again?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();


        }
        else
        {
            Log.d(TAG_BT,LogDefs.btQueryPairedDeviceFail);
            Toast.makeText(this, "Fail to find paired device.\n " +
                    "Trying to discover device", Toast.LENGTH_LONG).show();

            discoverNewDevicePair();
        }
    }

    protected boolean queryPairedDevice(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            if (bt.getAddress().equals(DEVICE_ADDRESS)) {
                device = bt;
                return true;
            }
        }
        return false;
    }

    public void terminateBT()
    {
        if(socket == null ){
            Log.d(TAG_BT, "The Socket never opened before");
            return;
        }
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG_BT, "Could not close the client socket", e);
        }    }


    /***
     * check if bt lost connection mid game, try to reastblish
     */

    private final BroadcastReceiver btConnectionLostReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(getApplicationContext(), "lost connection to BT, trying to reconnect",Toast.LENGTH_LONG).show();
                        disableEnableButtons(false);
                        BtInitOfek();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    private void setBtMacAddress()
    {
        Toast.makeText(getApplicationContext(),"player number -- "+game.getPlayerID(),Toast.LENGTH_LONG).show();
        if (_playerId == 1) {
            //player 1
            DEVICE_ADDRESS = DEVICE_ADDRESS_P1;
        }
        else{
            //player 2
            //message.equals("Join")
            DEVICE_ADDRESS = DEVICE_ADDRESS_P2;
        }
    }

    //TODO OFEK - currently not working
    protected void discoverNewDevicePair(){
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        // To scan for remote Bluetooth devices
        if(mBluetoothAdapter.isDiscovering())
        {
            //already discovering stop it first
            mBluetoothAdapter.cancelDiscovery();
        }


        if (mBluetoothAdapter.startDiscovery()) {
            setProgressBarIndeterminateVisibility(true);
            setTitle("Scanning for arduino bt...");
            Toast.makeText(getApplicationContext(), "Discovering other bluetooth devices...",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG_BT,LogDefs.btDiscoveryDevicesStartedSuc);
        } else {
            Toast.makeText(getApplicationContext(), "Discovery failed to start.",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG_BT,LogDefs.btDiscoveryDevicesStartedFail);

        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Whenever a remote Bluetooth device is found
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                if(device.getAddress() == DEVICE_ADDRESS)
                {
                    //adapter.add(device.getName() + "\n" + device.getAddress());
                    setProgressBarIndeterminateVisibility(false);
                    Toast.makeText(getApplicationContext(), "Found arduino bt device",Toast.LENGTH_SHORT).show();
                    mBluetoothAdapter.cancelDiscovery();
                    BtInitAfterEnable();
                }
            }
            else if(mBluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                Toast.makeText(getApplicationContext(), "Failed to Discover arduino BT"
                        + "Make sure arduino is on and try again",Toast.LENGTH_LONG).show();
                setProgressBarIndeterminateVisibility(false);

                //TODO OFEK - add pop up box, once pressed ok, try again
                Log.e(TAG_BT, LogDefs.btFailedToDiscoverArduino);
            }
        }
    };



    /** ************************* Handle Arduino Bluetooth Commands ************************* **/
    private char findCommandServo(int angle, int strength) {
        return (char)('A' + ((int)(angle / 45) * 2) + (strength > 50 ? 1 : 0));
    }


    private char findCommandCar(int angle, int strength) {
        if (strength == 0)
        {
            return '!';
        }
        return (char)('Q' + ((int)(angle / 22.5) * 2) + (strength > 50 ? 1 : 0));
    }

    private void SendCommandShotLaser()
    {
        btnShot.setEnabled(false);
        try {
            outputStream.write('#');
        } catch (IOException e) {
            e.printStackTrace();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream.write('$');
                } catch (IOException e) {
                    e.printStackTrace();
                }
                btnShot.setEnabled(true);

            }
        }, laserShootLengthMS);
    }





    public void BTconnect()
    {
        try
        {
            mBluetoothAdapter.cancelDiscovery();
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //Creates a socket to handle the outgoing connection
            socket.connect();
            _isBtConnected.set(true);
            Toast.makeText(getApplicationContext(),
                    "Connection to bluetooth device successful", Toast.LENGTH_LONG).show();

        }
        catch(IOException e)
        {
            e.printStackTrace();
            _isBtConnected.set(false);
        }

        if(_isBtConnected.get())
        {
            try
            {
                outputStream = socket.getOutputStream(); //gets the output stream of the socket
                //TODO NOTE - if in the end we want input stream
                //inputStream = socket.getInputStream();
            }
            catch(IOException e)
            {
                //TODO OFEK handle error here?
                e.printStackTrace();
            }



            _isBtConnected.set(true);
            if(_isGameStarted.get())
            {
                //in case game already started we got here because we lost connection, so restore user control
                disableEnableButtons(true);
            }
            else
            {
                //game didn't start yet, check if bt was the only hold up
                checkIfPlayerReady();
            }
        }

    }


    public void changeScreen(Class screen){
        Intent intent = new Intent(GameScreenActivity.this, screen);
        startActivity(intent);
    }


    public void makeToast(){
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.toastlayout,null);
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL,0,0);
        toast.show();
    }

}


