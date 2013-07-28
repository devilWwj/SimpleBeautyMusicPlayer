package com.wwj.sb.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.method.DigitsKeyListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wwj.sb.adapter.MenuAdapter;
import com.wwj.sb.adapter.MusicListAdapter;
import com.wwj.sb.custom.Menu;
import com.wwj.sb.domain.AppConstant;
import com.wwj.sb.domain.Contsant;
import com.wwj.sb.domain.Mp3Info;
import com.wwj.sb.service.PlayerService;
import com.wwj.sb.utils.ConstantUtil;
import com.wwj.sb.utils.CustomDialog;
import com.wwj.sb.utils.MediaUtil;
import com.wwj.sb.utils.Settings;

/**
 * 2013/5/7 简、美音乐播放器
 * 
 * @author wwj
 * 
 */
public class HomeActivity extends BaseActivity {
	private ListView mMusiclist; // 音乐列表
	private List<Mp3Info> mp3Infos = null;
	// private SimpleAdapter mAdapter; // 简单适配器
	MusicListAdapter listAdapter; // 改为自定义列表适配器
	private Button previousBtn; // 上一首
	private Button repeatBtn; // 重复（单曲循环、全部循环）
	private Button playBtn; // 播放（播放、暂停）
	private Button shuffleBtn; // 随机播放
	private Button nextBtn; // 下一首
	private TextView musicTitle;// 歌曲标题
	private TextView musicDuration; // 歌曲时间
	private Button musicPlaying; // 歌曲专辑
	private ImageView musicAlbum; // 专辑封面

	private int repeatState; // 循环标识
	private final int isCurrentRepeat = 1; // 单曲循环
	private final int isAllRepeat = 2; // 全部循环
	private final int isNoneRepeat = 3; // 无重复播放
	private boolean isFirstTime = true;
	private boolean isPlaying; // 正在播放
	private boolean isPause; // 暂停
	private boolean isNoneShuffle = true; // 顺序播放
	private boolean isShuffle = false; // 随机播放

	private int listPosition = 0; // 标识列表位置
	private HomeReceiver homeReceiver; // 自定义的广播接收器
	// 一系列动作
	public static final String UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION"; // 更新动作
	public static final String CTL_ACTION = "com.wwj.action.CTL_ACTION"; // 控制动作
	public static final String MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT"; // 当前音乐改变动作
	public static final String MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION"; // 音乐时长改变动作
	public static final String REPEAT_ACTION = "com.wwj.action.REPEAT_ACTION"; // 音乐重复改变动作
	public static final String SHUFFLE_ACTION = "com.wwj.action.SHUFFLE_ACTION"; // 音乐随机播放动作

	private int currentTime; // 当前时间
	private int duration; // 时长

	private Menu xmenu; // 自定义菜单
	private Toast toast;

	private TextView timers;// 显示倒计时的文字
	private Timers timer;// 倒计时内部对象
	private int c;// c确定一个标识

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_activity_layout);

		mMusiclist = (ListView) findViewById(R.id.music_list);
		mMusiclist.setOnItemClickListener(new MusicListItemClickListener());
		mMusiclist
				.setOnCreateContextMenuListener(new MusicListItemContextMenuListener());
		mp3Infos = MediaUtil.getMp3Infos(HomeActivity.this); // 获取歌曲对象集合
		// setListAdpter(MediaUtil.getMusicMaps(mp3Infos)); //显示歌曲列表
		listAdapter = new MusicListAdapter(this, mp3Infos);
		mMusiclist.setAdapter(listAdapter);
		findViewById(); // 找到界面上的每一个控件
		setViewOnclickListener(); // 为一些控件设置监听器
		repeatState = isNoneRepeat; // 初始状态为无重复播放状态

		homeReceiver = new HomeReceiver();
		// 创建IntentFilter
		IntentFilter filter = new IntentFilter();
		// 指定BroadcastReceiver监听的Action
		filter.addAction(UPDATE_ACTION);
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		filter.addAction(REPEAT_ACTION);
		filter.addAction(SHUFFLE_ACTION);
		// 注册BroadcastReceiver
		registerReceiver(homeReceiver, filter);

		LoadMenu();

	}

	private void LoadMenu() {
		xmenu = new Menu(this);
		List<int[]> data1 = new ArrayList<int[]>();
		data1.add(new int[] { R.drawable.btn_menu_skin, R.string.skin_settings });
		data1.add(new int[] { R.drawable.btn_menu_exit, R.string.menu_exit_txt });

		xmenu.addItem("常用", data1, new MenuAdapter.ItemListener() {

			@Override
			public void onClickListener(int position, View view) {
				xmenu.cancel();
				if (position == 0) {
					Intent intent = new Intent(HomeActivity.this,
							SkinSettingActivity.class);
					startActivityForResult(intent, 2);
				} else if (position == 1) {
					exit();
				}
			}
		});
		List<int[]> data2 = new ArrayList<int[]>();
		data2.add(new int[] { R.drawable.btn_menu_setting,
				R.string.menu_settings });
		data2.add(new int[] { R.drawable.btn_menu_sleep, R.string.menu_time_txt});
		Settings setting = new Settings(this, false);
		String brightness = setting.getValue(Settings.KEY_BRIGHTNESS);
		if (brightness != null && brightness.equals("0")) { // 夜间模式
			data2.add(new int[] { R.drawable.btn_menu_brightness,
					R.string.brightness_title });
		} else {
			data2.add(new int[] { R.drawable.btn_menu_darkness,
					R.string.darkness_title });
		}
		xmenu.addItem("工具", data2, new MenuAdapter.ItemListener() {

			@Override
			public void onClickListener(int position, View view) {
				xmenu.cancel();
				if (position == 0) {

				} else if (position == 1) {
					Sleep();
				} else if (position == 2) {
					setBrightness(view);
				}
			}

		});
		List<int[]> data3 = new ArrayList<int[]>();
		data3.add(new int[] { R.drawable.btn_menu_about, R.string.about_title });
		xmenu.addItem("帮助", data3, new MenuAdapter.ItemListener() {
			@Override
			public void onClickListener(int position, View view) {
				xmenu.cancel();
				Intent intent = new Intent(HomeActivity.this,AboutActivity.class);
				startActivity(intent);
			}
		});
		xmenu.create();		//创建菜单
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		menu.add("menu");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, android.view.Menu menu) {
		// 菜单在哪里显示。参数1是该布局总的ID，第二个位置，第三，四个是XY坐标
		xmenu.showAtLocation(findViewById(R.id.homeRLLayout), Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 0, 0);
		// 如果返回true的话就会显示系统自带的菜单，反之返回false的话就是显示自己写的
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && requestCode == 1) {
			Settings setting = new Settings(this, false);
			this.getWindow().setBackgroundDrawableResource(
					setting.getCurrentSkinResId());
		}
	}

	/**
	 * 退出程序
	 */
	private void exit() {
		Intent intent = new Intent(HomeActivity.this, PlayerService.class);
		stopService(intent);
		finish();
	}

	/**
	 * 休眠方法
	 */
   private void Sleep(){
	   final EditText edtext = new EditText(this);
	   edtext.setText("5");//设置初始值
		edtext.setKeyListener(new DigitsKeyListener(false, true));
		edtext.setGravity(Gravity.CENTER_HORIZONTAL);//设置摆设位置
		edtext.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//字体类型
		edtext.setTextColor(Color.BLUE);//字体颜色
		edtext.setSelection(edtext.length());//设置选择位置
		edtext.selectAll();//全部选择
	    new CustomDialog.Builder(HomeActivity.this).setTitle("请输入时间").setView(edtext).setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				dialog.cancel();
				/**如果输入小于2或者等于0会告知用户**/
				if (edtext.length() <= 2 && edtext.length() != 0) {
					if (".".equals(edtext.getText().toString())) {
						toast = Contsant.showMessage(toast,HomeActivity.this, "输入错误，你至少输入两位数字.");
					} else {
						final String time = edtext.getText().toString();
						long Money = Integer.parseInt(time);
						long cX = Money * 60000;
						timer= new Timers(cX, 1000);
					    timer.start();//倒计时开始
						toast = Contsant.showMessage(toast,HomeActivity.this, "休眠模式启动!于" + String.valueOf(time)+ "分钟后关闭程序!");
						timers.setVisibility(View.INVISIBLE);
						timers.setVisibility(View.VISIBLE);
						timers.setText(String.valueOf(time));
					}

				} else {
					Toast.makeText(HomeActivity.this, "请输入几分钟",Toast.LENGTH_SHORT).show();
				}
				
			}
		}).setNegativeButton(R.string.cancel, null).show();
		
}

	private class Timers extends CountDownTimer {

		public Timers(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			timers.setText("" + millisUntilFinished / 1000 / 60 + ":"
					+ millisUntilFinished / 1000 % 60);
			// 加入这个数大于9说明就是2位数了，可以直接输入。假如小于等于9就是1位数。所以前面加一个0
			String abc = (millisUntilFinished / 1000 / 60) > 9 ? (millisUntilFinished / 1000 / 60)
					+ ""
					: "0" + (millisUntilFinished / 1000 / 60);
			String b = (millisUntilFinished / 1000 % 60) > 9 ? (millisUntilFinished / 1000 % 60)
					+ ""
					: "0" + (millisUntilFinished / 1000 % 60);
			timers.setText(abc + ":" + b);
			timers.setVisibility(View.GONE);
		}

		@Override
		public void onFinish() {
			if (c == 0) {
				exit();
				finish();
				onDestroy();
			} else {
				finish();
				onDestroy();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}
	}

	/**
	 * 从界面上根据id获取按钮
	 */
	private void findViewById() {
		previousBtn = (Button) findViewById(R.id.previous_music);
		repeatBtn = (Button) findViewById(R.id.repeat_music);
		playBtn = (Button) findViewById(R.id.play_music);
		shuffleBtn = (Button) findViewById(R.id.shuffle_music);
		nextBtn = (Button) findViewById(R.id.next_music);
		musicTitle = (TextView) findViewById(R.id.music_title);
		musicDuration = (TextView) findViewById(R.id.music_duration);
		musicPlaying = (Button) findViewById(R.id.playing);
		musicAlbum = (ImageView) findViewById(R.id.music_album);
	}

	/**
	 * 给每一个按钮设置监听器
	 */
	private void setViewOnclickListener() {
		ViewOnClickListener viewOnClickListener = new ViewOnClickListener();
		previousBtn.setOnClickListener(viewOnClickListener);
		repeatBtn.setOnClickListener(viewOnClickListener);
		playBtn.setOnClickListener(viewOnClickListener);
		shuffleBtn.setOnClickListener(viewOnClickListener);
		nextBtn.setOnClickListener(viewOnClickListener);
		musicPlaying.setOnClickListener(viewOnClickListener);
	}

	/**
	 * 控件的监听器
	 * 
	 * @author Administrator
	 * 
	 */
	private class ViewOnClickListener implements OnClickListener {
		Intent intent = new Intent();

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.previous_music: // 上一首
				playBtn.setBackgroundResource(R.drawable.play_selector);
				isFirstTime = false;
				isPlaying = true;
				isPause = false;
				previous();
				break;
			case R.id.repeat_music: // 重复播放
				if (repeatState == isNoneRepeat) {
					repeat_one();
					shuffleBtn.setClickable(false);
					repeatState = isCurrentRepeat;
				} else if (repeatState == isCurrentRepeat) {
					repeat_all();
					shuffleBtn.setClickable(false);
					repeatState = isAllRepeat;
				} else if (repeatState == isAllRepeat) {
					repeat_none();
					shuffleBtn.setClickable(true);
					repeatState = isNoneRepeat;
				}
				switch (repeatState) {
				case isCurrentRepeat: // 单曲循环
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_current_selector);
					Toast.makeText(HomeActivity.this, R.string.repeat_current,
							Toast.LENGTH_SHORT).show();
					break;
				case isAllRepeat: // 全部循环
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_all_selector);
					Toast.makeText(HomeActivity.this, R.string.repeat_all,
							Toast.LENGTH_SHORT).show();
					break;
				case isNoneRepeat: // 无重复
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_none_selector);
					Toast.makeText(HomeActivity.this, R.string.repeat_none,
							Toast.LENGTH_SHORT).show();
					break;
				}

				break;
			case R.id.play_music: // 播放音乐
				if (isFirstTime) {
					play();
					isFirstTime = false;
					isPlaying = true;
					isPause = false;
				} else {
					if (isPlaying) {
						playBtn.setBackgroundResource(R.drawable.pause_selector);
						intent.setAction("com.wwj.media.MUSIC_SERVICE");
						intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
						startService(intent);
						isPlaying = false;
						isPause = true;

					} else if (isPause) {
						playBtn.setBackgroundResource(R.drawable.play_selector);
						intent.setAction("com.wwj.media.MUSIC_SERVICE");
						intent.putExtra("MSG",
								AppConstant.PlayerMsg.CONTINUE_MSG);
						startService(intent);
						isPause = false;
						isPlaying = true;
					}
				}
				break;
			case R.id.shuffle_music: // 随机播放
				if (isNoneShuffle) {
					shuffleBtn
							.setBackgroundResource(R.drawable.shuffle_selector);
					Toast.makeText(HomeActivity.this, R.string.shuffle,
							Toast.LENGTH_SHORT).show();
					isNoneShuffle = false;
					isShuffle = true;
					shuffleMusic();
					repeatBtn.setClickable(false);
				} else if (isShuffle) {
					shuffleBtn
							.setBackgroundResource(R.drawable.shuffle_none_selector);
					Toast.makeText(HomeActivity.this, R.string.shuffle_none,
							Toast.LENGTH_SHORT).show();
					isShuffle = false;
					isNoneShuffle = true;
					repeatBtn.setClickable(true);
				}
				break;
			case R.id.next_music: // 下一首
				playBtn.setBackgroundResource(R.drawable.play_selector);
				isFirstTime = false;
				isPlaying = true;
				isPause = false;
				next();
				break;
			case R.id.playing: // 正在播放
				Mp3Info mp3Info = mp3Infos.get(listPosition);
				Intent intent = new Intent(HomeActivity.this,
						PlayerActivity.class);
				intent.putExtra("title", mp3Info.getTitle());
				intent.putExtra("url", mp3Info.getUrl());
				intent.putExtra("artist", mp3Info.getArtist());
				intent.putExtra("listPosition", listPosition);
				intent.putExtra("currentTime", currentTime);
				intent.putExtra("duration", duration);
				intent.putExtra("MSG", AppConstant.PlayerMsg.PLAYING_MSG);
				startActivity(intent);
				break;
			}
		}
	}

	/**
	 * 列表点击监听器
	 * 
	 * @author wwj
	 * 
	 */
	private class MusicListItemClickListener implements OnItemClickListener {
		/**
		 * 点击列表播放音乐
		 */
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			listPosition = position; // 获取列表点击的位置
			playMusic(listPosition); // 播放音乐
		}

	}

	/**
	 * 上下文菜单显示监听器
	 * 
	 * @author Administrator
	 * 
	 */
	public class MusicListItemContextMenuListener implements
			OnCreateContextMenuListener {

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
			vibrator.vibrate(50); // 长按振动
			musicListItemDialog(); // 长按后弹出的对话框
			final AdapterView.AdapterContextMenuInfo menuInfo2 = (AdapterView.AdapterContextMenuInfo) menuInfo;
			listPosition = menuInfo2.position; // 点击列表的位置
		}

	}

	/**
	 * 填充列表
	 * 
	 * @param mp3Infos
	 */
	// public void setListAdpter(List<HashMap<String, String>> mp3list) {
	// mAdapter = new SimpleAdapter(this, mp3list,
	// R.layout.music_list_item_layout, new String[] {"title",
	// "Artist", "duration" }, new int[] {R.id.music_title,
	// R.id.music_Artist, R.id.music_duration });
	// // mMusiclist.setAdapter(mAdapter);
	//
	// }

	/**
	 * 下一首歌曲
	 */
	public void next() {
		listPosition = listPosition + 1;
		if (listPosition <= mp3Infos.size() - 1) {
			Mp3Info mp3Info = mp3Infos.get(listPosition);
			musicTitle.setText(mp3Info.getTitle());
			Intent intent = new Intent();
			intent.setAction("com.wwj.media.MUSIC_SERVICE");
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("MSG", AppConstant.PlayerMsg.NEXT_MSG);
			startService(intent);
		} else {
			listPosition = mp3Infos.size() - 1;
			Toast.makeText(HomeActivity.this, "没有下一首了", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * 上一首歌曲
	 */
	public void previous() {
		listPosition = listPosition - 1;
		if (listPosition >= 0) {
			Mp3Info mp3Info = mp3Infos.get(listPosition);
			musicTitle.setText(mp3Info.getTitle());
			Intent intent = new Intent();
			intent.setAction("com.wwj.media.MUSIC_SERVICE");
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("MSG", AppConstant.PlayerMsg.PRIVIOUS_MSG);
			startService(intent);
		} else {
			listPosition = 0;
			Toast.makeText(HomeActivity.this, "没有上一首了", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * 播放音乐
	 */
	public void play() {
		playBtn.setBackgroundResource(R.drawable.play_selector);
		Mp3Info mp3Info = mp3Infos.get(listPosition);
		musicTitle.setText(mp3Info.getTitle());
		Intent intent = new Intent();
		intent.setAction("com.wwj.media.MUSIC_SERVICE");
		intent.putExtra("listPosition", 0);
		intent.putExtra("url", mp3Info.getUrl());
		intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
		startService(intent);
	}

	/**
	 * 单曲循环
	 */
	public void repeat_one() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 1);
		sendBroadcast(intent);
	}

	/**
	 * 全部循环
	 */
	public void repeat_all() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 2);
		sendBroadcast(intent);
	}

	/**
	 * 顺序播放
	 */
	public void repeat_none() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 3);
		sendBroadcast(intent);
	}

	/**
	 * 随机播放
	 */
	public void shuffleMusic() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 4);
		sendBroadcast(intent);
	}

	/**
	 * 自定义对话框
	 */
	public void musicListItemDialog() {
		String[] menuItems = new String[] { "播放音乐", "设为铃声", "查看详情" };
		ListView menuList = new ListView(HomeActivity.this);
		menuList.setCacheColorHint(Color.TRANSPARENT);
		menuList.setDividerHeight(1);
		menuList.setAdapter(new ArrayAdapter<String>(HomeActivity.this,
				R.layout.context_dialog_layout, R.id.dialogText, menuItems));
		menuList.setLayoutParams(new LayoutParams(ConstantUtil
				.getScreen(HomeActivity.this)[0] / 2, LayoutParams.WRAP_CONTENT));

		final CustomDialog customDialog = new CustomDialog.Builder(
				HomeActivity.this).setTitle(R.string.operation)
				.setView(menuList).create();
		customDialog.show();

		menuList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				customDialog.cancel();
				customDialog.dismiss();
				if (position == 0) {
					playMusic(listPosition);
				} else if (position == 1) {
					setRing();
				} else if (position == 2) {
					showMusicInfo(listPosition);
				}
			}

		});
	}

	/**
	 * 显示音乐详细信息
	 * 
	 * @param position
	 */
	private void showMusicInfo(int position) {
		Mp3Info mp3Info = mp3Infos.get(position);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.music_info_layout, null);
		((TextView) view.findViewById(R.id.tv_song_title)).setText(mp3Info
				.getTitle());
		((TextView) view.findViewById(R.id.tv_song_artist)).setText(mp3Info
				.getArtist());
		((TextView) view.findViewById(R.id.tv_song_album)).setText(mp3Info
				.getAlbum());
		((TextView) view.findViewById(R.id.tv_song_filepath)).setText(mp3Info
				.getUrl());
		((TextView) view.findViewById(R.id.tv_song_duration)).setText(MediaUtil
				.formatTime(mp3Info.getDuration()));
		((TextView) view.findViewById(R.id.tv_song_format)).setText(Contsant
				.getSuffix(mp3Info.getDisplayName()));
		((TextView) view.findViewById(R.id.tv_song_size)).setText(Contsant
				.formatByteToMB(mp3Info.getSize()) + "MB");
		new CustomDialog.Builder(HomeActivity.this).setTitle("歌曲详细信息:")
				.setNeutralButton("确定", null).setView(view).create().show();
	}

	/**
	 * 设置铃声
	 */
	protected void setRing() {
		RadioGroup rg_ring = new RadioGroup(HomeActivity.this);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		rg_ring.setLayoutParams(params);
		// 第一个单选按钮，来电铃声
		final RadioButton rbtn_ringtones = new RadioButton(HomeActivity.this);
		rbtn_ringtones.setText("来电铃声");
		rg_ring.addView(rbtn_ringtones, params);
		// 第二个单选按钮，闹铃铃声
		final RadioButton rbtn_alarms = new RadioButton(HomeActivity.this);
		rbtn_alarms.setText("闹铃铃声");
		rg_ring.addView(rbtn_alarms, params);
		// 第三个单选按钮，通知铃声
		final RadioButton rbtn_notifications = new RadioButton(
				HomeActivity.this);
		rbtn_notifications.setText("通知铃声");
		rg_ring.addView(rbtn_notifications, params);
		new CustomDialog.Builder(HomeActivity.this).setTitle("设置铃声")
				.setView(rg_ring)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						dialog.dismiss();
						if (rbtn_ringtones.isChecked()) {
							try {
								// 设置来电铃声
								setRingtone(listPosition);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (rbtn_alarms.isChecked()) {
							try {
								// 设置闹铃
								setAlarm(listPosition);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (rbtn_notifications.isChecked()) {
							try {
								// 设置通知铃声
								setNotifaction(listPosition);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 设置提示音
	 * 
	 * @param position
	 */
	protected void setNotifaction(int position) {
		Mp3Info mp3Info = mp3Infos.get(position);
		File sdfile = new File(mp3Info.getUrl().substring(4));
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, false);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);

		Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
				.getAbsolutePath());
		Uri newUri = this.getContentResolver().insert(uri, values);
		RingtoneManager.setActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_NOTIFICATION, newUri);
		Toast.makeText(getApplicationContext(), "设置通知铃声成功！", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 设置闹铃
	 * 
	 * @param position
	 */
	protected void setAlarm(int position) {
		Mp3Info mp3Info = mp3Infos.get(position);
		File sdfile = new File(mp3Info.getUrl().substring(4));
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, false);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);

		Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
				.getAbsolutePath());
		Uri newUri = this.getContentResolver().insert(uri, values);
		RingtoneManager.setActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_ALARM, newUri);
		Toast.makeText(getApplicationContext(), "设置闹钟铃声成功！", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 设置来电铃声
	 * 
	 * @param position
	 */
	protected void setRingtone(int position) {
		Mp3Info mp3Info = mp3Infos.get(position);
		File sdfile = new File(mp3Info.getUrl().substring(4));
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, true);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);

		Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
				.getAbsolutePath());
		Uri newUri = this.getContentResolver().insert(uri, values);
		RingtoneManager.setActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_RINGTONE, newUri);
		Toast.makeText(getApplicationContext(), "设置来电铃声成功！", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 此方法通过传递列表点击位置来获取mp3Info对象
	 * 
	 * @param listPosition
	 */
	public void playMusic(int listPosition) {
		if (mp3Infos != null) {
			Mp3Info mp3Info = mp3Infos.get(listPosition);
			musicTitle.setText(mp3Info.getTitle()); // 这里显示标题
			Bitmap bitmap = MediaUtil.getArtwork(this, mp3Info.getId(),
					mp3Info.getAlbumId(), true, true);// 获取专辑位图对象，为小图
			musicAlbum.setImageBitmap(bitmap); // 这里显示专辑图片
			Intent intent = new Intent(HomeActivity.this, PlayerActivity.class); // 定义Intent对象，跳转到PlayerActivity
			// 添加一系列要传递的数据
			intent.putExtra("title", mp3Info.getTitle());
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("artist", mp3Info.getArtist());
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("currentTime", currentTime);
			intent.putExtra("repeatState", repeatState);
			intent.putExtra("shuffleState", isShuffle);
			intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
			startActivity(intent);
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 按返回键弹出对话框确定退出
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			new CustomDialog.Builder(HomeActivity.this)
					.setTitle(R.string.info)
					.setMessage(R.string.dialog_messenge)
					.setPositiveButton(R.string.confrim,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									exit();

								}
							}).setNeutralButton(R.string.cancel, null).show();
			return false;
		}
		return false;
	}

	// 自定义的BroadcastReceiver，负责监听从Service传回来的广播
	public class HomeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MUSIC_CURRENT)) {
				// currentTime代表当前播放的时间
				currentTime = intent.getIntExtra("currentTime", -1);
				musicDuration.setText(MediaUtil.formatTime(currentTime));
			} else if (action.equals(MUSIC_DURATION)) {
				duration = intent.getIntExtra("duration", -1);
			} else if (action.equals(UPDATE_ACTION)) {
				// 获取Intent中的current消息，current代表当前正在播放的歌曲
				listPosition = intent.getIntExtra("current", -1);
				if (listPosition >= 0) {
					musicTitle.setText(mp3Infos.get(listPosition).getTitle());
				}
			} else if (action.equals(REPEAT_ACTION)) {
				repeatState = intent.getIntExtra("repeatState", -1);
				switch (repeatState) {
				case isCurrentRepeat: // 单曲循环
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_current_selector);
					shuffleBtn.setClickable(false);
					break;
				case isAllRepeat: // 全部循环
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_all_selector);
					shuffleBtn.setClickable(false);
					break;
				case isNoneRepeat: // 无重复
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_none_selector);
					shuffleBtn.setClickable(true);
					break;
				}
			} else if (action.equals(SHUFFLE_ACTION)) {
				isShuffle = intent.getBooleanExtra("shuffleState", false);
				if (isShuffle) {
					isNoneShuffle = false;
					shuffleBtn
							.setBackgroundResource(R.drawable.shuffle_selector);
					repeatBtn.setClickable(false);
				} else {
					isNoneShuffle = true;
					shuffleBtn
							.setBackgroundResource(R.drawable.shuffle_none_selector);
					repeatBtn.setClickable(true);
				}
			}
		}

	}
}
