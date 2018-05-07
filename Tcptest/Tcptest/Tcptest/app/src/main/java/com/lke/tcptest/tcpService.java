package com.lke.tcptest;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class tcpService extends Service {

    static final int SEND_KEY       = 0; // for test
    static final int SEND_INFO      = 1;
    //   SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
    //   SharedPreferences.Editor editor = pref.edit();




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

        private final String ip = "192.168.123.102";
 //       private final String ip = "192.168.219.112";
        private int        port =  35357;
        private Handler myHandler = null;

        @Override
        public void run() {

            // connect
            try {
                mSocket = new Socket(ip,port);
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
            if(mSocket == null)         return;

            // buffer
            try {
                buffRecv = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                buffSend = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
            }

            // ** Message Code **
            // P : save value  >> call setActivity
            // A : Alert  1.finish 2.alert
            String aLine = null;
            Log.d("*****", "socket_thread loop started");
            while( ! Thread.interrupted() ) {
                    Log.d("*****", "reading thread started");
//            while(true) {
                    try {
                        Log.d("*****","fore-read");
                        aLine = buffRecv.readLine();
                        Log.d("*****", "read");

                        // password Received
                        if (aLine.startsWith("P")) {
                            // 받은 비밀번호 값 저장하기
                            SharedPreferences KEY = getSharedPreferences("KEY", 0);
                            SharedPreferences.Editor editor;
                            editor = KEY.edit();
                            editor.putString("given",aLine.substring(1));
                            editor.commit();
                            Log.d("*****", "data received" + aLine);

                        // alert received
                        } else if (aLine.startsWith("A")) {
                            Log.d("*****", "Alarm received");
                            NotificationSomethings();
                        } else if (aLine.startsWith("a")) {
                            Log.d("*****", "proAlarm received");
                            alertNotification();
                        } else if (aLine.startsWith("Q")) {
                            Log.d("*****", "Alert received");
                            alertNotification2();

                        }

                    }catch (IOException e) {
                        System.out.println(e);
                        e.printStackTrace();
                    }
 //               }
            }// while()

            try {
                buffRecv.close();
                buffSend.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } // run()
    } // tcpThread
/*
    //added
    Runnable sendString = new Runnable() {
        @Override
        public void run() {
            PrintWriter out = new PrintWriter(buffSend, true);
            out.println(sString);
        }
    };
  */


    // Handler of incoming messages from clients.
    //      SEND_KEY : send random number to server (for test)
    //      SEND_INFO: send selection values to server
    class IncomingHandler extends Handler {
        @Override

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_KEY: // temporary case
                    sString = msg.obj.toString();
       //             sendString.run();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PrintWriter out = new PrintWriter(buffSend, true);
                            out.println(sString);
                        }
                    }).start();
                    Log.d("*****","write finished");
                    Toast.makeText(getApplicationContext(), "sending "+msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case SEND_INFO:
                    sString = msg.obj.toString();
                    tm = sString;
   //                 sendString.run();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PrintWriter out = new PrintWriter(buffSend, true);
                            out.println(sString);
                        }
                    }).start();
                    Toast.makeText(getApplicationContext(), "sending info"+msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
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

        Intent notificationIntent = new Intent(this, nfc2Activity.class);
//        notificationIntent.putExtra("notificationId", "0"); //전달할 값
        PendingIntent contentIntent = PendingIntent.getActivity
                (this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("세탁 종료")
                .setContentText("세탁 종료")
                .setSmallIcon(R.mipmap.ic_launcher)
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

    public void alertNotification() {
        NotificationCompat.Builder builder2 =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("알림")
                .setContentText("종료"+tm+"초 전");

        Intent notificationIntent = new Intent(this, nfc2Activity.class);
        PendingIntent contentIntent = PendingIntent.getActivity
                (this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder2.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder2.build());

    }

    public void alertNotification2() {
        NotificationCompat.Builder builder2 =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("경고")
                        .setContentText(" 유효하지 않은 uid 인증 시도 감지 ");

        Intent notificationIntent = new Intent(this, nfc2Activity.class);
        PendingIntent contentIntent = PendingIntent.getActivity
                (this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder2.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder2.build());

    }

}