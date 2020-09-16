package com.caibixzy.ledmatrixctrl;

import android.app.Service;
import android.os.Bundle;
import android.content.Intent;
import android.os.IBinder;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.support.v7.app.AppCompatActivity;

import ch.bildspur.artnet.ArtNetClient;
import processing.android.PFragment;

import android.widget.Toast;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.PopupWindow;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static sketch_191214b sketch;
    private Button generated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FrameLayout frame = new FrameLayout(this);
        //frame.setId(CompatUtils.getUniqueViewId());
        //setContentView(frame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        //        ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(R.layout.main);

        sketch = new sketch_191214b();
        FrameLayout frame = findViewById(R.id.framelayout);
        generated = findViewById(R.id.generated);

        PFragment fragment = new PFragment(sketch);
        fragment.setView(frame, this);

        Intent intent=new Intent(MainActivity.this,SendService1.class);
        startService(intent);

        intent=new Intent(MainActivity.this,MainMixingService.class);
        startService(intent);

        generated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGenDialog();
            }
        });
    }

    /**
     * 弹窗
     */
    private void showGenDialog(){
        View view = LayoutInflater.from(this).inflate(R.layout.generatedtext, null);

        // 创建PopupWindow对象，指定宽度和高度
        PopupWindow window = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //window.setWidth(tvReport.getWidth());
        // 设置动画
        //window.setAnimationStyle(R.style.popup_window_anim);
        // 设置背景颜色
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#808080")));
        // 设置可以获取焦点
        window.setFocusable(true);
        // 设置可以触摸弹出框以外的区域
        //window.setOutsideTouchable(true);
        //设置PopupWindow可触摸
        //window.setTouchable(true);
        // 更新popupwindow的状态
        window.update();
        // 以下拉的方式显示，并且可以设置显示的位置
        //window.showAsDropDown(tvReport, 0, 20);
        window.showAtLocation(generated, Gravity.CENTER, 0, 0);//这里的50是因为我底部按钮的高度是50
        Button btn = view.findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast=Toast.makeText(getApplicationContext(), "Hello, world!",Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (sketch != null) {
            sketch.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (sketch != null) {
            sketch.onNewIntent(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (sketch != null) {
            sketch.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (sketch != null) {
            sketch.onBackPressed();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Intent intent=new Intent(MainActivity.this, SendService1.class);
        stopService(intent);
    }

    public static class SendService1 extends Service {

        private boolean threadDisable;

        int pix;
        byte[] dmx = new byte[1024];
        byte[] dmx0 = new byte[512];
        byte[] dmx1 = new byte[512];

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {

            super.onCreate();

            ArtNetClient artnet;

            // create artnet client without buffer (no receving needed)
            artnet = new ArtNetClient(null);
            artnet.start();

            Toast toast=Toast.makeText(getApplicationContext(), "Hello, world",Toast.LENGTH_LONG);
            toast.show();

            new Thread(new Runnable(){
                @Override
                public void run(){
                    while(!threadDisable){
                        /*
                        i++;

                        sketch.c = sketch.color(i % 50, (i * 3 + 100) % 50, (i * 7 + 200) % 50);

                        //sketch.background(c);

                        // fill dmx array
                        for(int j=0; j<10; j++) {
                            sketch.dmxData[j * 3 + 0] = (byte) sketch.red(sketch.c);
                            sketch.dmxData[j * 3 + 1] = (byte) sketch.green(sketch.c);
                            sketch.dmxData[j * 3 + 2] = (byte) sketch.blue(sketch.c);
                        }
                        */
                        if(sketch.feedPreviewImgA != null) {
                            for (int i = 0; i < sketch.feedPreviewImgA.width; i++) {
                                for (int j = 0; j < sketch.feedPreviewImgA.height; j++) {
                                    pix = sketch.feedPreviewImgA.get(i, j);
                                    if(i % 2 == 0) {
                                        dmx[48 * i + j * 3] = (byte) sketch.round(sketch.red(pix) * sketch.FeedIntensityA);
                                        dmx[48 * i + j * 3 + 1] = (byte) sketch.round(sketch.green(pix) * sketch.FeedIntensityA);
                                        dmx[48 * i + j * 3 + 2] = (byte) sketch.round(sketch.blue(pix) * sketch.FeedIntensityA);
                                    }
                                    else {
                                        dmx[48 * (i + 1) - (j * 3 + 3)] = (byte) sketch.round(sketch.red(pix) * sketch.FeedIntensityA);
                                        dmx[48 * (i + 1) - (j * 3 + 2)] = (byte) sketch.round(sketch.green(pix) * sketch.FeedIntensityA);
                                        dmx[48 * (i + 1) - (j * 3 + 1)] = (byte) sketch.round(sketch.blue(pix) * sketch.FeedIntensityA);
                                    }
                                }
                            }
                            // send dmx to localhost
                            dmx0 = Arrays.copyOfRange(dmx,0,510);
                            dmx1 = Arrays.copyOfRange(dmx,510,1020);
                            artnet.unicastDmx("2.0.0.1", 0, 0, dmx0);
                            artnet.unicastDmx("2.0.0.1", 0, 1, dmx1);
                        }

                        sketch.delay(30);
                    }
                }
            }).start();
        }

        @Override
        public void onDestroy(){
            super.onDestroy();
            this.threadDisable=true;
        }
    }

    public static class MainMixingService extends Service {

        private boolean threadDisable;

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {

            super.onCreate();

            new Thread(new Runnable(){
                @Override
                public void run(){
                    while(!threadDisable) {
                        if (sketch.isSetup) {
                            //------------------------------------------------ Feed A Content Update ----------------------------------------------------------------

                            try {
                                if ((sketch.millis() - sketch.layer0Holdmills) > sketch.cDefaultContentFPS) {
                                    if (sketch.tile0PlayMode == 0) {
                                        sketch.layer0Holdmills = sketch.millis();
                                        sketch.genContentText.buildFrame();
                                        sketch.mediaImage0 = sketch.genContentText.localPGBuf.get();
                                        //genContentBlocks.buildFrame();
                                        //mediaImage = genContentBlocks.localPGBuf.get();
                                        sketch.mixFeeds = true;
                                    }
                                }
                                if ((sketch.millis() - sketch.layer1Holdmills) > sketch.cDefaultContentFPS) {
                                    sketch.layer1Holdmills = sketch.millis();
                                    //genContentBlocks.buildFrame();
                                    //mediaImage1 = genContentBlocks.localPGBuf.get();
                                    //genContentPlasma.buildFrame();
                                    //mediaImage0 = genContentPlasma.localPGBuf.get();
                                    sketch.mixFeeds = true;
                                }
                            } catch (Exception e) {
                                sketch.println("FeedA had an error");
                            }
                            //------------------------------------------------ Feeds Updated, Now Mix Them ----------------------------------------------------------------

                            if ((sketch.millis() - sketch.holdMillisDraw) > sketch.matrix.outputFPS) {
                                //if (FeedPlayModeA == 1 && FeedPlayModeB == 1) mixFeeds = true; //if both feeds are paused, still need to mix them for the output
                                //If any media content has changed and the output FPS timer has elapsed, indicate to the transmission thread to send a packet
                                if (sketch.mixFeeds == true) {
                                    //println("mixed "+(millis()-holdMillisDraw)+"    OutFPS: "+matrix.outputFPS);
                                    //now mix the layers
                                    sketch.MainMixFunction(); //mix FeedA and FeedB
                                    //transmitPixelBuffer = MixedContentGBuf.get();
                                    sketch.mixFeeds = false;
                                    sketch.feedPreviewImgA = sketch.LayerContentGBufA.get();
                                    sketch.holdMillisDraw = sketch.millis(); //update here
                                    //PacketReadyForTransmit = true; //indicates to transmission thread that a new frame is ready
                                    //delay(10); //should sleep thread - does reduce CPU usage it seems
                                } //end mixFeeds if()
                            }
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onDestroy(){
            super.onDestroy();
            this.threadDisable=true;
        }
    }
}