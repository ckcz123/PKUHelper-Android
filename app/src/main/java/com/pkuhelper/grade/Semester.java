package com.pkuhelper.grade;

import java.util.ArrayList;
import java.util.Iterator;

public class Semester {
	String year;
	String term;
	ArrayList<Course> courses;
	String gpa;
	String weight;
	boolean isDual;
	
	public Semester(String _year, String _term, String _gpa, boolean _isDual) {
		year=_year;
		term=_term;
		gpa=_gpa;
		weight="";
		courses=new ArrayList<Course>();
		isDual=_isDual;
	}
	
	public void addCourse(String _name,String _fullname,String _type,
			String _weight,String _grade,String _delta,String _accurate,String _gpa) {
		Course course=new Course(this, _name, _fullname, _type, _weight, 
				_grade, _delta, _accurate, _gpa);
		courses.add(course);
	}
	
	public boolean isThisSemester(String _year, String _term) {
		return year.equals(_year) && term.equals(_term);
	}
	
	public void calWeight() {
		int w=0;
		Iterator<Course> iterator=courses.iterator();
		while (iterator.hasNext()) {
			float sc=Float.parseFloat(iterator.next().weight);
			w+=(int)sc;
		}
		weight=w+"";
	}
	
}
