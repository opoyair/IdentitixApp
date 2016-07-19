package com.identitix.cam;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Success extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        TextView text  = (TextView) findViewById(R.id.editText2);
        Bundle b = this.getIntent().getExtras();
        String name = b.getString("name");
        text.setText("Hi," + name.toString());

        Button okay  = (Button) findViewById(R.id.button33);
        okay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent in  = new Intent(getApplicationContext(),CamTestActivity.class);
                startActivity(in);
                finish();

            }
        });

    }
}
