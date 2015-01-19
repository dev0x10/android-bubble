package xyz.devlog.androidbubble.example;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent serviceIntent = new Intent(this, BubbleService.class);
        startService(serviceIntent);
        finish();
    }
}
