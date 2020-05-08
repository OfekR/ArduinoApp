package com.example.arduino.loby;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.arduino.R;
import com.example.arduino.gameScreen.GameScreenActivity;
import com.example.arduino.initGame.GameSetting;
import com.example.arduino.initGame.Member;
import com.example.arduino.menu.MenuActivity;
import com.example.arduino.utilities.HttpHelper;
import com.example.arduino.utilities.MediaPlayerWrapper;
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
    private DatabaseReference gamedocRef;
    private int flag =0;
    GameSetting gameSetting;
    private ListenerRegistration registration1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loby);
        btBack = (Button) findViewById(R.id.Lobybutton);
        // when pressed the back button , check who pressed and update firebase accrodingly
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
        //wait untill all players joined
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
                    //Both players are in - Start game
                    System.out.println("Inside::::The ---- valid Game is " + valid_game + " The ---- valid Join is " +valid_join );
                    Log.v("LOBY-CLASS","Game-is strating" );
                    // Retrive game setting from firebase
                    listnerForread();
                }
            }
        });


    }

    private void listnerForread() {
        registration.remove();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("GameSettings/");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gameSetting = dataSnapshot.getValue(GameSetting.class);
                ChangeScreenToGameLayout();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        };
        ref.addListenerForSingleValueEvent(valueEventListener);
    }


    private void ChangeScreenToGameLayout() {
        Intent intentGame = new Intent(getApplicationContext(), GameScreenActivity.class);
        //TODO - convert to gamesetting
        Member member = new Member(gameSetting);
        intentGame.putExtra("MyMember", member);
        changescreen(intentGame);
    }

    private void changescreen(Intent intent) {
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