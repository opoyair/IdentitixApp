package com.identitix.cam;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

public class LogoActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        new Thread(new Runnable() {

            @Override
            public void run() {
                SystemClock.sleep(2000);
                Intent in  = new Intent(getApplicationContext(),CamTestActivity.class);
                startActivity(in);
                finish();
            }
        }).start();
    }
}
