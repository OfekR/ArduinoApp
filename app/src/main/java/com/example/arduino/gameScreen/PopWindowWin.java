package com.example.arduino.gameScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import com.example.arduino.R;
import com.example.arduino.menu.MenuActivity;
import com.example.arduino.utilities.MediaPlayerWrapper;

public class PopWindowWin extends AppCompatActivity {
    private Button bt;
    private MediaPlayerWrapper mySong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mySong = new MediaPlayerWrapper(R.raw.claps,getApplicationContext());
        mySong.StartOrResume();
        bt = (Button) findViewById(R.id.btwin);
        setContentView(R.layout.activity_pop_window_win);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(MenuActivity.class);
            }
        });
    }
    public void initManger(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.8),(int)(height*.7));
    }
    public void changeScreen(Class screen){
        Intent intent = new Intent(this, screen);
        mySong.Pause();
        mySong.Destroy();
        startActivity(intent);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mySong.Pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mySong.Destroy();
    }
}
