package com.example.DontStepWhielBlock;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.DontStepWhielBlock.GameConfig.GameMode;
import com.example.DontStepWhielBlock.GameView.GameSurface;
import com.example.DontStepWhielBlock.GameView.GameSurface.playPanioMusic;
import com.example.DontStepWhielBlock.GameView.GameSurface.setGameOverEevent;
import com.example.DontStepWhielBlock.GameView.GameSurface.setProgressBar;

public class MainActivity extends Activity {

	private GameSurface gameSurface;
	private SharedPreferences sp;
	private int mode;
	private int phoneWidth;
	private int phoneHeight;
	private ProgressBar pb_time;
	private MediaPlayer media;
	private int[] painoTone = { R.raw.g4, R.raw.g5, R.raw.g6, R.raw.g6m,
			R.raw.a4, R.raw.a5, R.raw.a5m, R.raw.a6, R.raw.b3, R.raw.c3m,
			R.raw.d5 };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		gameSurface = (GameSurface) findViewById(R.id.gameView);
		pb_time = (ProgressBar) findViewById(R.id.pb_time);
		media = new MediaPlayer();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		phoneWidth = wm.getDefaultDisplay().getWidth();
		phoneHeight = wm.getDefaultDisplay().getHeight();
		sp = getSharedPreferences("Config", MODE_PRIVATE);
		mode = getIntent().getIntExtra(EntryGameActivity.GMAEMODE, 1);
		switch (mode) {
		case EntryGameActivity.BASEMODE:
			pb_time.setVisibility(View.VISIBLE);
			gameSurface.setGameMod(GameMode.BASE);
			// 监听游戏的计时事件
			gameSurface.setSetProBar(new setProgressBar() {
				@Override
				public void setProgress(long progress) {

					pb_time.setProgress((int) progress);

				}

				@Override
				public void setMax(long max) {

					pb_time.setMax((int) max);

				}
			});
			break;

		case EntryGameActivity.ROLLMODE:
			pb_time.setVisibility(View.GONE);
			gameSurface.setGameMod(GameMode.ROLLDOWN);
			break;
		case EntryGameActivity.CHANGESPEED:
			pb_time.setVisibility(View.GONE);
			gameSurface.setGameMod(GameMode.CHANGSPEED);
			break;
		}

		// 监听游戏结束的接口，分数判断
		gameSurface.setGameOverEevent(new setGameOverEevent() {

			@Override
			public void gameOver(int score) {

				gameOverState(score);
			}

		});

		gameSurface.setPlayMusic(new playPanioMusic() {

			@Override
			public void playPlayingMusci() {

//				play(painoTone[getRandom(painoTone.length)]);

			}

			@Override
			public void playGameOver() {

				play(R.raw.f1m);

			}
		});

	}

	private Random random;

	public int getRandom(int range) {
		return new Random().nextInt(range);

	}

	public void play(int resid) {

		try {
			if (media != null) {
				media.release();
			}
			media = MediaPlayer.create(this, resid);
			media.start();
		} catch (Exception e) {
			// Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	/**
	 * 将ACTIVITY的点击事件交给gameSurface 处理
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gameSurface.handlerTouchEvent(event);
		return super.onTouchEvent(event);
	}

	private void gameOverState(int score) {

		int maxScore = sp.getInt("maxScore" + mode, 0);
		if (score > maxScore) {
			// 发现大于以前的最高分，则保存最高分
			Editor editor = sp.edit();
			editor.putInt("maxScore" + mode, score);
			editor.commit();
			maxScore = score;
		}

		AlertDialog.Builder builder = new Builder(MainActivity.this);
		builder.setCancelable(false);
		final AlertDialog dialog = builder.create();
		dialog.setCancelable(false);
		View view = View.inflate(MainActivity.this, R.layout.gameover_dialog,
				null);

		Button bt_over = (Button) view.findViewById(R.id.bt_over);
		Button bt_continue = (Button) view.findViewById(R.id.bt_continue);
		TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
		TextView tvScore = (TextView) view.findViewById(R.id.tv_score);
		TextView tvMaxScore = (TextView) view.findViewById(R.id.tv_maxScore);
		tvScore.setText(score + "");
		tvMaxScore.setText("历史最佳记录：" + maxScore);
		bt_continue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gameSurface.startGame();
				dialog.dismiss();

			}
		});
		bt_over.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				gameSurface.setRunning(false);
				dialog.dismiss();
				finish();
			}
		});

		dialog.show();
		dialog.setContentView(view);
		// 根据屏幕宽度调整确定对话框宽高
		dialog.getWindow().setLayout(phoneWidth - 100, phoneWidth - 100);

	}

	@Override
	protected void onDestroy() {
		if (gameSurface != null) {
			gameSurface.setRunning(false);
			gameSurface = null;
		}
		super.onDestroy();
	}
}
