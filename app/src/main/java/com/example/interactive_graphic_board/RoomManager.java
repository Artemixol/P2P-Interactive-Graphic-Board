package com.example.interactive_graphic_board;

public class RoomManager {
    private static RoomManager instance;
    private String roomName;
    private String roomPassword;
    private boolean isHost;

    private RoomManager() {}

    public static synchronized RoomManager getInstance() {
        if (instance == null) instance = new RoomManager();
        return instance;
    }

    public void createRoom(String name, String password) {
        this.roomName = name;
        this.roomPassword = password;
        this.isHost = true;
    }

    public void joinRoom(String name, String password) {
        this.roomName = name;
        this.roomPassword = password;
        this.isHost = false;
    }

    public String getRoomName() { return roomName; }
    public String getRoomPassword() { return roomPassword; }
    public boolean isHost() { return isHost; }
}
