package org.iqdb.iqdbmobile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        this app won't appear in the launcher. Instead open a browser and go to iqdb.
//        much speed, so lazy, wao.
    }
}
