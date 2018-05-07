package com.lke.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class tcpService extends Service {

    int MessageCode = 0;
    int Number = 0;

    Messenger sService = null;
    Message msg = Message.obtain();

    tcpThread myThread = new tcpThread();
    private BufferedReader buffRecv;
    private BufferedWriter buffSend;
    private String sString;
    public String tm;

    @Override
    public void onCreate(){
        super.onCreate();

        myThread.start();
        Log.d("*****", "service onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("*****","onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    public class tcpThread extends Thread {

        private Socket mSocket;

     //   private final String ip = "192.168.219.104";
        private int        port =  35358;

        @Override
        public void run() {

            // connect
            try {
                Log.d("TCP", "S: Connecting...");
                ServerSocket serverSocket = new ServerSocket(port);

                while( ! Thread.interrupted() ) {
                    Socket client = serverSocket.accept();
                    Log.d("TCP", "S: Receiving...");
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        String aLine = in.readLine();
                        Number = Integer.valueOf(""+aLine.charAt(1));
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
                        editor.putInt(""+Number,MessageCode);
                        editor.commit();
                        Log.d("*****", "update information saved! ");


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


    // Handler of incoming messages from clients.
    class IncomingHandler extends Handler {
        @Override

        public void handleMessage(Message msg) {
        }
    } // IncomingHandler()

    // Target we publish for clients to send messages to IncomingHandler.
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    // When binding to the service, we return an interface to our messenger
    // for sending messages to the service.
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }


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
            case 2:
                builder .setContentTitle("세탁기"+Number)
                        .setContentText("반납 완료");
            case 3:
                builder .setContentTitle("세탁기"+Number)
                        .setContentText("불법 접근 시도 감지");
            case 4:
                builder .setContentTitle("세탁기"+Number)
                        .setContentText("사용 시간 초과");
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
}