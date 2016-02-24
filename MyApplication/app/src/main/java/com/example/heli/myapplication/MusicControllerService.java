package com.example.heli.myapplication;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MusicControllerService extends IntentService {
    private static final int SOCKET_TIMEOUT = 5000;
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public static final String EXTRAS_COMMAND = "command";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SEND_COMMAND = "com.example.heli.myapplication.action.SEND_COMMAND";

    // TODO: Rename parameters

    public MusicControllerService() {
        super("MusicControllerService");
    }

    /**
     * Starts this service to perform action SEND PLAY with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSendCommand(Context context, String host, int port, String command) {
        Intent intent = new Intent(context, MusicControllerService.class);
        intent.setAction(ACTION_SEND_COMMAND);
        intent.putExtra(EXTRAS_GROUP_OWNER_ADDRESS, host);
        intent.putExtra(EXTRAS_GROUP_OWNER_PORT, port);
        intent.putExtra(EXTRAS_COMMAND, command);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND_COMMAND.equals(action)) {
                String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
                Socket socket = new Socket();
                int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
                String command = intent.getExtras().getString(EXTRAS_COMMAND);

                try {
                    Log.d(MusicControllerActivity.TAG, "Opening client socket - ");
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                    Log.d(MusicControllerActivity.TAG, "Client socket - " + socket.isConnected());
                    OutputStream stream = socket.getOutputStream();
                    PrintStream printStream = new PrintStream(stream);
                    printStream.print(command);
                    printStream.close();
                    Log.d(MusicControllerActivity.TAG, "Client: Data written");
                } catch (IOException e) {
                    Log.e(MusicControllerActivity.TAG, e.getMessage());
                } finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                // Give up
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

}
