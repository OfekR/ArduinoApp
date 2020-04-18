package com.example.arduino.stats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.arduino.R;
import com.example.arduino.gameScreen.GameScreenActivity;
import com.example.arduino.menu.MenuActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;

public class StatitacsActivity extends AppCompatActivity {
    private Button sendDt;
    long gamesPlayed, gamesWon, gamesLost, totalPoints, bestTime,
    mostLaserHits, mostBombHits, totalBombHits, totalShots, totalHits, hitPercentage;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();

    ListView listView;
    ArrayList<HashMap<String,String>> list;
    public static final String FIRST_COLUMN="First";
    public static final String SECOND_COLUMN="Second";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statitacs);

        sendDt= (Button) findViewById(R.id.StatsMenu);
        sendDt.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                changeScreen(MenuActivity.class);
            }
        });

        listView = (ListView)findViewById(R.id.listView1);

        //PlayerStats.writeStats(userId, new PlayerStats(1,2,3,4,5,6,7,8,10,9));
        DocumentReference documentReference = db.collection("PlayerStats").document(userId);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //TODO - handle warnings?
                gamesPlayed = task.getResult().getLong("gamesPlayed");
                gamesWon = task.getResult().getLong("gamesWon");
                gamesLost = task.getResult().getLong("gamesLost");
                totalPoints = task.getResult().getLong("totalPoints");
                bestTime = task.getResult().getLong("bestTime");
                mostLaserHits = task.getResult().getLong("mostLaserHits");
                mostBombHits = task.getResult().getLong("mostBombHits");
                totalBombHits = task.getResult().getLong("totalBombHits");
                totalShots = task.getResult().getLong("totalShots");
                totalHits = task.getResult().getLong("totalHits");
                hitPercentage = task.getResult().getLong("hitPercentage");
                PlayerStats stats = new PlayerStats(gamesPlayed,gamesWon,gamesLost,totalPoints,bestTime,
                        mostLaserHits,mostBombHits,totalBombHits,totalShots,totalHits);
                populateList(stats);
                adapt();
            }
        });



    }

    public void changeScreen(Class screen){
        Intent intent = new Intent(this, screen);
        startActivity(intent);
    }

    private void adapt(){
        ListViewAdapter adapter = new ListViewAdapter(this, list);
        listView.setAdapter(adapter);

    }

    private void populateList(PlayerStats stats){
        list = new ArrayList<HashMap<String,String>>();
        HashMap<String,String> gamesPlayedRow = new HashMap<>();
        gamesPlayedRow.put(FIRST_COLUMN, "Games played:");
        gamesPlayedRow.put(SECOND_COLUMN,String.valueOf(stats.gamesPlayed));

        HashMap<String,String> gamesWonRow = new HashMap<>();
        gamesWonRow.put(FIRST_COLUMN, "Games Won:");
        gamesWonRow.put(SECOND_COLUMN,String.valueOf(stats.gamesWon));

        HashMap<String,String> gamesLostRow = new HashMap<>();
        gamesLostRow.put(FIRST_COLUMN, "Games Lost:");
        gamesLostRow.put(SECOND_COLUMN,String.valueOf(stats.gamesLost));

        HashMap<String,String> totalPointsRow = new HashMap<>();
        totalPointsRow.put(FIRST_COLUMN, "Total points:");
        totalPointsRow.put(SECOND_COLUMN,String.valueOf(stats.totalPoints));

        HashMap<String,String> bestTimeRow = new HashMap<>();
        bestTimeRow.put(FIRST_COLUMN, "Best time:");
        bestTimeRow.put(SECOND_COLUMN,String.valueOf(stats.bestTime));

        HashMap<String,String> mostLaserHitsRow = new HashMap<>();
        mostLaserHitsRow.put(FIRST_COLUMN, "Most laser hits:");
        mostLaserHitsRow.put(SECOND_COLUMN,String.valueOf(stats.mostLaserHits));

        HashMap<String,String> mostBombHitsRow = new HashMap<>();
        mostBombHitsRow.put(FIRST_COLUMN, "Most bomb hits:");
        mostBombHitsRow.put(SECOND_COLUMN,String.valueOf(stats.mostBombHits));

        HashMap<String,String> totalBombHitsRow = new HashMap<>();
        totalBombHitsRow.put(FIRST_COLUMN, "Total bomb hits:");
        totalBombHitsRow.put(SECOND_COLUMN,String.valueOf(stats.totalBombHits));

        HashMap<String,String> totalShotsRow = new HashMap<>();
        totalShotsRow.put(FIRST_COLUMN, "Total shots:");
        totalShotsRow.put(SECOND_COLUMN,String.valueOf(stats.totalShots));

        HashMap<String,String> totalHitsRow = new HashMap<>();
        totalHitsRow.put(FIRST_COLUMN, "Total hits:");
        totalHitsRow.put(SECOND_COLUMN,String.valueOf(stats.totalHits));

        HashMap<String,String> hitPercentageRow = new HashMap<>();
        hitPercentageRow.put(FIRST_COLUMN, "Hit accuracy:");
        hitPercentageRow.put(SECOND_COLUMN,String.valueOf(stats.hitPercentage) + "%");

        list.add(gamesPlayedRow);
        list.add(gamesWonRow);
        list.add(gamesLostRow);
        list.add(totalPointsRow);
        list.add(bestTimeRow);
        list.add(mostLaserHitsRow);
        list.add(mostBombHitsRow);
        list.add(totalBombHitsRow);
        list.add(totalShotsRow);
        list.add(totalHitsRow);
        list.add(hitPercentageRow);



    }

}

