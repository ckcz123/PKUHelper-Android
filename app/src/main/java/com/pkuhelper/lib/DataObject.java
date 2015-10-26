package com.pkuhelper.lib;

public class DataObject {
	private static DataObject instance;
	private Object object;

	public static DataObject getInstance() {
		if (instance == null)
			instance = new DataObject();
		return instance;
	}

	public void setObject(Object _object) {
		object = _object;
	}

	public Object getObject() {
		return object;
	}
}
