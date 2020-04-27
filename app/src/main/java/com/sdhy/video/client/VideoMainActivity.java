package com.sdhy.video.client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class VideoMainActivity extends Activity {
    //private SocketManager sockMgr = SocketManager.getManager();


    private PlayView videoWin = null;
    //private FourWayViewActivity videoWin4 = null;

    //private GestureDetector gestureDetector = null;

    public static ProgressDialog pd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		videoWin = new PlayView(this);
//		setContentView(videoWin);
//        
//		videoWin.startVideo();
//		
//
//		new Timer().schedule(new TimerTask()
//		{
//			@Override
//			public void run()
//			{
//				if (VideoMainActivity.pd != null)
//				{
//					VideoMainActivity.pd.dismiss();
//					VideoMainActivity.pd = null;
//				}									
//			}		
//		}, 30000);
//        
        //gestureDetector = new GestureDetector(new HyGestureListener());

        //Log.i(this.getClass().getName(), "onCreate");
    }

    @Override
    protected void onDestroy() {
        //Log.i(this.getClass().getName(), "onDestroy");

        videoWin.stopVideo();
        videoWin = null;

        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
        //return gestureDetector.onTouchEvent(event);
    }

    class HyGestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//			sockMgr.closeChannel(BusSelectActivity.lineCode, BusSelectActivity.busCode, BusSelectActivity.channel, "", "");
//			
//			BusSelectActivity.channel = (byte)(BusSelectActivity.channel + 1);
//			if (BusSelectActivity.channel > 4)
//			{
//				BusSelectActivity.channel = 1;
//			}
//						
//
//			sockMgr.openChannel(BusSelectActivity.lineCode, BusSelectActivity.busCode, BusSelectActivity.channel, "", "");

            return true;
        }
    }

}
