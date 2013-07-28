package com.wwj.sb.activity;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wwj.sb.custom.LrcView;
import com.wwj.sb.domain.AppConstant;
import com.wwj.sb.domain.Mp3Info;
import com.wwj.sb.service.PlayerService;
import com.wwj.sb.utils.ImageUtil;
import com.wwj.sb.utils.MediaUtil;

/**
 * 播放音乐界面
 * 
 * @author wwj 从主界面传递过来歌曲的Id、歌曲名、歌手、歌曲路径、播放状态
 */
public class PlayerActivity extends Activity {
	private TextView musicTitle = null;
	private TextView musicArtist = null;
	private Button previousBtn; // 上一首
	private Button repeatBtn; // 重复（单曲循环、全部循环）
	private Button playBtn; // 播放（播放、暂停）
	private Button shuffleBtn; // 随机播放
	private Button nextBtn; // 下一首
	private Button queueBtn; // 歌曲列表
	private SeekBar music_progressBar; // 歌曲进度
	private TextView currentProgress; // 当前进度消耗的时间
	private TextView finalProgress; // 歌曲时间

	private String title; // 歌曲标题
	private String artist; // 歌曲艺术家
	private String url; // 歌曲路径
	private int listPosition; // 播放歌曲在mp3Infos的位置
	private int currentTime; // 当前歌曲播放时间
	private int duration; // 歌曲长度
	private int flag; // 播放标识

	private int repeatState;
	private final int isCurrentRepeat = 1; // 单曲循环
	private final int isAllRepeat = 2; // 全部循环
	private final int isNoneRepeat = 3; // 无重复播放
	private boolean isPlaying; // 正在播放
	private boolean isPause; // 暂停
	private boolean isNoneShuffle; // 顺序播放
	private boolean isShuffle; // 随机播放

	private List<Mp3Info> mp3Infos;
	public static LrcView lrcView; // 自定义歌词视图

	private PlayerReceiver playerReceiver;
	public static final String UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION"; // 更新动作
	public static final String CTL_ACTION = "com.wwj.action.CTL_ACTION"; // 控制动作
	public static final String MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT"; // 音乐当前时间改变动作
	public static final String MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION";// 音乐播放长度改变动作
	public static final String MUSIC_PLAYING = "com.wwj.action.MUSIC_PLAYING"; // 音乐正在播放动作
	public static final String REPEAT_ACTION = "com.wwj.action.REPEAT_ACTION"; // 音乐重复播放动作
	public static final String SHUFFLE_ACTION = "com.wwj.action.SHUFFLE_ACTION";// 音乐随机播放动作
	public static final String SHOW_LRC = "com.wwj.action.SHOW_LRC"; // 通知显示歌词
	
	
	
	private AudioManager am;		//音频管理引用，提供对音频的控制
	RelativeLayout ll_player_voice;	//音量控制面板布局
	int currentVolume;				//当前音量
	int maxVolume;					//最大音量
	ImageButton ibtn_player_voice;	//显示音量控制面板的按钮
	SeekBar sb_player_voice;		//控制音量大小
	// 音量面板显示和隐藏动画
	private Animation showVoicePanelAnimation;
	private Animation hiddenVoicePanelAnimation;
	
	
	private ImageView musicAlbum;	//音乐专辑封面
	private ImageView musicAblumReflection;	//倒影反射
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("PlayerActivity onCreated");
		setContentView(R.layout.play_activity_layout);
		
		findViewById();
		setViewOnclickListener();
		getDataFromBundle();
		
		mp3Infos = MediaUtil.getMp3Infos(PlayerActivity.this);	//获取所有音乐的集合对象
		registerReceiver();

		// 添加来电监听事件
		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); // 获取系统服务
		telManager.listen(new MobliePhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);
		
		//音量调节面板显示和隐藏的动画
		showVoicePanelAnimation = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.push_up_in);
		hiddenVoicePanelAnimation = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.push_up_out);
		
		//获得系统音频管理服务对象
		am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		sb_player_voice.setProgress(currentVolume);
		initView();		//初始化视图
		am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
		System.out.println("currentVolume--->"+currentVolume);
		System.out.println("maxVolume-->" + maxVolume);
		
	}

	private void registerReceiver() {
		//定义和注册广播接收器
		playerReceiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		registerReceiver(playerReceiver, filter);
	}

	/**
	 * 从界面上根据id获取按钮
	 */
	private void findViewById() {
		musicTitle = (TextView) findViewById(R.id.musicTitle);
		musicArtist = (TextView) findViewById(R.id.musicArtist);
		previousBtn = (Button) findViewById(R.id.previous_music);
		repeatBtn = (Button) findViewById(R.id.repeat_music);
		playBtn = (Button) findViewById(R.id.play_music);
		shuffleBtn = (Button) findViewById(R.id.shuffle_music);
		nextBtn = (Button) findViewById(R.id.next_music);
		queueBtn = (Button) findViewById(R.id.play_queue);
		music_progressBar = (SeekBar) findViewById(R.id.audioTrack);
		currentProgress = (TextView) findViewById(R.id.current_progress);
		finalProgress = (TextView) findViewById(R.id.final_progress);
		lrcView = (LrcView) findViewById(R.id.lrcShowView);
		ibtn_player_voice = (ImageButton) findViewById(R.id.ibtn_player_voice);
		ll_player_voice = (RelativeLayout) findViewById(R.id.ll_player_voice);
		sb_player_voice = (SeekBar) findViewById(R.id.sb_player_voice);
		musicAlbum = (ImageView) findViewById(R.id.iv_music_ablum);
		musicAblumReflection = (ImageView) findViewById(R.id.iv_music_ablum_reflection);
	}
	

	/**
	 * 给每一个按钮设置监听器
	 */
	private void setViewOnclickListener() {
		ViewOnclickListener ViewOnClickListener = new ViewOnclickListener();
		previousBtn.setOnClickListener(ViewOnClickListener);
		repeatBtn.setOnClickListener(ViewOnClickListener);
		playBtn.setOnClickListener(ViewOnClickListener);
		shuffleBtn.setOnClickListener(ViewOnClickListener);
		nextBtn.setOnClickListener(ViewOnClickListener);
		queueBtn.setOnClickListener(ViewOnClickListener);
		music_progressBar
				.setOnSeekBarChangeListener(new SeekBarChangeListener());
		ibtn_player_voice.setOnClickListener(ViewOnClickListener);
		sb_player_voice.setOnSeekBarChangeListener(new SeekBarChangeListener());
	}

	/**
	 * 
	 * @author wwj
	 * 电话监听器类
	 */
	private class MobliePhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: // 挂机状态
				Intent intent = new Intent(PlayerActivity.this, PlayerService.class);
				playBtn.setBackgroundResource(R.drawable.play_selector);
				intent.setAction("com.wwj.media.MUSIC_SERVICE");
				intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);	//继续播放音乐
				startService(intent);
				isPlaying = false;
				isPause = true;
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:	//通话状态
			case TelephonyManager.CALL_STATE_RINGING:	//响铃状态
				Intent intent2 = new Intent(PlayerActivity.this, PlayerService.class);
				playBtn.setBackgroundResource(R.drawable.pause_selector);
				intent2.setAction("com.wwj.media.MUSIC_SERVICE");
				intent2.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
				startService(intent2);
				isPlaying = true;
				isPause = false;
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		System.out.println("PlayerActivity has started");
	}

	/**
	 * 从Bundle中获取来自HomeActivity中传过来的数据
	 */
	private void getDataFromBundle() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		title = bundle.getString("title");
		artist = bundle.getString("artist");
		url = bundle.getString("url");
		listPosition = bundle.getInt("listPosition");
		repeatState = bundle.getInt("repeatState");
		isShuffle = bundle.getBoolean("shuffleState");
		flag = bundle.getInt("MSG");
		currentTime = bundle.getInt("currentTime");
		duration = bundle.getInt("duration");
	}
	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("PlayerActivity has paused");
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver();
		System.out.println("PlayerActivity has onResume");
	}

	/**
	 * 初始化界面
	 */
	public void initView() {
		isPlaying = true;
		isPause = false;
		musicTitle.setText(title);
		musicArtist.setText(artist);
		music_progressBar.setProgress(currentTime);
		music_progressBar.setMax(duration);
		sb_player_voice.setMax(maxVolume);
		Mp3Info mp3Info = mp3Infos.get(listPosition);
		showArtwork(mp3Info);
		switch (repeatState) {
		case isCurrentRepeat: // 单曲循环
			shuffleBtn.setClickable(false);
			repeatBtn.setBackgroundResource(R.drawable.repeat_current_selector);
			break;
		case isAllRepeat: // 全部循环
			shuffleBtn.setClickable(false);
			repeatBtn.setBackgroundResource(R.drawable.repeat_all_selector);
			break;
		case isNoneRepeat: // 无重复
			shuffleBtn.setClickable(true);
			repeatBtn.setBackgroundResource(R.drawable.repeat_none_selector);
			break;
		}
		if (isShuffle) {
			isNoneShuffle = false;
			shuffleBtn.setBackgroundResource(R.drawable.shuffle_selector);
			repeatBtn.setClickable(false);
		} else {
			isNoneShuffle = true;
			shuffleBtn.setBackgroundResource(R.drawable.shuffle_none_selector);
			repeatBtn.setClickable(true);
		}
		if (flag == AppConstant.PlayerMsg.PLAYING_MSG) { // 如果播放信息是正在播放
			Toast.makeText(PlayerActivity.this, "正在播放--" + title, 1).show();
			Intent intent = new Intent();
			intent.setAction(SHOW_LRC);
			intent.putExtra("listPosition", listPosition);
			sendBroadcast(intent);
		} else if (flag == AppConstant.PlayerMsg.PLAY_MSG) { // 如果是点击列表播放歌曲的话
			playBtn.setBackgroundResource(R.drawable.play_selector);
			play();
		} else if (flag == AppConstant.PlayerMsg.CONTINUE_MSG) {
			Intent intent = new Intent(PlayerActivity.this, PlayerService.class);
			playBtn.setBackgroundResource(R.drawable.play_selector);
			intent.setAction("com.wwj.media.MUSIC_SERVICE");
			intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);	//继续播放音乐
			startService(intent);
		}
		
	}

	/**
	 * 显示专辑封面
	 */
	private void showArtwork(Mp3Info mp3Info) {
		Bitmap bm = MediaUtil.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
		//切换播放时候专辑图片出现透明效果
		Animation albumanim = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.album_replace);
		//开始播放动画效果
		musicAlbum.startAnimation(albumanim);
		if(bm != null) {
			musicAlbum.setImageBitmap(bm);	//显示专辑封面图片
			musicAblumReflection.setImageBitmap(ImageUtil.createReflectionBitmapForSingle(bm));	//显示倒影
		} else {
			bm = MediaUtil.getDefaultArtwork(this, false);
			musicAlbum.setImageBitmap(bm);	//显示专辑封面图片
			musicAblumReflection.setImageBitmap(ImageUtil.createReflectionBitmapForSingle(bm));	//显示倒影
		}
		
	}

	
	/**
	 * 反注册广播
	 */
	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(playerReceiver);
		System.out.println("PlayerActivity has stoped");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("PlayerActivity has Destoryed");
	}

	/**
	 * 控件点击事件
	 * 
	 * @author wwj
	 * 
	 */
	private class ViewOnclickListener implements OnClickListener {
		Intent intent = new Intent();

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.play_music:
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
					intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);
					startService(intent);
					isPause = false;
					isPlaying = true;
				}
				break;
			case R.id.previous_music: // 上一首歌曲
				previous_music();
				break;
			case R.id.next_music: // 下一首歌曲
				next_music();
				break;
			case R.id.repeat_music: // 重复播放音乐
				if (repeatState == isNoneRepeat) {
					repeat_one();
					shuffleBtn.setClickable(false); // 是随机播放变为不可点击状态
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
				Intent intent = new Intent(REPEAT_ACTION);
				switch (repeatState) {
				case isCurrentRepeat: // 单曲循环
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_current_selector);
					Toast.makeText(PlayerActivity.this,
							R.string.repeat_current, Toast.LENGTH_SHORT).show();

					intent.putExtra("repeatState", isCurrentRepeat);
					sendBroadcast(intent);
					break;
				case isAllRepeat: // 全部循环
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_all_selector);
					Toast.makeText(PlayerActivity.this, R.string.repeat_all,
							Toast.LENGTH_SHORT).show();
					intent.putExtra("repeatState", isAllRepeat);
					sendBroadcast(intent);
					break;
				case isNoneRepeat: // 无重复
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_none_selector);
					Toast.makeText(PlayerActivity.this, R.string.repeat_none,
							Toast.LENGTH_SHORT).show();
					intent.putExtra("repeatState", isNoneRepeat);
					break;
				}
				break;
			case R.id.shuffle_music: // 随机播放状态
				Intent shuffleIntent = new Intent(SHUFFLE_ACTION);
				if (isNoneShuffle) { // 如果当前状态为非随机播放，点击按钮之后改变状态为随机播放
					shuffleBtn
							.setBackgroundResource(R.drawable.shuffle_selector);
					Toast.makeText(PlayerActivity.this, R.string.shuffle,
							Toast.LENGTH_SHORT).show();
					isNoneShuffle = false;
					isShuffle = true;
					shuffleMusic();
					repeatBtn.setClickable(false);
					shuffleIntent.putExtra("shuffleState", true);
					sendBroadcast(shuffleIntent);
				} else if (isShuffle) {
					shuffleBtn
							.setBackgroundResource(R.drawable.shuffle_none_selector);
					Toast.makeText(PlayerActivity.this, R.string.shuffle_none,
							Toast.LENGTH_SHORT).show();
					isShuffle = false;
					isNoneShuffle = true;
					repeatBtn.setClickable(true);
					shuffleIntent.putExtra("shuffleState", false);
					sendBroadcast(shuffleIntent);
				}
				break;
				
			case R.id.ibtn_player_voice:	//控制音量
				voicePanelAnimation();
				break;
			case R.id.play_queue:
				showPlayQueue();
				break;
			}
		}
	}

	/**
	 * 实现监听Seekbar的类
	 * 
	 * @author wwj
	 * 
	 */
	private class SeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			switch(seekBar.getId()) {
			case R.id.audioTrack:
				if (fromUser) {
					audioTrackChange(progress); // 用户控制进度的改变
				}
				break;
			case R.id.sb_player_voice:
				// 设置音量
				am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
				System.out.println("am--->" + progress);
				break;
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}

	}

	/**
	 * 播放音乐
	 */
	public void play() {
		// 开始播放的时候为顺序播放
		repeat_none();
		Intent intent = new Intent();
		intent.setAction("com.wwj.media.MUSIC_SERVICE");
		intent.putExtra("url", url);
		intent.putExtra("listPosition", listPosition);
		intent.putExtra("MSG", flag);
		startService(intent);
	}
	
	
	
	/**
	 * 显示播放列表
	 */
	public void showPlayQueue() {
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View playQueueLayout = layoutInflater.inflate(R.layout.play_queue_layout, (ViewGroup)findViewById(R.id.play_queue_layout));
		ListView queuelist = (ListView) playQueueLayout.findViewById(R.id.lv_play_queue);
		
		List<HashMap<String, String>> queues = MediaUtil.getMusicMaps(mp3Infos);
		SimpleAdapter adapter = new SimpleAdapter(this, queues, R.layout.play_queue_item_layout, new String[]{"title",
				"Artist", "duration"}, new int[]{R.id.music_title, R.id.music_Artist, R.id.music_duration});
		queuelist.setAdapter(adapter);
		AlertDialog.Builder builder;
		final AlertDialog dialog;
		builder = new AlertDialog.Builder(this);
		dialog = builder.create();
		dialog.setView(playQueueLayout);
		dialog.show();
	}

	//控制显示音量控制面板的动画
	public void voicePanelAnimation() {
		if(ll_player_voice.getVisibility() == View.GONE) {
			ll_player_voice.startAnimation(showVoicePanelAnimation);
			ll_player_voice.setVisibility(View.VISIBLE);
		}
		else{
			ll_player_voice.startAnimation(hiddenVoicePanelAnimation);
			ll_player_voice.setVisibility(View.GONE);
		}
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
	 * 播放进度改变
	 * @param progress
	 */
	public void audioTrackChange(int progress) {
		Intent intent = new Intent();
		intent.setAction("com.wwj.media.MUSIC_SERVICE");
		intent.putExtra("url", url);
		intent.putExtra("listPosition", listPosition);
		intent.putExtra("MSG", AppConstant.PlayerMsg.PROGRESS_CHANGE);
		intent.putExtra("progress", progress);
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
	 * 上一首
	 */
	public void previous_music() {
		playBtn.setBackgroundResource(R.drawable.play_selector);
		listPosition = listPosition - 1;
		if (listPosition >= 0) {
			Mp3Info mp3Info = mp3Infos.get(listPosition); // 上一首MP3
			showArtwork(mp3Info);		//显示专辑封面
			musicTitle.setText(mp3Info.getTitle());
			musicArtist.setText(mp3Info.getArtist());
			url = mp3Info.getUrl();
			Intent intent = new Intent();
			intent.setAction("com.wwj.media.MUSIC_SERVICE");
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("MSG", AppConstant.PlayerMsg.PRIVIOUS_MSG);
			startService(intent);
			
			
			
		} else {
			listPosition = 0;
			Toast.makeText(PlayerActivity.this, "没有上一首了", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * 下一首
	 */
	public void next_music() {
		playBtn.setBackgroundResource(R.drawable.play_selector);
		listPosition = listPosition + 1;
		if (listPosition <= mp3Infos.size() - 1) {
			Mp3Info mp3Info = mp3Infos.get(listPosition);
			showArtwork(mp3Info);	//显示专辑封面
			url = mp3Info.getUrl();
			musicTitle.setText(mp3Info.getTitle());
			musicArtist.setText(mp3Info.getArtist());
			Intent intent = new Intent();
			intent.setAction("com.wwj.media.MUSIC_SERVICE");
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("MSG", AppConstant.PlayerMsg.NEXT_MSG);
			startService(intent);
			
		} else {
			listPosition = mp3Infos.size() - 1;
			Toast.makeText(PlayerActivity.this, "没有下一首了", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * 用来接收从service传回来的广播的内部类
	 * 
	 * @author wwj
	 * 
	 */
	public class PlayerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MUSIC_CURRENT)) {
				currentTime = intent.getIntExtra("currentTime", -1);
				currentProgress.setText(MediaUtil.formatTime(currentTime));
				music_progressBar.setProgress(currentTime);
			} else if (action.equals(MUSIC_DURATION)) {
				int duration = intent.getIntExtra("duration", -1);
				music_progressBar.setMax(duration);
				finalProgress.setText(MediaUtil.formatTime(duration));
			} else if (action.equals(UPDATE_ACTION)) {
				// 获取Intent中的current消息，current代表当前正在播放的歌曲
				listPosition = intent.getIntExtra("current", -1);
				url = mp3Infos.get(listPosition).getUrl();
				if (listPosition >= 0) {
					musicTitle.setText(mp3Infos.get(listPosition).getTitle());
					musicArtist.setText(mp3Infos.get(listPosition).getArtist());
				}
				if (listPosition == 0) {
					finalProgress.setText(MediaUtil.formatTime(mp3Infos.get(
							listPosition).getDuration()));
					playBtn.setBackgroundResource(R.drawable.pause_selector);
					isPause = true;
				}
			}
		}
	}

	/**
	 * 回调音量控制函数
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		switch(keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:	//按音量减键
			if(action == KeyEvent.ACTION_UP) {
				if(currentVolume < maxVolume) {
					currentVolume = currentVolume + 1;
					sb_player_voice.setProgress(currentVolume);
					am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
				} else {
					am.setStreamVolume(AudioManager.STREAM_MUSIC,
							currentVolume, 0);
				}
			}
			return false;
		case KeyEvent.KEYCODE_VOLUME_DOWN:	//按音量加键
			if(action == KeyEvent.ACTION_UP) {
				if(currentVolume > 0) {
					currentVolume = currentVolume - 1;
					sb_player_voice.setProgress(currentVolume);
					am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
				} else {
					am.setStreamVolume(AudioManager.STREAM_MUSIC,
							currentVolume, 0);
				}
			}
			return false;
			default:
				return super.dispatchKeyEvent(event);
		}
	}
}
