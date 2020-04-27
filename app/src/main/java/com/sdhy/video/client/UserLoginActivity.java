package com.sdhy.video.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
//import com.service.DBHandl

public class UserLoginActivity extends Activity {
//	public static String mainServerAddr = null;
//	public static int mainServerPort = 50001;
//
//	public static String videoServerAddr = null;
//	public static int videoServerPort = 60005;
//
//	public static String webServerAddr = null;
//	public static int webServerPort = 4001;
//
//	public static int videoFrameDelay = 80;
//	public static int videoFrameFormat = 0;
//
//	public static int videoDeviceType = 0;

    private MyHandler handler = null;

    public ArrayList<String> busList = new ArrayList<String>();
    public ArrayList<String> busIp = new ArrayList<String>();

    public String URL = "";
    public String downURL = "";

    // 检查网络连接的管理器
    public ConnectivityManager netConn = null;
    //软件版本管理器
//	private UpdateVersionManager mUpdateManager = null;
//    public static int version=0;

    public boolean isFirstRun = false; //true :第一次运行 ,false:不是第一次
    private String userType = "";
    private static String TAG = "访问数据库错误";

    public String loginMsg;

    private String userName;
    private String userPwd;

    private EditText edtUserName;
    private EditText edtUserPwd;

    private Button btnUserLogin;
    private Button btnServerConfig;
    public boolean isLogining = false; //登陆中
    public static ProgressDialog pd = null;
    private Timer time = new Timer();
    private CheckBox mPswCheck;
    private TextView tv1,tv2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.login);
        handler = new MyHandler();
        edtUserName = (EditText) findViewById(R.id.edtUserName);
        edtUserPwd = (EditText) findViewById(R.id.edtUserPwd);
        mPswCheck = (CheckBox) findViewById(R.id.psw_ckb);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserLoginActivity.this,TreatyActivity.class);
                intent.putExtra("tag","1");
                startActivity(intent);
            }
        });
        tv2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserLoginActivity.this,TreatyActivity.class);
                intent.putExtra("tag","2");
                startActivity(intent);
            }
        });
        btnUserLogin = (Button) findViewById(R.id.btnUserLogin);
        //配置按键
        btnServerConfig = (Button) findViewById(R.id.btnServerConfig);

        SharedPreferences shareObj = getSharedPreferences("config", MODE_PRIVATE);
        String userCode = shareObj.getString("userCode", "");
        String password = shareObj.getString("password", "");
        Toast.makeText(this, password, Toast.LENGTH_SHORT).show();
        boolean pswChecked = shareObj.getBoolean("pswChecked", false);
        edtUserName.setText(userCode);
        edtUserPwd.setText(password);
        mPswCheck.setChecked(pswChecked);

        netConn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        readConfig();

        //检查网络通不通
        if (!getNetWorkState()) {//wifi,3G。
            Toast.makeText(UserLoginActivity.this, "都不通 网络不通，请检查3G或WIFI是否开启", Toast.LENGTH_LONG).show();
        }
        btnUserLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = edtUserName.getText().toString().trim();
                userPwd = edtUserPwd.getText().toString().trim();
                if (ConstParm.webAddr == "" || ConstParm.conAddr == ""
                        || ConstParm.videoAddr == "") {
                    new AlertDialog.Builder(UserLoginActivity.this)
                            .setTitle("提示").setMessage("服务器地址不能为空")
                            .setPositiveButton("确定", null).show();
                    return;
                }
                if (CheckWebServerAddr(ConstParm.webAddr) == false) {
                    Toast.makeText(UserLoginActivity.this, "web服务器IP无效！", Toast.LENGTH_LONG).show();
                    return;
                }

                //检查网络通不通
                if (!getNetWorkState()) {//wifi,3G。
                    Toast.makeText(UserLoginActivity.this, "都不通 网络不通，请检查3G或WIFI是否开启", Toast.LENGTH_LONG).show();
                }

                if (isLogining) {
                    Toast.makeText(UserLoginActivity.this, "登陆中,请稍后再试", Toast.LENGTH_LONG).show();
                    return;
                }
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Message login_ms = new Message();
                        login_ms.what = 5;
                        handler.sendMessage(login_ms);

                        isLogining = true;
                        String str = "/sdhyschedule/PhoneQueryAction";
                        URL = "http://" + ConstParm.webAddr + ":" + ConstParm.webPort + str;
//                        URL = "http://" + "192.168.2.116" + ":" + "8088" + str;

                        String retMess = getPhoneUser(userName, userPwd, URL
                                + "!getVideoLoginUser.shtml", isFirstRun);

                        if (retMess.equals("0")) {
                            loginMsg = "0";
                        } else {
                            loginMsg = retMess;//.substring(0, retMess.indexOf(","));
                            //userType = retMess.substring(retMess.indexOf(",") + 1);
                        }

                        //Log.d("登陆验证返回", loginMsg);
                        Message ms = new Message();
                        if (loginMsg.equals("1") || loginMsg.equals("2")) {
                            ms.what = 0;
                            SharedPreferences shareObj = getSharedPreferences("config", MODE_PRIVATE);
                            SharedPreferences.Editor preM = shareObj.edit();

                            if (mPswCheck.isChecked()) {
                                preM.putString("userCode", userName);

                                preM.putString("password", userPwd);
                                preM.putBoolean("pswChecked", true);
                                preM.commit();
                            } else {
                                preM.putString("userCode", "");
                                preM.putString("password", "");
                                preM.putBoolean("pswChecked", false);
                                preM.commit();
                            }
//							Intent intent = new Intent(UserLoginActivity.this,
//									BusSelectActivity.class);
//							startActivity(intent);
                        } else if (loginMsg.equals("3") || loginMsg.equals("4")) {
                            ms.what = 1;
//							new AlertDialog.Builder(UserLoginActivity.this)
//									.setTitle("提示").setMessage("用户名或密码错误")
//									.setPositiveButton("确定", null).show();
                        } else {
                            ms.what = 2;
                        }
                        handler.sendMessage(ms);
                        isLogining = false;
                    }

                };
                thread.start();
            }


        });


        btnServerConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserLoginActivity.this, ServerConfigActivity.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    private void setLogin() {
                userName = edtUserName.getText().toString().trim();
                userPwd = edtUserPwd.getText().toString().trim();

                if (ConstParm.webAddr == "" || ConstParm.conAddr == "" || ConstParm.videoAddr == "") {
                    new AlertDialog.Builder(UserLoginActivity.this)
                            .setTitle("提示").setMessage("服务器地址不能为空")
                            .setPositiveButton("确定", null).show();
                    return;
                }
                if (CheckWebServerAddr(ConstParm.webAddr) == false) {
                    Toast.makeText(UserLoginActivity.this, "web服务器IP无效！", Toast.LENGTH_LONG).show();
                    return;
                }

                //检查网络通不通
                if (!getNetWorkState()) {//wifi,3G。
                    Toast.makeText(UserLoginActivity.this, "都不通 网络不通，请检查3G或WIFI是否开启", Toast.LENGTH_LONG).show();
                }

                if (isLogining) {
                    Toast.makeText(UserLoginActivity.this, "登陆中,请稍后再试", Toast.LENGTH_LONG).show();
                    return;
                }
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Message login_ms = new Message();
                        login_ms.what = 5;
                        handler.sendMessage(login_ms);

                        isLogining = true;
                        String str = "/sdhyschedule/PhoneQueryAction";
                        URL = "http://" + ConstParm.webAddr + ":" + ConstParm.webPort + str;

                        String retMess = getPhoneUser(userName, userPwd, URL + "!getVideoLoginUser.shtml", isFirstRun);

                        if (retMess.equals("0")) {
                            loginMsg = "0";
                        } else {
                            loginMsg = retMess;//.substring(0, retMess.indexOf(","));
                            //userType = retMess.substring(retMess.indexOf(",") + 1);
                        }

                        //Log.d("登陆验证返回", loginMsg);
                        Message ms = new Message();
                        if (loginMsg.equals("1") || loginMsg.equals("2")) {
                            ms.what = 0;
                            SharedPreferences shareObj = getSharedPreferences("config", MODE_PRIVATE);
                            SharedPreferences.Editor preM = shareObj.edit();

                            if (mPswCheck.isChecked()) {
                                preM.putString("userCode", userName);

                                preM.putString("password", userPwd);
                                preM.putBoolean("pswChecked", true);
                                preM.commit();
                            } else {
                                preM.putString("userCode", "");
                                preM.putString("password", "");
                                preM.putBoolean("pswChecked", false);
                                preM.commit();
                            }
//							Intent intent = new Intent(UserLoginActivity.this,
//									BusSelectActivity.class);
//							startActivity(intent);
                        } else if (loginMsg.equals("3") || loginMsg.equals("4")) {
                            ms.what = 1;
//							new AlertDialog.Builder(UserLoginActivity.this)
//									.setTitle("提示").setMessage("用户名或密码错误")
//									.setPositiveButton("确定", null).show();
                        } else {
                            ms.what = 2;
                        }

                        handler.sendMessage(ms);
                        isLogining = false;
                    }

                };
                thread.start();
//					else {
//						String s = "";
//						s = "其他错误，错误代码" + loginMsg + "!";
//						Toast.makeText(UserLoginActivity.this, s,
//								Toast.LENGTH_LONG).show();
//
//					}

//				} catch (Exception ex) {
//					new AlertDialog.Builder(UserLoginActivity.this)
//							.setTitle("提示").setMessage("URL链接失败！")
//							.setPositiveButton("确定", null).show();
//				}
    }

    //检查网络通不通
    public boolean getNetWorkState() {
        try {
            State wifi = netConn.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            Log.d("WIFI网络状态", wifi.toString());
            State mobile = netConn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            Log.d("3G网络状态", mobile.toString());

            if (wifi.toString().equalsIgnoreCase("connected") || mobile.toString().equalsIgnoreCase("connected")) {
                return true;
            } else {
                return false;
            }

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("网络检测失败！", "error6:" + e.toString());
            //Log.e("错误6","llllllllllllllllllllll");
        }

//		try
//		{
//
//			State mobile = netConn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
//			Log.d("3G网络状态", mobile.toString());
//
//			if(mobile.toString().equalsIgnoreCase("connected")){
//		    	 return true;
//		     }
//
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch(Exception e)
//		{
//			Log.e("网络检测失败！", "error5:" + e.toString());
//			Log.e("错误5","啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦");
//		}
//


        return false;

    }

    private void getVersion() {
        // TODO Auto-generated method stub

    }


    @Override
    protected void onDestroy() {
        //Log.i(this.getClass().getName(), "onDestroy");
        super.onDestroy();
        System.exit(0);
        busIp.clear();
        busList.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            readConfig();
        }
    }

    private void readConfig() {
        SharedPreferences shareObj = getSharedPreferences("config", MODE_PRIVATE);

        ConstParm.webAddr = shareObj.getString("webServerAddr", "");
        String webPort = shareObj.getString("webServerPort", "4001");

        ConstParm.conAddr = shareObj.getString("mainServerAddr", "");
        String mainPort = shareObj.getString("mainServerPort", "50001");

        ConstParm.videoAddr = shareObj.getString("videoServerAddr", "");
        String videoPort = shareObj.getString("videoServerPort", "60005");

        String videoDelay = shareObj.getString("videoFrameDelay", "80");
        long videoFormat = shareObj.getLong("videoFrameFormat", 0);

        int deviceType = shareObj.getInt("videoDevice", 0);

        try {
            ConstParm.webPort = Integer.parseInt(webPort);
            ConstParm.conPort = Integer.parseInt(mainPort);
            ConstParm.videoPort = Integer.parseInt(videoPort);
            ConstParm.frameDelay = Integer.parseInt(videoDelay);
            ConstParm.resolutionType = (int) videoFormat;
            ConstParm.videoDeviceType = deviceType;
            Log.e("UserLoginActivity", "deviceType =" + deviceType);

        } catch (Exception ex) {
            new AlertDialog.Builder(UserLoginActivity.this)
                    .setTitle("提示")
                    .setMessage("端口错误")
                    .setPositiveButton("确定", null).show();
        }
    }

    public boolean CheckWebServerAddr(String webIp) {
        boolean ret = true;

        SharedPreferences shareObj = getSharedPreferences("config", MODE_PRIVATE);

        int size = shareObj.getInt("city_size", -1);
        Log.e(null, "***************** =" + webIp);
        Log.e("UserLoginActivity", "city.size =" + size);
        for (int i = 0; i < size; i++) {
            String cityIp = shareObj.getString("city_" + i, null);
            if (webIp.equals(cityIp)) {
                ret = true;
            }

        }
        return ret;
    }

    public ArrayList<String> getCityList(String url) {

        ArrayList<String> list = new ArrayList<String>();
        List<NameValuePair> nvs = new ArrayList<NameValuePair>();
//		nvs.add(new BasicNameValuePair("userCode", userCode));
//		nvs.add(new BasicNameValuePair("password", password));
        HttpPost httpRequst = new HttpPost(url);
        HttpClient httpClient = new DefaultHttpClient();

        //StringBuffer sb = new StringBuffer();
        try {
            // 将参数添加的POST请求中
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(nvs,
                    "utf-8");
            httpRequst.setEntity(uefEntity);

            Log.d("查询", url);
            // 发送请求

            HttpParams params = null;
            params = httpClient.getParams();
            //set timeout
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpResponse res = httpClient.execute(httpRequst);
            HttpEntity entity = res.getEntity();
            // 读取返回值

            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();

            JSONArray jsonArg = new JSONArray(sb.toString());
            Log.e(null, "&&&&&&&&&&&&&&&&&&&&777" + jsonArg.toString());
            // /处理返回值
            if (jsonArg.length() > 0) {

                for (int i = 0; i < jsonArg.length(); i++) {
                    JSONObject json = jsonArg.getJSONObject(i);
                    String strIp = json.getString("IP");
                    Log.e(null, "##########&&&" + jsonArg.toString());
                    list.add(strIp);

                }
            }


        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "无法连接服务器" + e.toString());
            //Toast.makeText(this, text, duration)
//			Toast.makeText(UserLoginActivity.this, "无法连接服务器",
//					Toast.LENGTH_LONG).show();
            return list;
        } catch (Exception e) {
            Log.e(TAG, "error6" + e.toString());
            //Log.e("错误6","sssssssssssss");
        }
        return list;
        //String url = NSURL(string:strUrl)
//        let request = NSMutableURLRequest(URL: url!)
//        request.HTTPMethod = "POST"
//
//        var param = String.localizedStringWithFormat("")
//
//        let postBody = param.dataUsingEncoding(NSUTF8StringEncoding)
//        request.HTTPBody = postBody
//
//        var response:NSURLResponse?
//        var error:NSError?
//        request.timeoutInterval = 20
//        // Sending Synchronous request using NSURLConnection
//
//
//        let data = NSURLConnection.sendSynchronousRequest(request,returningResponse: &response, error:&error)
//        if error != nil
//        {
//            println("getCityList http request failed!")
//            return list
//        }
//        else
//        {
//            if data == nil || data?.length == 0
//            {
//                return list
//            }
//
//            let jsonArray = NSJSONSerialization.JSONObjectWithData(data!, options:
//                NSJSONReadingOptions.MutableContainers, error: nil) as NSArray
//            if error != nil
//            {
//                println("getCityList data serialization failed!")
//                return list
//            }
//
//            if(jsonArray.count > 0)
//            {
//                for var i=0; i < jsonArray.count; i++
//                {
//                    var json: AnyObject = jsonArray[i]
//
//                    var ip : String?
//                    ip = json["IP"] as? String
//
//
//                    if ip != nil
//                    {
//                        list.addObject(ip!)
//                    }
//                }
//            }
//
//        }
//
//        return list;

    }


    /**
     * 登陆验证
     */
    public String getPhoneUser(String userCode, String password, String url, boolean runFlag) {
        List<NameValuePair> nvs = new ArrayList<NameValuePair>();
        nvs.add(new BasicNameValuePair("userCode", userCode));
        nvs.add(new BasicNameValuePair("password", password));
        HttpPost httpRequst = new HttpPost(url);
        HttpClient httpClient = new DefaultHttpClient();
        String result = "0";
        busList.clear();
        //StringBuffer sb = new StringBuffer();
        try {
            // 将参数添加的POST请求中
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(nvs,
                    "utf-8");

            httpRequst.setEntity(uefEntity);
            Log.d("查询", url);
            // 发送请求
            HttpParams params = null;
            params = httpClient.getParams();
            //set timeout
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpResponse res = httpClient.execute(httpRequst);
            HttpEntity entity = res.getEntity();
            // 读取返回值
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					entity.getContent()));
//
//			String line = null;
//			while ((line = reader.readLine()) != null) {
//				sb.append(line + "\n");
//			}
//			reader.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            // /处理返回值
            if (json != null) {
                result = json.getString("message");//.getJSONObject("message").toString();
                if (result.equals("1")) {
                    JSONArray jsonArg = json.getJSONArray("busList");
                    JSONArray jsonIp = json.getJSONArray("busIp");
                    if (jsonArg.length() > 0) {
                        //Log.e(TAG, "jsonAry.length = " + jsonArg.length());
                        for (int i = 0; i < jsonArg.length(); i++) {
                            String bus = jsonArg.getString(i);
                            busList.add(bus);
                        }
                    }
                    if (jsonIp.length() > 0) {
                        //Log.e(TAG, "jsonAry.length = " + jsonArg.length());
                        for (int i = 0; i < jsonIp.length(); i++) {
                            String Ip = jsonIp.getString(i);
                            busIp.add(Ip);
                        }
                    }

                }
            }

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "无法连接服务器" + e.toString());
            //Toast.makeText(this, text, duration)
//
            return result;
        } catch (Exception e) {
            Log.e(TAG, "error5:" + e.toString());
            //Log.e("错误5","pppppppppppppppppppppp");
        }
        return result;
    }

    // 登陆成功 跳转到主页面
    public void goToMain() {
        Intent intent = new Intent(UserLoginActivity.this, BusSelectActivity.class);
//			Bundle bl = new Bundle();
        //	bl.putStringArrayList("chlSel", aa);
//				bl.putInt("busList", busList);
        Bundle bl = new Bundle();
        bl.putStringArrayList("busList", busList);
        bl.putStringArrayList("busIp", busIp);
//			Log.e("UserLoginActivit", "busList= " + busList.toString());
//			Log.e("UserLoginActivit__","busIp= " + busIp.toString());
        intent.putExtras(bl);
        startActivity(intent);// 跳转主页
    }

    // 弹出登陆失败
    public void goToLoginError() {
        new AlertDialog.Builder(UserLoginActivity.this)
                .setTitle("提示").setMessage("用户名或密码错误")
                .setPositiveButton("确定", null).show();
    }

    final class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (UserLoginActivity.pd != null) {
                        UserLoginActivity.pd.dismiss();
                        UserLoginActivity.pd = null;
                    }
                    goToMain();
                    break;
                case 1:
                    if (UserLoginActivity.pd != null) {
                        UserLoginActivity.pd.dismiss();
                        UserLoginActivity.pd = null;
                    }
                    goToLoginError();
                    break;
                case 2:
                    if (UserLoginActivity.pd != null) {
                        UserLoginActivity.pd.dismiss();
                        UserLoginActivity.pd = null;
                    }
                    Toast.makeText(UserLoginActivity.this, "无法连接到服务器！",
                            Toast.LENGTH_LONG).show();
                    //goToMain();
                    break;
                case 5:
                    pd = ProgressDialog.show(UserLoginActivity.this, "提示", "登录中...");
                    time.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (UserLoginActivity.pd != null) {
                                UserLoginActivity.pd.dismiss();
                                UserLoginActivity.pd = null;
                            }
                        }
                    }, 20000);
                    break;
                default:
                    break;
            }
        }
    }
}