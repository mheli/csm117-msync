package com.example.heli.myapplication;

import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MusicPlayerActivity extends Activity implements MediaController.MediaPlayerControl{
    public static final String TAG = "musicplayer";
    private static View mContentView = null;
    private static Uri mSong = null;
    private static MediaPlayer mediaPlayer = null;
    private static Context mContext = null;
    private static Application mApplication = null;
    private static AsyncTask<Void, Void, String> mMusicPlayerAsyncTask;
    private static Activity mActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mContentView = this.findViewById(android.R.id.content);
        mContext = getApplicationContext();
        mApplication = getApplication();
        mediaPlayer = new MediaPlayer();
        mActivity = this;

        Intent intent = getIntent();
        String stringUri = intent.getStringExtra(WiFiDirectActivity.EXTRA_SONG_URI);
        mSong = Uri.parse(stringUri);
        Log.d(MusicPlayerActivity.TAG, "music player activity received uri:" + mSong);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //DeviceDetailFragment.stopFileServerAsyncTask();
        startMusicPlayerAsyncTask();
    }

    public static void startMusicPlayerAsyncTask(){
        mMusicPlayerAsyncTask = new MusicPlayerAsyncTask(mApplication, mContentView.findViewById(R.id.status_text))
                .execute();
        Log.d(MusicPlayerActivity.TAG, "started MusicPlayer");
    }

    public static void playSong(){
        mediaPlayer.reset();
        try{
            mediaPlayer.setDataSource(mContext, mSong);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e){
            Log.e(MusicPlayerActivity.TAG, e.getMessage());
        }
    }

    public static void pauseSong(){
        mediaPlayer.pause();
    }

    public static void kill(){
        mediaPlayer.release();
        mActivity.finish();
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class MusicPlayerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;

        /**
         * @param context
         * @param statusText
         */
        public MusicPlayerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            if(isCancelled())
                return null;
            try {
                ServerSocket serverSocket = new ServerSocket(8989);
                Log.d(MusicPlayerActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(MusicPlayerActivity.TAG, "Server: connection done");

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
                Log.e(MusicPlayerActivity.TAG, e.getMessage());
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
                            MusicPlayerActivity.playSong();
                            startMusicPlayerAsyncTask();
                        break;
                    case "STOP":
                            MusicPlayerActivity.pauseSong();
                            startMusicPlayerAsyncTask();
                        break;
                    case "KILL":
                            this.cancel(true);
                            Log.d(MusicPlayerActivity.TAG, "Stopped MusicPlayer");
                            MusicPlayerActivity.kill();
                        break;
                }

                /*
                Intent intent = new Intent(context, MusicPlayerActivity.class);
                intent.putExtra(WiFiDirectActivity.EXTRA_SONG_URI, "file://"+result);
                context.startActivity(intent);
                */
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


    @Override
    public void start() {

    }

    @Override
    public void pause() {

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

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle app bar item clicks here. The app bar
        // automatically handles clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */


    /**
     * A placeholder fragment containing a simple view.
     */
    /*
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() { }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_display_message,
                    container, false);
            return rootView;
        }
    }
    */

}
