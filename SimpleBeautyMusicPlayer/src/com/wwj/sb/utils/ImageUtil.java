package com.wwj.sb.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * 图片工具类
 * @author wwj
 * 2013/7/3
 */
public class ImageUtil {
	/**图片的八个位置**/
	public static final int TOP = 0;			//上	
	public static final int BOTTOM = 1;			//下
	public static final int LEFT = 2;			//左
	public static final int RIGHT = 3;			//右
	public static final int LEFT_TOP = 4;		//左上
	public static final int LEFT_BOTTOM = 5;	//左下
	public static final int RIGHT_TOP = 6;		//右上
	public static final int RIGHT_BOTTOM = 7;	//右下
	
	/**
	 * 图像的放大缩小方法
	 * @param src		源位图对象
	 * @param scaleX	宽度比例系数
	 * @param scaleY	高度比例系数
	 * @return 返回位图对象
	 */
	public static Bitmap zoomBitmap(Bitmap src, float scaleX, float scaleY) {
		Matrix matrix = new Matrix();
		matrix.setScale(scaleX, scaleY);
		Bitmap t_bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
		return t_bitmap;
	}
	
	/**
	 * 图像放大缩小--根据宽度和高度
	 * @param src
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap zoomBimtap(Bitmap src, int width, int height) {
		return Bitmap.createScaledBitmap(src, width, height, true);
	}
	
	/**
	 * 将Drawable转为Bitmap对象
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		return ((BitmapDrawable)drawable).getBitmap();
	}
	
	
	/**
	 * 将Bitmap转换为Drawable对象
	 * @param bitmap
	 * @return
	 */
	public static Drawable bitmapToDrawable(Bitmap bitmap) {
		Drawable drawable = new BitmapDrawable(bitmap);
		return drawable;
	}
	
	/**
	 * Bitmap转byte[]
	 * @param bitmap
	 * @return
	 */
	public static byte[] bitmapToByte(Bitmap bitmap) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		return out.toByteArray();
	}
	
	/**
	 * byte[]转Bitmap
	 * @param data
	 * @return
	 */
	public static Bitmap byteToBitmap(byte[] data) {
		if(data.length != 0) {
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		return null;
	}
	
	/**
	 * 绘制带圆角的图像
	 * @param src
	 * @param radius
	 * @return
	 */
	public static Bitmap createRoundedCornerBitmap(Bitmap src, int radius) {
		final int w = src.getWidth();
		final int h = src.getHeight();
		// 高清量32位图
		Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Paint paint = new Paint();
		Canvas canvas = new Canvas(bitmap);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(0xff424242);
		// 防止边缘的锯齿
		paint.setFilterBitmap(true);
		Rect rect = new Rect(0, 0, w, h);
		RectF rectf = new RectF(rect);
		// 绘制带圆角的矩形
		canvas.drawRoundRect(rectf, radius, radius, paint);
		
		// 取两层绘制交集，显示上层
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		// 绘制图像
		canvas.drawBitmap(src, rect, rect, paint);
		return bitmap;
	}
	
	/**
	 * 创建选中带提示图片
	 * @param context
	 * @param srcId
	 * @param tipId
	 * @return
	 */
	public static Drawable createSelectedTip(Context context, int srcId, int tipId) {
		Bitmap src = BitmapFactory.decodeResource(context.getResources(), srcId);
		Bitmap tip = BitmapFactory.decodeResource(context.getResources(), tipId);
		final int w = src.getWidth();
		final int h = src.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Paint paint = new Paint();
		Canvas canvas = new Canvas(bitmap);
		//绘制原图
		canvas.drawBitmap(src, 0, 0, paint);
		//绘制提示图片
		canvas.drawBitmap(tip, (w - tip.getWidth()), 0, paint);
		return bitmapToDrawable(bitmap);
	}
	
	/**
	 * 带倒影的图像
	 * @param src
	 * @return
	 */
	public static Bitmap createReflectionBitmap(Bitmap src) {
		// 两个图像间的空隙
		final int spacing = 4;
		final int w = src.getWidth();
		final int h = src.getHeight();
		// 绘制高质量32位图
		Bitmap bitmap = Bitmap.createBitmap(w, h + h / 2 + spacing, Config.ARGB_8888);
		// 创建燕X轴的倒影图像
		Matrix m = new Matrix();
		m.setScale(1, -1);
		Bitmap t_bitmap = Bitmap.createBitmap(src, 0, h / 2, w, h / 2, m, true);
		
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		//	绘制原图像
		canvas.drawBitmap(src, 0, 0, paint);
		// 绘制倒影图像
		canvas.drawBitmap(t_bitmap, 0, h + spacing, paint);
		// 线性渲染-沿Y轴高到低渲染
		Shader shader = new LinearGradient(0, h + spacing, 0, h + spacing + h / 2, 0x70ffffff, 0x00ffffff, Shader.TileMode.MIRROR);
		paint.setShader(shader);
		// 取两层绘制交集，显示下层。
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// 绘制渲染倒影的矩形
		canvas.drawRect(0, h + spacing, w, h + h / 2 + spacing, paint);
		return bitmap;
	}
	
	
	/**
	 * 独立的倒影图像
	 * @param src
	 * @return
	 */
	public static Bitmap createReflectionBitmapForSingle(Bitmap src) {
		final int w = src.getWidth();
		final int h = src.getHeight();
		// 绘制高质量32位图
		Bitmap bitmap = Bitmap.createBitmap(w, h / 2, Config.ARGB_8888);
		// 创建沿X轴的倒影图像
		Matrix m = new Matrix();
		m.setScale(1, -1);
		Bitmap t_bitmap = Bitmap.createBitmap(src, 0, h / 2, w, h / 2, m, true);

		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		// 绘制倒影图像
		canvas.drawBitmap(t_bitmap, 0, 0, paint);
		// 线性渲染-沿Y轴高到低渲染	
		Shader shader = new LinearGradient(0, 0, 0, h / 2, 0x70ffffff,
				0x00ffffff, Shader.TileMode.MIRROR);
		paint.setShader(shader);
		// 取两层绘制交集。显示下层。
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// 绘制渲染倒影的矩形
		canvas.drawRect(0, 0, w, h / 2, paint);
		return bitmap;
	}
	
	
	public static Bitmap createGreyBitmap(Bitmap src) {
		final int w = src.getWidth();
		final int h = src.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		// 颜色变换的矩阵
		ColorMatrix matrix = new ColorMatrix();
		// saturation 饱和度值，最小可设为0，此时对应的是灰度图；为1表示饱和度不变，设置大于1，就显示过饱和
		matrix.setSaturation(0);
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
		paint.setColorFilter(filter);
		canvas.drawBitmap(src, 0, 0, paint);
		return bitmap;
	}
	
	/**
	 * 保存图片
	 * @param src
	 * @param filepath
	 * @param format:[Bitmap.CompressFormat.PNG,Bitmap.CompressFormat.JPEG]
	 * @return
	 */
	public static boolean saveImage(Bitmap src, String filepath, CompressFormat format) {
		boolean rs = false;
		File file = new File(filepath);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if(src.compress(format, 100, out)) {
				out.flush();	//写入流
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	/**
	 * 添加水印效果
	 * @param src		源位图
	 * @param watermark	水印
	 * @param direction	方向
	 * @param spacing 间距
	 * @return
	 */
	public static Bitmap createWatermark(Bitmap src, Bitmap watermark, int direction, int spacing) {
		final int w = src.getWidth();
		final int h = src.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(src, 0, 0, null);
		if(direction == LEFT_TOP) {
			canvas.drawBitmap(watermark, spacing, spacing, null);
		} else if(direction == LEFT_BOTTOM){
			canvas.drawBitmap(watermark, spacing, h - watermark.getHeight() - spacing, null);
		} else if(direction == RIGHT_TOP) {
			canvas.drawBitmap(watermark, w - watermark.getWidth() - spacing, spacing, null);
		} else if(direction == RIGHT_BOTTOM) {
			canvas.drawBitmap(watermark, w - watermark.getWidth() - spacing, h - watermark.getHeight() - spacing, null);
		}
		return bitmap;
	}
	
	
	/**
	 * 合成图像
	 * @param direction
	 * @param bitmaps
	 * @return
	 */
	public static Bitmap composeBitmap(int direction, Bitmap... bitmaps) {
		if(bitmaps.length < 2) {
			return null;
		}
		Bitmap firstBitmap = bitmaps[0];
		for (int i = 0; i < bitmaps.length; i++) {
			firstBitmap = composeBitmap(firstBitmap, bitmaps[i], direction);
		}
		return firstBitmap;
	}

	/**
	 * 合成两张图像
	 * @param firstBitmap
	 * @param secondBitmap
	 * @param direction
	 * @return
	 */
	private static Bitmap composeBitmap(Bitmap firstBitmap, Bitmap secondBitmap,
			int direction) {
		if(firstBitmap == null) {
			return null;
		}
		if(secondBitmap == null) {
			return firstBitmap;
		}
		final int fw = firstBitmap.getWidth();
		final int fh = firstBitmap.getHeight();
		final int sw = secondBitmap.getWidth();
		final int sh = secondBitmap.getHeight();
		Bitmap bitmap = null;
		Canvas canvas = null;
		if(direction == TOP) {
			bitmap = Bitmap.createBitmap(sw > fw ? sw : fw, fh + sh, Config.ARGB_8888);
			canvas = new Canvas(bitmap);
			canvas.drawBitmap(secondBitmap, 0, 0, null);
			canvas.drawBitmap(firstBitmap, 0, sh, null);
		} else if(direction == BOTTOM) {
			bitmap = Bitmap.createBitmap(fw > sw ? fw : sw, fh + sh, Config.ARGB_8888);
			canvas = new Canvas(bitmap);
			canvas.drawBitmap(firstBitmap, 0, 0, null);
			canvas.drawBitmap(secondBitmap, 0, fh, null);
		} else if(direction == LEFT) {
			bitmap = Bitmap.createBitmap(fw + sw, sh > fh ? sh : fh, Config.ARGB_8888);
			canvas = new Canvas(bitmap);
			canvas.drawBitmap(secondBitmap, 0, 0, null);
			canvas.drawBitmap(firstBitmap, sw, 0, null);
		} else if(direction == RIGHT) {
			bitmap = Bitmap.createBitmap(fw + sw, fh > sh ? fh : sh,
					Config.ARGB_8888);
			canvas = new Canvas(bitmap);
			canvas.drawBitmap(firstBitmap, 0, 0, null);
			canvas.drawBitmap(secondBitmap, fw, 0, null);
		}
		return bitmap;
	}
	
	
}
