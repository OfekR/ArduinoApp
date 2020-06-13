package com.example.arduino.gameScreen;

import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ValuesShared {
    private  Integer ammo1,ammo2,life1, life2, flag1, flag2, valid1, valid2, points1, points2,defuse1,defuse2,mine1,mine2,keys1,keys2,specialKey1,specialKey2;

    public ValuesShared(){}

    public Integer getValid1() {
        return valid1;
    }

    public Integer getDefuse1() {
        return defuse1;
    }

    public Integer getDefuse2() {
        return defuse2;
    }

    public Integer getValid2() {
        return valid2;
    }



    public ValuesShared(Integer ammo1, Integer ammo2, Integer life1, Integer life2, Integer flag1, Integer flag2, Integer valid1, Integer valid2, Integer points1, Integer points2, Integer defuse1, Integer defuse2, Integer mine1, Integer mine2, Integer keys1, Integer keys2, Integer specialKey1, Integer specialKey2) {
        this.ammo1 = ammo1;
        this.ammo2 = ammo2;
        this.life1 = life1;
        this.life2 = life2;
        this.flag1 = flag1;
        this.flag2 = flag2;
        this.valid1 = valid1;
        this.valid2 = valid2;
        this.points1 = points1;
        this.points2 = points2;
        this.defuse1 = defuse1;
        this.defuse2 = defuse2;
        this.mine1 = mine1;
        this.mine2 = mine2;
        this.keys1 = keys1;
        this.keys2 = keys2;
        this.specialKey1 = specialKey1;
        this.specialKey2 = specialKey2;
    }

    public Integer getAmmo1() {
        return ammo1;
    }

    public Integer getAmmo2() {
        return ammo2;
    }

    public Integer getSpecialKey1() {
        return specialKey1;
    }

    public Integer getSpecialKey2() {
        return specialKey2;
    }

    public Integer getLife1() {
        return life1;
    }


    public Integer getLife2() {
        return life2;
    }


    public Integer getFlag1() {
        return flag1;
    }


    public Integer getFlag2() {
        return flag2;
    }

    public Integer getMine1() {
        return mine1;
    }

    public Integer getMine2() {
        return mine2;
    }

    public Integer getKeys1() {
        return keys1;
    }

    public Integer getKeys2() {
        return keys2;
    }

    public Integer getPoints1() {
        return points1;
    }


    public Integer getPoints2() {
        return points2;
    }

    public Map<String,Integer> resetObject(){
        Map<String,Integer> map = new HashMap<>();
        map.put("points1",0);
        map.put("points2",0);
        map.put("flag1",0);
        map.put("flag2",0);
        map.put("life1",20);
        map.put("life2",20);
        map.put("valid1",0);
        map.put("valid2",0);
        map.put("defuse1",0);
        map.put("defuse2",0);
        map.put("keys1",0);
        map.put("keys2",0);
        map.put("mine1",0);
        map.put("mine2",0);
        map.put("specialKey1",0);
        map.put("specialKey2",0);
        map.put("ammo1",0);
        map.put("ammo2",0);


        return map;
    }
}