package com.example.interactive_graphic_board;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class FindSessionActivity extends AppCompatActivity {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WiFiDirectBroadcastReceiver receiver;

    private final IntentFilter intentFilter = new IntentFilter();

    private Boolean isWifiP2pEnabled;

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

        addActions(); // добавление отслеживаемых событий
        createWifiManager(); // создание Wifi менеджера и канала
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES}) // проверка разрешений
    @Override
    protected void onResume() {
        super.onResume();

        // Создание приёмника и его регистрация в активити
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);

        startSearchPeers(); // поиск узлов в одноранговой сети
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(receiver); // конец вещания
    }

    // Метод, добавляющий элементы ArrayList в ListView
    public void setDataInListView(ArrayList<String> sessionsList) {
        ListView listView = (ListView) findViewById(R.id.scrollRoomsListView);

        // Объект-адаптер с указанием контекста, интерфейса для отображения элементов
        // и массива самих элементов
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, sessionsList);

        listView.setAdapter(adapter);
    }

    public void addActions() {
        Log.d(this.getClass().getSimpleName(), "addActions");

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public void createWifiManager() {
        Log.d(this.getClass().getSimpleName(), "createWifiManager");

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES}) // проверка разрешений
    public void startSearchPeers() {
        Log.d(this.getClass().getSimpleName(), "startSearchPeers");

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            // Обратная связь о выполняемых операциях

            @Override
            public void onSuccess() { // успех
                Log.d(this.getClass().getSimpleName(), "onSuccess");
            }

            @Override
            public void onFailure(int reasonCode) { // неудача
                Log.d(this.getClass().getSimpleName(), "onFailure");
            }
        });
    }
    public void GoBack(View v){
        /*Возвращение на MainActivity*/
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}