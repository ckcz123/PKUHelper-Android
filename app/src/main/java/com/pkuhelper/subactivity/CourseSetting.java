package com.pkuhelper.subactivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.Lib;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.widget.WidgetCourse2Provider;
import com.pkuhelper.widget.WidgetCourseProvider;

import java.util.ArrayList;

public class CourseSetting {
	SubActivity subActivity;

	public CourseSetting(SubActivity _subactivity) {
		subActivity = _subactivity;
	}

	public void show() {
		subActivity.setContentView(R.layout.settings_course);
		subActivity.setTitle("课表设置");

		final Spinner courseWeekSpinner = (Spinner) subActivity.findViewById(R.id.settings_spinner_week);
		ArrayList<String> weeks = new ArrayList<String>();
		for (int i = 0; i <= 20; i++) weeks.add(i + "");
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(subActivity, android.R.layout.simple_spinner_item, weeks);
		courseWeekSpinner.setAdapter(spinnerAdapter);
		int week = Editor.getInt(subActivity, "week");
		if (week < 0 || week >= 21) week = 0;
		boolean autoweek = Editor.getBoolean(subActivity, "autoweek", true);
		ViewSetting.setSwitchChecked(subActivity, R.id.settings_switch_week, autoweek);
		ViewSetting.setSwitchOnCheckChangeListener(subActivity, R.id.settings_switch_week, new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor.putBoolean(subActivity, "autoweek", isChecked);
				subActivity.findViewById(R.id.settings_week_text).setEnabled(!isChecked);
				subActivity.findViewById(R.id.settings_spinner_week).setEnabled(!isChecked);
				if (isChecked) {
					Editor.putInt(subActivity, "week", Constants.week);
					Editor.putLong(subActivity, "time", System.currentTimeMillis());
					courseWeekSpinner.setSelection(Constants.week);

					Lib.sendBroadcast(subActivity, WidgetCourseProvider.class, Constants.ACTION_REFRESH_COURSE);
					Lib.sendBroadcast(subActivity, WidgetCourse2Provider.class, Constants.ACTION_REFRESH_COURSE);
				}
			}
		});
		subActivity.findViewById(R.id.settings_week_text).setEnabled(!autoweek);
		subActivity.findViewById(R.id.settings_spinner_week).setEnabled(!autoweek);

		courseWeekSpinner.setSelection(week);
		courseWeekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				Editor.putLong(subActivity, "time", System.currentTimeMillis());
				Editor.putInt(subActivity, "week", position);

				Lib.sendBroadcast(subActivity, WidgetCourseProvider.class, Constants.ACTION_REFRESH_COURSE);
				Lib.sendBroadcast(subActivity, WidgetCourse2Provider.class, Constants.ACTION_REFRESH_COURSE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		ViewSetting.setSwitchChecked(subActivity, R.id.settings_course_elective_switch,
				Editor.getBoolean(subActivity, "course_elective", true));
		ViewSetting.setSwitchChecked(subActivity, R.id.settings_course_dean_switch,
				Editor.getBoolean(subActivity, "course_dean", true));
		ViewSetting.setSwitchChecked(subActivity, R.id.settings_course_dual_switch,
				Editor.getBoolean(subActivity, "course_dual", true));
		ViewSetting.setSwitchChecked(subActivity, R.id.settings_course_custom_switch,
				Editor.getBoolean(subActivity, "course_custom", true));

		ViewSetting.setSwitchOnCheckChangeListener(subActivity, R.id.settings_course_elective_switch,
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Editor.putBoolean(subActivity, "course_elective", isChecked);
					}
				});
		ViewSetting.setSwitchOnCheckChangeListener(subActivity, R.id.settings_course_dean_switch,
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Editor.putBoolean(subActivity, "course_dean", isChecked);
					}
				});
		ViewSetting.setSwitchOnCheckChangeListener(subActivity, R.id.settings_course_dual_switch,
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Editor.putBoolean(subActivity, "course_dual", isChecked);
					}
				});
		ViewSetting.setSwitchOnCheckChangeListener(subActivity, R.id.settings_course_custom_switch,
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Editor.putBoolean(subActivity, "course_custom", isChecked);
					}
				});

	}

}
