package com.pkuhelper.subactivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Shows {
	SubActivity subActivity;
	ListView listView;
	ArrayList<ShowInfo> arrayList;


	public Shows(SubActivity subActivity) {
		this.subActivity = subActivity;
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("InflateParams")
	public Shows getShows() {
		subActivity.getActionBar().setTitle("百讲演出");
		subActivity.setContentView(R.layout.subactivity_listview);
		subActivity.findViewById(R.id.subactivity_swipeRefreshLayout)
				.setBackgroundColor(Color.parseColor("#feb772"));
		listView = (ListView) subActivity.findViewById(R.id.subactivity_listview);
		LayoutInflater layoutInflater = subActivity.getLayoutInflater();
		View headerView = layoutInflater.inflate(R.layout.subactivity_listview_headerview, null);
		ViewSetting.setImageResource(headerView, R.id.subactivity_listview_image, R.drawable.bjyc);
		listView.addHeaderView(headerView);
		listView.addFooterView(layoutInflater.inflate(R.layout.subactivity_listview_footerview, null));
		listView.setHeaderDividersEnabled(false);
		listView.setFooterDividersEnabled(false);
		arrayList = new ArrayList<ShowInfo>();
		listView.setAdapter(new SimpleAdapter(subActivity,
				new ArrayList<HashMap<String, String>>(),
				R.layout.subactivity_listview_item, new String[]{}, new int[]{}));

		new RequestingTask(subActivity, "正在获取百讲演出信息...",
				Constants.domain + "/pkuhelper/getshows.php", Constants.REQUEST_SUBACTIVITY_SHOWS)
				.execute(new ArrayList<Parameters>());


		return this;
	}

	public void finishRequest(String string) {
		try {
			arrayList = new ArrayList<ShowInfo>();
			JSONArray jsonArray = new JSONArray(string);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				ShowInfo showInfo = ShowInfo.getShowInfo(subActivity, subActivity.handler, jsonArray.getJSONObject(i));
				if (showInfo != null) arrayList.add(showInfo);
			}
		} catch (Exception e) {
			arrayList = new ArrayList<ShowInfo>();
		} finally {
			show();
		}
	}

	public void show() {
		listView.setAdapter(new BaseAdapter() {
			@SuppressLint("ViewHolder")
			public View getView(int position, View convertView, ViewGroup parent) {
				final ShowInfo showInfo = arrayList.get(position);
				convertView = subActivity.getLayoutInflater().inflate(R.layout.subactivity_shows_listitem, parent, false);

				String text = showInfo.title + "\n\n\n" + showInfo.location + "\n\n" + showInfo.date + " " + showInfo.time;
				ViewSetting.setTextView(convertView, R.id.subactivity_shows_text, text);
				ViewSetting.setTextViewBold(convertView, R.id.subactivity_shows_text);

				Bitmap bitmap = showInfo.bitmap;
				if (bitmap != null) {
					convertView.findViewById(R.id.subactivity_shows_image).setVisibility(View.VISIBLE);
					ViewSetting.setImageBitmap(convertView, R.id.subactivity_shows_image, bitmap);
				}
				ViewSetting.setOnClickListener(convertView, R.id.subactivity_shows_image, new View.OnClickListener() {
					public void onClick(View v) {
						File file = MyFile.getCache(subActivity, Util.getHash(showInfo.image));
						if (file.exists()) {
							Intent intent = new Intent(subActivity, SubActivity.class);
							intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_FILE);
							intent.putExtra("file", file.getAbsolutePath());
							intent.putExtra("title", showInfo.title);
							subActivity.startActivity(intent);
						}
					}
				});

				ArrayList<Price> prices = showInfo.prices;
				String priceString = "";
				String availableColor = "#aaaaaa";
				int len = prices.size();
				for (int i = 0; i < len; i++) {
					Price price = prices.get(i);
					String color = "#aaaaaa";
					if (price.isAvailable) {
						if (price.isSchool) {
							color = "green";
							availableColor = "green";
						} else {
							color = "#ffd700";
							if (!"green".equals(availableColor))
								availableColor = "#ffd700";
						}
					}
					priceString += "<font color='" + color + "'>" + price.price + "</font>&nbsp;&nbsp;";
				}
				ViewSetting.setTextView(convertView, R.id.subactivity_shows_price, Html.fromHtml(priceString));
				ViewSetting.setTextViewColor(convertView, R.id.subactivity_shows_available, Color.parseColor(availableColor));
				convertView.setTag(showInfo.id);
				return convertView;
			}

			public long getItemId(int position) {
				return 0;
			}

			public Object getItem(int position) {
				return null;
			}

			public int getCount() {
				return arrayList.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				try {
					int i = (Integer) view.getTag();
					ShowInfo showInfo = ShowInfo.getShowInfo(i);
					String url = showInfo.link;
					if ("".equals(url)) {
						CustomToast.showInfoToast(subActivity, "该演出没有简介");
						return;
					}
					Intent intent = new Intent(subActivity, SubActivity.class);
					intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW_SHOWS);
					intent.putExtra("url", url);
					intent.putExtra("title", showInfo.title);
					subActivity.startActivity(intent);
				} catch (Exception e) {
				}
			}
		});
		updateBitmap();
	}

	public void updateBitmap() {
		new Thread(new Runnable() {
			public void run() {
				try {
					try {
						ShowInfo.updateBitmaps(subActivity.handler);
					} catch (Exception e) {
					}
					subActivity.handler.sendEmptyMessage(Constants.MESSAGE_SUBACTIVITY_SHOWS_PICTURE);
				} catch (Exception e) {
				}
			}
		}).start();
	}

	public void updateImage(int showid) {
		try {
			int cnt = listView.getCount();
			for (int i = 0; i < cnt; i++) {
				View view = listView.getChildAt(i);
				if (view == null) continue;
				try {
					int id = (Integer) view.getTag();
					if (showid != 0 && id != showid) continue;
					if (view.findViewById(R.id.subactivity_shows_image).getVisibility() == View.VISIBLE)
						continue;
					Bitmap bitmap = ShowInfo.getShowInfo(id).bitmap;
					if (bitmap == null) return;
					view.findViewById(R.id.subactivity_shows_image).setVisibility(View.VISIBLE);
					ViewSetting.setImageBitmap(view, R.id.subactivity_shows_image, bitmap);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}
	}


	@SuppressWarnings("unchecked")
	public Shows getHtml(String title, String url) {
		subActivity.setContentView(R.layout.subactivity_webview);
		subActivity.getActionBar().setTitle(title);
		subActivity.webView =
				(WebView) subActivity.findViewById(R.id.subactivity_webview);
		subActivity.webView.setVerticalScrollBarEnabled(false);
		subActivity.webView.setHorizontalScrollBarEnabled(false);
		new RequestingTask(subActivity, "正在获取详细信息...", url
				, Constants.REQUEST_SUBACTIVITY_SHOWS_DETAIL)
				.execute(new ArrayList<Parameters>());
		return this;
	}

	public void viewHtml(String html) {
		String realHtml = dealWithHtml(html);
		if (realHtml == null) return;
		subActivity.webView.loadDataWithBaseURL(
				null, realHtml, "text/html", "utf-8", null);
		subActivity.html = getDescription(realHtml);
	}

	private String dealWithHtml(String html) {
		try {
			String realHtml = "<html><body style='word-wrap:break-word'>";
			Document document = Jsoup.parse(html);
			Elements elements = document.getElementsByClass("bj-dian");
			Element title = elements.first().child(0);
			Element tt = title.child(0);
			//realHtml+="<p style='font-size:14px;font-weight:bold;color:#804012'>";
			//realHtml+=tt.text()+"</p><br>";

			//System.out.println(tt.text());
			//System.out.println("------");
			Element ttdetail = title.child(1).getElementsByTag("div").first();
			ttdetail.getElementsByTag("img").remove();
			//System.out.println(ttdetail.toString());
			//System.out.println("------");
			realHtml += ttdetail.toString();

			Element text = elements.get(1).child(0).getElementsByTag("div").first();
			Iterator<Element> iterator = text.children().iterator();
			while (iterator.hasNext()) {
				Element el = iterator.next();
				if (!el.getElementsByTag("img").isEmpty()) {
					el.remove();
					continue;
				}
				String string = new String(el.html());
				string = string.trim();
				string = string.replace("<p>", "");
				string = string.replace("</p>", "");
				string = string.replace(" ", "");
				string = string.replace("&nbsp;", "");
				string = string.trim();
				if (string.length() == 0)
					el.remove();
			}

			realHtml += text.html();

			realHtml += "<br><br>***********************"
					+ "<br>此网页由PKU Helper经过了摘要提取，如需访问原网页请点击<a href='"
					+ subActivity.url + "' target='_blank'>这里</a><br><br></body></html>";

			subActivity.getActionBar().setTitle(tt.text());

			return realHtml;

		} catch (Exception e) {
			CustomToast.showErrorToast(subActivity, "获取失败");
			return null;
		}
	}

	private String getDescription(String html) {
		try {
			Document document = Jsoup.parse(html);
			String text = document.text();
			if (text.length() >= 35)
				text = text.substring(0, 33) + "...";
			return text;
		} catch (Exception e) {
			return "";
		}
	}

}

class Price {
	String price;
	boolean isSchool, isAvailable;

	public Price(String price, boolean isSchool, boolean isAvailable) {
		this.price = new String(price);
		this.isSchool = isSchool;
		this.isAvailable = isAvailable;
	}

	public static ArrayList<Price> getPrices(JSONArray jsonArray) {
		try {
			ArrayList<Price> arrayList = new ArrayList<Price>();
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				arrayList.add(new Price(jsonObject.getString("price"),
						jsonObject.optInt("isSchool") != 0,
						jsonObject.optInt("isAvailable") != 0));
			}
			Collections.sort(arrayList, new Comparator<Price>() {
				public int compare(Price lhs, Price rhs) {
					int lp, rp;
					try {
						lp = Integer.parseInt(lhs.price);
					} catch (Exception e) {
						return 1;
					}
					try {
						rp = Integer.parseInt(rhs.price);
					} catch (Exception e) {
						return -1;
					}
					return lp - rp;
				}
			});
			return arrayList;
		} catch (Exception e) {
			return new ArrayList<Price>();
		}
	}
}

class ShowInfo {
	Context context;
	int id;
	String title;
	ArrayList<Price> prices;
	String date, time, location;
	String link;
	String image;
	Bitmap bitmap;

	@SuppressLint("UseSparseArrays")
	public static HashMap<Integer, ShowInfo> showsMap = new HashMap<Integer, ShowInfo>();

	public static ShowInfo getShowInfo(Context context,
									   Handler handler, JSONObject jsonObject) {
		ShowInfo showInfo = new ShowInfo(context, handler, jsonObject);
		if (showInfo.id < 0) return null;
		return showInfo;
	}

	public ShowInfo(Context _context,
					final Handler handler, JSONObject jsonObject) {
		try {
			context = _context;
			id = jsonObject.getInt("id");
			title = jsonObject.optString("title");
			date = jsonObject.getString("action_date");
			time = jsonObject.getString("action_time");
			location = jsonObject.optString("location");
			prices = Price.getPrices(jsonObject.getJSONArray("prices"));
			image = jsonObject.optString("image");
			link = jsonObject.optString("link");
			bitmap = null;
			if (!"".equals(image)) {
				image = "http://www.pku-hall.com/" + image;
				final File file = MyFile.getCache(context, Util.getHash(image));
				if (!file.exists()) {
					new Thread(new Runnable() {
						public void run() {
							if (MyFile.urlToFile(image, file)
									&& handler != null) {
								handler.sendEmptyMessage(Constants.MESSAGE_SUBACTIVITY_SHOWS_UPDATE_BITMAP);
							}
						}
					}).start();
				}
			}
			showsMap.put(id, this);
		} catch (Exception e) {
			id = -1;
		}
	}

	public Bitmap getBitmap() {
		if (bitmap != null) return bitmap;
		File file = MyFile.getCache(context, Util.getHash(image));
		if (!file.exists()) return bitmap = null;
		return bitmap = MyBitmapFactory.getCompressedBitmap(file.getAbsolutePath(), 2);
	}

	public static ShowInfo getShowInfo(int id) {
		return showsMap.get(id);
	}

	public static void updateBitmaps(final Handler handler) {
		for (Map.Entry<Integer, ShowInfo> entry : showsMap.entrySet()) {
			final int id = entry.getKey();
			new Thread(new Runnable() {
				public void run() {
					showsMap.get(id).getBitmap();
					handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_SUBACTIVITY_SHOWS_PICTURE, id, 0));
				}
			}).start();
		}
	}

}
