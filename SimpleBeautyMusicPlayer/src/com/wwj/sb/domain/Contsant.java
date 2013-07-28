package com.wwj.sb.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.wwj.sb.activity.R;
import com.wwj.sb.utils.CustomDialog;
import com.wwj.sb.utils.CustomDialog.Builder;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * 常量类
 * @author 涙星
 */
public class Contsant {
	/***判断网络是否正常**/
	public static boolean getNetIsAvailable(Context context){
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo=connManager.getActiveNetworkInfo();
		if(networkInfo==null){
			return false;
		}
		return networkInfo.isAvailable();
	}
	/**
	 * 提示消息
	 * */
	public static Toast showMessage(Toast toastMsg, Context context, String msg) {
		if (toastMsg == null) {
			toastMsg = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		} else {
			toastMsg.setText(msg);
		}
		toastMsg.show();
		return toastMsg;
	}
	/**
	 * 删除文件并删除媒体库中数据
	 * */
	public static boolean deleteFile(Context context,String filePath){
		new File(filePath).delete();
		ContentResolver cr=context.getContentResolver();
		int id=-1;
		Cursor cursor=cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media._ID}
		, MediaStore.Audio.Media.DATA+"=?", new String[]{filePath}, null);
		if(cursor.moveToNext()){
			id=cursor.getInt(0);
		}
		cursor.close();
		if(id!=-1){
			return cr.delete(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id), null, null)>0;
		}
		return false;
	}
	
	
    /**
	 * 单个按钮对话框
	 * */
	public static CustomDialog createConfirmDialog(Context context, String text,
			String title, String msg, DialogInterface.OnClickListener listener) {
		Builder builder = new CustomDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton(text, listener);
		return builder.create();
	}
	/**
	 * 获取屏幕的大小0：宽度 1：高度
	 * */
	public static int[] getScreen(Context context) {
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		return new int[] { (int) (outMetrics.density * outMetrics.widthPixels),
				(int) (outMetrics.density * outMetrics.heightPixels) };
	}
	/**
	 * 获取文件的后缀名，返回大写
	 * */
	public static String getSuffix(String str) {
		int i = str.lastIndexOf('.');
		if (i != -1) {
			return str.substring(i + 1).toUpperCase();
		}
		return str;
	}
	/**
	 * 格式化毫秒->00:00
	 * */
	public static String formatSecondTime(int millisecond) {
		if (millisecond == 0) {
			return "00:00";
		}
		millisecond = millisecond / 1000;
		int m = millisecond / 60 % 60;
		int s = millisecond % 60;
		return (m > 9 ? m : "0" + m) + ":" + (s > 9 ? s : "0" + s);
	}

	/**
	 * 格式化文件大小 Byte->MB
	 * */
	public static String formatByteToMB(long l){
		float mb=l/1024f/1024f;
		return String.format("%.2f",mb);
	}
	/**
	 * 根据文件路径获取文件目录
	 * */
	public static String clearFileName(String str) {
		int i = str.lastIndexOf(File.separator);
		if (i != -1) {
			return str.substring(0, i + 1);
		}
		return str;
	}
	/**
	 * 根据文件名获取不带后缀名的文件名
	 * */
	public static String clearSuffix(String str) {
		int i = str.lastIndexOf(".");
		if (i != -1) {
			return str.substring(0, i);
		}
		return str;
	}
	/**
	 * 根据文件路径获取不带后缀名的文件名
	 * */
	public static String clearDirectory(String str) {
		int i = str.lastIndexOf(File.separator);
		if (i != -1) {
			return clearSuffix(str.substring(i + 1, str.length()));
		}
		return str;
	}
	/**
	 * 检查SD卡是否已装载
	 * */
	public static boolean isExistSdCard(){
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	/**
	 * 获得SD目录路径
	 * */
	public static String getSdCardPath(){
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * 判断文件是否存在
	 * */
	public static boolean isExistFile(String file){
		return new File(file).exists();
	}
	/**
	 * 判断目录是否存在，不在则创建
	 * */
	public static void isExistDirectory(String directoryName) {
		File file = new File(directoryName);
		if (!file.exists()) {
			file.mkdirs();
		}
	}
	/**
	 * 修改文件名
	 * */
	public static String renameFileName(String str){
		int i=str.lastIndexOf('.');
		if(i!=-1){
			File file=new File(str);
			file.renameTo(new File(str.substring(0,i)));
			return str.substring(0,i);
		}
		return str;
	}
	/**
	 * 获取音乐列表类别
	 * */
	public static List<HashMap<String, Object>> getListMusicData() {
		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		

		HashMap<String, Object> map2 = new HashMap<String, Object>();
		map2.put("icon", String.valueOf(R.drawable.local_singer));
		map2.put("title", "歌手");
		map2.put("icon2", String.valueOf(R.drawable.playlist_sign));
		data.add(map2);

		HashMap<String, Object> map3 = new HashMap<String, Object>();
		map3.put("icon", String.valueOf(R.drawable.local_album));
		map3.put("title", "专辑");
		map3.put("icon2", String.valueOf(R.drawable.playlist_sign));
		data.add(map3);

		HashMap<String, Object> map4 = new HashMap<String, Object>();
		map4.put("icon", String.valueOf(R.drawable.local_file));
		map4.put("title", "下载的音乐");
		map4.put("icon2", String.valueOf(R.drawable.playlist_sign));
		data.add(map4);


		HashMap<String, Object> map7 = new HashMap<String, Object>();
		map7.put("icon", String.valueOf(R.drawable.lately_player));
		map7.put("title", "最近播放");
		map7.put("icon2", String.valueOf(R.drawable.playlist_sign));
		data.add(map7);
		return data;
	}
	/***
	 * 获取列表下载数据
	 * @return
	 */
	public static List<HashMap<String, Object>> getListDownLoadData() {
		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("icon", String.valueOf(R.drawable.download_icon_down));
		map.put("title", "正在下载");
		map.put("icon2", String.valueOf(R.drawable.playlist_sign));
		data.add(map);

		HashMap<String, Object> map2 = new HashMap<String, Object>();
		map2.put("icon", String.valueOf(R.drawable.download_icon_finish));
		map2.put("title", "下载完成");
		map2.put("icon2", String.valueOf(R.drawable.playlist_sign));
		data.add(map2);
		return data;
	}

}
