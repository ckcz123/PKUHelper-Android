package com.pkuhelper.lib;

import java.util.ArrayList;

import com.pkuhelper.MYPKU;
import com.pkuhelper.Settings;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class Constants {
	public static final String version="2.0.3";
	public static final String update_time="2015-10-18";

	public static final String domain="http://www.xiongdianpku.com";
	
	public static final int REQUEST_REPORT=300;
	public static final int REQUEST_UPDATE=301;
	public static final int REQUEST_FEATURES_IMAGE=302;
	public static final int REQUEST_BACKGROUND_IMAGE=303;
	public static final int REQUEST_FOUND_USERNAME=304;
	public static final int REQUEST_IAAA=400;
	public static final int REQUEST_IAAA_TOKEN=401;
	public static final int REQUEST_IAAA_INFO=402;
	public static final int REQUEST_ITS_CONNECT=500;
	public static final int REQUEST_ITS_CONNECT_NO_FREE=501;
	public static final int REQUEST_ITS_DISCONNECT=502;
	public static final int REQUEST_ITS_DISCONNECT_ALL=503;
	public static final int REQUEST_ELECTIVE=600;
	public static final int REQUEST_ELECTIVE_TOKEN=601;
	public static final int REQUEST_ELECTIVE_COOKIE=602;
	public static final int REQUEST_ELECTIVE_COURSES=603;
	public static final int REQUEST_ELECTIVE_CUSTOM=604;
	public static final int REQUEST_DEAN_LOGIN=700;
	public static final int REQUEST_DEAN_GETTING_GRADE=701;
	public static final int REQUEST_CLASSROOM_LIST=800;
	public static final int REQUEST_CLASSROOM=801;
	public static final int REQUEST_SUBACTIVITY_WEBSITE=900;
	public static final int REQUEST_SUBACTIVITY_CALENDAR=901;
	public static final int REQUEST_SUBACTIVITY_LECTURE=902;
	public static final int REQUEST_SUBACTIVITY_LECTURE_DETAIL=903;
	public static final int REQUEST_SUBACTIVITY_SHOWS=904;
	public static final int REQUEST_SUBACTIVITY_SHOWS_DETAIL=905;
	public static final int REQUEST_SUBACTIVITY_MYMESSAGE=906;
	public static final int REQUEST_SUBACTIVITY_CERTIFICATION=907;
	public static final int REQUEST_SUBACTIVITY_PUSHES_GET=909;
	public static final int REQUEST_SUBACTIVITY_PUSHES_SET=910;
	public static final int REQUEST_SUBACTIVITY_CARD_AMOUNT=911;
	public static final int REQUEST_PE_TEST=1000;
	public static final int REQUEST_PE_CARD=1001;
	public static final int REQUEST_PKUHOLE_GET_PAGE=1100;
	public static final int REQUEST_PKUHOLE_GET_DETAIL_FINISHED=1101;
	public static final int REQUEST_PKUHOLE_GET_DETAIL_FAILED=1102;
	public static final int REQUEST_PKUHOLE_POST_MESSAGE=1103;
	public static final int REQUEST_PKUHOLE_POST_COMMENT=1104;
	public static final int REQUEST_NOTICECENTER_GETSOURCE=1200;
	public static final int REQUEST_NOTICECENTER_SAVESOURCE=1201;
	public static final int REQUEST_NOTICECENTER_GETCONTENT_ALL=1202;
	public static final int REQUEST_NOTICECENTER_GETCONTENT_ONE=1203;
	public static final int REQUEST_NOTICECENTER_COURSE_LOGIN=1204;
	public static final int REQUEST_NOTICECENTER_COURSE_GETDETAIL=1205;
	public static final int REQUEST_NOTICECENTER_COURSE_GETWEBSITE=1206;
	public static final int REQUEST_LOSTFOUND_GETLOST=1300;
	public static final int REQUEST_LOSTFOUND_GETFOUND=1301;
	public static final int REQUEST_LOSTFOUND_GETMINE=1302;
	public static final int REQUEST_LOSTFOUND_ADD=1303;
	public static final int REQUEST_LOSTFOUND_DELETE=1304;
	public static final int REQUEST_LOSTFOUND_GET=1305;
	public static final int REQUEST_CUSTOM_COURSE_GET=1400;
	public static final int REQUEST_CUSTOM_COURSE_SAVE=1401;
	public static final int REQUEST_CHAT_GET_LIST=1500;
	public static final int REQUEST_CHAT_GET_CONTENT=1501;
	public static final int REQUEST_CHAT_SEND_CONTENT=1502;
	public static final int REQUEST_CHAT_DELETE_LIST=1503;
	public static final int REQUEST_CHAT_DELETE_CONTENT=1504;
	public static final int REQUEST_CHAT_BLACKLIST_MOVE_IN=1505;
	public static final int REQUEST_CHAT_BLACKLIST_MOVE_OUT=1506;
	public static final int REQUEST_CHAT_GET_NC_SERVICES=1507;
	public static final int REQUEST_BBS_GET_TOP=1600;
	public static final int REQUEST_BBS_GET_ALL_BOARDS=1601;
	public static final int REQUEST_BBS_LOGIN=1602;
	public static final int REQUEST_BBS_GET_LIST=1603;
	public static final int REQUEST_BBS_GET_POST=1604;
	public static final int REQUEST_BBS_GET_QUOTE=1605;
	public static final int REQUEST_BBS_POST=1606;
	public static final int REQUEST_BBS_REPRINT=1607;
	public static final int REQUEST_BBS_GET_EDIT=1608;
	public static final int REQUEST_BBS_EDIT=1609;
	public static final int REQUEST_BBS_SEARCH=1610;
	public static final int REQUEST_BBS_GET_MAIL_LIST=1611;
	public static final int REQUEST_BBS_GET_MAIL=1612;
	public static final int REQUEST_BBS_POST_MAIL=1613;
	public static final int REQUEST_MEDIA_FETCH=1700;
	public static final int REQUEST_MEDIA_FETCH_ONE=1701;
	public static final int REQUEST_HOLE_GETLIST_ALL=1800;
	public static final int REQUEST_HOLE_GETLIST_MINE=1801;
	public static final int REQUEST_HOLE_GETCOMMENT=1802;
	public static final int REQUEST_HOLE_POST=1803;
	public static final int REQUEST_HOLE_POST_COMMENT=1804;
	public static final int REQUEST_HOLE_GET_SETTINGS=1805;
	public static final int REQUEST_HOLE_SET_SETTINGS=1806;
	public static final int REQUEST_HOLE_SET_ATTENTION=1807;
	public static final int REQUEST_HOLE_REPORT=1808;
	public static final int REQUEST_HOLE_SEARCH=1809;
	
	public static final int MENU_IPGW_SET_BACKGROUND=10000;
	public static final int MENU_QRCODE=10001;
	public static final int MENU_COURSE_ADD=10002;
	public static final int MENU_COURSE_REFRESH=10003;
	public static final int MENU_COURSE_SHARE=10004;
	public static final int MENU_MYPKU_SET=10005;
	public static final int MENU_CLASSROOM_SELECT=10100;
	public static final int MENU_PKUHOLE_ADD=10200;
	public static final int MENU_PKUHOLE_REFRESH=10201;
	public static final int MENU_PKUHOLE_SETTINGS=10202;
	public static final int MENU_PKUHOLE_ATTENSION=10203;
	public static final int MENU_PKUHOLE_REPORT=10204;
	public static final int MENU_PKUHOLE_SEARCH=10205;
	public static final int MENU_PKUHOLE_CLOSE=10206;
	public static final int MENU_SUBACTIVITY_SAVE=10300;
	public static final int MENU_SUBACTIVITY_REPLY=10301;
	public static final int MENU_SUBACTIVITY_REFRESH=10302;
	public static final int MENU_SUBACTIVITY_OPEN_IN_BROWSER=10303;
	public static final int MENU_SUBACTIVITY_SHARE=10304;
	public static final int MENU_SUBACTIVITY_SAVE_PICTURE=10305;
	public static final int MENU_SUBACTIVITY_CLOSE=10310;
	public static final int MENU_NOTICECENTER_SETSOURCE_SAVE=10400;
	public static final int MENU_NOTICECENTER_SHOWCONTENTS_CHOOSE=10401;
	public static final int MENU_NOTICECENTER_SHOWCONTENTS_SETTINGS=10402;
	public static final int MENU_LOSTFOUND_ADD=10500;
	public static final int MENU_LOSTFOUND_CHOOSE=10501;
	public static final int MENU_LOSTFOUND_SAVE=10502;
	public static final int MENU_LOSTFOUND_CLOSE=10510;
	public static final int MENU_CUSTOM_COURSE_ADD=10600;
	public static final int MENU_CUSTOM_COURSE_SAVE=10601;
	public static final int MENU_CUSTOM_COURSE_CLOSE=10602;
	public static final int MENU_EXAM_ADD=10700;
	public static final int MENU_EXAM_SAVE=10701;
	public static final int MENU_EXAM_CLOSE=10702;
	public static final int MENU_CHAT_ADD = 10800;
	public static final int MENU_CHAT_REFRESH = 10801;
	public static final int MENU_CHAT_CLOSE = 10802;
	public static final int MENU_CHAT_BLACKLIST = 10803;
	public static final int MENU_BBS_FAVORITE = 10900;
	public static final int MENU_BBS_REFRESH = 10901;
	public static final int MENU_BBS_VIEW_PREVIOUS = 10902;
	public static final int MENU_BBS_VIEW_NEXT = 10903;
	public static final int MENU_BBS_VIEW_POST = 10904;
	public static final int MENU_BBS_VIEW_SHARE = 10905;
	public static final int MENU_BBS_VIEW_JUMP = 10906;
	public static final int MENU_BBS_VIEW_EXIT = 10910;
	public static final int MENU_BBS_MESSAGE_POST = 10911;
	public static final int MENU_MEDIA_CHOOSE=11000;

	public static final int CONTEXT_MENU_MYPKU_SET = 15000;
	public static final int CONTEXT_MENU_CHAT_COPY = 15100;
	public static final int CONTEXT_MENU_CHAT_BLACKLIST = 15101;
	public static final int CONTEXT_MENU_CHAT_DELETE = 15102;
	public static final int CONTEXT_MENU_CHAT_GET_URL = 15103;
	// do not use 15104-15199 !!!
	public static final int CONTEXT_MENU_BBS_FAVORITE = 15200;
	public static final int CONTEXT_MENU_BBS_ATTACHES = 15201;
	public static final int CONTEXT_MENU_BBS_IMAGES = 15202;
	public static final int CONTEXT_MENU_BBS_POST = 15203;
	public static final int CONTEXT_MENU_BBS_MESSAGE = 15204;
	public static final int CONTEXT_MENU_BBS_REPRINT = 15205;
	public static final int CONTEXT_MENU_BBS_EDIT = 15206;
	public static final int CONTEXT_MENU_BBS_URL = 15207;
	// do not use 15208-15299 !!!
	public static final int CONTEXT_MENU_SUBACTIVITY_SHARE_PICTURE = 15300;
	public static final int CONTEXT_MENU_SUBACTIVITY_SAVE_PICTURE = 15301;
	public static final int CONTEXT_MENU_SUBACTIVITY_DECODE_PICTURE = 15302;
	
	public static final int MESSAGE_CHECK_CONNECTED_FINISHED = 20000;
	public static final int MESSAGE_SLEEP_FINISHED = 20001;
	public static final int MESSAGE_IMAGE_REQUEST_FINISHED = 20002;
	public static final int MESSAGE_IMAGE_REQUEST_FAILED = 20003;
	public static final int MESSAGE_OTHERS = 20004;
	public static final int MESSAGE_STATISTICS = 20005;
	public static final int MESSAGE_SERVICE_FINISHED = 20006;
	public static final int MESSAGE_DEAN_PICTURE_FINISHED = 20100;
	public static final int MESSAGE_DEAN_PICTURE_FAILED = 20101;
	public static final int MESSAGE_DEAN_DECODE_FINISHED = 20102;
	public static final int MESSAGE_PKUHOLE_LIST_MORE_FINISHED = 20200;
	public static final int MESSAGE_PKUHOLE_LIST_MORE_FAILED = 20201;
	public static final int MESSAGE_PKUHOLE_REFRESH_FINISHED = 20202;
	public static final int MESSAGE_PKUHOLE_REFRESH_FAILED = 20203;
	public static final int MESSAGE_PKUHOLE_IMAGE_REQUEST = 20204;
	public static final int MESSAGE_NOTICECENTER_IMAGE_REQUEST = 20300;
	public static final int MESSAGE_NOTICECENTER_LIST_MORE_FINISHED = 20301;
	public static final int MESSAGE_NOTICECENTER_LIST_MORE_FAILED = 20302;
	public static final int MESSAGE_NOTICECENTER_ONE_MORE_FINISHED = 20303;
	public static final int MESSAGE_NOTICECENTER_ONE_MORE_FAILED = 20304;
	public static final int MESSAGE_LOSTFOUND_IMAGE_REQUEST_FINISHED = 20500;
	public static final int MESSAGE_LOSTFOUND_IMAGE_REQUEST_FAILED = 20501;
	public static final int MESSAGE_LOSTFOUND_LOST_MORE_FINISHED = 20502;
	public static final int MESSAGE_LOSTFOUND_LOST_MORE_FAILED = 20503;
	public static final int MESSAGE_LOSTFOUND_FOUND_MORE_FINISHED = 20504;
	public static final int MESSAGE_LOSTFOUND_FOUND_MORE_FAILED = 20505;
	public static final int MESSAGE_LOSTFOUND_MY_MORE_FINISHED = 20506;
	public static final int MESSAGE_LOSTFOUND_MY_MORE_FAILED = 20507;
	public static final int MESSAGE_LOSTFOUND_REFRESH_FAILED = 20508;
	public static final int MESSAGE_LOSTFOUND_REFRESH_FINISHED = 20509;
	public static final int MESSAGE_LOSTFOUND_LOAD_MORE_FINISHED = 20510;
	public static final int MESSAGE_WIDGET_FINISHED = 20600;
	public static final int MESSAGE_WIDGET_FAILED = 20601;
	public static final int MESSAGE_CHAT_GET_MORE_FINISHED=20700;
	public static final int MESSAGE_CHAT_GET_MORE_FAILED=20701;
	public static final int MESSAGE_CHAT_REFRESH_LIST=20702;
	public static final int MESSAGE_CHAT_REFRESH_DETAIL=20703;
	public static final int MESSAGE_CHAT_SEND_FINISHED=20704;
	public static final int MESSAGE_CHAT_SEND_FAILED=20705;
	public static final int MESSAGE_QRCODE_AUTOFOCUS=20800;
	public static final int MESSAGE_QRCODE_DECODE=20801;
	public static final int MESSAGE_QRCODE_DECODE_SUCCEEDED=20802;
	public static final int MESSAGE_QRCODE_DECODE_FAILED=20803;
	public static final int MESSAGE_QRCODE_QUIT=20804;
	public static final int MESSAGE_QRCODE_RESTART_PREVIEW=20805;	
	public static final int MESSAGE_QRCODE_RETURN_RESULT=20806;	
	public static final int MESSAGE_QRCODE_LAUNCH_QUERY=20807;
	public static final int MESSAGE_MEDIA_LIST_MORE_FINISHED = 20900;
	public static final int MESSAGE_MEDIA_LIST_MORE_FAILED = 20901;
	public static final int MESSAGE_HOLE_FILE_DOWNLOAD_FINISHED = 21000;
	public static final int MESSAGE_HOLE_FILE_DOWNLOAD_FAILED = 21001;
	public static final int MESSAGE_HOLE_AUDIO_TIME_UPDATE = 21002;
	public static final int MESSAGE_SUBACTIVITY_DECODE_PICTURE=21100;
	public static final int MESSAGE_SUBACTIVITY_CERTIFICATION = 21101;
	public static final int MESSAGE_SUBACTIVITY_SHOWS_PICTURE = 21102;
	public static final int MESSAGE_SUBACTIVITY_SHOWS_UPDATE_BITMAP = 21103;
	public static final int MESSAGE_BBS_LOGIN = 21200;
	
	public static final int SUBACTIVITY_TYPE_ABOUT = 30000;
	public static final int SUBACTIVITY_TYPE_LECTURE = 30001;
	public static final int SUBACTIVITY_TYPE_SHOWS = 30002;
	public static final int SUBACTIVITY_TYPE_PHONE = 30003;
	public static final int SUBACTIVITY_TYPE_PICTURE_RESOURCE = 30010;
	public static final int SUBACTIVITY_TYPE_PICTURE_FILE = 30011;
	public static final int SUBACTIVITY_TYPE_PICTURE_GIF = 30012;
	public static final int SUBACTIVITY_TYPE_PICTURE_URL = 30013;
	public static final int SUBACTIVITY_TYPE_WEBVIEW = 30020;
	public static final int SUBACTIVITY_TYPE_WEBVIEW_CALENDAR = 30021;
	public static final int SUBACTIVITY_TYPE_WEBVIEW_HTML = 30022;
	public static final int SUBACTIVITY_TYPE_WEBVIEW_PKUMAIL = 30023;
	public static final int SUBACTIVITY_TYPE_WEBVIEW_SHOWS = 30025;
	public static final int SUBACTIVITY_TYPE_MYMESSAGE = 30030;
	public static final int SUBACTIVITY_TYPE_CERTIFICATION = 30040;
	public static final int SUBACTIVITY_TYPE_NOTIFICATIONS = 30050;
	public static final int SUBACTIVITY_TYPE_NOTIFICATIONS_SETTING = 30051;
	public static final int SUBACTIVITY_TYPE_MYPKU_SET = 30060;
	public static final int SUBACTIVITY_TYPE_COURSE_SET = 30070;
	public static final int SUBACTIVITY_TYPE_INFORMATION = 30080;
	public static final int SUBACTIVITY_TYPE_IPGW_SET = 30090;
	
	// 课程单周还是双周，或者每周
	public static final int COURSE_TYPE_NONE = -1;
	public static final int COURSE_TYPE_EVERY = 0;
	public static final int COURSE_TYPE_ODD = 1;
	public static final int COURSE_TYPE_EVEN = 2;
	
	public static final String ACTION_ALARM = "com.pkuhelper.action.ALARM";
	public static final String ACTION_CONNECT = "com.pkuhelper.action.CONNECT";
	public static final String ACTION_CONNECT_NO_FREE = "com.pkuhelper.action.CONNECT_NO_FREE";
	public static final String ACTION_DISCONNECT = "com.pkuhelper.action.DISCONNECT";
	public static final String ACTION_DISCONNECT_ALL = "com.pkuhelper.action.DISCONNECT_ALL";
	public static final String ACTION_REFRESH_COURSE = "com.pkuhelper.action.REFRESH_COURSE";
	public static final String ACTION_VIEW_COURSE = "com.pkuhelper.action.VIEW_COURSE";
	public static final String ACTION_CONNECT_STATUS_SET = "com.pkuhelper.action.CONNECT_STATUS";
	public static final String ACTION_PAGE_PREVIOUS = "com.pkuhelper.action.PAGE_PREVIOUS";
	public static final String ACTION_PAGE_NEXT = "com.pkuhelper.action.PAGE_NEXT";
	public static final String ACTION_REFRESH_EXAM = "com.pkuhelper.action.REFRESH_EXAM";
	public static final String ACTION_SET_EXAM = "com.pkuhelper.action.SET_EXAM";
	
	public static String token="";
	public static String user_token="";
	public static String birthday="";
	public static String username="";
	public static String password="";
	public static String name="";
	public static String major="";
	public static String sex="";
	public static String phpsessid="";
	public static int week=0;
	public static boolean inSchool=false;
	public static boolean connected=false;
	public static boolean connectedToNoFree=false;
	public static boolean hasUpdate=false;
	public static int newMsg=0;
	public static int newPass=0;
	public static ArrayList<Features> features=new ArrayList<>();
	public static String updateVersion=Constants.version;
	public static String updateMessage="";
	
	public static void setDrawable(int id, Drawable drawable) {
		if (id>=features.size()) return;
		features.get(id).drawable=drawable;
		MYPKU.setOthers(features);
	}
	
	public static boolean isLogin() {
		return !"".equals(token);
	}
	
	public static boolean isValidLogin() {
		return !"".equals(token) && !"12345678".equals(username) && !"guest".equals(username);
	}
	
	public static void init(Context context) {
		token=Editor.getString(context, "token");
		user_token=Editor.getString(context, "user_token");
		username=Editor.getString(context, "username");
		password=Editor.getString(context, "password");
		name=Editor.getString(context, "name");
		major=Editor.getString(context, "major");
		sex=Editor.getString(context, "sex");
		birthday=Editor.getString(context, "birthday");
		
		if ("".equals(token) || "".equals(user_token))
			reset(context);
		
		if (isLogin())
			Lib.updateAndCheck(context);
		
		Lib.sendStatistics(context);
		Settings.setName();
	}
	
	public static void reset(Context context) {
		Editor.putString(context, "token", "");
		Editor.putString(context, "user_token", "");
		Editor.putString(context, "username", "");
		Editor.putString(context, "password", "");
		Editor.putString(context, "name", "");
		Editor.putString(context, "major", "");
		Editor.putString(context, "sex", "");
		Editor.putString(context, "birthday", "");
		user_token=token=username=password=name=major=sex="";
	}
	
}
