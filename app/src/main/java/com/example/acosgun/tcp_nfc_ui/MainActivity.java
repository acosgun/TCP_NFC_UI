package com.example.acosgun.tcp_nfc_ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;


//import android.app.ActionBar.TabListener;
//import android.support.v7.app.ActionBar.TabListener;

public class MainActivity extends ActionBarActivity implements CardReaderFragment.NfcReaderInputListener {

    private static final String TAG= "MainActivity";
    public static final String PREFS_NAME = "MyPrefsFile";
    //private final Activity mActivity;

    private Menu mMenu;
    private Fragment mSettingsFragment;

    private TCPClient  tcp_client;

    private static final int PORT = 4545;
    private String IP_ADDRESS= "192.168.0.127";
    //private static final String IP_ADDRESS= "127.0.0.1";

    public Handler tcp_handler;
    Thread mThread;
    int last_elev_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar mActionBar = getSupportActionBar();
        //ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

        this.setConnectButtonColor(Color.RED);

        Button stopRobotButton= (Button) findViewById(R.id.stopRobotButton);
        stopRobotButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onClickStopRobot();
            }
        });

        Button baseButton= (Button) findViewById(R.id.baseButton);
        baseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onClickBase();
            }
        });

        Button followButton= (Button) findViewById(R.id.followButton);
        followButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onClickFollow();
            }
        });

        Button connectButton= (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onClickConnect();
            }
        });


        setStatusString("N/A");

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
              String stringMessage = msg.getData().getString("data");
              boolean booleanConnected = msg.getData().getBoolean("connected");
              if(booleanConnected) {
                  setConnectButtonColor(Color.GREEN);
                  if(stringMessage!=null) {
                      Log.d(TAG, "Msg: " + stringMessage);
                      setStatusString(stringMessage);
                  }
              }
              else {
                  setConnectButtonColor(Color.RED);
              }

          }
        };

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String ip_addy= settings.getString("ip_addy", IP_ADDRESS);
        IP_ADDRESS = ip_addy;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        restartTCPClient();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the mMenu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        this.mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                onClickSettings();
                return true;
            case R.id.action_main_menu:
                onClickMainMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void onClickSettings () {
        Log.i(TAG,"onClickSettings");
        setContentView(R.layout.activity_setup);


        EditText ipEditText= (EditText) findViewById(R.id.ip_editText);

        ipEditText.setText(IP_ADDRESS);


        ipEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do your stuff here

                    IP_ADDRESS = v.getText().toString();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("ip_addy", IP_ADDRESS);
                    editor.apply();

                    tcp_client.changeIP(IP_ADDRESS);
                    tcp_client.disconnect();
                }

                return false;
            }
        });
        ipEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideSoftKeyboard(v);
                }
            }
        });
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void restartTCPClient() {
        tcp_client = new TCPClient(tcp_handler, IP_ADDRESS, PORT);
        mThread = new Thread(tcp_client);
        mThread.start();
    }

    public void onClickMainMenu () {
        Log.i(TAG,"onClickMainMenu");
        setContentView(R.layout.activity_main);
    }
    public void onClickStopRobot () {
        sendGuideCommand("s");
    }
    public void onClickBase () {
        sendGuideCommand("b");
    }
    public void onClickFollow () {
        sendGuideCommand("f");
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



    public void setStatusString(String str) {
        TextView title_textView = (TextView) findViewById(R.id.title_text);
        title_textView.setText("Status: " + str);
    }

    private boolean sendGuideCommand (String str) {
        if(!tcp_client.isConnected())
            return false;
        tcp_client.sendMessage(str);
            return true;
    }



    public void onClickConnect () {
        Button connect_button= (Button) findViewById(R.id.connect_button);
        ColorDrawable buttonColor = (ColorDrawable) connect_button.getBackground();
        int colorId = buttonColor.getColor();
        if (colorId == Color.RED) {
            /*
            if(!tcp_client.isRunning()) {
                mThread.run();
            } else {
                restartTCPClient();
            }*/

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
        last_elev_number = getElevNum(floorNum) - 1;
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
            sendGuideCommand("g"+last_elev_number);
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

    public void getCurrentWaypoints(View view) {
        Log.i(TAG,"getCurrentWaypoints");
    }

}
