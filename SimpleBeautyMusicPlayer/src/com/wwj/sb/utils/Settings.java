package com.wwj.sb.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.wwj.sb.activity.R;

public class Settings {
	/**
	 * SD卡下载歌曲目录
	 */
	public static final String DOWNLOAD_MUSIC_DIRECTORY = "/Music/download_music/";
	/**
	 * SD卡下载歌词目录
	 */
	public static final String DOWNLOAD_LYRIC_DIRECTORY = "/Music/download_lyric/";
	/**
	 * SD卡下载专辑图片目录
	 * */
	public static final String DOWNLOAD_ALBUM_DIRECTORY="/Music/download_album/";
	/**
	 * SD卡下载歌手图片目录
	 * */
	public static final String DOWNLOAD_ARTIST_DIRECTORY="/Music/download_artist/";
	/**
	 * 系统设置的保存的文件名
	 */
	public static final String PREFERENCE_NAME = "com.wwj.music.settings";
	public static final String KEY_SKINID = "skin_id";
	private SharedPreferences settingPreferences;
	public static final String KEY_AUTO_SLEEP = "sleep";	//定时关闭时间
	public static final String KEY_BRIGHTNESS = "brightness";	//屏幕模式->1:正常模式，0:夜间模式
	public static final float KEY_DARKNESS = 0.1f;	//夜间模式值level
	
	
	/**
	 * 皮肤资源ID数组
	 */
	public static final int[] SKIN_RESOURCES = {
		R.drawable.main_bg01, R.drawable.main_bg02,
		R.drawable.main_bg03, R.drawable.main_bg04,
		R.drawable.main_bg05, R.drawable.main_bg06
	};
	
	public Settings(Context context, boolean isWrite) {
		settingPreferences = context.getSharedPreferences(PREFERENCE_NAME,
				isWrite ? Context.MODE_WORLD_READABLE
						: Context.MODE_WORLD_WRITEABLE);
	}
	
	/**
	 * 获取设置数据
	 * @param key
	 * @return
	 */
	public String getValue(String key) {
		return settingPreferences.getString(key, null);
	}
	/**
	 * 获取皮肤资源ID
	 * @return
	 */
	public int getCurrentSkinResId() {
		int skinIndex = settingPreferences.getInt(KEY_SKINID, 0);
		if(skinIndex >= SKIN_RESOURCES.length) {
			skinIndex = 0;
		}
		return SKIN_RESOURCES[skinIndex];
	}
	
	/**
	 * 获取皮肤Id
	 * @return
	 */
	public int getCurrentSkinId() {
		int skinIndex = settingPreferences.getInt(KEY_SKINID, 0);
		if(skinIndex >= SKIN_RESOURCES.hashCode()) {
			skinIndex = 0;
		}
		return skinIndex;
	}
	
	/**
	 * 设置皮肤资源ID
	 * @param skinIndex
	 */
	public void setCurrentSkinResId(int skinIndex) {
		Editor it = settingPreferences.edit();
		it.putInt(KEY_SKINID, skinIndex);
		it.commit();
	}
	
	/**
	 * 设置键值
	 * @param key
	 * @param value
	 */
	public void setValue(String key, String value) {
		Editor it = settingPreferences.edit();
		it.putString(key, value);
		it.commit();
	}
}
