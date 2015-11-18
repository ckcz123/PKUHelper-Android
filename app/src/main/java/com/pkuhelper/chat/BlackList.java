package com.pkuhelper.chat;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import org.json.JSONObject;

import java.util.ArrayList;

public class BlackList {
	private static String toUid = "";

	public static boolean isInBlackList(String username) {
		return ChatActivity.chatActivity.blackList.contains(username);
	}

	public static boolean isInBlackList() {
		return isInBlackList(ChatActivity.toUid);
	}

	@SuppressWarnings("unchecked")
	public static void moveIn(String username) {
		toUid = username;
		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("token", Constants.token));
		arrayList.add(new Parameters("target", username));
		arrayList.add(new Parameters("type", "kill"));
		new RequestingTask(ChatActivity.chatActivity, "正在加入黑名单...", Constants.domain + "/services/msg.php",
				Constants.REQUEST_CHAT_BLACKLIST_MOVE_IN).execute(arrayList);
	}

	@SuppressWarnings("unchecked")
	public static void moveOut(String username) {
		toUid = username;
		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("token", Constants.token));
		arrayList.add(new Parameters("target", username));
		arrayList.add(new Parameters("type", "rescue"));
		new RequestingTask(ChatActivity.chatActivity, "正在移出黑名单...", Constants.domain + "/services/msg.php",
				Constants.REQUEST_CHAT_BLACKLIST_MOVE_OUT).execute(arrayList);
	}

	public static void finishRequest(int type, String string) {
		try {
			ChatActivity chatActivity = ChatActivity.chatActivity;
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(chatActivity, jsonObject.optString("msg", "操作失败"));
				return;
			}
			CustomToast.showSuccessToast(chatActivity, "操作成功！", 1500);
			if (type == Constants.REQUEST_CHAT_BLACKLIST_MOVE_OUT) {
				chatActivity.blackList.remove(toUid);
			} else if (type == Constants.REQUEST_CHAT_BLACKLIST_MOVE_IN) {
				chatActivity.blackList.add(toUid);
			}
			if (chatActivity.pageShowing == ChatActivity.PAGE_CHAT) {
				ChatDetail.realShowDetail();
			} else {
				chatActivity.invalidateOptionsMenu();
			}
		} catch (Exception e) {
		} finally {
			toUid = "";
		}
	}

}
