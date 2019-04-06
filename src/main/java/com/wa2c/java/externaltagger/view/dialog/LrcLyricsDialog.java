package com.wa2c.java.externaltagger.view.dialog;

import com.pedrohlc.viewlyricsppensearcher.LyricInfo;
import com.wa2c.java.externaltagger.common.Logger;
import com.wa2c.java.externaltagger.controller.MediaFileController;
import com.wa2c.java.externaltagger.controller.ViewLyricsController;
import com.wa2c.java.externaltagger.model.FieldDataMap;
import com.wa2c.java.externaltagger.value.MediaField;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LrcLyricsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonSave;
    private JButton buttonClose;
    private JTextField titleTextField;
    private JTextField artistTextField;
    private JTextArea lyricsTextArea;
    private JButton buttonSearch;
    private JTable searchResultTable;
    private JScrollPane lyricsScrollPane;
    private JScrollPane searchResultScrollPane;

    private static String[] columnNames = {"Title", "Artist", "Album", "Length", "Uploader", "Rate", "RateCount", "Download"};

    private final ViewLyricsController viewLyricsController = new ViewLyricsController();
    private final MediaFileController mediaFileController = new MediaFileController();

    private final FieldDataMap fieldDataMap;
    private final ArrayList<LyricInfo> resultList = new ArrayList<>();
    private final Map<Integer, String> lyricsMap = new HashMap<>();

    public LrcLyricsDialog(FieldDataMap map) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonSave);

        fieldDataMap = map;
        titleTextField.setText(map.getFirstData(MediaField.TITLE));
        artistTextField.setText(map.getFirstData(MediaField.ARTIST));

        buttonSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // table
        searchResultTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            downloadLyrics();
        });

        // update list
        search();
    }

    private void onOK() {
        // add your code here
        try {
            File mediaFile = new File(fieldDataMap.getFirstData(MediaField.FILE_PATH));
            File lrcFile = mediaFileController.getLrcFile(mediaFile);
            mediaFileController.writeLrcFile(lrcFile.getCanonicalPath(), lyricsTextArea.getText());
        } catch (Exception e) {
            Logger.e(e);
        }

        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void search() {
        try {
            List<LyricInfo> infoList = viewLyricsController.search(titleTextField.getText(), artistTextField.getText());
            resultList.clear();
            resultList.addAll(infoList);
            updateResultList();
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private void updateResultList() {
        if (resultList.size() == 0)
            return;

        List<String[]> dataList = new ArrayList<>(resultList.size());
        for (int i = 0; i < resultList.size(); i++) {
            LyricInfo result = resultList.get(i);
            String[] data = new String[columnNames.length];
            data[0] = result.getMusicTitle();
            data[1] = result.getMusicArtist();
            data[2] = result.getMusicAlbum();
            data[3] = result.getMusicLenght();
            data[4] = result.getLyricUploader();
            data[5] = result.getLyricRate().toString();
            data[6] = result.getLyricRatesCount().toString();
            data[7] = result.getLyricDownloadsCount().toString();
            dataList.add(data);
        }

        DefaultTableModel tableModel = new DefaultTableModel(dataList.toArray(new String[0][0]), columnNames);
        searchResultTable.setModel(tableModel);

        // scroll
        SwingUtilities.invokeLater(() -> {
            searchResultScrollPane.getVerticalScrollBar().setValue(0);
            searchResultScrollPane.getHorizontalScrollBar().setValue(0);
            searchResultTable.setRowSelectionInterval(0, 0);
            searchResultTable.requestFocus();
        });
    }

    private void downloadLyrics() {
        int selectedRow = searchResultTable.getSelectedRow();

        LyricInfo info = resultList.get(selectedRow);
        String lyrics = lyricsMap.get(selectedRow);
        if (lyrics == null) {
            byte[] data = viewLyricsController.getLyrics(info.getLyricURL());
            if (data == null)
                lyrics = "";
            else {
                if (data.length >= 3 && data[0] == (byte)0xEF && data[1] == (byte)0xBB && data[2] == (byte)0xBF) {
                    data = Arrays.copyOfRange(data, 3, data.length); // Remove BOM
                }
                lyrics = new String(data, StandardCharsets.UTF_8);
            }
            lyricsMap.put(selectedRow, lyrics);
        }
        lyricsTextArea.setText(lyrics);

        // scroll
        SwingUtilities.invokeLater(() -> {
            lyricsScrollPane.getVerticalScrollBar().setValue(0);
            lyricsScrollPane.getHorizontalScrollBar().setValue(0);
        });
    }

}
