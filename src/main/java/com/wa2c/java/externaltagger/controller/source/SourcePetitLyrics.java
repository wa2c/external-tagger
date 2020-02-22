package com.wa2c.java.externaltagger.controller.source;

import com.wa2c.java.externaltagger.model.FieldDataMap;
import com.wa2c.java.externaltagger.value.MediaField;
import com.wa2c.java.externaltagger.value.SearchFieldUsing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SourcePetitLyrics extends AbstractHtmlSource {
    @Override
    protected String getSearchAnchorXPath() {
        return null;
    }

    @Override
    public Set<MediaField> getSearchField() {
        return null;
    }

    @Override
    public HashMap<MediaField, SourceConversion> getConversionMap() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<MediaField> getResultField() {
        return null;
    }

    @Override
    public FieldDataMap getFieldDataMap(FieldDataMap fieldData, Map<MediaField, SearchFieldUsing> searchUsing) {
        return null;
    }
}
