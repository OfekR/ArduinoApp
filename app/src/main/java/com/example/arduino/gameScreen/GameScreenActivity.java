package com.example.arduino.gameScreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arduino.R;
import com.example.arduino.initGame.Member;
import com.example.arduino.loby.PopWindow;
import com.example.arduino.utilities.HttpHelper;
import com.example.arduino.utilities.MediaPlayerWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;



/*
TODO : we need to reorganize the class.
TODO : add the auth with the game for change the field in the database
 */

public class GameScreenActivity extends AppCompatActivity {
    private static final long START_TIME_IN_MILLIS = 600000 ;
    private ValueEventListener registration;
    private DatabaseReference  mDatabase;
    private Game game;
    private DatabaseReference gamedocRef;
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
    private final String DEVICE_ADDRESS = "98:D3:51:FD:D9:45"; //MAC Address of Bluetooth Module
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private String command ="";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_game_screen);
        // Register bluetooth receiver
        findAllView();
        initBluetoothConnection();
        getDataFromSetting();
        JoystickView joystick = (JoystickView) findViewById(R.id.joystickViewCarControl);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                try {
                    char c = findCommand(angle,strength);
                    System.out.println("The-JoyStick angle -------> " + angle+ "and the Char i send is ---> " + c);
                    outputStream.write(c); //transmits the value of command to the bluetooth

                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("The-JoyStick angle -------> " + angle+ "The- Strength--------->  "+strength);
            }
        });
        // Listen to the shot button and update val
        JoystickView joystickServo = (JoystickView) findViewById(R.id.joystickViewTurretControl);
        joystickServo.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                try {
                    char c = findCommandServo(angle,strength);
                    System.out.println("The-JoyStick angle -------> " + angle+ "and the Char i send is ---> " + c);
                    outputStream.write(c); //transmits the value of command to the bluetooth

                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("The-JoyStick angle -------> " + angle+ "The- Strength--------->  "+strength);
            }
        });

        btnShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer num =game.getAmmuo() -1;
                game.raiseBy1TotalData("Shots");
                if(num > 0 ){
                    if(mySong !=  null) mySong.Destroy();
                    mySong = new MediaPlayerWrapper(R.raw.goodgunshot,getApplicationContext());
                    mySong.StartOrResume();
                    game.setAmmuo(num);
                    txtAmmo.setText("Ammuo: - " + (game.getAmmuo().toString()));
                    //hit sound
                   // checkForHit();
                }
                else{
                    txtAmmo.setText("NO- Ammuo - !!!!!!!!: - ");
                }
            }
        });

        startTimer();
        ListnerForChangeInGame();
    }

    private void initBluetoothConnection() {
        // TODO: 1) block activity to allow safe connection (require few seconds to establish connection)
        // TODO 2) retry to connect BT

        if(BTinit()){
            if(BTconnect()){
              //  changeScreen(PopWindow.class);
            }
            else{
                Toast.makeText(getApplicationContext(), "No-BlueTooth-Connection-Please restart", Toast.LENGTH_SHORT).show();

            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Please pair the device first", Toast.LENGTH_SHORT).show();

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
            hit = Integer.parseInt(game.getValuesShared().getLife1()) -1;
        }
        // player 2
        else{
            hit = Integer.parseInt(game.getValuesShared().getLife2())  -1;
        }
        if(mySong !=  null) mySong.Destroy();
        mySong = new MediaPlayerWrapper(R.raw.boom,getApplicationContext());
        mySong.StartOrResume();
        mDatabase.child("Game").child("life"+game.getPlayerID()).setValue(hit.toString());
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

    // All game manegement
    private void ListnerForChangeInGame(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        gamedocRef = database.getReference("Game/");

        registration = gamedocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer my_life_left,opp_flag,my_flag,opp_life_left,my_hit,opp_hit,opp_points;
                ValuesShared values = dataSnapshot.getValue(ValuesShared.class);
                game.setValuesShared(values);
                assert values != null;
                if(game.getPlayerID().equals("1")){
                    my_life_left = Integer.parseInt(values.getLife1());
                    my_flag = Integer.parseInt(values.getFlag1());
                    opp_flag = Integer.parseInt(values.getFlag2());
                    opp_life_left = Integer.parseInt(values.getLife2());
                    my_hit = Integer.parseInt(values.getValid1());
                    opp_hit = Integer.parseInt(values.getValid2());
                    opp_points =Integer.parseInt(values.getPoints2());
                }
                else{
                     my_life_left = Integer.parseInt(values.getLife2());
                     my_flag = Integer.parseInt(values.getFlag2());
                     opp_flag = Integer.parseInt(values.getFlag1());
                     opp_life_left = Integer.parseInt(values.getLife1());
                     opp_hit = Integer.parseInt(values.getValid1());
                     my_hit = Integer.parseInt(values.getValid2());
                    opp_points =Integer.parseInt(values.getPoints1());
                }
                game.setFlag(my_flag);
                game.setLife(my_life_left);

                //update number hit
                assert(my_hit >= game.getNum_hits());
                if(my_hit > game.getNum_hits()){
                    game.setNum_hits(game.getNum_hits()+1);
                    gatHit();
                    //TODO - add field to Game firbase field which indicate hit, and change the point calc to oppenet side
                    opp_points = opp_points+10;
                    mDatabase.child("Game").child("points"+game.getOppID()).setValue(opp_points.toString());
                }
                if(my_life_left == 0 || opp_life_left == 0 ||  my_flag ==  1 || opp_flag == 1 ) {
                    checkForWin();
                }
                pbLife.setProgress(my_life_left);
                txtLife.setText("LIFE: - " +my_life_left);
                txtScore.setText(("SCORE- ")+game.getPoint().toString());

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
            my_life_left = Integer.parseInt(values.getLife1());
            my_flag = Integer.parseInt(values.getFlag1());
            opp_flag = Integer.parseInt(values.getFlag2());
            opp_life_left = Integer.parseInt(values.getLife2());
            opp_points = Integer.parseInt(values.getPoints2());
            my_points = Integer.parseInt(values.getPoints1());
        }
        else{
            my_life_left = Integer.parseInt(values.getLife2());
            my_flag = Integer.parseInt(values.getFlag2());
            opp_flag = Integer.parseInt(values.getFlag1());
            opp_life_left = Integer.parseInt(values.getLife1());
            opp_points = Integer.parseInt(values.getPoints1());
            my_points = Integer.parseInt(values.getPoints2());
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
        try {
            socket.close();
        }
        catch (Exception e){}
        HttpHelper httpHelper = new HttpHelper();
        HttpHelper httpHelper1 = new HttpHelper();
        ValuesShared vs = new ValuesShared();
        mDatabase.child("Game").setValue(vs.resetObject());
        httpHelper.HttpRequestForLooby("NO-ONE-IS-WAITING", "https://us-central1-arduino-a5968.cloudfunctions.net/addJoin");
        httpHelper1.HttpRequestForLooby("GAME-NOT-READY","https://us-central1-arduino-a5968.cloudfunctions.net/setGameReady");

    }


    //Initializes bluetooth module
    public boolean BTinit()
    {
        boolean found = false;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null) //Checks if the device supports bluetooth
        {
            Toast.makeText(getApplicationContext(), "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
        }

        if(bluetoothAdapter.isEnabled()) //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
        {

            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

            if (bondedDevices.isEmpty()) //Checks for paired bluetooth devices
            {
                Toast.makeText(getApplicationContext(), "Please pair the device first", Toast.LENGTH_SHORT).show();
            }
            else {
                for (BluetoothDevice iterator : bondedDevices) {
                    Toast.makeText(this,"The paired mac --  is "+ iterator.getAddress(),Toast.LENGTH_LONG).show();

                    if (iterator.getAddress().equals(DEVICE_ADDRESS)) {
                        device = iterator;
                        Toast.makeText(this,"The paired mac --  is "+ device.getAddress(),Toast.LENGTH_LONG).show();
                        found = true;
                        break;
                    }
                }
            }
        }

        return found;
    }

    public boolean BTconnect()
    {
        boolean connected = true;

        try
        {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //Creates a socket to handle the outgoing connection
            socket.connect();

            Toast.makeText(getApplicationContext(),
                    "Connection to bluetooth device successful", Toast.LENGTH_LONG).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            connected = false;
        }

        if(connected)
        {
            try
            {
                //TODO
                outputStream = socket.getOutputStream(); //gets the output stream of the socket
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return connected;
    }
    private char findCommand(int angle, int strength) {
        if((angle >= 0 && angle < 10) || (angle >= 350 && angle <= 360)){
            return '1';
        }
        else if((angle >= 10) && (angle < 30)){
            return '2';
        }
        else if((angle >= 30) && (angle < 60)){
            return '3';

        }
        else if((angle >= 60) && (angle < 80)){
            return '4';

        }
        else if((angle >= 80) && (angle < 100)){
            return '5';

        }
        else if((angle >= 100) && (angle < 130)){
            return '6';

        }
        else if((angle >= 130) && (angle < 150)){
            return '7';

        }
        else if((angle >= 150) && (angle < 170)){
            return '8';

        }
        else if((angle >= 170) && (angle < 190)){
            return '9';

        }
        else if((angle >= 190) && (angle < 210)){
            return '0';

        }
        else if((angle >= 210) && (angle < 230)){
            return 'a';

        }
        else if((angle >= 230) && (angle < 250)){
            return 'b';

        }
        else if((angle >= 250) && (angle < 270)){
            return 'c';

        }
        else if((angle >= 270) && (angle < 290)){
            return 'd';

        }
        else if((angle >= 290) && (angle < 310)){
            return 'e';

        }
        else if((angle >= 310) && (angle < 330)){
            return 'f';

        }
        else if((angle >= 330) && (angle < 350)){
            return 'g';

        }
        else{
            return 'h';
        }

    }

    //TODO - change to real values, consider using strength etc..
    private char findCommandServo(int angle, int strength) {
        if((angle > 0 && angle < 10) || (angle >= 350 && angle <= 360)){
            return 'A';
        }
        else if((angle >= 10) && (angle < 30)){
            return 'B';
        }
        else if((angle >= 30) && (angle < 60)){
            return 'C';

        }
        else if((angle >= 60) && (angle < 80)){
            return 'D';

        }
        else if((angle >= 80) && (angle < 100)){
            return 'E';

        }
        else if((angle >= 100) && (angle < 130)){
            return 'F';

        }
        else if((angle >= 130) && (angle < 170)){
            return 'G';

        }
        else if((angle >= 170) && (angle < 190)){
            return 'H';

        }
        else if((angle >= 190) && (angle < 220)){
            return 'I';

        }
        else if((angle >= 220) && (angle < 260)){
            return 'J';

        }
        else if((angle >= 260) && (angle < 280)){
            return 'K';

        }
        else if((angle >= 280) && (angle < 320)){
            return 'L';

        }
        else if((angle >= 320) && (angle < 350)){
            return 'M';

        }
        else{
            return 'N';
        }

    }


    public void changeScreen(Class screen){
        Intent intent = new Intent(GameScreenActivity.this, screen);
        startActivity(intent);
    }

}


