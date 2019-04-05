package com.wa2c.java.externaltagger.value;

import com.wa2c.java.externaltagger.common.AppUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.ResourceBundle;

/**
 * Search field text.
 */
public enum SearchFieldUsing {
	/** Optimize. */
	Optimize(ResourceBundle.getBundle("resource").getString("label.Optimize")),
	/** Normalize. */
	Normalize(ResourceBundle.getBundle("resource").getString("label.Normalize")),
	/** Raw. */
	Raw(ResourceBundle.getBundle("resource").getString("label.Raw")),
	/** None. */
	None(ResourceBundle.getBundle("resource").getString("label.Unused")),
	/** None. */
	Edit(ResourceBundle.getBundle("resource").getString("label.Edit"));



	private String name;


	private SearchFieldUsing(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}


	/**
	 * 検索用テキストに変換する。
	 * @param text テキスト。
	 * @return 検索用テキスト。
	 */
	public String format(String text) {
		if (StringUtils.isEmpty(text))
			return "";

		switch (this) {
			case Optimize:
				return AppUtils.optimizeText(text);
			case Normalize:
				return AppUtils.normalizeTitle(text);
			case Raw:
				return text;
			case Edit:
				return text;
			default:
				return "";
		}
	}

}
