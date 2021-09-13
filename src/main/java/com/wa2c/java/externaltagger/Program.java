package com.wa2c.java.externaltagger;

import com.wa2c.java.externaltagger.common.Logger;
import com.wa2c.java.externaltagger.model.Settings;
import com.wa2c.java.externaltagger.view.MainForm;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;
import org.jaudiotagger.tag.reference.ID3V2Version;

import javax.swing.*;
import java.awt.*;

/**
 * Created by wa2c on 2016/02/24.
 */
public class Program {

    public static Settings Pref;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        // タグ設定
        TagOptionSingleton.getInstance().setId3v1Save(false);
        TagOptionSingleton.getInstance().setID3V2Version(ID3V2Version.ID3_V23);

        // MP3の文字コード設定
        TagOptionSingleton.getInstance().setId3v23DefaultTextEncoding(TextEncoding.UTF_16);
        TagOptionSingleton.getInstance().setId3v24DefaultTextEncoding(TextEncoding.UTF_16);
        TagOptionSingleton.getInstance().setId3v24UnicodeTextEncoding(TextEncoding.UTF_16);

        // ジャンルをテキストとして書込む
        TagOptionSingleton.getInstance().setWriteMp3GenresAsText(false);
        TagOptionSingleton.getInstance().setWriteMp4GenresAsText(true);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    MainForm f = new MainForm();
                    f.setVisible(true);
                } catch (Exception e) {
                    Logger.e(e);
                }
            }
        });
    }
}
