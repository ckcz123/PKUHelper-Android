package com.pkuhelper.lib;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DeanDecode {

	private static ArrayList<Template> arrayList = null;

	public static String decode(Drawable drawable) {
		if (drawable == null) return "";
		return decode(((BitmapDrawable) drawable).getBitmap());
	}

	public static String decode(Bitmap bitmap) {
		if (bitmap == null) return "";
		try {
			if (arrayList == null)
				arrayList = getList();
			// 从左到右，依次识别四个位置
			// 每次识别了一个后，就相当于把这个及之前的图像给切掉，只继续识别接下来的
			Score s1 = checkScore(arrayList, bitmap, 0);
			Score s2 = checkScore(arrayList, bitmap, s1.x + s1.template.width - 3);
			Score s3 = checkScore(arrayList, bitmap, s2.x + s2.template.width - 3);
			Score s4 = checkScore(arrayList, bitmap, s3.x + s3.template.width - 3);
			return "" + s1.template.c + s2.template.c + s3.template.c + s4.template.c;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 从横坐标为start的位置开始进行识别
	 *
	 * @param arrayList 模板字符串
	 * @param bitmap    图片
	 * @param start     开始位置
	 * @return 识别结果 {score, x, y, Template}
	 * @throws Exception
	 */
	private static Score checkScore(
			ArrayList<Template> arrayList, Bitmap bitmap, int start) throws Exception {
		int width = bitmap.getWidth(), height = bitmap.getHeight();

		ArrayList<Score> scoreList = new ArrayList<Score>();
		int size = arrayList.size();
		for (int i = 0; i < size; i++) {
			Template template = arrayList.get(i);
			int w = template.width, h = template.height;
			// 对每一个点进行评分
			for (int u = start; u < width - w; u++)
				for (int v = 0; v < height - h; v++) {
					Score score = Score.getScore(bitmap, template, u, v);
					if (score != null)
						scoreList.add(score);
				}
		}
		if (scoreList.size() == 0) return null;

		// 将结果按照横坐标排序
		Collections.sort(scoreList, new Comparator<Score>() {
			public int compare(Score o1, Score o2) {
				return o1.x - o2.x;
			}
		});

		// 找到最小的横坐标（作为最左边的字符）
		int minx = 9999;
		for (int i = 0; i < scoreList.size(); i++) {
			if (scoreList.get(i).x < minx)
				minx = scoreList.get(i).x;
		}

		Score score = null;
		for (int i = 0; i < scoreList.size(); i++) {
			Score s = scoreList.get(i);
			if (s.x <= minx + 4) {
				if (score == null) score = s;
				else if (score.score < s.score)
					score = s;
			}
		}
		return score;

	}

	/**
	 * 初始化录入所有的模板点
	 *
	 * @return
	 */
	private static ArrayList<Template> getList() throws Exception {
		ArrayList<Template> arrayList = new ArrayList<Template>();

		arrayList.add(new Template('1', 6, 10, new int[]{2, 9, 101, 102, 109, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 409, 509}));
		arrayList.add(new Template('1', 5, 10, new int[]{2, 9, 101, 109, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 309, 409}));
		arrayList.add(new Template('2', 8, 10, new int[]{2, 9, 101, 102, 108, 109, 200, 201, 207, 208, 209, 300, 306, 307, 309, 400, 405, 406, 409, 500, 501, 504, 505, 509, 601, 602, 603, 604, 609, 702, 703, 709}));
		arrayList.add(new Template('2', 6, 10, new int[]{1, 2, 7, 8, 9, 100, 106, 109, 200, 205, 209, 300, 304, 309, 400, 404, 409, 501, 502, 503, 509}));
		arrayList.add(new Template('3', 8, 10, new int[]{1, 8, 100, 101, 108, 109, 200, 209, 300, 304, 309, 400, 404, 409, 500, 501, 503, 504, 505, 508, 509, 601, 602, 603, 605, 606, 607, 608, 702, 706, 707}));
		arrayList.add(new Template('3', 6, 10, new int[]{1, 2, 7, 8, 100, 109, 200, 204, 209, 300, 304, 309, 400, 404, 409, 501, 502, 503, 505, 506, 507, 508}));
		arrayList.add(new Template('4', 8, 10, new int[]{5, 6, 104, 105, 106, 203, 204, 206, 302, 303, 306, 401, 402, 406, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609, 706}));
		arrayList.add(new Template('4', 6, 10, new int[]{4, 5, 6, 103, 106, 202, 206, 301, 306, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 506}));
		arrayList.add(new Template('5', 8, 10, new int[]{0, 1, 2, 3, 4, 7, 100, 101, 102, 103, 104, 107, 108, 200, 204, 208, 209, 300, 303, 309, 400, 403, 409, 500, 503, 504, 508, 509, 600, 604, 605, 606, 607, 608, 705, 706, 707}));
		arrayList.add(new Template('5', 6, 10, new int[]{0, 1, 2, 3, 4, 8, 100, 104, 109, 200, 204, 209, 300, 304, 309, 400, 404, 409, 500, 505, 506, 507, 508}));
		arrayList.add(new Template('6', 8, 10, new int[]{2, 3, 4, 5, 6, 7, 101, 102, 103, 104, 105, 106, 107, 108, 200, 201, 205, 208, 209, 300, 304, 309, 400, 404, 409, 500, 501, 504, 505, 508, 509, 601, 602, 605, 606, 607, 608, 706, 707}));
		arrayList.add(new Template('6', 6, 10, new int[]{2, 3, 4, 5, 6, 7, 8, 101, 104, 109, 200, 204, 209, 300, 304, 309, 400, 404, 409, 505, 506, 507, 508}));
		arrayList.add(new Template('7', 8, 10, new int[]{0, 8, 9, 100, 107, 108, 109, 200, 206, 207, 300, 305, 306, 400, 404, 405, 500, 503, 504, 600, 601, 602, 603, 700, 701, 702}));
		arrayList.add(new Template('7', 6, 10, new int[]{0, 100, 200, 300, 306, 307, 308, 309, 400, 403, 404, 405, 500, 501, 502}));
		arrayList.add(new Template('8', 8, 10, new int[]{2, 6, 7, 101, 102, 103, 105, 106, 107, 108, 200, 201, 203, 204, 205, 208, 209, 300, 304, 309, 400, 404, 409, 500, 501, 503, 504, 505, 508, 509, 601, 602, 603, 605, 606, 607, 608, 702, 706, 707}));
		arrayList.add(new Template('8', 6, 10, new int[]{1, 2, 3, 5, 6, 7, 8, 100, 104, 109, 200, 204, 209, 300, 304, 309, 400, 404, 409, 501, 502, 503, 505, 506, 507, 508}));
		arrayList.add(new Template('9', 8, 10, new int[]{2, 3, 101, 102, 103, 104, 107, 108, 200, 201, 204, 205, 208, 209, 300, 305, 309, 400, 405, 409, 500, 501, 504, 508, 509, 601, 602, 603, 604, 605, 606, 607, 608, 702, 703, 704, 705, 706, 707}));
		arrayList.add(new Template('9', 6, 10, new int[]{1, 2, 3, 100, 104, 109, 200, 204, 209, 300, 304, 309, 400, 404, 408, 501, 502, 503, 504, 505, 506, 507}));
		arrayList.add(new Template('A', 8, 10, new int[]{3, 4, 5, 6, 7, 8, 9, 102, 103, 104, 105, 106, 107, 108, 109, 201, 202, 206, 300, 301, 306, 400, 401, 406, 501, 502, 506, 602, 603, 604, 605, 606, 607, 608, 609, 703, 704, 705, 706, 707, 708, 709}));
		arrayList.add(new Template('A', 6, 10, new int[]{2, 3, 4, 5, 6, 7, 8, 9, 101, 105, 200, 205, 300, 305, 401, 405, 502, 503, 504, 505, 506, 507, 508, 509}));
		arrayList.add(new Template('B', 8, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 200, 204, 209, 300, 304, 309, 400, 404, 409, 500, 501, 503, 504, 505, 508, 509, 601, 602, 603, 605, 606, 607, 608, 702, 706, 707}));
		arrayList.add(new Template('B', 6, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 104, 109, 200, 204, 209, 300, 304, 309, 400, 404, 409, 501, 502, 503, 505, 506, 507, 508}));
		arrayList.add(new Template('C', 8, 10, new int[]{2, 3, 4, 5, 6, 7, 101, 102, 103, 104, 105, 106, 107, 108, 200, 201, 208, 209, 300, 309, 400, 409, 500, 509, 600, 601, 608, 609, 701, 702, 707, 708}));
		arrayList.add(new Template('C', 6, 10, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 100, 109, 200, 209, 300, 309, 400, 409, 501, 508}));
		arrayList.add(new Template('D', 8, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 200, 209, 300, 309, 400, 409, 500, 501, 508, 509, 601, 602, 603, 604, 605, 606, 607, 608, 702, 703, 704, 705, 706, 707}));
		arrayList.add(new Template('D', 6, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 109, 200, 209, 300, 309, 401, 408, 502, 503, 504, 505, 506, 507}));
		arrayList.add(new Template('H', 8, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 204, 304, 404, 504, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609, 700, 701, 702, 703, 704, 705, 706, 707, 708, 709}));
		arrayList.add(new Template('H', 6, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 104, 204, 304, 404, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509}));
		arrayList.add(new Template('J', 6, 10, new int[]{7, 8, 108, 109, 200, 209, 300, 308, 309, 400, 401, 402, 403, 404, 405, 406, 407, 408, 500, 501, 502, 503, 504, 505, 506, 507}));
		arrayList.add(new Template('J', 6, 10, new int[]{7, 8, 109, 209, 300, 309, 400, 401, 402, 403, 404, 405, 406, 407, 408, 500}));
		arrayList.add(new Template('K', 8, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 204, 205, 303, 304, 305, 306, 402, 403, 406, 407, 501, 502, 507, 508, 600, 601, 608, 609, 700, 709}));
		arrayList.add(new Template('K', 6, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 104, 105, 203, 206, 302, 307, 401, 408, 500, 509}));
		arrayList.add(new Template('L', 7, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 209, 309, 409, 509, 609}));
		arrayList.add(new Template('L', 6, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 109, 209, 309, 409, 509}));
		arrayList.add(new Template('M', 8, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 201, 202, 302, 303, 304, 305, 402, 403, 404, 405, 501, 502, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609, 700, 701, 702, 703, 704, 705, 706, 707, 708, 709}));
		arrayList.add(new Template('M', 7, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 101, 102, 203, 204, 305, 306, 403, 404, 501, 502, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609}));
		arrayList.add(new Template('N', 8, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 201, 202, 203, 302, 303, 304, 305, 404, 405, 406, 506, 507, 508, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609, 700, 701, 702, 703, 704, 705, 706, 707, 708, 709}));
		arrayList.add(new Template('N', 6, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 101, 102, 203, 204, 305, 306, 407, 408, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509}));
		//arrayList.add(new Template('P', 8, 10, new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 200, 204, 300, 304, 400, 404, 500, 504, 600, 601, 602, 603, 604, 701, 702, 703}));
		arrayList.add(new Template('P', 6, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 104, 200, 204, 300, 304, 400, 404, 501, 502, 503}));
		arrayList.add(new Template('R', 8, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 200, 204, 205, 300, 304, 305, 400, 404, 405, 406, 500, 504, 505, 506, 507, 600, 601, 602, 603, 604, 607, 608, 609, 701, 702, 703, 708, 709}));
		arrayList.add(new Template('R', 6, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 104, 200, 204, 300, 304, 305, 400, 404, 406, 407, 501, 502, 503, 508, 509}));
		arrayList.add(new Template('T', 8, 10, new int[]{0, 100, 200, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 500, 600, 700}));
		arrayList.add(new Template('T', 7, 10, new int[]{0, 100, 200, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 400, 500, 600}));
		arrayList.add(new Template('U', 8, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 100, 101, 102, 103, 104, 105, 106, 107, 108, 208, 209, 309, 409, 508, 509, 600, 601, 602, 603, 604, 605, 606, 607, 608, 700, 701, 702, 703, 704, 705, 706, 707}));
		arrayList.add(new Template('U', 6, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 109, 209, 309, 409, 500, 501, 502, 503, 504, 505, 506, 507, 508}));
		arrayList.add(new Template('V', 8, 10, new int[]{0, 1, 2, 100, 101, 102, 103, 104, 105, 203, 204, 205, 206, 207, 306, 307, 308, 309, 406, 407, 408, 409, 503, 504, 505, 506, 507, 600, 601, 602, 603, 604, 605, 700, 701, 702}));
		arrayList.add(new Template('V', 7, 10, new int[]{0, 1, 2, 103, 104, 105, 206, 207, 308, 309, 406, 407, 503, 504, 505, 600, 601, 602}));
		arrayList.add(new Template('W', 8, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 207, 208, 304, 305, 306, 307, 404, 405, 406, 407, 507, 508, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609, 700, 701, 702, 703, 704, 705, 706, 707, 708, 709}));
		arrayList.add(new Template('W', 7, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 109, 207, 208, 303, 304, 305, 306, 407, 408, 509, 600, 601, 602, 603, 604, 605, 606, 607, 608}));
		arrayList.add(new Template('X', 8, 10, new int[]{0, 1, 8, 9, 100, 101, 102, 107, 108, 109, 202, 203, 206, 207, 303, 304, 305, 306, 403, 404, 405, 406, 502, 503, 506, 507, 600, 601, 602, 607, 608, 609, 700, 701, 708, 709}));
		arrayList.add(new Template('X', 6, 10, new int[]{0, 1, 8, 9, 102, 103, 106, 107, 204, 205, 304, 305, 402, 403, 406, 407, 500, 501, 508, 509}));
		arrayList.add(new Template('Y', 8, 10, new int[]{0, 1, 100, 101, 102, 202, 203, 303, 304, 305, 306, 307, 308, 309, 403, 404, 405, 406, 407, 408, 409, 502, 503, 600, 601, 602, 700, 701}));
		arrayList.add(new Template('Y', 7, 10, new int[]{0, 1, 102, 103, 204, 305, 306, 307, 308, 309, 404, 502, 503, 600, 601}));

		return arrayList;

	}

}

class Score {
	int x, y;
	double score;
	Template template;

	public Score(int _x, int _y, double _score, Template _temp) {
		x = _x;
		y = _y;
		score = _score;
		template = _temp;
	}

	/**
	 * 评分函数，其实就是看该点能否完全匹配模板中的所有点
	 *
	 * @param bitmap
	 * @param template
	 * @param x
	 * @param y
	 * @return
	 */
	@SuppressLint("UseSparseArrays")
	public static Score getScore(Bitmap bitmap, Template template,
								 int x, int y) throws Exception {
		int width = bitmap.getWidth(), height = bitmap.getHeight();
		ArrayList<Integer> arrayList = template.arrayList;
		int size = arrayList.size();
		// 背景色
		int defcolor = Color.parseColor("#beb4b4");

		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		for (int i = 0; i < size; i++) {
			// 对每个点，如果是背景色（不匹配），则认为是不合格的
			int point = arrayList.get(i);
			int xx = x + point / 100;
			int yy = y + point % 100;
			if (xx < width && yy < height) {
				int color = bitmap.getPixel(xx, yy);
				if (color == defcolor) return null;
				Integer cnt = map.get(color);
				if (cnt == null) cnt = 0;
				map.put(color, cnt + 1);
			} else return null;
		}
		// get main color
		int maincolor = 0, cnt = 0;
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			if (cnt < entry.getValue()) {
				cnt = entry.getValue();
				maincolor = entry.getKey();
			}
		}

		cnt = 0;
		for (int i = 0; i < template.width; i++)
			for (int j = 0; j < template.height; j++) {
				int xx = x + i, yy = y + j;
				if (xx < width && yy < height) {
					int color = bitmap.getPixel(xx, yy);
					if (color == maincolor) {
						if (arrayList.contains(100 * i + j)) cnt++;
						else cnt--;
					}
				}
			}
		return new Score(x, y, (cnt + 0.0) / size, template);
	}

}

class Template {
	char c;
	int width;
	int height;
	ArrayList<Integer> arrayList;

	public Template(char _c, int _width, int _height,
					int[] data) throws Exception {
		c = _c;
		width = _width;
		height = _height;
		int size = data.length;
		arrayList = new ArrayList<Integer>();
		for (int i = 0; i < size; i++)
			arrayList.add(data[i]);
	}

	public Template(char _c, int _width, int _height,
					ArrayList<Integer> list) throws Exception {
		c = _c;
		width = _width;
		height = _height;
		arrayList = list;
	}
}
