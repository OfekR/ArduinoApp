package com.example.arduino.initGame;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class GameSetting implements Parcelable {
    private  String duration, keys, mines, shots, type, playerId1, playerId2;
    public GameSetting(){
        //Miki changed values from "null" to null
       duration = null;
        keys = null;
        mines = null;
        shots = null;
        type = null;
        //Miki added this
        this.playerId1 = FirebaseAuth.getInstance().getUid();
    }

    public GameSetting(String duration, String keys, String mines, String shots, String type) {
        this.duration = duration;
        this.keys = keys;
        this.mines = mines;
        this.shots = shots;
        this.type = type;
        this.playerId1 = FirebaseAuth.getInstance().getUid();
    }

    protected GameSetting(Parcel in) {
        String[] data= new String[7];

        in.readStringArray(data);
        this.duration= data[0];
        this.shots= data[1];
        this.type=data[2];
        this.keys=data[3];
        this.mines=data[4];
        this.playerId1=data[5];
        this.playerId2=data[6];

    }

    public static final Creator<GameSetting> CREATOR = new Creator<GameSetting>() {
        @Override
        public GameSetting createFromParcel(Parcel in) {
            return new GameSetting(in);
        }

        @Override
        public GameSetting[] newArray(int size) {
            return new GameSetting[size];
        }
    };

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

    public void setNumberShots(String numberShots) {
        this.shots = numberShots;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setMine(String mines){
        this.mines = mines;
    }

    public void setKeys(String keys){
        this.keys = keys;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.duration, this.shots,this.type,this.keys,this.mines,this.playerId1,this.playerId2});
    }
}
