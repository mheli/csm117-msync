package com.example.heli.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MusicPlayerActivity extends Activity{
    public static final String TAG = "musicplayer";
    private String host = null;
    private int port;
    private Uri mSong = null;
    private Intent playIntent;
    private MusicService musicSrv;
    private boolean musicBound = false;
    private boolean isController = false;
    private static MusicPlayerActivity mActivity = null;
    private static AsyncTask<Void, Void, String> mMusicPlayerAsyncTask;

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

        mActivity = this;
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


    public void sendPlay(){
        if (musicBound) {
            SendCommandService.startActionSendCommand(getApplicationContext(), host, port, "PLAY");
            musicSrv.playSong(mSong);
        }
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
                        musicSrv.playSong(mParent.mSong);
                        mParent.startMusicPlayerAsyncTask();
                        break;
                    case "STOP":
                        musicSrv.pauseSong();
                        mParent.startMusicPlayerAsyncTask();
                        break;
                    case "KILL":
                        this.cancel(true);
                        Log.d(TAG, "Stopped MusicPlayerAsyncTask");
                        mParent.kill();
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
