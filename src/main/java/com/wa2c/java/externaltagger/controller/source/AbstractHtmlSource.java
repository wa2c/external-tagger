package com.wa2c.java.externaltagger.controller.source;

import com.gargoylesoftware.css.parser.selector.SelectorSpecificity;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.css.StyleElement;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.wa2c.java.externaltagger.common.Logger;
import com.wa2c.java.externaltagger.model.FieldDataMap;
import com.wa2c.java.externaltagger.value.MediaField;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import us.codecraft.xsoup.XElements;
import us.codecraft.xsoup.Xsoup;

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

			if (sourceConversion.parseType == 1) {
				StringBuilder builder = new StringBuilder();
				parseLyrics(builder, data);
				text = builder.toString().trim();
			} else {
				text = data.asText();
			}

			/*
		StyleElement styleElement = new StyleElement("white-space", "pre-wrap", "", SelectorSpecificity.DEFAULT_STYLE_ATTRIBUTE);
		Map<String, StyleElement> style = new HashMap<>();
		style.put("white-space", styleElement);
			Map<String, StyleElement> stylemap = data.getStyleMap();
			stylemap.put("aaa", styleElement);
			data.writeStyleToElement(stylemap);
			//data.writeStyleToElement(style);



			text = data.asText();

			String xml = data.asXml();
			String xml3 = xml
					.replaceAll("(?i)<br", "\r\n<br")
					.replaceAll("(?i)<p", "\r\n<p");
			String test = Jsoup.clean(xml3, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false) );
			String test2 = Jsoup.clean(xml3, "", Whitelist.none(), new Document.OutputSettings() );

//			//get the HTML from the document, and retaining original new lines
//			String str = doc.html().replaceAll("\\\\n", "\n");

			String xml2 = xml.replaceAll("(\r\n|\n|\r)", "<br />");



//			TextNode node =  (TextNode)Jsoup.parse(xml).childNodes().get(0);
//			String wholeText = node.getWholeText();
			Elements aabb = Xsoup.compile(sourceConversion.xPath).evaluate(Jsoup.parse(pageElement.asXml())).getElements();


			Document doc = Jsoup.parse(xml);
			Document doc2 = Jsoup.parse(xml2);
			Document doc3 = Jsoup.parse(xml3);

			String text1 = doc.text();
			String text2 = doc2.text();
			String text3 = doc3.text();
			Logger.d(text1);
			Logger.d(text2);

//  ここ編集厨
//			// get pretty printed html with preserved br and p tags
//			String prettyPrintedBodyFragment = Jsoup.clean(data.asXml(), "", Whitelist.none().addTags("br"), new Document.OutputSettings().prettyPrint(true));
//			// get plain text with preserved line breaks by disabled prettyPrint
//			String aaa =  Jsoup.clean(prettyPrintedBodyFragment, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
//			Logger.d(aaa);
//
////			String text2 = Jsoup.parse(data.asXml());
////			Logger.d(text2);
//
////			String xml = ;
////			String text2 = data.asText();
////			text = data.getTextContent();
//			String dd = data.asXml();




//			Parser parser = new Parser();
//			try {
//				parser.setProperty(Parser.schemaProperty, new HTMLSchema());
//				parser.parse(new InputSource(new StringReader(dd)));
//				String aaaa = parser.toString();
//				Logger.d(aaaa);
//			} catch (Exception e) {
//				// Should not happen.
//				throw new RuntimeException(e);
//			}


//            text = data.asXml().replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "");

			 */
		} catch (ClassCastException e) {
			text = elements.get(0).toString();
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


//		// 改行
//		if (sourceConversion.brNewline) {
//			textStream = textStream.map(e -> e.replaceAll("(\r\n|\n|\r)", "\r\n"));
//		}

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
			String br = "\r\n\r\n";
			if (parent instanceof HtmlBreak) {
				br = "\r\n";
			}
			builder.append(parent.getTextContent()).append(br);
		}
	}

//	private void parseLyrics(StringBuilder builder, DomElement element) {
//		if (element == null)
//			return;
//		if (element.getChildElementCount() > 0) {
//			for (DomNode node : element.getChildren()) {
//				parseLyrics(builder, domElement);
//			}
//		} else {
//			builder.append(element.getTextContent()).append("\r\n\r\n");
//		}
//	}


	/**
	 * ページを取得する。
	 * @param searchUrl 検索URL。
	 * @return ページ。
	 */
	protected FieldDataMap getMeidaData(String searchUrl) {
		Logger.d("Search Page URL: " + searchUrl);

        try (WebClient webClient = getWebClient()) {
            String lyricsPageUrl = getLyricsPageUrl(webClient, searchUrl);
            return getLyricsPageData(webClient, lyricsPageUrl);
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
	protected FieldDataMap getLyricsPageData(WebClient webClient, String lyricsPageUrl) throws IOException {
		FieldDataMap outputData = new FieldDataMap();
		HtmlPage lyricsPage = webClient.getPage(lyricsPageUrl);
		for (MediaField field : getResultField()) {
			List<String> values = getElementText(lyricsPage, getConversionMap().get(field));
			if (field != MediaField.LYRICS) {
				outputData.put(field, values);
			} else {
				outputData.put(field, StringUtils.join(values, "\r\n"));
			}
		}
		return outputData;
	}

}
