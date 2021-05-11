package com.example.rtmp210121_095030;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.contentcapture.ContentCaptureSession;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.encoder.input.video.CameraCallbacks;
import com.pedro.encoder.input.video.CameraHelper;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtplibrary.rtmp.RtmpCamera2;
import com.pedro.rtplibrary.view.GlInterface;
import com.pedro.rtplibrary.view.OpenGlView;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ConnectCheckerRtmp, SurfaceHolder.Callback, View.OnClickListener {

    private OpenGlView openGlView;
    private RtmpCamera2 rtmpCamera2;

    private int previewHeight;
    private int previewWidth;
    private int streamHeight = 480;
    private int streamWidth = 640;
    private int elapsedTime = 0;
    private String RESPONSE_MESSAGE;

    private UserInfo userInfo;

    private Button btn_start;
    private Button btn_option;
    private TextView textViewResolution;
    private TextView textViewUserInfo;
    private TextView textViewRecording;
    private ImageView imageViewCapture;
    private TextView textViewStreamInfo;
    private TimerTask timerTaskStreamInfo = new TimerTask() {
        @Override
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (rtmpCamera2.isStreaming()) {
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("cache Size : " + Integer.toString(rtmpCamera2.getCacheSize()) + "\n");
                        stringBuffer.append("Bitrate : " + Integer.toString(rtmpCamera2.getBitrate()) + "\n");
                        stringBuffer.append("sent frame : " + Long.toString(rtmpCamera2.getSentVideoFrames()) + "\n");
                        stringBuffer.append("resolution : "+Integer.toString(rtmpCamera2.getStreamWidth()));
                        stringBuffer.append(" x "+Integer.toString(rtmpCamera2.getStreamHeight()));
                        textViewStreamInfo.setTextSize(20);
                        textViewStreamInfo.setText(stringBuffer.toString());
                    } else {
                        textViewStreamInfo.setTextSize(40);
                        textViewStreamInfo.setText("송출 중이 아닙니다");
                    }
                }
            });
        }
    };
    private TimerTask timerTaskRecording = new TimerTask() {
        @Override
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    elapsedTime = rtmpCamera2.isStreaming() ? elapsedTime + 1 : 0;
                    textViewRecording.setText(String.format("%02d : %02d : %02d",
                            elapsedTime / 3600,
                            (elapsedTime % 3600) / 60,
                            elapsedTime % 60));
                }
            });
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                    if (rtmpCamera2.isStreaming()) { // only when user explicitly terminated streaming
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new ServerRequest(3, userInfo, MainActivity.this).Request();
                        }
                    }).start();
                }
                StreamManager(true);
                break;
            case R.id.btn_camera_change:
                if (rtmpCamera2.isStreaming()){
                    ShowToastMessage("can't change camera while streaming");
                } else {
                    rtmpCamera2.stopPreview();
                    if (rtmpCamera2.isFrontCamera()){
                        rtmpCamera2.startPreview(CameraHelper.Facing.BACK, previewWidth, previewHeight);
                    } else {
                        rtmpCamera2.startPreview(CameraHelper.Facing.FRONT, previewWidth, previewHeight);
                    }
                }
                break;
            case R.id.textViewUserInfo:
                Log.d("textView clicked","");
                v.setVisibility(((v.getVisibility()==View.VISIBLE) ? View.INVISIBLE : View.VISIBLE));
                break;
        }
    }

    private void StreamManager(boolean type) { // type ? start : stop
        if (!rtmpCamera2.isStreaming() && type) {
            if (rtmpCamera2.prepareVideo(streamWidth, streamHeight, 1000 * 1000)
                    && rtmpCamera2.prepareAudio()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new ServerRequest(2, userInfo, MainActivity.this).Request();
                    }
                }).start();
                rtmpCamera2.startStream(userInfo.getRtmpURL());
                elapsedTime = 0;
                ShowToastMessage("start streaming");
            } else {
                ShowToastMessage("Failed to start streaming");
            }
        } else if (rtmpCamera2.isStreaming()) {
            rtmpCamera2.stopStream();
            ShowToastMessage("stop streaming");
        }
        if (rtmpCamera2.isStreaming()){
            btn_start.setBackgroundResource(R.drawable.streaming_button_inside_1);
        } else {
            btn_start.setBackgroundResource(R.drawable.streaming_button_inside_0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        userInfo = (UserInfo) intent.getSerializableExtra("UserInfoFromLogin");
        imageViewCapture = (ImageView) findViewById(R.id.imageViewCapture);

        /* set preview size */
        Display display = getWindowManager().getDefaultDisplay();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int ORIENTATION = display.getRotation();
        int WIDTH = metrics.widthPixels;
        int HEIGHT = metrics.heightPixels;
        Log.d("Streaming Activity","orientation : " + ORIENTATION + " | " + WIDTH + "x" + HEIGHT);

        if (WIDTH * 3 > 4 * HEIGHT){
            previewWidth = HEIGHT * 4 / 3;
            previewHeight = HEIGHT;
        } else {
            previewWidth = WIDTH;
            previewHeight = WIDTH * 3 / 4;
        }
        openGlView = (OpenGlView) findViewById(R.id.openGlView);
        openGlView.getHolder().setFixedSize(previewWidth, previewHeight);
        openGlView.getHolder().addCallback(this);
        rtmpCamera2 = new RtmpCamera2(openGlView, this);
        rtmpCamera2.setReTries(10);
        rtmpCamera2.enableAutoFocus();

        textViewStreamInfo = (TextView) findViewById(R.id.textViewCurrentState);
        textViewUserInfo = (TextView) findViewById(R.id.textViewUserInfo);
        textViewRecording = (TextView) findViewById(R.id.textViewTimer);
        textViewUserInfo.setText(userInfo.getSummary());

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);

        btn_option = (Button) findViewById(R.id.btn_camera_change);
        btn_option.setOnClickListener(this);

        Timer timerStreamInfo = new Timer();
        timerStreamInfo.schedule(timerTaskStreamInfo,0,100);

        Timer timerCaptureInfo = new Timer();
        timerCaptureInfo.schedule(timerTaskRecording, 0, 1000);

        textViewStreamInfo.setVisibility(View.INVISIBLE);
        //textViewUserInfo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowToastMessage("Connection success");
            }
        });
    }

    @Override
    public void onConnectionFailedRtmp(@NonNull String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rtmpCamera2.reTry(5000, reason)) {
                    ShowToastMessage("Please retry");
                    StreamManager(false);
                } else {
                    ShowToastMessage("Connection failed" + reason);
                    StreamManager(false);
                }
            }
        });
    }

    @Override
    public void onNewBitrateRtmp(long bitrate) {
    }

    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowToastMessage("Disconnected");
                StreamManager(false);
            }
        });
    }

    @Override
    public void onAuthErrorRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowToastMessage("Auth Error");
                StreamManager(false);
            }
        });
    }

    @Override
    public void onAuthSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowToastMessage("Auth success");
            }
        });
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        rtmpCamera2.startPreview(CameraHelper.Facing.BACK, previewWidth, previewHeight);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        rtmpCamera2.startPreview(CameraHelper.Facing.BACK, previewWidth, previewHeight);
        Log.d("Streaming Activity", "SurfaceChanged to " +
                format + " width " + width + " height " + height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        StreamManager(false);
        rtmpCamera2.stopPreview();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("streaming","configuration changed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        StreamManager(false);
        rtmpCamera2.stopPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
        StreamManager(false);
        rtmpCamera2.stopPreview();
    }

    private void ShowToastMessage(String str){
        MainActivity.this.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }
}