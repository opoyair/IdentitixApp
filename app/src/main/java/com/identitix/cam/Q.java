package com.identitix.cam;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Q extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q);

        ImageView img = (ImageView) findViewById(R.id.imageView3);

        img.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle userData = new Bundle();
                userData.putString("name","Dany");
                Intent in  = new Intent(getApplicationContext(),Success.class);
                in.putExtras(userData);
                startActivity(in);
                finish();

            }
        });


    }
}
