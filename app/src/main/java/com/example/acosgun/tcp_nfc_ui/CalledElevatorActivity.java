package com.example.acosgun.tcp_nfc_ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by acosgun on 4/3/15.
 */
public class CalledElevatorActivity extends Activity  implements TextToSpeech.OnInitListener{
    private static final String TAG = "CalledElevatorActivity";
    static Handler mHandler;

    TextToSpeech ttobj;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevator_called);


        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.US);
                        }
                    }
                });


        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        int floor = intent.getIntExtra("floor",0);
        int elevNum = intent.getIntExtra("elevNum",0);
        int duration = intent.getIntExtra("duration",3);
        boolean show_guide = intent.getBooleanExtra("show_guide", true);


        String str = getElevatorInfoString(name, floor, elevNum);

        final TextView info_textview = (TextView) findViewById(R.id.elevatorInfoTextView);
        info_textview.setText(str);

        Button guide_button= (Button) findViewById(R.id.guide_button);
        //final Button = (TextView) findViewById(R.id.elevatorInfoTextView);
        if(show_guide) {
            guide_button.setVisibility(View.VISIBLE);
        } else {
            guide_button.setVisibility(View.INVISIBLE);
        }


        if(duration>0) {
            mHandler = new Handler();
            mHandler.postDelayed(mRunnable, duration * 1000);
        }
    }


    @Override
    public void onInit(int status) {
        Log.i(TAG, "onInit");
        if (status == TextToSpeech.SUCCESS) {
            speakText("Called");
        }
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private String getElevatorInfoString(String name, int floorNum, int elevNum)
    {
        return "Welcome, " + name + "!\n\nEntered Floor: " + floorNum + "\n\nTake elevator: " + elevNum;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run()
        {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    };

    public void guideButtonClicked(View v){
        Log.i(TAG,"guideButtonClicked:");
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public void speakText(String toSpeak){
        Log.i(TAG,"speakText");
        //ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH,null);
    }

}

