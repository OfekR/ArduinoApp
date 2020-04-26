package com.example.arduino.initGame;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Member implements Parcelable {

    private  String type;
    private  String numberShot;
    private  String time;
    private  String keys;
    private  String mines;
    private  String player1;
    private  String player2;
    private Map<String, Object> data = new HashMap<String, Object>();

   public Member(GameSetting gameSetting){
        this.time= gameSetting.getDuration();
        this.numberShot= gameSetting.getShots();
        this.type=gameSetting.getType();
        this.keys=gameSetting.getKeys();
        this.mines=gameSetting.getMines();
        this.player1=gameSetting.getPlayerId1();
        this.player2=gameSetting.getPlayerId2();
    }

    public String getType() {
        return type;
    }

    public String getKeys() {
        return keys;
    }

    public String getMines() {
        return mines;
    }

    public Member() {
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public Object getGameType() {
        return type;
    }

    public void setGameType(String gameType) {
        this.type = gameType;
        this.data.put("Type", gameType);
    }

    public Object getNumberShot() {
        return numberShot;
    }

    public void setNumberShot(String numberShot) {
        this.numberShot = numberShot;
        this.data.put("Shots", numberShot);
    }

    public Object getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
        data.put("Duration", time);
    }

    public Map<String, Object> getMap() {
        return data;
    }
    public Member(Parcel in){
        String[] data= new String[7];

        in.readStringArray(data);
        this.time= data[0];
        this.numberShot= data[1];
        this.type=data[2];
        this.keys=data[3];
        this.mines=data[4];
        this.player1=data[5];
        this.player2=data[6];
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeStringArray(new String[]{this.time, this.numberShot,this.type,this.keys,this.mines,this.player1,this.player2});
    }

    public static final Parcelable.Creator<Member> CREATOR = new Parcelable.Creator<Member>() {

        @Override
        public Member createFromParcel(Parcel source) {
            return new Member(source);  //using parcelable constructor
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

    public void setKeys(String valueOf) {
        this.keys = valueOf;
    }

    public void setMines(String valueOf) {
        this.mines = valueOf;
    }
}
