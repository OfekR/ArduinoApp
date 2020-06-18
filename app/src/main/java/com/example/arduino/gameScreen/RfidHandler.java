package com.example.arduino.gameScreen;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.arduino.R;
import com.example.arduino.defines.InGameConstants;
import com.example.arduino.defines.LogDefs;
import com.example.arduino.utilities.MediaPlayerWrapper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import io.github.controlwear.virtual.joystick.android.JoystickView;

/****
 * This class handle all the rfid reading from cloud - and also update game UI accordingly
 * Firebase responsible for: RfidReading
 */
public class RfidHandler {
    // Constant
    private static final String TAG = LogDefs.tagRFID;
    final String firebaseRfidPath;
    private MediaPlayerWrapper mySong;

    // rfid listeners
    private ChildEventListener barrierListener;
    private ChildEventListener lootBoxListener;
    private ChildEventListener mineListener;
    private ValueEventListener gotHitListener;

    private DatabaseReference barrierDocRef;
    private DatabaseReference lootBoxDocRef;
    private DatabaseReference mineDocRef;
    private DatabaseReference gotHitDocRef;

    // Game screen variables
    private Button _btnMine;
    private Button _btnKey;
    private Button _btnShot;
    private JoystickView _joystickCar;
    private JoystickView _joystickServo;
    private LiveGameInfo _liveGameInfo;

    private int _playerId;
    private String _playerIdStr;
    private Context _context;


    // current status
    public String currentBarrierName;
    public String currentMineName;

    DatabaseReference mDatabase;

    public RfidHandler(Context context, DatabaseReference mDatabaseRef,int playerId, Button btnMine, Button btnKey,Button btnShot,
                       JoystickView joystickCar, JoystickView joystickServo, LiveGameInfo liveGameInfo) {
        _btnMine = btnMine;
        _btnKey = btnKey;
        _btnShot = btnShot;
        _joystickCar = joystickCar;
        _joystickServo = joystickServo;
        _liveGameInfo = liveGameInfo;

        _playerId = playerId;
        _playerIdStr = (_playerId == 1 ? "Player1" : "Player2");
        _context = context;
        firebaseRfidPath = "RfidReading/" + _playerIdStr + "/";

        currentBarrierName = "";
        currentMineName = "";

        mDatabase = mDatabaseRef;

    }


    public void resetAllRfidValues() {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("Barrier/OpenBarrier", "0");
        hashMap.put("Barrier/OpenBarrierSpecial", 0);

        hashMap.put("LootBox/PickedLootbox", "0");
        hashMap.put("LootBox/PickedSpecialLootbox", 0);

        hashMap.put("Mine/Disarm", 0);
        hashMap.put("Mine/Explode", 0);
        hashMap.put("Mine/PlaceMine", "0");

        hashMap.put("gotHit", 0);

        mDatabase.child(firebaseRfidPath).updateChildren(hashMap);
    }

    public void startListeners() {
        startBarrierListener();
        startMineListener();
        startLootboxListener();
        startGotHitListener();
    }

    //TODO - call this
    public void stopListeners() {
        if(barrierListener != null)
            barrierDocRef.removeEventListener(barrierListener);
        if(mineListener != null)
            mineDocRef.removeEventListener(mineListener);
        if(lootBoxListener != null)
            lootBoxDocRef.removeEventListener(lootBoxListener);
        if(gotHitListener != null)
            gotHitDocRef.removeEventListener(gotHitListener);
    }

    private void startBarrierListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        barrierDocRef = database.getReference(firebaseRfidPath+"Barrier/");
        barrierListener = barrierDocRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                if (key.equals("OpenBarrier")) {
                    //regular barrier
                    String oldName = currentBarrierName;
                    currentBarrierName = dataSnapshot.getValue(String.class);
                    //set barrier to received uid or if close, reset to 0
                    if(currentBarrierName.equals("0")) {
                        //disable barrier
                        Log.d(TAG,LogDefs.OpenBarrierDisabled + oldName);
                        if(!oldName.equals("99"))
                        {
                            Toast.makeText(_context, "Barrier no longer in distance to be opened", Toast.LENGTH_SHORT).show();
                        }
                        //else last read had no keys, no need to notify user
                        _btnKey.setEnabled(false);
                    }
                    else if(currentBarrierName.equals("99")) {
                        //No keys
                        Log.d(TAG,LogDefs.OpenBarrierNoKeys);
                        Toast.makeText(_context, "Keys required to open barrier", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.d(TAG,LogDefs.OpenBarrierEnabled + currentBarrierName);
                        _btnKey.setEnabled(true);
                        Toast.makeText(_context, "Barrier available to be opened", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(key.equals("OpenBarrierSpecial")) {
                    Integer value = dataSnapshot.getValue(Integer.class);
                    if(value.equals(1)) {
                        Log.d(TAG,LogDefs.OpenBarrierSpecial);
                        _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.SCORE, InGameConstants.AddPointsDueSpecialBarrierOpen);
                        Toast.makeText(_context, "Special Barrier Opened", Toast.LENGTH_SHORT).show();

                        if(mySong !=  null) mySong.Destroy();
                        mySong = new MediaPlayerWrapper(R.raw.special_barrier, _context);
                        mySong.StartOrResume();

                    }
                    else if(value.equals(99)) {
                        Log.d(TAG,LogDefs.OpenBarrierSpecialNoKeys);
                        Toast.makeText(_context, "Special keys required to open special barrier", Toast.LENGTH_LONG).show();
                    }
                    else if(value.equals(9)) {
                        Log.d(TAG,LogDefs.OpenBarrierSpecialOppents);
                        Toast.makeText(_context, "Can't open barrier, this is opponent barrier", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Log.e(TAG, "Barrier in RfidReading have unknown child");
                    assert(false);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void disableEnableButtons(boolean isEnable)
    {
        //TODO OFEK - verify when joystick disabled, it's really disabled

        // shouldn't enable shot automaticaly because maybe no shoots left
        boolean isEnableShot = isEnable && _liveGameInfo.isShotBtnEnabled();

        _btnShot.setEnabled(isEnableShot);
        _joystickCar.setEnabled(isEnable);
        _joystickServo.setEnabled(isEnable);
    }

    private void startMineListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mineDocRef = database.getReference(firebaseRfidPath+"Mine/");
        mineListener = mineDocRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                if (key.equals("PlaceMine")) {
                    String oldName = currentMineName;
                    currentMineName = dataSnapshot.getValue(String.class);
                    //set mine according to recieved value
                    if(currentMineName.equals("0")) {
                        //disable mine button
                        Log.d(TAG,LogDefs.PlaceMineDisabled + oldName);
                        if(!oldName.equals("99"))
                        {
                            Toast.makeText(_context, "Mine pit no longer in distance to place mine", Toast.LENGTH_SHORT).show();
                        }
                        //else last read had no mine, no need to notify user
                        _btnMine.setEnabled(false);
                    }
                    else if(currentMineName.equals("99")) {
                        //No keys
                        Log.d(TAG,LogDefs.PlaceMineNoMines);
                        Toast.makeText(_context, "Mines required to place mines", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.d(TAG,LogDefs.PlaceMineEnabled + currentMineName);
                        _btnMine.setEnabled(true);
                        Toast.makeText(_context, "Mine available to be placed", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(key.equals("Disarm")) {
                    Integer value = dataSnapshot.getValue(Integer.class);
                    if(value != 1) {
                        return;
                    }
                    Log.d(TAG,LogDefs.DisarmMines);
                    Toast.makeText(_context, "Mine was disarmed!!!", Toast.LENGTH_SHORT).show();
                    mDatabase.child("RfidReading").child(_playerIdStr).child("Mine").child("Disarm").setValue(0);
                    _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.SCORE, InGameConstants.AddPointDueMineDefusion);
                }
                else if(key.equals("Explode")) {
                    Integer value = dataSnapshot.getValue(Integer.class);
                    if(value != 1) {
                        return;
                    }

                    Log.d(TAG,LogDefs.ExplodeMine);
                    Toast.makeText(_context, "Mine exploded!!! Car is paralyzed for " + InGameConstants.ExplodeCarParalyzed.toString() + " seconds", Toast.LENGTH_LONG).show();
                    if(mySong !=  null) mySong.Destroy();
                    mySong = new MediaPlayerWrapper(R.raw.mineexplode, _context);
                    mySong.StartOrResume();


                    stopCar();
                    // update stats
                    _liveGameInfo.addMineInjured();
                    _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.SCORE, InGameConstants.ReducePointDueExplosion);
                    _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.LIFE, InGameConstants.ReduceLifeDueExplosion);

                    //restore control after ExplodeCarDelay
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            restoreCar();
                            mDatabase.child("RfidReading").child(_playerIdStr).child("Mine").child("Explode").setValue(0);
                            Toast.makeText(_context, "Smoke is clear - car systems are back online", Toast.LENGTH_LONG).show();
                        }
                    }, InGameConstants.ExplodeCarParalyzed * 1000);

                }
                else {
                    Log.e(TAG, "Mine in RfidReading have unknown child");
                    assert(false);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }


    private void startLootboxListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        lootBoxDocRef = database.getReference(firebaseRfidPath+"LootBox/");
        lootBoxListener = lootBoxDocRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final Integer waitTimeMs = 10 * 1000;
                String key = dataSnapshot.getKey();
                if (key.equals("PickedLootbox")) {
                    //regular lootbox
                    String giftCode = dataSnapshot.getValue(String.class);
                    Integer giftNum = giftCode.charAt(0) - '0';
                    if(giftNum.equals(0)) {
                        //left lootbox - nothing to do
                        return;
                    }
                    else if(giftNum.equals(9)) {
                        Log.d(TAG,LogDefs.LootBoxUnavailable);
                        Toast.makeText(_context, "LootBox unavailable, Drive away and try again later", Toast.LENGTH_SHORT).show();
                        mDatabase.child("RfidReading").child(_playerIdStr).child("LootBox").child("PickedLootbox").setValue("0");

                    }
                    else if(giftNum > 0 && giftNum < 7) {
                        String giftName = "";
                        switch (giftNum) {
                            case 1:
                                giftName = "Key";
                                break;
                            case 2:
                                giftName = "Ammo";
                                break;
                            case 3:
                                giftName = "Mine";
                                break;
                            case 4:
                                giftName = "DisarmMine";
                                break;
                            case 5:
                                giftName = "Points";
                                break;
                            case 6:
                                giftName = "Life";
                                break;
                        }
                        Log.d(TAG,LogDefs.LootBoxGot + giftName);
                        Toast.makeText(_context, "You found " + giftName + " in the loot box", Toast.LENGTH_SHORT).show();
                        //indicate lootbox was picked
                        mDatabase.child("RfidReading").child(_playerIdStr).child("LootBox").child("PickedLootbox").setValue("0");

                        if(mySong !=  null) mySong.Destroy();
                        mySong = new MediaPlayerWrapper(R.raw.lootbox, _context);
                        mySong.StartOrResume();


                        //turn lootbox on again after delay
                        final String lootBoxName = giftCode.substring(1,3);

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // lootbox will be turned on after waitTimeMs ms
                                mDatabase.child("Lootbox").child(lootBoxName).child("status").setValue(0);
                            }
                        }, waitTimeMs);
                    }
                }
                else if(key.equals("PickedSpecialLootbox")) {
                    Integer value = dataSnapshot.getValue(Integer.class);
                    if(value.equals(1)) {
                        Log.d(TAG,LogDefs.LootBoxSpecialGot);
                        Toast.makeText(_context, "Special Key acquired, drive for your glorious victory!", Toast.LENGTH_SHORT).show();
                        _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.SCORE, InGameConstants.AddPointDueSpecialKeyAcquire);
                        if(mySong !=  null) mySong.Destroy();
                        mySong = new MediaPlayerWrapper(R.raw.special_lootbox, _context);
                        mySong.StartOrResume();
                    }
                    else if(value.equals(99)) {
                        Log.d(TAG,LogDefs.LootBoxSpecialUnavailable);
                        Toast.makeText(_context, "Already opened this special loot box", Toast.LENGTH_SHORT).show();
                        mDatabase.child("RfidReading").child(_playerIdStr).child("LootBox").child("PickedSpecialLootbox").setValue(1);
                    }
                    else if(value.equals(9)) {
                        //TODO Extra - there is a bug here in case PickedSpecialLootbox was equal to 1 before,
                        //             not really important because pretty stupid after you picked your special you will go back to ur place
                        //             can be fixed easily by doing 2 values in cloud (8/9) and etc
                        Log.d(TAG,LogDefs.LootBoxSpecialOppents);
                        Toast.makeText(_context, "Can't open loot box, this is opponent loot box", Toast.LENGTH_LONG).show();
                        mDatabase.child("RfidReading").child(_playerIdStr).child("LootBox").child("PickedSpecialLootbox").setValue(0);
                    }

                }
                else {
                    Log.e(TAG, "Lootbox in RfidReading have unknown child");
                    assert(false);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }


    public void startGotHitListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        gotHitDocRef = database.getReference(firebaseRfidPath + "gotHit");
        gotHitListener = gotHitDocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assert(dataSnapshot.getKey().equals("gotHit"));
                //Get new value
                Integer hitValue = dataSnapshot.getValue(Integer.class);
                if(hitValue == 1) {
                    Log.d(TAG,LogDefs.gotHit);
                    Toast.makeText(_context, "You got hit!!! Car is paralyzed for " + InGameConstants.LaserHitParalyzed.toString() + " seconds", Toast.LENGTH_LONG).show();
                    if(mySong !=  null) mySong.Destroy();
                    mySong = new MediaPlayerWrapper(R.raw.boom, _context);
                    mySong.StartOrResume();


                    stopCar();

                    // update stats
                    _liveGameInfo.addShotInjured();
                    _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.SCORE, InGameConstants.ReducePointDueHit);
                    _liveGameInfo.updateFieldRelativeValue(LiveGameInfoField.LIFE, InGameConstants.ReduceLifeDueHit);

                    //restore control after ExplodeCarDelay
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after waitTimeMs ms
                            restoreCar();
                            mDatabase.child("RfidReading").child(_playerIdStr).child("gotHit").setValue(0);
                            Toast.makeText(_context, "Car has recovered - car systems are back online", Toast.LENGTH_LONG).show();
                        }
                    }, InGameConstants.ExplodeCarParalyzed * 1000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public boolean planetMineInCloud() {
        // save value locally to avoid async race condition
        final String currentMineNameFrezze = currentMineName;
        if(currentMineNameFrezze.charAt(0) != 'M') {
            Toast.makeText(_context, "Mine couldn't be placed, as you got away from mine pit", Toast.LENGTH_LONG).show();
            return false;
        }
        Toast.makeText(_context, "Mine is placed, you have "+ InGameConstants.PlaceMineDelay.toString() + " seconds to run for your life", Toast.LENGTH_LONG).show();
        currentMineName = "";
        _btnMine.setEnabled(false);

        // set a delay on mine setting to allow user to get away from mine
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after waitTimeMs ms
                mDatabase.child("Mines").child(currentMineNameFrezze).child("status").setValue(1);
                if(mySong !=  null) mySong.Destroy();
                mySong = new MediaPlayerWrapper(R.raw.mineplanted, _context);
                mySong.StartOrResume();
                Toast.makeText(_context, "Mine has been planted", Toast.LENGTH_LONG).show();
            }
        }, InGameConstants.PlaceMineDelay);

        return true;
    }

    public boolean openBarrierInCloud() {
        // save value locally to avoid async race condition
        final String currentBarrierNameFrezze = currentBarrierName;
        if(currentBarrierNameFrezze.charAt(0) != 'B') {
            Toast.makeText(_context, "Barrier couldn't be opened, as you got away from barrier", Toast.LENGTH_LONG).show();
            return false;
        }
        Toast.makeText(_context, "Barrier is opening", Toast.LENGTH_SHORT).show();
        mDatabase.child("Barriers").child(currentBarrierNameFrezze).child("status").setValue(1);

        currentMineName = "";
        _btnKey.setEnabled(false);
        return true;
    }


    private void stopCar() {
        //TODO - need to send BT command of stop car
        //TODO Maybe - cause car to blink (via BT or just let car listen to explode variable)

        disableEnableButtons(false);
    }

    private void restoreCar() {
        disableEnableButtons(true);
    }


}
