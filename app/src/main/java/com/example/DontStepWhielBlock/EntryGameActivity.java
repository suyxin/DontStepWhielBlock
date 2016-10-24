package com.example.DontStepWhielBlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Space;
import android.widget.TextView;

/**
 * @Description 游戏入口
 * @author suyxin
 * @time 11:09:45 PM Aug 7, 2015
 */

public class EntryGameActivity extends Activity implements OnClickListener {
	private Button bt_classicMode;
	private Button bt_roll;
	private Button bt_history;
	private Button bt_changeSpeed;
	private SharedPreferences sp;
	private int phoneWidth;
	private int phoneHeight;
	
	
	/**
	 * 用户选择的游戏模式的值的健名
	 */
	public static final String GMAEMODE = "gameMode";
	public static final int BASEMODE = 1;
	public static final int ROLLMODE = 2;
	public static final int CHANGESPEED = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_initia_game);
		bt_classicMode = (Button) findViewById(R.id.bt_classicMode);
		bt_roll = (Button) findViewById(R.id.bt_roll);
		bt_changeSpeed = (Button) findViewById(R.id.bt_changeSpeed);
		bt_history = (Button) findViewById(R.id.bt_history);
		bt_classicMode.setOnClickListener(this);
		bt_roll.setOnClickListener(this);
		bt_history.setOnClickListener(this);
		bt_changeSpeed.setOnClickListener(this);
		sp = getSharedPreferences("Config", MODE_PRIVATE);
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		phoneWidth = wm.getDefaultDisplay().getWidth();
		phoneHeight = wm.getDefaultDisplay().getHeight();
	}
	
	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {

		case R.id.bt_classicMode:
			intent = new Intent(EntryGameActivity.this, MainActivity.class);
			intent.putExtra(GMAEMODE, BASEMODE);
			startActivity(intent);
			break;

		case R.id.bt_roll:
			intent = new Intent(EntryGameActivity.this, MainActivity.class);
			intent.putExtra(GMAEMODE, ROLLMODE);
			startActivity(intent);
			break;
		case R.id.bt_changeSpeed:
			intent = new Intent(EntryGameActivity.this, MainActivity.class);
			intent.putExtra(GMAEMODE, CHANGESPEED);
			startActivity(intent);
			break;
		case R.id.bt_history:
			showHistory();
			break;
		}

	}

	/**
	 * 显示最佳记录分数
	 */
	private void showHistory() {

		AlertDialog.Builder builder = new Builder(EntryGameActivity.this);
		final AlertDialog dialog = builder.create();
		View view = View.inflate(EntryGameActivity.this,
				R.layout.history_score_dialog, null);
		Button bt_reback = (Button) view.findViewById(R.id.bt_reback);
		Button bt_share = (Button) view.findViewById(R.id.bt_share);
		TextView tv_baseMaxScore = (TextView) view
				.findViewById(R.id.tv_baseMaxScore);
		TextView tv_downMaxScore = (TextView) view
				.findViewById(R.id.tv_downMaxScore);
		TextView tv_speedMaxScore = (TextView) view
				.findViewById(R.id.tv_speedMaxScore);
		final int baseMaxScore = sp.getInt("maxScore" + BASEMODE, 0);
		final int downMaxScore = sp.getInt("maxScore" + ROLLMODE, 0);
		final int speedMaxScore = sp.getInt("maxScore" + CHANGESPEED, 0);
		tv_baseMaxScore.setText(baseMaxScore + "");
		tv_downMaxScore.setText(downMaxScore + "");
		tv_speedMaxScore.setText(speedMaxScore + "");
		bt_reback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				dialog.dismiss();

			}
		});
		bt_share.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);// 发送的意图
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.setType("text/plain");
				String contentShare = String.format("我在别踩白块儿游戏中的最佳记录:\n"
								+ "基本模式：%s \n下落模式:%s\n加速挑战:%s\n想要挑战么？来玩别踩白块儿！！",
						baseMaxScore, downMaxScore, speedMaxScore);
				intent.putExtra(Intent.EXTRA_TEXT, contentShare);
				startActivity(intent);
			}
		});
		dialog.show();
		dialog.setContentView(view);
		// 根据屏幕宽度调整确定对话框宽高
		dialog.getWindow().setLayout(phoneWidth - 50, phoneWidth + 50);

	}

}
