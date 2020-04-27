package com.sdhy.video.client;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public abstract class SocketManager {
    protected SocketHelper sockHelper = new SocketHelper();

    protected boolean started = true;

    protected byte[] liveMsg = new byte[32];

    private final int max_size = 256;
    protected byte[] recvBuf = new byte[1024 * 2];
    protected byte[] dataBuf = new byte[1024 * max_size];
    protected byte[] newDataBuf = null;
    protected int dataLen = 0;

    protected byte[] workBuf = new byte[1024 * 2];
    protected int workLen = 0;

    protected Thread recvThread = null;

    protected Timer tmrLive = null;
    //private boolean stopFlag = true;

    protected SocketManager() {
        initLive();
    }

    protected abstract void parseMsg();

    public void stop() {
        started = false;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean start(String serverAddr, int serverPort) {
        if (!sockHelper.open(serverAddr, serverPort)) {
            return false;
        }

        started = true;

        recvThread = new Thread() {
            @Override
            public void run() {
                recvMsg();
            }
        };
        recvThread.start();
        System.out.println("start111111111111111");
        tmrLive = new Timer();
        tmrLive.schedule(new TimerTask() {
            @Override
            public void run() {
                sendLive();
            }
        }, 20000, 20000);
        return true;
    }

    protected void initLive() {
        //包头
        liveMsg[0] = (byte) 0xA0;

        //包长度
        liveMsg[1] = 0;
        liveMsg[2] = 32;

        //版本号
        liveMsg[3] = 2;

        //包序号
        liveMsg[4] = 0;
        liveMsg[5] = 0;

        //包类型
        liveMsg[6] = (byte) 0xA3;

        liveMsg[7] = 1;
        liveMsg[8] = 0;
        liveMsg[9] = 0;
        liveMsg[10] = 0;
        liveMsg[11] = 0;

        liveMsg[12] = 2;
        liveMsg[13] = 0;
        liveMsg[14] = 0;
        liveMsg[15] = 0;
        liveMsg[16] = 0;

        liveMsg[17] = 4;
        liveMsg[18] = (byte) ((100001 & 0xFF000000) >>> 24);
        liveMsg[19] = (byte) ((100001 & 0xFF0000) >>> 16);
        liveMsg[20] = (byte) ((100001 & 0xFF00) >>> 8);
        liveMsg[21] = (byte) (100001 & 0xFF);

        liveMsg[22] = (byte) 0xF1;
        liveMsg[23] = 0;
        liveMsg[24] = 0;
        liveMsg[25] = 0;
        liveMsg[26] = 0;

        checkCode(liveMsg);

        liveMsg[31] = (byte) 0xA1;
    }

    protected long checkCode(byte[] dataBuf) {
        long check = 0;
        for (int i = 0; i < dataBuf.length - 5; i++) {
            check += (short) (dataBuf[i] & 0xFF);
        }
        dataBuf[dataBuf.length - 5] = (byte) ((check & 0xFF000000) >>> 24);
        dataBuf[dataBuf.length - 4] = (byte) ((check & 0xFF0000) >>> 16);
        dataBuf[dataBuf.length - 3] = (byte) ((check & 0xFF00) >>> 8);
        dataBuf[dataBuf.length - 2] = (byte) (check & 0xFF);
        return check;
    }

    protected void sendLive() {
        sockHelper.send(liveMsg);
    }

    protected boolean sendMsg(byte[] msg) {
        Log.e("socketManage", "11111111111");
        boolean f = sockHelper.send(msg);
        return f;
    }

    protected void recvMsg() {
        while (started) {
            try {
                int len = sockHelper.recv(recvBuf);
                if (len <= 0) {
                    //stop();
                    continue;
                }
                if (dataLen + len > 1024 * (max_size - 1) - 1) {
                    Log.e("socketMange", "lose sockect data1");
                    dataLen = 0;
                }

                System.arraycopy(recvBuf, 0, dataBuf, dataLen, len);
                dataLen += len;

                if (!started) {
                    break;
                }

                splitMsg();
            } catch (Exception ex) {
                Log.e("SocketManage:recvMsg", "1111111111111111");
                break;
            }
        }
        started = false;
        sockHelper.close();

        if (tmrLive != null) {
            tmrLive.cancel();
            tmrLive = null;
        }

        if (recvThread != null) {
            recvThread.interrupt();
            recvThread = null;
        }
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

    protected void splitMsg() {
//        if((workBuf[6] & 0xFF) == 0x95){
//            try {
//                while (dataLen > 9) {
//                    //数据包头验证
//                    if ((dataBuf[0] & 0xFF) != 0x80 && (dataBuf[0] & 0xFF) != 0xA0) {
//                        System.arraycopy(dataBuf, 1, dataBuf, 0, dataLen - 1);
//                        dataLen -= 1;
//                        continue;
//                    }
//                    //验证数据长度，不得小于最小值9
//                    int packSize = ((dataBuf[1] & 0xFF) << 8) | (dataBuf[2] & 0xFF);
//                    if (packSize < 9) {
//                        Log.e("SocketManage1", "splitMsg:22222222222222");
//                        System.arraycopy(dataBuf, 1, dataBuf, 0, dataLen - 1);
//                        dataLen -= 1;
//                        continue;
//                    }
//
//                    //验证数据长度，不得大于最大值2048
//                    if (packSize > 2048) {
//                        Log.e("SocketManage2", "splitMsg:33333333333333");
//                        System.arraycopy(dataBuf, 1, dataBuf, 0, dataLen - 1);
//                        dataLen -= 1;
//
//                        continue;
//                    }
//
//                    //包数据未接收完毕
//                    if (packSize > dataLen) {
//                        break;
//                    }
//
//                    //包头包尾校验
//                    if (dataBuf[0] + 1 != dataBuf[packSize - 1]) {
//                        Log.e("SocketManage3", "splitMsg:44444444444");
//                        System.arraycopy(dataBuf, 1, dataBuf, 0, dataLen - 1);
//                        dataLen -= 1;
//
//                        continue;
//                    }
//
//
//                    //协议版本号处理
//                    //版本号为1-10之间
//                    if (dataBuf[3] <= 0 || dataBuf[3] > 10) {
//                        System.arraycopy(dataBuf, 1, dataBuf, 0, dataLen - 1);
//                        dataLen -= 1;
//
//                        continue;
//                    }
//
//                    //校验和的判断
//                    //排除版本号为1和3的
//                    if (dataBuf[3] != 1 && dataBuf[3] != 3) {
//                        long check1 = 0;
//                        for (int j = 0; j <= packSize - 6; j++) {
//                            check1 += (short) (dataBuf[j] & 0xFF);
//                        }
//
//                        long check2 = ((dataBuf[packSize - 5] & 0xFF) << 24) | ((dataBuf[packSize - 4] & 0xFF) << 16) | ((dataBuf[packSize - 3] & 0xFF) << 8) | (dataBuf[packSize - 2] & 0xFF);
//                        if (check1 != check2) {
//                            System.arraycopy(dataBuf, packSize, dataBuf, 0, dataLen - packSize);
//
//                            dataLen -= packSize;
//                            continue;
//                        }
//                    }
//
//                    System.arraycopy(dataBuf, 0, workBuf, 0, packSize);
//                    workLen = packSize;
//                    if (packSize > dataLen) {
//                        break;
//                    }
//                    parseMsg();
//                    System.arraycopy(dataBuf, packSize, dataBuf, 0, dataLen - packSize);
//                    dataLen -= packSize;
//                }
//            } catch (Exception e) {
//                Log.e("SocketManage:splitMsg", e.toString());
//                //111.225.238.203
//            }
//        }else {
//        String s = byte2hex(dataBuf);
//        Log.e("ZZZ",s);
            try {
                while (dataLen > 9) {
                    //数据包头验证
                    if ((dataBuf[0] & 0xFF) != 0x80 && (dataBuf[0] & 0xFF) != 0xA0) {
                        System.arraycopy(dataBuf, 1, dataBuf, 0, dataLen - 1);
                        dataLen -= 1;
                        continue;
                    }
                    //验证数据长度，不得小于最小值9
                    int packSize = ((dataBuf[1] & 0xFF) << 8) | (dataBuf[2] & 0xFF);
                    if (packSize < 9) {
                        Log.e("SocketManage1", "splitMsg:22222222222222");
                        System.arraycopy(dataBuf, 1, dataBuf, 0, dataLen - 1);
                        dataLen -= 1;
                        continue;
                    }

                    //验证数据长度，不得大于最大值2048
                    if (packSize > 2048) {
                        Log.e("SocketManage2", "splitMsg:33333333333333");
                        System.arraycopy(dataBuf, 1, dataBuf, 0, dataLen - 1);
                        dataLen -= 1;
                        continue;
                    }

                    //包数据未接收完毕
                    if (packSize > dataLen) {
                        break;
                    }

                    //包头包尾校验
                    if (dataBuf[0] + 1 != dataBuf[packSize - 1]) {
                        Log.e("SocketManage3", "splitMsg:44444444444");
                        System.arraycopy(dataBuf, 1, dataBuf, 0, dataLen - 1);
                        dataLen -= 1;
                        continue;
                    }


                    //协议版本号处理
                    //版本号为1-10之间
                    if (dataBuf[3] <= 0 || dataBuf[3] > 10) {
                        Log.e("SocketManage4", "splitMsg:55555555555");
                        System.arraycopy(dataBuf, 1, dataBuf, 0, dataLen - 1);
                        dataLen -= 1;
                        continue;
                    }

                    //校验和的判断
                    //排除版本号为1和3的
                    if (dataBuf[3] != 1 && dataBuf[3] != 3) {
                        long check1 = 0;
                        for (int j = 0; j <= packSize - 6; j++) {
                            check1 += (short) (dataBuf[j] & 0xFF);
                        }

                        long check2 = ((dataBuf[packSize - 5] & 0xFF) << 24) | ((dataBuf[packSize - 4] & 0xFF) << 16) | ((dataBuf[packSize - 3] & 0xFF) << 8) | (dataBuf[packSize - 2] & 0xFF);
                        if (check1 != check2) {
                            Log.e("SocketManage5", "splitMsg:666666666666");
                            System.arraycopy(dataBuf, packSize, dataBuf, 0, dataLen - packSize);
                            dataLen -= packSize;
                            continue;
                        }
                    }

                    System.arraycopy(dataBuf, 0, workBuf, 0, packSize);
                    workLen = packSize;
                    if (packSize > dataLen) {
                        break;
                    }
                    parseMsg();
                    System.arraycopy(dataBuf, packSize, dataBuf, 0, dataLen - packSize);
                    dataLen -= packSize;
                }
            } catch (Exception e) {
                Log.e("SocketManage:splitMsg", "11111111111111111111");
            }
    }


}
