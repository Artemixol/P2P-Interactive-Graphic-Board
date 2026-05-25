package com.example.interactive_graphic_board;

import android.Manifest;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

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
    private DrawingView drawingView;
    private TextView tvRoomInfo;

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
      
        drawingView = findViewById(R.id.drawingView);
        tvRoomInfo = findViewById(R.id.tvRoomInfo);
        Button btnClear = findViewById(R.id.btnClear);

        String roomName = roomManager.getRoomName();
        boolean isHost = roomManager.isHost();
        String role = isHost ? "хост" : "гость";
        tvRoomInfo.setText("Комната: " + roomName + " (" + role + ")");

        btnClear.setOnClickListener(v -> drawingView.clearCanvas());
      
        wifiManager = WifiDirectManager.getInstance(this);
        wifiManager.registerCallback(this, this);

        if (wifiManager.getHostStatus()) {
            wifiManager.discoverPeers();

            server = new ServerP2P(this.getApplicationContext(), roomManager);
            server.start();
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (server != null) {
            server.stopServer();
        }

        wifiManager.unregisterCallback(this);
        wifiManager.cancelConnect();
        wifiManager.requestGroupInfo();
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