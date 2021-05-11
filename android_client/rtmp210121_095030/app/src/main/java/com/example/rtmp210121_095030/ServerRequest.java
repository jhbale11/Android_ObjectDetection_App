package com.example.rtmp210121_095030;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class ServerRequest {

    /*
     * send request and get response
     * type 0 : login and get subject list
     * type 1 : get RTMP url
     * type 2 : terminate streaming
     */

    public void setType(int type) {
        this.type = type;
    }
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    public String getRESPONSE_MESSAGE() {
        return RESPONSE_MESSAGE;
    }

    private int type;
    private UserInfo userInfo;
    private String REQUEST_URL;
    private String RESPONSE_MESSAGE;

    ServerRequest() {
        type = 0;
        REQUEST_URL = "";
        userInfo = new UserInfo();
    }

    ServerRequest(int type, UserInfo userInfo, Context context) {
        this.type = type;
        this.userInfo = userInfo;
        REQUEST_URL = context.getString(R.string.SERVER_URL);
        switch (type){
            case 0:
                REQUEST_URL += context.getString(R.string.COMMAND_LOGIN);
                break;
            case 1:
                REQUEST_URL += context.getString(R.string.COMMAND_ENDPOINT);
                break;
            case 2:
                REQUEST_URL += context.getString(R.string.COMMAND_INITIALIZE);
                break;
            default:
                REQUEST_URL += context.getString(R.string.COMMAND_TERMINATE);
        }
    }

    public String Request() {
        try{
            /* connection setting */
            URL url = new URL(REQUEST_URL);
            Log.d("Server Request","URL : " + REQUEST_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setConnectTimeout(10*1000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            Log.d("Server Request","Connection Setting");

            /* parameter setting */
            boolean isAnd = false;
            StringBuffer stringBuffer = new StringBuffer();
            Map<String, String> parameter = userInfo.getParameterSet(type);
            for (Map.Entry<String, String> parameter_ : parameter.entrySet()){
                if (isAnd)
                    stringBuffer.append("&");
                String key = parameter_.getKey();
                String value = parameter_.getValue();
                stringBuffer.append(key).append("=").append(value); // e.g) num=2020-00000
                isAnd = true;
            }
            String parameters = stringBuffer.toString();
            Log.d("Server Request","set parameter");
            Log.d("Server Request", "parameter : " + parameters);

            /* output stream */
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(parameters.getBytes("EUC-KR"));
            outputStream.flush();
            outputStream.close();
            Log.d("Server Request","output stream flushed");

            /* response */
            Log.d("Server Request","RESPONSE CODE : " + conn.getResponseCode());
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK){ // connection error
                RESPONSE_MESSAGE = "0";
                Log.d("Server Request","CONNECTION FAILED");
                return RESPONSE_MESSAGE;
            }
            InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream(), "EUC-KR");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            StringBuffer page = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                page.append(line);
            }
            reader.close();
            RESPONSE_MESSAGE = page.toString();
            Log.d("Server Request", "RESPONSE MESSAGE : " + RESPONSE_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            RESPONSE_MESSAGE = "0";
        }
        return RESPONSE_MESSAGE;
    }
}