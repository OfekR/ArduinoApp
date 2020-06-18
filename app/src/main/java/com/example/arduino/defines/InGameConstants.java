package com.example.arduino.defines;

public class InGameConstants {
    private InGameConstants() {

    }

    // Score change
    public static final Integer AddPointDueMinePlace = 20;
    public static final Integer AddPointDueMineDefusion = 50;
    public static final Integer AddPointsDueBarrierOpen = 10;
    public static final Integer AddPointsDueSpecialBarrierOpen = 10;
    public static final Integer ReducePointDueExplosion = -40;
    public static final Integer addPointDueExplosion = 40;
    public static final Integer ReduceLifeDueExplosion = -5;
    public static final Integer ReducePointDueHit = -20;
    public static final Integer addPointDueHit = 20;
    public static final Integer ReduceLifeDueHit = -2;
    public static final Integer AddPointDueSpecialKeyAcquire = 10;


    // Timing change in seconds
    public static final Integer PlaceMineDelay = 5;
    public static final Integer ExplodeCarParalyzed = 5;
    public static final Integer LaserHitParalyzed = 2;
    public static final Integer LaserHitBtnDelay = 3;

    //winner cause
    public static final Integer winnerCauseReachEndTag = 3;
    public static final Integer winnerCauseReachPlayerDied = 4;
    public static final Integer winnerCauseReachTimeLimit = 5;
    public static final Integer winnerCauseReachPlayerForfeit = 6;


}
