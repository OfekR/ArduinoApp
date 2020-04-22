package com.example.arduino.gameScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import com.example.arduino.R;
import com.example.arduino.menu.MenuActivity;
import com.example.arduino.utilities.HttpHelper;
import com.example.arduino.utilities.MediaPlayerWrapper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PopWindowGameOver extends AppCompatActivity {
    private Button bt;
    private MediaPlayerWrapper mySong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_window_game_over);
        mySong = new MediaPlayerWrapper(R.raw.evil,getApplicationContext());
        mySong.StartOrResume();
        bt = (Button) findViewById(R.id.btgameover);
        initManger();
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pushData();
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
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
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
    private void pushData() {
        Bundle data = getIntent().getExtras();
        assert data != null;
        DocumentMover documentMover = (DocumentMover) data.getParcelable("DocumentPusher");
        assert documentMover != null;
        HttpHelper httpHelper = new HttpHelper();
        String url = "https://us-central1-arduino-a5968.cloudfunctions.net/lstUpdate";
        String arrg = "?id="+documentMover.getId()+"&time="+documentMover.get_bestTime()+"&play="+documentMover.get_gamesPlayed()+"&lost="+documentMover.get_gamesLost()+
                "&pre="+documentMover.get_hitsPercentage()+"&won="+documentMover.get_gamesWon()+"&mbomb="+documentMover.get_mostBombHits()+"&mlaser="+documentMover.get_mostLaserHits()+
                "&tbomb="+documentMover.get_totalBombHits()+"&thits="+documentMover.get_totalHits()+"&points="+documentMover.get_totalPoints()+"&shots="+documentMover.get_totalShots();
        url= url+arrg;
        httpHelper.HttpRequest(url);
     /*
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = documentMover.getId();
        Map<String, Object> docData = new HashMap<>();
        docData.put("bestTime", documentMover.get_bestTime());
        docData.put("gamesLost", documentMover.get_gamesLost());
        docData.put("gamesPlayed", documentMover.get_gamesPlayed());
        docData.put("gamesWon", documentMover.get_gamesWon());
        docData.put("hitsPercentage", documentMover.get_hitsPercentage());
        docData.put("mostBombHits", documentMover.get_mostBombHits());
        docData.put("mostLaserHits", documentMover.get_mostLaserHits());
        docData.put("totalBombHits", documentMover.get_totalBombHits());
        docData.put("totalHits", documentMover.get_totalHits());
        docData.put("totalPoints", documentMover.get_totalPoints());
        docData.put("totalShots", documentMover.get_totalShots());
        db.collection("PlayerStats").document(uid).set(docData);

      */
    }

}

