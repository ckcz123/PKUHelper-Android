package com.pkuhelper.grade;

public class Course {
	String name;
	String fullname;
	Semester semester;
	String weight;
	String grade;
	String delta;
	String accurate;
	String gpa;
	String type;

	public Course(Semester _semester, String _name, String _fullname, String _type,
				  String _weight, String _grade, String _delta, String _accurate, String _gpa) {
		name = _name.trim();
		fullname = _fullname.trim();
		type = _type.trim();
		semester = _semester;
		weight = _weight.trim();
		grade = _grade.trim();
		delta = _delta.trim();
		accurate = _accurate.trim();
		gpa = _gpa.trim();
		if ("0".equals(accurate)) {
			grade = grade + "±" + delta;
		}
	}
}
