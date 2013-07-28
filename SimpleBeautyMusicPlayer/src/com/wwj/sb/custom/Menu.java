package com.wwj.sb.custom;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wwj.sb.activity.R;
import com.wwj.sb.adapter.MenuAdapter;
import com.wwj.sb.adapter.MenuAdapter.ItemListener;

/**
 * 自定义菜单
 * @author wwj
 * 
 */
public class Menu {
	private Context context;
	private List<GridView> contents;	// 标签GridView
	private List<List<int[]>> datas;	// 0:图标，1:标题
	private List<TextView> tabs;		// 标签标题
	private int index = 0;				// 显示标签索引
	private LinearLayout layout;
	private PopupWindow popwindow;		
	
	public Menu(Context context) {
		this.context = context;
		tabs = new ArrayList<TextView>();
		contents = new ArrayList<GridView>();
		datas = new ArrayList<List<int[]>>();
	
		layout = new LinearLayout(context);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setGravity(Gravity.CLIP_HORIZONTAL);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setBackgroundResource(R.color.menu_bg_focus);
	}
	
	public void addItem(String oftenuse, List<int[]> data,
			MenuAdapter.ItemListener listener) {
		tabs.add(createTextView(oftenuse));		//添加标题
		datas.add(data);						//
		contents.add(createGridView(data, listener));//
	}

	
	private TextView createTextView(String title) {
		TextView textView = new TextView(context);
		textView.setText(title);
		textView.setGravity(Gravity.CENTER);
		textView.setPadding(0, 10, 0, 10);
		textView.setTextColor(Color.WHITE);
		textView.setTextSize(15);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		textView.setLayoutParams(params);
		textView.setOnClickListener(clickListener);
		return textView;
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int i = Integer.valueOf(v.getTag().toString());
			if(index != i) {
				tabs.get(index).setBackgroundResource(R.color.menu_bg_normal);
				tabs.get(i).setBackgroundResource(0);
				contents.get(index).setVisibility(View.GONE);
				contents.get(i).setVisibility(View.VISIBLE);
				index = i;
			}
		}
	};
	
	private GridView createGridView(List<int[]> data, final MenuAdapter.ItemListener listener) {
		GridView gridView = new GridView(context);
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		gridView.setLayoutParams(params);
		gridView.setNumColumns(4);
		gridView.setHorizontalSpacing(15);
		gridView.setVerticalSpacing(15);
		gridView.setPadding(15, 15, 15, 15);
		gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		gridView.setGravity(Gravity.CENTER);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				listener.onClickListener(position, view);
			}
		});
		gridView.setAdapter(new MenuAdapter(context, data).setmItemListener(listener));
		return gridView;
	}

	
	public void setDefaultTab(int index) {
		this.index = index;
	}
	
	
	public PopupWindow create() {
		popwindow = new PopupWindow(context);
		LinearLayout tab_layout = new LinearLayout(context);
		tab_layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		tab_layout.setOrientation(LinearLayout.HORIZONTAL);
		
		for(int i = 0, len = tabs.size(); i < len; i++) {
			final TextView textView = tabs.get(i);
			textView.setTag(i);
			final GridView gridView = contents.get(i);
			if(index != i) {
				textView.setBackgroundResource(R.color.menu_bg_normal);
				gridView.setVisibility(View.GONE);
			}
			tab_layout.addView(textView);
			if(i == 0) {
				layout.addView(tab_layout);
			}
			layout.addView(gridView);
		}
		
		popwindow.setWidth(LayoutParams.MATCH_PARENT);
		popwindow.setHeight(LayoutParams.WRAP_CONTENT);
		popwindow.setFocusable(true);
		popwindow.setAnimationStyle(R.style.popwindow_anim_style);
		ColorDrawable dw = new ColorDrawable(-00000);
		popwindow.setBackgroundDrawable(dw);
		popwindow.setContentView(layout);
		return popwindow;
	}
	
	/**
	 * 判断popwindow是否正在显示
	 * @return
	 */
	public boolean isShowing() {
		return popwindow.isShowing();
	}
	
	
	public void showAtLocation(View parent, int gravity, int x, int y) {
		popwindow.showAtLocation(parent, gravity, x, y);
	}
	
	
	public void cancel() {
		popwindow.dismiss();
	}
	
}
