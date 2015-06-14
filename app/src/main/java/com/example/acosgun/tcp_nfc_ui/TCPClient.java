package com.example.acosgun.tcp_nfc_ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;



/**
 * Created by acosgun on 5/21/15.
 */
public class TCPClient implements Runnable{
    private static final String TAG= "TCPClient";
    int PORT;
    String IP_ADDRESS;
    Socket socket;

    private PrintWriter out = null;
    private BufferedReader in = null;
    private boolean mRun = false;
    Handler mHandler;

    public TCPClient (Handler handler, String ip_add, int port) {
        this.PORT = port;
        this.IP_ADDRESS = ip_add;
        this.mHandler = handler;
    }

    public boolean isRunning () {
        return mRun;
    }

    public void sendMessage (String msg) {
        if(out != null && !out.checkError()) {
            out.print(msg);
            out.flush();
            Log.d(TAG, "Sent Message: " + msg);
        }
    }

    public void stopClient(){
        Log.d(TAG, "Client stopped!");
        mRun = false;
        //mMainActivity.tcpInputCallback(false, "");
    }

    public boolean disconnect() {
        try {
        socket.close();
        in.close();
        out.close();
        mRun = false;
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return false;
        }
        Log.d(TAG, "Disconnected..");
        //mMainActivity.tcpInputCallback(false, "");
        return true;
    }

    public void run() {
        mRun = true;
        try {
            InetAddress serverAddr = InetAddress.getByName(this.IP_ADDRESS);
            socket = new Socket(serverAddr, this.PORT);

            Log.d(TAG, "socket successfully created");

            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.d(TAG, "In/Out created");

                Message msg0 = mHandler.obtainMessage();
                Bundle bun0 = new Bundle();
                bun0.putString("data", null);
                bun0.putBoolean("connected", true);
                msg0.setData(bun0);
                mHandler.sendMessage(msg0);


                while (mRun) {
                    String incomingMessage = in.readLine();
                    if (incomingMessage != null) {
                        Log.d(TAG, "Received Message: " +incomingMessage);
                        //mMainActivity.tcpInputCallback(true,incomingMessage);


                        //Message msg = new Message();
                        Message msg1 = mHandler.obtainMessage();
                        Bundle bun1 = new Bundle();
                        bun1.putString("data", incomingMessage);
                        bun1.putBoolean("connected", true);
                        msg1.setData(bun1);
                        mHandler.sendMessage(msg1);

                    }
                }
            }
            catch (Exception e) {
                Log.d(TAG, "Error", e);
                mRun = false;

                Message msg0 = mHandler.obtainMessage();
                Bundle bun0 = new Bundle();
                bun0.putString("data", null);
                bun0.putBoolean("connected", false);
                msg0.setData(bun0);
                mHandler.sendMessage(msg0);

                return;

            }

        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            mRun = false;

            Message msg0 = mHandler.obtainMessage();
            Bundle bun0 = new Bundle();
            bun0.putString("data", null);
            bun0.putBoolean("connected", false);
            msg0.setData(bun0);
            mHandler.sendMessage(msg0);

            return;
        }
        mRun = false;
    }

}
