package com.example.rtmp210121_095030;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn, btn1, btn2;
    private String RESPONSE_MESSAGE = "";
    private UserInfo userInfo = new UserInfo();
    private EditText editTextID, editTextName;
    private Context context;

    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.btn_debug:
                RESPONSE_MESSAGE = "rtmp://live-sel.twitch.tv/app/live_";
                userInfo.setRtmpURL(RESPONSE_MESSAGE);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("UserInfoFromLogin",userInfo);
                startActivity(intent);
                break;

            case R.id.btn_login:
                /* user setting */
                Log.d("Login Activity" ,"login button clicked");
                userInfo.setUserID(editTextID.getText().toString());
                userInfo.setUserName(editTextName.getText().toString());

                /* get exam info */
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        RESPONSE_MESSAGE = new ServerRequest(0, userInfo, context).Request();

                        //RESPONSE_MESSAGE = "calculus1.midterm_20200101";
                        Log.d("Login Activity", "RESPONSE MESSAGE : " + RESPONSE_MESSAGE);
                        Pattern pattern = Pattern.compile("^\\w*_\\w*_\\d{8}_\\d{4}_\\d{4}$");
                        if (pattern.matcher(RESPONSE_MESSAGE).find()){
                            // subject_midterm_20200101_0000_0000 -> subject.midterm_20200101
                            userInfo.setTable(RESPONSE_MESSAGE);
                            //userInfo.setLecID(RESPONSE_MESSAGE);

                            String[] part = RESPONSE_MESSAGE.split("_");
                            userInfo.setLecID(part[0] + "." +part[1] + "_" + part[2]);

                            ShowToastMessage("Subject : " + part[0]);
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn1.setEnabled(true);
                                    btn1.setText(String.format("%s (%s)", part[0], part[1]));
                                }
                            });
                        } else {
                            ShowToastMessage(RESPONSE_MESSAGE);
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn1.setEnabled(false);
                                    btn1.setText("No Exam Available");
                                }
                            });
                        }
                    }
                }).start();
                Log.d("Login Activity", "\n" + userInfo.getSummary());
                break;

            case R.id.btn_request:
                Log.d("Login Activity" ,"request button clicked");
                /* request RTMP endpoint */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RESPONSE_MESSAGE = new ServerRequest(1, userInfo, context).Request();
                        //RESPONSE_MESSAGE = "rtmp://~~~";
                        Pattern pattern = Pattern.compile("^rtmp://\\S*$");
                        if (pattern.matcher(RESPONSE_MESSAGE).find()) {
                            userInfo.setRtmpURL(RESPONSE_MESSAGE);
                            SaveUserInfo();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("UserInfoFromLogin", userInfo);
                            startActivity(intent);
                        } else {
                            ShowToastMessage("Failed to get Streaming URL");
                        }
                    }
                }).start();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* permission */
        TedPermission.with(this)
                .setPermissionListener(permission)
                .setRationaleMessage("Permission Granted.")
                .setDeniedMessage("Permission Denied.")
                .setPermissions(
                        Manifest.permission.INTERNET,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();

        /* load user info */
        SharedPreferences sf = getSharedPreferences("pref",MODE_PRIVATE);
        String id = sf.getString("id","");
        String name = sf.getString("name","");

        /* view & parameter setting */
        context = this;
        userInfo.setUserID(id);
        userInfo.setUserName(name);
        userInfo.setMacAdr(GetMacAddress());
        editTextID = (EditText) findViewById(R.id.editTextID);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextID.setText(id);
        editTextName.setText(name);
        btn = (Button) findViewById(R.id.btn_login);
        btn.setOnClickListener(this);
        btn1 = (Button) findViewById(R.id.btn_request);
        btn1.setOnClickListener(this);
        btn2 = (Button) findViewById(R.id.btn_debug);
        btn2.setOnClickListener(this);
    }


    private PermissionListener permission = new PermissionListener() {
        /* permission listener setup */
        @Override
        public void onPermissionGranted() {
            Toast.makeText(LoginActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(LoginActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SaveUserInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SaveUserInfo();
    }

    private void SaveUserInfo() {
        /* save user info */
        SharedPreferences sf = getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();
        editor.putString("id",editTextID.getText().toString());
        editor.putString("name",editTextName.getText().toString());
        editor.commit();
    }

    private String GetMacAddress() {
        return "0";
    }

    private void ShowToastMessage(String str){
        LoginActivity.this.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }
        });
    }
}


