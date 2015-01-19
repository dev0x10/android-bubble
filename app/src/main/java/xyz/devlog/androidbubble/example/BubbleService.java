package xyz.devlog.androidbubble.example;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class BubbleService extends Service implements View.OnTouchListener{

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private ImageView bubble;
    private RelativeLayout bubbleLayout;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    public BubbleService() {}

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        bubbleLayout = new RelativeLayout(this);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        this.createSuperDroid();
    }

    private void createSuperDroid() {
        params.gravity = Gravity.TOP | Gravity.START;
        bubble = new ImageView(this);
        bubble.setImageResource(R.drawable.ic_bubble_android);
        bubbleLayout.addView(bubble, 0);
        bubble.setOnTouchListener(this);
        windowManager.addView(bubbleLayout, params);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                windowManager.updateViewLayout(bubbleLayout, params);
                break;
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (bubbleLayout != null) {
            windowManager.removeView(bubbleLayout);
        }
    }
}
