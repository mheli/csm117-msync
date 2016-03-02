package com.example.heli.msync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by Elvin on 3/2/2016.
 */
public class MainActivity extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ImageButton find_peers = (ImageButton) findViewById(R.id.find_peer);
        find_peers.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), WiFiDirectActivity.class);
                startActivityForResult(myIntent, 0);
            }

        });
    }
}

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.find_peers);
//
//        // add necessary intent values to be matched.
//
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
//
//        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        channel = manager.initialize(this, getMainLooper(), null);
//
//        ImageButton find_peer = (ImageButton) findViewById(R.id.find_peer);
//        find_peer.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                setContentView(R.layout.find_peers);
//            }
//        });
//    }