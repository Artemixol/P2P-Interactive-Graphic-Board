package com.example.interactive_graphic_board;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import androidx.annotation.RequiresPermission;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private final WifiDirectManager wifiManager;
    public WifiDirectBroadcastReceiver(WifiDirectManager wifiManager) { this.wifiManager = wifiManager; }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) { // список пиров изменился
            Log.d("P2P", "onReceive:WIFI_P2P_PEERS_CHANGED_ACTION");

            wifiManager.onPeersChanged();
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) { // изменилось состояние подключения к узлу
            Log.d("P2P", "onReceive:WIFI_P2P_STATE_CHANGED_ACTION");

            wifiManager.onConnectionChanged(intent);
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) { // изменение сведений об устройстве
            Log.d("P2P", "onReceive:WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");

            wifiManager.onThisDeviceChanged(intent);
        }
        else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) { // изменение состояния Wi-Fi P2P: включён или отключён
            Log.d("P2P", "onReceive:WIFI_P2P_STATE_CHANGED_ACTION");
        }
    }
}
