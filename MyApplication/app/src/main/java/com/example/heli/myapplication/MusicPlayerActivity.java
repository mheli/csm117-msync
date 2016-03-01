package com.example.heli.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;

public class MusicPlayerActivity extends Activity{
    public static final String TAG = "musicplayer";
    private String host = null;
    private int port;
    private Uri mSong = null;
    private Intent playIntent;
    private MusicService musicSrv;
    private boolean musicBound = false;
    private boolean isController = false;
    private static AsyncTask<Void, Void, String> mMusicPlayerAsyncTask;
    private long startTime;
    private Handler mHandler = null;

    public static final String EXTRAS_IS_CONTROLLER = "is_controller";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String stringUri = intent.getStringExtra(WiFiDirectActivity.EXTRA_SONG_URI);
        mSong = Uri.parse(stringUri);

        host = intent.getStringExtra(SendCommandService.EXTRAS_GROUP_OWNER_ADDRESS);
        port = intent.getExtras().getInt(SendCommandService.EXTRAS_GROUP_OWNER_PORT);
        isController = intent.getExtras().getBoolean(MusicPlayerActivity.EXTRAS_IS_CONTROLLER);

        Log.d(TAG, "received host:" + host);
        Log.d(TAG, "received port:" + port);
        Log.d(TAG, "received is controller:" + isController);

        mHandler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to LocalService
        if(playIntent==null){
            Log.d(TAG, "starting music service");
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
            Log.d(TAG, "started music service");
        }

        if (isController){
            Log.d(TAG, "making buttons");
            final Button buttonPlay = (Button) findViewById(R.id.buttonPlay);
            buttonPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendPlay();
                }
            });

            final Button buttonStop = (Button) findViewById(R.id.buttonStop);
            buttonStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendPause();
                }
            });
            Log.d(TAG, "made buttons");
        }

    }

    private void sendLocalIpAddress(){
        String address = getLocalAddress();
        SendCommandService.startActionSendCommand(getApplicationContext(), host, port, address);
    }

    public String getLocalAddress() {
        String localAddress = "";
        try {
            List<NetworkInterface> nInterfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
            if (nInterfaces != null){
                Log.d(TAG, "Number of interfaces: " + String.valueOf(nInterfaces.size()));
            }
            for (NetworkInterface nInterface : nInterfaces) {
                if (nInterface.getName().contains("p2p")) {

                    List<InetAddress> iAddresses = Collections.list(nInterface.getInetAddresses());
                    for (InetAddress iAddress : iAddresses) {
                        if (!iAddress.getHostAddress().contains("p2p"))
                            localAddress = iAddress.getHostAddress();
                    }

                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        Log.d(TAG, "local address: " + localAddress);
        return localAddress;
    }

    private void startMusicPlayerAsyncTask(){
        mMusicPlayerAsyncTask = new MusicPlayerAsyncTask(this).execute();
        Log.d(TAG, "started MusicPlayerAsyncTask");
}

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicBound = true;

            if (!isController)
                startMusicPlayerAsyncTask();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void guessOffset(){
        startMusicPlayerAsyncTask();
        sendLocalIpAddress();
    }

    private void onServerReady(){
        startTime = System.nanoTime();
        SendCommandService.startActionSendCommand(getApplicationContext(), host, port, "TIME");
    }

    public void sendOffsetPlay(){
        long offset = ((System.nanoTime() - startTime) / 2);
        long msOffset = offset / 1000000;
        Log.d(TAG, "offset: " + offset);
        Log.d(TAG, "ms offset: " + msOffset);
        if (musicBound) {
            SendCommandService.startActionSendCommand(getApplicationContext(), host, port, "PLAY");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "playing offsetted song");
                    musicSrv.playSong(mSong);
                }
            }, msOffset);
        }
    }

    private void sendPlay(){
        guessOffset();
    }

    public void sendPause(){
        if (musicBound){
            SendCommandService.startActionSendCommand(getApplicationContext(), host, port, "STOP");
            musicSrv.pauseSong();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        SendCommandService.startActionSendCommand(getApplicationContext(), host, port, "KILL");
        if (musicBound) {
            unbindService(musicConnection);
            stopService(playIntent);
            musicBound = false;
        }
        mMusicPlayerAsyncTask.cancel(true);
    }

    public void kill() {
        finish();
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class MusicPlayerAsyncTask extends AsyncTask<Void, Void, String> {

        private MusicPlayerActivity mParent;
        private TextView statusText;
        private MusicService musicSrv;

        public MusicPlayerAsyncTask(MusicPlayerActivity parent) {
            this.mParent = parent;
            this.musicSrv = this.mParent.musicSrv;
            this.statusText = (TextView) this.mParent.findViewById(android.R.id.content).findViewById(R.id.status_text);
        }

        @Override
        protected String doInBackground(Void... params) {
            if(isCancelled())
                return null;
            try {
                ServerSocket serverSocket = new ServerSocket(8989);
                Log.d(TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(TAG, "Server: connection done");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                String response = "";
                InputStream inputstream = client.getInputStream();

                while ((bytesRead = inputstream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }

                serverSocket.close();
                return response;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onCancelled(){

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("Command received- " + result);
                switch(result){
                    case "PLAY":
                        mParent.startMusicPlayerAsyncTask();
                        musicSrv.playSong(mParent.mSong);
                        break;
                    case "STOP":
                        mParent.startMusicPlayerAsyncTask();
                        musicSrv.pauseSong();
                        break;
                    case "TIME":
                        mParent.startMusicPlayerAsyncTask();
                        SendCommandService.startActionSendCommand(mParent.getApplicationContext(), mParent.host, mParent.port, "PING");
                        break;
                    case "PING":
                        this.cancel(true);
                        mParent.sendOffsetPlay();
                        break;
                    case "KILL":
                        this.cancel(true);
                        Log.d(TAG, "Stopped MusicPlayerAsyncTask");
                        mParent.kill();
                        break;
                    case "READY":
                        mParent.startMusicPlayerAsyncTask();
                        mParent.onServerReady();
                        break;
                    default:
                        // ip address of client
                        mParent.startMusicPlayerAsyncTask();
                        Log.d(TAG, "Got client IP "+result);
                        mParent.host = result;
                        SendCommandService.startActionSendCommand(mParent.getApplicationContext(), mParent.host, mParent.port, "READY");
                        break;
                }
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Receiving command");
        }

    }

}
