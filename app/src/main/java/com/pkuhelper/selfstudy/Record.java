package com.pkuhelper.selfstudy;

public class Record {
	private int id;
	private long timeStart;
	private long timeEnd;

	private String formattedTime;
	private String formattedDuration;
	private String formattedDate;
	private String formattedWeek;

	private String durationInHHmm;
	
	public String getDurationInHHmm() {
		return durationInHHmm;
	}

	public void setDurationInHHmm(String durationInHHmm) {
		this.durationInHHmm = durationInHHmm;
	}

	private int colorResourceId;
	
	public Record(){}
	
	public Record(long timeStart, long timeEnd) {
		this.timeEnd = timeEnd;
		this.timeStart = timeStart;
	}
	
	public int getColorResourceId() {
		return colorResourceId;
	}

	public void setColorResourceId(int colorResourceId) {
		this.colorResourceId = colorResourceId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getTimeStart() {
		return timeStart;
	}

	public void setTimeStart(long timeStart) {
		this.timeStart = timeStart;
	}

	public long getTimeEnd() {
		return timeEnd;
	}

	public void setTimeEnd(long timeEnd) {
		this.timeEnd = timeEnd;
	}

	public String getFormattedTime() {
		return formattedTime;
	}

	public void setFormattedTime(String formattedTime) {
		this.formattedTime = formattedTime;
	}

	public String getFormattedDuration() {
		return formattedDuration;
	}

	public void setFormattedDuration(String formattedDuration) {
		this.formattedDuration = formattedDuration;
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	public void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}

	public String getFormattedWeek() {
		return formattedWeek;
	}

	public void setFormattedWeek(String formattedWeek) {
		this.formattedWeek = formattedWeek;
	}

}
