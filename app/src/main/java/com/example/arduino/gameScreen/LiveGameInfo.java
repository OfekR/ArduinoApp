package com.example.arduino.gameScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.arduino.defines.InGameConstants;
import com.example.arduino.defines.LogDefs;
import com.example.arduino.stats.PlayerStats;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

enum LiveGameInfoField {
    AMMO,
    DEFUSERS,
    KEYS,
    LIFE,
    MINES,
    SCORE,
    SPECIAL_KEYS
}

    /****Class manage all info related to the user when game is live
     *     all update and retrieval of this info should be done via this class
     *     Firebase responsible for: LiveGameInfo and EnemyInjured
     */

public class LiveGameInfo {
    private static final String TAG = LogDefs.tagRFID;
    DatabaseReference mDatabase;
    final String firebaseLiveGameInfoPath;
    final String firebaseEnemyInjuredPath;
    static final Map<LiveGameInfoField, String> ENUM_TO_FIREBASE = ImmutableMap.<LiveGameInfoField, String>builder()
            .put(LiveGameInfoField.AMMO, "ammo")
            .put(LiveGameInfoField.DEFUSERS, "defusers")
            .put(LiveGameInfoField.KEYS, "keys")
            .put(LiveGameInfoField.LIFE, "life")
            .put(LiveGameInfoField.MINES, "mines")
            .put(LiveGameInfoField.SCORE, "score")
            .put(LiveGameInfoField.SPECIAL_KEYS, "specialKeys")
            .build();


    // Game screen variables
    private Button _btnShot;
    private TextView _txtLife;
    private TextView _txtAmmo;
    private TextView _txtScore;
    private TextView _txtKeys;
    private TextView _txtMines;
    private TextView _txtDefuse;
    private TextView _txtSpecialKey;

    private int _playerId;
    private String _playerIdStr;
    private Context _context;

    // field listeners
    private ValueEventListener ammoListener;
    private ValueEventListener defusersListener;
    private ValueEventListener keysListener;
    private ValueEventListener lifeListener;
    private ValueEventListener minesListener;
    private ValueEventListener scoreListener;
    private ValueEventListener specialKeysListener;

    private DatabaseReference ammoDocRef;
    private DatabaseReference defusersDocRef;
    private DatabaseReference keysDocRef;
    private DatabaseReference lifeDocRef;
    private DatabaseReference minesDocRef;
    private DatabaseReference scoreDocRef;
    private DatabaseReference specialKeysRef;

    //
    private ValueEventListener enemyInjuredListener;
    private DatabaseReference enemyInjuredDocRef;

    private LivePlayerInfo _livePlayerInfo;
    private boolean _isShootBtnEnabled;
    // Statistics
    //TODO Extra - generally fact enemy explode doesn't mean my mine explode (might be his) but it's ok IO guess
    public Integer totalMineHits;
    public Integer totalMinePlanted;
    public Integer totalMineInjured;
    public Integer totalShotsHits;
    public Integer totalShotsFired;
    public Integer totalShotsInjured;
    public Integer totalKeyUsed;
    public Integer gameType;

    public LiveGameInfo(Context context, DatabaseReference mDatabaseRef, int playerId, Button btnShot,
    TextView txtLife, TextView txtAmmo, TextView txtScore, TextView txtKeys, TextView txtMines, TextView txtDefuse, TextView txtSpecialKey) {
        _btnShot = btnShot;
        _txtLife = txtLife;
        _txtAmmo = txtAmmo;
        _txtScore = txtScore;
        _txtKeys = txtKeys;
        _txtMines = txtMines;
        _txtDefuse = txtDefuse;
        _txtSpecialKey = txtSpecialKey;

        _playerId = playerId;
        _playerIdStr = (_playerId == 1 ? "Player1" : "Player2");
        _context = context;
        firebaseLiveGameInfoPath = "LiveGameinfo/" + _playerIdStr + "/";
        firebaseEnemyInjuredPath = "EnemyInjured/" + _playerIdStr + "/";
        mDatabase = mDatabaseRef;
        _livePlayerInfo = new LivePlayerInfo();
        _isShootBtnEnabled = false;

        // init stats
        totalMineHits = 0;
        totalMinePlanted = 0;
        totalMineInjured = 0;
        totalShotsHits = 0;
        totalShotsFired = 0;
        totalShotsInjured = 0;
        totalKeyUsed = 0;
        gameType = 0;





    }

    //TODO - need maybe to indicate finished updating setting (I guess 3 seconds count down should be enough..)
    public void initLivePlayerInfoFromSettings() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference settingDocRef = database.getReference("GameSettings");
        settingDocRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //TODO - change GameSetting to integer
                _livePlayerInfo.setAmmo(Integer.parseInt(dataSnapshot.child("shots").getValue(String.class)));
                _livePlayerInfo.setKeys(Integer.parseInt(dataSnapshot.child("keys").getValue(String.class)));
                _livePlayerInfo.setMines(Integer.parseInt(dataSnapshot.child("mines").getValue(String.class)));
                gameType = Integer.parseInt(dataSnapshot.child("type").getValue(String.class));
                mDatabase.child("LiveGameinfo").child(_playerIdStr).setValue(_livePlayerInfo);
                if(_playerId == 1) {
                    mDatabase.child("LiveGameinfo").child("gameEnd").setValue(0);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());

            }
        });
    }

    public void resetEnemyInjuredFirebase() {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("enemyExplode", 0);
        hashMap.put("enemyHit", 0);
        mDatabase.child(firebaseEnemyInjuredPath).updateChildren(hashMap);
    }

    public Integer getFieldValue(LiveGameInfoField field) {
        switch(field) {
            case AMMO:
                return _livePlayerInfo.getAmmo();
            case DEFUSERS:
                return _livePlayerInfo.getDefuser();
            case KEYS:
                return _livePlayerInfo.getKeys();
            case LIFE:
                return _livePlayerInfo.getLife();
            case MINES:
                return _livePlayerInfo.getMines();
            case SCORE:
                return _livePlayerInfo.getScore();
            case SPECIAL_KEYS:
                return _livePlayerInfo.getSpecialKeys();
        }
        assert(false);
        return 0;
    }


    // update given field with given value
    public void updateFieldAbsoluteValue(LiveGameInfoField field, Integer value) {
        String fieldString = ENUM_TO_FIREBASE.get(field);
        mDatabase.child("LiveGameinfo").child(_playerIdStr).child(fieldString).setValue(value);
    }

    // add to given field the given value
    public void updateFieldRelativeValue(LiveGameInfoField field, Integer value) {
        Integer valueToWrite = getFieldValue(field) + value;
        updateFieldAbsoluteValue(field, valueToWrite);
    }


    /*********************** Field Listeners ***********************/
    public void startListeners() {

        startAmmoListener();
        startDefuserListener();
        startKeysListener();
        startLifeListener();
        startMinesListener();
        startScoreListener();
        startSpecialKeyListener();
        startEnemyExplodeListener();
    }
    public void stopListeners(){
        if(ammoListener != null)
            ammoDocRef.removeEventListener(ammoListener);
        if(defusersListener != null)
            defusersDocRef.removeEventListener(defusersListener);
        if(keysListener != null)
            keysDocRef.removeEventListener(keysListener);
        if(lifeListener != null)
            lifeDocRef.removeEventListener(lifeListener);
        if(minesListener != null)
            minesDocRef.removeEventListener(minesListener);
        if(scoreListener != null)
            scoreDocRef.removeEventListener(scoreListener);
        if(specialKeysListener != null)
            specialKeysRef.removeEventListener(specialKeysListener);
        if(enemyInjuredListener != null)
            enemyInjuredDocRef.removeEventListener(enemyInjuredListener);
    }

    public void startAmmoListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ammoDocRef = database.getReference(firebaseLiveGameInfoPath + "ammo");
        ammoListener = ammoDocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assert(dataSnapshot.getKey().equals("ammo"));
                //Get new value
                Integer newAmmo = dataSnapshot.getValue(Integer.class);
                //keep local copy updated
                _livePlayerInfo.setAmmo(newAmmo);

                final boolean isAmmoZero = newAmmo.equals(0);

                // update UI text
                String newAmmoText = isAmmoZero ? "No Ammo left" : ("Ammo: - " + (newAmmo.toString()));
                _txtAmmo.setText(newAmmoText);


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // enable / disable shoot button according if there is any ammo left
                        _isShootBtnEnabled = !isAmmoZero;
                        _btnShot.setEnabled(_isShootBtnEnabled);

                    }
                }, InGameConstants.LaserHitBtnDelay * 1000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());

            }
        });
    }

    public void startDefuserListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        defusersDocRef = database.getReference(firebaseLiveGameInfoPath + "defuser");
        defusersListener = defusersDocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assert(dataSnapshot.getKey().equals("defuser"));
                //Get new value
                Integer newDefuser = dataSnapshot.getValue(Integer.class);
                //keep local copy updated
                _livePlayerInfo.setDefuser(newDefuser);
                _txtDefuse.setText("Defuse: " + newDefuser.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public void startKeysListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        keysDocRef = database.getReference(firebaseLiveGameInfoPath + "keys");
        keysListener = keysDocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assert(dataSnapshot.getKey().equals("keys"));
                //Get new value
                Integer newkeys = dataSnapshot.getValue(Integer.class);
                //keep local copy updated
                _livePlayerInfo.setKeys(newkeys);
                _txtKeys.setText("Keys:" + newkeys.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public void startLifeListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        lifeDocRef = database.getReference(firebaseLiveGameInfoPath + "life");
        lifeListener = lifeDocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assert(dataSnapshot.getKey().equals("life"));
                //Get new value
                Integer newLife = dataSnapshot.getValue(Integer.class);
                //keep local copy updated
                _livePlayerInfo.setLife(newLife);
                _txtLife.setText("Life: " + newLife.toString());

                if(newLife <= 0) {
                    Integer endGameCode = (InGameConstants.winnerCauseReachPlayerDied) + ((_playerId == 1) ? 2 : 1);
                    mDatabase.child("LiveGameinfo").child("gameEnd").setValue(endGameCode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public void startMinesListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        minesDocRef = database.getReference(firebaseLiveGameInfoPath + "mines");
        minesListener = minesDocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assert(dataSnapshot.getKey().equals("mines"));
                //Get new value
                Integer newMines = dataSnapshot.getValue(Integer.class);
                //keep local copy updated
                _livePlayerInfo.setMines(newMines);
                _txtMines.setText("Mines: " + newMines.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public void startScoreListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        scoreDocRef = database.getReference(firebaseLiveGameInfoPath + "score");
        scoreListener = scoreDocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assert(dataSnapshot.getKey().equals("score"));
                //Get new value
                Integer newScore = dataSnapshot.getValue(Integer.class);
                //keep local copy updated
                _livePlayerInfo.setScore(newScore);
                _txtScore.setText("Score: " + newScore.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public void startSpecialKeyListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        specialKeysRef = database.getReference(firebaseLiveGameInfoPath + "specialKeys");
        specialKeysListener = specialKeysRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assert(dataSnapshot.getKey().equals("specialKeys"));
                //Get new value
                Integer newSpecialKeys = dataSnapshot.getValue(Integer.class);
                //keep local copy updated
                _livePlayerInfo.setSpecialKeys(newSpecialKeys);
                _txtSpecialKey.setText("Special Keys: " + newSpecialKeys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    /*********************** Stats Listeners ***********************/
    public void startEnemyExplodeListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        enemyInjuredDocRef = database.getReference(firebaseEnemyInjuredPath);
        enemyInjuredListener = enemyInjuredDocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("enemyExplode").getValue(Integer.class) == 1) {
                    mDatabase.child(firebaseEnemyInjuredPath).child("enemyExplode").setValue(0);
                    Toast.makeText(_context, "Your enemy exploded", Toast.LENGTH_SHORT).show();
                    updateFieldRelativeValue(LiveGameInfoField.SCORE, InGameConstants.addPointDueExplosion);
                    totalMineHits++;
                }
                else if(dataSnapshot.child("enemyHit").getValue(Integer.class) == 1) {
                    mDatabase.child(firebaseEnemyInjuredPath).child("enemyHit").setValue(0);
                    Toast.makeText(_context, "You hit the enemy, Nice shot comrade", Toast.LENGTH_SHORT).show();
                    updateFieldRelativeValue(LiveGameInfoField.SCORE, InGameConstants.addPointDueHit);
                    totalShotsHits++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    /*********************** Stats updaters ***********************/
    public void addSingleShotFired() {
        totalShotsFired++;
    }

    public void addShotInjured() {
        totalShotsInjured++;
    }

    public void addMinePlanted() {
        totalMinePlanted++;
    }

    public void addMineInjured() {
        totalMineInjured++;
    }

    public void addKeyUsed() {
        totalKeyUsed++;
    }

    public Integer getScore() {
        return _livePlayerInfo.getScore();
    }

    public boolean isShotBtnEnabled() {
        return _isShootBtnEnabled;
    }

    /*********************** Finalize Game ***********************/

    public Map<String, Object> toMap(boolean isWin, double timeleft, String playerUid, Integer winnerCause){
        Map<String, Object> docData = new HashMap<>();
        Double num = (timeleft)/1000;
        docData.put("ID",playerUid);
        StatusGame statusGame = isWin ? StatusGame.WIN : StatusGame.LOSE;
        docData.put("STATUS-END-OF-GAME", statusGame);
        docData.put("LIFE-END-OF-GAME", _livePlayerInfo.getLife());
        docData.put("AMMO-END-OF-GAME", _livePlayerInfo.getAmmo());
        docData.put("TIME-LEFT-IN-SEC",  num);
        docData.put("TYPE-END-OF-GAME", gameType);
        docData.put("POINTS-END-OF-GAME", _livePlayerInfo.getScore());
        docData.put("MINES-END-OF-GAME", _livePlayerInfo.getMines());
        docData.put("DEFUSE-END-OF-GAME", _livePlayerInfo.getDefuser());
        docData.put("KEYS-END-OF-GAME", _livePlayerInfo.getKeys());
        //TODO - need to add more fields and maybe fix existing, check all _total... update collectStatsAndPopFinalGameScreen also
        docData.put("NUM-HITS-END-OF-GAME", totalShotsHits);
        docData.put("NUM-SHOTS-END-OF-GAME", totalShotsFired);
        docData.put("NUM-BOMB-END-OF-GAME", totalMinePlanted);

        Integer reachedEnd = (isWin && winnerCause.equals(InGameConstants.winnerCauseReachEndTag)) ? 1 : 0;
        docData.put("FLAG", reachedEnd);
        return docData;
    }

    public void readMyStats(final Integer time, final boolean state){

        final FirebaseFirestore db =FirebaseFirestore.getInstance();
        final String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        final DocumentReference documentReference = db.collection("PlayerStats").document(uid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot queryDocumentSnapshots) {
                TransfromerData fromFirebase = queryDocumentSnapshots.toObject(TransfromerData.class);
                fromFirebase.setparams(time,totalShotsHits,_livePlayerInfo.getScore(),totalShotsFired,state);
                db.collection("PlayerStats").document(uid).set(fromFirebase.tomap());
            }
        });
    }



    }
