package com.wa2c.java.externaltagger.controller.source;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.wa2c.java.externaltagger.common.Logger;
import com.wa2c.java.externaltagger.model.FieldDataMap;
import com.wa2c.java.externaltagger.value.MediaField;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class AbstractHtmlSource extends AbstractExternalSource {

	protected abstract String getSearchAnchorXPath();

	protected static WebClient getWebClient() {
		WebClient _webClient = new WebClient(BrowserVersion.FIREFOX_78);
		_webClient.getOptions().setJavaScriptEnabled(true);
		_webClient.getOptions().setDownloadImages(false);
		_webClient.getOptions().setRedirectEnabled(true);
		_webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		_webClient.waitForBackgroundJavaScript(5000);
		_webClient.getOptions().setRedirectEnabled(true);
		_webClient.getOptions().setThrowExceptionOnScriptError(false);
		_webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		_webClient.getOptions().setCssEnabled(false);
		_webClient.getOptions().setUseInsecureSSL(true);
		_webClient.getCookieManager().setCookiesEnabled(true);

		_webClient.setAjaxController(new AjaxController(){
			@Override
			public boolean processSynchron(HtmlPage page, WebRequest request, boolean async)
			{
				return true;
			}
		});

//		// Disable log
//		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
//		System.setProperty("org.Apache.commons.logging.Log", "org.Apache.commons.logging.impl.NoOpLog");

		return _webClient;
	}


	public abstract Set<MediaField>  getSearchField();

	public abstract HashMap<MediaField, SourceConversion> getConversionMap();


	/**
	 * Get HTML Source Text List.
	 * @param pageElement HTML
	 * @param sourceConversion 変換内容。
	 * @return
	 */
	protected List<String> getElementText(HtmlPage pageElement, SourceConversion sourceConversion) {
		if (pageElement == null || sourceConversion == null)
			return null;

		List<?> elements = pageElement.getByXPath(sourceConversion.xPath);
		if (elements.size() == 0)
			return null;

		// テキスト取得
		String text;
		try {
			Object element = elements.get(0);
			if (element instanceof HtmlElement) {
				HtmlElement data = (HtmlElement) element;

				if (sourceConversion.parseType == 1) {
					StringBuilder builder = new StringBuilder();
					parseLyrics(builder, data);
					text = builder.toString().trim();
				} else {
					text = data.asText();
				}
			} else if (element instanceof DomText) {
				DomText domText = (DomText) element;
				text = domText.getWholeText();
			} else {
				text = element.toString();
			}
		} catch (ClassCastException e) {
			text = elements.get(0) .toString();
		}
		if (StringUtils.isEmpty(text))
			return null;

		// 分割
		List<String> textList = new ArrayList<>();
		if (!StringUtils.isEmpty(sourceConversion.splitText)) {
			textList.addAll(Arrays.asList(text.split(sourceConversion.splitText)));
		} else {
			textList.add(text);
		}

		Stream<String> textStream = textList.stream();

		// トリミング
		if (sourceConversion.trimSpace) {
			textStream = textStream.map(e -> e.replaceAll("^[\\s　\t]*", "").replaceAll("[\\s　]*$", ""));
		}

		// 削除
		if (!StringUtils.isEmpty(sourceConversion.removeRegexp)) {
			textStream = textStream.map(e -> e.replaceAll(sourceConversion.removeRegexp, ""));
		}

		// 置換
		if (!StringUtils.isEmpty(sourceConversion.replaceRegexp)) {
			textStream = textStream.map(e -> e.replaceAll(sourceConversion.replaceRegexp, sourceConversion.replaceOutput == null ? "" : sourceConversion.replaceOutput));
		}

		//return textList;
		return textStream.collect(Collectors.toList());
	}

	/**
	 * Parse lyrics
	 * @param builder String builder.
	 * @param parent Parent node.
	 */
	private void parseLyrics(StringBuilder builder, DomNode parent) {
		if (parent == null)
			return;
		if (parent.hasChildNodes()) {
			for (DomNode child : parent.getChildren()) {
				parseLyrics(builder, child);
			}
		} else {
			//String br = "\r\n\r\n";
			String br = "\r\n";
			String text = parent.getTextContent().trim();
			if (parent instanceof HtmlBreak) {
				br = "\r\n";
			} else if (text.isEmpty()) {
				br = "\r\n";
			}
			builder.append(text).append(br);
		}
	}

	/**
	 * ページを取得する。
	 * @param searchUrl 検索URL。
	 * @return ページ。
	 */
	protected FieldDataMap getMeidaData(String searchUrl) {
		Logger.d("Search Page URL: " + searchUrl);

        try (WebClient webClient = getWebClient()) {
            String lyricsPageUrl = getLyricsPageUrl(webClient, searchUrl);
			HtmlPage lyricsPage = webClient.getPage(lyricsPageUrl);
            return getLyricsPageData(lyricsPage);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
	}

	/**
	 * 歌詞ページのURLを取得
	 * @return 歌詞ページのURL
	 */
	protected String getLyricsPageUrl(WebClient webClient, String searchUrl) throws IOException {
		HtmlPage page = webClient.getPage(searchUrl);
		Logger.d(page.asXml());

		List<?> list = page.getByXPath(getSearchAnchorXPath());
		if (list == null || list.size() == 0) {
			return null; // TODO LOG
		}

		HtmlAnchor anchor = (HtmlAnchor)list.get(0);
		String relativeUrl = anchor.getHrefAttribute();
		String absoluteUrl =  UrlUtils.resolveUrl(page.getBaseURL(), relativeUrl);
		Logger.d("Lyrics Page URL: " + absoluteUrl);

		return absoluteUrl;
	}

	/**
	 * 歌詞ページからデータを取得
	 * @return 歌詞ページの\\出力データ
	 */
	protected FieldDataMap getLyricsPageData(HtmlPage lyricsPage) throws IOException {
		FieldDataMap outputData = new FieldDataMap();
		for (MediaField field : getResultField()) {
			List<String> values = getElementText(lyricsPage, getConversionMap().get(field));
			if (values == null)
				continue;
			if (field != MediaField.LYRICS) {
				outputData.put(field, values);
			} else {
				outputData.put(field, StringUtils.join(values, "\r\n"));
			}
		}
		return outputData;
	}

}
