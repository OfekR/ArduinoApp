package com.example.arduino.menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.arduino.R;
import com.example.arduino.defines.LogDefs;
import com.example.arduino.initGame.InitGameActivity;
import com.example.arduino.loby.LobyActivity;
import com.example.arduino.loby.PopWindow;
import com.example.arduino.stats.StatitacsActivity;
import com.example.arduino.ui.login.LoginActivity;
import com.example.arduino.utilities.HttpHelper;
import com.example.arduino.utilities.MediaPlayerWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Objects;

public class MenuActivity extends AppCompatActivity {
    private static final String TAG = LogDefs.tagMenu;


    Button joinBtn;
    Button startButton;
    Button statBtn;
    Button logOut;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        mDatabase = FirebaseDatabase.getInstance().getReference();
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
                checkforJoin();
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

    private void checkforJoin() {
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        DocumentReference docRef = fstore.collection("Appending").document("Append1");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot= task.getResult();
                    String valid_join = snapshot.getString("waitTojoin");
                    // antoher player is already wating
                    if (valid_join.equals("WAITING")) {
                        Log.v("MENU-CLASS", "SOMEONE - IS ALREADY WAITS");
                        Toast.makeText(MenuActivity.this, "SOMEONE - IS ALREADY WAITS",
                                Toast.LENGTH_SHORT).show();


                    }
                    // you can join game
                    else if (valid_join.equals("NO-ONE-IS-WAITING")) {
                        HttpHelper httpHelper = new HttpHelper();
                        httpHelper.HttpRequestForLooby("WAITING", "https://us-central1-arduino-a5968.cloudfunctions.net/addJoin");
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("playerId2",FirebaseAuth.getInstance().getUid());
                        mDatabase.child("GameSettings").updateChildren(hashMap);
                        Intent intent = new Intent(getApplicationContext(),LobyActivity.class);
                        intent.putExtra("Classifier", "Join");
                        startActivity(intent);
                    }
                    // check for athoer senrio if we want 3 state
                    else {

                    }
                }
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

    public void changeScreen(Class screen){
        Intent intent = new Intent(this, screen);
        startActivity(intent);
    }

}
