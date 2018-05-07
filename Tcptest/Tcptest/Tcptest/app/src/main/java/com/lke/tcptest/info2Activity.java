package com.lke.tcptest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class info2Activity extends AppCompatActivity {

    Button btn_info2_next;
    EditText Atime;
    String AlarmTime;

    Messenger mService = null;
    boolean mBound;

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
        Log.d("*****","infoActivity started");
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info2);

        btn_info2_next = (Button) findViewById(R.id.btn_info2_next);
        Atime = (EditText) findViewById(R.id.editText);




        btn_info2_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Message msg = Message.obtain(null, tcpService.SEND_INFO);

                AlarmTime = Atime.getText().toString();
                if(AlarmTime == "") {
                    Toast.makeText(getApplicationContext(),
                            "기본 알림은 10초 전입니다.", Toast.LENGTH_SHORT).show();
                    AlarmTime = "10";
                } //if

                msg.obj = "T"+AlarmTime;

                if(mBound){
                    try{
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();}
                } else Toast.makeText(getApplicationContext(),
                        "no bound with tcpService", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), nfc2Activity.class);
                Log.d("*****","goto firstActivity");
                startActivity(intent);

            }
        });

    }
}
