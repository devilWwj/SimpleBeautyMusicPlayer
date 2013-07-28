package com.wwj.sb.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.wwj.sb.adapter.ImageAdapter;
import com.wwj.sb.utils.Settings;

public class SkinSettingActivity extends SettingActivity{
	private GridView gv_skin;			//网格视图
	private ImageAdapter adapter;		//图片适配器
	private Settings mSetting;			//设置引用
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.skinsetting_layout);
		
		resultCode = 2;
		setBackButton();
		setTopTitle(getResources().getString(R.string.skin_settings));
		
		mSetting = new Settings(this, true);
		
		
		adapter = new ImageAdapter(this, mSetting.getCurrentSkinId());
		gv_skin = (GridView) findViewById(R.id.gv_skin);
		gv_skin.setAdapter(adapter);
		gv_skin.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//更新GridView
				adapter.setCurrentId(position);
				//更新背景图片
				SkinSettingActivity.this.getWindow().setBackgroundDrawableResource(Settings.SKIN_RESOURCES[position]);
				//保存数据
				mSetting.setCurrentSkinResId(position);
			}
		});
	}
	
}
