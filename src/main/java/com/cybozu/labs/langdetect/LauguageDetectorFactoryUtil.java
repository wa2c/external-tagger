package com.cybozu.labs.langdetect;

import com.cybozu.labs.langdetect.util.LangProfile;
import com.google.gson.Gson;
import com.wa2c.java.externaltagger.common.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LauguageDetectorFactoryUtil {

    private static final HashMap<String, String> languageProfileMap = new HashMap<String, String>() {{
        put("af",    "profiles/af"   );
        put("ar",    "profiles/ar"   );
        put("bg",    "profiles/bg"   );
        put("bn",    "profiles/bn"   );
        put("cs",    "profiles/cs"   );
        put("da",    "profiles/da"   );
        put("de",    "profiles/de"   );
        put("el",    "profiles/el"   );
        put("en",    "profiles/en"   );
        put("es",    "profiles/es"   );
        put("et",    "profiles/et"   );
        put("fa",    "profiles/fa"   );
        put("fi",    "profiles/fi"   );
        put("fr",    "profiles/fr"   );
        put("gu",    "profiles/gu"   );
        put("he",    "profiles/he"   );
        put("hi",    "profiles/hi"   );
        put("hr",    "profiles/hr"   );
        put("hu",    "profiles/hu"   );
        put("id",    "profiles/id"   );
        put("it",    "profiles/it"   );
        put("ja",    "profiles/ja"   );
        put("kn",    "profiles/kn"   );
        put("ko",    "profiles/ko"   );
        put("lt",    "profiles/lt"   );
        put("lv",    "profiles/lv"   );
        put("mk",    "profiles/mk"   );
        put("ml",    "profiles/ml"   );
        put("mr",    "profiles/mr"   );
        put("ne",    "profiles/ne"   );
        put("nl",    "profiles/nl"   );
        put("no",    "profiles/no"   );
        put("pa",    "profiles/pa"   );
        put("pl",    "profiles/pl"   );
        put("pt",    "profiles/pt"   );
        put("ro",    "profiles/ro"   );
        put("ru",    "profiles/ru"   );
        put("sk",    "profiles/sk"   );
        put("sl",    "profiles/sl"   );
        put("so",    "profiles/so"   );
        put("sq",    "profiles/sq"   );
        put("sv",    "profiles/sv"   );
        put("sw",    "profiles/sw"   );
        put("ta",    "profiles/ta"   );
        put("te",    "profiles/te"   );
        put("th",    "profiles/th"   );
        put("tl",    "profiles/tl"   );
        put("tr",    "profiles/tr"   );
        put("uk",    "profiles/uk"   );
        put("ur",    "profiles/ur"   );
        put("vi",    "profiles/vi"   );
        put("zh_cn", "profiles/zh_cn");
        put("zh_tw", "profiles/zh_tw");
    }};


    public static Detector createDetectorAll() throws LangDetectException {
        if (DetectorFactory.getLangList().size() == 0) {
            Gson gson = new Gson();
            Object object = new Object();
            int index = 0;
            for (Map.Entry<String, String> entry : languageProfileMap.entrySet()) {
                try {
                    URL url = object.getClass().getClassLoader().getResource(entry.getValue());
                    URLConnection connection = url.openConnection();
                    try (InputStream stream = connection.getInputStream();
                        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        LangProfile profile = gson.fromJson(reader, LangProfile.class);
                        DetectorFactory.addProfile(profile, index++, languageProfileMap.size());
                    }
                } catch (Exception e) {
                    Logger.e(e);
                }
            }
        }

        return DetectorFactory.create();
    }

    //    /**
//     * Get language detector.
//     * @return Language detector.
//     */
//    @Synchronized
//    @Throws(LangDetectException::class)
//    fun createDetectorAll(context: Context): Detector {
//        if (DetectorFactory.getLangList() == null || DetectorFactory.getLangList().size == 0) {
//            val coreCount = Runtime.getRuntime().availableProcessors()
//
//            val gson = Gson()
//            var index = 0
//            for (item in languageProfileMap) {
//                try {
//                    context.resources.openRawResource(item.value).use {
//                        val size = it.available()
//                        it.reader().use {
//                            val profile = gson.fromJson(it, LangProfile::class.java)
//                            if (profile != null)
//                                DetectorFactory.addProfile(profile, index++, languageProfileMap.size)
//                        }
//                    }
//                } catch (e: Exception) {
//                    Timber.e(e)
//                }
//            }
//
//        }
//
//        return DetectorFactory.create()
//    }

}
