package com.example.interactive_graphic_board;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
    }

    public void GoToFindActivity(View v){
        /*Переход на новую страничку ДЛЯ ГОСТЯ, чтобы найти комнату*/
        Intent intent = new Intent(this, FindSessionActivity.class);
        startActivity(intent);
    }

    public void GoToCreateActivity(View v){
        /*Переход на новую страничку ДЛЯ ХОСТА, чтобы создать комнату*/
        Intent intent = new Intent(this, CreateSessionActivity.class);
        startActivity(intent);
    }

}