package com.example.interactive_graphic_board;

import android.Manifest;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
        wifiManager.cancelConnect();
        wifiManager.requestGroupInfo();
        wifiManager.setHostStatus(this);

        wifiManager.registerCallback(this, this);
        wifiManager.discoverPeers(); // поиск узлов в одноранговой сети
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        wifiManager.unregisterCallback(this);
    }

    /*
        Отображение интерфейса
     */

    // Поле со списком устройств, доступных для подключения
    // Устанавливается один раз в setAdapterInListView()
    ArrayAdapter<String> arrayAdapterDevices; // обновляется в onPeersUpdated()
    // Текущие доступные для подключения устройства
    List<WifiP2pDevice> currentDevices; // обновляется в onPeersUpdated()
    WifiP2pDevice selectedDevice; // выбранное устройство

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
                wifiManager.cancelConnect(); // отключиться от остальных устройств
                selectedDevice = currentDevices.get(position); // выбранное устройство по позиции
                wifiManager.connectToDevice(selectedDevice); // подключение к выбранному устройству
            }
        });
    }

    // Метод для обновления данных в адаптере
    public void updateAdapter() {
        arrayAdapterDevices.clear();

        ArrayList<String> devicesDescriptions = new ArrayList<String>();
        for (WifiP2pDevice device : currentDevices) {
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

        finish(); // завершение активности
    }

    // Информация по P2P подключению
    WifiP2pInfo info; // обновляется в onConnectionInfo()

    // Присоединение к комнате и переход на графическую доску
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    public void GoToBoard(View v) {

        if (selectedDevice == null) {
            Log.e("P2P", "FindSessionActivity:GoToBoard:Device is not selected");
            Toast.makeText(this, "Выберите устройство для подключения", Toast.LENGTH_SHORT).show();
            return;
        }
        if (info == null) {
            Log.e("P2P", "FindSessionActivity:GoToBoard:Info is empty");
            Toast.makeText(this, "Подключение не удалось", Toast.LENGTH_SHORT).show();
            return;
        }

        if (info.isGroupOwner) {
            Log.e("P2P", "FindSessionActivity:GoToBoard:info.isGroupOwner=true");
            Toast.makeText(this, "Нельзя подключиться к самому себе", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Log.d("P2P", "FindSessionActivity:GoToBoard:info.isGroupOwner=false");
        }

        Socket socket = new Socket();
        try { // запрос на подключение и ожидание его выполнения
            Thread request = connectionRequest(socket);
            request.start();
            request.join();

        } catch (InterruptedException e) {
            Log.e("P2P", "FindSessionActivity:GoToBoard:" + e);
        }

        wifiManager.setClientSocket(socket);

        RoomManager.getInstance().createRoom(this, null, null);

        Intent intent = new Intent(this, CanvasActivity.class);
        startActivity(intent);

        finish(); // завершение активности
    }

    // Отправка пароля хосту и получение информации о его корректности
    public Thread connectionRequest(Socket socket) {

        return new Thread(() -> {
            String password = ((TextView) findViewById(R.id.joinPassword)).getText().toString();

            // Подготовка сообщения: сообщение + его длина (в 4 байта)
            byte[] messagePassword = password.getBytes(StandardCharsets.UTF_8);
            byte[] messageLength = ByteBuffer.allocate(4).putInt(messagePassword.length).array();

            int code = 0;

            try {
                socket.connect((new InetSocketAddress(info.groupOwnerAddress, ServerP2P.PORT)), 5000);

                Log.d("P2P", "FindSessionActivity:connectionRequest:Connected to " + selectedDevice.deviceAddress + ":" + ServerP2P.PORT + " from " + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());

                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                bos.write(messageLength); // отправка длины сообщения
                bos.write(messagePassword); // отправка самого сообщения
                bos.flush();

                Log.d("P2P", "FindSessionActivity:connectionRequest:Password sent, waiting for response");

                BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                code = bis.read();

                Log.d("P2P", "FindSessionActivity:connectionRequest:Response code: " + code);

                if (code != 1)
                    throw new IOException("Incorrect password");

            } catch (SocketTimeoutException e) {
                Log.e("P2P", "FindSessionActivity:connectionRequest:Timeout waiting for server response:", e);
                runOnUiThread(() -> Toast.makeText(this, "Сервер не ответил", Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                Log.e("P2P", "FindSessionActivity:connectionRequest:" + e);
                runOnUiThread(() -> Toast.makeText(this, "Подключение не удалось", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                Log.e("P2P", "FindSessionActivity:connectionRequest:" + e);
                runOnUiThread(() -> Toast.makeText(this, "Фатальная ошибка", Toast.LENGTH_SHORT).show());

            } finally {
                if (code != 1) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Log.e("P2P", "FindSessionActivity:connectionRequest:Couldn't close socket:" + e);
                    }
                }
            }
        });
    }

    /*
        Реализация методов интерфейса WifiDirectCallback
     */

    @Override public void onPeersUpdated(List<WifiP2pDevice> peers) {
        Log.d("P2P", "FindSessionActivity:onPeersUpdated");

        currentDevices = peers;
        updateAdapter();
    }
    @Override public void onConnectionInfo(WifiP2pInfo info, NetworkInfo networkInfo) {
        Log.d("P2P", "FindSessionActivity:onConnectionInfo");

        if (info != null && networkInfo != null && networkInfo.isConnected()) // соединение установлено
            this.info = info;
    }
    @Override public void onDeviceChanged(WifiP2pDevice device) {
        Log.d("P2P", "FindSessionActivity:onDeviceChanged");
    }
}