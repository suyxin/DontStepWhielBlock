package com.example.DontStepWhielBlock.GameView;

import android.R.color;
import android.R.style;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.nfc.tech.IsoDep;

/**
 * @Description
 * @author suyxin
 * @time 12:30:16 AM Aug 7, 2015
 */

public class RectBean {

	private Rect rect;// 绘绘制边框
	private Style style;
	private Rect colorRect;// 绘制矩形内部颜色
	private int color;
	private long startTime;// 闪亮的开始的时间
	private boolean istwinkel = false;

	public RectBean() {
		super();
		this.colorRect = new Rect();
	}

	public boolean isIstwinkel() {
		return istwinkel;
	}

	public void setIstwinkel(boolean istwinkel) {
		this.istwinkel = istwinkel;
	}

	public Rect getColorRect() {
		return colorRect;
	}

	/**
	 * 绘制颜色矩形
	 * @param rect void
	 */
	public void setColorRect(Rect rect) {

		this.colorRect.set(rect.left + strokeWidth, rect.top + strokeWidth,
				rect.right - strokeWidth, rect.bottom - strokeWidth);
	}

	/**
	 * 绘制边框
	 * @return Rect
	 */
	public Rect getRect() {
		return rect;
	}

	private int strokeWidth = 1;

	public void setRect(Rect rect) {

		this.rect = rect;

	}

	public Style getStyle() {
		return style;
	}

	public void setStyle(Style style) {
		this.style = style;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	/**
	 * 是否被触摸到的检测事件
	 * 
	 * @param touchPoint
	 *            屏幕的触摸点的坐标
	 * @return boolean
	 */
	public boolean isTouch(Point touchPoint) {

		return (rect.contains(touchPoint.x, touchPoint.y));

	}

	private int startColor;
	private Style startStyle;

	public void startTwinkle(long startTime) {

		this.startTime = startTime;
		this.istwinkel = true;
		this.startColor = this.color;
		this.startStyle = this.style;

	}

	private boolean change = false;
	private static int changeCount = 5;

	/**
	 * 每隔500秒改变一次颜色
	 * 
	 * @param currentTime
	 *            void
	 */

	public void checkTwinkle(long currentTime) {

		if (currentTime - startTime >= 200) {
			this.startTime = currentTime;
			if (change) {
				this.color = startColor;
				this.style = startStyle;
				change = !change;
			} else {
				this.color = Color.BLACK;
				this.style = Style.STROKE;
				change = !change;
			}

		}
	}
}
