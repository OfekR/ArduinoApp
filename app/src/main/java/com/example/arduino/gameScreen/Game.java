package com.example.arduino.gameScreen;

import android.util.Log;

import com.example.arduino.initGame.Member;

import com.google.firebase.auth.FirebaseAuth;


import java.util.HashMap;
import java.util.Map;

enum StatusGame{DRAW,WIN,LOSE,DONTKNOW};
enum EndOfGameReason{TIME,LIFE,FLAG};
public class Game {
    private Integer num_hits;
    private StatusGame statusEndofGame;
    private Integer life;
    private  Integer Ammuo;
    private Integer time;
    private  Integer type;
    private Integer point;
    private String playerID;
    private  String oppID;
    private Integer mines;
    private Integer keys;
    private  Integer flag;
    private Integer defuse;
    private ValuesShared valuesShared;
    private Long[] totalData; // in the first place 1.totalHit , 2.TotalShots, 3.TotalBombHit;
    final private String firebaseId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public Integer getDefuse() {
        return defuse;
    }

    public void setDefuse(Integer defuse) {
        this.defuse = defuse;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public Integer getType() {
        return type;
    }

    public Integer getMines() {
        return mines;
    }

    public void setMines(Integer mines) {
        this.mines = mines;
    }

    public Integer getKeys() {
        return keys;
    }

    public void setKeys(Integer keys) {
        this.keys = keys;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public Integer getPoint() {
        return point;
    }

    public ValuesShared getValuesShared() {
        return valuesShared;
    }

    public void setValuesShared(ValuesShared valuesShared) {
        this.valuesShared = valuesShared;
    }

    public Integer getNum_hits() {
        return num_hits;
    }

    public void setNum_hits(Integer num_hits) {
        this.num_hits = num_hits;
    }

    public Game(Member meb) {

        this.time =  Integer.parseInt((String)meb.getTime());
        this.Ammuo =  Integer.parseInt((String)meb.getNumberShot());
        this.type = Integer.parseInt((String)meb.getGameType());
        this.keys =  Integer.parseInt(meb.getKeys());
        this.mines = Integer.parseInt(meb.getMines());
        this.playerID = (String) meb.getPlayer1();
        this.oppID = (String) meb.getPlayer2();
        this.defuse = 0;
        this.life = 20;
        this.flag = 0;
        this.num_hits =0;   // num of hits i gat from opp
        this.totalData = new Long[3];
        for(int i=0; i<3;i++){
            totalData[i] = Long.valueOf(0);
        }
        valuesShared = new ValuesShared("20","20","0","0","1","1","0","0");
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

    public Integer getLife() {
        return life;

    }



    public Game() {
    }

    public Integer getAmmuo() {
        return Ammuo;
    }

    public void setAmmuo(Integer ammuo) {
        Ammuo = ammuo;
    }


    public void setLife(Integer life) {
        this.life = life;
    }

    public Map<String, Object> toMap(StatusGame statusGame, double timeleft){
        Map<String, Object> docData = new HashMap<>();
        this.statusEndofGame = statusGame;
        Double num = (timeleft)/1000;
        docData.put("ID",firebaseId);
        docData.put("STATUS-END-OF-GAME", statusGame);
        docData.put("LIFE-END-OF-GAME", life);
        docData.put("AMMO-END-OF-GAME", Ammuo);
        docData.put("TIME-LEFT-IN-SEC",  num);
        docData.put("TYPE-END-OF-GAME", type);
        docData.put("POINTS-END-OF-GAME", point);
        docData.put("MINES-END-OF-GAME", mines);
        docData.put("DEFUSE-END-OF-GAME", defuse);
        docData.put("KEYS-END-OF-GAME", keys);
        docData.put("NUM-HITS-END-OF-GAME", totalData[1]);
        docData.put("NUM-SHOTS-END-OF-GAME", totalData[0]);
        docData.put("NUM-BOMB-END-OF-GAME", totalData[2]);
        docData.put("FLAG", flag);
        return docData;
    }

    public Long[] getTotalData() {
        return totalData;
    }

    public void raiseBy1TotalData(String field){
        if(field.equals("Shots")){
            totalData[0] = totalData[0] +1;
        }
        else if(field.equals("Hits")) {
            totalData[1] = totalData[1] + 1;
            // Bomb
        }
        else{
                totalData[2] = totalData[2] +1;
            }
    }

}
