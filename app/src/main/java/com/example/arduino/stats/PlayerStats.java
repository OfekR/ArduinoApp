package com.example.arduino.stats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats {
    long gamesPlayed;
    long gamesWon;
    long gamesLost;
    long totalPoints;
    long bestTime;
    long mostLaserHits;
    long mostBombHits;
    long totalBombHits;
    long totalShots;
    long totalHits;
    long hitPercentage;


    public PlayerStats(long gamesPlayed, long gamesWon, long gamesLost, long totalPoints, long bestTime, long mostLaserHits, long mostBombHits, long totalBombHits, long totalShots, long totalHits, long hitPercentage) {
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.gamesLost = gamesLost;
        this.totalPoints = totalPoints;
        this.bestTime = bestTime;
        this.mostLaserHits = mostLaserHits;
        this.mostBombHits = mostBombHits;
        this.totalBombHits = totalBombHits;
        this.totalShots = totalShots;
        this.totalHits = totalHits;
        this.hitPercentage = hitPercentage;
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
        hm.put("mostLaserHits", stats.mostLaserHits);
        hm.put("mostBombHits", stats.mostBombHits);
        hm.put("totalBombHits", stats.totalBombHits);
        hm.put("totalShots", stats.totalShots);
        hm.put("totalHits", stats.totalHits);
        hm.put("hitPercentage", stats.hitPercentage);
        documentReference.set(hm).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //TODO
            }
        });
    }

//    public static PlayerStats readStats(String userId){
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference documentReference = db.collection("PlayerStats").document(userId);
//        final PlayerStats stats=  new PlayerStats();
//        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                long gamesPlayed, gamesWon, gamesLost;
//                gamesPlayed = task.getResult().getLong("gamesPlayed");
//                gamesWon= task.getResult().getLong("gamesWon");
//                gamesLost = task.getResult().getLong("gamesLost");
//                stats.setGamesLost((int)gamesLost);
//                stats.setGamesWon((int)gamesWon);
//                stats.setGamesPlayed((int)gamesPlayed);
//                System.out.println("DONE");
//            }
//        });
//
//        return stats;
//
//    }
}
