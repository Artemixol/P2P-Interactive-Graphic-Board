package com.example.interactive_graphic_board;

import android.Manifest;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.List;

public class CanvasActivity extends AppCompatActivity implements WifiDirectCallback {

    RoomManager roomManager;
    WifiDirectManager wifiManager;
    ServerP2P server;

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_canvas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        roomManager = RoomManager.getInstance();

        wifiManager = WifiDirectManager.getInstance(this);
        wifiManager.registerCallback(this, this);

        if (roomManager.isHost()) {
            wifiManager.discoverPeers();

            server = new ServerP2P(this.getApplicationContext(), roomManager);
            server.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (server != null) {
            server.stopServer();
        }

        wifiManager.unregisterCallback(this);
        wifiManager.cancelConnect();
    }

    /*
         Реализации методов WifiDirectCallback
    */

    @Override
    public void onPeersUpdated(List<WifiP2pDevice> peers) {
        Log.d("P2P", "CanvasActivity:onPeersUpdated");
    }

    @Override
    public void onConnectionInfo(WifiP2pInfo info, NetworkInfo networkInfo) {
        Log.d("P2P", "CanvasActivity:onConnectionInfo");

        if (info != null && networkInfo != null && networkInfo.isConnected()) {} // соединение установлено
    }

    @Override
    public void onDeviceChanged(WifiP2pDevice device) {
        Log.d("P2P", "CanvasActivity:onDeviceChanged");
    }
}