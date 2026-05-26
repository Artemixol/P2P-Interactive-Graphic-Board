package com.example.interactive_graphic_board;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
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
public class ServerP2P extends Thread {
    public static final int PORT = 50000;
    private static final int MAX_CLIENTS = 3;

    private final Context context;
    private final RoomManager roomManager;
    private final ThreadPoolExecutor executor;
    private final List<Socket> connectedClients = new CopyOnWriteArrayList<>(); // потокобезопасный список сокетов клиентов
    private ServerSocket serverSocket;
    private volatile boolean isRunning = true;

    public ServerP2P(Context context, RoomManager roomManager) {
        this.context = context.getApplicationContext();
        this.roomManager = roomManager;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
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

                // Уже подключенным клиентам оправляем данные
                if (!connectedClients.isEmpty()) {
                    for (Socket clientSocket : connectedClients) {
                        try {
                            executor.execute(new SendDataTask(clientSocket));
                        } catch (RejectedExecutionException e) {
                            Log.e("P2P", "ServerP2P:run:", e);
                        }
                    }
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

    // Проверка пароля
    private class CheckPasswordTask implements Runnable {
        private final Socket socket;

        CheckPasswordTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            Log.d("P2P", "ServerP2P:CheckPasswordTask:Task started");

            try (socket;
                 BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                 BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream())) {

                // Чтение длины сообщения (4 байта)
                byte[] lenBuffer = new byte[4];
                int bytesRead = bis.read(lenBuffer);
                if (bytesRead != 4) {
                    Log.e("P2P", "ServerP2P:CheckPasswordTask:Failed to read message length");
                    return;
                }
                int messageLength = ByteBuffer.wrap(lenBuffer).getInt();
                if (messageLength <= 0 || messageLength > 4096) { // защита от огромных сообщений
                    Log.e("P2P", "ServerP2P:CheckPasswordTask:Invalid message length:" + messageLength);
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
                    return;
                }

                String gotPassword = new String(data, StandardCharsets.UTF_8);
                String expectedPassword = roomManager.getRoomPassword();

                byte response;
                if (gotPassword.equals(expectedPassword) && addClient(socket)) {
                    response = 1;
                } else {
                    response = 0;
                }
                bos.write(response);
                bos.flush();
                Log.d("P2P", "ServerP2P:CheckPasswordTask:Response sent:" + response);

            } catch (IOException e) {
                Log.e("P2P", "ServerP2P:CheckPasswordTask:Error in CheckPasswordTask:", e);
            }
        }
    }

    // Отправка данных с холста
    private class SendDataTask implements Runnable {
        private final Socket socket;

        SendDataTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            Log.d("P2P", "ServerP2P:CheckPasswordTask:Task started");

            try (socket;
                 BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream())) {

                // TODO: добавить подключение и отправку данных

            } catch (IOException e) {
                Log.e("P2P", "ServerP2P:CheckPasswordTask:Error in SendDataTask:", e);
            }
        }
    }
}
