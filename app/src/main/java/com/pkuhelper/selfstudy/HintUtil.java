package com.pkuhelper.selfstudy;

import java.util.Random;

import android.content.Context;

public class HintUtil {
	
	private Context context;
	private static String[] hint0 = new String[]{
		"今天按下的关闭app，都是明天错过的满G4.0",
		"在关掉我之前，想想即将到来的ddl，如果你还是坚持离开我，那我……也无话可说",
		"当初说好的一心学习，原来只有我一个当真",
		"身为学渣，你没有点关闭的资格",
		"主人，你真的要让奴家去死嘛（哭）"
	};
	private static String[] hint1 = new String[]{
		"你已坚持了x分钟，胜利在望啊！",
		"主人你已坚持x分钟，我好崇拜你，快让我来为你捏捏肩捶捶腿～"
	};
	private static String[] hint2 = new String[]{
		"欢迎踏上学霸的征程，满G已经等你很久了",
		"终于等到你，还好我没放弃，欢迎开始学霸养成计划！",
		"你已进入“学神的秘密花园”，酷爱让我来蹂躏你[微笑]"
	};
	private static String[] hint3 = new String[]{
		"成功晋级学霸！",
		"向学神致敬！"
	};

	public static String getString0() {
		return hint0[new Random().nextInt(hint0.length)];
	}
	public static String getString1(int duration) {
		return hint1[new Random().nextInt(hint1.length)].replace("x", duration + "");
	}
	public static String getString2() {
		return hint2[new Random().nextInt(hint2.length)];
	}
	public static String getString3() {
		return hint3[new Random().nextInt(hint3.length)];
	}
}
