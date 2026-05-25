package com.example.interactive_graphic_board;

import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

import java.io.IOException;
import java.util.List;

public interface WifiDirectCallback {
    void onPeersUpdated(List<WifiP2pDevice> peers); // реагирует на изменение списка пиров
    void onConnectionInfo(WifiP2pInfo info, NetworkInfo networkInfo); // определение состояния подключения и начало передачи данных
    void onDeviceChanged(WifiP2pDevice device);
}
