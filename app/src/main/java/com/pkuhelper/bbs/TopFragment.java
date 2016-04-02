package com.pkuhelper.bbs;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.ui.CompatListView;
import com.pkuhelper.ui.hole.IHoleListUI;
import com.pkuhelper.util.SizeUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;

public class TopFragment extends Fragment {
	public static TopFragment topFragment;
	public static ArrayList<ThreadInfo> tops = new ArrayList<ThreadInfo>();
	static View topView;
    private static PtrClassicFrameLayout ptrLayout;
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.bbs_top_listview, container, false);
		topFragment = this;
		topView = rootView;
		realShowView(true);
        setupPtrLayout(rootView);
		return rootView;
	}

	@SuppressWarnings("unchecked")
	public static void showView() {
		new RequestingTask(BBSActivity.bbsActivity, "正在获取热门帖子...", "http://www.bdwm.net/client/bbsclient.php?type=gettop&number=30",
				Constants.REQUEST_BBS_GET_TOP).execute(new ArrayList<Parameters>());
	}

	public static void finishRequest(String string) {
		tops = new ArrayList<ThreadInfo>();
		try {
			JSONArray jsonArray = new JSONArray(string);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				try {
					tops.add(new ThreadInfo(jsonObject.getInt("rank"),
							jsonObject.getString("boardName"), jsonObject.getString("boardDescription"),
							jsonObject.optString("author"), jsonObject.getString("date"),
							jsonObject.optString("title"), jsonObject.getInt("threadid")));
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			tops = new ArrayList<ThreadInfo>();
		} finally {
			if (tops.size() == 0) {
				CustomToast.showInfoToast(BBSActivity.bbsActivity, "暂时没有热门帖子，请刷新重试", 1500);
			}
			realShowView(false);
		}
        ptrLayout.refreshComplete();
	}

	public static void realShowView(boolean refresh) {
		if (tops.size() == 0 && refresh) {
			showView();
			return;
		}

		CompatListView listView = (CompatListView) topView.findViewById(R.id.lv_bbs);
		listView.setAdapter(new BaseAdapter() {
			@Override
			@SuppressLint("ViewHolder")
			public View getView(final int position, View convertView, ViewGroup parent) {
				convertView = BBSActivity.bbsActivity.getLayoutInflater().inflate(R.layout.bbs_top_listitem,
						parent, false);
				ThreadInfo threadInfo = tops.get(position);
				ViewSetting.setTextView(convertView, R.id.bbs_top_item_from, threadInfo.board + " / " + threadInfo.boardName);
				ViewSetting.setTextView(convertView, R.id.bbs_top_item_rank, "#" + threadInfo.rank);
				ViewSetting.setTextView(convertView, R.id.bbs_top_item_title, threadInfo.title);
				ViewSetting.setTextView(convertView, R.id.bbs_top_item_author, threadInfo.author);
				ViewSetting.setTextView(convertView, R.id.bbs_top_item_time,
						MyCalendar.format(threadInfo.time));

                CardView cardView = (CardView) convertView.findViewById(R.id.card_bbs_item);
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ThreadInfo threadInfo = tops.get(position);
                        Intent intent = new Intent(BBSActivity.bbsActivity, ViewActivity.class);
                        intent.putExtra("board", threadInfo.board);
                        intent.putExtra("threadid", threadInfo.threadid + "");
                        intent.putExtra("type", "thread");
                        BBSActivity.bbsActivity.startActivity(intent);
                    }
                });
				return convertView;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public int getCount() {
				return tops.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				ThreadInfo threadInfo = tops.get(position);
				Intent intent = new Intent(BBSActivity.bbsActivity, ViewActivity.class);
				intent.putExtra("board", threadInfo.board);
				intent.putExtra("threadid", threadInfo.threadid + "");
				intent.putExtra("type", "thread");
				BBSActivity.bbsActivity.startActivity(intent);
			}
		});

	}

    /**
     * 配置PullToRefreshLayout
     * @param view rootView
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void setupPtrLayout(View view) {
        Context mContext = getContext();
        ptrLayout = (PtrClassicFrameLayout) view.findViewById(R.id.ptr_bbs_list);

        ptrLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout ptrFrameLayout) {
                TopFragment.showView();
            }
        });

        // 设置下拉刷新时ListView保持不动（仿Chrome刷新效果）
        ptrLayout.setPinContent(true);

        // set material pull-to-refresh progressBar
        final MaterialHeader header = new MaterialHeader(getActivity());
        header.setPtrFrameLayout(ptrLayout);
        int[] colors = {
                ContextCompat.getColor(mContext, R.color.colorPrimaryDark),
                ContextCompat.getColor(mContext, R.color.colorPrimary) };
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        header.setPadding(0, SizeUtil.dip2px(mContext, 16), 0, SizeUtil.dip2px(mContext, 16));
        ptrLayout.setHeaderView(header);
        ptrLayout.addPtrUIHandler(header);
    }

    public void completePullToRefresh() {
        ptrLayout.refreshComplete();
    }
}
