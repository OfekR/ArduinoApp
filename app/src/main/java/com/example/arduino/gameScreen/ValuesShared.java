package com.example.arduino.gameScreen;

import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ValuesShared {
    private  String life1, life2, flag1, flag2, valid1, valid2, points1, points2;

    public ValuesShared(){}

    public String getValid1() {
        return valid1;
    }

    public String getValid2() {
        return valid2;
    }

    public ValuesShared(String life1, String life2, String flag1, String flag2, String valid1, String valid2, String points1, String points2) {
        this.life1 = life1;
        this.life2 = life2;
        this.flag1 = flag1;
        this.flag2 = flag2;
        this.valid1 = valid1;
        this.valid2 = valid2;
        this.points1 = points1;
        this.points2 = points2;
    }
    public String getLife1() {
        return life1;
    }


    public String getLife2() {
        return life2;
    }


    public String getFlag1() {
        return flag1;
    }


    public String getFlag2() {
        return flag2;
    }


    public String getPoints1() {
        return points1;
    }


    public String getPoints2() {
        return points2;
    }

    public Map<String,String> resetObject(){
        Map<String,String> map = new HashMap<>();
        map.put("points1","0");
        map.put("points2","0");
        map.put("flag1","0");
        map.put("flag2","0");
        map.put("life1","20");
        map.put("life2","20");
        map.put("valid1","0");
        map.put("valid2","0");
        return map;
    }
}