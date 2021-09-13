package com.wa2c.java.externaltagger.controller.source;

import com.wa2c.java.externaltagger.model.FieldDataMap;
import com.wa2c.java.externaltagger.value.MediaField;
import com.wa2c.java.externaltagger.value.SearchFieldUsing;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SourcePetitLyrics extends AbstractHtmlSource {

    /** サイト名。 */
    private static final String SITE_NAME = "プチリリ";
    /** 検索URI。 */
    private static final String SEARCH_URL = "https://petitlyrics.com/search_lyrics?title=%s&artist=%s&album=%s";
    /** 検索結果アンカー。 */
    private static final String SEARCH_ANCHOR_XPATH = "//*[@id=\"lyrics_list\"]/tbody/tr[1]/td[2]/a[1]";

    private final static HashSet<MediaField> searchFieldSet = new HashSet<MediaField>() { {
        add(MediaField.TITLE);
        add(MediaField.ARTIST);
        add(MediaField.ALBUM);
    } };

    protected final static HashMap<MediaField, SourceConversion> sourceConversionMap = new HashMap<MediaField, SourceConversion>() { {
        put(MediaField.TITLE        , new SourceConversion(MediaField.TITLE        , "/html/body/div[3]/div[1]/div/div/div[2]/text()" ));
        put(MediaField.ARTIST       , new SourceConversion(MediaField.ARTIST       , "//*[@id=\"lyrics_list\"]/tbody/tr/td/div[1]/div/div[1]/p/a[1]/text()" ));
        put(MediaField.ALBUM        , new SourceConversion(MediaField.ALBUM        , "//*[@id=\"lyrics_list\"]/tbody/tr/td/div[1]/div/div[1]/p/a[2]/text()" ));
        put(MediaField.LYRICIST     , new SourceConversion(MediaField.LYRICIST     , "//*[@id=\"lyrics_list\"]/tbody/tr/td/div[1]/div/div[1]/p/text()[3]" ));
        put(MediaField.COMPOSER     , new SourceConversion(MediaField.COMPOSER     , "//*[@id=\"lyrics_list\"]/tbody/tr/td/div[1]/div/div[1]/p/text()[4]" ));
        put(MediaField.LYRICS       , new SourceConversion(MediaField.LYRICS       , "//*[@id=\"lyrics\"]/text()" ));
    } };



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
    protected String getSearchAnchorXPath() { return SEARCH_ANCHOR_XPATH; }

    @Override
    public FieldDataMap getFieldDataMap(FieldDataMap fieldData, Map<MediaField, SearchFieldUsing> searchUsing) {
        // 検索テキスト
        String title = fieldData.getFirstData(MediaField.TITLE);
        if (searchUsing.get(MediaField.TITLE) != null) title = searchUsing.get(MediaField.TITLE).format(title);
        String artist = fieldData.getFirstData(MediaField.ARTIST);
        if (searchUsing.get(MediaField.ARTIST) != null) artist = searchUsing.get(MediaField.ARTIST).format(artist);
        String album = fieldData.getFirstData(MediaField.ALBUM);
        if (searchUsing.get(MediaField.ALBUM) != null) artist = searchUsing.get(MediaField.ALBUM).format(artist);

        String searchUrl;
        try {
            searchUrl = String.format(SEARCH_URL, URLEncoder.encode(title, "utf-8"), URLEncoder.encode(artist, "utf-8"), URLEncoder.encode(album, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        // 歌詞ページ取得
        return getMeidaData(searchUrl);
    }
}
