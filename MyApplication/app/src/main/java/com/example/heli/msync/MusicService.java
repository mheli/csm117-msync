package com.example.heli.msync;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MusicService extends Service {
    public static final String TAG = "musicservice";
    private MediaPlayer player;
    private final IBinder musicBind = new MusicBinder();

    public class MusicBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d(TAG, "bound");
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        Log.d(TAG, "unbound");
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "creating player");
        player = new MediaPlayer();
        Log.d(TAG, "created player");
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

    }

    public void playSong(Uri trackUri){
        //play a song
        player.reset();
        try{
            player.setDataSource(getApplicationContext(), trackUri);
            player.prepare();
            player.start();
        }
        catch(Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

    public void pauseSong(){
        player.pause();
    }

}