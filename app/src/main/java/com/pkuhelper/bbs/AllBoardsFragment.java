package com.pkuhelper.bbs;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;

import com.pkuhelper.R;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.ViewSetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AllBoardsFragment extends Fragment {
	public static ArrayList<HashMap<String, String>> showList = new ArrayList<HashMap<String, String>>();
	static View allBoardsView;
	static String tmpBoard;
	static boolean allBoards = true;
	static ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.bbs_allboards_view, container, false);
		allBoardsView = rootView;
		allBoards = Editor.getBoolean(BBSActivity.bbsActivity, "bbs_viewall", true);
		show();
		return rootView;
	}

	public static void show() {
		if (allBoardsView == null) return;
		listView = (ListView) allBoardsView.findViewById(R.id.bbs_allboards_listview);
		showList = new ArrayList<HashMap<String, String>>();
		listView.setAdapter(new SimpleAdapter(BBSActivity.bbsActivity, showList, R.layout.bbs_allboards_item,
				new String[]{"text"}, new int[]{R.id.bbs_allboards_item}));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				Intent intent = new Intent(BBSActivity.bbsActivity, ViewActivity.class);
				intent.putExtra("board", showList.get(position).get("board"));
				intent.putExtra("type", "board");
				BBSActivity.bbsActivity.startActivity(intent);
			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
										   int position, long id) {
				tmpBoard = showList.get(position).get("board");
				return false;
			}
		});
		BBSActivity.bbsActivity.registerForContextMenu(listView);
		resetList();

		SearchView searchView = (SearchView) allBoardsView.findViewById(R.id.bbs_allboards_searchview);
		AutoCompleteTextView search_text = (AutoCompleteTextView) searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
		search_text.setTextSize(14);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				getQuery(newText);
				return true;
			}
		});

	}

	public static void setHeaderView() {
		try {
			if (allBoards) {
				allBoardsView.findViewById(R.id.bbs_boards_all).setBackgroundResource(R.drawable.lf_btn_left_selected);
				allBoardsView.findViewById(R.id.bbs_boards_favorite).setBackgroundResource(R.drawable.lf_btn_right);
				ViewSetting.setTextViewColor(allBoardsView, R.id.bbs_boards_all, Color.parseColor("#e8e8e7"));
				ViewSetting.setTextViewColor(allBoardsView, R.id.bbs_boards_favorite, Color.parseColor("#333333"));
			} else {
				allBoardsView.findViewById(R.id.bbs_boards_all).setBackgroundResource(R.drawable.lf_btn_left);
				allBoardsView.findViewById(R.id.bbs_boards_favorite).setBackgroundResource(R.drawable.lf_btn_right_selected);
				ViewSetting.setTextViewColor(allBoardsView, R.id.bbs_boards_all, Color.parseColor("#333333"));
				ViewSetting.setTextViewColor(allBoardsView, R.id.bbs_boards_favorite, Color.parseColor("#e8e8e7"));
			}
			ViewSetting.setOnClickListener(allBoardsView, R.id.bbs_boards_all, new View.OnClickListener() {
				public void onClick(View v) {
					if (allBoards) return;
					allBoards = true;
					resetList();
				}
			});
			ViewSetting.setOnClickListener(allBoardsView, R.id.bbs_boards_favorite, new View.OnClickListener() {
				public void onClick(View v) {
					if (!allBoards) return;
					allBoards = false;
					resetList();
				}
			});
		} catch (Exception e) {
		}
	}

	public static void resetList() {
		try {
			setHeaderView();
			SearchView searchView = (SearchView) allBoardsView.findViewById(R.id.bbs_allboards_searchview);
			AutoCompleteTextView search_text = (AutoCompleteTextView) searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
			search_text.setText("");
			if (allBoards)
				search_text.setHint("搜索所有版面...");
			else
				search_text.setHint("搜索收藏版面...");
			allBoardsView.requestFocus();
		} catch (Exception e) {
		}
		getQuery("");
	}

	private static void getQuery(String newText) {
		showList.clear();
		for (Map.Entry<String, Board> entry : Board.boards.entrySet()) {
			String boardname = entry.getKey();
			Board board = entry.getValue();
			if (!allBoards && !Board.favorite.contains(boardname)) continue;
			String string = board.board + " / " + board.name + "  " + board.category;
			if (string.toLowerCase(Locale.getDefault()).contains(newText.toLowerCase(Locale.getDefault()))) {
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("text", string);
				hashMap.put("board", boardname);
				showList.add(hashMap);
			}
		}
		Collections.sort(showList, new Comparator<HashMap<String, String>>() {
			@Override
			public int compare(HashMap<String, String> lhs,
							   HashMap<String, String> rhs) {
				return lhs.get("board").toLowerCase(Locale.getDefault())
						.compareTo(rhs.get("board").toLowerCase(Locale.getDefault()));
			}
		});
		try {
			SimpleAdapter simpleAdapter = (SimpleAdapter) listView.getAdapter();
			simpleAdapter.notifyDataSetChanged();
		} catch (Exception e) {
		}
	}

}
