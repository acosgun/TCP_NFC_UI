package com.example.acosgun.tcp_nfc_ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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
    private boolean connected_ = false;

    public TCPClient (Handler handler, String ip_add, int port) {
        this.PORT = port;
        this.IP_ADDRESS = ip_add;
        this.mHandler = handler;
    }

    public void changeIP(String ip) {
        this.IP_ADDRESS = ip;
    }
    public void changePort(int port) {
        this.PORT = port;
    }

    public boolean isRunning () {
        return mRun;
    }
    public boolean isConnected () {
        return connected_;
    }

    public void sendMessage (String msg) {
        if(out != null && !out.checkError()) {
            out.print(msg);
            out.flush();
            Log.d(TAG, "Sent Message: " + msg);
        }
    }

    /*
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
*/

    public boolean disconnect() {
        sendConnectDataToActivity("",false);
        connected_ = false;

        try {

            socket.close();
            in.close();
            out.close();
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return false;
        }
        Log.d(TAG, "disconnect called");
        return true;
    }

    private void sendConnectDataToActivity(String data, boolean connected) {
        Message msg1 = mHandler.obtainMessage();
        Bundle bun1 = new Bundle();
        bun1.putString("data", data);
        bun1.putBoolean("connected", connected);
        msg1.setData(bun1);
        mHandler.sendMessage(msg1);
    }

    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        while(true) {

            try
            {
                Log.w(TAG, "Try Creating Sockets w IP:" + IP_ADDRESS);
                InetAddress serverAddr = InetAddress.getByName(this.IP_ADDRESS);
                socket = new Socket(serverAddr, this.PORT);
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected_ = true;
                sendConnectDataToActivity("Connected",true);
            }
            catch (Exception e)
            {
                Log.w(TAG, "Socket cannot be created.", e);
                disconnect();
            }

            while (connected_) {
                //Log.d(TAG, "connected_: " + connected_);
                try
                {
                    String incomingMessage;
                        try
                        {
                        incomingMessage = in.readLine();
                            if (incomingMessage != null) {
                                //Log.d(TAG, "RCVD MSG: " +incomingMessage);

                                sendConnectDataToActivity(incomingMessage,true);
                            }
                            else {
                                disconnect();
                                break;
                            }
                        }
                        catch(IOException e)
                        {
                            Log.d(TAG, "MY IOException!!");
                        }

                } catch (Exception e){
                    Log.d(TAG, "Error", e);
                    disconnect();
                    break;
                }


            } // end of inner while

            try { //SLEEP for 1 second
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e(TAG, "local Thread error", e);
            }
        } //end of outer while
    }




/*
    public void run() {
        mRun = true;
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        try {
            InetAddress serverAddr = InetAddress.getByName(this.IP_ADDRESS);
            socket = new Socket(serverAddr, this.PORT);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);



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
*/

}
