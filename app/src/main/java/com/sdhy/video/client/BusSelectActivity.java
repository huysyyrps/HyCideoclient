package com.sdhy.video.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.BusAdapter;
import com.component.DatePickActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


@SuppressLint("NewApi")
public class BusSelectActivity extends Activity implements OnClickListener {

    private ClientSocketManager clientSockMgr = ClientSocketManager.getManager();

    private VideoSocketManager videoSockMgr = VideoSocketManager.getManager();

    public static long lineCode = 17;
    public static long busCode = 1701;
    public static String typeBus = "";
    public static String Ip = null;
    public ArrayList<String> busList = new ArrayList<String>();
    public ArrayList<String> busIp = new ArrayList<String>();
    private String huifang = "1";
    private int channelID = 15;
    private int channelNum = 8;


    private InstantAutoComplete actvLineCode;
    private InstantAutoComplete actvBusCode;
    //	private Spinner spinnerLine;
//	private Spinner spinnerBus;
    private long time = 0;
    private long time2 = 0;

    private int[] chlSel = new int[8];

    private Button btnNextStep;
    private ImageView btnBackReturn;

    private RadioButton radioFourChl;
    private RadioButton radioEightChl;
    private RadioButton radioSelectChl;

    private CheckBox checkChannel1;
    private CheckBox checkChannel2;
    private CheckBox checkChannel3;
    private CheckBox checkChannel4;
    private CheckBox checkChannel5;
    private CheckBox checkChannel6;
    private CheckBox checkChannel7;
    private CheckBox checkChannel8;
    private CheckBox cbType;
    private Button btnServer;
    private EditText repairTimeEt, repairlast_time_etlast;
    private ArrayAdapter<String> adapterLine = null;
    private ArrayAdapter<String> adapterBus = null;
    private String datefirst, datelast;
    private Object lock;
    private RadioButton btn_man;
    private RadioButton btn_women;
    private LinearLayout kaishi, jieshu;
    SharedPreferencesHelper spHelper;
    SharedPreferencesHelper spHelper1;
    private static boolean isExit = false;
    private TextView tv1,tv2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spHelper = new SharedPreferencesHelper(this,"tag");
        spHelper1 = new SharedPreferencesHelper(BusSelectActivity.this, "login");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Bundle bundle = getIntent().getExtras();
        lock = new Object();
        busList = bundle.getStringArrayList("busList");
        busIp = bundle.getStringArrayList("busIp");
	    System.out.println("busList="+busList);
	    System.out.println("busIp="+busIp);
        setContentView(R.layout.select);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BusSelectActivity.this,TreatyActivity.class);
                intent.putExtra("tag","1");
                startActivity(intent);
            }
        });
        tv2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BusSelectActivity.this,TreatyActivity.class);
                intent.putExtra("tag","2");
                startActivity(intent);
            }
        });
        cbType = (CheckBox) findViewById(R.id.cbType);
        btnServer = (Button) findViewById(R.id.btnServer);
        final String type = spHelper1.getData(BusSelectActivity.this, "check", "");
        if (type.equals("yes")){
            cbType.setChecked(true);
        }else if (type.equals("no")){
            cbType.setChecked(false);
        }

        if (videoSockMgr.isStarted()) {
            videoSockMgr.stop();
        }

        btn_man = (RadioButton) findViewById(R.id.btn_re0);
        btn_women = (RadioButton) findViewById(R.id.btn_re1);
        kaishi = (LinearLayout) findViewById(R.id.kaishishijian);
        jieshu = (LinearLayout) findViewById(R.id.jieshushijian);
        btn_man.setOnClickListener(this);
        btn_women.setOnClickListener(this);
        repairTimeEt = (EditText) findViewById(R.id.repair_time_et);
        repairlast_time_etlast = (EditText) findViewById(R.id.repairlast_time_etlast);
        repairTimeEt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusSelectActivity.this, DatePickActivity.class);
                intent.putExtra("date", repairTimeEt.getText().toString());
                startActivityForResult(intent, 1);
            }
        });
        repairlast_time_etlast.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusSelectActivity.this, DatePickActivity.class);
                intent.putExtra("date", repairlast_time_etlast.getText().toString());
                startActivityForResult(intent, 2);
            }
        });
        checkChannel1 = (CheckBox) findViewById(R.id.cbChannelNum1);
        checkChannel2 = (CheckBox) findViewById(R.id.cbChannelNum2);
        checkChannel3 = (CheckBox) findViewById(R.id.cbChannelNum3);
        checkChannel4 = (CheckBox) findViewById(R.id.cbChannelNum4);
        checkChannel5 = (CheckBox) findViewById(R.id.cbChannelNum5);
        checkChannel6 = (CheckBox) findViewById(R.id.cbChannelNum6);
        checkChannel7 = (CheckBox) findViewById(R.id.cbChannelNum7);
        checkChannel8 = (CheckBox) findViewById(R.id.cbChannelNum8);

        radioFourChl = (RadioButton) findViewById(R.id.cbFourChannel);
        radioEightChl = (RadioButton) findViewById(R.id.cbEightChannel);

        radioSelectChl = (RadioButton) findViewById(R.id.cbSelectChannel);

        actvLineCode = (InstantAutoComplete) findViewById(R.id.actvLineCode);
        actvBusCode = (InstantAutoComplete) findViewById(R.id.actvBusCode);

        actvLineCode.setThreshold(0);
        actvBusCode.setThreshold(0);

        btnNextStep = (Button) findViewById(R.id.btnNextStep);
        btnBackReturn = (ImageView) findViewById(R.id.btnBackReturn);


        time = System.currentTimeMillis();
        time2 = time;

        SetCheckBoxType(false);

        //initLineAdapter();
        new Thread() {
            @Override
            public void run() {
                if (!clientSockMgr.start(ConstParm.conAddr, ConstParm.conPort)) {
                    showMsg("连接通讯服务器失败");
                    return;
                }
                clientSockMgr.openLogin();
            }

        }.start();


        actvLineCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
//                new Handler().postDelayed(new Runnable() {
//                    public void run() {
                        //你需要跳转的地方的代码
                        if (actvLineCode.getText().length() == 0) {
                            long t = System.currentTimeMillis();
                            if (t - time > 3000) {
                                time = t;
                                initLineAdapter();
                                //Thread.sleep(50);
                                actvLineCode.showDropDown();
                            }
                        }
//                    }
//                },1000); //延迟2秒跳转
                return false;
            }
        });

        actvLineCode.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    initLineAdapter();
                }
            }
        });

        actvLineCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                actvBusCode.setText("");
                initLineAdapter();
            }
        });

        actvBusCode.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                if (actvBusCode.getText().length() == 0) {

                    long t = System.currentTimeMillis();
                    if (t - time2 > 500) {
                        time2 = t;
                        initLineAdapter();
                        actvBusCode.showDropDown();
                    }
                }

                return false;
            }
        });

        actvBusCode.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    initBusAdapter();
                }
            }
        });

        actvBusCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                initBusAdapter();
            }
        });

        radioSelectChl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                //boolean flg = radioSelectChl.isChecked();
                SetCheckBoxType(true);
            }
        });

        radioFourChl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (checkChannel1.isEnabled()) {
                    SetCheckBoxType(false);
                }
            }
        });

        radioEightChl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (checkChannel1.isEnabled()) {
                    SetCheckBoxType(false);
                }
            }
        });

        btnBackReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoSockMgr.isStarted()) {
                    videoSockMgr.stop();
                }
                finish();
            }
        });

        btnServer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BusSelectActivity.this,ServerConfigActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnNextStep.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean deviceFlg = cbType.isChecked();
                if (deviceFlg == false) {
                    spHelper1.saveData(BusSelectActivity.this, "check", "no");
                } else {
                    spHelper1.saveData(BusSelectActivity.this, "check", "yes");
                }
                if (huifang.equals("1")) {
                    //直播
                    String line = actvLineCode.getText().toString();
                    String bus1 = actvBusCode.getText().toString();
                    String bus = actvBusCode.getText().toString().split("---")[0];
                    if (line == "" || bus == "") {
                        new AlertDialog.Builder(BusSelectActivity.this)
                                .setTitle("提示").setMessage("线路号车号不能为空")
                                .setPositiveButton("确定", null).show();
                        return;
                    }

                    try {
                        lineCode = Long.parseLong(line);
                    } catch (Exception ex) {
                        new AlertDialog.Builder(BusSelectActivity.this)
                                .setTitle("提示").setMessage("线路号错误")
                                .setPositiveButton("确定", null).show();
                        return;
                    }

                    try {
                        busCode = Long.parseLong(bus1.split("---")[0]);
                        typeBus = bus1.split("---")[1];
                    } catch (Exception ex) {
                        new AlertDialog.Builder(BusSelectActivity.this)
                                .setTitle("提示").setMessage("车号错误")
                                .setPositiveButton("确定", null).show();
                        return;
                    }

                    if (radioFourChl.isChecked()) {
                        spHelper.saveData(BusSelectActivity.this,"chtag","top");
                        channelID = 0x0F;
                        channelNum = 4;
                        for (int i = 0; i < 8; i++) {
                            if (i < 4) {
                                chlSel[i] = 1;
                            } else {
                                chlSel[i] = 0;
                            }
                        }
                    } else if (radioEightChl.isChecked()) {
                        spHelper.saveData(BusSelectActivity.this,"chtag","top");
                        channelID = 0xFF;
                        channelNum = 8;
                        for (int i = 0; i < 8; i++) {
                            chlSel[i] = 1;
                        }
                    } else {
                        //自定义通道
                        spHelper.saveData(BusSelectActivity.this,"chtag","button");
                        AnalysisChannelID();

                    }

                    int flg = 0;
                    Log.e("BusSelectActivity", "busList2.length = " + busList.size());
                    for (int i = 0; i < busList.size(); i++) {
                        String str = busList.get(i).toString();
                        String ip = busIp.get(i).toString();
                        if (str.equals(bus)) {
//                            System.out.println("i=" + i + "和" + busList.get(i) + "和" + busIp.get(i));
                            flg = 1;
                            Ip = ip;
                        }
                    }

                    if (flg == 0) {
                        new AlertDialog.Builder(BusSelectActivity.this)
                                .setTitle("提示").setMessage("您没有查看此视频的权限！")
                                .setPositiveButton("确定", null).show();
                        return;
                    }

                    new Thread() {
                        @Override
                        public void run() {
                            String[] addr = Ip.split(":");
                            String ip = addr[0];
                            String port = addr[1];
                            if (!videoSockMgr.start(ConstParm.videoAddr, ConstParm.videoPort)) {
                                showMsg("连接视频服务器失败");
                                return;
                            }
                            //发送视屏回放
                            boolean iden = videoSockMgr.openChannel(lineCode, busCode, (byte) channelID, "", "", chlSel, ip, port,typeBus);// 15四通道视频传输，即1111
                        }
                    }.start();
                    try {
                        Intent intent = new Intent(BusSelectActivity.this, MainViewActivity.class);
                        ArrayList<String> aa = new ArrayList<String>();
                        for (int i = 0; i < 4; i++) {
                            aa.add(Integer.toString(chlSel[i]));
                        }
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        //				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        //				if (imm.isActive())  //一直是true
                        //					imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                        //					     InputMethodManager.HIDE_NOT_ALWAYS);
                        //	Log.e("****************", aa.get(0)+"-"+aa.get(1));
                        Bundle bl = new Bundle();
                        //	bl.putStringArrayList("chlSel", aa);
                        bl.putInt("chlNum", channelNum);
                        bl.putIntArray("chlSel", chlSel);
                        bl.putLong("lineCode", lineCode);
                        bl.putLong("busCode", busCode);
                        bl.putString("busType", typeBus);
                        bl.putInt("channelID", channelID);
                        bl.putString ("beginTime", "");
                        bl.putString ("endTime", "");
                        bl.putStringArrayList("busList", busList);
                        bl.putStringArrayList("busIp", busIp);
                        intent.putExtras(bl);
                        startActivity(intent);
//                        finish();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                } else if (huifang.equals("2")) {
                    //回放
                    String line = actvLineCode.getText().toString();
                    String bus1 = actvBusCode.getText().toString();
                    String bus = actvBusCode.getText().toString().split("---")[0];
                    String timekai = repairTimeEt.getText().toString();
                    String timejieshu = repairlast_time_etlast.getText().toString();

                    if (line == "" || bus == "" || timekai == "" || timejieshu == "") {
                        new AlertDialog.Builder(BusSelectActivity.this)
                                .setTitle("提示").setMessage("线路号车号开始结束时间不能为空")
                                .setPositiveButton("确定", null).show();
                        return;
                    }
                    try {
                        lineCode = Long.parseLong(line);
                    } catch (Exception ex) {
                        new AlertDialog.Builder(BusSelectActivity.this)
                                .setTitle("提示").setMessage("线路号错误")
                                .setPositiveButton("确定", null).show();
                        return;
                    }
                    try {
                        busCode = Long.parseLong(bus1.split("---")[0]);
                        typeBus = bus1.split("---")[1];
                    } catch (Exception ex) {
                        new AlertDialog.Builder(BusSelectActivity.this)
                                .setTitle("提示").setMessage("车号错误")
                                .setPositiveButton("确定", null).show();
                        return;
                    }
                    if (radioFourChl.isChecked()) {
                        spHelper.saveData(BusSelectActivity.this,"chtag","top");
                        channelID = 0x0F;
                        channelNum = 4;
                        for (int i = 0; i < 8; i++) {
                            if (i < 4) {
                                chlSel[i] = 1;
                            } else {
                                chlSel[i] = 0;
                            }
                        }
                    } else if (radioEightChl.isChecked()) {
                        spHelper.saveData(BusSelectActivity.this,"chtag","top");
                        channelID = 0xFF;
                        channelNum = 8;
                        for (int i = 0; i < 8; i++) {
                            chlSel[i] = 1;
                        }
                    } else {
                        spHelper.saveData(BusSelectActivity.this,"chtag","button");
                        AnalysisChannelID();

                    }
                    int flg = 0;
                    Log.e("BusSelectActivity", "busList2.length = " + busList.size());
                    for (int i = 0; i < busList.size(); i++) {
                        String str = busList.get(i).toString();
                        String ip = busIp.get(i).toString();
                        if (str.equals(bus)) {
//                            System.out.println("i=" + i + "和" + busList.get(i) + "和" + busIp.get(i));
                            flg = 1;
                            Ip = ip;
                        }
                    }
                    if (flg == 0) {
                        new AlertDialog.Builder(BusSelectActivity.this)
                                .setTitle("提示").setMessage("您没有查看此视频的权限！")
                                .setPositiveButton("确定", null).show();
                        return;
                    }
                    new Thread() {
                        @Override
                        public void run() {
                            String[] addr = Ip.split(":");
                            String ip = addr[0];
                            String port = addr[1];
                            if (!videoSockMgr.start(ConstParm.videoAddr,
                                    ConstParm.videoPort)) {
                                showMsg("连接视频服务器失败");
                                return;
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            Date date_utilf;
                            Date date_utill;
                            String res;
                            String resl;
                            try {
                                if (datefirst != null && datelast != null) {
                                    date_utilf = sdf.parse(datefirst);
                                    date_utill = sdf.parse(datelast);
                                    long ts = date_utilf.getTime();
                                    long tsl = date_utill.getTime();
                                    res = String.valueOf(ts);
                                    resl = String.valueOf(tsl);
                                    Log.e(null, "===========" + res.substring(0, res.length() - 3));

                                    boolean iden = videoSockMgr.openChannel(lineCode, busCode, (byte) channelID,
                                            datefirst, datelast, chlSel, ip, port,typeBus);// 15四通道视频传输，即1111
                                    System.out.println("iden=" + iden);
                                } else {
                                    Message message = new Message();
                                    message.what = 1;
                                    handler.sendMessage(message);
                                }
                            } catch (ParseException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } //转换为util.date
                        }
                    }.start();
                    if (datefirst != null && datelast != null) {
                        try {
                            Intent intent = new Intent(BusSelectActivity.this, MainViewActivity.class);
                            ArrayList<String> aa = new ArrayList<String>();
                            for (int i = 0; i < 4; i++) {
                                aa.add(Integer.toString(chlSel[i]));
                            }
                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                            //				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            //				if (imm.isActive())  //一直是true
                            //					imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                            //					     InputMethodManager.HIDE_NOT_ALWAYS);
                            //	Log.e("****************", aa.get(0)+"-"+aa.get(1));
                            Bundle bl = new Bundle();
                            //	bl.putStringArrayList("chlSel", aa);
                            bl.putInt("chlNum", channelNum);
                            bl.putIntArray("chlSel", chlSel);
                            bl.putLong("lineCode", lineCode);
                            bl.putLong("busCode", busCode);
                            bl.putString("busType", typeBus);
                            bl.putInt("channelID", channelID);
                            bl.putString ("beginTime", datefirst);
                            bl.putString ("endTime", datelast);
                            bl.putStringArrayList("busList", busList);
                            bl.putStringArrayList("busIp", busIp);
                            intent.putExtras(bl);
                            startActivityForResult(intent, 0);
//                            finish();
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }
                }
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Toast.makeText(BusSelectActivity.this, "开始时间，结束时间不能为空", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        if (videoSockMgr.isStarted()) {
            videoSockMgr.stop();
        }
    }

    private void initLineAdapter() {
        if (clientSockMgr.getLineMap().isEmpty()) {
            return;
        }
        List<String> listLine = new ArrayList<String>();
        getLineListFromMap(clientSockMgr.getLineMap(), listLine);
        if (listLine.size() > 1) {
            //排序
            Collections.sort(listLine, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
        }
        adapterLine = new ArrayAdapter<String>(BusSelectActivity.this, R.layout.down, R.id.contentTextView, listLine);
        actvLineCode.setAdapter(adapterLine);
    }

    private void initBusAdapter() {

        String lineCode = actvLineCode.getText().toString();

        if (lineCode.isEmpty()) {
            Toast.makeText(BusSelectActivity.this, "线路号不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        if (clientSockMgr.getLineMap().isEmpty() || clientSockMgr.getLineMap().containsKey(lineCode) == false) {
            return;
        }
        Map<String, String> busMap = clientSockMgr.getLineMap().get(lineCode);
        List<String> listBus = new ArrayList<String>();

        getBusListFromMap(busMap, listBus);
        if (listBus.size() > 1) {
            //排序
            Collections.sort(listBus, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
        }
        for (int i = 0;i<listBus.size();i++){
            Log.e("CCCCC",listBus.get(i));
        }
        BusAdapter adapter = new BusAdapter(BusSelectActivity.this,0, listBus);
        adapterBus = new ArrayAdapter<String>(BusSelectActivity.this, R.layout.down, R.id.contentTextView, listBus);
        actvBusCode.setAdapter(adapterBus);
    }

    private void getLineListFromMap(Map<String, Map<String, String>> srcMap, List<String> dstList) {
        if (srcMap == null) {
            return;
        }
        if (dstList == null) {
            dstList = new ArrayList<String>();
        }
        Set<String> keySet = srcMap.keySet();
        Iterator<String> i = keySet.iterator();


        while (i.hasNext()) {
            String str = i.next();
            if (str != null) {
                dstList.add(str);
            }
        }
    }

    private void getBusListFromMap(Map<String, String> srcMap, List<String> dstList) {
        if (srcMap == null) {
            return;
        }

        if (dstList == null) {
            dstList = new ArrayList<String>();
        }

        Set<Entry<String, String>> entrySet = srcMap.entrySet();
        Iterator<Entry<String, String>> i = entrySet.iterator();

//        Set<Entry<String, String>> entrySet1 = typeMap.entrySet();
//        Iterator<Entry<String, String>> k = entrySet1.iterator();

//        for (int k = 0;k<entrySet.size();k++){
//            Entry<String, String> entry = i.next();
//            dstList.set(k,entry.getKey()+"---"+entry.getValue());
//        }
        while (i.hasNext()) {
            Entry<String, String> entry = i.next();
//            Entry<String, String> entry1 = k.next();
//            if (System.currentTimeMillis() - entry.getValue().longValue() < 120 * 1000) {
            //<img src=\"" +  IMG_PATH+ tokens[i] + "\"/>
            dstList.add(entry.getKey()+"---"+entry.getValue());
//            dstList.add(entry.getKey()+"");
//            if (dstList.size() == 0){
//                dstList.add(entry.getKey()+"---"+entry.getValue());
//            }else {
//                for(int k = 0;k<dstList.size();k++){
////                    if (!dstList.get(k).split("---")[0].equals(busCode)){
////                        dstList.add(entry.getKey()+"---"+entry.getValue());
////                    }
//                    if (!dstList.contains(busCode+"---"+"new")||!dstList.contains(busCode+"---"+"old")) {
//                        Log.e("buscode:",busCode+"---"+"new");
//                        dstList.add(entry.getKey()+"---"+entry.getValue());
//                    }
//                }
//            }
////            }
        }
    }

    private void showMsg(String msg) {
        class ShowWork implements Runnable {
            private String msg = null;

            public ShowWork(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new ShowWork(msg));
    }

    private void SetCheckBoxType(boolean flg) {

        checkChannel1.setEnabled(flg);
        checkChannel2.setEnabled(flg);
        checkChannel3.setEnabled(flg);
        checkChannel4.setEnabled(flg);
        checkChannel5.setEnabled(flg);
        checkChannel6.setEnabled(flg);
        checkChannel7.setEnabled(flg);
        checkChannel8.setEnabled(flg);

    }

    private void AnalysisChannelID() {
        //int id = 0;
        int n = 0;
        channelID = 0;
        for (int i = 0; i < 8; i++) {
            chlSel[i] = 0;
        }
        if (checkChannel1.isChecked()) {
            channelID |= 0x01;
            chlSel[0] = 1;
            n++;
        }
        if (checkChannel2.isChecked()) {
            channelID |= 0x02;
            chlSel[1] = 1;
            n++;
        }
        if (checkChannel3.isChecked()) {
            channelID |= 0x04;
            chlSel[2] = 1;
            n++;
        }
        if (checkChannel4.isChecked()) {
            channelID |= 0x08;
            chlSel[3] = 1;
            n++;
        }
        if (checkChannel5.isChecked()) {
            channelID |= 0x10;
            chlSel[4] = 1;
            n++;
        }
        if (checkChannel6.isChecked()) {
            channelID |= 0x20;
            chlSel[5] = 1;
            n++;
        }
        if (checkChannel7.isChecked()) {
            channelID |= 0x40;
            chlSel[6] = 1;
            n++;
        }
        if (checkChannel8.isChecked()) {
            channelID |= 0x80;
            chlSel[7] = 1;
            n++;
        }
        channelNum = n;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            // 选择预约时间的页面被关闭
            datefirst = data.getStringExtra("date");
            if (!repairTimeEt.getText().toString().equals(datefirst)) {
                repairTimeEt.setText(data.getStringExtra("date"));
            } else {
                System.out.println("选择未变");
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == 2) {
            datelast = data.getStringExtra("date");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            try {
                Date dt1 = df.parse(datefirst);
                Date dt2 = df.parse(datelast);
                if (dt1.getTime() > dt2.getTime()) {
                    Toast.makeText(this, "结束时间必须大于开始时间", Toast.LENGTH_SHORT).show();
                } else if (dt1.getTime() < dt2.getTime()) {
                    if (!repairlast_time_etlast.getText().toString().equals(datelast)) {
                        repairlast_time_etlast.setText(data.getStringExtra("date"));
                    } else {
                        System.out.println("选择未变");
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_re0:
                huifang = "1";
                kaishi.setVisibility(View.GONE);
                jieshu.setVisibility(View.GONE);
                break;
            case R.id.btn_re1:
                huifang = "2";
                kaishi.setVisibility(View.VISIBLE);
                jieshu.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    //推出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
//            ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//            List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
//            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList) {
//                if (runningAppProcessInfo.pid != android.os.Process.myPid()) {
//                    android.os.Process.killProcess(runningAppProcessInfo.pid);
//                }
//            }
//            android.os.Process.killProcess(android.os.Process.myPid());
            finish();
            System.exit(0);
        }
    }

    //推出程序
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

}
