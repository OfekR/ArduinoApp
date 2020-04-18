package com.example.arduino.gameScreen;
import android.content.Intent;
import android.graphics.Path;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

import java.util.Objects;

public class Game {

    private String life;
    private String Ammuo;
    private String time;
    private  String type;
    private Integer point;
    private String playerID;
    private  String OppID;
    private String mines;
    private String keys;

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
        this.OppID = (String) meb.getPlayer2();
        checkValidId();

    }

    private void checkValidId() {
        final String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //TODO:REMOVE
        Log.d("MY GAME -------->  ", "My ID -- "+myID+" PLAYER-ID -------> "+playerID);
        if(playerID.equals(myID)){
            playerID="1";
        }
        else{
            playerID = "2";
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

}
