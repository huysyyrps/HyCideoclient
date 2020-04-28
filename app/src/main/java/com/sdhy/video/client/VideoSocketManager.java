package com.sdhy.video.client;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class VideoSocketManager extends SocketManager {

    private static VideoSocketManager sockMgr = new VideoSocketManager();

    private byte[] actionMsg;
    ///////////////////////////////////////////////
    //序列号
    protected int seqNum = 1;
    //车号
    protected long busNum = 0;
    //	protected String Ip=null;
//	protected String port=null;
    protected long Ip1 = 0;
    protected long Ip2 = 0;
    protected long Ip3 = 0;
    protected long Ip4 = 0;
    protected long port_ = 0;
    protected String busType = "";
    protected long total;
    protected long size = 0;
    //通道
//	protected byte channelNum = 0;

    private final int chlNum = 8;
    public PacketList[] videoList = new PacketList[chlNum];

    private int[] portArray = new int[chlNum];
    public PacketList audioList = new PacketList();
    long num = 1;
    long packSize = 0;
    long allPackSize = 0;
    private String TAG = "VideoSocketManager";
    PacketObject packObj = new PacketObject();
    SharedPreferencesHelper spHelper;

    private VideoSocketManager() {
        super();
//        spHelper = new SharedPreferencesHelper(MyApplication.getContextObject(), "login");
//        String tag = spHelper.getData(MyApplication.getContextObject(), "check", "");
//        if (tag.equals("yes")) {
//            actionMsg = new byte[54];
//        } else {
//            actionMsg = new byte[51];
//        }
//        initAction();
        for (int i = 0; i < chlNum; i++) {
            videoList[i] = new PacketList();
        }
    }

    public static VideoSocketManager getManager() {
        return sockMgr;
    }

    @Override
    public void stop() {
        super.stop();
        num = 1;
        packSize = 0;
        allPackSize = 0;
        packObj = new PacketObject();
        for (int i = 0; i < chlNum; i++) {
            videoList[i].clear();
            packObj = new PacketObject();
        }
        audioList.clear();
    }

    @Override
    protected void parseMsg() {
//        String s = byte2hex(workBuf);
//        Log.e("---:", s);
        if (busType.equals("new")) {
            spHelper = new SharedPreferencesHelper(MyApplication.getContextObject(),"login")
                    .saveData(MyApplication.getContextObject(),"tag","new");
            if ((workBuf[0] & 0xFF) != 0x80 && (workBuf[6] & 0xFF) != 0x95) {
                return;
            }
//            if ((workBuf[6] & 0xFF) != 0x95) {
//                return;
//            }
            //状态操作码
            byte stateCode = workBuf[28];
            if (stateCode != 2 && stateCode != 3 && stateCode != 4) {
                return;
            }
            //数据传输类型
            //实时视频0x01 指定时间段的视频0x02
            //实时音频0x03 指定时间段的音频0x04
            byte dataType = workBuf[32];
            if (dataType != 1 && dataType != 2 && dataType != 6) {
                return;
            }
            byte dataType1 = workBuf[41];
            //摄像头编号
            String chtag = spHelper.getData(MyApplication.getContextObject(), "chtag", "");
            byte videoChannel;
            videoChannel = (byte) ((workBuf[39] & 0xFF) << 8 | workBuf[40] & 0xFF);
            //摄像头编号转通道
            for (int i = 0; i < 8; i++) {
                int let = (videoChannel >> i) & 1;
                if (let == 1) {
                    videoChannel = (byte) i;
                }
            }

            if (videoChannel < 0 || videoChannel > 7) {
                return;
            }


            //车号
            long busCode = ((workBuf[13] & 0xFF) << 24) | ((workBuf[14] & 0xFF) << 16) | ((workBuf[15] & 0xFF) << 8) | (workBuf[16] & 0xFF);
            if (busCode != busNum) {
                return;
            }
            //数据包时间
            long recvTime = ((workBuf[23] & 0xFF) << 24) | ((workBuf[24] & 0xFF) << 16) | ((workBuf[25] & 0xFF) << 8) | (workBuf[26] & 0xFF);

            //小包序号
//        long seqNum = ((workBuf[33] & 0xFF) << 24) | ((workBuf[34] & 0xFF) << 16) | ((workBuf[35] & 0xFF) << 8) | (workBuf[36] & 0xFF);

            if (dataType1 != 2) {
                long seqNo = ((workBuf[33] & 0xFF) << 8) | (workBuf[34] & 0xFF);
                long seqNum = ((workBuf[37] & 0xFF) << 8) | (workBuf[38] & 0xFF);
                long allNum = ((workBuf[35] & 0xFF) << 8) | (workBuf[36] & 0xFF);
                packObj.seqNum = seqNum;
                packObj.busCode = busCode;
                packObj.channel = videoChannel;
                packObj.packNo  = seqNo;
                packSize = workLen - 56;

                if (packObj.packBuff == null) {
                    allPackSize += packSize;
                    packObj.packBuff = new byte[(int) packSize];
                    System.arraycopy(workBuf, 51, packObj.packBuff, 56, workLen);
                    num += 1;
                } else if (num < allNum) {
                    allPackSize += packSize;
                    byte[] oldPackBuff = packObj.packBuff;
                    packObj.packBuff = new byte[packObj.packBuff.length + workLen - 56];
                    System.arraycopy(oldPackBuff, 0, packObj.packBuff, 0, oldPackBuff.length);
                    System.arraycopy(workBuf, 51, packObj.packBuff, packObj.packBuff.length - (workLen - 56), workLen - 56);
                    num += 1;
                } else if (num == allNum) {
                    Log.e("seqNum", seqNum + "");
                    allPackSize += packSize;
                    byte[] oldPackBuff = packObj.packBuff;
                    packObj.packBuff = new byte[packObj.packBuff.length + workLen - 56];
                    System.arraycopy(oldPackBuff, 0, packObj.packBuff, 0, oldPackBuff.length);
                    System.arraycopy(workBuf, 51, packObj.packBuff, packObj.packBuff.length - (workLen - 56), workLen - 56);
                    packObj.packSize = (int) allPackSize;
                    videoList[portArray[videoChannel]].push(packObj);
                    num = 1;
                    allPackSize = 0;
                    packObj = new PacketObject();
                } else {
                    num = 1;
                    allPackSize = 0;
                    packObj = new PacketObject();
                }
            }
            if (dataType1 == 2) {
                long seqNum = ((workBuf[37] & 0xFF) << 8) | (workBuf[38] & 0xFF);
                PacketObject packObj = new PacketObject();
                packObj.seqNum = seqNum;
//                packObj.recvTime = recvTime;//
                packObj.busCode = busCode;
                packObj.channel = videoChannel;
                packObj.packSize = workLen - 56;
                packObj.packBuff = new byte[packObj.packSize];
                System.arraycopy(workBuf, 51, packObj.packBuff, 0, packObj.packSize);
                audioList.push(packObj);
            }
        } else {
            spHelper = new SharedPreferencesHelper(MyApplication.getContextObject(),"login")
                    .saveData(MyApplication.getContextObject(),"tag","old");
            if ((workBuf[0] & 0xFF) != 0x80 && (workBuf[6] & 0xFF) != 0x95) {
                return;
            }
            byte stateCode = workBuf[28];
            if (stateCode != 2 && stateCode != 3 && stateCode != 4) {
                return;
            }
            byte dataType = workBuf[32];
            if (dataType != 1 && dataType != 2 && dataType != 3 && dataType != 4) {
                return;
            }
            byte videoChannel = workBuf[31];

            if (videoChannel < 0 || videoChannel > 7) {
                return;
            }

            long busCode = ((workBuf[13] & 0xFF) << 24) | ((workBuf[14] & 0xFF) << 16) | ((workBuf[15] & 0xFF) << 8) | (workBuf[16] & 0xFF);
            if (busCode != busNum) {
                return;
            }
            // Log.e("VideoSocketManger","parseMsg + 999999999999999");
            //????????
            long recvTime = ((workBuf[23] & 0xFF) << 24) | ((workBuf[24] & 0xFF) << 16) | ((workBuf[25] & 0xFF) << 8) | (workBuf[26] & 0xFF);

            //С?????
            long seqNum = ((workBuf[33] & 0xFF) << 24) | ((workBuf[34] & 0xFF) << 16) | ((workBuf[35] & 0xFF) << 8) | (workBuf[36] & 0xFF);

            PacketObject packObj = new PacketObject();

            packObj.seqNum = seqNum;
            packObj.recvTime = recvTime;
            packObj.busCode = busCode;
            packObj.channel = videoChannel;

            //??????????41???
            //??β??У???5???
            packObj.packSize = workLen - 46;
            packObj.packBuff = new byte[packObj.packSize];

            if (packObj.packSize < 0) {
                Log.e("videoSocketManager", "packObj.packSize<0");
            }

            System.arraycopy(workBuf, 41, packObj.packBuff, 0, packObj.packSize);
            if (dataType == 1 || dataType == 2) {
                videoList[portArray[videoChannel]].push(packObj);
                //Log.e("VideoSocketManger","videoChannel +" + portArray[videoChannel]);
            }

            if (dataType == 3 || dataType == 4) {
//	    	if(videoChannel == 0)
//	    	{
                //Log.e("VideoSocketManger","parseMsg + 44444444444");
                System.out.println("packObj=" + packObj.packSize);
                audioList.push(packObj);
//	    	}
            }
        }
    }


    public boolean openChannel(long lineCode, long busCode, byte channel, String beginTime,
                               String endTime, int[] chlSel, String Ip, String port, String busType1) {
        boolean f = false;
        busType = busType1;
        if (busType1.equals("new")) {
            actionMsg = new byte[54];
        } else {
            actionMsg = new byte[51];
        }
        initAction();
        if (!port.equals("null")){
            port_ = Long.parseLong(port);
        }
        if (!Ip.equals("null")){
            String[] addr = Ip.split("\\.");
            Ip1 = Long.parseLong(addr[0]);
            Ip2 = Long.parseLong(addr[1]);
            Ip3 = Long.parseLong(addr[2]);
            Ip4 = Long.parseLong(addr[3]);
        }

        int n = 0;

        int chl = -1;
        for (int i = 0; i < 8; i++) {
            if (chlSel[i] == 1) {
                if (chl < 0) {
                    chl = i + 1;
                }
                portArray[i] = n;
                n++;
            }
        }
        busNum = busCode;

        fillAction(lineCode, busCode);
        //开始结束时间
        controlChannel(lineCode, busCode, channel, true, beginTime, endTime);
        total = checkCode(actionMsg);
//        initTotal();
        if (!beginTime.equals("")) {
            if (busType1.equals("new")) {
                sendMsg(actionMsg);
//                String s = byte2hex(actionMsg);
//                Log.e("XXX",s);
                byte[] actionMsg1 = sendOldMsg(actionMsg);
                checkCode(actionMsg1);
//                String s1 = byte2hex(actionMsg1);
//                Log.e("YYY",s1);
                sendMsg(actionMsg1);

            } else {
                sendMsg(actionMsg);
                controlAudio(lineCode, busCode, (byte) chl, true, beginTime, endTime);
                total = checkCode(actionMsg);
                initTotal();
//                String s = byte2hex(actionMsg);
//                Log.e("ZZZ",s);
                sendMsg(actionMsg);
            }
        } else {
            if (busType1.equals("new")) {
                sendMsg(actionMsg);
//                String s = byte2hex(actionMsg);
//                Log.e("XXX111",s);
            } else {
//                initTotal();
//                f = sendMsg(actionMsg);
//                return f;
                sendMsg(actionMsg);
                controlAudio(lineCode, busCode, (byte) chl, true, beginTime, endTime);
                total = checkCode(actionMsg);
                initTotal();
                sendMsg(actionMsg);
            }
        }
        return f;
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


    protected byte[] sendOldMsg(byte[] msg) {
        actionMsg[28] = 0x11;
        actionMsg[5] = (byte) (seqNum);
        //回放数据的结束时间
        msg[36] = 0;
        msg[37] = 0;
        msg[38] = 0;
        msg[39] = 0;
        return msg;
    }

    public void closeChannel(long lineCode, long busCode, byte channel, String beginTime, String endTime, int[] chlSel) {

        int chl = -1;
        for (int i = 0; i < 8; i++) {
            if (chlSel[i] == 1) {
                if (chl < 0) {
                    chl = i + 1;
                }
            }
        }
        busNum = 0;

        fillAction(lineCode, busCode);

        controlChannel(lineCode, busCode, channel, false, beginTime, endTime);
        checkCode(actionMsg);
        sendMsg(actionMsg);

        if (ConstParm.videoDeviceType == 1) {
            controlAudio(lineCode, busCode, (byte) chl, false, beginTime, endTime);
            checkCode(actionMsg);
            sendMsg(actionMsg);
        }
    }

    private void controlAudio(long lineCode, long busCode, byte chl, boolean openFlag, String beginTime, String endTime) {
        byte[] tempBuf = new byte[19];
        //操作类型
        tempBuf[0] = 0x02;
        //状态Z
        if (openFlag) {
            tempBuf[1] = 0x03;
        } else {
            tempBuf[1] = 0x04;
        }
        //摄像头编号
        tempBuf[2] = 0;
        //打开
        //tempBuf[3] = (byte) (1 << (channel-1));
        tempBuf[3] = chl;

        //默认为实时数据
        tempBuf[4] = 1;

        //回放数据的开始时间
        tempBuf[5] = 0;
        tempBuf[6] = 0;
        tempBuf[7] = 0;
        tempBuf[8] = 0;

        //回放数据的结束时间
        tempBuf[9] = 0;
        tempBuf[10] = 0;
        tempBuf[11] = 0;
        tempBuf[12] = 0;

        //实时
        if (!beginTime.equals("") && !endTime.equals("")) {
            long begin = 0;
            long end = 0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            try {
                //历史回放
                begin = sdf.parse(beginTime).getTime() / 1000 + 3600 * 8;
                end = sdf.parse(endTime).getTime() / 1000 + 3600 * 8;
                tempBuf[4] = 2;
            } catch (Exception ex) {
                //调整为实时数据
                begin = 0;
                end = 0;
                tempBuf[4] = 1;
            }
            //回放数据的开始时间
            tempBuf[5] = (byte) ((begin & 0xFF000000) >>> 24);
            tempBuf[6] = (byte) ((begin & 0xFF0000) >>> 16);
            tempBuf[7] = (byte) ((begin & 0xFF00) >>> 8);
            tempBuf[8] = (byte) (begin & 0xFF);
            //回放数据的结束时间
            tempBuf[9] = (byte) ((end & 0xFF000000) >>> 24);
            tempBuf[10] = (byte) ((end & 0xFF0000) >>> 16);
            tempBuf[11] = (byte) ((end & 0xFF00) >>> 8);
            tempBuf[12] = (byte) (end & 0xFF);
        }
        //视频服务器的IP地址和端口
        //由视频服务器进行调整
        //所以这里全部填0即可
        tempBuf[13] = (byte) ((Ip4 & 0xFF));
        tempBuf[14] = (byte) ((Ip3 & 0xFF));
        tempBuf[15] = (byte) ((Ip2 & 0xFF));
        tempBuf[16] = (byte) ((Ip1 & 0xFF));

        tempBuf[17] = (byte) (port_ & 0xFF);
        tempBuf[18] = (byte) ((port_ & 0xFF00) >>> 8);
        System.arraycopy(tempBuf, 0, actionMsg, 27, 19);
    }

    private void initAction() {
        //包头
        actionMsg[0] = (byte) 0xA0;
        //包长度
        actionMsg[1] = 0;
        if (busType.equals("new")) {
            actionMsg[2] = 54;
        } else {
            actionMsg[2] = 51;
        }

        //版本号
        actionMsg[3] = 2;

        //包类型
        actionMsg[6] = (byte) 0xC2;

        actionMsg[7] = 1;

        actionMsg[12] = 2;

        actionMsg[17] = 4;
        long now = System.currentTimeMillis();
        actionMsg[18] = (byte) ((now & 0xFF000000) >>> 24);
        actionMsg[19] = (byte) ((now & 0xFF0000) >>> 16);
        actionMsg[20] = (byte) ((now & 0xFF00) >>> 8);
        actionMsg[21] = (byte) (now & 0xFF);
        actionMsg[22] = 6;

        if (busType.equals("new")) {
            actionMsg[53] = (byte) 0xA1;
        } else {
            actionMsg[50] = (byte) 0xA1;
        }
    }

    private void fillAction(long lineCode, long busCode) {
        //包序号
        actionMsg[4] = 0;
        actionMsg[5] = (byte) (seqNum);


        seqNum++;

        //线路号
        actionMsg[8] = (byte) ((lineCode & 0xFF000000) >>> 24);
        actionMsg[9] = (byte) ((lineCode & 0xFF0000) >>> 16);
        actionMsg[10] = (byte) ((lineCode & 0xFF00) >>> 8);
        actionMsg[11] = (byte) (lineCode & 0xFF);

        //车号
        actionMsg[13] = (byte) ((busCode & 0xFF000000) >>> 24);
        actionMsg[14] = (byte) ((busCode & 0xFF0000) >>> 16);
        actionMsg[15] = (byte) ((busCode & 0xFF00) >>> 8);
        actionMsg[16] = (byte) (busCode & 0xFF);


//        long begin = System.currentTimeMillis();
//        //回放数据的开始时间
//        actionMsg[23] = (byte) ((begin & 0xFF000000) >>> 24);
//        actionMsg[24] = (byte) ((begin & 0xFF0000) >>> 16);
//        actionMsg[25] = (byte) ((begin & 0xFF00) >>> 8);
//        actionMsg[26] = (byte) (begin & 0xFF);
        //以秒为单位 //令需减8个时区
//        long now = System.currentTimeMillis() / 1000 - 3600 * 8;
        long now = System.currentTimeMillis();
        //unix时间
        actionMsg[23] = (byte) ((now & 0xFF000000) >>> 24);
        actionMsg[24] = (byte) ((now & 0xFF0000) >>> 16);
        actionMsg[25] = (byte) ((now & 0xFF00) >>> 8);
        actionMsg[26] = (byte) (now & 0xFF);
    }

    private void controlChannel(long lineCode, long busCode, byte channel, boolean openFlag, String beginTime, String endTime) {
        if (busType.equals("new")) {
            byte[] tempBuf = new byte[22];

            //操作类型
            tempBuf[0] = 0x10;

            //状态
            tempBuf[1] = 0x01;

            //摄像头编号
            tempBuf[2] = 0;
            if (openFlag) {
                //打开
                //tempBuf[3] = (byte) (1 << (channel-1));
                tempBuf[3] = channel;
            } else {
                //关闭
                tempBuf[3] = 0;
            }

            //默认为实时数据
            tempBuf[4] = 1;

            //实时开始时间
            tempBuf[5] = 0;
            tempBuf[6] = 0;
            tempBuf[7] = 0;
            tempBuf[8] = 0;

            //实时数据的结束时间
            tempBuf[9] = 0;
            tempBuf[10] = 0;
            tempBuf[11] = 0;
            tempBuf[12] = 0;

            //实时
            if (!beginTime.equals("") && !endTime.equals("")) {
                long begin = 0;
                long end = 0;
                if (openFlag) {
                    //打开
                    //tempBuf[3] = (byte) (1 << (channel-1));
                    tempBuf[3] = 1;
                } else {
                    //关闭
                    tempBuf[3] = 1;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

                try {
                    //历史回放
                    begin = sdf.parse(beginTime + ":00").getTime() / 1000 + 3600 * 8;
                    end = sdf.parse(endTime + ":00").getTime() / 1000 + 3600 * 8;
//                    begin = sdf.parse(beginTime).getTime() / 1000;
//                    end = sdf.parse(endTime).getTime() / 1000;
                    tempBuf[4] = 2;
                } catch (Exception ex) {
                    //调整为实时数据
                    begin = 0;
                    end = 0;
                    tempBuf[4] = 1;
                }

                //回放数据的开始时间
                tempBuf[5] = (byte) ((begin & 0xFF000000) >>> 24);
                tempBuf[6] = (byte) ((begin & 0xFF0000) >>> 16);
                tempBuf[7] = (byte) ((begin & 0xFF00) >>> 8);
                tempBuf[8] = (byte) (begin & 0xFF);

                //回放数据的结束时间
                tempBuf[9] = (byte) ((end & 0xFF000000) >>> 24);
                tempBuf[10] = (byte) ((end & 0xFF0000) >>> 16);
                tempBuf[11] = (byte) ((end & 0xFF00) >>> 8);
                tempBuf[12] = (byte) (end & 0xFF);
            }

            //视频服务器的IP地址和端口
            //由视频服务器进行调整
            //所以这里全部填0即可
            tempBuf[13] = (byte) ((Ip4 & 0xFF));
            tempBuf[14] = (byte) ((Ip3 & 0xFF));
            tempBuf[15] = (byte) ((Ip2 & 0xFF));
            tempBuf[16] = (byte) ((Ip1 & 0xFF));

            tempBuf[17] = (byte) (port_ & 0xFF);
            tempBuf[18] = (byte) ((port_ & 0xFF00) >>> 8);
            tempBuf[19] = (byte) (0x00);
            tempBuf[20] = (channel);
            tempBuf[21] = (byte) (0x01);
            System.arraycopy(tempBuf, 0, actionMsg, 27, 22);
        } else {
            byte[] tempBuf = new byte[19];
            tempBuf[0] = 0x01;
            tempBuf[1] = 0x01;
            tempBuf[2] = 0;
            if (openFlag) {
                tempBuf[3] = channel;
            } else {
                tempBuf[3] = 0;
            }
            tempBuf[4] = 1;
            tempBuf[5] = 0;
            tempBuf[6] = 0;
            tempBuf[7] = 0;
            tempBuf[8] = 0;
            tempBuf[9] = 0;
            tempBuf[10] = 0;
            tempBuf[11] = 0;
            tempBuf[12] = 0;
            if (!beginTime.equals("") && !endTime.equals("")) {
                long begin = 0;
                long end = 0;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

                try {
                    //??????
                    begin = sdf.parse(beginTime + ":00").getTime() / 1000 + 3600 * 8;
                    end = sdf.parse(endTime + ":00").getTime() / 1000 + 3600 * 8;
                    tempBuf[4] = 2;
                } catch (Exception ex) {
                    //???????????
                    begin = 0;
                    end = 0;
                    tempBuf[4] = 1;
                }
                tempBuf[5] = (byte) ((begin & 0xFF000000) >>> 24);
                tempBuf[6] = (byte) ((begin & 0xFF0000) >>> 16);
                tempBuf[7] = (byte) ((begin & 0xFF00) >>> 8);
                tempBuf[8] = (byte) (begin & 0xFF);
                tempBuf[9] = (byte) ((end & 0xFF000000) >>> 24);
                tempBuf[10] = (byte) ((end & 0xFF0000) >>> 16);
                tempBuf[11] = (byte) ((end & 0xFF00) >>> 8);
                tempBuf[12] = (byte) (end & 0xFF);
            }
            tempBuf[13] = (byte) ((Ip4 & 0xFF));
            tempBuf[14] = (byte) ((Ip3 & 0xFF));
            tempBuf[15] = (byte) ((Ip2 & 0xFF));
            tempBuf[16] = (byte) ((Ip1 & 0xFF));
            tempBuf[17] = (byte) (port_ & 0xFF);
            tempBuf[18] = (byte) ((port_ & 0xFF00) >>> 8);
            System.arraycopy(tempBuf, 0, actionMsg, 27, 19);
        }
    }

    private void initTotal() {
        if (busType.equals("new")) {
            actionMsg[49] = (byte) ((total & 0xFF000000) >>> 24);
            actionMsg[50] = (byte) ((total & 0xFF0000) >>> 16);
            actionMsg[51] = (byte) ((total & 0xFF00) >>> 8);
            actionMsg[52] = (byte) ((total & 0xFF));
        } else {
            actionMsg[46] = (byte) ((total & 0xFF000000) >>> 24);
            actionMsg[47] = (byte) ((total & 0xFF0000) >>> 16);
            actionMsg[48] = (byte) ((total & 0xFF00) >>> 8);
            actionMsg[49] = (byte) ((total & 0xFF));
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

}