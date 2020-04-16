package com.example.arduino.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.arduino.R;
import com.example.arduino.defines.LogDefs;
import com.example.arduino.initGame.InitGameActivity;
import com.example.arduino.loby.LobyActivity;
import com.example.arduino.stats.StatitacsActivity;
import com.example.arduino.ui.login.LoginActivity;
import com.example.arduino.utilities.HttpHelper;
import com.example.arduino.utilities.MediaPlayerWrapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity {
    private static final String TAG = LogDefs.tagMenu;


    Button joinBtn;
    Button startButton;
    Button statBtn;
    Button logOut;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        startButton = findViewById(R.id.start_btn);
        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                changeScreen(InitGameActivity.class);
            }
        });

        logOut = findViewById(R.id.logout_btn);
        logOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                SignOut();
            }
        });
        joinBtn = findViewById(R.id.join_btn);
        joinBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                HttpHelper httpHelper = new HttpHelper();
                //TODO : check no join ready
                httpHelper.HttpRequestForLooby("WAITING", "https://us-central1-arduino-a5968.cloudfunctions.net/addJoin");
                changeScreen(LobyActivity.class);
                }
        });

        statBtn = findViewById(R.id.Stat_btn);
        statBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                changeScreen(StatitacsActivity.class);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
            mAuthStateListener = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setupFirebaseListener();
    }

    @Override
    public void onResume() {
        super.onResume();
    }



    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int optionChoosen) {
                switch (optionChoosen){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        SignOut();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked - nothing to do
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to sign out?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    /*  Private Methods */
    private void changeScreen(Class screen){
        Intent intent = new Intent(this, screen);
        startActivity(intent);
    }

    private void SignOut() {
        Log.d(TAG,LogDefs.signoutMsg);
        //no need to listen anymore, because user signed out
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        mAuthStateListener = null;

        //sign out
        FirebaseAuth.getInstance().signOut();

        Toast.makeText(MenuActivity.this, "User signed out",
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        startActivity(intent);
    }

    //set listener to follow if user is logged in or not
    private void setupFirebaseListener(){
        if(mAuthStateListener != null) {
            //already initialized
            return;
        }
        Log.d(TAG, LogDefs.setupFirebaseListener);

        mAuthStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG,LogDefs.userSignedInIs + user.getUid());
                }
                else {
                    SignOut();

                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }



}
