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
    private float lastX, lastY, lastTouchX, lastTouchY, offsetX = 0, offsetY = 0;
    private boolean isMoving = false;


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


    public boolean isMoving() {
        return isMoving;
    }


    public void setMovingMode(boolean move) {
        isMoving = move;
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
                if (!isMoving) {
                    /*Если не двигаем наш экран, то занимаемся искусством*/
                    float drawX = x - offsetX;
                    float drawY = y - offsetY;
                    path.moveTo(drawX, drawY);
                    lastX = drawX;
                    lastY = drawY;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                /*Отслеживаем движение пальца и рисуем кривые Безье*/
                if (isMoving){
                    /*Смещаем экран если включен режим*/
                    offsetX += x - lastTouchX;
                    offsetY += y - lastTouchY;
                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                } else {
                    /*Иначе рисуем каракули*/
                    float drawX = x - offsetX;
                    float drawY = y - offsetY;
                    float dx = Math.abs(drawX - lastX);
                    float dy = Math.abs(drawY - lastY);
                    if (dx > 4 || dy > 4) {
                        path.quadTo(lastX, lastY, (drawX + lastX)/2, (drawY + lastY)/2);
                        lastX = drawX;
                        lastY = drawY;
                    }
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                /*Подняли палец дорисовали линию, без этого кейса линия может закончиться не в точке поднятия*/
                if (!isMoving) {
                    float drawX = x - offsetX;
                    float drawY = y - offsetY;
                    path.lineTo(drawX, drawY);
                    invalidate();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onDraw(Canvas cnvs) {
        /*Рисуем пути, сдвигаем камеру если надо*/
        super.onDraw(cnvs);
        cnvs.save();
        cnvs.translate(offsetX, offsetY);
        cnvs.drawPath(path, paint);
        cnvs.restore();
    }


    public void clearCanvas() {
        path.reset();
        invalidate();
    }
}