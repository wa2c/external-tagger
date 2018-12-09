package com.wa2c.java.externaltagger.source;

import com.wa2c.java.externaltagger.common.FieldDataMap;
import com.wa2c.java.externaltagger.common.MediaField;
import com.wa2c.java.externaltagger.common.SearchFieldUsing;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class SourceKashiGet extends AbstractHtmlSource {

	private static final String SITE_NAME = "歌詞GET";
	/** 検索URI。 */
	private static final String SEARCH_URL = "http://www.kget.jp/search/index.php?c=0&t=%s&r=%s";
	/** 検索結果アンカー。 */
	private static final String SEARCH_ANCHOR_XPATH = "//*[@id=\"search-result\"]/ul/li/div[1]/a";

	private final static HashSet<MediaField> searchFieldSet = new HashSet<MediaField>() { {
		add(MediaField.TITLE);
		add(MediaField.ARTIST);
	} };

	protected final static HashMap<MediaField, SourceConversion> sourceConversionMap = new HashMap<MediaField, SourceConversion>() { {
		put(MediaField.TITLE        , new SourceConversion(MediaField.TITLE        , "//*[@id=\"status-heading\"]/h1/strong" ));
		put(MediaField.ARTIST       , new SourceConversion(MediaField.ARTIST       , "//*[@id=\"content\"]/div[1]/table/tbody/tr[1]/td/span/a" ));
		put(MediaField.LYRICIST     , new SourceConversion(MediaField.LYRICIST     , "//*[@id=\"content\"]/div[1]/table/tbody/tr[2]/td/text()" ));
		put(MediaField.COMPOSER     , new SourceConversion(MediaField.COMPOSER     , "//*[@id=\"content\"]/div[1]/table/tbody/tr[3]/td/text()" ));
		put(MediaField.LYRICS       , new SourceConversion(MediaField.LYRICS       , "//*[@id=\"lyric-trunk\"]" ));
	} };



	public SourceKashiGet() {

	}



	@Override
	public String getName() {
		return SITE_NAME;
	}

	@Override
	public Set<MediaField> getSearchField() {
		return searchFieldSet;
	}

	@Override
	public Set<MediaField> getResultField() {
		return this.sourceConversionMap.keySet();
	}

	@Override
	public HashMap<MediaField, SourceConversion> getConversionMap() {
		return sourceConversionMap;
	}


	@Override
	public FieldDataMap getFieldDataMap(FieldDataMap fieldData, Map<MediaField, SearchFieldUsing> searchUsing) {
		this.inputDataMap = fieldData;

		// 検索テキスト
		String title = fieldData.getFirstData(MediaField.TITLE);
		if (searchUsing.get(MediaField.TITLE) != null) title = searchUsing.get(MediaField.TITLE).format(title);
		String artist = fieldData.getFirstData(MediaField.ARTIST);
		if (searchUsing.get(MediaField.ARTIST) != null) artist = searchUsing.get(MediaField.ARTIST).format(artist);
		String searchWord = (title + " " + artist).trim();
		if (StringUtils.isEmpty(searchWord)) {
			this.outputDataMap = null;
			return null;
		}

		String searchUrl;
		try {
			searchUrl = String.format(SEARCH_URL, URLEncoder.encode(title, "utf-8"), URLEncoder.encode(artist, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			this.outputDataMap = null;
			return null;
		}

		// 歌詞ページ取得
		return getTargetPage(searchUrl, SEARCH_ANCHOR_XPATH);
	}

}
