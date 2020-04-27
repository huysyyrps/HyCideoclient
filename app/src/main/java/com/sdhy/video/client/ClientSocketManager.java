package com.sdhy.video.client;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClientSocketManager extends SocketManager {
    private static ClientSocketManager sockMgr = new ClientSocketManager();

    private byte[] loginMsg = new byte[28];

    private Map<String, Map<String, String>> lineMap = new LinkedHashMap<String, Map<String, String>>();

    private Object lock;

    private int frameDelay = ConstParm.frameDelay;

    private long frameBus = 0;

    public Map<String, Map<String, String>> getLineMap() {
        return lineMap;
    }

    public void setLineMap(Map<String, Map<String, String>> lineMap) {
        this.lineMap = lineMap;
    }

    public int getFrameDelay() {
        return frameDelay;
    }

    public long getFrameBus() {
        return frameBus;
    }

    private ClientSocketManager() {
        super();
        lock = new Object();
        initLogin();
    }

    public static ClientSocketManager getManager() {
        return sockMgr;
    }

    public void openLogin() {
        checkCode(loginMsg);
        sendMsg(loginMsg);
        //Log.e("ClietnSocket", loginMsg.toString());
    }

    private static String byte2hex(byte[] buffer) {
        String h = "";

        for (int i = 0; i < buffer.length; i++) {
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            h = h + " " + temp;
        }

        return h;

    }

    @Override
    protected void parseMsg() {
//        String ss = byte2hex(workBuf);
//        Log.e("---:",ss);
        if ((workBuf[0] & 0xFF) == 0x80 && (workBuf[6] & 0xFF) == 0x77) {
            char c1 = (char) workBuf[29];
            char c2 = (char) workBuf[30];
            String s = String.format("%c%c", c1, c2);
            int rate = Integer.parseInt(s);
            if (rate != 0) {
                frameDelay = 1000 / rate;
            }
            frameBus = ((workBuf[13] & 0xFF) << 24) | ((workBuf[14] & 0xFF) << 16) | ((workBuf[15] & 0xFF) << 8) | (workBuf[16] & 0xFF);
        }
        if ((workBuf[0] & 0xFF) != 0x80 && (workBuf[6] & 0xFF) != 0x83 && (workBuf[6] & 0xFF) != 0x89) {
            return;
        }

        //线路号
        long lineNum = ((workBuf[8] & 0xFF) << 24) | ((workBuf[8] & 0xFF) << 16) | ((workBuf[10] & 0xFF) << 8) | (workBuf[11] & 0xFF);
        //车号
        long busNum = ((workBuf[13] & 0xFF) << 24) | ((workBuf[14] & 0xFF) << 16) | ((workBuf[15] & 0xFF) << 8) | (workBuf[16] & 0xFF);
        long c3 = workBuf[18];
        String lineCode = String.valueOf(lineNum);
        String busCode = String.valueOf(busNum);
        c3 = (c3 < 0) ? -c3 : c3;
//        if ((workBuf[6] & 0xFF) == 0x83){
        if (c3 > 80) {
            if (lineMap.containsKey(lineCode)) {
                Map<String, String> busMap = lineMap.get(lineCode);
                busMap.put(busCode, "new");
//                Log.e("XXXZZZ",busCode);
            } else {
                Map<String, String> busMap = new LinkedHashMap<String, String>();
                synchronized (lock) {
                    busMap.put(busCode, "new");
                    lineMap.put(lineCode, busMap);
//                    Log.e("XXXZZZ",busCode);
                }
            }
        } else if (c3 < 80) {
            if (lineMap.containsKey(lineCode)) {
                Map<String, String> busMap = lineMap.get(lineCode);
                busMap.put(busCode, "old");
//                Log.e("XXXZZZ",busCode);
            } else {
                Map<String, String> busMap = new LinkedHashMap<String, String>();
                synchronized (lock) {
                    busMap.put(busCode, "old");
                    lineMap.put(lineCode, busMap);
//                    Log.e("XXXZZZ",busCode);
                }
            }
        }
//        }
    }

    private void initLogin() {
        //包头
        loginMsg[0] = (byte) 0xB0;

        //包长度
        loginMsg[1] = 0;
        loginMsg[2] = 28;

        //版本号
        loginMsg[3] = 2;

        //包类型
        loginMsg[6] = (byte) 0xA2;

        loginMsg[7] = 1;
        loginMsg[8] = (byte) ((10001 & 0xFF000000) >>> 24);
        loginMsg[9] = (byte) ((10001 & 0xFF0000) >>> 16);
        loginMsg[10] = (byte) ((10001 & 0xFF00) >>> 8);
        loginMsg[11] = (byte) (10001 & 0xFF);

        loginMsg[12] = 2;
        loginMsg[13] = (byte) ((10001 & 0xFF000000) >>> 24);
        loginMsg[14] = (byte) ((10001 & 0xFF0000) >>> 16);
        loginMsg[15] = (byte) ((10001 & 0xFF00) >>> 8);
        loginMsg[16] = (byte) (10001 & 0xFF);

        loginMsg[17] = (byte) 0x1A;
        loginMsg[18] = 1;

        loginMsg[19] = (byte) 0xF1;
        loginMsg[20] = (byte) 0x31;
        loginMsg[21] = (byte) 0x2C;
        loginMsg[22] = (byte) 0x32;

        loginMsg[27] = (byte) 0xB1;
    }

}
