package com.wa2c.java.externaltagger.common;

import com.wa2c.java.externaltagger.value.MediaField;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class FieldDataMap extends EnumMap<MediaField, List<String>> {

	private static final long serialVersionUID = 1L;

	public FieldDataMap() {
		super(new EnumMap<MediaField, List<String>>(MediaField.class));
	}

	public FieldDataMap(EnumMap<MediaField, ? extends List<String>> m) {
		super(m);

	}

	public String getFirstData(MediaField field) {
		List<String> list = this.get(field);
		if (list == null || list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}

	public void put(MediaField field, String data) {
		data = AppUtils.trim(data.trim());

		List<String> list = this.get(field);
		if (list == null) {
			list = new ArrayList<String>();
		}

		if (!list.contains(data)) {
			list.add(data);
			put(field, list);
		}
	}


	public void putFirstData(MediaField field, String data) {
		data = AppUtils.trim(data.trim());

		List<String> list = this.get(field);
		if (list == null) {
			list = new ArrayList<String>();
		}

		if (list.contains(data)) {
			list.remove(data);
		}
		list.add(0, data);

		put(field, list);
	}

	public void putNewData(MediaField field, String data) {
		data = AppUtils.trim(data.trim());

		List<String> list = new ArrayList<String>();
		list.add(data);

		putNewData(field, list);
	}

	public void putNewData(MediaField field, List<String> data) {
		put(field, data);
	}

}
