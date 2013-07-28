package com.wwj.sb.activity;

import android.os.Bundle;

public class AboutActivity extends SettingActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_about_layout);
		setTopTitle(getResources().getString(R.string.about_title));
	}

}
