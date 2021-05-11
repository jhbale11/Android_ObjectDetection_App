package com.example.rtmp210121_095030;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserInfo implements Serializable {

    UserInfo(){
        userName="이름";
        userID="2020-00000";
        macAdr="1";
        rtmpURL="rtmp://1.1.1.1";
        lecID="logicDesign.midterm_20210110";
        table="logicdesign_midterm_20210110_1200_1330";
    }

    public void setUserName(String userName) { this.userName = userName; }
    public void setUserID(String userID) { this.userID = userID; }
    public void setMacAdr(String macAdr) { this.macAdr = macAdr; }
    public void setRtmpURL(String rtmpURL) { this.rtmpURL = rtmpURL; }
    public void setLecID(String lecID) { this.lecID = lecID; }
    public void setTable(String table) { this.table = table; }

    public String getUserName() { return userName; }
    public String getUserID() { return userID; }
    public String getMacAdr() { return macAdr; }
    public String getRtmpURL() { return rtmpURL; }
    public String getLecID() { return lecID; }
    public String getTable() { return table; }
    public String getLecName() {
        String[] part = table.split("_");
        try{
            return  part[0];
        } catch (Exception e){
            return "no course name available";
        }
    }
    public String getTime() {
        String[] part = table.split("_");
        try{
            return part[3]+"-"+part[4];
        } catch (Exception e) {
            return "0000-0000";
        }
    }

    public Map<String, String> getParameterSet(int type) {
        Map<String, String> parameterSet = new HashMap<String, String>();
        switch (type){
            case 0:
                parameterSet.put("num",userID);
                parameterSet.put("name",userName);
                parameterSet.put("mac",macAdr);
                break;
            case 1:
                parameterSet.put("num",userID);
                parameterSet.put("name",userName);
                parameterSet.put("mac", macAdr);
                parameterSet.put("lec_id",lecID);
                break;
            default:
                parameterSet.put("num",userID);
                parameterSet.put("name",userName);
                parameterSet.put("lec_id",lecID);
                parameterSet.put("mac",macAdr);
                break;
        }
        return parameterSet;
    }

    public String getSummary() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("userName : ").append(userName);
        stringBuffer.append("\nuserID : ").append(userID);
        stringBuffer.append("\nmacAddress : ").append(macAdr);
        stringBuffer.append("\nRTMP URL : ").append(rtmpURL);
        stringBuffer.append("\nLEC ID : ").append(lecID);
        stringBuffer.append("\ntable : ").append(table);
        return stringBuffer.toString();
    }

    private String userName, userID, macAdr, rtmpURL, lecID, table;
}
