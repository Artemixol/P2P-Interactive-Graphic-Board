package com.example.interactive_graphic_board;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CanvasActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private TextView tvRoomInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_canvas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        drawingView = findViewById(R.id.drawingView);
        tvRoomInfo = findViewById(R.id.tvRoomInfo);
        Button btnClear = findViewById(R.id.btnClear);

        String roomName = RoomManager.getInstance().getRoomName();
        tvRoomInfo.setText("Комната: " + roomName + " (хост)");

        btnClear.setOnClickListener(v -> drawingView.clearCanvas());

    }
}