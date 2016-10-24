package com.example.DontStepWhielBlock.GameView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.example.DontStepWhielBlock.GameConfig.GameMode;

/**
 * @Description
 * @author suyxin
 * @time 1:55:06 PM Aug 6, 2015
 */

public class GameSurface extends SurfaceView implements Runnable, Callback {

	public GameSurface(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initiaGameView();
	}

	public GameSurface(Context context) {
		super(context);
		initiaGameView();
	}

	public GameSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		initiaGameView();
	}

	private SurfaceHolder mHolder;
	private Thread refreshThread;
	private boolean isRunning;
	private int recViewHeight;
	private int recViewWidth;
	private int rowCount = 4;
	private int columnCount = 4;
	private Canvas canvas;
	private Paint textPiant;
	private FontMetricsInt fontMetrics;
	private float strokeWidth = 1f;
	private Paint bgPaint;
	private Paint borderPaint;
	private float textSize = 80;

	protected void initiaGameView() {
		mHolder = getHolder();// 获得容器
		mHolder.addCallback(this);// 添加回调
		setFocusable(true);// 设置可获得焦点
		setFocusableInTouchMode(true);
		setKeepScreenOn(true);// 设置屏幕常亮
		bgPaint = new Paint();
		bgPaint.setColor(Color.WHITE);

		borderPaint = new Paint();
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setColor(Color.BLACK);
		rectViews = new ArrayList<RectBean>();

		textPiant = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPiant.setTextSize(textSize);
		textPiant.setTextAlign(Paint.Align.CENTER);
		textPiant.setColor(Color.RED);		
		fontMetrics = new FontMetricsInt();

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		recViewHeight = h / rowCount;
		recViewWidth = w / columnCount;
		System.out.println("onSizeChanged调用 ");
		startGame();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	public void startGame() {

		gameOver = false;
		gameStart = false;
		startPlay = false;
		speed = MIN_SPEED;
		changCount = DRAW_COUNT;		
		score = 0;
		
		if (gameMod==GameMode.BASE) {
			count = 0;
			setProBar.setProgress(count);
			handler.removeMessages(HANLDER_GAMEOVER);
			handler.removeMessages(COUNTTIMET);
		}
		
		
		if (!rectViews.isEmpty()) {
			rectViews.clear();
		}
		if (!rowList.isEmpty()) {
			rowList.clear();
		}

		for (int i = -1; i < rowCount - 1; i++) {
			rectViews.addAll(addRow(i));
		}
		// 第四行全为黄色,原始时一共添加为5行，可见的有4行，之后一直循环利用
		rectViews.addAll(addBottomRow());
	}



	/**
	 *
	 */
	/**
	 * 增加一行的矩形到指定的行位位置处
	 * 
	 * @param row
	 *            行数值从0开始
	 * @return List<RectView> 添加的一行矩形对象
	 */
	public List<RectBean> addRow(int row) {
		List<RectBean> rowRects = new ArrayList<RectBean>();
		int blackBlock = getRandom(columnCount);
		for (int i = 0; i < columnCount; i++) {
			RectBean bean = new RectBean();
			Rect rect = new Rect(recViewWidth * i, row * recViewHeight,
					recViewWidth * (i + 1), recViewHeight * (row + 1));
			if (i != blackBlock) {
				bean.setStyle(Paint.Style.STROKE);
			} else {
				bean.setStyle(Paint.Style.FILL);
			}
			bean.setRect(rect);
			bean.setColorRect(rect);
			bean.setColor(Color.BLACK);
			rowRects.add(bean);
		}

		/**
		 * 在一行中随机选取一个为黑色填充，其他为黑框主体白色
		 */

		return rowRects;
	}

	/**
	 * 游戏开始时添加到底部一行的黄芭矩形
	 * 
	 * @return List<RectBean>
	 */

	private static final int lightYellow = 0x83ffff00;

	public List<RectBean> addBottomRow() {

		List<RectBean> rowRects = new ArrayList<RectBean>();
		for (int i = 0; i < columnCount; i++) {
			Rect rect = new Rect(recViewWidth * i, (rowCount - 1)
					* recViewHeight, recViewWidth * (i + 1), recViewHeight
					* rowCount);
			RectBean bean = new RectBean();
			bean.setRect(rect);
			bean.setColorRect(rect);
			bean.setColor(lightYellow);
			bean.setStyle(Paint.Style.FILL);
			rowRects.add(bean);
		}

		/**
		 * 在一行中随机选取一个为黑色填充，其他为黑框主体白色
		 */

		return rowRects;
	}

	/**
	 * surfaceView被创建时的三个回调方法，创建时启动一个自定义的看门狗子线程（刷帧），不断地循环绘制
	 */

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		isRunning = true;
		gameOver = false;
		refreshThread = new Thread(this);
		refreshThread.start();
	

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		isRunning = false;
		if (rectViews != null) {
			rectViews.clear();
			rectViews = null;
		}
		


	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	private long minDrawTime = 200;
	private int fps;
	private List<RectBean> rectViews;
	private static final String TAG = "FPS";

	@Override
	public void run() {

		while (isRunning) {
			long startTime = System.currentTimeMillis();

			draw(); // 渲染绘制一次

			long endTime = System.currentTimeMillis();
			long drawTime = endTime - startTime;
			fps = (int) (1000 / drawTime);// 每秒的绘制次数
			// System.out.println(fps);
			// // 延时的原因是限制视图刷新频率FPS，过高是没有必要的
			// System.out.println("fps:"+fps);
			// if (drawTime<minDrawTime) {
			// try {
			// Thread.sleep(minDrawTime-drawTime);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			Log.i(TAG, fps + "");
		}

	}

	/**        
	 *  
	 */

	// 从容器中获取这个画面
	// 脏矩形概念，在绘制的矩形区域会像素被重写，而在每次绘制的矩形区域之外的绘制的图形则保持不变。
	// ！！！！重点：因为这个特性，每次绘制实质内容之前需要把全屏幕绘制一次背景，把之前绘制的内容全部覆盖掉。否则会出现图形移动时，移动之前的所有位置
	// 都会被绘制出来,导致连续的图形串
	// * Just like {@link #lockCanvas()} but allows specification of a dirty
	// rectangle.
	// * Every
	// * pixel within that rectangle must be written; however pixels outside
	// * the dirty rectangle will be preserved by the next call to
	// lockCanvas().
	// */
	// public Canvas lockCanvas(Rect dirty);

	private void draw() {

		canvas = mHolder.lockCanvas();

		try {
			if (canvas != null) {
				// 全屏幕覆盖掉原来的绘制的区域，每次都必须重新绘制。
				// !!!此步非常重要，必须把上次绘制的整个屏幕的内容覆盖掉，系统默认是每次绘制的内家在锁定的画布区域内都会保留

				canvas.drawRect(0, 0, this.getWidth(), this.getHeight(),
						bgPaint);
				// 绘制所有矩形图
				if (!gameOver) {
					moveLogic();
				}
				// 绘制
				for (RectBean r : rectViews) {

					// 绘制边框
					canvas.drawRect(r.getRect(), borderPaint);

					// 绘制色块
					if (r.getStyle() == Style.FILL) {
						paint.setStyle(r.getStyle());//
						paint.setColor(r.getColor());
						canvas.drawRect(r.getColorRect(), paint);

					}
					// 红块闪烁
					if (r.isIstwinkel()) {
						r.checkTwinkle(System.currentTimeMillis());
					}

					//开始时在第三行的黑块绘制“开始”字样
					if (!startPlay) {
						if (r.getColor() == Color.BLACK
								&& r.getStyle() == Style.FILL
								&& r.getRect().bottom == (rowCount - 1)
										* recViewHeight) {
							textPiant.setColor(Color.WHITE);
							textPiant.setTextSize(recViewWidth/2-10);
							int baseline = r.getRect().top
									+ (r.getRect().bottom - r.getRect().top
											- fontMetrics.bottom + fontMetrics.top)
									/ 2 - fontMetrics.top;
							canvas.drawText("开始", r.getRect().left
									+ recViewWidth / 2, baseline, textPiant);
						}
					}

				}				
					textPiant.setColor(Color.RED);
					canvas.drawText(score + "分", getWidth() / 2, 130, textPiant);
				
				

			}

		} catch (Exception e) {

		} finally {
			if (canvas != null) {
				mHolder.unlockCanvasAndPost(canvas);

			}

		}

	}

	/**
	 * 游戏没有结束时的移动逻辑判断
	 */
	private GameMode gameMod;

	public GameMode getGameMod() {
		return gameMod;
	}

	public void setGameMod(GameMode gameMod) {
		this.gameMod = gameMod;
	}

	public int getSpeed() {
		return speed;
	}

	// 滚动速度的设置
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	private List<RectBean> rowList = new ArrayList<RectBean>();
	private long changCount;// 递增一次速度
	private int speed;// 下移的速度

	private static final int DRAW_COUNT = 60 * 2;
	private static final int SPEED_ADD = 1;
	private static final int MAX_SPEED = 48;
	private static final int MIN_SPEED = 30;
	private static final int SHOW__DELAY = 600;
	private boolean gameStart = false;
  

	// 消息处理器
	private static final int HANLDER_GAMEOVER = 1;
	private static final int COUNT = 30*5;
	 private static final int PB_SHOW_DALAY = 200;
	private int count = 0;
	private Handler handler = new Handler() {

	
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANLDER_GAMEOVER:				
				gameOverEevent.gameOver(score);
								
				if (gameMod==GameMode.BASE) {
					handler.removeMessages(COUNTTIMET);
					handler.removeMessages(HANLDER_GAMEOVER);
				}
				break;				
			case COUNTTIMET:
				//倒计时60秒
				if (count==0) {
					setProBar.setMax(COUNT);
				}
				if (count<COUNT) {
				  handler.sendEmptyMessageDelayed(COUNTTIMET,PB_SHOW_DALAY);
				}else if (count ==COUNT) {
					handler.sendEmptyMessage(HANLDER_GAMEOVER);
					handler.removeMessages(COUNTTIMET);				
				}			
				count++;
				setProBar.setProgress(count);			
				break;
			default:
				break;
			}

		}

		

	};

	private void moveLogic() {
		// 根据游戏模式选择移动逻辑
		// 游戏没有开始，则不执行移动逻辑
		switch (gameMod) {
		case CHANGSPEED:
			if (!gameStart) {
				return;
			}
			changCount--;
			// 每绘制多少次改变一次速度
			if (changCount <= 0) {
				speed += SPEED_ADD;
				changCount = DRAW_COUNT;
			}
			speed = speed < MAX_SPEED ? speed : MAX_SPEED;

		case ROLLDOWN:// 向下自由移动

			if (!gameStart) {
				return;
			}

			// 遍历每一个矩形，改变矩形的坐标，使之下移
			for (RectBean r : rectViews) {
				// 移动位置
				r.getRect().top = r.getRect().top + speed;
				r.getRect().bottom = r.getRect().bottom + speed;
				r.setColorRect(r.getRect());

				// 找到正在移出屏幕的一行,遍历，如果已经移出超过1/2的矩形高度而且黑块没有被踩中，则游戏结束

				if (r.getRect().top >= (rowCount - 1) * recViewHeight
						+ recViewHeight / 2) {
					if (r.getColor() == Color.BLACK
							&& r.getStyle() == Style.FILL) {
						gameOver = true;
						playMusic.playGameOver();
						Message message = new Message();
						message.what = HANLDER_GAMEOVER;
						handler.sendMessageDelayed(message, SHOW__DELAY);
						r.startTwinkle(System.currentTimeMillis());
					}
				}

				// 移出屏幕的矩形进行删除
				if (r.getRect().top >= rowCount * recViewHeight) {
					// 复用已经移出去屏幕的矩形

					// 如果移出屏幕的黑块没有踩中，游戏结束
					r.getRect().bottom = r.getRect().top - rowCount
							* recViewHeight;
					r.getRect().top = r.getRect().bottom - recViewHeight;
					r.setStyle(Style.STROKE);
					r.setColor(Color.BLACK);
					rowList.add(r);
				}
			}
			// 已经移出屏幕的一行，再行随机一个黑块，然后出现到屏幕最上方
			if (rowList.size() > 0) {
				rowList.get(getRandom(columnCount)).setStyle(Style.FILL);
				rowList.clear();
			}

			break;
		case BASE:
		default:
			
			if (isMove) {
				for (RectBean r : rectViews) {
					// 移动位置
					r.getRect().top = r.getRect().top + recViewHeight;
					r.getRect().bottom = r.getRect().bottom + recViewHeight;
					r.setColorRect(r.getRect());
					// 移出屏幕的矩形利用到顶部
					if (r.getRect().top >= rowCount * recViewHeight) {
						r.getRect().bottom = 0 * recViewHeight;
						r.getRect().top = -recViewHeight;
						r.setColorRect(r.getRect());
						r.setStyle(Style.STROKE);
						r.setColor(Color.BLACK);
						rowList.add(r);
					}
				}
				// 删除移出屏幕的矩形
				// 已经移出屏幕下端的一行，再行随机一个黑块，然后出现到屏幕最上方
				if (rowList.size() > 0) {
					rowList.get(getRandom(columnCount)).setStyle(Style.FILL);
					rowList.clear();
				}
				isMove = false;
			}
			break;
		}
		// 原始模式，按中一个黑色块，下移一行
	}
	
	List<RectBean> removeRecs = new ArrayList<RectBean>();
	private Paint paint = new Paint();
	private boolean isMove = false;
	private RectBean redRect;
	
	public int getRandom(int range) {
		return new Random().nextInt(range);

	}

	/**
	 * 由AVTIVITY传递进来
	 * 
	 * @param event
	 *            触摸事件
	 */
	private Point touchPoint = new Point();
	private boolean startPlay = false;
	private boolean gameOver = false;
	private int score = 0;
	private int MYGRAY = 0xFFCCCCCC;
	private static final int COUNTTIMET = 5;
	

	public void handlerTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			if (!gameOver) {// 游戏不结束前不改状态
				// 获取触摸点
				touchPoint.x = (int) event.getRawX();
				touchPoint.y = (int) event.getRawY();
				for (RectBean rectBean : rectViews) {
					if (rectBean.isTouch(touchPoint)) {
						// 踩中第三行的黑块才开始,只执行一次
						if (rectBean.getColor() == Color.BLACK
								&& rectBean.getStyle() == Style.FILL
								&& rectBean.getRect().bottom == (rowCount - 1)
										* recViewHeight&&!gameStart) {
							startPlay = true;						
							//如果是基础模式则开始计时	
							if (gameMod==GameMode.BASE) {
								handler.sendEmptyMessage(COUNTTIMET);
							}																		
						}

						// 游戏开始才执行下面的判断逻辑
						if (startPlay) {
							if (rectBean.getColor() == Color.BLACK
									&& rectBean.getStyle() == Style.FILL) {
								isMove = true;
								score += 10;
								rectBean.setColor(MYGRAY);
								rectBean.setStyle(Style.FILL);
								playMusic.playPlayingMusci();
								gameStart = true;
							} else if (rectBean.getColor() == Color.BLACK
									&& rectBean.getStyle() == Style.STROKE) {
								rectBean.setColor(Color.RED);
								rectBean.setStyle(Style.FILL);
								System.out.println("游戏结束");
								gameOver = true;
								// 触发游戏结束边框
								redRect = rectBean;
								redRect.startTwinkle(System.currentTimeMillis());
								Message message = new Message();
								message.what = HANLDER_GAMEOVER;
								handler.sendMessageDelayed(message, SHOW__DELAY);
								playMusic.playGameOver();
							
							}

						}

						break;// 结束循环
					}
				}
			}

			break;
		case MotionEvent.ACTION_UP:

			break;
		}

	}

	/**
	 * 游戏结束后的干什么的接口,给activity处理
	 */
	private setGameOverEevent gameOverEevent;

	public setGameOverEevent getGameOverEevent() {
		return gameOverEevent;
	}

	public void setGameOverEevent(setGameOverEevent gameOverEevent) {
		this.gameOverEevent = gameOverEevent;
	}

	/**
	 * 
	 * @Description 定义一个接口，给ACTIVITY处理
	 * @author suyxin
	 * @time 2:39:48 PM Aug 8, 2015
	 */
	public interface setGameOverEevent {

		public void gameOver(int score);

	}
	private setProgressBar setProBar;
	
	public setProgressBar getSetProBar() {
		return setProBar;
	}

	public void setSetProBar(setProgressBar setProBar) {
		this.setProBar = setProBar;
	}

	/*
	 * 设置进度条接口
	 */
	public interface setProgressBar{
		
		public void setMax(long max);
		public void setProgress(long progress);
	}
	
	private playPanioMusic playMusic;
	
	public playPanioMusic getPlayMusic() {
		return playMusic;
	}

	public void setPlayMusic(playPanioMusic playMusic) {
		this.playMusic = playMusic;
	}

	public interface playPanioMusic{
		
		public void playGameOver();
		public void playPlayingMusci();
		
	}

}
