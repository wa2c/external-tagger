package com.wa2c.java.externaltagger.controller;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.Language;
import com.cybozu.labs.langdetect.LauguageDetectorFactoryUtil;
import com.pedrohlc.viewlyricsppensearcher.LyricInfo;
import com.pedrohlc.viewlyricsppensearcher.Result;
import com.pedrohlc.viewlyricsppensearcher.ViewLyricsSearcher;
import com.wa2c.java.externaltagger.common.Logger;
import org.apache.commons.lang3.math.NumberUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewLyricsController {

    public List<LyricInfo> search(String title, String artist) {
        try {
            Result result = ViewLyricsSearcher.search(artist, title, 0);

            URL url= getClass().getResource("img/sample.png");

            // sort
            List<LyricInfo> itemList = result.getLyricsInfo();
            if (itemList == null || itemList.isEmpty())
                return null;

            itemList.sort((o1, o2) -> {
                if (o1 == null && o2 == null)
                    return 0;
                if (o1 == null)
                    return 1;
                if (o2 == null)
                    return -1;

                Double o1Rating = o1.getLyricRate();
                Double o2Rating = o2.getLyricRate();
                if (!o1Rating.equals(o2Rating)) {
                    return -Double.compare(o1Rating, o2Rating);
                }

                Integer o1Download = o1.getLyricDownloadsCount();
                Integer o2Download = o2.getLyricDownloadsCount();
                return -Integer.compare(o1Download, o2Download);
            });

            return itemList;

//            // read
//            for (LyricInfo item : itemList) {
//                byte[] lyricsBytes = getLyrics(item.getLyricURL());
//                if (lyricsBytes == null || lyricsBytes.length == 0)
//                    continue;
//
//                // lyrics
//                String lyrics = new String(lyricsBytes, StandardCharsets.UTF_8);
//
//
//                Detector detector = LauguageDetectorFactoryUtil.createDetectorAll();
//                detector.append(lyrics);
//                ArrayList<Language> language = detector.getProbabilities();
//
//
//            }

        } catch (Exception e) {
            Logger.d(e);
        }
        return null;
    }

    public byte[] getLyrics(String downloadUrl) {
        byte[] lyricsBytes;
        try {
            URL url = new URL(downloadUrl);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            con.getResponseCode();

            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // failed
                return null;
            }

            try (InputStream inputStream = con.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                while (true) {
                    int len = inputStream.read(buffer);
                    if (len < 0) {
                        break;
                    }
                    outputStream.write(buffer, 0, len);
                }
                lyricsBytes = outputStream.toByteArray();
            }
        } catch (Exception e) {
            Logger.e(e);
            return null;
        }

        return lyricsBytes;
    }




}
