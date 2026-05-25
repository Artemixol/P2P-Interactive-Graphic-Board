package com.example.interactive_graphic_board;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateSessionActivity extends AppCompatActivity {

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_session);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

       EditText room_name_input_field = findViewById(R.id.room_name_input_field);
       EditText password_field = findViewById(R.id.password_field);
       Button create_canvas = findViewById(R.id.create_canvas);

       // Убираем все существующие старые соединения
       WifiDirectManager wifiManager = WifiDirectManager.getInstance(this);
       wifiManager.cancelConnect();
       wifiManager.requestGroupInfo();
       wifiManager.createGroup();
       wifiManager.setHostStatus(this);

       create_canvas.setOnClickListener(v -> {
           String roomName = room_name_input_field.getText().toString().trim();
           String roomPassword = password_field.getText().toString().trim();
           if (roomName.isEmpty()) {
               room_name_input_field.setError("Введите название комнаты");
               return;
           }
           if (roomPassword.isEmpty()) {
               password_field.setError("Введите пароль");
               return;
           }


           RoomManager.getInstance().createRoom(this, roomName, roomPassword);

           Intent intent = new Intent(this, CanvasActivity.class);
           startActivity(intent);

           finish(); // завершение активности
       });

    }



    public void GoBack(View v){
        /*Возвращение на MainActivity*/
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish(); // завершение активности
    }
}