package com.example.arduino.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.arduino.R;
import com.example.arduino.initGame.InitGameActivity;
import com.example.arduino.stats.StatitacsActivity;
import com.example.arduino.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity {
    Button joinBtn;
    Button startBuuton;
    MediaPlayer mysong;
    Button statBtn;
    Button logOut;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        mysong = MediaPlayer.create(getApplicationContext(), R.raw.songwar);
        mysong.start();
        startBuuton = (Button) findViewById(R.id.start_btn);
        startBuuton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                changeScreen(InitGameActivity.class);
            }
        });
        setupFirebaseListener();
        logOut = (Button) findViewById(R.id.logout_btn);
        logOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                Log.d("MenuActicity","onClick: signing out user");
                FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
                FirebaseAuth.getInstance().signOut();
            }
        });
        joinBtn = (Button) findViewById(R.id.join_btn);
        joinBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){

            }
        });

        statBtn = (Button) findViewById(R.id.Stat_btn);
        statBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                changeScreen(StatitacsActivity.class);
            }
        });

    }

    private void setupFirebaseListener(){
        Log.d("MenuActicity", "setuPfirebaseListner: setting up the auth state listner");
        mAuthStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d("MenuActicity","onAuthStateChanged: signing in: " + user.getUid());
                }else{
                    Log.d("MenuActicity","onAuthStateChanged: signing out");
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    startActivity(intent);
                }
            }
        };
    }

    public void changeScreen(Class screen){
        Intent intent = new Intent(this, screen);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mysong.release();
        if(mAuthStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        }
        finish();
    }
}
