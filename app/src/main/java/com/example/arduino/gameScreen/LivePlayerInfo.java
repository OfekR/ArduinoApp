package com.example.arduino.gameScreen;

// "stupid struct" which only hold all the fields togther
public class LivePlayerInfo {
    private Integer ammo;
    private Integer defuser;
    private Integer keys;
    private Integer life;
    private Integer mines;
    private Integer score;
    private Integer specialKeys;

    LivePlayerInfo() {
        ammo = 0;
        defuser = 0;
        keys = 0;
        life = 20;
        mines = 0;
        score = 0;
        specialKeys = 0;
    }

    LivePlayerInfo(Integer rAmmo,Integer rDefuser,Integer rKeys,Integer rLife,Integer rMines,Integer rScore,Integer rSpecialKeys) {
        ammo = rAmmo;
        defuser = rDefuser;
        keys = rKeys;
        life = rLife;
        mines = rMines;
        score = rScore;
        specialKeys = rSpecialKeys;
    }

    public Integer getAmmo() {
        return ammo;
    }

    public Integer getDefuser() {
        return defuser;
    }

    public Integer getKeys() {
        return keys;
    }

    public Integer getLife() {
        return life;
    }

    public Integer getMines() {
        return mines;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getSpecialKeys() {
        return specialKeys;
    }

    public void setAmmo(Integer ammo) {
        this.ammo = ammo;
    }

    public void setDefuser(Integer defuser) {
        this.defuser = defuser;
    }

    public void setKeys(Integer keys) {
        this.keys = keys;
    }

    public void setLife(Integer life) {
        this.life = life;
    }

    public void setMines(Integer mines) {
        this.mines = mines;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setSpecialKeys(Integer specialKeys) {
        this.specialKeys = specialKeys;
    }
}
