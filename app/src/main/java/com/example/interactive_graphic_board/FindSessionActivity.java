package com.example.interactive_graphic_board;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FindSessionActivity extends AppCompatActivity {

    /*
        Жизненный цикл активити
     */

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
        checkAndRequestPermissions();
        addActions();
        createWifiManager();
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES}) // проверка разрешений
    @Override
    protected void onResume() {
        super.onResume();

        // Создание приёмника и его регистрация в активити
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this, peerListListener);
        registerReceiver(receiver, intentFilter);

        startSearchPeers(); // поиск узлов в одноранговой сети
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(receiver); // конец вещания
    }

    /*
        Отображение интерфейса
     */

    // Поле со списком устройств, доступных для подключения
    // Устанавливается один раз в setAdapterInListView()
    // Обновляется методом onPeersAvailable() объекта PeerListListener
    ArrayAdapter<WifiP2pDevice> arrayAdapterDevices;

    // Метод для установки ArrayAdapter в ListView
    public void setAdapterInListView() {
        ListView listView = (ListView) findViewById(R.id.scrollRoomsListView);

        // Изначально пустой список устройств
        ArrayList<WifiP2pDevice> devices = new ArrayList<WifiP2pDevice>();

        // Инициализация ArrayAdapter с элементами WifiP2pDevice
        arrayAdapterDevices = new ArrayAdapter<WifiP2pDevice>(this,
                android.R.layout.simple_list_item_1, devices);

        // Установка адаптера в ListView
        listView.setAdapter(arrayAdapterDevices);
    }

    // Метод для обновления данных в адаптере
    public void updateAdapter(Collection<WifiP2pDevice> devices) {
        arrayAdapterDevices.clear();
        arrayAdapterDevices.addAll(devices);
        arrayAdapterDevices.notifyDataSetChanged();
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

    /*
        Поиск устройств
     */

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WiFiDirectBroadcastReceiver receiver;

    private final IntentFilter intentFilter = new IntentFilter();

    // Метод для добавления отслеживаемых событий
    public void addActions() {
        Log.d(this.getClass().getSimpleName(), "addActions");

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    // Создание Wifi Direct менеджера и канала
    public void createWifiManager() {
        Log.d(this.getClass().getSimpleName(), "createWifiManager");

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    // Метод для поиска узлов (пиров) P2P сети
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES}) // проверка разрешений
    public void startSearchPeers() {
        Log.d(this.getClass().getSimpleName(), "startSearchPeers");

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            // Обратная связь о выполняемых операциях

            @Override
            public void onSuccess() { // успех
                Log.d("FindSessionActivity", "onSuccess");
            }

            @Override
            public void onFailure(int reasonCode) { // неудача
                Log.d("FindSessionActivity", "onFailure:" + reasonCode);
            }
        });
    }

    // Поле с реализацией интерфейса PeerListListener
    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) { // вызов, когда список устройств готов
            Log.d(this.getClass().getSimpleName(), "peerListListener.onPeersAvailable");

            Collection<WifiP2pDevice> devices = peerList.getDeviceList();

            updateAdapter(devices);
        }
    };

    // Метод для подключения к устройству (узлу)
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    private void connectToDevice(WifiP2pDevice device) {
        Log.d(this.getClass().getSimpleName(), "connectToDevice");

        // Создание объекта конфигурации
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress; // MAC-адрес целевого устройства

        // Запрос системе на соединение
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("WiFiDirect", "Запрос на подключение успешно отправлен");
            }

            @Override
            public void onFailure(int reason) {
                Log.e("WiFiDirect", "Не удалось отправить запрос на подключение. Код ошибки: " + reason);
            }
        });
    }

    /*
        Проверка разрешений
     */
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    // Проверка разрешений для приложения
    private void checkAndRequestPermissions() {
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
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        // Если какое-то разрешение не предоставлено
        if (!allGranted) {
            if (shouldShowRequestPermissionRationale(permissions[0])) {
                new AlertDialog.Builder(this)
                        .setTitle("Необходимо разрешение")
                        .setMessage("Для поиска устройств по Wi-Fi Direct требуется разрешение.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
                            // после нажатия на кнопку - запуск onRequestPermissionsResult
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            } else {
                // Запрашиваем разрешения напрямую
                ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            // Все разрешения уже есть
        }
    }

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
}