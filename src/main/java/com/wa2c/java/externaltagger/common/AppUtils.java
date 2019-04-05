package com.wa2c.java.externaltagger.common;

import java.text.Normalizer;

import com.wa2c.java.externaltagger.value.SearchFieldUsing;
import org.apache.commons.lang3.StringUtils;

public final class AppUtils {

	/**
	 * Unicodeの空白。
	 */
	private static final String UNICODE_SPACES = "[" +
			"\\u0009-\\u000d" +     //  # White_Space # Cc   [5] <control-0009>..<control-000D>
			"\\u0020" +             // White_Space # Zs       SPACE
			"\\u0085" +             // White_Space # Cc       <control-0085>
			"\\u00a0" +             // White_Space # Zs       NO-BREAK SPACE
			"\\u1680" +             // White_Space # Zs       OGHAM SPACE MARK
			"\\u180E" +             // White_Space # Zs       MONGOLIAN VOWEL SEPARATOR
			"\\u2000-\\u200a" +    // # White_Space # Zs  [11] EN QUAD..HAIR SPACE
			"\\u2028" +             // White_Space # Zl       LINE SEPARATOR
			"\\u2029" +             // White_Space # Zp       PARAGRAPH SEPARATOR
			"\\u202F" +             // White_Space # Zs       NARROW NO-BREAK SPACE
			"\\u205F" +             // White_Space # Zs       MEDIUM MATHEMATICAL SPACE
			"\\u3000" +             // White_Space # Zs       IDEOGRAPHIC SPACE
			"]";

	/**
	 * 検索用テキストを取得する。
	 * @param text テキスト。
	 * @param fieldUsing フィールド利用。
     * @return 検索用テキスト。
     */
	public static String getSearchText(String text, SearchFieldUsing fieldUsing) {
		switch (fieldUsing) {
			case Optimize:
				return optimizeText(text);
			case Normalize:
				return normalizeTitle(text);
			case Raw:
				return text;
			default:
				return "";
		}
	}

	/**
	 * テキストの正規化。
	 * @param text テキスト。
	 * @return 正規化テキスト。
	 */
	public static String normalizeTitle(String text) {
		if (StringUtils.isEmpty(text))
			return "";

		// 正規化
		String output = Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase()
				.replaceAll(UNICODE_SPACES, " ")
				.replaceAll("゠", "=")
				.replaceAll("(“|”)", "\"")
				.replaceAll("(‘|’)", "\'")
				.trim();

		return output;
	}


	/**
	 * テキストの最適化。(括弧の除去)
	 * @param text テキスト。
	 * @return 最適化テキスト。
	 */
	public static String optimizeText(String text) {
		if (StringUtils.isEmpty(text))
			return "";

//		String output = normalizeTitle(text)
//////				// 括弧内の除去
//////				.replaceAll("\\(.*?\\)", "")
//////				.replaceAll("\\[.*?\\]", "")
//////				.replaceAll("\\{.*?\\}", "")
//////				.replaceAll("\\<.*?\\>", "")
//////				.replaceAll("\\（.*?\\）", "")
//////				.replaceAll("\\［.*?\\］", "")
//////				.replaceAll("\\｛.*?\\｝", "")
//////				.replaceAll("\\＜.*?\\＞", "")
//////				.replaceAll("\\【.*?\\】", "")
//////				.replaceAll("\\〔.*?\\〕", "")
//////				.replaceAll("\\〈.*?\\〉", "")
//////				.replaceAll("\\《.*?\\》", "")
//////				.replaceAll("\\「.*?\\」", "")
//////				.replaceAll("\\『.*?\\』", "")
//////				.replaceAll("\\〖.*?\\〗", "")
//////				.replaceAll("-(inst|without|off).*-", " ")
//////				.replaceAll("－(inst|without|off).*－", "")
//////				.replaceAll("(-|－|~|～|〜|〰).*", " ")
//////				;

		String output = removeParentheses(text);
		output = removeDash(output);
		output = removeTextInfo(output);

		return trim(output);
	}


	/**
	 * Remove parentheses.
	 * @param text text.
	 * @return removed text.
	 */
	public static String removeParentheses(String text) {
		if (text == null || text.isEmpty())
			return "";

		return text
				.replaceAll("(^[^\\(]+)\\(.*?\\)", "$1")
				.replaceAll("(^[^\\[]+)\\[.*?\\]", "$1")
				.replaceAll("(^[^\\{]+)\\{.*?\\}", "$1")
				.replaceAll("(^[^\\<]+)\\<.*?\\>", "$1")
				.replaceAll("(^[^\\（]+)\\（.*?\\）", "$1")
				.replaceAll("(^[^\\［]+)\\［.*?\\］", "$1")
				.replaceAll("(^[^\\｛]+)\\｛.*?\\｝", "$1")
				.replaceAll("(^[^\\＜]+)\\＜.*?\\＞", "$1")
				.replaceAll("(^[^\\【]+)\\【.*?\\】", "$1")
				.replaceAll("(^[^\\〔]+)\\〔.*?\\〕", "$1")
				.replaceAll("(^[^\\〈]+)\\〈.*?\\〉", "$1")
				.replaceAll("(^[^\\《]+)\\《.*?\\》", "$1")
				.replaceAll("(^[^\\「]+)\\「.*?\\」", "$1")
				.replaceAll("(^[^\\『]+)\\『.*?\\』", "$1")
				.replaceAll("(^[^\\〖]+)\\〖.*?\\〗", "$1")
				;
	}

	/**
	 * Remove text after dash characters.
	 * @param text text.
	 * @return removed text.
	 */
	public static String removeDash(String text) {
		if (text == null || text.isEmpty())
			return "";

		return text
				.replaceAll("\\s+(-|－|―|ー|ｰ|~|～|〜|〰|=|＝).*", "");
	}

	/**
	 * Remove attached info.
	 * @param text text.
	 * @return removed text.
	 */
	public static String removeTextInfo(String text) {
		if (text == null || text.isEmpty())
			return "";

		return text
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?off vocal.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?no vocal.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?less vocal.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?without.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?w/o.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?backtrack.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?backing track.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?karaoke.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?カラオケ.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?からおけ.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?歌無.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?vocal only.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?instrumental.*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?inst\\..*", "")
				.replaceAll("(?i)[\\(\\<\\[\\{\\s]?インスト.*", "")
				;
	}


	/**
	 * 全角を含めてトリミング。
	 * @param text 元テキスト。
	 * @return トリミングテキスト。
	 */
	public static String trim(String text) {
		char[] value = text.toCharArray();
		int len = value.length;
		int st = 0;
		char[] val = value;

		while ((st < len) && (val[st] <= ' ' || val[st] == '　')) {
			st++;
		}
		while ((st < len) && (val[len - 1] <= ' ' || val[len - 1] == '　')) {
			len--;
		}

		return ((st>0) || (len<value.length)) ? text.substring(st,len):text;
	}

	/**
	 * 空白を置換える
	 * @param text テキスト。
	 * @param insertSpace スペースに置換える場合はtrue。
	 * @return 変換後テキスト。
	 */
	public static String removeWhitespace(String text, boolean insertSpace) {
		if (StringUtils.isEmpty(text))
			return "";

		return text.replaceAll(UNICODE_SPACES, insertSpace ? " " : "");
	}

	/**
	 * 2つのテキストを比較して、ほぼ同じ場合はtrue。
	 * @param text1 比較テキスト1。
	 * @param text2 比較テキスト2。
	 * @return
	 */
	public static boolean sameText(String text1, String text2) {
		if (StringUtils.isEmpty(text1) || StringUtils.isEmpty(text2))
			return false;

		String it = removeWhitespace(normalizeTitle(text1), false);
		String ot = removeWhitespace(normalizeTitle(text2), false);
		if (it.equals(ot)) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * ジャンルを一般化する。
	 * @param inputGenre 入力ジャンル。
	 * @return ジャンル。
	 */
	public static String generalizeGenre(String inputGenre) {
		if (StringUtils.isEmpty(inputGenre)) {
			return null;
		} else if (inputGenre.matches(".*(Anime|アニメ).*")) {
			return "Anime";
		} else if (inputGenre.matches(".*(Game|ゲーム).*")) {
			return "Game";
		} else if (inputGenre.matches(".*(Other|その他).*")) {
			return "Other";
		} else {
			return "JPop";
		}
	}

}
