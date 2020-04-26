package com.example.arduino.gameScreen;

import com.google.firebase.database.ValueEventListener;

public class ValuesShared {
    private  String life1, life2, flag1, flag2, valid1, valid2, points1, points2;

    public ValuesShared(){}

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

}