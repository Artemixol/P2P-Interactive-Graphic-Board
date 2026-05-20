package com.example.interactive_graphic_board;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {
    private Paint paint;
    private Path path;
    private float lastX, lastY;


    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);
        paint.setAntiAlias(true);
        path = new Path();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                lastX = x;
                lastY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - lastX);
                float dy = Math.abs(y - lastY);
                if (dx > 4 || dy > 4) {
                    path.quadTo(lastX, lastY, (x + lastX)/2, (y + lastY)/2);
                    lastX = x;
                    lastY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                path.lineTo(x, y);
                break;
        }

        invalidate();
        return true;
    }


    @Override
    protected void onDraw(Canvas cnvs) {
        super.onDraw(cnvs);
        cnvs.drawPath(path, paint);
    }


    public void clearCanvas() {
        path.reset();
        invalidate();
    }
}