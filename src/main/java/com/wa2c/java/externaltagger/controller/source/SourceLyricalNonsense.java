package com.wa2c.java.externaltagger.controller.source;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.wa2c.java.externaltagger.common.Logger;
import com.wa2c.java.externaltagger.model.FieldDataMap;
import com.wa2c.java.externaltagger.value.MediaField;
import com.wa2c.java.externaltagger.value.SearchFieldUsing;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


public class SourceLyricalNonsense extends AbstractHtmlSource {

	private static final String SITE_NAME = "LYRICS NONSENSE";
	/** 検索URI。 */
	private static final String SEARCH_URL = "https://www.lyrical-nonsense.com/";
	/** 検索結果アンカー。 */
	//private static final String SEARCH_ANCHOR_XPATH = "//*[@id=\"lyricList\"]/div[2]/div[2]/a";
	private static final String SEARCH_ANCHOR_XPATH = "//*[@id=\"mnb\"]/div[2]/p[1]/a";

	private final static HashSet<MediaField> searchFieldSet = new HashSet<MediaField>() { {
		add(MediaField.TITLE);
		add(MediaField.ARTIST);
	} };

	protected final static HashMap<MediaField, SourceConversion> sourceConversionMap = new HashMap<MediaField, SourceConversion>() { {
		put(MediaField.TITLE        , new SourceConversion(MediaField.TITLE        , "substring-before(//*[@id=\"Lyrics\"]/div[1]/div, '歌詞')" ));
		put(MediaField.ARTIST       , new SourceConversion(MediaField.ARTIST       , "//*[@id=\"Lyrics\"]/div[1]/table/thead/tr[1]/td/a" ));
		put(MediaField.COMMENT      , new SourceConversion(MediaField.COMMENT      , "//*[@id=\"content2\"]/div[2]/div/span[@class=\"lptitlet\"]" ));
		put(MediaField.LYRICS       , new SourceConversion(MediaField.LYRICS       , "//*[@id=\"Lyrics\"]/div[3]/div" ) {{ parseType = 1; }} );
	} };

	public SourceLyricalNonsense() {

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
	protected String getSearchAnchorXPath() { return SEARCH_ANCHOR_XPATH; }

	@Override
	public FieldDataMap getFieldDataMap(FieldDataMap fieldData, Map<MediaField, SearchFieldUsing> searchUsing) {
		String title = fieldData.getFirstData(MediaField.TITLE);
		if (searchUsing.get(MediaField.TITLE) != null) title = searchUsing.get(MediaField.TITLE).format(title);
		String artist = fieldData.getFirstData(MediaField.ARTIST);
		if (searchUsing.get(MediaField.ARTIST) != null) artist = searchUsing.get(MediaField.ARTIST).format(artist);
		String searchWord = (title + " " + artist).trim();
		if (StringUtils.isEmpty(searchWord)) {
			return null;
		}

		FieldDataMap outputData = new FieldDataMap();
		try (WebClient webClient = getWebClient()) {
			// 検索結果URL取得
			HtmlPage page = webClient.getPage(SEARCH_URL);
			Logger.d(page.asXml());

			List<HtmlTextInput> textInput = page.getByXPath("//*[@id=\"gsc-i-id1\"]");
			if (textInput == null || textInput.isEmpty())
				return null;
			textInput.get(0).setText(title + " " + artist);

			List<HtmlElement> button = page.getByXPath("//*[@id=\"___gcse_0\"]/div/div/form/table/tbody/tr/td[2]/button");
			if (button == null || button.isEmpty())
				return null;
			button.get(0).click();

			List<HtmlAnchor> anchor = page.getByXPath("//*[@id=\"___gcse_0\"]/div/div/div/div[5]/div[2]/div/div/div[1]/div[1]/div[1]/div[1]/div/a");
			if (anchor == null || anchor.isEmpty())
				return null;
			String url = anchor.get(0).getHrefAttribute();
			outputData = getLyricsPageData(webClient, url);

			String comment = outputData.getFirstData(MediaField.COMMENT);
			if (StringUtils.isNotEmpty(comment)) {
				if (comment.contains("アニメ"))
					outputData.put(MediaField.GENRE, "Anime");
				else if (comment.contains("劇場"))
					outputData.put(MediaField.GENRE, "Anime");
				else if (comment.contains("ゲーム"))
					outputData.put(MediaField.GENRE, "Game");
			}
			if (StringUtils.isEmpty(outputData.getFirstData(MediaField.COMMENT)))
				outputData.put(MediaField.GENRE, "JPop");


		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

		return outputData;
	}

}
