////package com.sdhy.video.client;
////
////import android.app.Activity;
////import android.app.AlertDialog;
////import android.app.ProgressDialog;
////import android.content.BroadcastReceiver;
////import android.content.Intent;
////import android.os.Bundle;
////import android.util.Log;
////import android.view.KeyEvent;
////import android.view.WindowManager;
////
////import java.util.ArrayList;
////import java.util.Timer;
////import java.util.TimerTask;
////
////public class MainViewActivity extends Activity {
////    private VideoSocketManager videoSockMgr ;
////
////    private boolean closeFlg = false;
////    private PlayView videoWin = null;
////    private PlayPCM playSound = null;
////    private int[] chlSel;
////    private int chlNum = 4;
////    private long lineCode = 0;
////    private long busCode = 0;
////    private int channelID = 0;
////    private int homeKey = 0;
////    private String beginTime;
////    private String endTime;
////    private Timer time;
////    public static ProgressDialog pd = null;
////    public ArrayList<String> busList = new ArrayList<String>();
////    public ArrayList<String> busIp = new ArrayList<String>();
////    @Override
////    protected void onDestroy() {
////        // TODO Auto-generated method stub
////        //	Thread.sleep(100);
////        super.onDestroy();
////        if (pd != null && pd.isShowing()) {
////            pd.dismiss();
////        }
////        Log.d("MainViewACtivity", "on destroy");
////
////    }
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        // TODO Auto-generated method stub
////        super.onCreate(savedInstanceState);
////        chlSel = new int[8];
////        time = new Timer();
////        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
////        Bundle bundle = getIntent().getExtras();
////
////        lineCode = bundle.getLong("lineCode");
////        busCode = bundle.getLong("busCode");
////        channelID = bundle.getInt("channelID");
////        chlSel = bundle.getIntArray("chlSel");
////        chlNum = bundle.getInt("chlNum");
////        beginTime = bundle.getString("beginTime");
////        endTime = bundle.getString("endTime");
////        busList = bundle.getStringArrayList("busList");
////        busIp = bundle.getStringArrayList("busIp");
////        SharedPreferencesHelper spHelper = new SharedPreferencesHelper(this,"date");
////        if (beginTime.equals("")&&endTime.equals("")){
////            spHelper.saveData(this,"beginTime",beginTime);
////            spHelper.saveData(this,"endTime",endTime);
////            videoSockMgr = VideoSocketManager.getManager();
////        }else {
////            spHelper.saveData(this,"beginTime",beginTime);
////            spHelper.saveData(this,"endTime",endTime);
////            videoSockMgr = VideoSocketManager.getManager();
////        }
////
////        BroadcastReceiver mHomeKeyEventReceiver = null;
////
////        if (chlNum <= 0) {
////            return;
////        }
////        playSound = new PlayPCM();
////        videoWin = new PlayView(this, chlSel, chlNum, busCode);
////
////        setContentView(videoWin);
////        System.out.println("MainViewActivity~~~~111111111111");
////        playSound.startSound();
////        videoWin.startVideo();
////
////        pd = ProgressDialog.show(MainViewActivity.this, "提示", "数据加载中，请稍后... ...");
////
////        time.schedule(new TimerTask() {
////            @Override
////            public void run() {
////                if (MainViewActivity.pd != null) {
////                    MainViewActivity.pd.dismiss();
////                    MainViewActivity.pd = null;
////                }
////            }
////        }, 10000);
////
////        Log.d("MainViewACtivity", "on create");
////    }
////
////    @Override
////    public boolean onKeyDown(int keyCode, KeyEvent event) {
////        if (keyCode == KeyEvent.KEYCODE_BACK) {//如果返回键按下
////            if (videoSockMgr.isStarted()) {
////                videoSockMgr.stop();
////            }
////            if (playSound != null) {
////                playSound.stopSound();
////                playSound = null;
////            }
////            //此处写退向后台的处理
////            videoSockMgr = VideoSocketManager.getManager();
////            videoSockMgr.stop();
////            videoWin.stopVideo();
////            Intent intent = new Intent(this,BusSelectActivity.class);
////            Bundle bl = new Bundle();
////            //	bl.putStringArrayList("chlSel", aa);
////            bl.putStringArrayList("busList", busList);
////            bl.putStringArrayList("busIp", busIp);
////            intent.putExtras(bl);
////            startActivity(intent);
////            finish();
////            return true;
////        }
////        return super.onKeyDown(keyCode, event);
////    }
////
////    @Override
////    protected void onPause() {
////        // TODO Auto-generated method stub
////        super.onPause();
////        if (videoSockMgr.isStarted()) {
////            videoSockMgr.stop();
////        }
////        if (playSound != null) {
////            playSound.stopSound();
////            playSound = null;
////        }
////        Log.d("MainViewACtivity", "close channel");
////    }
////
////    @Override
////    protected void onRestart() {
////        // TODO Auto-generated method stub
////        super.onRestart();
////        Log.d("MainViewACtivity", "on restart");
////        new AlertDialog.Builder(this)
////                .setTitle("提示框")
////                .setMessage("视频已经停止，请返回上一页！")
////                .setPositiveButton("确定", null)
////                .show();
////        //pd = ProgressDialog.show(MainViewActivity.this, "提示", "数据加载中，请稍后... ...");
//////		super.onRestart();
//////		Log.d("MainViewACtivity","restart activity");
//////		onDestroy();
//////		Intent intent = new Intent(MainViewActivity.this,
//////		UserLoginActivity.class);
//////		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//////		startActivityForResult(intent, 0);
////    }
////
////    @Override
////    protected void onStop() {
////        // TODO Auto-generated method stub
////        super.onStop();
////        Log.d("MainViewACtivity", "stop video");
////    }
////
////
////}
//package com.sdhy.video.client;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.content.BroadcastReceiver;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.WindowManager;
//
//import java.util.ArrayList;
//import java.util.Timer;
//import java.util.TimerTask;
//
//public class MainViewActivity extends Activity {
//    private VideoSocketManager videoSockMgr ;
//
//    private boolean closeFlg = false;
//    private PlayView videoWin = null;
//    private PlayPCM playSound = null;
//    private int[] chlSel;
//    private int chlNum = 4;
//    private long lineCode = 0;
//    private long busCode = 0;
//    private int channelID = 0;
//    private int homeKey = 0;
//    private String beginTime;
//    private String endTime;
//    private Timer time;
//    public static ProgressDialog pd = null;
//    public ArrayList<String> busList = new ArrayList<String>();
//    public ArrayList<String> busIp = new ArrayList<String>();
//    @Override
//    protected void onDestroy() {
//        // TODO Auto-generated method stub
//        //	Thread.sleep(100);
//        super.onDestroy();
//        if (pd != null && pd.isShowing()) {
//            pd.dismiss();
//        }
//        Log.d("MainViewACtivity", "on destroy");
//
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        super.onCreate(savedInstanceState);
//        chlSel = new int[8];
//        time = new Timer();
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        Bundle bundle = getIntent().getExtras();
//
//        lineCode = bundle.getLong("lineCode");
//        busCode = bundle.getLong("busCode");
//        channelID = bundle.getInt("channelID");
//        chlSel = bundle.getIntArray("chlSel");
//        chlNum = bundle.getInt("chlNum");
//        beginTime = bundle.getString("beginTime");
//        endTime = bundle.getString("endTime");
//        busList = bundle.getStringArrayList("busList");
//        busIp = bundle.getStringArrayList("busIp");
//        SharedPreferencesHelper spHelper = new SharedPreferencesHelper(this,"date");
//        if (beginTime.equals("")&&endTime.equals("")){
//            spHelper.saveData(this,"beginTime",beginTime);
//            spHelper.saveData(this,"endTime",endTime);
//            videoSockMgr = VideoSocketManager.getManager();
//        }else {
//            spHelper.saveData(this,"beginTime",beginTime);
//            spHelper.saveData(this,"endTime",endTime);
//            videoSockMgr = VideoSocketManager.getManager();
//        }
//
//        BroadcastReceiver mHomeKeyEventReceiver = null;
//
//        if (chlNum <= 0) {
//            return;
//        }
//        playSound = new PlayPCM();
//        videoWin = new PlayView(this, chlSel, chlNum, busCode);
//
//        setContentView(videoWin);
//        System.out.println("MainViewActivity~~~~111111111111");
//        playSound.startSound();
//        videoWin.startVideo();
//
//        pd = ProgressDialog.show(MainViewActivity.this, "提示", "数据加载中，请稍后... ...");
//
//        time.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (MainViewActivity.pd != null) {
//                    MainViewActivity.pd.dismiss();
//                    MainViewActivity.pd = null;
//                }
//            }
//        }, 10000);
//
//        Log.d("MainViewACtivity", "on create");
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {//如果返回键按下
//            if (videoSockMgr.isStarted()) {
//                videoSockMgr.stop();
//            }
//            if (playSound != null) {
//                playSound.stopSound();
//                playSound = null;
//            }
//            //此处写退向后台的处理
//            videoSockMgr.stop();
//            Intent intent = new Intent(this,BusSelectActivity.class);
//            Bundle bl = new Bundle();
//            //	bl.putStringArrayList("chlSel", aa);
//            bl.putStringArrayList("busList", busList);
//            bl.putStringArrayList("busIp", busIp);
//            intent.putExtras(bl);
//            startActivity(intent);
//            finish();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    @Override
//    protected void onPause() {
//        // TODO Auto-generated method stub
//        super.onPause();
//        if (videoSockMgr.isStarted()) {
//            videoSockMgr.stop();
//        }
//        if (playSound != null) {
//            playSound.stopSound();
//            playSound = null;
//        }
//        Log.d("MainViewACtivity", "close channel");
//    }
//
//    @Override
//    protected void onRestart() {
//        // TODO Auto-generated method stub
//        super.onRestart();
//        Log.d("MainViewACtivity", "on restart");
//        new AlertDialog.Builder(this)
//                .setTitle("提示框")
//                .setMessage("视频已经停止，请返回上一页！")
//                .setPositiveButton("确定", null)
//                .show();
//        //pd = ProgressDialog.show(MainViewActivity.this, "提示", "数据加载中，请稍后... ...");
////		super.onRestart();
////		Log.d("MainViewACtivity","restart activity");
////		onDestroy();
////		Intent intent = new Intent(MainViewActivity.this,
////		UserLoginActivity.class);
////		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////		startActivityForResult(intent, 0);
//    }
//
//    @Override
//    protected void onStop() {
//        // TODO Auto-generated method stub
//        super.onStop();
//        Log.d("MainViewACtivity", "stop video");
//    }
//
//
//}

package com.sdhy.video.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainViewActivity extends Activity {
    private VideoSocketManager videoSockMgr ;

    private boolean closeFlg = false;
    private PlayView videoWin = null;
    private PlayPCM playSound = null;
    private int[] chlSel;
    private int chlNum = 4;
    private long lineCode = 0;
    private long busCode = 0;
    private int channelID = 0;
    private int homeKey = 0;
    private String busType = "";
    private String beginTime;
    private String endTime;
    private Timer time;
    public static ProgressDialog pd = null;
    public ArrayList<String> busList = new ArrayList<String>();
    public ArrayList<String> busIp = new ArrayList<String>();
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        //	Thread.sleep(100);
        super.onDestroy();
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        Log.d("MainViewACtivity", "on destroy");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        chlSel = new int[8];
        time = new Timer();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Bundle bundle = getIntent().getExtras();

        lineCode = bundle.getLong("lineCode");
        busCode = bundle.getLong("busCode");
        channelID = bundle.getInt("channelID");
        chlSel = bundle.getIntArray("chlSel");
        chlNum = bundle.getInt("chlNum");
        busType = bundle.getString("busType");
        beginTime = bundle.getString("beginTime");
        endTime = bundle.getString("endTime");
        busList = bundle.getStringArrayList("busList");
        busIp = bundle.getStringArrayList("busIp");
        SharedPreferencesHelper spHelper = new SharedPreferencesHelper(this,"date");
        if (beginTime.equals("")&&endTime.equals("")){
            spHelper.saveData(this,"beginTime",beginTime);
            spHelper.saveData(this,"endTime",endTime);
            videoSockMgr = VideoSocketManager.getManager();
        }else {
            spHelper.saveData(this,"beginTime",beginTime);
            spHelper.saveData(this,"endTime",endTime);
            videoSockMgr = VideoSocketManager.getManager();
        }

        BroadcastReceiver mHomeKeyEventReceiver = null;

        if (chlNum <= 0) {
            return;
        }
        playSound = new PlayPCM();
        videoWin = new PlayView(this, chlSel, chlNum, busCode,busType);

        setContentView(videoWin);
        System.out.println("MainViewActivity~~~~111111111111");
        playSound.startSound();
        videoWin.startVideo();

        pd = ProgressDialog.show(MainViewActivity.this, "提示", "数据加载中，请稍后... ...");

        time.schedule(new TimerTask() {
            @Override
            public void run() {
                if (MainViewActivity.pd != null) {
                    MainViewActivity.pd.dismiss();
                    MainViewActivity.pd = null;
                }
            }
        }, 10000);

        Log.d("MainViewACtivity", "on create");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//如果返回键按下
            if (videoSockMgr.isStarted()) {
                videoSockMgr.stop();
            }
            if (playSound != null) {
                playSound.stopSound();
                playSound = null;
            }
            //此处写退向后台的处理
            videoSockMgr.stop();

//            ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//            List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
//            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList) {
//                if (runningAppProcessInfo.pid != android.os.Process.myPid()) {
//                    android.os.Process.killProcess(runningAppProcessInfo.pid);
//                }
//            }
//            android.os.Process.killProcess(android.os.Process.myPid());
//
//            Intent intent = new Intent(this,BusSelectActivity.class);
//            Bundle bl = new Bundle();
//            //	bl.putStringArrayList("chlSel", aa);
//            bl.putStringArrayList("busList", busList);
//            bl.putStringArrayList("busIp", busIp);
//            intent.putExtras(bl);
//            startActivity(intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (videoSockMgr.isStarted()) {
            videoSockMgr.stop();
        }
        if (playSound != null) {
            playSound.stopSound();
            playSound = null;
        }
        Log.d("MainViewACtivity", "close channel");
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        Log.d("MainViewACtivity", "on restart");
        new AlertDialog.Builder(this)
                .setTitle("提示框")
                .setMessage("视频已经停止，请返回上一页！")
                .setPositiveButton("确定", null)
                .show();
        //pd = ProgressDialog.show(MainViewActivity.this, "提示", "数据加载中，请稍后... ...");
//		super.onRestart();
//		Log.d("MainViewACtivity","restart activity");
//		onDestroy();
//		Intent intent = new Intent(MainViewActivity.this,
//		UserLoginActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivityForResult(intent, 0);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.d("MainViewACtivity", "stop video");
    }


}
