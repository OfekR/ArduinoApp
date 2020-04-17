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
    private Map<String, Object> data = new HashMap<String, Object>();

    public Member() {
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
        String[] data= new String[3];

        in.readStringArray(data);
        this.time= data[0];
        this.numberShot= data[1];
        this.type=data[2];
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeStringArray(new String[]{this.time, this.numberShot,this.type });
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
}
