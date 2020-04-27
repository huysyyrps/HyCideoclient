package com.sdhy.video.client;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import java.util.concurrent.Executor;
//import android.util.concurrent.locks.reentrantLock
//import android.app.Activity;
//import android.text.method.MovementMethod;
//import android.view.KeyEvent;

public class PlayView extends SurfaceView implements Runnable, Callback {
    static {
        try {
            System.loadLibrary("h264");
        } catch (UnsatisfiedLinkError e) {
            Log.e("playView:loadLibrary", e.getMessage());
        }
    }

    private ClientSocketManager clientSockMgr = ClientSocketManager.getManager();
    private static Object lock = new Object();

    private int width = 352;
    private int height = 288;
    private final int maxListNum = 500;
    //private Lock lock = new ReentrantLock();

    private long count = 0;

    public native int InitDecoder(int width, int height);

    public native int UninitDecoder(int port);

    public native int DecoderNal(int port, byte[] in, int insize, byte[] out, int type);

    private VideoSocketManager sockMgr = VideoSocketManager.getManager();

    //public PacketList videoList = null;
    private PixelList[] pixelList;

    private boolean stopFlag = false;

    private SurfaceHolder surfaceHolder = null;

    private Thread drawThread = null;

    public static int chlNum;
    public int[] chlSel;
    private byte[][] dataPixel;
    private int pixelLen[];

    private ByteBuffer[] dataBuff;

    private Bitmap[] drawBmp;
    private long[] lastDraw;

    private int[] port;
    private int[] mTrans;
    private int[] destUsed;
    private byte[][] destBuf;
    public int[] nFindPPS;
    public int[] nFirst;
    private int[] nalLen;
    public int[] srcUsed;
    private int[] destStart;

    private byte[][] srcBuf; //= new byte[chlNum][1024*2];
    private boolean canPlay = false;

    private int delay = 100;// UserLoginActivity.videoFrameDelay;
    //public static int delay2 = 0;
    private int type = 1;
    private boolean bRotate = false;

    private Context mcontext = null;

    private int playFlag = -1;//是否全屏，-1为多路显示，>0表示某一路显示
    private int playFlag2 = -1;

    private int screenWidth = 0;
    private int screenHeight = 0;

    private Rect[] dstRect1;

    //private final Rect  srcRect = new Rect(0,0,width,height);

    private long time = 0;
    public Timer mTimer = null;// 定时器
    private String busType = "";
    private long busNum = 0;
    private PacketObject lastObj[] = new PacketObject[8];

    public PlayView(Context context, int[] array, int num, long busCode,String busType1) {
        super(context);
        setFocusable(true);

        mTimer = new Timer();
        dstRect1 = new Rect[8];
        mcontext = context;

        delay = ConstParm.frameDelay;
        if (ConstParm.resolutionType == 0) {
            width = 352;
            height = 288;
        } else if (ConstParm.resolutionType == 1) {
            width = 352 * 2;
            height = 288 * 2;
        }

        chlNum = num;
        chlSel = array;
        busNum = busCode;
        busType = busType1;
        initView();

        pixelList = new PixelList[chlNum];
        int i;
        for (i = 0; i < chlNum; i++) {
            port[i] = -1;
            pixelList[i] = new PixelList();
        }

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this); // 为SurfaceView添加状态监听


    }

    private void initView() {
        //chlNum =  BusSelectActivity.channelNum;
        //srcBuf = new byte[chlNum][1024*16];
        pixelLen = new int[chlNum];
        dataPixel = new byte[chlNum][width * height * 2];
        dataBuff = new ByteBuffer[chlNum];
        drawBmp = new Bitmap[chlNum];

        lastDraw = new long[chlNum];
        port = new int[chlNum];
        mTrans = new int[chlNum];
        destUsed = new int[chlNum];
        //destBuf = new byte[chlNum][128*1024];
        nFindPPS = new int[chlNum];
        nFirst = new int[chlNum];
        nalLen = new int[chlNum];
        destStart = new int[chlNum];
        srcUsed = new int[chlNum];
        srcBuf = new byte[chlNum][1024 * 4];


    }


    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    // Handler处于UI线程中,更新界面的操作在此处执行
                    if (stopFlag == false) {
                        for (int i = 0; i < chlNum; i++) {
                            doDraw(i);
                        }
                    }
                    break;
//            case 2:
//            	//synchronized (lock) {
//
//            		if (mTimer != null)
//                	{
//    	            	mTimer.cancel();//
//    	                timerTask();
//                	}
//				//}
//            	break;
                default:
                    break;
            }
            //super.handleMessage(msg);
        }
    };

    TimerTask timeTask = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            mHandler.sendMessage(message);
        }
    };

    public void startVideo() {
        stopFlag = false;
        screenWidth = this.getWidth();
        screenHeight = this.getHeight();
        dstRect1 = getDstRect(screenWidth, screenHeight);
        //timerTask(); // 定时执行
        if (busNum == clientSockMgr.getFrameBus()) {
            delay = clientSockMgr.getFrameDelay();
            Log.e("palyview", "clientSockMgr.getFrameDelay()=" + delay);
        }
        mTimer.schedule(timeTask, 0, delay);
        drawThread = new Thread(this);
        drawThread.start();


    }

    public void stopVideo() {

        stopFlag = true;


        if (drawThread != null) {
            drawThread.interrupt();
            drawThread = null;
        }

//		if (mTimer != null)
//    	{
//			mTimer.cancel();//
//	        mTimer=null;
//    	}

        //surfaceDestroyed(surfaceHolder);


    }

    public Rect[] getDstRect(int width, int height) {
        Rect[] dstRect = new Rect[8];
        int cNum = (chlNum % 2 == 0) ? chlNum : chlNum + 1;
        //Rect rect[cNum]

        for (int i = 0; i < cNum; i++) {
            dstRect[i] = GetShowRect(i, cNum, width, height);
//	        String msg = "dstRect("+i+") :[" + dstRect[i].left + "," + dstRect[i].top +", " + dstRect[i].right + ", " + dstRect[i].bottom+ "]";
//			Log.e("PlayView",msg);
        }

        return dstRect;
    }

    //	public mCountext
//	@SuppressLint("NewApi")
    public void doDraw(int port) {
        Canvas canvas = null;
//		String msg = "pixelList.length = " + pixelList[port].count();
//		Log.e("PlayView",msg);
        if (pixelList[port].count() < 2) {
            return;
        }
        if (pixelList[port].count() > 100) {
            String msg = "pixelList.length = " + pixelList[port].count();
            Log.e("PlayView", msg);
            for (int i = 0; i < chlNum; i++) {
                pixelList[i].clear();
            }
            return;
        }

        try {
            if (MainViewActivity.pd != null) {
                MainViewActivity.pd.dismiss();
                MainViewActivity.pd = null;
            }

            //屏幕旋转
            if (screenWidth != this.getWidth()) {
                //bRotate = true;
                canvas = surfaceHolder.lockCanvas();
                canvas.drawRGB(0, 0, 0);//画黑屏
                screenWidth = this.getWidth();
                screenHeight = this.getHeight();
                //lock.lock();
                Rect[] rects = getDstRect(screenWidth, screenHeight);
                dstRect1 = Arrays.copyOf(rects, rects.length);
                //lock.unlock();

                return;
            }
            byte[] pixel = this.pixelList[port].get();
            if (pixel == null) {
                return;
            }
            dataBuff[port] = ByteBuffer.wrap(pixel);
            //二路以上显示
            if (chlNum > 1) {
                //取消全屏显示后清空背景
                if (playFlag2 != playFlag) {
                    canvas = surfaceHolder.lockCanvas();
                    //Log.e("doDraw1",canvas.toString());
                    canvas.drawRGB(0, 0, 0);//画黑屏
                    playFlag2 = playFlag;
                    return;
                }
                //多路显示
                if (playFlag < 0) {
                    drawBmp[port].copyPixelsFromBuffer(dataBuff[port]);
                    dataBuff[port].position(0);
                    //lock.lock();
                    Rect rc = new Rect(dstRect1[port]);
                    //lock.unlock();
                    canvas = surfaceHolder.lockCanvas(rc);
                    //Log.e("doDraw1",drawBmp[port].toString());
                    canvas.drawBitmap(drawBmp[port], null, rc,
                            null);
//						String msg = "Draw" + port +":[" + dstRect1[port].left + "," + dstRect1[port].top +", " + dstRect1[port].right + ", " + dstRect1[port].bottom+ "]";
//						Log.d("PlayView",msg);
                } else if (port == playFlag) {
                    Rect dstRect = new Rect(0, 0, this.getWidth(),
                            this.getHeight());
                    drawBmp[port].copyPixelsFromBuffer(dataBuff[port]);
                    dataBuff[port].position(0);
                    canvas = surfaceHolder.lockCanvas();
                    //Log.e("doDraw1",drawBmp[port].toString());
                    canvas.drawBitmap(drawBmp[port], null, dstRect, null);
                }
            } else {
                //只显示某一路
                Rect dstRect = new Rect(0, 0, this.getWidth(), this.getHeight());
                drawBmp[port].copyPixelsFromBuffer(dataBuff[port]);
                dataBuff[port].position(0);
                canvas = surfaceHolder.lockCanvas();
                //Log.e("doDraw1",drawBmp[port].toString());
                canvas.drawBitmap(drawBmp[port], null, dstRect, null);
            }
            //surfaceHolder.unlockCanvasAndPost(canvas);
        } catch (Exception ex) {
            Log.e("PlayView:doDraw1", "111111111111111");
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
                canvas = null;
            }
        }
    }

    private Object rc(Rect rect) {
        // TODO Auto-generated method stub
        return null;
    }

    public int decode(int index, PacketObject packObj) {
        if (stopFlag == true) {
            return 0;
        }
        try {
            if (lastObj[index] != null) {
                long a = packObj.seqNum - lastObj[index].seqNum;
                if (a > 1) {
                    String str = String.format("packObj.seqNum-lastObj[%d].seqNum=%d", index, a);
                    Log.e("playView", str);
                }
            }
            lastObj[index] = packObj;
//			srcUsed[index] = packObj.packSize;
//			//srcBuf[index] = packObj.packBuff;
//			System.arraycopy(packObj.packBuff, 0, srcBuf[index], 0, packObj.packSize);
//
//			if (DecoderNal(port[index], srcBuf[index],
//					srcUsed[index], dataPixel[index], 1) > 0)
//			{
            //Log.e("playView","decode start port=");
            if (packObj.packBuff == null) {
                return 0;
            }
//			synchronized(lock)
//			{
            pixelLen[index] = DecoderNal(port[index], packObj.packBuff,
                    packObj.packSize, dataPixel[index], 1);
//			}

            while (pixelLen[index] > 0) {
                this.pixelList[index].push(dataPixel[index].clone());
//				synchronized(lock)
//				{
                pixelLen[index] = DecoderNal(port[index], packObj.packBuff, 0, dataPixel[index], 1);
//				}
            }
        } catch (Exception ex) {
            Log.e("PlayView", "lost video frame!" + index + ":" + port[index]);
        }
        return 0;
    }

    private int GetTouchRectPort(float x, float y) {
        int flag = 0;

        if (chlNum < 2) {
            flag = 0;
        } else if (chlNum < 3) {
            if (y > 0 && y < screenHeight / 2) {
                flag = 0;
            } else if (y < screenHeight && y > screenHeight / 2) {
                flag = 1;
            }
        } else {
            if (x > 0 && x < screenWidth / 2) {// port = 0,2,4,6
                for (int i = 0; i < chlNum; i = i + 2) {
                    if (y > dstRect1[i].top && y < dstRect1[i].bottom) {
                        flag = i;
                        break;
                    }
                }
            } else if (x < screenWidth && x > screenWidth / 2) {// port =
                for (int i = 1; i < chlNum; i = i + 2) {
                    if (y > dstRect1[i].top && y < dstRect1[i].bottom) {
                        flag = i;
                        break;
                    }
                }
            }
        }
        return flag;
    }

    private Thread NewDecodeThread(final int i) {
        Thread thread = null;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                pixelList[i].clear();
                srcUsed[i] = 0;
                while (!stopFlag) {
                    try {
                        if (sockMgr.videoList[i].count() > maxListNum) {
                            sockMgr.videoList[i].clear();
                            nFindPPS[i] = 1;
                            nFirst[i] = 1;
                            continue;
                        }
//                        Log.e("sockMgr.videoList", sockMgr.videoList[i].count() + "");
                        if (sockMgr.videoList[i].count() >20) {
                            decode(i, sockMgr.videoList[i].get());
                        }
                    } catch (Exception ex) {
                        Log.e("PlayView:run", "00000000000000");
                    }
                }
                pixelList[i].clear();
            }

        });
        return thread;

    }

    @Override
    public void run() {
        this.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                long t = System.currentTimeMillis();
                if (t - time < 1000) {
                    time = t;
                    return false;
                }
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    if (playFlag < 0) {
                        float x = arg1.getX();
                        float y = arg1.getY();
                        playFlag = GetTouchRectPort(x, y);
//						String msg = "arg0 +　x = " + x + "; y = " +y +";flag = " +playFlag;
//						Log.d("PlayView",msg);
                    } else {
                        playFlag = -1;
                    }
                }
                return false;
            }
        });
        stopFlag = false;
        playFlag = -1;
        count = 0;

        int i;
        try {
            for (i = 0; i < chlNum; i++) {
                pixelLen[i] = 0;
                drawBmp[i] = Bitmap.createBitmap(width, height, Config.RGB_565);
                destUsed[i] = 0;
                nFindPPS[i] = 1;
                mTrans[i] = 0x0F0F0F0F;
                nFirst[i] = 1;
                port[i] = InitDecoder(width, height);
                lastDraw[i] = System.currentTimeMillis();
                if (port[i] < 0) {
                    return;
                }
            }

        } catch (Exception ex) {
            Log.e("PlayView:run1", "111111111111111111111");
        }

        Thread[] thread = new Thread[chlNum];
        int n = 0;
        i = 0;
        while (n < 8) {
            if (chlSel[n] == 1) {
                thread[i] = NewDecodeThread(i);
                i++;
            }
            n++;
        }
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
//	    	String msg = "run + 11111111111111111111111" ;
//			Log.e("PlayView",msg);
            for (i = 0; i < chlNum; i++) {
                pool.execute(thread[i]);
            }
            while (!Thread.currentThread().isInterrupted()) {

                if (stopFlag) {
                    break;
                }
                if (delay != clientSockMgr.getFrameDelay() && busNum == clientSockMgr.getFrameBus()) {
                    //Log.e("playViewbus",busNum + "" + clientSockMgr.getFrameBus());
//					if (this.mTimer != null)
//			    	{
                    Log.e("playView", "mTimer is reset!");
                    delay = clientSockMgr.getFrameDelay();
                    Field[] fields = timeTask.getClass().getSuperclass().getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getName().endsWith("period")) {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            field.set(timeTask, delay);
                        }
                    }
//			    	}
                }
            }

            stopFlag = true;
//			msg = "run + 5555555555555555555555" ;
//			Log.e("PlayView",msg);
            for (i = 0; i < chlNum; i++) {
                while (thread[i].isAlive()) {
                    Thread.sleep(10);
                }
            }

            //关闭线程池
            pool.shutdown();

//	    	msg = "run + 66666666666666666666666" ;
//			Log.e("PlayView",msg);

        } catch (Exception ex) {
            Log.e("play:run2", "2222222222222222");
        } finally {
            try {
                if (mTimer != null) {
                    mTimer.cancel();//
                    //mTimer=null;
                    Log.d("playView", "mTimer is cancel!");
                }

                for (i = 0; i < chlNum; i++) {
                    if (port[i] >= 0) {
                        UninitDecoder(port[i]);
                        port[i] = -1;
                    }
                }
                //Thread.sleep(100);
            } catch (Exception ex) {
                Log.e("playview:run3", "33333333333333333333333");
            }
        }
    }

    private int MergeBuffer(int inx, byte[] NalBuf, int NalBufUsed, byte[] SockBuf, int SockBufUsed, int SockRemain) {
        int i = 0;
        byte temp;

        for (i = 0; i < SockRemain; i++) {
            temp = SockBuf[i + SockBufUsed];
            NalBuf[i + NalBufUsed] = temp;

            mTrans[inx] <<= 8;
            mTrans[inx] |= temp;

            if (mTrans[inx] == 1) // 找到一个开始字
            {
                i++;
                break;
            }
        }

        return i;
    }

    private Rect GetShowRect(int i, int winNum, int width, int height) {

        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        if (winNum < 2) {
            Rect rc = new Rect(0, 0, width, height);
            return rc;
        } else if (winNum < 3) {
            left = 0;
            right = width;

            int a = height / winNum;
            top = i * a;
            bottom = (i + 1) * a;

            Rect rc = new Rect(left, top, right, bottom);
            return rc;

        } else {
            if (i % 2 == 0) {
                left = 0;
                right = width / 2;
            } else {
                left = width / 2;
                right = width;
            }

            //int a = ;

            top = i / 2 * (height / (winNum / 2));
            bottom = (i / 2 + 1) * (height / (winNum / 2));

            Rect rc = new Rect(left, top, right, bottom);
//			String msg = "left = " + left + "; top = " + top +";right = " + right + ";bottom = " + bottom;
//			Log.d("PlayView",msg);

            return rc;
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        // TODO Auto-generated method stub
        //this.startVideo();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO Auto-generated method stub
        this.stopVideo();

    }


}
