package com.wa2c.java.externaltagger.controller.source;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
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
		put(MediaField.TITLE        , new SourceConversion(MediaField.TITLE        , "substring-before(//*[@id=\"lpleftblock\"]/div[1]/div[3]/div/div/h1/span[1], '歌詞')" ));
		put(MediaField.ARTIST       , new SourceConversion(MediaField.ARTIST       , "//*[@id=\"lpleftblock\"]/div[1]/div[3]/div/div/h1/span[2]/a" ));
//		put(MediaField.COMPOSER       , new SourceConversion(MediaField.COMPOSER       , "//*[@id=\"Lyrics\"]/div[5]/table/thead/tr[4]/td" ));
//		put(MediaField.LYRICIST       , new SourceConversion(MediaField.LYRICIST       , "//*[@id=\"Lyrics\"]/div[5]/table/thead/tr[3]/td" ));
		put(MediaField.COMMENT      , new SourceConversion(MediaField.COMMENT      , "string(//*[@id=\"lpleftblock\"]/div[1]/div[3]/div/div/h1/span[3])" ));
		put(MediaField.LYRICS       , new SourceConversion(MediaField.LYRICS       , "//*[@id=\"Original\"]/div[3]" ) {{ parseType = 0; }} );
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
		Logger.d("Search: title=\"" + title + "\", artist=\"" + artist + "\"");

		try (WebClient webClient = getWebClient()) {
			// 検索結果URL取得
			Logger.d("Search Page Url=" + SEARCH_URL);
			HtmlPage page = webClient.getPage(SEARCH_URL);
			Logger.d("Search Page done.");
			//Logger.d(page.asXml());

			List<HtmlTextInput> textInput = page.getByXPath("//*[@id=\"gsc-i-id1\"]");
			if (textInput == null || textInput.isEmpty())
				return null;
			textInput.get(0).setText(title + " " + artist);

			List<HtmlElement> button = page.getByXPath("//*[@id=\"___gcse_0\"]/div/div/form/table/tbody/tr/td[2]/button");
			if (button == null || button.isEmpty())
				return null;
			button.get(0).click();

			List<HtmlAnchor> anchor = page.getByXPath("//*/a[@class=\"gs-title\"]");
			if (anchor == null || anchor.isEmpty())
				return null;
			String url = anchor.get(0).getHrefAttribute();

			Logger.d("Lyrics Page Url=" + url);
			HtmlPage lyricsPage = webClient.getPage(url);
			final FieldDataMap outputData = getLyricsPageData(lyricsPage);
			Logger.d("Lyrics Page done.");
			Logger.d("FieldDataMap=" + outputData.toString());

			//List<HtmlTable> infoTables = lyricsPage.getByXPath("//*[@id=\"Lyrics\"]/div[6]/table");
			List<HtmlUnorderedList> infoTables = lyricsPage.getByXPath("//*[@id=\"Original\"]/div[8]/div/ul");
			if (!infoTables.isEmpty()) {
				HtmlUnorderedList list = infoTables.get(0);

				// 作曲者・作詞者
				list.getChildren().forEach(row -> {
					Iterator<DomNode> iterator = row.getChildren().iterator();
					String key = iterator.next().getVisibleText();
					String value = iterator.next().getVisibleText().trim();
					if (key.contains("作曲")) {
						outputData.putNewData(MediaField.COMPOSER, value);
					} else if (key.contains("作詞")) {
						outputData.putNewData(MediaField.LYRICIST, value);
					}
				});
//						.getRows().forEach(row -> {
//					if (row.getCell(0).getVisibleText().contains("作曲")) {
//						outputData.putNewData(MediaField.COMPOSER, row.getCell(1).getVisibleText().trim());
//					}
//					if (row.getCell(0).getVisibleText().contains("作詞")) {
//						outputData.putNewData(MediaField.LYRICIST, row.getCell(1).getVisibleText().trim());
//					}
//				});
			}



//			// コメント拡張情報情報
//			String workTitle = outputData.getFirstData(MediaField.COMMENT);
//			List<HtmlAnchor> extraAnchor = lyricsPage.getByXPath(sourceConversionMap.get(MediaField.COMMENT).xPath);
//			if (extraAnchor != null && !extraAnchor.isEmpty()) {
//				HtmlPage extraPage = webClient.getPage(extraAnchor.get(0).getHrefAttribute());
//				List<HtmlElement> workGenres = extraPage.getByXPath("//*[@id=\"content2\"]/div[1]/div/div/div/div[1]/span[2]");
//				String workGenre = "";
//				if (!workGenres.isEmpty()) {
//					workGenre  = workGenres.get(0).asText();
//				}
//
//				List<HtmlTable> workTable = extraPage.getByXPath("//*[@id=\"content2\"]/div[1]/div/div/div/table");
//				if (!workTable.isEmpty()) {
//					String opedType = "";
//					HtmlTable table = workTable.get(0);
//					for (int i = 0; i < table.getRowCount(); i++) {
//						HtmlTableRow row = table.getRow(0);
//						String rowTitle = table.getCellAt(i, 0).asText().trim();
//						if (rowTitle.contains(outputData.getFirstData(MediaField.TITLE))) {
//							opedType = table.getCellAt(i, 2).getFirstElementChild().getTextContent();
//							break;
//						}
//					}
//					List<String> works = new ArrayList<>();
//					works.add(workGenre);
//					works.add(workTitle);
//					works.add(opedType);
//					outputData.putNewData(MediaField.COMMENT, StringUtils.join(works, " "));
//				}
//			}

			outputData.put(MediaField.GENRE, getGenreCode(outputData.getFirstData(MediaField.COMMENT)));
			return outputData;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * ジャンルの日本名
	 */
	private String getGenreJp(String genre) {
		if (genre.contains("Anime"))
			return "アニメ";
		else if (genre.contains("Movie"))
			return "劇場アニメ";
		else if (genre.contains("Game"))
			return "ゲーム";
		else
			return null;
	}

	/**
	 * ジャンルのコード
	 */
	private String getGenreCode(String genre) {
		if (StringUtils.isEmpty(genre)) {
			return "JPop";
		} if (genre.contains("Anime") || genre.contains("アニメ"))
			return "Anime";
		else if (genre.contains("Movie") || genre.contains("映画"))
			return "Anime";
		else if (genre.contains("Game") || genre.contains("ゲーム"))
			return "Game";
		else
			return "JPop";
	}

}
