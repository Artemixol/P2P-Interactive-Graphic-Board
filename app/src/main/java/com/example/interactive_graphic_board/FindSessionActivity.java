package com.example.interactive_graphic_board;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class FindSessionActivity extends AppCompatActivity {

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

        setDataInListView();
    }

    // Метод, добавляющий элементы ArrayList в ListView
    // TODO реализовать получение доступных сессий и их отображение в ListView
    public void setDataInListView() {
        // Код для примера
        ArrayList<String> strings = new ArrayList<String>();
        for (int i = 0;i < 30;i++) {
            strings.add(String.valueOf(i));
        }

        ListView listView = (ListView) findViewById(R.id.scrollRoomsListView);
        // Объект-адаптер с указанием контекста, интерфейса для отображения элементов
        // и массива самих элементов
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, strings);
        listView.setAdapter(adapter);
    }

    public void GoBack(View v){
        /*Возвращение на MainActivity*/
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}