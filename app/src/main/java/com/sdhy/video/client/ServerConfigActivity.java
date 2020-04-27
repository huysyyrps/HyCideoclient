package com.sdhy.video.client;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class ServerConfigActivity extends Activity {
//	private String webServerAddr;
//	private String webServerPort;
//	
//	private String mainServerAddr;
//	private String mainServerPort;
//	
//	private String videoServerAddr;
//	private String videoServerPort;
//	
//	private String videoFrameDelay;
//	private long videoFrameFormat;
//	
//	private int videoDevice;

    private EditText edtWebServerAddr;
    private EditText edtWebServerPort;

    private EditText edtMainServerAddr;
    private EditText edtMainServerPort;

    private EditText edtVideoServerAddr;
    private EditText edtVideoServerPort;

    private EditText edtVideoFrameDelay;
    private Spinner sprinnerFormat;

    private CheckBox deviceCheckBox;

    private List<String> format_list;
    private ArrayAdapter<String> arr_adapt;

    private Button btnConfigSave;
    private Button btnConfigCancel;
    private RelativeLayout homepage_titleshe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.config);
        format_list = new ArrayList<String>();
        format_list.add("CIF");
        format_list.add("D1");
        //??????
        arr_adapt = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, format_list);
        //???????
        arr_adapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        edtWebServerAddr = (EditText) findViewById(R.id.edtWebServerAddr);
        edtWebServerPort = (EditText) findViewById(R.id.edtWebServerPort);

        edtMainServerAddr = (EditText) findViewById(R.id.edtMainServerAddr);
        edtMainServerPort = (EditText) findViewById(R.id.edtMainServerPort);

        edtVideoServerAddr = (EditText) findViewById(R.id.edtVideoServerAddr);
        edtVideoServerPort = (EditText) findViewById(R.id.edtVideoServerPort);

        edtVideoFrameDelay = (EditText) findViewById(R.id.edtVideoFrameDelay);
        sprinnerFormat = (Spinner) findViewById(R.id.spinnerFormat);

        deviceCheckBox = (CheckBox) findViewById(R.id.config_checkBox1);
        SharedPreferencesHelper spHelper = new SharedPreferencesHelper(ServerConfigActivity.this, "login");
        String type = spHelper.getData(ServerConfigActivity.this, "check", "");
        if (type.equals("yes")){
            deviceCheckBox.setChecked(true);
        }else if (type.equals("no")){
            deviceCheckBox.setChecked(false);
        }
        homepage_titleshe = (RelativeLayout) findViewById(R.id.homepage_titleshe);
        homepage_titleshe.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ServerConfigActivity.this.finish();
            }
        });
        //??????????
        sprinnerFormat.setAdapter(arr_adapt);


        //springFormat.setId(2);

        btnConfigSave = (Button) findViewById(R.id.btnConfigSave);
        btnConfigCancel = (Button) findViewById(R.id.btnConfigCancel);

        readConfig();


        btnConfigCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnConfigSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                String webServerAddr = edtWebServerAddr.getText().toString();
                String webServerPort = edtWebServerPort.getText().toString();

                String mainServerAddr = edtMainServerAddr.getText().toString();
                String mainServerPort = edtMainServerPort.getText().toString();

                String videoServerAddr = edtVideoServerAddr.getText().toString();
                String videoServerPort = edtVideoServerPort.getText().toString();

                String videoFrameDelay = edtVideoFrameDelay.getText().toString();
                long videoFrameFormat = sprinnerFormat.getSelectedItemId();

                boolean deviceFlg = deviceCheckBox.isChecked();

                SharedPreferences shareObj = getSharedPreferences("config", MODE_PRIVATE);
                SharedPreferences.Editor editor = shareObj.edit();

                editor.putString("webServerAddr", webServerAddr);
                editor.putString("webServerPort", webServerPort);

                editor.putString("mainServerAddr", mainServerAddr);
                editor.putString("mainServerPort", mainServerPort);

                editor.putString("videoServerAddr", videoServerAddr);
                editor.putString("videoServerPort", videoServerPort);

                editor.putString("videoFrameDelay", videoFrameDelay);
                editor.putLong("videoFrameFormat", videoFrameFormat);
                SharedPreferencesHelper spHelper = new SharedPreferencesHelper(ServerConfigActivity.this, "login");
                if (deviceFlg == false) {
                    spHelper.saveData(ServerConfigActivity.this, "check", "no");
                    editor.putInt("videoDevice", 0);
                } else {
                    spHelper.saveData(ServerConfigActivity.this, "check", "yes");
                    editor.putInt("videoDevice", 1);
                }
                if (editor.commit()) {
                    setResult(Activity.RESULT_OK);
                }

                finish();
            }
        });
    }

    private void readConfig() {
        SharedPreferences shareObj = getSharedPreferences("config", MODE_PRIVATE);
        String webServerAddr = shareObj.getString("webServerAddr", "");
        String webServerPort = shareObj.getString("webServerPort", "4001");

        String mainServerAddr = shareObj.getString("mainServerAddr", "");
        String mainServerPort = shareObj.getString("mainServerPort", "50001");

        String videoServerAddr = shareObj.getString("videoServerAddr", "");
        String videoServerPort = shareObj.getString("videoServerPort", "60005");

        String videoFrameDelay = shareObj.getString("videoFrameDelay", "100");
        long videoFrameFormat = shareObj.getLong("videoFrameFormat", 0);

        int videoDevice = shareObj.getInt("videoDevice", 0);

        edtWebServerAddr.setText(webServerAddr);
        edtWebServerPort.setText(webServerPort);

        edtMainServerAddr.setText(mainServerAddr);
        edtMainServerPort.setText(mainServerPort);

        edtVideoServerAddr.setText(videoServerAddr);
        edtVideoServerPort.setText(videoServerPort);

        edtVideoFrameDelay.setText(videoFrameDelay);
        sprinnerFormat.setSelection((int) videoFrameFormat, true);

        boolean flg = false;

        if (videoDevice != 0) {
            flg = true;
        }

//        deviceCheckBox.setChecked(flg);

    }

}
