package com.wa2c.java.externaltagger.source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.wa2c.java.externaltagger.common.FieldDataMap;
import com.wa2c.java.externaltagger.value.MediaField;
import com.wa2c.java.externaltagger.value.SearchFieldUsing;



public class SourceAmazonJp extends AbstractHtmlSource {

	/** サイト名。 */
	private static final String SITE_NAME = "Amazon.co.jp";
	/** 検索URI。 */
	private static final String SEARCH_URL = "http://www.amazon.co.jp/s/?field-keywords=%s";
	/** 検索結果アンカー。 */
	private static final String SEARCH_ANCHOR_XPATH = "//*[@id=\"result_0\"]/div/div[2]/div[1]/a";

	private final static HashSet<MediaField> searchFieldSet = new HashSet<MediaField>() { {
		add(MediaField.TITLE);
		add(MediaField.ARTIST);
		add(MediaField.ALBUM);
	} };

	protected final static HashMap<MediaField, SourceConversion> sourceConversionMap = new HashMap<MediaField, SourceConversion>() { {
		put(MediaField.ALBUM_ARTIST , new SourceConversion(MediaField.ALBUM_ARTIST , "//*[@id=\"byline\"]/span[1]/a"));
		put(MediaField.MEDIA        , new SourceConversion(MediaField.MEDIA        , "//*[@id=\"byline\"]/span[3]"));
		put(MediaField.YEAR         , new SourceConversion(MediaField.YEAR         , "substring-before( substring-after( //*[@id=\"productDetailsTable\"]/tbody/tr/td/div/ul/li[1]/text(), '(') , ')')"));
		put(MediaField.RECORD_LABEL , new SourceConversion(MediaField.RECORD_LABEL , "//*[@id=\"productDetailsTable\"]/tbody/tr/td/div/ul/li[4]/text()"));
		put(MediaField.AMAZON_ID    , new SourceConversion(MediaField.AMAZON_ID    , "//*[@id=\"productDetailsTable\"]/tbody/tr/td/div/ul/li[6]/text()"));
	} };



	/**
	 * コンストラクタ。
	 */
	public SourceAmazonJp() {
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
		String album = fieldData.getFirstData(MediaField.ALBUM);
		if (searchUsing.get(MediaField.ALBUM) != null) album = searchUsing.get(MediaField.ALBUM).format(album);
		String searchWord = (album + " " + artist).trim();
		if (StringUtils.isEmpty(searchWord)) {
			this.outputDataMap = null;
			return null;
		}

		String searchUrl;
		try {
			searchUrl = String.format(SEARCH_URL, URLEncoder.encode(album + " " + artist + " CD", "utf-8")); // CDのみを対象とするため、CDを追加
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			this.outputDataMap = null;
			return null;
		}

		// 歌詞ページ取得
		return getTargetPage(searchUrl, SEARCH_ANCHOR_XPATH);
	}

}
