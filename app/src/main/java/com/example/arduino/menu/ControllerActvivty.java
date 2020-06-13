package com.example.arduino.menu;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.example.arduino.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;


public class ControllerActvivty extends AppCompatActivity {

    private final String DEVICE_ADDRESS = "98:D3:51:FD:D9:45"; //MAC Address of Bluetooth Module
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    BluetoothAdapter mBluetoothAdapter;
    private byte[] buffer = new byte[256];
    private Map<Integer,Character> map_dictionray = new HashMap<>();


    Button forward_btn, forward_left_btn, forward_right_btn, reverse_btn, reverse_left_btn, reverse_right_btn, bluetooth_connect_btn;

    String command; //string variable that will store value to be transmitted to the bluetooth module

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_actvivty);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        makeConnectionForBluetooth();
        bluetooth_connect_btn = (Button) findViewById(R.id.bluetooth_connect_btn);
        JoystickView joystick = (JoystickView) findViewById(R.id.joysticktry);
        JoystickView joystick1 = (JoystickView) findViewById(R.id.joystickservo);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                try {
                    findCommand(angle,strength);
                  //  Double x = Math.abs((255/100)*strength*Math.cos(angle));
                   // Double y = Math.abs((255/100)*strength*Math.sin(angle));
                    char c = findCommand(angle,strength);
                    System.out.println("The-JoyStick angle -------> " + angle+ "and the Char i send is ---> " + c);
                    // System.out.println("x -------> " + send_x+ "y--------->  "+send_y);
                    outputStream.write(c); //transmits the value of command to the bluetooth
                  //  outputStream.write(send_y.byteValue()); //transmits the value of command to the bluetooth module

                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("The-JoyStick angle -------> " + angle+ "The- Strength--------->  "+strength);
            }
        });

        joystick1.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                try {
                    findCommand(angle,strength);
                    //  Double x = Math.abs((255/100)*strength*Math.cos(angle));
                    // Double y = Math.abs((255/100)*strength*Math.sin(angle));
                    char c = findCommandServo(angle,strength);
                    System.out.println("The-JoyStick angle -------> " + angle+ "and the Char i send is ---> " + c);
                    // System.out.println("x -------> " + send_x+ "y--------->  "+send_y);
                    outputStream.write(c); //transmits the value of command to the bluetooth
                    //  outputStream.write(send_y.byteValue()); //transmits the value of command to the bluetooth module

                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("The-JoyStick angle -------> " + angle+ "The- Strength--------->  "+strength);
            }
        });

        //Button that connects the device to the bluetooth module when pressed
        bluetooth_connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(BTinit())
                {
                    BTconnect();
                }

            }
        });

    }



    private char findCommandServo(int angle, int strength) {
        if((angle > 0 && angle < 10) || (angle >= 350 && angle <= 360)){
            return 'A';
        }
        else if((angle >= 10) && (angle < 30)){
            return 'B';
        }
        else if((angle >= 30) && (angle < 60)){
            return 'C';

        }
        else if((angle >= 60) && (angle < 80)){
            return 'D';

        }
        else if((angle >= 80) && (angle < 100)){
            return 'E';

        }
        else if((angle >= 100) && (angle < 130)){
            return 'F';

        }
        else if((angle >= 130) && (angle < 170)){
            return 'G';

        }
        else if((angle >= 170) && (angle < 190)){
            return 'H';

        }
        else if((angle >= 190) && (angle < 220)){
            return 'I';

        }
        else if((angle >= 220) && (angle < 260)){
            return 'J';

        }
        else if((angle >= 260) && (angle < 280)){
            return 'K';

        }
        else if((angle >= 280) && (angle < 320)){
            return 'L';

        }
        else if((angle >= 320) && (angle < 350)){
            return 'M';

        }
        else{
            return 'N';
        }

    }


    private char findCommand(int angle, int strength) {
        if((angle > 0 && angle < 10) || (angle >= 350 && angle <= 360)){
            return '1';
        }
        else if((angle >= 10) && (angle < 30)){
            return '2';
        }
        else if((angle >= 30) && (angle < 60)){
            return '3';

        }
        else if((angle >= 60) && (angle < 80)){
            return '4';

        }
        else if((angle >= 80) && (angle < 100)){
            return '5';

        }
        else if((angle >= 100) && (angle < 130)){
            return '6';

        }
        else if((angle >= 130) && (angle < 150)){
            return '7';

        }
        else if((angle >= 150) && (angle < 170)){
            return '8';

        }
        else if((angle >= 170) && (angle < 190)){
            return '9';

        }
        else if((angle >= 190) && (angle < 210)){
            return '0';

        }
        else if((angle >= 210) && (angle < 230)){
            return 'a';

        }
        else if((angle >= 230) && (angle < 250)){
            return 'b';

        }
        else if((angle >= 250) && (angle < 270)){
            return 'c';

        }
        else if((angle >= 270) && (angle < 290)){
            return 'd';

        }
        else if((angle >= 290) && (angle < 310)){
            return 'e';

        }
        else if((angle >= 310) && (angle < 330)){
            return 'f';

        }
        else if((angle >= 330) && (angle < 350)){
            return 'g';

        }
        else{
            return 'h';
        }

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

        if(bluetoothAdapter.isEnabled()) //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
        {

            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

            if (bondedDevices.isEmpty()) //Checks for paired bluetooth devices
            {
                Toast.makeText(getApplicationContext(), "Please pair the device first", Toast.LENGTH_SHORT).show();
            }
            else {
                for (BluetoothDevice iterator : bondedDevices) {
                    Toast.makeText(this,"The paired mac --  is "+ iterator.getAddress(),Toast.LENGTH_LONG).show();

                    if (iterator.getAddress().equals(DEVICE_ADDRESS)) {
                        device = iterator;
                        Toast.makeText(this,"The paired mac --  is "+ device.getAddress(),Toast.LENGTH_LONG).show();
                        found = true;
                        break;
                    }
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
                inputStream = socket.getInputStream();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return connected;
    }
    @Override
    protected void onStart()
    {
        super.onStart();
    }

    void makeConnectionForBluetooth() {
        if (mBluetoothAdapter.isEnabled()) //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
        {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice bt : pairedDevices) {
                if (bt.getName().equals("car1")) {
                    device = bt;
                    if (BTconnect()) {
                        Toast.makeText(this, "The-Connection-as Succeded", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "The-Connection-is Failed", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }


}
