package com.example.interactive_graphic_board;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

// Сервер для обработки запросов на подключение и передачи данных
public class ServerP2P extends Thread implements DrawObserver {
    public static final int PORT = 50000;
    private static final int MAX_CLIENTS = 3;

    private final RoomManager roomManager;
    private final ThreadPoolExecutor executor;
    private final List<Socket> connectedClients = new CopyOnWriteArrayList<>(); // потокобезопасный список сокетов клиентов
    private ServerSocket serverSocket;
    private volatile boolean isRunning;

    public ServerP2P(RoomManager roomManager) {
        this.roomManager = roomManager;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(25);
        isRunning = true;
    }

    // Реакция на действие с холстом
    @Override
    public void onDrawAction(DrawAction action) {
        if (!connectedClients.isEmpty()) { // проверка наличия клиентов
            for (Socket clientSocket : connectedClients) {
                try {
//                    if (clientSocket.isClosed()) {
//                        clientSocket.connect((new InetSocketAddress(clientSocket.getInetAddress(), clientSocket.getPort())), 500);
//                    }
                    executor.execute(new SendDataTask(clientSocket, action));
                    Log.d("P2P", "ServerP2P:onDrawAction");
                } catch (RejectedExecutionException e) {
                    Log.e("P2P", "ServerP2P:onDrawAction:", e);
                }
            }
        }
    }

    // Работа сервера в отдельном потоке
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            Log.d("P2P", "ServerP2P:run:port=" + PORT);

            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executor.execute(new CheckPasswordTask(clientSocket));
                } catch (SocketException e) {
                    if (!isRunning) break; // ожидаемое закрытие извне
                    Log.e("P2P", "ServerP2P:run:", e);
                } catch (RejectedExecutionException e) {
                    Log.e("P2P", "ServerP2P:run:", e);
                }
            }
        } catch (IOException e) {
            Log.e("P2P", "ServerP2P:run:", e);
        } finally {
            stopServer();
            executor.shutdown();
        }
    }

    // Закрытие сервера
    public void stopServer() {
        isRunning = false;

        removeClients();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e("P2P", "ServerP2P:stopServer:", e);
        }
    }

    // Добавление новых клиентов (в виде сокетов)
    private boolean addClient(Socket clientSocket) {
        if (connectedClients.size() >= MAX_CLIENTS) {
            Log.w("P2P", "Max clients reached, rejecting " + clientSocket.getInetAddress());
            return false;
        }
        connectedClients.add(clientSocket);
        Log.d("P2P", "Client added: " + clientSocket.getInetAddress() + ", total: " + connectedClients.size());
        return true;
    }

    // Очистка списка клиентов и закрытие сокетов
    private void removeClients() {
        if (!connectedClients.isEmpty()) {
            for (Socket clientSocket : connectedClients) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    Log.e("P2P", "ServerP2P:removeClients:", e);
                }
            }

            connectedClients.clear();
        }
    }

    // Проверка пароля
    private class CheckPasswordTask implements Runnable {
        private final Socket socket;

        CheckPasswordTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            Log.d("P2P", "ServerP2P:CheckPasswordTask:Task started");

            byte response = 0;
            try {
                BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

                // Чтение длины сообщения (4 байта)
                byte[] lenBuffer = new byte[4];
                int bytesRead = bis.read(lenBuffer);
                if (bytesRead != 4) {
                    Log.e("P2P", "ServerP2P:CheckPasswordTask:Failed to read message length");
                    socket.close();
                    return;
                }
                int messageLength = ByteBuffer.wrap(lenBuffer).getInt();
                if (messageLength <= 0 || messageLength > 4096) { // защита от огромных сообщений
                    Log.e("P2P", "ServerP2P:CheckPasswordTask:Invalid message length:" + messageLength);
                    socket.close();
                    return;
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
                    Log.e("P2P", "ServerP2P:CheckPasswordTask:Incomplete message received");
                    socket.close();
                    return;
                }

                String gotPassword = new String(data, StandardCharsets.UTF_8);
                String expectedPassword = roomManager.getRoomPassword();

                if (gotPassword.equals(expectedPassword) && addClient(socket)) {
                    response = 1;
                }

                bos.write(response);
                bos.flush();
                Log.d("P2P", "ServerP2P:CheckPasswordTask:Response sent:" + response);

            } catch (IOException e) {
                Log.e("P2P", "ServerP2P:CheckPasswordTask:Error in CheckPasswordTask:", e);
            } finally {
                if (response == 0) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        Log.e("P2P", "ServerP2P:CheckPasswordTask:", ex);
                    }
                }
            }
        }
    }

    // Отправка данных с холста
    private class SendDataTask implements Runnable {
        private final Socket socket;
        private final DrawAction action;

        SendDataTask(Socket socket, DrawAction action) {
            this.socket = socket;
            this.action = action;
        }

        @Override
        public void run() {
            Log.d("P2P", "ServerP2P:SendDataTask:Task started");

            try {
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

                // Подготовка сообщения
                byte[] message = action.toBytes();
                byte[] messageLength = ByteBuffer.allocate(4).putInt(message.length).array();

                bos.write(messageLength); // отправка длины сообщения
                bos.write(message); // отправка самого сообщения
                bos.flush();

                Log.d("P2P", "ServerP2P:SendDataTask:Data sent");

            } catch (IOException e) {
                Log.e("P2P", "ServerP2P:SendDataTask:Error in SendDataTask:", e);
            }
        }
    }
}
