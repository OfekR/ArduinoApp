package com.example.arduino.gameScreen;

import java.util.HashMap;
import java.util.Map;

public class TransfromerData{
    private long bestTime;
    private long gamesLost;
    private long gamesPlayed;
    private long gamesWon;
    private long hitsPercentage;
    private String nickname;
    private long totalHits;

    public long getBestTime() {
        return bestTime;
    }

    public void setBestTime(long bestTime) {
        this.bestTime = bestTime;
    }

    public long getGamesLost() {
        return gamesLost;
    }

    public void setGamesLost(long gamesLost) {
        this.gamesLost = gamesLost;
    }

    public long getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(long gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public long getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(long gamesWon) {
        this.gamesWon = gamesWon;
    }

    public long getHitsPercentage() {
        return hitsPercentage;
    }

    public void setHitsPercentage(long hitsPercentage) {
        this.hitsPercentage = hitsPercentage;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(long totalHits) {
        this.totalHits = totalHits;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public long getTotalShots() {
        return totalShots;
    }

    public void setTotalShots(long totalShots) {
        this.totalShots = totalShots;
    }

    private long totalPoints;
    private long totalShots;
    public TransfromerData(){}
    public void setparams(Integer time ,Integer totalShotsHits,Integer score,Integer totalShotsFired,boolean state){
        if(state){
            bestTime = (time > bestTime ? time : bestTime);
            gamesWon = gamesWon +1;
        }
        else{
            gamesLost = gamesLost +1;
        }
        gamesPlayed = gamesPlayed +1;
        totalHits = totalShotsHits +totalHits;
        totalPoints = score +totalPoints;
        totalShots = totalShotsFired +totalShots;
        hitsPercentage = (totalShotsFired !=0) ? totalShotsHits/totalShotsFired:0;
    }
    public Map<String, Object> tomap(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("bestTime", bestTime);
        docData.put("gamesLost",gamesLost);
        docData.put("gamesPlayed",gamesPlayed);
        docData.put("gamesWon",  gamesWon);
        docData.put("hitsPercentage", hitsPercentage);
        docData.put("nickname",nickname);
        docData.put("totalHits",totalHits);
        docData.put("totalPoints", totalPoints);
        docData.put("totalShots", totalShots);
        return docData;
    }

    public TransfromerData(long bestTime, long gamesLost, long gamesPlayed, long gamesWon, long hitsPercentage, String nickname, long totalHits, long totalPoints, long totalShots) {
        this.bestTime = bestTime;
        this.gamesLost = gamesLost;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.hitsPercentage = hitsPercentage;
        this.nickname = nickname;
        this.totalHits = totalHits;
        this.totalPoints = totalPoints;
        this.totalShots = totalShots;
    }
}
