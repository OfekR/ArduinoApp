package com.example.arduino.gameScreen;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.arduino.initGame.InitGameActivity;
import com.example.arduino.initGame.Member;
import com.example.arduino.loby.LobyActivity;
import com.example.arduino.loby.PopWindow;
import com.example.arduino.utilities.HttpHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

enum StatusGame{DRAW,WIN,LOSE,DONTKNOW};
enum EndOfGameReason{TIME,LIFE,FLAG};
public class Game {
    private StatusGame statusEndofGame;
    private String life;
    private String Ammuo;
    private String time;
    private  String type;
    private Integer point;
    private String playerID;
    private  String oppID;
    private String mines;
    private String keys;
    private String defuse;
    private String[] totalData; // in the first place 1.totalHit , 2.TotalShots, 3.TotalBombHit;
    final private String firebaseId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public String getDefuse() {
        return defuse;
    }

    public void setDefuse(String defuse) {
        this.defuse = defuse;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public String getType() {
        return type;
    }

    public String getMines() {
        return mines;
    }

    public void setMines(String mines) {
        this.mines = mines;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public Integer getPoint() {
        return point;
    }

    public Game(Member meb) {

        this.time =  (String) meb.getTime();
        this.Ammuo =  (String) meb.getNumberShot();
        this.type = (String) meb.getGameType();
        this.keys = (String) meb.getKeys();
        this.mines = (String) meb.getMines();
        this.playerID = (String) meb.getPlayer1();
        this.oppID = (String) meb.getPlayer2();
        this.defuse = "0";
        this.life = "100";
        this.totalData = new String[3];
        for(int i=0; i<3;i++){
            totalData[i] = "0";
        }
        checkValidId();
        statusEndofGame = StatusGame.DRAW;
    }

    public String getOppID() {
        return oppID;
    }

    private void checkValidId() {
        final String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //TODO:REMOVE
        Log.d("MY GAME -------->  ", "My ID -- "+myID+" PLAYER-ID -------> "+playerID);
        if(playerID.equals(myID)){
            playerID="1";
            oppID="2";
        }
        else{
            playerID = "2";
            oppID="1";
        }
        //TODO REMOVE
        Log.d("MY GAME -------->  ", "My ID -- "+playerID);
    }

    public String getPlayerID() {
        return playerID;
    }

    public String getLife() {
        return life;

    }
    public Game() {
        this.life = "10";
    }
    public String getAmmuo() {
        return Ammuo;
    }

    public void setAmmuo(String ammuo) {
        Ammuo = ammuo;
    }

    public Game(String life) {
        this.life = life;
    }

    public void setLife(String life) {
        this.life = life;
    }

    public Map<String, Object> toMap(StatusGame statusGame, double timeleft){
        Map<String, Object> docData = new HashMap<>();
        this.statusEndofGame = statusGame;
        Double num = (timeleft)/1000;
        docData.put("STATUS-END-OF-GAME", statusGame);
        docData.put("LIFE-END-OF-GAME", life);
        docData.put("AMMO-END-OF-GAME", Ammuo);
        docData.put("TIME-LEFT-IN-SEC", num.toString());
        docData.put("TYPE-END-OF-GAME", type);
        docData.put("POINTS-END-OF-GAME", point);
        docData.put("MINES-END-OF-GAME", mines);
        docData.put("DEFUSE-END-OF-GAME", defuse);
        docData.put("KEYS-END-OF-GAME", keys);
        docData.put("NUM-HITS-END-OF-GAME", totalData[1]);
        docData.put("NUM-SHOTS-END-OF-GAME", totalData[0]);
        docData.put("NUM-BOMB-END-OF-GAME", totalData[2]);
        return docData;
    }

    public void raiseBy1TotalData(String field){
        if(field.equals("Shots")){
            Integer num = (Integer.parseInt(totalData[0])+1);
            totalData[0] = num.toString();
        }
        else if(field.equals("Hits")){
            Integer num = (Integer.parseInt(totalData[1])+1);
            totalData[1] = num.toString();
        }
        // Bomb
        else{
            Integer num = (Integer.parseInt(totalData[2])+1);
            totalData[2] = num.toString();
        }
    }

}
