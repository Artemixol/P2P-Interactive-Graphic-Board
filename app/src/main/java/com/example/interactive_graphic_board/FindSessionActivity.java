package com.example.interactive_graphic_board;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class FindSessionActivity extends AppCompatActivity implements WifiDirectCallback {

    private WifiDirectManager wifiManager;

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_session);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setAdapterInListView();

        wifiManager = WifiDirectManager.getInstance(this);
        wifiManager.checkAndRequestPermissions(this);
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES}) // проверка разрешений
    @Override
    protected void onResume() {
        super.onResume();

        wifiManager.registerCallback(this, this);
        wifiManager.discoverPeers(); // поиск узлов в одноранговой сети
    }

    @Override
    public void onPause() {
        super.onPause();

        wifiManager.unregisterCallback(this);
    }

    /*
        Отображение интерфейса
     */

    // Поле со списком устройств, доступных для подключения
    // Устанавливается один раз в setAdapterInListView()
    // Обновляется методом onPeersUpdated()
    ArrayAdapter<String> arrayAdapterDevices;
    List<WifiP2pDevice> currentDevices; // обновляется в onPeersUpdated()

    // Метод для установки ArrayAdapter в ListView
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    public void setAdapterInListView() {
        ListView listView = (ListView) findViewById(R.id.scrollRoomsListView);

        // Изначально пустой список устройств
        ArrayList<String> devices = new ArrayList<String>();

        // Инициализация ArrayAdapter
        arrayAdapterDevices = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devices);

        // Установка адаптера в ListView
        listView.setAdapter(arrayAdapterDevices);

        // Возможность обработки нажатия на элемент списка
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Устройство по позиции
                WifiP2pDevice selectedDevice = currentDevices.get(position);

                wifiManager.connectToDevice(selectedDevice); // подключение к устройству
            }
        });
    }

    // Метод для обновления данных в адаптере
    public void updateAdapter(List<WifiP2pDevice> devices) {
        arrayAdapterDevices.clear();

        ArrayList<String> devicesDescriptions = new ArrayList<String>();
        for (WifiP2pDevice device : devices) {
            String deviceName = device.deviceName;
            String status = getStatus(device.status);

            devicesDescriptions.add(deviceName + "  " + status);
        }
        arrayAdapterDevices.addAll(devicesDescriptions);
        arrayAdapterDevices.notifyDataSetChanged();
    }

    // Расшифровка статуса устройства
    public String getStatus(int intStatus) {
        String status;

        switch (intStatus) {
            case (WifiP2pDevice.CONNECTED):
                status = "CONNECTED";
                break;
            case (WifiP2pDevice.INVITED):
                status = "INVITED";
                break;
            case (WifiP2pDevice.FAILED):
                status = "FAILED";
                break;
            case (WifiP2pDevice.AVAILABLE):
                status = "AVAILABLE";
                break;
            case (WifiP2pDevice.UNAVAILABLE):
                status = "UNAVAILABLE";
                break;
            default: status = "UNKNOWN";
        }

        return status;
    }

    public void GoBack(View v){
        /*Возвращение на MainActivity*/
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Переход на графическую доску
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    public void GoToBoard(View v){

        // TODO Реализовать подключение к выбранному пользователем устройству и при успешном: отправление пароля и переход на доску
        // connectToDevice();

//        Intent intent = new Intent(this, BoardActivity.class);
//        startActivity(intent);
    }

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    // Обработка результата запроса разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Проверка, все ли запрошенные разрешения были предоставлены
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // Разрешения получены
            } else {
                // Разрешения не получены
                Toast.makeText(this, "Для работы приложения необходимы разрешения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
        Реализация методов интерфейса WifiDirectCallback
     */

    @Override public void onPeersUpdated(List<WifiP2pDevice> peers) {
        Log.d("P2P", "onPeersUpdated");

        currentDevices = peers;
        updateAdapter(peers);
    }
    @Override public void onConnectionInfo(WifiP2pInfo info, NetworkInfo networkInfo) {
        Log.d("P2P", "onConnectionInfo");

        if (info != null && networkInfo != null && networkInfo.isConnected()) {
            // соединение установлено
        }
    }
    @Override public void onDeviceChanged(WifiP2pDevice device) {
        Log.d("P2P", "onDeviceChanged");
    }
    @Override public void onDiscoveryStarted() {
        Log.d("P2P", "onDiscoveryStarted");
    }
    @Override public void onDiscoveryFailed(int reason) {
        Log.d("P2P", "onDiscoveryFailed:" + reason);
    }
    @Override public void onConnectSuccess() {
        Log.d("P2P", "onConnectSuccess");
    }
    @Override public void onConnectFailed(int reason) {
        Log.d("P2P", "onConnectFailed:" + reason);
    }
}