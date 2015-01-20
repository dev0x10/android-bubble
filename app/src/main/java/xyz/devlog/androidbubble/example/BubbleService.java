package xyz.devlog.androidbubble.example;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringSystemListener;

public class BubbleService extends Service implements View.OnTouchListener, SpringListener, SpringSystemListener {

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private ImageView bubble;
    private RelativeLayout bubbleLayout;
    private Point displaySize;
    private boolean dragging;
    private float lastX;
    private float lastY;
    private float initialTouchX;
    private float initialTouchY;
    private float radius = 100;
    private float x;
    private float y;
    private Spring xSpring;
    private Spring ySpring;
    private SpringSystem springSystem;
    private SpringConfig COASTING;
    private VelocityTracker velocityTracker;
    private SpringConfig CONVERGING = SpringConfig.fromOrigamiTensionAndFriction(20, 3);

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

        COASTING = SpringConfig.fromOrigamiTensionAndFriction(0, 0.5);
        COASTING.tension = 0;
        springSystem = SpringSystem.create();
        springSystem.addListener(this);
        xSpring = springSystem.createSpring();
        ySpring = springSystem.createSpring();
        xSpring.addListener(this);
        ySpring.addListener(this);
        this.createSuperDroid();
        getDisplaySize();

        xSpring.setCurrentValue(displaySize.x / 2f).setAtRest();
        ySpring.setCurrentValue(displaySize.y / 2f).setAtRest();

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
        float touchX = event.getRawX();
        float touchY = event.getRawY();
        boolean ret = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialTouchX = touchX;
                initialTouchY = touchY;
                lastX = initialTouchX;
                lastY = initialTouchY;
                velocityTracker = VelocityTracker.obtain();
                velocityTracker.addMovement(event);
                dragging = true;
                ret = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!dragging) {
                    break;
                }
                velocityTracker.addMovement(event);
                float offsetX = lastX - touchX;
                float offsetY = lastY - touchY;
                xSpring.setCurrentValue(xSpring.getCurrentValue() - offsetX).setAtRest();
                ySpring.setCurrentValue(ySpring.getCurrentValue() - offsetY).setAtRest();
                checkConstraints();
                ret = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!dragging) {
                    break;
                }
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                dragging = false;
                ySpring.setSpringConfig(COASTING);
                xSpring.setSpringConfig(COASTING);
                initialTouchX = 0;
                initialTouchY = 0;
                xSpring.setVelocity(velocityTracker.getXVelocity());
                ySpring.setVelocity(velocityTracker.getYVelocity());
                ret = true;
        }
        lastX = touchX;
        lastY = touchY;
        return ret;
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

    @Override
    public void onSpringUpdate(Spring spring) {
        x = (float) xSpring.getCurrentValue();
        y = (float) ySpring.getCurrentValue();
        params.x = (int) x;
        params.y = (int) y;
        windowManager.updateViewLayout(bubbleLayout, params);
    }

    @Override
    public void onSpringAtRest(Spring spring) {}

    @Override
    public void onSpringActivate(Spring spring) {}

    @Override
    public void onSpringEndStateChange(Spring spring) {}

    @Override
    public void onBeforeIntegrate(BaseSpringSystem springSystem) {}

    @Override
    public void onAfterIntegrate(BaseSpringSystem springSystem) {
        checkConstraints();
    }

    private void checkConstraints() {
        if (x + radius >= displaySize.x) {
            xSpring.setVelocity(-xSpring.getVelocity());
            xSpring.setCurrentValue(xSpring.getCurrentValue() - (x + radius - displaySize.x), false);
        }
        if (y + radius >= displaySize.y) {
            ySpring.setVelocity(-ySpring.getVelocity());
            ySpring.setCurrentValue(ySpring.getCurrentValue() - (y + radius - displaySize.y), false);
        }
        if (x - radius <= 0) {
            xSpring.setVelocity(-xSpring.getVelocity());
            xSpring.setCurrentValue(xSpring.getCurrentValue() - (x - radius), false);
        }
        if (y - radius <= 0) {
            ySpring.setVelocity(-ySpring.getVelocity());
            ySpring.setCurrentValue(ySpring.getCurrentValue() - (y - radius), false);
        }
    }

    private void getDisplaySize() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);
    }
}
