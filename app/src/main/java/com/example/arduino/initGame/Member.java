package com.example.arduino.initGame;

import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

public class Member {


    private Map<String, String> data = new HashMap<String, String>();
    public Member() {
    }

    public Object getGameType() {
        return data.get("Type");
    }

    public void setGameType(String gameType) {
        this.data.put("Type",gameType);
    }

    public Object getNumberShot() {
        return data.get("Shots");
    }

    public void setNumberShot(String numberShot) {
        this.data.put("Shots",numberShot);
    }

    public Object getTime() {
        return data.get("Duration");
    }

    public void setTime(String time) {
        data.put("Duration",time);
    }
}
