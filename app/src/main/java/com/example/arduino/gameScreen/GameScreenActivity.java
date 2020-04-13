package com.example.arduino.gameScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.arduino.R;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class GameScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // do whatever you want
            }
        });
    }
}
