package com.example.heli.myapplication;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MusicControllerActivity extends Activity implements MediaController.MediaPlayerControl{
    public static final String TAG = "musiccontroller";
    private String host = null;
    private int port;
    private MediaPlayer mediaPlayer = null;
    private Uri mSong = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_controller);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String stringUri = intent.getStringExtra(WiFiDirectActivity.EXTRA_SONG_URI);
        mSong = Uri.parse(stringUri);

        host = intent.getStringExtra(MusicControllerService.EXTRAS_GROUP_OWNER_ADDRESS);
        port = intent.getExtras().getInt(MusicControllerService.EXTRAS_GROUP_OWNER_PORT);
        Log.d(WiFiDirectActivity.TAG, "music controller activity received host:" + host);
        Log.d(WiFiDirectActivity.TAG, "music controller activity received port:" + port);

        mediaPlayer = new MediaPlayer();

        final Button buttonPlay = (Button) findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

        final Button buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });
    }

    @Override
    public void onStop(){
        super.onStop();
        kill();
    }

    public void kill(){
        MusicControllerService.startActionSendCommand(getApplicationContext(), host, port, "KILL");
        mediaPlayer.release();
    }

    @Override
    public void start() {
        MusicControllerService.startActionSendCommand(getApplicationContext(), host, port, "PLAY");
        mediaPlayer.reset();
        try{
            mediaPlayer.setDataSource(getApplicationContext(), mSong);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e){
            Log.e(MusicPlayerActivity.TAG, e.getMessage());
        }
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        MusicControllerService.startActionSendCommand(getApplicationContext(), host, port, "STOP");
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
