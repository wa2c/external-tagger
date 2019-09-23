package com.wa2c.java.externaltagger.controller.source;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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
		    WebClient _webClient = new WebClient(BrowserVersion.FIREFOX_60);
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
			HtmlElement data = (HtmlElement)elements.get(0);
			text = data.asText();
		} catch (ClassCastException e) {
			text = elements.get(0).toString();
		}
		if (StringUtils.isEmpty(text))
			return null;


		// 分割

		List<String> textList = new ArrayList<String>();
		if (!StringUtils.isEmpty(sourceConversion.splitText)) {
			textList.addAll(Arrays.asList(text.split(sourceConversion.splitText)));
		} else {
			textList.add(text);
		}

		Stream<String> textStream = textList.stream();


		// 改行
		if (sourceConversion.brNewline) {
			textStream = textStream.map(e -> e.replaceAll("(\r\n|\n|\r)", "\r\n"));
		}

		// トリミング
		if (sourceConversion.trimSpace) {
			textStream = textStream.map(e -> e.replaceAll("^[\\s　]*", "").replaceAll("[\\s　]*$", ""));
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
	 * ページを取得する。
	 * @param searchUrl 検索URL。
	 * @return ページ。
	 */
	//protected FieldDataMap getTargetPage(String searchUrl, String searchAnchorXPath) {
	protected FieldDataMap getTargetPage(String searchUrl) {
		Logger.d("Search Page URL: " + searchUrl);

		WebClient webClient = null;
		//FieldDataMap outputData = new FieldDataMap();
		try {
			webClient = getWebClient();
			String lyricsPageUrl = getLyricsPageUrl(webClient, searchUrl);
			this.outputDataMap = getLyricsPageData(webClient, lyricsPageUrl);
			return this.outputDataMap;
		} catch (Exception ex) {
			ex.printStackTrace();
			this.outputDataMap = null;
			return null;
		} finally {
			if (webClient != null)
				webClient.close();
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
	protected FieldDataMap getLyricsPageData(WebClient webClient, String lyricsPageUrl) throws IOException {
		FieldDataMap outputData = new FieldDataMap();
		HtmlPage lyricsPage = webClient.getPage(lyricsPageUrl);
		for (MediaField field : getResultField()) {
			outputData.put(field, getElementText(lyricsPage, getConversionMap().get(field)));
		}
		return outputData;
	}

}
