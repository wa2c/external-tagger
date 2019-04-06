package com.wa2c.java.externaltagger.controller.source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.wa2c.java.externaltagger.model.FieldDataMap;
import com.wa2c.java.externaltagger.value.MediaField;
import com.wa2c.java.externaltagger.value.SearchFieldUsing;



public class SourceKashiTime extends AbstractHtmlSource {

	/** サイト名。 */
	private static final String SITE_NAME = "歌詞タイム";
	/** 検索URI。 */
	private static final String SEARCH_URL = "http://www.kasi-time.com/allsearch.php?q=%s";
	//private static final String SEARCH_URL = "http://lyrics.kenichimaehashi.com/#site:www.kasi-time.com%%20%s";
	/** 検索結果アンカー。 */
	//private static final String SEARCH_ANCHOR_XPATH = "//a[@class='gs-title']";
	private static final String SEARCH_ANCHOR_XPATH = "//a[@class='gs-title']";

	private final static HashSet<MediaField> searchFieldSet = new HashSet<MediaField>() { {
		add(MediaField.TITLE);
		add(MediaField.ARTIST);
	} };

	protected final static HashMap<MediaField, SourceConversion> sourceConversionMap = new HashMap<MediaField, SourceConversion>() { {
		put(MediaField.TITLE        , new SourceConversion(MediaField.TITLE        , "normalize-space( //h1 )" ));
		put(MediaField.ARTIST       , new SourceConversion(MediaField.ARTIST       , "normalize-space( substring-before( //div[@class='person_list']//tbody/tr[1]/td, '関連リンク' ) )" ));
		put(MediaField.LYRICIST     , new SourceConversion(MediaField.LYRICIST     , "normalize-space(//div[@class='person_list']//tbody/tr[2]/td )" ));
		put(MediaField.COMPOSER     , new SourceConversion(MediaField.COMPOSER     , "normalize-space(//div[@class='person_list']//tbody/tr[3]/td )" ));
		put(MediaField.ARRANGER     , new SourceConversion(MediaField.ARRANGER     , "normalize-space(//div[@class='person_list']//tbody/tr[4]/td )" ));
		put(MediaField.COMMENT      , new SourceConversion(MediaField.COMMENT      , "normalize-space( //div[@class='other_list']//tbody/tr[1]/td )" ));
		put(MediaField.GENRE        , new SourceConversion(MediaField.GENRE        , "substring-before( normalize-space( //div[@class='other_list']//tbody/tr[1]/td ), ' ' )" ));
		put(MediaField.TITLE_SORT   , new SourceConversion(MediaField.TITLE_SORT   , "normalize-space( //div[@class='other_list']//tbody/tr[2]/td )" ));
		put(MediaField.LYRICS       , new SourceConversion(MediaField.LYRICS       , "//div[@id='lyrics']" ));
	} };




	public SourceKashiTime() {
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
		return sourceConversionMap.keySet();
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
			//searchUrl = String.format(SEARCH_URL, URLEncoder.encode(searchWord + " item", "utf-8")); // アーティスト結果がヒットしないよう、itemを追加
			searchUrl = String.format(SEARCH_URL, URLEncoder.encode(searchWord, "utf-8")); // アーティスト結果がヒットしないよう、itemを追加
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			this.outputDataMap = null;
			return null;
		}

		// 歌詞ページ取得
		return getTargetPage(searchUrl, SEARCH_ANCHOR_XPATH);
	}


}
