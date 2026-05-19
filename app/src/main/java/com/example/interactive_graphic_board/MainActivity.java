package com.example.interactive_graphic_board;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        /* привязка кнопок из XML-разметки к переменным
        Может будут использоваться, но для начала реализовал переход в xml файлике в интерфейсе
        Button create_session = findViewById(R.id.create_session);
        Button find_session = findViewById(R.id.find_session);*/

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Проверка необходимых разрешений для Wifi Direct
        WifiDirectManager wifiManager = WifiDirectManager.getInstance(this);
        wifiManager.checkAndRequestPermissions(this);
    }

    public void GoToFindActivity(View v){
        /*Переход на новую страничку ДЛЯ ГОСТЯ, чтобы найти комнату*/
        Intent intent = new Intent(this, FindSessionActivity.class);
        startActivity(intent);

        finishAffinity(); // очистка стека активностей
    }

    public void GoToCreateActivity(View v){
        /*Переход на новую страничку ДЛЯ ХОСТА, чтобы создать комнату*/
        Intent intent = new Intent(this, CreateSessionActivity.class);
        startActivity(intent);

        finishAffinity(); // очистка стека активностей
    }


    private static final int PERMISSIONS_REQUEST_CODE = 100;

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