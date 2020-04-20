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

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.HashMap;
import java.util.Map;

public class LobyActivity extends AppCompatActivity {
    private MediaPlayer mMediaPlayer;
    int mCurrentVideoPosition;
    private VideoView videoBG;
    private  Button btBack;
    private MediaPlayerWrapper mySong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loby);
        btBack = (Button) findViewById(R.id.Lobybutton);
        btBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                HttpHelper httpHelper = new HttpHelper();
                httpHelper.HttpRequestForLooby("NO-ONE-IS-WAITING", "https://us-central1-arduino-a5968.cloudfunctions.net/addJoin");
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(intent);
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
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                String valid_join = documentSnapshot.getString("waitTojoin");
                final String valid_game = documentSnapshot.getString("gameReady");
                if(valid_game.equals("GAME-READY") && valid_join.equals("WAITING")){
                    Log.v("LOBY-CLASS","Game-is strating" );
                    Intent intent = new Intent(getApplicationContext(), PopWindow.class);
                    startActivity(intent);
                }
            }
        });
    }




    @Override
    protected void onPause() {
        super.onPause();
        mySong.Pause();
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