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



public class SourceJLyrics extends AbstractHtmlSource {

	private static final String SITE_NAME = "J-Lyrics";
	/** 検索URI。 */
	private static final String SEARCH_URL = "http://search2.j-lyric.net/index.php?kt=%s&ct=2&ka=%s&ca=2";
	/** 検索結果アンカー。 */
	//private static final String SEARCH_ANCHOR_XPATH = "//*[@id=\"lyricList\"]/div[2]/div[2]/a";
    private static final String SEARCH_ANCHOR_XPATH = "//*[@id=\"mnb\"]/div[2]/p[1]/a";

	private final static HashSet<MediaField> searchFieldSet = new HashSet<MediaField>() { {
		add(MediaField.TITLE);
		add(MediaField.ARTIST);
	} };

	protected final static HashMap<MediaField, SourceConversion> sourceConversionMap = new HashMap<MediaField, SourceConversion>() { {
		put(MediaField.TITLE        , new SourceConversion(MediaField.TITLE        , "//*[@id=\"mnb\"]/div[2]/h2" ));
		put(MediaField.ARTIST       , new SourceConversion(MediaField.ARTIST       , "//*[@id=\"mnb\"]/div[3]/p[1]/a" ));
		put(MediaField.LYRICIST     , new SourceConversion(MediaField.LYRICIST     , "substring-after(//*[@id=\"mnb\"]/div[3]/p[2], '：')" ));
		put(MediaField.COMPOSER     , new SourceConversion(MediaField.COMPOSER     , "substring-after(//*[@id=\"mnb\"]/div[3]/p[3], '：')"));
		put(MediaField.LYRICS       , new SourceConversion(MediaField.LYRICS       , "//*[@id=\"Lyric\"]" ));
	} };

//    protected final static HashMap<MediaField, SourceConversion> sourceConversionMap = new HashMap<MediaField, SourceConversion>() { {
//        put(MediaField.TITLE        , new SourceConversion(MediaField.TITLE        , "//*[@id=\"lyricBlock\"]/div[1]/h2" ));
//        put(MediaField.ARTIST       , new SourceConversion(MediaField.ARTIST       , "substring-after(//*[@id=\"lyricBlock\"]/div[2]/table/tbody/tr/td[1], '：')" ));
//        put(MediaField.LYRICIST     , new SourceConversion(MediaField.LYRICIST     , "substring-after(//*[@id=\"lyricBlock\"]/div[2]/table/tbody/tr/td[2], '：')" ));
//        put(MediaField.COMPOSER     , new SourceConversion(MediaField.COMPOSER     , "substring-after(//*[@id=\"lyricBlock\"]/div[2]/table/tbody/tr/td[3], '：')"));
//        put(MediaField.LYRICS       , new SourceConversion(MediaField.LYRICS       , "//*[@id=\"lyricBody\"]" ));
//    } };


	public SourceJLyrics() {

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
        FieldDataMap outputMap = getTargetPage(searchUrl, SEARCH_ANCHOR_XPATH);
		if (outputMap != null) {
			// 特殊置換え
			String outputTitle = outputMap.getFirstData(MediaField.TITLE);
			if (StringUtils.isNotEmpty(outputTitle)) {
				outputMap.putNewData(MediaField.TITLE, outputTitle.replaceFirst(" 歌詞$", ""));
			}
		}
		return outputMap;
	}

}
