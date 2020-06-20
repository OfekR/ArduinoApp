package com.example.arduino.stats;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerStats {
    long gamesPlayed;
    long gamesWon;
    long gamesLost;
    long totalPoints;
    long bestTime;
    long totalShots;
    long totalHits;
    long hitPercentage;
    String nickname;


    //C'tor to create new player stats
    public PlayerStats() {
        gamesPlayed = 0;
        gamesWon = 0;
        gamesLost = 0;
        totalPoints = 0;
        bestTime = 0;
        totalShots = 0;
        totalHits = 0;
        hitPercentage = 0;
        nickname =  Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
    }

    public PlayerStats(long gamesPlayed, long gamesWon, long gamesLost, long totalPoints, long bestTime,  long totalShots, long totalHits) {
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.gamesLost = gamesLost;
        this.totalPoints = totalPoints;
        this.bestTime = bestTime;
        this.totalShots = totalShots;
        this.totalHits = totalHits;
        this.hitPercentage = (long)((float)totalHits / totalShots * 100);
    }

    public static void writeStats(String userId, PlayerStats stats){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("PlayerStats").document(userId);
        Map<String, Object> hm = new HashMap<>();
        hm.put("gamesPlayed", stats.gamesPlayed);
        hm.put("gamesWon", stats.gamesWon);
        hm.put("gamesLost", stats.gamesLost);
        hm.put("totalPoints", stats.totalPoints);
        hm.put("bestTime", stats.bestTime);
        hm.put("totalShots", stats.totalShots);
        hm.put("totalHits", stats.totalHits);
        hm.put("hitsPercentage", stats.hitPercentage);
        hm.put("nickname",stats.nickname);
        documentReference.set(hm);
    }
}
