package com.wa2c.java.externaltagger.controller.source;

import com.wa2c.java.externaltagger.model.FieldDataMap;
import com.wa2c.java.externaltagger.value.MediaField;
import com.wa2c.java.externaltagger.value.SearchFieldUsing;

import java.util.Map;
import java.util.Set;


public abstract class AbstractExternalSource {

	protected FieldDataMap inputDataMap;
	protected FieldDataMap outputDataMap;



	public abstract String getName();

	//public abstract Set<MediaField> getTargetSource();


	public abstract Set<MediaField>  getResultField();



	public abstract FieldDataMap getFieldDataMap(FieldDataMap fieldData, Map<MediaField, SearchFieldUsing> searchUsing);



	private boolean isEnabled = true;

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public boolean getEnabled() {
		return isEnabled;
	}

	@Override
	public String toString() {
		return getName();
	}

}
