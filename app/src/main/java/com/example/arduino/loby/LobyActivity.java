package com.example.arduino.loby;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.arduino.R;
import com.example.arduino.gameScreen.GameScreenActivity;
import com.example.arduino.initGame.Member;
import com.example.arduino.menu.MenuActivity;
import com.example.arduino.menu.PlayerId;
import com.example.arduino.utilities.HttpHelper;
import com.example.arduino.utilities.MediaPlayerWrapper;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class LobyActivity extends AppCompatActivity {
    private MediaPlayer mMediaPlayer;
    int mCurrentVideoPosition;
    private VideoView videoBG;
    ListenerRegistration registration;
    private  Button btBack;
    private MediaPlayerWrapper mySong;
    private int flag =0;
    private ListenerRegistration registration1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loby);
        btBack = (Button) findViewById(R.id.Lobybutton);
        btBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                Bundle bundle = getIntent().getExtras();
                String message = bundle.getString("Classifier");
                if (message.equals("Init")) {
                    HttpHelper httpHelper = new HttpHelper();
                    httpHelper.HttpRequestForLooby("GAME-NOT-READY", "https://us-central1-arduino-a5968.cloudfunctions.net/setGameReady");
                }
                else{
                    HttpHelper httpHelper = new HttpHelper();
                    httpHelper.HttpRequestForLooby("NO-ONE-IS-WAITING", "https://us-central1-arduino-a5968.cloudfunctions.net/addJoin");
                }
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
            }
        });
        initiliazeVideo();
        videoBG.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer =mp;
                mMediaPlayer.setLooping(true);
                if(mCurrentVideoPosition !=0){
                    mMediaPlayer.seekTo(mCurrentVideoPosition);
                    mMediaPlayer.start();
                }
            }
        });
        waitForStartingGame();

    }

    public void initiliazeVideo(){
        videoBG = (VideoView) findViewById(R.id.videoView);
        String path = "android.resource://"+getPackageName()+"/"+R.raw.hd1384;
        Uri uri = Uri.parse(path);
        videoBG.setVideoURI(uri);
        videoBG.start();
    }

    private void waitForStartingGame() {
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        final DocumentReference documentReference = fstore.collection("Appending").document("Append1");
        registration =documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                String valid_join = documentSnapshot.getString("waitTojoin");
                String valid_game = documentSnapshot.getString("gameReady");
                System.out.println("The ---- valid Game is " + valid_game + " The ---- valid Join is " +valid_join );
                assert valid_join != null;
                assert valid_game != null;
                if((valid_game.equals("GAME-READY") && valid_join.equals("WAITING"))){
                    System.out.println("Inside::::The ---- valid Game is " + valid_game + " The ---- valid Join is " +valid_join );
                    Log.v("LOBY-CLASS","Game-is strating" );
                    resetchange();
                }
            }
        });


    }

    private void resetchange() {
        registration.remove();
        final DocumentReference docReff = FirebaseFirestore.getInstance().collection("GameSettings").document("doucment1");
        registration1 = docReff.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot Snapshot, @Nullable FirebaseFirestoreException e) {
                assert Snapshot != null;
                Member meb = new Member();
                String valid_time = Snapshot.getString("Duration");
                String valid_shot = Snapshot.getString("Shots");
                String valid_type = Snapshot.getString("Type");
                String valid_keys = Snapshot.getString("Keys");
                String valid_mines = Snapshot.getString("Mines");
                String valid_player1 = Snapshot.getString("playerId1");
                String valid_player2 = Snapshot.getString("playerId2");

                if (valid_time.equals("null") || valid_time.equals("")) {  // defulat value
                    meb.setTime("10");
                } else {
                    meb.setTime(valid_time);
                }
                if (valid_shot.equals("null") || valid_shot.equals("")) { // defulat value
                    meb.setNumberShot("30");
                } else {
                    meb.setNumberShot(valid_shot);
                }
                if (valid_type.equals("null") || valid_type.equals("")) {  // defulat value
                    meb.setGameType("1");
                } else {
                    meb.setGameType(valid_type);
                }
                if (valid_keys.equals("null") || valid_keys.equals("")) { // defulat value
                    meb.setKeys("0");
                } else {
                    meb.setKeys(valid_keys);
                }
                if (valid_mines.equals("null") || valid_mines.equals("")) { // defulat value
                    meb.setMines("0");
                } else {
                    meb.setMines(valid_mines);
                }

                meb.setPlayer1(valid_player1);
                meb.setPlayer2(valid_player2);
                Intent intentGame = new Intent(getApplicationContext(), GameScreenActivity.class);
                intentGame.putExtra("MyMember", meb);
                Log.d("MY-TAG----->","the parma of :"+"shot: "+ valid_shot+"time:  "+valid_time);
                changescreen(intentGame);

            }
        });
    }

    private void changescreen(Intent intent) {
        registration1.remove();
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mySong.Pause();
        if(registration != null){
            registration.remove();
        }
        if(mMediaPlayer != null) {
            mCurrentVideoPosition = mMediaPlayer.getCurrentPosition();
        }
        videoBG.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mySong.Destroy();
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if(mySong == null) {
            mySong = new MediaPlayerWrapper(R.raw.songwar,getApplicationContext());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mySong.StartOrResume();
        if(videoBG != null ) {
            videoBG.start();
        }
    }

}