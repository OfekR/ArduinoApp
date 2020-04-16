package com.example.arduino.gameScreen;
import android.os.Parcelable;
import android.util.Log;

import com.example.arduino.initGame.Member;

public class Game {
    private String life;
    private String Ammuo;
    private String time;
    private  String type;
    private String point;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getPoint() {
        return point;
    }

    public Game(Member meb) {
        this.time =  (String) meb.getTime();
        this.Ammuo =  (String) meb.getNumberShot();
        this.type = (String) meb.getGameType();
        Log.d("OWW-TAG----->","the parma of :"+"shot: "+Ammuo+"time:  "+ time);

    }

    public String getLife() {
        return life;

    }
    public Game() {
        this.life = "10";
    }
    public String getAmmuo() {
        return Ammuo;
    }

    public void setAmmuo(String ammuo) {
        Ammuo = ammuo;
    }

    public Game(String life) {
        this.life = life;
    }

    public void setLife(String life) {
        this.life = life;
    }

}
