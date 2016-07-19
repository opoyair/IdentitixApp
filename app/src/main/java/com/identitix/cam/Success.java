package com.identitix.cam;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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
    }
}
