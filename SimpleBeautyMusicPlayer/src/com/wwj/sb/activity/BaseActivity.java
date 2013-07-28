package com.wwj.sb.activity;


import com.wwj.sb.adapter.MenuAdapter;
import com.wwj.sb.utils.Settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class BaseActivity extends Activity {
	public static final String BROADCASTRECEVIER_ACTON="com.wwj.music.commonrecevier";
	private CommonRecevier commonRecevier;
	public float brightnesslevel=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // 设置皮肤背景
		Settings setting = new Settings(this, false);
		String brightness=setting.getValue(Settings.KEY_BRIGHTNESS);
		android.view.WindowManager.LayoutParams attributes = getWindow().getAttributes();
		brightnesslevel=attributes.screenBrightness;
		if(brightness!=null&&brightness.equals("0")){//夜间模式
			attributes.screenBrightness=Settings.KEY_DARKNESS;	
			getWindow().setAttributes(attributes);
		}
		this.getWindow().setBackgroundDrawableResource(
				setting.getCurrentSkinResId());
		commonRecevier=new CommonRecevier();
	}
	/**
	 * 设置正常模式和夜间模式
	 * */
	public void setBrightness(View v) {
       Settings setting = new Settings(this, true);
		String brightness=setting.getValue(Settings.KEY_BRIGHTNESS);
		MenuAdapter.ViewHolder viewHolder=(MenuAdapter.ViewHolder)v.getTag();
	   WindowManager.LayoutParams attributes = getWindow().getAttributes();
		if(brightness!=null&&brightness.equals("0")){//夜间模式
			viewHolder.tv_title.setText(getResources().getString(R.string.darkness_title));
			viewHolder.btn_menu.setBackgroundResource(R.drawable.btn_menu_darkness);
			attributes.screenBrightness=brightnesslevel;
			setting.setValue(Settings.KEY_BRIGHTNESS, "1");
			getWindow().setAttributes(attributes);
		}else{//正常模式
			viewHolder.tv_title.setText(getResources().getString(R.string.brightness_title));
			viewHolder.btn_menu.setBackgroundResource(R.drawable.btn_menu_brightness);
			attributes.screenBrightness=Settings.KEY_DARKNESS;	
			setting.setValue(Settings.KEY_BRIGHTNESS, "0");
			getWindow().setAttributes(attributes);
		}
	
	} 
	


	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(commonRecevier, new IntentFilter(BROADCASTRECEVIER_ACTON));
	}



	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(commonRecevier);
	}



	public class CommonRecevier extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	}
}
