package com.sdhy.video.client;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class PlayPCM implements Runnable {
    static {
        try {
            //System.loadLibrary("voiceEngine");
            System.loadLibrary("ADPCM");
        } catch (UnsatisfiedLinkError e) {
            //Log.d("PlayPCM", "Couldn't load lib: faad2 --- " + e.getMessage());
        }
    }

//	public native int FaadOpen();
//
//	public native int FaadClose();
//	
//	public native int FaadConfig();

    //	public native int FaadInit(byte[] DataBuf, int DataLen);
//	
//	public native int FaadDec(byte[] DataBuf, int DataLen, byte[] DataOut);
    private adpcmState state = null;

//	private int[] state = new int[4];

//  public native int DecInit(int[] state,short code);
//	public native int DecodeFrame(int[] state, byte[] in, byte[] out);
//	public native int DecRelease();

    private final short code = 0x03;

    private final int headCount = 20;

    public native int AdpcmDecoder(byte[] in, short[] out, int len, adpcmState state);

    private VideoSocketManager sockMgr = VideoSocketManager.getManager();

    private boolean stopFlag;

    private Thread soundThread = null;

    private AudioTrack audioTrack = null;

    //private List<short[]> dataList = new ArrayList<short[]>();

    public PlayPCM() {
        state = new adpcmState();
    }

    public void startSound() {
        stopFlag = false;
        try {
            int minBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 8, AudioTrack.MODE_STREAM);
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("playPcm:startSound", "1111111111111111");
        }
        soundThread = new Thread(this);
        soundThread.start();
    }

    public void stopSound() {
        stopFlag = true;
        if (soundThread != null) {
            soundThread.interrupt();
            soundThread = null;
        }

        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }

    }


    public byte[] ShortArrayToByteArray(short[] sBuf) {
        byte[] buf = new byte[sBuf.length * 2];

        for (int j = 0; j < sBuf.length; j++) {
            for (int i = 0; i < 2; i++) {
                buf[j * 2 + i] = (byte) (sBuf[j] & 0x00ff);
                sBuf[j] >>= 8;
            }

        }
        return buf;
    }

    @Override
    public void run() {
        state.valprev = 0;
        state.index = 0;
        //	int dataLen = 0;
        //  byte[] dataBuf = new byte[1024*10];

        //	int srcLen = 0;
        //	byte[] srcBuf = new byte[1024];
        int destLen = 0;
        short[] destBuf = new short[1024 * 4];
        //short[] audioBuf = new short[1024*2];
        int byteIndex = 0;
        try {
            audioTrack.play();
            String SDPath = Environment.getExternalStorageDirectory().getPath();
            String fPath = SDPath + "/music/audio2.pcm";
            //			String fPath = SDPath + "/music/audio2.pcm";
            File file = new File(fPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            while (!Thread.currentThread().isInterrupted()) {
                if (stopFlag) {
                    break;
                }
                try {
                    int acount = sockMgr.audioList.count();
                    System.out.println("acount=" + acount);
                    if (acount <= 15) {
                        Thread.sleep(500);
                        continue;
                    }
                    PacketObject packObj = sockMgr.audioList.get();
                    if (packObj == null) {
                        continue;
                    }

                    byte[] dataBuf = packObj.packBuff;

                    byteIndex = 0;
                    while (byteIndex < packObj.packSize) {
//                        byteIndex = byteIndex + headCount;//老视频加  新视频不加
                        SharedPreferencesHelper spHelper = new SharedPreferencesHelper(MyApplication.getContextObject(),"login");
                        String tag = spHelper.getData(MyApplication.getContextObject(),"tag","");
                        if (tag.equals("old")){
                            byteIndex = byteIndex + headCount;
                            if (dataBuf[byteIndex] != 0 || dataBuf[byteIndex + 1] != 1) {
                                break;
                            }
                        } else if (tag.equals("new")){
                            if(dataBuf[byteIndex] != 0 || dataBuf[byteIndex+1] !=1) {
							/*msg = "packSize is not audio data�� 0=" + dataBuf[byteIndex] ;
							msg = msg +"; 1=" + dataBuf[byteIndex+1] ;
							Log.e("PlayPCM",msg);*/
                                break;
                            }
                        }
                        short aLen = 0;
                        aLen = dataBuf[byteIndex + 3];
                        aLen <<= 8;
                        aLen |= dataBuf[byteIndex + 2];
                        int audioLen = aLen * 2;

//                        if (audioLen < 0) {
//                            FileOutputStream fis = new FileOutputStream(fPath, true);
//                            fis.write(packObj.packBuff, 0, packObj.packSize);
//                            fis.flush();
//                            fis.close();
//                            break;
//                        }

                        if (byteIndex + 4 + audioLen > packObj.packSize) {
                            break;
                        }
                        if (audioLen < 4) {
                            break;
                        }

                        byte[] head = new byte[4];
                        byte[] srcBuf = new byte[audioLen - 4];
                        System.arraycopy(dataBuf, byteIndex + 4, head, 0, 4);
                        System.arraycopy(dataBuf, byteIndex + 8, srcBuf, 0, audioLen - 4);
                        state.valprev = head[1];
                        state.valprev <<= 8;
                        state.valprev |= head[0];
                        state.index = (char) head[2];
                        destLen = AdpcmDecoder(srcBuf, destBuf, audioLen - 4, state);
                        System.out.println("destLen=" + destLen);

                        if (destLen > 0) {
                            audioTrack.write(destBuf, 0, destLen);
                        }
                        byteIndex = byteIndex + 4 + audioLen;
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("playPcm:Run", "222222222222222222222");
                }

            }
        } catch (Exception e) {
            Log.e("playPcm:Run", "111111111111111111111");
        } finally {
            stopFlag = false;
        }

    }

}


