package com.sdhy.video.client;
/**
 * ����汾�Զ�����
 * 
 * */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

//import com.sdhy.video.client.R;
     public class UpdateVersionManager {
		private Context mContext;
		//��ʾ��
		private String updateMsg = "���µ�������������ذ�~";
		//���صİ�װ��url
		private String apkUrl = "/HyAndroidApp.apk";
		private Dialog noticeDialog;
		private Dialog downloadDialog;
		 /* ���ذ���װ·�� */
	    private static  String savePath = "/sdcard/updatedemo/";
	    private static  String saveFileName = "";

	    /* ��������֪ͨuiˢ�µ�handler��msg���� */
	    private ProgressBar mProgress;
	    private static final int DOWN_UPDATE = 1;
	    private static final int DOWN_OVER = 2;
	    private int progress;
	    private Thread downLoadThread;
	    private boolean interceptFlag = false;
	    private Handler mHandler = new Handler(){
	    	@Override
			public void handleMessage(Message msg) {
	    		switch (msg.what) {
				case DOWN_UPDATE:
					mProgress.setProgress(progress);
					break;
				case DOWN_OVER:
					
					installApk();
					break;
				default:
					break;
				}
	    	};
	    };
	    
		public UpdateVersionManager(Context context) {
			this.mContext = context;
		}
		
		//�ⲿ�ӿ�����Activity����
		public void checkUpdateInfo(String url,int ver ){
			int oldVer=getVersionCode();
			apkUrl =url+apkUrl;
			 if(ver>oldVer){
				//���°汾��Ҫ����
				showNoticeDialog();
			}
			
		}
		
		
		private void showNoticeDialog(){
			Builder builder = new Builder(mContext);
			builder.setTitle("����汾����");
			builder.setMessage(updateMsg);
			builder.setPositiveButton("����", new OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					showDownloadDialog();			
				}
			});
			builder.setNegativeButton("�Ժ���˵", new OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();				
				}
			});
			noticeDialog = builder.create();
			noticeDialog.show();
		}
		
		private void showDownloadDialog(){
		 	Builder builder = new Builder(mContext);
			builder.setTitle("����汾����");
			
			final LayoutInflater inflater = LayoutInflater.from(mContext);
			View v = inflater.inflate(R.layout.progress, null);
			mProgress = (ProgressBar)v.findViewById(R.id.progress);
			
			builder.setView(v);
			builder.setNegativeButton("ȡ��", new OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					interceptFlag = true;
				}
			});
			downloadDialog = builder.create();
			downloadDialog.show(); 
			
			downloadApk();
		}
		//APK�����߳�
		private Runnable mdownApkRunnable = new Runnable() {	
			@Override
			public void run() {
				try {
					 // �ж�SD���Ƿ���ڣ������Ƿ���ж�дȨ��  
	                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))  
	                { 
	                	 // ��ô洢����·��  
	                    String sdpath = Environment.getExternalStorageDirectory() + "/";  
	                    savePath = sdpath + "download";  
	                    URL url = new URL(apkUrl);
	    				
						HttpURLConnection conn = (HttpURLConnection)url.openConnection();
						conn.connect();
						int length = conn.getContentLength();
						InputStream is = conn.getInputStream();
						
						File file = new File(savePath);
						if(!file.exists()){
							file.mkdir();
						}
						saveFileName=savePath + "/��Ƶ�ͻ���.apk";
						String apkFile = saveFileName;
						File ApkFile = new File(apkFile);
						FileOutputStream fos = new FileOutputStream(ApkFile);
						int count = 0;
						byte buf[] = new byte[1024];
						
						do{   		   		
				    		int numread = is.read(buf);
				    		count += numread;
				    	    progress =(int)(((float)count / length) * 100);
				    	    //���½���
				    	    mHandler.sendEmptyMessage(DOWN_UPDATE);
				    		if(numread <= 0){	
				    			//�������֪ͨ��װ
				    			mHandler.sendEmptyMessage(DOWN_OVER);
				    			break;
				    		}
				    		fos.write(buf,0,numread);
				    	}while(!interceptFlag);//���ȡ����ֹͣ����.
						fos.close();
						is.close();
	                }
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch(IOException e){
					e.printStackTrace();
				}
				
			}
		};
		/** 
		 * ��ȡ����汾�� 
		 *  
		 * @param context 
		 * @return 
		 */  
		private int getVersionCode( )  
		{  
		    int versionCode = 0;  
		    try  
		    {  
		        // ��ȡ����汾�ţ���ӦAndroidManifest.xml��android:versionCode  
		        versionCode = mContext.getPackageManager().getPackageInfo("com.sdhy.video.client", 0).versionCode;  
		    } catch (NameNotFoundException e)  
		    {  
		        e.printStackTrace();  
		    }  
		    return versionCode;  
		}	
		 /**
	     * ����apk
	     * @param url
	     */
		
		private void downloadApk(){
			downLoadThread = new Thread(mdownApkRunnable);
			downLoadThread.start();
		}
		 /**
	     * ��װapk
	     * @param url
	     */
		private void installApk(){
			File apkfile = new File(saveFileName);
	        if (!apkfile.exists()) {
	            return;
	        } 
	        noticeDialog.cancel();
	        Intent i = new Intent(Intent.ACTION_VIEW);
	        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive"); 
	        mContext.startActivity(i);
		
		}
	}
