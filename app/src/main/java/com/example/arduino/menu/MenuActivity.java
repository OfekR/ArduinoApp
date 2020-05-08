package com.example.arduino.menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.arduino.R;
import com.example.arduino.defines.LogDefs;
import com.example.arduino.initGame.InitGameActivity;
import com.example.arduino.loby.LobyActivity;
import com.example.arduino.loby.PopWindow;
import com.example.arduino.stats.StatitacsActivity;
import com.example.arduino.ui.login.LoginActivity;
import com.example.arduino.utilities.HttpHelper;
import com.example.arduino.utilities.MediaPlayerWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MenuActivity extends AppCompatActivity {
    private static final String TAG = LogDefs.tagMenu;


    Button joinBtn;
    Button startButton;
    Button statBtn;
    Button logOut;
    Button moveBt;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mDatabase;
    private BluetoothSocket bluetoothSocket;
    private final String DEVICE_ADDRESS = "98:D3:51:FD:D9:45"; //MAC Address of Bluetooth Module
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        moveBt = findViewById(R.id.controller);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        startButton = findViewById(R.id.start_btn);
        moveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(ControllerActvivty.class);
            }
        });
        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                changeScreen(InitGameActivity.class);

            }
        });


        logOut = findViewById(R.id.logout_btn);
        logOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                SignOut();
            }
        });
        joinBtn = findViewById(R.id.join_btn);
        joinBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                checkforJoin();
                }
        });

        statBtn = findViewById(R.id.Stat_btn);
        statBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                changeScreen(StatitacsActivity.class);
            }
        });

    }

    private void checkforJoin() {
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        DocumentReference docRef = fstore.collection("Appending").document("Append1");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot= task.getResult();
                    String valid_join = snapshot.getString("waitTojoin");
                    // antoher player is already wating
                    if (valid_join.equals("WAITING")) {
                        Log.v("MENU-CLASS", "SOMEONE - IS ALREADY WAITS");
                        Toast.makeText(MenuActivity.this, "SOMEONE - IS ALREADY WAITS",
                                Toast.LENGTH_SHORT).show();


                    }
                    // you can join game
                    else if (valid_join.equals("NO-ONE-IS-WAITING")) {
                        HttpHelper httpHelper = new HttpHelper();
                        httpHelper.HttpRequestForLooby("WAITING", "https://us-central1-arduino-a5968.cloudfunctions.net/addJoin");
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("playerId2",FirebaseAuth.getInstance().getUid());
                        mDatabase.child("GameSettings").updateChildren(hashMap);
                        Intent intent = new Intent(getApplicationContext(),LobyActivity.class);
                        intent.putExtra("Classifier", "Join");
                        startActivity(intent);
                    }
                    // check for athoer senrio if we want 3 state
                    else {

                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
            mAuthStateListener = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setupFirebaseListener();
    }

    @Override
    public void onResume() {
        super.onResume();
    }



    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int optionChoosen) {
                switch (optionChoosen){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        SignOut();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked - nothing to do
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to sign out?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    /*  Private Methods */

    private void SignOut() {
        Log.d(TAG,LogDefs.signoutMsg);
        //no need to listen anymore, because user signed out
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        mAuthStateListener = null;

        //sign out
        FirebaseAuth.getInstance().signOut();

        Toast.makeText(MenuActivity.this, "User signed out",
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        startActivity(intent);
    }

    //set listener to follow if user is logged in or not
    private void setupFirebaseListener(){
        if(mAuthStateListener != null) {
            //already initialized
            return;
        }
        Log.d(TAG, LogDefs.setupFirebaseListener);

        mAuthStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG,LogDefs.userSignedInIs + user.getUid());
                }
                else {
                    SignOut();

                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    public void changeScreen(Class screen){
        Intent intent = new Intent(this, screen);
        startActivity(intent);
    }

    //Initializes bluetooth module
    public boolean BTinit()
    {
        boolean found = false;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null) //Checks if the device supports bluetooth
        {
            Toast.makeText(getApplicationContext(), "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
        }

        if(!bluetoothAdapter.isEnabled()) //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter,0);

            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        if(bondedDevices.isEmpty()) //Checks for paired bluetooth devices
        {
            Toast.makeText(getApplicationContext(), "Please pair the device first", Toast.LENGTH_SHORT).show();
        }
        else
        {
            for(BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    public boolean BTconnect()
    {
        boolean connected = true;

        try
        {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //Creates a socket to handle the outgoing connection
            socket.connect();

            Toast.makeText(getApplicationContext(),
                    "Connection to bluetooth device successful", Toast.LENGTH_LONG).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            connected = false;
        }

        if(connected)
        {
            try
            {
                outputStream = socket.getOutputStream(); //gets the output stream of the socket
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return connected;
    }


}
