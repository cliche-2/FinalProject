package com.lke.tcptest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class FirstActivity extends AppCompatActivity {
    Button w_start;
    Button w_finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        w_start = (Button)findViewById(R.id.btn_start);
        w_finish = (Button)findViewById(R.id.btn_finish);

        w_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), nfcActivity.class);
                startActivity(intent);
            }
        });

        w_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), finishActivity.class);
                startActivity(intent);
            }
        });
    }
}
