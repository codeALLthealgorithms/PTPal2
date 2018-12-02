package com.ptpal.ptpal.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.*;

//package Android.Arduino.Bluetooth;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.util.Log;

public class SessionActivity extends Activity
{
    TextView myLabel;
    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    volatile boolean stopWorker;
    TextView read3Message;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int test;
    String out;
    EditText height;
    Button genderFemale;
    Button genderMale;
    int gender;
    double heightcm = 0;
    double demispan = 0;
    TextView results;
    Button calibrate;
    int runs = 0;

    //////  VALUES  /////
    double accelX;
    double accelY;
    double accelZ;
    double distance;
    double realAccelMag;
    double initAccelX;
    double initAccelY;
    double initAccelZ;
    double initAccelMag;
    double initDist;



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        height = (EditText)findViewById(R.id.userHeight);
        genderFemale = (Button) findViewById(R.id.female);
        genderMale = (Button) findViewById(R.id.male);
        results = (TextView) findViewById(R.id.results);
        calibrate = (Button) findViewById(R.id.calibrate);



        genderFemale.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gender = 0;
                String temp = height.getText().toString();
                heightcm = Double.parseDouble(temp);
            }
        });

        genderMale.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gender = 1;
                String temp = height.getText().toString();
                heightcm = Double.parseDouble(temp);
            }
        });

        calibrate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(runs == 1)
                {
                    gender = 1;
                    String temp = height.getText().toString();
                    heightcm = Double.parseDouble(temp);
                }

            }
        });

        Button openButton = (Button)findViewById(R.id.open);
        Button sendButton = (Button)findViewById(R.id.send);
        Button closeButton = (Button)findViewById(R.id.close);
        myLabel = (TextView)findViewById(R.id.label);
        myTextbox = (EditText)findViewById(R.id.entry);
        read3Message = (TextView) findViewById(R.id.textHere);
        test = 0;
        out = "";


        //Open Button
        openButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    findBT();
                    openBT();
                }
                catch (IOException ex)
                {

                }
            }
        });

        //Close button
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    closeBT();
                }
                catch (IOException ex) { }
            }
        });
    }

    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            myLabel.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("RNBT-80D7"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found");
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        myLabel.setText("Bluetooth Opened");
    }

    void beginListenForData()
    {
//        new Handler(Looper.getMainLooper()).post(new Runnable(){
//            byte[] buffer = new byte[1024];
//            int bytes;
//            String readMessage = "hello";
//            @Override
//            public void run()
//            {
//                while (true) {
//                    try {
//                        bytes = mmInputStream.read(buffer);            //read bytes from input buffer
//                        readMessage = new String(buffer, 0, bytes);
//                        // Send the obtained bytes to the UI Activity via handler
//                        Log.i("logging", readMessage + "");
//                        read3Message.setText(readMessage);
//                    } catch (IOException e) {
//                        break;
//                    }
//                }
//            }
//        });
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    //Log.i("logging", data + "");
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            runs = 1;
                                            if(test < 30)
                                            {
                                                out = out + data + "\n";
                                                test++;
                                                if(test == 10)
                                                {
                                                    read3Message.setText(out);
                                                    //Log.d("myTag", out);

                                                    String values[] = out.split(":");

                                                    accelX = Double.parseDouble(values[0]);
                                                    accelY = Double.parseDouble(values[1]);
                                                    accelZ = Double.parseDouble(values[2]);
                                                    distance = Double.parseDouble(values[3]);
                                                    realAccelMag = Double.parseDouble(values[4]);
                                                    initAccelX = Double.parseDouble(values[5]);
                                                    initAccelY = Double.parseDouble(values[6]);
                                                    initAccelZ = Double.parseDouble(values[7]);
                                                    initAccelMag = Double.parseDouble(values[8]);
                                                    initDist = Double.parseDouble(values[9]);
                                                    

                                                    test = 0;
                                                    out = "";
                                                    if(heightcm > 0)
                                                    {
                                                        if(gender == 0)
                                                        {
                                                            demispan = (heightcm - 60.1) / 1.35;
                                                        }
                                                        if(gender == 1)
                                                        {
                                                            demispan = (heightcm - 57.8) / 1.40;
                                                        }
                                                        results.setText(Double.toString(demispan));
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }
}

