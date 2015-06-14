package com.example.acosgun.tcp_nfc_ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {

    private static final String TAG= "MainActivity";
    private TCPClient  tcp_client;

    private static final int PORT = 45454;
    private static final String IP_ADDRESS= "192.168.0.127";
    //private static final String IP_ADDRESS= "127.0.0.1";

    public Handler handler;
    Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setConnectButtonColor(Color.RED);
        handler = new Handler() {
          @Override
          public void handleMessage(Message msg) {
              //Log.d(TAG, "handleMessage: " + msg);
              String stringMessage = msg.getData().getString("message");
              boolean booleanConnected = msg.getData().getBoolean("connected");
              if(booleanConnected) {
                  setConnectButtonColor(Color.GREEN);
                  if(stringMessage!=null) {
                      Log.d(TAG, "Msg: " + stringMessage);
                  }
              }
              else {
                  setConnectButtonColor(Color.RED);
              }

          }
        };


        tcp_client = new TCPClient(handler, IP_ADDRESS, PORT);
        mThread = new Thread(tcp_client);
        mThread.start();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    void setConnectButtonColor (final int color) {

        runOnUiThread(new Runnable() {

        @Override
        public void run() {
            Button connect_button = (Button) findViewById(R.id.connect_button);
            connect_button.setBackgroundColor(color);
        }

        });
    }

    public void onClickSend (View view) {
        if(!tcp_client.isRunning())
            return;
        String str = "g";
        tcp_client.sendMessage(str);
    }

    public void onClickConnect (View view) {
        Button connect_button= (Button) findViewById(R.id.connect_button);
        ColorDrawable buttonColor = (ColorDrawable) connect_button.getBackground();
        int colorId = buttonColor.getColor();
        if (colorId == Color.RED) {
            if(!tcp_client.isRunning())
                mThread.run();
        } else {
            tcp_client.disconnect();
        }


    }


}
