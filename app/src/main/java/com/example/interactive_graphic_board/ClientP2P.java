package com.example.interactive_graphic_board;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

// Класс клиента P2P соединения
public class ClientP2P {
    private Socket socket;
    private final DrawingView drawingView;
    private final WifiDirectManager wifiManager;
    private Thread dataTread;

    private volatile boolean isRunning = false;

    public ClientP2P(DrawingView drawingView, WifiDirectManager wifiManager) {
        this.drawingView = drawingView;
        this.wifiManager = wifiManager;
        socket = wifiManager.getClientSocket();
    }

    public boolean getIsRunning() {
        return isRunning;
    }

    // Запуск клиента
    public void startClient() {
        if (isRunning) {
            Log.e("P2P", "ClientP2P:startClient:Client is already running");
            return;
        }

        try {
            dataTread = getDataTread();
            dataTread.start();
        } catch (IllegalThreadStateException e) {
            Log.e("P2P", "ClientP2P:startClient:Client is already running:" + e);
            return;
        }

        isRunning = true;
    }

    // Остановка клиента
    public void stopClient() {
        isRunning = false;

        try {
            socket.close();
        } catch (IOException e) {
            Log.e("P2P", "ClientP2P:stopClient:" + e);
        }
    }

    // Метод для получения потока
    private Thread getDataTread() {
        return new Thread(() -> {
            Log.d("P2P", "ClientP2P:dataTread:Started");

            while (isRunning && !Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                try {
                    BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());

                    // Чтение длины сообщения (4 байта)
                    byte[] lenBuffer = new byte[4];
                    int bytesRead = bis.read(lenBuffer);
                    if (bytesRead != 4) {
                        Log.e("P2P", "ClientP2P:dataTread:Failed to read message length");
                        continue;
                    }
                    int messageLength = ByteBuffer.wrap(lenBuffer).getInt();
                    if (messageLength <= 0 || messageLength > 4096) { // защита от огромных сообщений
                        Log.e("P2P", "ClientP2P:dataTread:Invalid message length:" + messageLength);
                        continue;
                    }

                    // Читаем сообщение
                    byte[] data = new byte[messageLength];
                    int totalRead = 0;
                    while (totalRead < messageLength) {
                        int readNow = bis.read(data, totalRead, messageLength - totalRead);
                        if (readNow == -1) break;
                        totalRead += readNow;
                    }
                    if (totalRead != messageLength) {
                        Log.e("P2P", "ClientP2P:dataTread:Incomplete message received");
                        continue;
                    }

                    // Отрисовка полученного рисунка
                    drawingView.applyRemoteAction(DrawAction.fromBytes(data));

                } catch (SocketTimeoutException e) {
                    Log.e("P2P", "ClientP2P:dataTread:Timeout waiting for server response:", e);
                } catch (IOException e) {
                    Log.e("P2P", "ClientP2P:dataTread:" + e);
                }
            }
        });
    }
}
