package com.example.interactive_graphic_board;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class WifiDirectManager { // паттерн Синглтон
    private static volatile WifiDirectManager instance;
    private final Context appContext; // общий контекст приложения
    private WifiDirectCallback callback = null; // текущий активити
    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private WifiDirectBroadcastReceiver receiver;
    private boolean isReceiverRegistered = false;

    private WifiDirectManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.manager = (WifiP2pManager) appContext.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(appContext, appContext.getMainLooper(), null);
        this.receiver = new WifiDirectBroadcastReceiver(this);
    }

    public static WifiDirectManager getInstance(Context context) {
        if (instance == null) {
            synchronized (WifiDirectManager.class) {
                if (instance == null) {
                    instance = new WifiDirectManager(context);
                }
            }
        }
        return instance;
    }

    // Регистрация активити и ресивера
    public void registerCallback(WifiDirectCallback callback, Activity activity) {
        this.callback = callback;

        if (!isReceiverRegistered) {
            ContextCompat.registerReceiver(activity, receiver, getIntentFilter(), ContextCompat.RECEIVER_NOT_EXPORTED);
            isReceiverRegistered = true;
        }
    }

    // Отмена регистрации
    public void unregisterCallback(Activity activity) {
        if (isReceiverRegistered) {
            activity.unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }
        this.callback = null;
    }

    // Метод для поиска узлов (пиров) P2P сети
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    public void discoverPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            // Обратная связь о выполняемых операциях
            @Override
            public void onSuccess() { // успех
                Log.d("P2P", "discoverPeers:onSuccess");
            }

            @Override
            public void onFailure(int reasonCode) { // неудача
                Log.d("P2P", "discoverPeers:onFailure:" + reasonCode);
            }
        });
    }

    // Получение списка пиров
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    public void requestPeers() {
        manager.requestPeers(channel, peers -> callback.onPeersUpdated(new ArrayList<>(peers.getDeviceList())));
    }

    // Подключение к определённому узлу (пиру)
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    public void connectToDevice(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("P2P", "connectToDevice:onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.e("P2P", "connectToDevice:onFailure:" + reason);
            }
        });
    }

    /*
        Методы, вызываемые из BroadcastReceiver
     */

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    public void onPeersChanged() { requestPeers(); }
    public void onConnectionChanged(Intent intent) {
        WifiP2pInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
        NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        callback.onConnectionInfo(info, netInfo);
    }
    public void onThisDeviceChanged(Intent intent) {
        WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        callback.onDeviceChanged(device);
    }

    /*
        Проверка разрешений
    */

    private static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        return filter;
    }

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    // Проверка разрешений для приложения
    public void checkAndRequestPermissions(Activity activity) {
        String[] permissions;

        // Для Android 13 (API 33) и выше
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[] {
                    Manifest.permission.NEARBY_WIFI_DEVICES
            };
        }
        // Для более старых версий
        else {
            permissions = new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }

        // Проверка, все ли разрешения уже предоставлены
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        // Если какое-то разрешение не предоставлено
        if (!allGranted) {
            if (activity.shouldShowRequestPermissionRationale(permissions[0])) {
                new AlertDialog.Builder(activity)
                        .setTitle("Необходимо разрешение")
                        .setMessage("Для поиска устройств по Wi-Fi Direct требуется разрешение.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(activity, permissions, PERMISSIONS_REQUEST_CODE);
                            // после нажатия на кнопку - запуск onRequestPermissionsResult
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            } else {
                // Запрашиваем разрешения напрямую
                ActivityCompat.requestPermissions(activity, permissions, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            // Все разрешения уже есть
        }
    }
}
