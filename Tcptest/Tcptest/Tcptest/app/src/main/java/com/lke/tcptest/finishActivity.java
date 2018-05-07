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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class finishActivity extends AppCompatActivity {

    Messenger mService = null;
    boolean mBound;
    Button btnEnd, btnRet;
    int id = 0;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        // noti
        /*
        CharSequence s=" ";

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            s = "error";
        }
        else {
            id = extras.getInt("notificationId");
        }
        TextView t = (TextView) findViewById(R.id.textView);
        s = s+"test"+id;
        Toast.makeText(getApplicationContext(),""+s,Toast.LENGTH_SHORT).show();
        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(id);
        */


        btnEnd = (Button) findViewById(R.id.btn_end);
        btnRet = (Button) findViewById(R.id.btn_ret);

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),tcpService.class);
                stopService(intent);
                mBound = false;
                Toast.makeText(getApplicationContext(),
                        "stopService()", Toast.LENGTH_SHORT).show();

                SharedPreferences KEY = getSharedPreferences("KEY", 0);
                SharedPreferences.Editor editor = KEY.edit();
                editor.remove("given");
                editor.remove("made");
                editor.commit();

            }
        });

        btnRet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    Message msg = Message.obtain(null, tcpService.SEND_KEY);
                    int a=-1,b=-1; String key = "1";
                    SharedPreferences KEY = getSharedPreferences("KEY", 0);
                    a = Integer.parseInt(KEY.getString("given", ""));
                    b = Integer.parseInt(KEY.getString("made",""));
                    if (a==-1 || b==-1)
                        Log.d("*****","data corrupted");
                    key = "W"+(a^b);
                    msg.obj = key;

                    try{
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();}
                } else Toast.makeText(getApplicationContext(),
                        "no bound with tcpService", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
