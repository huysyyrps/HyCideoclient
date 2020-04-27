package com.component;

import java.io.File;
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

public class Util {
	public static File cameraFile;

	/**
	 * ��ȡ��Ļ�߶ȺͿ��
	 * 
	 * @param mContext
	 * @return int[�ߣ���]
	 */
	public static int[] getScreen(Context mContext) {
		DisplayMetrics dm = new DisplayMetrics();
		// ȡ�ô�������
		// getWindowManager().getDefaultDisplay().getMetrics(dm);
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);

		// ���ڵĿ��
		int screenWidth = dm.widthPixels;

		// ���ڸ߶�
		int screenHeight = dm.heightPixels;
		int screen[] = { screenHeight, screenWidth };
		return screen;

	}

	public static int getSecreenW(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		dm = null;
		return width;
	}

	/**
	 * 
	 * @Title: dip2px @Description: TODO(dpתpx) @param @param
	 *         context @param @param dpValue @param @return �趨�ļ� @return int
	 *         �������� @throws
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}







	// �����Ƿ���SD��
	public static boolean GetSDState() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @Title: deleteFile @Description: TODO(ɾ���ļ�) @param @param file
	 *         �趨�ļ� @return void �������� @throws
	 */
	public static void deleteFile(File file) {
		if (file.exists()) { // �ж��ļ��Ƿ����
			if (file.isFile()) { // �ж��Ƿ����ļ�
				file.delete(); // delete()���� ��Ӧ��֪�� ��ɾ������˼;
			} else if (file.isDirectory()) { // �����������һ��Ŀ¼
				File files[] = file.listFiles(); // ����Ŀ¼�����е��ļ� files[];
				for (int i = 0; i < files.length; i++) { // ����Ŀ¼�����е��ļ�
					deleteFile(files[i]); // ��ÿ���ļ� ������������е���
				}
			}
			file.delete();
		} else {

		}
	}

	/**
	 * ��ʾtoast
	 * 
	 * @param context
	 *            ��ǰactivity
	 * @param content
	 *            ��ʾ������
	 */

	public static void showToast(Context mContext, String content) {
		Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
	}

	/**
	 * actionbar�߶�
	 * 
	 * @param context
	 * @return
	 */
	public static int getBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 38;// Ĭ��Ϊ38��ò�ƴ󲿷���������

		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = context.getResources().getDimensionPixelSize(x);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return sbar;
	}


	/**
	 * ��ȡʱ���
	 * @return
	 */
	public static long getSeq() {
		return System.currentTimeMillis();
	}

	/**
	 * �������״̬
	 */
	public static boolean isNetUseable(Context context) {
		boolean have=false;
		ConnectivityManager cwjManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo info = cwjManager.getActiveNetworkInfo(); 
		if (info != null && info.isAvailable()){ 
			have=true;
		} 
		else
		{
		
			showToast(context, "����������");
		}
		return have;
	}

}
