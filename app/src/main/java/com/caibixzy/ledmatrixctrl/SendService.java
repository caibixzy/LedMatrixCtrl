package com.caibixzy.ledmatrixctrl;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import android.support.v4.app.NotificationCompat;
import android.graphics.BitmapFactory;

public class SendService extends Service {

    private boolean threadDisable;
    /*
    private void improvePriority() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.SendService1.class), 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Foreground Service")
                .setContentText("Foreground Service Started.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        notification.contentIntent = contentIntent;
        startForeground(1, notification);
    }
    */
    private static final int NOTIFICATION_DOWNLOAD_PROGRESS_ID = 0x0001;

    /**
     * Notification
     */

    public void createNotification(){
        //使用兼容版本
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        //设置状态栏的通知图标
        builder.setSmallIcon(R.mipmap.ic_launcher);
        //设置通知栏横条的图标
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        //禁止用户点击删除按钮删除
        builder.setAutoCancel(false);
        //禁止滑动删除
        builder.setOngoing(true);
        //右上角的时间显示
        builder.setShowWhen(true);
        //设置通知栏的标题内容
        builder.setContentTitle("I am Foreground Service!!!");
        //创建通知
        Notification notification = builder.build();
        //设置为前台服务
        startForeground(NOTIFICATION_DOWNLOAD_PROGRESS_ID,notification);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        //createNotification();
        //improvePriority();

        Toast toast=Toast.makeText(getApplicationContext(), "Hello, world",Toast.LENGTH_LONG);
        toast.show();

            /*
            new Thread(new Runnable(){
                @Override
                public void run(){
                    while(!threadDisable){

                        int c = sketch.color(sketch.frameCount % 255, (sketch.frameCount * 3) % 255, (sketch.frameCount * 7) % 255);

                        sketch.background(c);

                        // fill dmx array
                        sketch.dmxData[0] = (byte) sketch.red(c);
                        sketch.dmxData[1] = (byte) sketch.green(c);
                        sketch.dmxData[2] = (byte) sketch.blue(c);

                        // send dmx to localhost
                        sketch.artnet.unicastDmx("2.0.0.1", 0, 0, sketch.dmxData);

                        sketch.delay(100);
                    }
                }
            }).start();*/
    }

    @Override
    public void onDestroy(){
        stopForeground(true);
        super.onDestroy();
        this.threadDisable=true;
    }
}
