package com.example.arduino.stats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.arduino.R;
import com.example.arduino.menu.MenuActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatitacsActivity extends AppCompatActivity {
    private Button sendDt;
    long gamesPlayed, gamesWon, gamesLost, totalPoints, bestTime,
    mostLaserHits, mostBombHits, totalBombHits, totalShots, totalHits, hitPercentage,numPlayedhighscore,numPlayedtime,numPlayedflags,flags;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    ListView listView;
    ArrayList<HashMap<String,String>> list;
    ArrayList<HashMap<String,String>> listTop5Score;
    ArrayList<HashMap<String,String>> listTop5Time;
    ArrayList<HashMap<String,String>> listTop5Flag;
    public static final String FIRST_COLUMN="First";
    public static final String SECOND_COLUMN="Second";
    private RatingBar ratingBar;
    private ImageView imageView;
    private TextView usernameText;
    private Button btTop5Flag;
    private Button btTop5Score;
    private Button bTtop5Time;
    private Button bTMystats;
    private TextView sumflag;
    private TextView sumgame;
    private TextView sumTime;
    private TextView sumScore;


    private ProgressBar acuurcyProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.altrenative_stats_menu);
        listTop5Score = new ArrayList<HashMap<String,String>>();
        listTop5Flag = new ArrayList<HashMap<String,String>>();
        listTop5Time = new ArrayList<HashMap<String,String>>();


        sendDt= (Button) findViewById(R.id.backTomenu);
        sendDt.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                changeScreen(MenuActivity.class);
            }
        });

        usernameText = (TextView) findViewById(R.id.txtusername1);
        acuurcyProgressbar = (ProgressBar) findViewById(R.id.progressBar5);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        imageView = (ImageView) findViewById(R.id.imageStats);
        listView = (ListView) findViewById(R.id.listView1);
        btTop5Flag = (Button) findViewById(R.id.btTopFlag);
        btTop5Score = (Button) findViewById(R.id.btTopScore);
        bTtop5Time = (Button) findViewById(R.id.btTopTime);
        bTMystats = (Button) findViewById(R.id.btMystats);
        sumTime = (TextView) findViewById(R.id.sctxtTime);
        sumScore = (TextView) findViewById(R.id.sctxtScore);
        sumgame = (TextView) findViewById(R.id.sctxtsumgame);
        sumflag = (TextView) findViewById(R.id.scFlag);


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
                hitPercentage = task.getResult().getLong("hitsPercentage");
                flags = task.getResult().getLong("flags");
                numPlayedflags = task.getResult().getLong("numPlayedflags");
                numPlayedtime = task.getResult().getLong("numPlayedtime");
                numPlayedhighscore = task.getResult().getLong("numPlayedhighscore");

                PlayerStats stats = new PlayerStats(gamesPlayed, gamesWon, gamesLost, totalPoints, bestTime,
                        mostLaserHits, mostBombHits, totalBombHits, totalShots, totalHits, flags, numPlayedflags, numPlayedtime, numPlayedhighscore);
                changeRateandPic();
                getuserNameAcc();
                setupChart();
                populateList(stats);
                adapt();
            }
        });

        btTop5Score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!listTop5Score.isEmpty()){
                    activateList(listTop5Score);

                }
                else{
                    sendClicker("totalPoints",listTop5Score);

                }
            }
        });
        btTop5Flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!listTop5Flag.isEmpty()){
                    activateList(listTop5Flag);
                }
                else{
                    sendClicker("flags",listTop5Flag);

                }
            }
        });
        bTtop5Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!listTop5Time.isEmpty()){
                    activateList(listTop5Time);
                }
                else{
                    sendClicker("bestTime", listTop5Time);
                }
            }
        });
        bTMystats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              adapt();           }
        });
    }

    private void setupChart() {
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(numPlayedflags));
        pieEntries.add(new PieEntry(numPlayedhighscore));
        pieEntries.add(new PieEntry(numPlayedtime));
        PieDataSet dataset = new PieDataSet(pieEntries,"Games");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData data = new PieData((dataset));
        Long _tmpFlag =  numPlayedflags;
        Long _tmpsum =  gamesPlayed;
        Long _tmpscore =  numPlayedhighscore;
        Long _tmptime =  numPlayedtime;

        PieChart chart = (PieChart) findViewById(R.id.pieChart);
        chart.setDrawMarkers(false); // To remove markers when click
        chart.setDrawEntryLabels(false); // To remove labels from piece of pie
        chart.setDrawMarkers(false);
        chart.setDrawCenterText(false);
        chart.getDescription().setEnabled(false); // To remove description of pie
        chart.getLegend().setEnabled(false);

        sumflag.setText("Flag-Game: " + _tmpFlag.toString());
        sumgame.setText("Total-Game: " + _tmpsum.toString());
        sumScore.setText("High-Score: " + _tmpscore.toString());
        sumTime.setText("Last-Tank : " + _tmptime.toString());

        chart.setData(data);
        chart.invalidate();
    }


    private  void sendClicker(final String field , final ArrayList<HashMap<String,String>> list){
        CollectionReference stats = db.collection("PlayerStats");
                final Query query = stats.orderBy(field, Query.Direction.DESCENDING).limit(5);
                query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots.isEmpty()){
                                // need to handle if empty
                        }
                        else{
                            for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                                HashMap<String,String> firstTopScore = new HashMap<>();
                                firstTopScore.put(FIRST_COLUMN, doc.getString("nickname"));
                                firstTopScore.put(SECOND_COLUMN,doc.getLong(field).toString());
                                list.add(firstTopScore);
                            }
                        }
                    }
                });
                ListViewAdapter adapter = new ListViewAdapter(this, list);
                listView.setAdapter(adapter);

        }

        private void activateList(ArrayList<HashMap<String,String>> list){
            ListViewAdapter adapter = new ListViewAdapter(this, list);
            listView.setAdapter(adapter);
        }
    private void getuserNameAcc() {
        usernameText.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        if(totalShots == 0){
            acuurcyProgressbar.setProgress(0);
        }
        else {
            int num = (int) ((totalHits * 100 / totalShots));
            acuurcyProgressbar.setProgress(num);
        }
    }

    // change rating by the number of game you played
    private void changeRateandPic() {
        if(gamesPlayed < 2){
            // dont do nothing
        }
        else if(gamesPlayed < 5){
            imageView.setImageResource(R.drawable.advanced);
            ratingBar.setRating(2);
        }
        else {
            imageView.setImageResource(R.drawable.pro);
            ratingBar.setRating(3);
        }

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

        HashMap<String,String> numFlags = new HashMap<>();
        numFlags.put(FIRST_COLUMN, "NumFlags:");
        numFlags.put(SECOND_COLUMN,String.valueOf(stats.numFlags));

        HashMap<String,String> totalPlayedFlags = new HashMap<>();
        totalPlayedFlags.put(FIRST_COLUMN, "Total Played - Flags:");
        totalPlayedFlags.put(SECOND_COLUMN,String.valueOf(stats.numPlayedFlags));

        HashMap<String,String> totalPalyedScore = new HashMap<>();
        totalPalyedScore.put(FIRST_COLUMN, "Total Played - Score:");
        totalPalyedScore.put(SECOND_COLUMN,String.valueOf(stats.numPlayedHighScore));

        HashMap<String,String> totalPlayedTime = new HashMap<>();
        totalPlayedTime.put(FIRST_COLUMN, "Total Played - Time:");
        totalPlayedTime.put(SECOND_COLUMN,String.valueOf(stats.numPlayedTime));



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
        list.add(numFlags);
        list.add(totalPlayedFlags);
        list.add(totalPalyedScore);
        list.add(totalPlayedTime);


    }


}

