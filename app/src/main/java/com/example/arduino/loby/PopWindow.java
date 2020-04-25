package com.example.arduino.loby;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.VideoView;

import com.example.arduino.R;
import com.example.arduino.gameScreen.GameScreenActivity;
import com.example.arduino.initGame.Member;
import com.example.arduino.menu.MenuActivity;
import com.example.arduino.utilities.HttpHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

public class PopWindow extends AppCompatActivity {
    private VideoView videoBG;
    private ListenerRegistration registration;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_window);
        initiliazeVideo();
        initManger();
        // video finish listener
        videoBG.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                final DocumentReference docReff = FirebaseFirestore.getInstance().collection("GameSettings").document("doucment1");
                 registration = docReff.addSnapshotListener(new EventListener<DocumentSnapshot>() {

                    @Override
                    public void onEvent(@Nullable DocumentSnapshot Snapshot, @Nullable FirebaseFirestoreException e) {
                        assert Snapshot != null;
                        Member meb = new Member();
                        String valid_time = Snapshot.getString("Duration");
                        String valid_shot = Snapshot.getString("Shots");
                        String valid_type = Snapshot.getString("Type");
                        String valid_keys = Snapshot.getString("Keys");
                        String valid_mines = Snapshot.getString("Mines");
                        String valid_player1 = Snapshot.getString("playerId1");
                        String valid_player2 = Snapshot.getString("playerId2");

                        if (valid_time.equals("null") || valid_time.equals("")) {  // defulat value
                            meb.setTime("10");
                        } else {
                            meb.setTime(valid_time);
                        }
                        if (valid_shot.equals("null") || valid_shot.equals("")) { // defulat value
                            meb.setNumberShot("30");
                        } else {
                            meb.setNumberShot(valid_shot);
                        }
                        if (valid_type.equals("null") || valid_type.equals("")) {  // defulat value
                            meb.setGameType("1");
                        } else {
                            meb.setGameType(valid_type);
                        }
                        if (valid_keys.equals("null") || valid_keys.equals("")) { // defulat value
                            meb.setKeys("0");
                        } else {
                            meb.setKeys(valid_keys);
                        }
                        if (valid_mines.equals("null") || valid_mines.equals("")) { // defulat value
                            meb.setMines("0");
                        } else {
                            meb.setMines(valid_mines);
                        }

                        meb.setPlayer1(valid_player1);
                        meb.setPlayer2(valid_player2);
                        Intent intentGame = new Intent(getApplicationContext(), GameScreenActivity.class);
                        Log.d("MY-TAG----->","the parma of :"+"shot: "+ valid_shot+"time:  "+valid_time);
                        intentGame.putExtra("MyMember", meb);
                        startActivity(intentGame.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
                    }
                });

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
    @Override
    public void onPause() {
        super.onPause();
        // Remove post value event listener
        if (registration != null) {
            registration.remove();
        }
    }
}
