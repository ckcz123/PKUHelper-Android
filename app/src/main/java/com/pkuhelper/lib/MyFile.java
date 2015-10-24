package com.pkuhelper.lib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Base64;

import com.pkuhelper.lib.webconnection.WebConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

public class MyFile {

	private static boolean useSDCard = true;

	public static void setUseSDCard(boolean use) {
		setUseSDCard(use, null);
	}

	public static void setUseSDCard(boolean use, final Activity activity) {
		useSDCard = use;
		if (use && activity != null) {
			try {
				if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					new AlertDialog.Builder(activity).setTitle("没有SD卡！")
							.setMessage("未检测到有效的SD卡，请插入SD卡后重试")
							.setCancelable(true).setPositiveButton("关闭", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							activity.finish();
						}
					}).setOnCancelListener(new DialogInterface.OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							// TODO Auto-generated method stub
							activity.finish();
						}
					}).show();
				}
			} catch (Exception e) {
			}
		}
	}

	private static File getFilesDir(Context context) {
		return useSDCard && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())?
				context.getExternalFilesDir(null) : context.getFilesDir();
	}

	public static File getFile(Context context, String _dir, String _name) {
		String dir = new String(_dir == null ? "" : _dir).trim(),
				name = new String(_name == null ? "" : _name).trim();

		File file = "".equals(dir) ? getFilesDir(context) : new File(getFilesDir(context), dir);
		if (file.exists() && file.isFile()) file.delete();
		file.mkdirs();
		return "".equals(name) ? file : new File(file, name);
	}

	public static String getString(Context context,
								   String username, String name,
								   String defaultString) throws Exception {
		File file = getFile(context, username, name);
		if (!file.exists()) {
			file.createNewFile();
			if (defaultString != null) {
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				fileOutputStream.write(Base64.encode(defaultString.getBytes(), Base64.DEFAULT));
				fileOutputStream.close();
			}
		}
		if (file.length() == 0) return "";
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] bytes = new byte[(int) file.length()];
		fileInputStream.read(bytes);
		fileInputStream.close();
		return new String(Base64.decode(bytes, Base64.DEFAULT));
	}

	public static void putString(
			Context context, String username, String name, String stringToWrite)
			throws Exception {
		File file = getFile(context, username, name);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		fileOutputStream.write(Base64.encode(stringToWrite.getBytes(), Base64.DEFAULT));
		fileOutputStream.close();
	}

	public static File getCache(Context context, String name) {
		File cacheDir = useSDCard && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				? context.getExternalCacheDir() : context.getCacheDir();
		return name == null || "".equals(name) ? cacheDir : new File(cacheDir, name);
	}

	public static void deleteFile(String path) {
		deleteFile(new File(path));
	}

	public static void deleteFile(File file) {
		if (file == null || !file.exists()) return;
		try {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) {
					deleteFile(f);
				}
			}
			file.delete();
		} catch (Exception e) {
		}
	}

	public static boolean streamToFile(InputStream inputStream, File file) {
		File tmpFile = new File(file + "_tmp");
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
			byte[] bts = new byte[2048];
			int len;
			while ((len = inputStream.read(bts)) != -1)
				fileOutputStream.write(bts, 0, len);
			fileOutputStream.close();
			file.delete();
			return tmpFile.renameTo(file);
		} catch (Exception e) {
			tmpFile.delete();
			file.delete();
			return false;
		}
	}

	public static boolean urlToFile(String url, Context context) {
		return urlToFile(url, MyFile.getCache(context, Util.getHash(url)), false);
	}

	public static boolean urlToFile(String url, File file) {
		return urlToFile(url, file, false);
	}

	public static boolean urlToFile(String url, File file, boolean override) {
		if (!override && file != null && file.exists()) return true;
		try {
			return streamToFile(WebConnection.connect(url), file);
		} catch (Exception e) {
			return false;
		}
	}

	public static int getFileCount(File file) {
		if (file == null) return 0;
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			int cnt = 0;
			for (File f : files)
				cnt += getFileCount(f);
			return cnt;
		}
		return 1;
	}

	public static long getFileSize(File file) {
		if (file == null) return 0;
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			long size = 0;
			for (File f : files)
				size += getFileSize(f);
			return size;
		}
		return file.length();
	}

	public static String getFileSizeString(File file) {
		return formatFileSize(getFileSize(file));
	}

	public static String formatFileSize(long size) {
		double sz = size + 0.0;
		DecimalFormat decimalFormat = new DecimalFormat("#.00");
		if (size < 1024) return decimalFormat.format(sz) + "B";
		if (size < 1048576) return decimalFormat.format(sz / 1024) + "K";
		if (size < 1073741824) return decimalFormat.format(sz / 1048576) + "M";
		return decimalFormat.format(sz / 1073741824) + "G";
	}

	public static void clearCache(Context context) {
		File file = getCache(context, null);
		if (!file.exists() || !file.isDirectory()) return;
		File[] files = file.listFiles();
		for (File f : files)
			deleteFile(f);
	}

	public static boolean copyFile(String source, String destination) {
		return copyFile(new File(source), new File(destination));
	}

	public static boolean copyFile(File source, File destination) {
		if (source == null || destination == null) return false;
		if (!source.exists()) return false;
		try {
			destination.delete();
			destination.createNewFile();
			FileInputStream inputStream = new FileInputStream(source);
			FileOutputStream outputStream = new FileOutputStream(destination);
			FileChannel fileChannel = inputStream.getChannel();
			fileChannel.transferTo(0, fileChannel.size(), outputStream.getChannel());
			inputStream.close();
			outputStream.close();
			return true;
		} catch (Exception e) {
			destination.delete();
			return false;
		}
	}


}
