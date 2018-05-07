package com.lke.tcptest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Button btn,btn_Next;
    TextView tv;
    TextView tv2;

    Messenger mService = null;
    boolean mBound;

    // generate android random key
    Random rnd = new Random();
    int randNum = rnd.nextInt(100);


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, tcpService.class), mConnection,
               Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.btn);
        btn_Next = (Button) findViewById(R.id.btn_next);
      //  tv = (TextView) findViewById(R.id.tv);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv2.setText(" "+randNum);

        SharedPreferences KEY = getSharedPreferences("KEY", 0);
        SharedPreferences.Editor editor;

        editor = KEY.edit();
        editor.putString("made",""+randNum);
        editor.commit();

        // *** send randNum ***
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    intent.putExtra("randNum", tv2.getText().toString());
                if(mBound){
                    Message msg = Message.obtain(null, tcpService.SEND_KEY, tv2.getText().toString());
                    try{
                       mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();}
                } else Toast.makeText(getApplicationContext(),
                        "no bound with tcpService", Toast.LENGTH_SHORT).show();
         //       Intent intent = new Intent(getApplicationContext(), tcpService.class);
            }
        });

        btn_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), infoActivity.class);
                startActivity(intent);
            }
        });
    } // onCreate()

}

