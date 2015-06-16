package com.example.acosgun.tcp_nfc_ui;

import android.content.Intent;
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



public class MainActivity extends ActionBarActivity implements CardReaderFragment.NfcReaderInputListener {

    private static final String TAG= "MainActivity";
    private TCPClient  tcp_client;

    private static final int PORT = 4545;
    private static final String IP_ADDRESS= "192.168.1.8";
    //private static final String IP_ADDRESS= "127.0.0.1";

    public Handler tcp_handler;
    Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setConnectButtonColor(Color.RED);


        if (savedInstanceState == null) {
            //FragmentManager fragmentManager = getFragmentManager();
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            CardReaderFragment cardFragment = new CardReaderFragment();
            fragmentTransaction.add(cardFragment,TAG);
            fragmentTransaction.commit();
        }

        tcp_handler = new Handler() {
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


        tcp_client = new TCPClient(tcp_handler, IP_ADDRESS, PORT);
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

    private boolean sendGuideCommand () {
        if(!tcp_client.isRunning())
            return false;
        String str = "g";
        tcp_client.sendMessage(str);
            return true;
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

    public void elevatorButtonClicked(View v){
        int floorNum;

        switch(v.getId()) {
            case R.id.button1:
                floorNum = 1;
                break;
            case R.id.button2:
                floorNum = 2;
                break;
            case R.id.button3:
                floorNum = 3;
                break;
            case R.id.button4:
                floorNum = 4;
                break;
            case R.id.button5:
                floorNum = 5;
                break;
            case R.id.button6:
                floorNum = 6;
                break;
            case R.id.button7:
                floorNum = 7;
                break;
            case R.id.button8:
                floorNum = 8;
                break;
            case R.id.button9:
                floorNum = 9;
                break;
            case R.id.button10:
                floorNum = 10;
                break;
            case R.id.button11:
                floorNum = 11;
                break;
            case R.id.button12:
                floorNum = 12;
                break;
            case R.id.button13:
                floorNum = 13;
                break;
            case R.id.button14:
                floorNum = 14;
                break;
            case R.id.button15:
                floorNum = 15;
                break;
            case R.id.button16:
                floorNum = 16;
                break;
            default:
                floorNum = 0;
                break;
        }



        Intent intent = createElevatorIntent("Guest",floorNum, 4, getElevNum(floorNum), true);
        int requestCode = 1;
        startActivityForResult(intent, requestCode);
        overridePendingTransition(0,0);
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        // Collect data from the intent and use it
        if (resultCode == RESULT_CANCELED) {
            Log.i(TAG,"RESULT_CANCELED");
        } else if (resultCode == RESULT_OK) {
            Log.i(TAG,"RESULT_OK");
            sendGuideCommand();
        }
    }

    private Intent createElevatorIntent(String str, int floorNum, int duration, int elevNum, boolean guide_visible)
    {
        Intent intent = new Intent(this, CalledElevatorActivity.class);
        intent.putExtra("name",str);
        intent.putExtra("floor", floorNum);
        intent.putExtra("duration",duration);
        intent.putExtra("elevNum",elevNum);
        intent.putExtra("show_guide", guide_visible);
        return intent;
    }

    public int getElevNum(int floorNum) {
        int elev_num;
        if(floorNum >= 1 && floorNum <= 4) {
            elev_num = 1;
        } else if (floorNum >= 5 && floorNum <= 8) {
            elev_num = 2;
        } else if (floorNum >= 9 && floorNum <= 12) {
            elev_num = 3;
        } else {
            elev_num = 4;
        }
        return elev_num;
    }

    @Override
    public void nfcCallback(int floor, String name) {
        Log.i(TAG,"nfcCallback");
        Intent intent = createElevatorIntent(name,floor, 3, getElevNum(floor) , false);
        int requestCode = 1;
        startActivityForResult(intent, requestCode);
        overridePendingTransition(0,0);
    }

}
