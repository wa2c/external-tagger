package com.wa2c.java.externaltagger.controller.source;

import com.wa2c.java.externaltagger.model.FieldDataMap;
import com.wa2c.java.externaltagger.value.MediaField;
import com.wa2c.java.externaltagger.value.SearchFieldUsing;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class SourceJoySound extends AbstractHtmlSource {

	private static final String SITE_NAME = "JOYSOUND";
	/** 検索URI。 */
	private static final String SEARCH_URL = "http://search.j-lyric.net/index.php?kt=%s&ct=0&ka=%s&ca=0";
	//private static final String SEARCH_URL = "https://www.google.co.jp/search?num=5&q=site%3Awww.joysound.com+%MEDIA_TITLE%+%MEDIA_ARTIST%";
	//private static final String SEARCH_URL = "https://www.google.co.jp/search?num=5&ie=utf-8&oe=utf-8&hl=ja&q=site%%3Awww.joysound.com+%s+%s";


	/** 検索結果アンカー。 */
	private static final String SEARCH_ANCHOR_XPATH = "//*[@id=\"rso\"]/div/div[1]/div/h3/a";

	private final static HashSet<MediaField> searchFieldSet = new HashSet<MediaField>() { {
		add(MediaField.TITLE);
		add(MediaField.ARTIST);
	} };

	protected final static HashMap<MediaField, SourceConversion> sourceConversionMap = new HashMap<MediaField, SourceConversion>() { {
		put(MediaField.TITLE        , new SourceConversion(MediaField.TITLE        , "//*[@id=\"jp-cmp-main\"]/section[1]/header/h1" ));
		put(MediaField.ARTIST       , new SourceConversion(MediaField.ARTIST       , "//*[@id=\"jp-cmp-main\"]/section[1]/div[1]/div[1]/div[2]/table/tbody/tr[1]/td/div/p/a" ));
		put(MediaField.LYRICIST     , new SourceConversion(MediaField.LYRICIST     , "//*[@id=\"jp-cmp-main\"]/section[1]/div[1]/div[1]/div[2]/table/tbody/tr[2]/td/div/p/span" ));
		put(MediaField.COMPOSER     , new SourceConversion(MediaField.COMPOSER     , "//*[@id=\"jp-cmp-main\"]/section[1]/div[1]/div[1]/div[2]/table/tbody/tr[3]/td/div/p/span"));
		put(MediaField.COMMENT      , new SourceConversion(MediaField.COMMENT      , "//*[@id=\"jp-cmp-main\"]/section[1]/div[1]/div[1]/div[2]/table/tbody/tr[4]/td/div/a"));
		put(MediaField.LYRICS       , new SourceConversion(MediaField.LYRICS       , "//*[@id=\"lyrics\"]/div/div[2]/div[1]/div/div[1]/p" ));
	} };



	public SourceJoySound() {

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
	protected String getSearchAnchorXPath() { return SEARCH_ANCHOR_XPATH ;}

	@Override
	public FieldDataMap getFieldDataMap(FieldDataMap fieldData, Map<MediaField, SearchFieldUsing> searchUsing) {
		this.inputDataMap = fieldData;

		// 検索テキスト
		String title = fieldData.getFirstData(MediaField.TITLE);
		if (searchUsing.get(MediaField.TITLE) != null) title = searchUsing.get(MediaField.TITLE).format(title);
		String artist = fieldData.getFirstData(MediaField.ARTIST);
		if (searchUsing.get(MediaField.ARTIST) != null) artist = searchUsing.get(MediaField.ARTIST).format(artist);
		if (StringUtils.isEmpty(title) && StringUtils.isEmpty(artist)) {
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
		return getTargetPage(searchUrl);
	}

}
