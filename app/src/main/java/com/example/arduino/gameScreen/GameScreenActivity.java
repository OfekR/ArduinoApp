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
import com.example.arduino.defines.InGameConstants;
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
TODO : add the auth with the game for change the field in the database
 */

public class GameScreenActivity extends AppCompatActivity {
    private static final String TAG = LogDefs.tagGameScreen;

    private static final long START_TIME_IN_MILLIS = 600000;
    private ValueEventListener registration;
    private ValueEventListener gameStartRegistration;
    private ValueEventListener carInPlaceListener;
    private ValueEventListener gameEndListener;
    private DatabaseReference  gameEndDocRef;


    private DatabaseReference mDatabase;
    private Game game;
    private DatabaseReference gamedocRef;
    private DatabaseReference carInPlaceDocRef;

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
    private final String DEVICE_ADDRESS_P1 = "98:D3:51:FD:D9:45";
    //private final String DEVICE_ADDRESS_P1 = "98:D3:61:F5:E7:3C";
    private final String DEVICE_ADDRESS_P2 = "98:D3:61:F5:E7:3C";
    private static final String TAG_BT = LogDefs.tagBT;
    private static final int REQUEST_ENABLE_BT = 3; //just random value , 3 doesn't mean anything

    private String DEVICE_ADDRESS; //MAC Address of Bluetooth Module
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private BluetoothAdapter mBluetoothAdapter;

    private AtomicBoolean _isBtConnected;
    private AtomicBoolean _isInternetConnected;
    private AtomicBoolean _isCarInPlace;

    private Integer _playerId;
    private String _playerIdStr;

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
        //TODO Future - add pop up box which mark if each compenet was complete (meaning - listen to these 3 boolean
        //TODO              and start with X for all, when ever boolean set to true - switch to V
        //TODO          Or do thread which wake up every X seconds, and check what missing
        initBluetoothConnection();
        initCarInPlace();
        initInternetConnected();

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
        Integer endGameCode = (InGameConstants.winnerCauseReachPlayerForfeit * 10) + ((_playerId == 1) ? 2 : 1);
        mDatabase.child("LiveGameinfo").child("gameEnd").setValue(endGameCode);
    }

    private void updateGameStartedField(boolean isReady)
    {
        String key = "P" + _playerId.toString() + "_Ready";
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
        _isBtConnected =  new AtomicBoolean(false);
        _isCarInPlace = new AtomicBoolean(false);

        //check which player is it
        Bundle bundle = getIntent().getExtras();
        String message = bundle.getString("Classifier");
        _playerId = (message.equals("Init")) ? 1 : 2;
        _playerIdStr = (_playerId == 1 ? "Player1" : "Player2");

        _liveGameInfo = new LiveGameInfo(this, mDatabase, _playerId, btnShot, txtLife, txtAmmo, txtScore, txtKeys, txtMines, txtDefuse, txtSpeicalKey);
        _liveGameInfo.initLivePlayerInfoFromSettings();
        _liveGameInfo.resetEnemyInjuredFirebase();

        _rfidHandler = new RfidHandler(this, mDatabase ,_playerId,btnMine,btnKey,btnShot,joystickCar,joystickServo, _liveGameInfo);
        _rfidHandler.resetAllRfidValues();


        if (_playerId == 1)
        {
            //avoid both player update this
            resetAllGameLayoutVariables();
        }
    }

    /**
     * init all listeners
     */
    private void initButtonsListeners()
    {
        joystickCar.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                if(!_rfidHandler.isCarDisabled)
                {
                    try {
                        char c = findCommandCar(angle,strength);
                        System.out.println("The-Car-JoyStick angle -------> " + angle+ "and the Char i send is ---> " + c);
                        outputStream.write(c); //transmits the value of command to the bluetooth

                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("The-CarFail-JoyStick angle -------> " + angle+ "The- Strength--------->  "+strength);
                    }
                }

            }
        });

        // Listen to the shot button and update val
        joystickServo.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                if(!_rfidHandler.isCarDisabled) {
                    try {
                        if (strength != 0) {
                            char c = findCommandServo(angle, strength);
                            System.out.println("The-Servo-JoyStick angle -------> " + angle + "and the Char i send is ---> " + c);
                            outputStream.write(c); //transmits the value of command to the bluetooth
                        }
                        //else - strength 0, nothing to move


                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("The-ServoFail-JoyStick angle -------> " + angle + "The- Strength--------->  " + strength);
                    }
                }
            }
        });

        btnShot.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mySong !=  null) mySong.Destroy();
            mySong = new MediaPlayerWrapper(R.raw.lasershoot,getApplicationContext());
            mySong.StartOrResume();

            _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.AMMO,-1);
            _liveGameInfo.addSingleShotFired();
            SendCommandShotLaser();
        }
    });
        btnMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean planetSuccessfully = _rfidHandler.planetMineInCloud();
                if(planetSuccessfully) {
                    _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.MINES,-1);
                    _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.SCORE,InGameConstants.AddPointDueMinePlace);
                    _liveGameInfo.addMinePlanted();
                }
            }
        });

        btnKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean openSuccessfully = _rfidHandler.openBarrierInCloud();
                if(openSuccessfully) {
                    if(mySong !=  null) mySong.Destroy();
                    mySong = new MediaPlayerWrapper(R.raw.barrieropen,getApplicationContext());
                    mySong.StartOrResume();

                    _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.KEYS,-1);
                    _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.SCORE,InGameConstants.AddPointsDueBarrierOpen);

                    _liveGameInfo.addKeyUsed();
                }
            }
        });
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
     * does it by listening to db which contain boolean which indicate car in right place
     * boolean is updated by cloud function called by arduino which RFID tag
     */
    private void initCarInPlace()
    {
        //wait untill car in starting position
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        String firebaseLiveGameInfoPath = "RfidReading/" + _playerIdStr + "/";

        carInPlaceDocRef = database.getReference(firebaseLiveGameInfoPath + "Start");

        carInPlaceListener = carInPlaceDocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                assert(dataSnapshot.getKey().equals("Start"));
                Integer startValue = dataSnapshot.getValue(Integer.class);
                if(startValue == 1) {
                    //car is in starting position
                    _isCarInPlace.set(true);
                    checkIfPlayerReady();
                    carInPlaceDocRef.removeEventListener(this);
                }
                else {
                    //TODO Future - can do else statment and check if car was moved after being placed in starting position
                    Toast.makeText(getApplicationContext(), "Please place car in starting position", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
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
        _liveGameInfo.startListeners();
        listenToGameEnd();

        startTimer();
    }

    /**
     * called by one player, to put all firebase Barrier, Mines and lootbox on starting position
     *
     */
    private void resetAllGameLayoutVariables() {
        HashMap<String,Object> hashMap = new HashMap<>();

        final Integer numberOfMines = 6;
        final Integer numberOfBarriers = 8;
        final Integer numberOfLootbox = 6;


        for(Integer i=1; i<=numberOfMines; ++i) {
            String curName = "Mines/M" + i.toString() + "/status";
            String curName2 = "Mines/M" + i.toString() + "/board";

            //handle mine which are armed on start
            Integer value = (i==3 || i==4) ? 1 : 0;
            hashMap.put(curName, value);
            hashMap.put(curName2, 1);

        }

        for(Integer i=1; i<=numberOfBarriers; ++i) {
            String curName = "Barriers/B" + i.toString() + "/status";
            String curName2 = "Barriers/B" + i.toString() + "/board";

            hashMap.put(curName, 0);
            hashMap.put(curName2, 1);

        }

        for(Integer i=1; i<=numberOfLootbox; ++i) {
            String curName = "Lootbox/L" + i.toString() + "/status";
            String curName2 = "Lootbox/L" + i.toString() + "/board";

            hashMap.put(curName, 0);
            hashMap.put(curName2, 1);

        }

        mDatabase.updateChildren(hashMap);
    }

    /** ************************* In Game Settings ************************* **/

    private void listenToGameEnd()
    {
        //wait untill car in starting position
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        String firebaseLiveGameInfoPath = "LiveGameinfo/";

        gameEndDocRef = database.getReference(firebaseLiveGameInfoPath + "gameEnd");

        gameEndListener = gameEndDocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                assert(dataSnapshot.getKey().equals("gameEnd"));
                Integer finishCode = dataSnapshot.getValue(Integer.class);
                if(finishCode != 0) {
                    Integer winner_player_id = finishCode % 10;
                    Integer winner_cause = finishCode / 10;
                    finishGame(winner_player_id,winner_cause);
                    gameEndDocRef.removeEventListener(this);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    //finish the game for the player
    private void finishGame(Integer winner_player_id, Integer winner_cause) {
        _liveGameInfo.stopListeners();
        _rfidHandler.stopListeners();
        if(countDownTimer != null) countDownTimer.cancel();

        // winner_cause dic:
        // 3 - player reached end tag
        // 4 - player died
        // 5 - time limit reached
        // 6 - player forfeit
        resetValue();
        Log.v("GAME-CLASS", "GAME------------FINISHED");
        collectStatsAndPopFinalGameScreen(winner_player_id, winner_cause);
    }

    //TODO Generally - copy pasta of original game over, verify all fields are logged and maybe need to add idk
    //TODO - use winner cause more (like present it or log it) today only used little in toMap function
    private void collectStatsAndPopFinalGameScreen(Integer winner_player_id, Integer winner_cause) {
        FirebaseFirestore db =FirebaseFirestore.getInstance();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final boolean isWinner = winner_player_id.equals(_playerId);
        db.collection("Logs").document(uid).set(_liveGameInfo.toMap(isWinner,mTimeLeftInMils,uid, winner_cause));
        _liveGameInfo.readMyStats((int) (mTimeLeftInMils/1000),isWinner);
        if(isWinner){
            Intent intent = new Intent(getApplicationContext(), PopWindowWin.class);
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        }
        else {
            Intent intent = new Intent(getApplicationContext(), PopWindowGameOver.class); // Todo change for the right screen
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        }
    }


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


    //TODO - fix timer, it doesn't take the right time now
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
                    //TODO - test this ending
                    //indicate game is over, but only player 1 to avoid both player update it
                    if(_playerId == 1) {
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        database.getReference("LiveGameinfo").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Integer player1Points = dataSnapshot.child("Player1").child("score").getValue(Integer.class);
                                Integer player2Points = dataSnapshot.child("Player2").child("score").getValue(Integer.class);
                                Integer winner_player_id = (player1Points > player2Points) ? 1 : 2;
                                //TODO - add tie breaker (in case points are equal)
                                Integer endGameCode = winner_player_id + (InGameConstants.winnerCauseReachTimeLimit * 10);
                                mDatabase.child("LiveGameinfo").child("gameEnd").setValue(endGameCode);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                System.out.println("The read failed: " + databaseError.getCode());
                            }
                        });
                    }

                    //checkForWin();
                }
            }.start();
            mTimerRunning = true;
        }
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
        _rfidHandler.resetAllRfidValues();
        _liveGameInfo.resetEnemyInjuredFirebase();

        if (_playerId == 1)
        {
            //avoid both player update this
            resetAllGameLayoutVariables();
        }

       // httpHelper.HttpRequestForLooby("NO-ONE-IS-WAITING", "https://us-central1-arduino-a5968.cloudfunctions.net/addJoin");
        //httpHelper1.HttpRequestForLooby("GAME-NOT-READY","https://us-central1-arduino-a5968.cloudfunctions.net/setGameReady");

        //update GameStarted
        updateGameStartedField(false);
    }



    // used to toggle status of buttons, in order to disbale buttons once coneection to BT is lost or game hasn't started yet
    private void disableEnableButtons(boolean isEnable)
    {
        boolean isEnableShot = isEnable && _liveGameInfo.isShotBtnEnabled();
        btnShot.setEnabled(isEnableShot);
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

    //TODO - debug why this didn't work or just remove interent check
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
        }, InGameConstants.LaserHitBtnDelay * 1000);
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
                _rfidHandler.setOutputStream(outputStream);
                //NOTE - if in the end we want input stream
                //inputStream = socket.getInputStream();
            }
            catch(IOException e)
            {
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


