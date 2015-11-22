package com.pkuhelper.selfstudy;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;

public class ActivityRecords extends BaseActivity {
	
	private ListView lv = null;
	private List<Record> list;
	private ServiceRecord serviceRecord;
	
	private RecordAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selfstudy_records);
		lv = (ListView)findViewById(R.id.lv);
		serviceRecord = new ServiceRecord(this);
		String sql = "select * from records order by timeStart desc";
		list = serviceRecord.getRecords(sql);
		adapter = new RecordAdapter();
		lv.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void finishRequest(int type, String string) {
	}

	private class RecordAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder;
			if(convertView == null) {
				convertView = LayoutInflater.from(ActivityRecords.this).inflate(R.layout.selfstudy_item, parent, false);
	            holder = new Holder();
	            holder.llItem = (LinearLayout)convertView.findViewById(R.id.llItem);
	            holder.tvTime = (TextView)convertView.findViewById(R.id.tvTime);
	            holder.tvDuration = (TextView)convertView.findViewById(R.id.tvDuration);
	            holder.tvDate = (TextView)convertView.findViewById(R.id.tvDate);
	            holder.tvWeek = (TextView)convertView.findViewById(R.id.tvWeek);
	            convertView.setTag(holder);
			} else {
				holder = (Holder)convertView.getTag();
			}
			Record record = FormatRecord.getFormattedData(list.get(position));
			holder.llItem.setBackgroundResource(record.getColorResourceId());
			holder.tvTime.setText(record.getFormattedTime());
			holder.tvDuration.setText(record.getFormattedDuration());
			holder.tvDate.setText(record.getFormattedDate());
			holder.tvWeek.setText(record.getFormattedWeek());
			return convertView;
		}
	}
	
	private static class Holder {
		LinearLayout llItem;
		TextView tvTime;
		TextView tvDuration;
		TextView tvDate;
		TextView tvWeek;
	}
}
