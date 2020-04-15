package com.example.arduino.loby;

import androidx.appcompat.app.AppCompatActivity;
import com.example.arduino.R;
import com.example.arduino.gameScreen.GameScreenActivity;
import com.example.arduino.utilities.MediaPlayerWrapper;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
    private MediaPlayerWrapper mySong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loby);
        videoBG = (VideoView) findViewById(R.id.videoView);
        String path = "android.resource://"+getPackageName()+"/"+R.raw.hd1384;
        Uri uri = Uri.parse(path);
        videoBG.setVideoURI(uri);
        videoBG.start();
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


    }

    @Override
    protected void onPause() {
        super.onPause();
        mySong.Pause();
        mCurrentVideoPosition = mMediaPlayer.getCurrentPosition();
        videoBG.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mySong.Destroy();
        mMediaPlayer.release();
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
        videoBG.start();
    }


}