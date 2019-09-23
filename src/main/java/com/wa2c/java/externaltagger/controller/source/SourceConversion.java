package com.wa2c.java.externaltagger.controller.source;

import com.wa2c.java.externaltagger.value.MediaField;

/**
 * Source HTML Element.
 */
class SourceConversion {
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
