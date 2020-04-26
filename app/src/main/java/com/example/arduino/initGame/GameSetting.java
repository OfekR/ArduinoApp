package com.example.arduino.initGame;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class GameSetting {
    private  String duration, keys, mines, shots, type, playerId1, playerId2;
    public GameSetting(){
       duration = "null";
        keys = "null";
        mines = "null";
        shots = "null";
        type = "null";

    }

    public GameSetting(String duration, String keys, String mines, String shots, String type) {
        this.duration = duration;
        this.keys = keys;
        this.mines = mines;
        this.shots = shots;
        this.type = type;
        this.playerId1 = FirebaseAuth.getInstance().getUid();
    }

    public String getDuration() {
        return duration;
    }

    public String getKeys() {
        return keys;
    }

    public String getMines() {
        return mines;
    }

    public String getShots() {
        return shots;
    }

    public String getType() {
        return type;
    }

    public String getPlayerId1() {
        return playerId1;
    }

    public String getPlayerId2() {
        return playerId2;
    }

    // defulat values
    private void valdiateArggs(){
        if(duration == null) duration = "10";
        if(keys == null) keys = "0";
        if(mines == null) mines = "0";
        if(shots == null) shots = "20";
        if(type == null) type = "1";

    }
   public Map<String, Object> toHashMap(){
        valdiateArggs();
       HashMap<String,Object> hashMap = new HashMap<>();
       hashMap.put("duration",duration);
       hashMap.put("keys",keys);
       hashMap.put("mines",mines);
       hashMap.put("shots",shots);
       hashMap.put("type",type);
       hashMap.put("playerId1",playerId1);
       return hashMap;
   }
}
