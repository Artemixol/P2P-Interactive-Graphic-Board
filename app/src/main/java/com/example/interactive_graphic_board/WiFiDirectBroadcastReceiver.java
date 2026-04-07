package com.example.interactive_graphic_board;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import androidx.annotation.RequiresPermission;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;

    Context activity;

    private final WifiP2pManager.PeerListListener peerListListener;

    public WiFiDirectBroadcastReceiver(
            WifiP2pManager manager,
            WifiP2pManager.Channel channel,
            Context activity,
            WifiP2pManager.PeerListListener peerListListener
    ) {
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        this.peerListListener = peerListListener;
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) { // изменение состояния Wi-Fi P2P: включён или отключён
            Log.d(this.getClass().getSimpleName(), "onReceive:WIFI_P2P_STATE_CHANGED_ACTION");

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) { // список пиров изменился
            Log.d(this.getClass().getSimpleName(), "onReceive:WIFI_P2P_PEERS_CHANGED_ACTION");

            // Запрос списка у менеджера
            if (manager != null) {
                manager.requestPeers(channel, peerListListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) { // изменилось состояние подключения к узлу
            Log.d(this.getClass().getSimpleName(), "onReceive:WIFI_P2P_CONNECTION_CHANGED_ACTION");

            // Получение информации о текущем соединении
            WifiP2pInfo connectionInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            // Получение информации о сетевом интерфейсе
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (connectionInfo != null && networkInfo != null && networkInfo.isConnected()) {
                // TODO Реализовать передачу данных через сокеты (для начала - пароля)
            } else {
                // Соединение разорвано или не было установлено
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) { // изменение сведений об устройстве
            Log.d(this.getClass().getSimpleName(), "onReceive:WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
        }
    }
}
