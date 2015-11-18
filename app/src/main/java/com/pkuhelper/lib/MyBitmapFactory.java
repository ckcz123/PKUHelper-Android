package com.pkuhelper.lib;

import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureStroke;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.subactivity.SubActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;

public class MyBitmapFactory {

	public static byte[] getCompressedBitmapBytes(String imagePath, double size) {
		return getCompressedBitmapBytes(imagePath, size, false);
	}

	public static byte[] getCompressedBitmapBytes(String imagePath, double size,
												  boolean ispng) {
		return bitmapToArray(getCompressedBitmap(imagePath, size), size, ispng);
	}

	/**
	 * Get a compressed bitmap from file
	 *
	 * @param filePath
	 * @param size     1 for 100KB, 10 for 1MB, -1 for unlimited
	 * @return
	 */
	public static Bitmap getCompressedBitmap(String filePath, double size) {
		Bitmap bitmap = null;
		boolean outofmemory = true;
		BitmapFactory.Options options = new BitmapFactory.Options();
		int inSampleSize = 0;
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		while (outofmemory) {
			try {
				options.inSampleSize = ++inSampleSize;
				output.reset();
				bitmap = BitmapFactory.decodeFile(filePath, options);
				if (size > 0) {
					bitmap.compress(CompressFormat.JPEG, 100, output);
					if (output.toByteArray().length > 102400 * size) {
						outofmemory = true;
						bitmap.recycle();
						bitmap = null;
						continue;
					}
				}
				outofmemory = false;
			} catch (Exception e) {
				e.printStackTrace();
				bitmap = null;
				outofmemory = false;
			} catch (OutOfMemoryError err) {
				outofmemory = true;
				try {
					bitmap.recycle();
				} catch (Exception e) {
				}
				System.gc();
			}
		}

		return bitmap;
	}

	public static byte[] bitmapToArray(Bitmap bitmap) {
		return MyBitmapFactory.bitmapToArray(bitmap, -1, false);
	}

	public static byte[] bitmapToArray(Bitmap bitmap, boolean ispng) {
		return MyBitmapFactory.bitmapToArray(bitmap, -1, ispng);
	}

	public static byte[] bitmapToArray(Bitmap bitmap, double size) {
		return MyBitmapFactory.bitmapToArray(bitmap, size, false);
	}

	public static byte[] bitmapToArray(Bitmap bitmap, double size, boolean ispng) {
		if (bitmap == null) return null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int options = 100;
		CompressFormat format = ispng ? CompressFormat.PNG : CompressFormat.JPEG;
		bitmap.compress(format, 100, output);
		while (size > 0 && output.toByteArray().length > 102400 * size) {
			options -= 10;
			output.reset();
			bitmap.compress(format, options, output);
		}
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean bitmapToFile(File file, Bitmap bitmap) {
		return bitmapToFile(file, bitmap, -1, false);
	}

	public static boolean bitmapToFile(File file, Bitmap bitmap, double size) {
		return bitmapToFile(file, bitmap, size, false);
	}

	public static boolean bitmapToFile(File file, Bitmap bitmap, double size, boolean ispng) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			byte[] bts = bitmapToArray(bitmap, size, ispng);
			fileOutputStream.write(bts);
			fileOutputStream.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void showBitmap(Context context, Bitmap bitmap) {
		File file = MyFile.getCache(context, bitmap.toString());
		if (bitmapToFile(file, bitmap)) {
			Intent intent = new Intent(context, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_FILE);
			intent.putExtra("file", file.getAbsolutePath());
			context.startActivity(intent);
		} else {
			CustomToast.showErrorToast(context, "无法打开图片！");
		}
	}

	public static String decodeQRCode(Bitmap bitmap) {
		try {
			Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
			Collection<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
			decodeFormats.addAll(EnumSet.of(BarcodeFormat.QR_CODE));
			hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
			hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
			int lWidth = bitmap.getWidth();
			int lHeight = bitmap.getHeight();
			int[] lPixels = new int[lWidth * lHeight];
			bitmap.getPixels(lPixels, 0, lWidth, 0, 0, lWidth, lHeight);
			BinaryBitmap binaryBitmap = new BinaryBitmap(
					new HybridBinarizer(new RGBLuminanceSource(lWidth, lHeight, lPixels)));
			Result lResult = new MultiFormatReader().decode(binaryBitmap, hints);
			return lResult.getText().trim();
		} catch (Exception e) {
			return "";
		} catch (OutOfMemoryError err) {
			return "";
		} finally {
			System.gc();
		}
	}

	public static Bitmap generateQRCode(String string, int width, int height) {
		try {
			if (string == null) return null;
			string = string.trim();
			if ("".equals(string))
				return null;
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new QRCodeWriter().encode(string, BarcodeFormat.QR_CODE, width, height, hints);
			int[] pixels = new int[width * height];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (bitMatrix.get(x, y))
						pixels[y * width + x] = 0xff000000;
					else
						pixels[y * width + x] = 0xffffffff;
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (Exception e) {
			return null;
		}
	}

	public static Bitmap generateQRCode(String string, int width) {
		return generateQRCode(string, width, width);
	}

	public static Bitmap generateQRCode(String string) {
		return generateQRCode(string, 250);
	}

	public static Bitmap gestureToBitmap(Gesture gesture, int color, float strokeWidth) {
		boolean BITMAP_RENDERING_ANTIALIAS = true;
		boolean BITMAP_RENDERING_DITHER = true;
		float BITMAP_RENDERING_WIDTH = 10;
		int NUM_SAMPLES = 20;
		RectF bounds = gesture.getBoundingBox();
		int width = (int) bounds.width() + 8;
		int height = (int) bounds.height() + 8;
		final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		int edge = 4;
		canvas.translate(edge, edge);
		final Paint paint = new Paint();
		paint.setAntiAlias(BITMAP_RENDERING_ANTIALIAS);
		paint.setDither(BITMAP_RENDERING_DITHER);
		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(BITMAP_RENDERING_WIDTH);
		final ArrayList<GestureStroke> strokes = gesture.getStrokes();
		final int count = strokes.size();
		for (int i = 0; i < count; i++) {
			Path path = strokes.get(i).toPath(width - 2 * edge, height - 2 * edge, NUM_SAMPLES);
			canvas.drawPath(path, paint);
		}
		return bitmap;
	}

}
