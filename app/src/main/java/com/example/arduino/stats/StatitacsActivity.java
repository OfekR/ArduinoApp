package com.example.arduino.stats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.arduino.R;
import com.example.arduino.gameScreen.GameScreenActivity;
import com.example.arduino.menu.MenuActivity;
import com.example.arduino.utilities.MediaPlayerWrapper;
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

    private ProgressBar acuurcyProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.altrenative_stats_menu);

        /*
        sendDt= (Button) findViewById(R.id.StatsMenu);
        sendDt.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                changeScreen(MenuActivity.class);
            }
        });
*/
        usernameText = (TextView) findViewById(R.id.txtusername1);
        acuurcyProgressbar = (ProgressBar) findViewById(R.id.progressBar5);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        imageView = (ImageView) findViewById(R.id.imageStats);
        listView = (ListView) findViewById(R.id.listView1);
        btTop5Flag = (Button) findViewById(R.id.btTopFlag);
        btTop5Score = (Button) findViewById(R.id.btTopScore);
        bTtop5Time = (Button) findViewById(R.id.btTopTime);
        bTMystats = (Button) findViewById(R.id.btMystats);

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
                PlayerStats stats = new PlayerStats(gamesPlayed, gamesWon, gamesLost, totalPoints, bestTime,
                        mostLaserHits, mostBombHits, totalBombHits, totalShots, totalHits);
                changeRateandPic();
                getuserNameAcc();
                populateList(stats);
                adapt();
            }
        });

        btTop5Score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendClicker("TopScore");
            }
        });
        btTop5Flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendClicker("TopFlag");
            }
        });
        bTtop5Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendClicker("TopTime");
            }
        });
        bTMystats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              adapt();           }
        });
    }


    private  void sendClicker(final String doc){
            DocumentReference documentReference = db.collection("TopPlayer").document(doc);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    setList((task.getResult().getString("first")),(task.getResult().getString("second")),
                            (task.getResult().getString("third")),(task.getResult().getString("four")),(task.getResult().getString("five")),doc);
                }
            });
        }


    private void setList(String first,String second,String third,String four,String five,String doc ){
            if(doc.equals("TopScore")){
                listTop5Score = new ArrayList<HashMap<String,String>>();
                HashMap<String,String> firstTopScore = new HashMap<>();
                firstTopScore.put(FIRST_COLUMN, first);
                firstTopScore.put(SECOND_COLUMN,"Score");
                HashMap<String,String> secondTopScore = new HashMap<>();
                secondTopScore.put(FIRST_COLUMN, second);
                secondTopScore.put(SECOND_COLUMN,"Score");
                HashMap<String,String> thirdTopScore = new HashMap<>();
                thirdTopScore.put(FIRST_COLUMN, third);
                thirdTopScore.put(SECOND_COLUMN,"Score");
                HashMap<String,String> fourTopScore = new HashMap<>();
                fourTopScore.put(FIRST_COLUMN, four);
                fourTopScore.put(SECOND_COLUMN,"Score");
                HashMap<String,String> fiveTopScore = new HashMap<>();
                fiveTopScore.put(FIRST_COLUMN, five);
                fiveTopScore.put(SECOND_COLUMN,"Score");
                listTop5Score.add(firstTopScore);
                listTop5Score.add(secondTopScore);
                listTop5Score.add(thirdTopScore);
                listTop5Score.add(fourTopScore);
                listTop5Score.add(fiveTopScore);
                ListViewAdapter adapter = new ListViewAdapter(this, listTop5Score);
                listView.setAdapter(adapter);


            }
            else if(doc.equals("TopFlag")){
                listTop5Flag = new ArrayList<HashMap<String,String>>();
                HashMap<String,String> firstTopFlag= new HashMap<>();
                firstTopFlag.put(FIRST_COLUMN, first);
                firstTopFlag.put(SECOND_COLUMN,"Score");
                HashMap<String,String> secondTopFlag = new HashMap<>();
                secondTopFlag.put(FIRST_COLUMN, second);
                secondTopFlag.put(SECOND_COLUMN,"Score");
                HashMap<String,String> thirdTopFlag = new HashMap<>();
                thirdTopFlag.put(FIRST_COLUMN, third);
                thirdTopFlag.put(SECOND_COLUMN,"Score");
                HashMap<String,String> fourTopFlag = new HashMap<>();
                fourTopFlag.put(FIRST_COLUMN, four);
                fourTopFlag.put(SECOND_COLUMN,"Score");
                HashMap<String,String> fiveTopFlag = new HashMap<>();
                fiveTopFlag.put(FIRST_COLUMN, five);
                fiveTopFlag.put(SECOND_COLUMN,"Score");
                listTop5Flag.add(firstTopFlag);
                listTop5Flag.add(secondTopFlag);
                listTop5Flag.add(thirdTopFlag);
                listTop5Flag.add(fourTopFlag);
                listTop5Flag.add(fiveTopFlag);
                ListViewAdapter adapter = new ListViewAdapter(this, listTop5Flag);
                listView.setAdapter(adapter);
            }
            else if(doc.equals("TopTime")){
                listTop5Time = new ArrayList<HashMap<String,String>>();
                HashMap<String,String> firstTopTime = new HashMap<>();
                firstTopTime.put(FIRST_COLUMN, first);
                firstTopTime.put(SECOND_COLUMN,"Score");
                HashMap<String,String> secondTopTime = new HashMap<>();
                secondTopTime.put(FIRST_COLUMN, second);
                secondTopTime.put(SECOND_COLUMN,"Score");
                HashMap<String,String> thirdTopTime = new HashMap<>();
                thirdTopTime.put(FIRST_COLUMN, third);
                thirdTopTime.put(SECOND_COLUMN,"Score");
                HashMap<String,String> fourTopTime = new HashMap<>();
                fourTopTime.put(FIRST_COLUMN, four);
                fourTopTime.put(SECOND_COLUMN,"Score");
                HashMap<String,String> fiveTopTime = new HashMap<>();
                fiveTopTime.put(FIRST_COLUMN, five);
                fiveTopTime.put(SECOND_COLUMN,"Score");
                listTop5Time.add(firstTopTime);
                listTop5Time.add(secondTopTime);
                listTop5Time.add(thirdTopTime);
                listTop5Time.add(fourTopTime);
                listTop5Time.add(fiveTopTime);
                ListViewAdapter adapter = new ListViewAdapter(this, listTop5Time);
                listView.setAdapter(adapter);


            }
        }

    private void getuserNameAcc() {
        usernameText.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        acuurcyProgressbar.setProgress((int) hitPercentage);
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

