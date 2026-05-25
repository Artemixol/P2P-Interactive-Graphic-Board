package com.example.interactive_graphic_board;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DrawAction {
    /*Для передачи состояния рисования
     * Палец коснулся экрана - down
     * Палец в движении - move
     * Ну и палец поднят - up
     * Также очистка холста - clear*/
    private String type;
    private float x, y, lastX, lastY;


    public DrawAction(String type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.lastX = x;
        this.lastY = y;
    }


    public DrawAction(String type, float x, float y, float lastX, float lastY) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.lastX = lastX;
        this.lastY = lastY;
    }


    public String getType() {
        return type;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getLastX() {
        return lastX;
    }
    public float getLastY() {
        return lastY;
    }

    public byte[] toBytes() throws IOException {
        /*Метод, реализующий сериализацию*/
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(type);
        dos.writeFloat(x);
        dos.writeFloat(y);
        dos.writeFloat(lastX);
        dos.writeFloat(lastY);

        dos.flush();
        return baos.toByteArray();
    }


    public static DrawAction fromBytes(byte[] data) throws IOException {
        /*Метод, реализующий десериализацию*/
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

        String type = dis.readUTF();
        float x = dis.readFloat();
        float y = dis.readFloat();
        float lastX = dis.readFloat();
        float lastY = dis.readFloat();

        return new DrawAction(type, x, y, lastX, lastY);
    }

}