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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class AbstractHtmlSource extends AbstractExternalSource {



	private static WebClient getWebClient() {
		    WebClient _webClient = new WebClient(BrowserVersion.CHROME);
			_webClient.getOptions().setJavaScriptEnabled(true);
			_webClient.getOptions().setDownloadImages(false);
			_webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			_webClient.waitForBackgroundJavaScript(5000);
			_webClient.getOptions().setRedirectEnabled(true);
			_webClient.getOptions().setThrowExceptionOnScriptError(false);
			_webClient.getOptions().setCssEnabled(false);
			_webClient.getOptions().setUseInsecureSSL(true);
			_webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
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

	/**
	 * Source HTML Element.
	 */
	protected static class SourceConversion {
		MediaField mediaField = null;
		public String xPath = null;
		public boolean brNewline = true;
		public boolean trimSpace = true;
		public String splitText = null;
		public String removeRegexp = null;
		public String replaceRegexp = null;
		public String replaceOutput = null;

		public SourceConversion(MediaField mediaField, String xPath) {
			this.mediaField = mediaField;
			this.xPath = xPath;
		}
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
	 * @param searchAnchorXPath 検索結果のアンカーXPath。
	 * @return ページ。
	 */
	protected FieldDataMap getTargetPage(String searchUrl, String searchAnchorXPath) {
		Logger.d("Search Page URL: " + searchUrl);
		Logger.d("Search Anchor XPath: " + searchAnchorXPath);

		WebClient webClient = null;
		FieldDataMap outputData = new FieldDataMap();
		try {
			webClient = getWebClient();

			// 検索結果URL取得
			HtmlPage page = (HtmlPage)webClient.getPage(searchUrl);
			webClient.waitForBackgroundJavaScript(2000);
			Logger.d(page.asXml());


			List<?> list = page.getByXPath(searchAnchorXPath);
			if (list == null || list.size() == 0) {
				return null; // TODO LOG
			}

			HtmlAnchor anchor = (HtmlAnchor)list.get(0);
			String mediaUrl = anchor.getHrefAttribute();
			// TODO LOG

			String lyricsPageUrl =  UrlUtils.resolveUrl(page.getBaseURL(), mediaUrl);
			Logger.d("Lyrics Page URL: " + lyricsPageUrl);

			// 歌詞ページ取得
			HtmlPage mediaPage = (HtmlPage)webClient.getPage(lyricsPageUrl);

			// 歌詞ページ取得
			for (MediaField field : getResultField()) {
				outputData.put(field, getElementText(mediaPage, getConversionMap().get(field)));
			}

			this.outputDataMap = outputData;
			return outputData;
		} catch (Exception ex) {
			ex.printStackTrace();
			this.outputDataMap = null;
			return null;
		} finally {
			if (webClient != null)
				webClient.close();
		}
	}

}
