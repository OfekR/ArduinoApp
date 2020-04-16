package com.example.arduino.loby;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.VideoView;

import com.example.arduino.R;
import com.example.arduino.menu.MenuActivity;

public class PopWindow extends AppCompatActivity {
    private VideoView videoBG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_window);
        initiliazeVideo();
        initManger();
        // video finish listener
        videoBG.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent intent = new Intent(getApplicationContext(),com.example.arduino.gameScreen.GameScreenActivity.class);
                startActivity(intent);
            }
        });

    }
    public void initiliazeVideo(){
        videoBG = (VideoView) findViewById(R.id.PopUpvideoView);
        String path = "android.resource://"+getPackageName()+"/"+R.raw.countdown;
        Uri uri = Uri.parse(path);
        videoBG.setVideoURI(uri);
        videoBG.start();
    }
    public void initManger(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.8),(int)(height*.6));
    }
}
