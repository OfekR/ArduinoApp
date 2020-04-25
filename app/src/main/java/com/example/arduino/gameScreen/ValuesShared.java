package com.example.arduino.gameScreen;

import com.google.firebase.database.ValueEventListener;

public class ValuesShared {
    String life1, life2, flag1, flag2, valid1, valid2, points1, points2;

    public String getLife1() {
        return life1;
    }
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

    public void setLife1(String life1) {
        this.life1 = life1;
    }

    public String getLife2() {
        return life2;
    }

    public void setLife2(String life2) {
        this.life2 = life2;
    }

    public String getFlag1() {
        return flag1;
    }

    public void setFlag1(String flag1) {
        this.flag1 = flag1;
    }

    public String getFlag2() {
        return flag2;
    }

    public void setFlag2(String flag2) {
        this.flag2 = flag2;
    }

    public String getValid1() {
        return valid1;
    }

    public void setValid1(String valid1) {
        this.valid1 = valid1;
    }

    public String getValid2() {
        return valid2;
    }

    public void setValid2(String valid2) {
        this.valid2 = valid2;
    }

    public String getPoints1() {
        return points1;
    }

    public void setPoints1(String points1) {
        this.points1 = points1;
    }

    public String getPoints2() {
        return points2;
    }

    public void setPoints2(String points2) {
        this.points2 = points2;
    }
}