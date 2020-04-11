package com.example.arduino.menu;

import androidx.appcompat.app.AppCompatActivity;
import com.example.arduino.R;
import com.example.arduino.initGame.InitGameActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    Button startBuuton;
    MediaPlayer mysong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        startBuuton = (Button) findViewById(R.id.start_btn);
        startBuuton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openInitGame();
            }
        });
        mysong = MediaPlayer.create(getApplicationContext(), R.raw.songwar);
        mysong.start();
    }

    public void openInitGame(){
        Intent intent = new Intent(this, InitGameActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mysong.release();
        finish();
    }
}
