package com.example.interactive_graphic_board;

import android.content.Context;

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

    public void createRoom(Context activity, String name, String password) {
        this.roomName = name;
        this.roomPassword = password;

        if (activity.getClass() == CreateSessionActivity.class) this.isHost = true;
        else this.isHost = false;
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
