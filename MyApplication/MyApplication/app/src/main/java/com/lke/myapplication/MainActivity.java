package com.lke.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    static final int START_WASHER       = 1;
    static final int FINISH_WASHER      = 2;
    static final int WARN_INTRUSION     = 3;
    static final int OVER_CHARGED       = 4;

    static final int NumberOfWasher = 1;
    int MessageCode = 0;
    int Number = 0;


    TextView status_;
    Button btn_update;
/*
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

*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread sThread = new Thread(new tcpThread());
        sThread.start();

        status_ = (TextView) findViewById(R.id.status_2);
        // status_2 = ....

        btn_update = (Button) findViewById(R.id.btn_update);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatus();
                Toast.makeText(getApplicationContext(), "updated !",Toast.LENGTH_SHORT).show();
            }
        });
    } // onCreate()


    public class tcpThread extends Thread {

        //     private Socket mSocket;

        //   private final String ip = "192.168.219.104";
        private int        port =  35358;

        @Override
        public void run() {

            // connect
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                Log.d("TCP", "S: Connecting...");
                while( ! Thread.interrupted() ) {
                    Socket client = serverSocket.accept();
                    Log.d("TCP", "S: Receiving...");
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                        while(true) {
                            String aLine = in.readLine();
                            Number = Integer.valueOf("" + aLine.charAt(1));
                            Log.d("TCP", "S: Received: '" + aLine + "'");

                            // ** Message Code **
                            // S: washer started    -> status_using
                            // A: washer finished   -> status_empty
                            // W: warn intrusion    -> status_warn
                            // O: overcharged washer-> status_time's over

                            if (aLine.startsWith("S")) {
                                Log.d("*****", "washer started");
                                MessageCode = MainActivity.START_WASHER;
                                NotificationSomethings();
                                // alert received
                            } else if (aLine.startsWith("A")) {
                                Log.d("*****", "washer is returned");
                                MessageCode = MainActivity.FINISH_WASHER;
                                NotificationSomethings();
                                // warn intrusion
                            } else if (aLine.startsWith("W")) {
                                Log.d("*****", "waring intrusion");
                                MessageCode = MainActivity.WARN_INTRUSION;
                                NotificationSomethings();
                                // overcharged
                            } else if (aLine.startsWith("O")) {
                                Log.d("*****", "overcharged washer");
                                MessageCode = MainActivity.OVER_CHARGED;
                                NotificationSomethings();
                            }

                            // save received data
                            SharedPreferences pref = getSharedPreferences("update", 0);
                            SharedPreferences.Editor editor;
                            editor = pref.edit();
                            editor.putInt("" + Number, MessageCode);
                            editor.commit();
                            Log.d("*****", "update information saved! ");

                        }
                    } catch(Exception e) {
                        Log.e("TCP", "S: Error", e);
                    } finally {
                        client.close();
                        Log.d("TCP", "S: Done.");
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        } // run()
    } // tcpThread

    public void NotificationSomethings() {

        Resources res = getResources();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("notificationId", "0"); //전달할 값
        PendingIntent contentIntent = PendingIntent.getActivity
                (this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        switch (MessageCode) {
            case 1:
                builder .setContentTitle("세탁기"+Number)
                        .setContentText("사용 시작");
                break;
            case 2:
                builder .setContentTitle("세탁기"+Number)
                        .setContentText("반납 완료");
                break;
            case 3:
                builder .setContentTitle("세탁기"+Number)
                        .setContentText("불법 접근 시도 감지");
                break;
            case 4:
                builder .setContentTitle("세탁기"+Number)
                        .setContentText("사용 시간 초과");
                break;
            default:
                Log.d("*****"," MessageCode Error ");
        }

        builder .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1234, builder.build());
    }


    void updateStatus() {
        SharedPreferences pref = getSharedPreferences("update", 0);
        int s = 0;
        s = pref.getInt("1", 0);
        if (status_ == null) {Log.d("*****","failed to find view");}
        switch (s){
            case START_WASHER:
                status_.setText("working...");
                break;
            case FINISH_WASHER:
                status_.setText(" - ");
                break;
            case WARN_INTRUSION:
                status_.setText("illegal access attempt");
            case OVER_CHARGED:
                status_.setText("over-charged");
            default:
        }
/*
        for(int s = 0, i = 1, resid; i<=NumberOfWasher; i++) {
            s = pref.getInt(""+i, 0);
            if (status_ == null) {
                Log.d("*****","failed to find view");
                break;
            }
            switch (s){
                case START_WASHER:
                case FINISH_WASHER:
                case WARN_INTRUSION:
                case OVER_CHARGED:

                default:
            }


        } // for()
   */
    } // updateStatus()

}
