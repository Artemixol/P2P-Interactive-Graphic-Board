package com.example.interactive_graphic_board;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;



public class DrawingView extends View {

    private Paint paint;
    private Path path;
    private float lastX, lastY;
    private List<DrawObserver> observers = new ArrayList<>();
    private boolean notifyObservers = true;


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


    public void addObserver(DrawObserver observer) {
        /*Стать наблюдателем*/

        if (!observers.contains(observer)) {
            observers.add(observer);
        }

    }

    public void removeObserver(DrawObserver observer) {
        /*Выйти из общества наблюдателей*/

        observers.remove(observer);

    }

    private void notifyObservers(DrawAction action) {
        /*Разослать всем уведомления*/

        if (!notifyObservers) return;
        for (DrawObserver observer : observers) {
            observer.onDrawAction(action);
        }

    }


    @Override
    protected void onDraw(Canvas cnvs) {

        super.onDraw(cnvs);
        cnvs.drawPath(path, paint);

    }


    public void clearCanvas() {

        path.reset();
        invalidate();
        notifyObservers(new DrawAction("clear", 0, 0));

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
                notifyObservers(new DrawAction("down", x, y));
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - lastX);
                float dy = Math.abs(y - lastY);
                if (dx > 4 || dy > 4) {
                    path.quadTo(lastX, lastY, (x + lastX)/2, (y + lastY)/2);
                    notifyObservers(new DrawAction("move", (x + lastX)/2, (y + lastY)/2, lastX, lastY));
                    lastX = x;
                    lastY = y;
                }
                break;

            case MotionEvent.ACTION_UP:
                path.lineTo(x, y);
                notifyObservers(new DrawAction("up", x, y));
                break;

        }

        invalidate();
        return true;

    }


    public void applyRemoteAction(DrawAction action) {

        notifyObservers = false;

        switch (action.getType()) {
            case "down":
                path.moveTo(action.getX(), action.getY());
                lastX = action.getX();
                lastY = action.getY();
                break;
            case "move":
                path.quadTo(action.getLastX(), action.getLastY(), action.getX(), action.getY());
                lastX = action.getX();
                lastY = action.getY();
                break;
            case "up":
                path.lineTo(action.getX(), action.getY());
                break;
            case "clear":
                path.reset();
                break;
        }

        invalidate();
        notifyObservers = true;

    }

}