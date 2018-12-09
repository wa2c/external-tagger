package com.wa2c.java.externaltagger;

import com.wa2c.java.externaltagger.common.*;
import com.wa2c.java.externaltagger.source.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * External Tagger
 * Created by wa2c on 2016/02/23.
 */
public class MainForm extends JFrame {
    private JPanel mainContentPanel;
    private JButton fieldGetButton;
    private JButton fieldDeleteButton;
    private JButton mediaAddButton;
    private JButton mediaClearButton;
    private JButton fieldWriteButton;
    private JButton fieldResetButton;
    private JList sourceList;
    private JCheckBox sourceEnabledCheckBox;
    private JCheckBox searchCompareTitleCheckBox;
    private JComboBox searchFieldTitleComboBox;
    private JComboBox searchFieldArtistComboBox;
    private JComboBox searchFieldAlbumComboBox;
    private JLabel searchFieldTitleLabel;
    private JLabel searchFieldArtistLabel;
    private JLabel searchFieldAlbumLabel;
    private JTable mediaTable;
    private JScrollPane mediaScrollPane;
    private JLabel sourceLabel;
    private JPanel sourceParamPanel;
    private JTextField searchFieldTitleTextField;
    private JTextField searchFieldArtistTextField;
    private JTextField searchFieldAlbumTextField;
    private JButton launchSettingsButton;
    private JButton button1;
    private JButton searchIndividualButton;
    private JList list1;


    private final List<AbstractExternalSource> externalSource = new ArrayList<>();
    private final List<FieldDataMap> mediaList = new ArrayList<>();


    public MainForm() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(ResourceBundle.getBundle("resource").getString("title.App"));
        setContentPane(mainContentPanel);
        pack();
        setVisible(true);

        // settings
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Program.Pref.b = 1000;
                Settings.writeSettings(Program.Pref);
            }
        });
        Program.Pref = Settings.readSettings();

        initializeData();



        // popup

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Open Folder");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mediaTable.getSelectedRow() < 0)
                    return;

                int row = mediaTable.getSelectedRow();
                String filePath = mediaList.get(row).getFirstData(MediaField.FILE_PATH);
                File f = new File(filePath);
                if (!f.exists() || !f.isFile()) {
                    return;
                }

                File dir = f.getParentFile();
                try {
                    String cmd = "";
                    String osName = System.getProperty("os.name").toLowerCase();
                    if(osName.contains("windows")){
                        cmd = "explorer";
                    } else if(osName.contains("mac")){
                        cmd = "open";
                    } else if(osName.contains("linux")){
                        cmd = "xdg-open";
                    } else {
                        cmd = "open";
                    }

                    ProcessBuilder pb = new ProcessBuilder(cmd, dir.getCanonicalPath());
                    Process process = pb.start();
                } catch (IOException ex) {
                    Logger.e(ex);
                }
            }
        });
        popupMenu.add(menuItem);
        JMenuItem resetMenuItem = new JMenuItem("Reset");
        resetMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedMedia();
            }
        });
        popupMenu.add(resetMenuItem);
        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedMedia();
            }
        });
        popupMenu.add(deleteMenuItem);

        // TODO test
        JMenuItem testMenuItem = new JMenuItem("test");
        testMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ;
            }
        });
        popupMenu.add(testMenuItem);

        // button

        searchIndividualButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getIndividualInfo();
            }
        });

        mediaAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileDialog = new JFileChooser();
                fileDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fileDialog.setMultiSelectionEnabled(true);
                if (fileDialog.showOpenDialog(MainForm.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileDialog.getSelectedFile();
                    readFile(fileDialog.getSelectedFiles());
                    updateMediaTable();
                }
            }
        });
        mediaClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaList.clear();
                updateMediaTable();
            }
        });
        fieldGetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                int[] rows = mediaTable.getSelectedRows();
//                DownloadConfirmationDialog dialog = new DownloadConfirmationDialog();
//                dialog.pack();
//                dialog.setVisible(true);
//                if (dialog.getResult() != JOptionPane.OK_OPTION)
//                    return;
                // 情報取得
                //mediaTable.changeSelection();
                getSelectedMediaInfo();
            }
        });
        fieldWriteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writeSelectedMediaInfo();
            }
        });
        fieldResetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetSelectedMediaInfo();
            }
        });
        fieldDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedMedia();
            }
        });
        launchSettingsButton.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog();
            dialog.pack();
            dialog.setVisible(true);
        });

        // listener

        mediaTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateSearchField();
            }
        });

        mediaTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
            }
            @Override
            public void keyReleased(KeyEvent e) {

            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    removeSelectedMedia();
                }
            }
        });
        mediaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        mediaScrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mediaTable.clearSelection();
            }
        });

        sourceList.addListSelectionListener(e -> updateSourcePaneTable());

        // 検索フィールド
        searchFieldTitleComboBox.addItemListener(e -> {
            searchFieldTitleTextField.setEnabled((searchFieldTitleComboBox.getSelectedItem() == SearchFieldUsing.Edit));
            if (searchFieldTitleComboBox.getSelectedItem() != SearchFieldUsing.Edit)
                updateSearchField();
        });
        searchFieldArtistComboBox.addItemListener(e -> {
            searchFieldArtistTextField.setEnabled((searchFieldArtistComboBox.getSelectedItem() == SearchFieldUsing.Edit));
            if (searchFieldArtistComboBox.getSelectedItem() != SearchFieldUsing.Edit)
                updateSearchField();
        });
        searchFieldAlbumComboBox.addItemListener(e -> {
            searchFieldAlbumTextField.setEnabled((searchFieldAlbumComboBox.getSelectedItem() == SearchFieldUsing.Edit));
            if (searchFieldAlbumComboBox.getSelectedItem() != SearchFieldUsing.Edit)
                updateSearchField();
        });
    }

    private ChangeListener sourceEnabledActionListener =  new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (!sourceList.isSelectionEmpty()) {
                JCheckBox c = (JCheckBox)e.getSource();
                externalSource.get(sourceList.getSelectedIndex()).setEnabled(c.isSelected());
                Program.Pref.sourceEnabledMap.put( externalSource.get(sourceList.getSelectedIndex()).getClass().getName(), c.isSelected() );
            }
        }
    };



    /**
     * Initialize data.
     */
    private void initializeData() {
        // Media Table
        Object[] header = Arrays.asList(MediaField.values()).stream().map(x -> x != null ? x.getLabel() : null).toArray();
        DefaultTableModel tableModel = new DefaultTableModel(header, 0);
        mediaTable.setModel(tableModel);

        mediaTable.setTransferHandler(new DropFileHandler());
        mediaScrollPane.setTransferHandler(new DropFileHandler());
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            adjustColumnSizes(mediaTable, i, 4);
        }
        mediaTable.clearSelection();




        // Search Condition Field
        for (SearchFieldUsing u : SearchFieldUsing.values()) {
            searchFieldTitleComboBox.addItem(u);
            searchFieldArtistComboBox.addItem(u);
            searchFieldAlbumComboBox.addItem(u);
        }
        searchFieldTitleComboBox.setSelectedIndex(0);
        searchFieldArtistComboBox.setSelectedIndex(0);
        searchFieldAlbumComboBox.setSelectedIndex(0);

        // Source
        AbstractExternalSource source;
        source = new SourceAmazonJp();
        externalSource.add(source);
        source = new SourceJLyrics();
        externalSource.add(source);
        source = new SourceKashiTime();
        externalSource.add(source);
        source = new SourceJoySound();
        externalSource.add(source);
        source = new SourceKashiGet();
        externalSource.add(source);

        for (AbstractExternalSource s : externalSource) {
            Boolean val;
            if ((val = Program.Pref.sourceEnabledMap.get(s.getClass().getName())) != null) {
                s.setEnabled(val);
            } else {
                s.setEnabled(false);
            }
        }

        updateSourceList();
    }

    /**
     * テーブルのカラム幅を調整
     * @param table テーブル。
     * @param column カラム番号。
     * @param margin マージン。
     */
    public void adjustColumnSizes(JTable table, int column, int margin) {
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
        TableColumn col = colModel.getColumn(column);
        int width;

        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
        width = comp.getPreferredSize().width;

        for (int r = 0; r < table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, column);
            comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, column), false, false, r, column);
            int currentWidth = comp.getPreferredSize().width;
            width = Math.max(width, currentWidth);
        }

        width += 2 * margin;

        col.setPreferredWidth(width);
        col.setWidth(width);
    }



    /**
     * Update sources list.
     */
    private void updateSourceList() {
        sourceList.removeAll();
        sourceList.setListData(externalSource.toArray(new AbstractExternalSource[externalSource.size()]));
        updateSourcePaneTable();
    }


    /**
     * Update source properties list.
     */
    private void updateSourcePaneTable() {
        // 状態変更
        sourceEnabledCheckBox.removeChangeListener(sourceEnabledActionListener);
        if (sourceList.isSelectionEmpty()) {
            sourceEnabledCheckBox.setSelected(false);
            sourceEnabledCheckBox.setEnabled(false);
        } else {
            sourceEnabledCheckBox.setEnabled(true);
            sourceEnabledCheckBox.setSelected(externalSource.get(sourceList.getSelectedIndex()).getEnabled());
        }
        sourceEnabledCheckBox.addChangeListener(sourceEnabledActionListener);

        sourceParamPanel.removeAll();
        int selectedIndex = sourceList.getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }


        GridBagLayout gridLayout = (GridBagLayout)sourceParamPanel.getLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        int i = 0;
        for (MediaField f : externalSource.get(selectedIndex).getResultField()) {
            Label lb = new Label(f.getLabel());
            gbc.gridy = i++;
            gbc.gridx = 0;
            gridLayout.setConstraints(lb, gbc);
            sourceParamPanel.add(lb);
        }


//        externalSource.get(selectedIndex).getResultField().stream().forEach(c -> {
//            JCheckBox cb = new JCheckBox(c.getLabel());
//            Label lb = new Label(c.getLabel());
//            gbc.gridx = 0;
//            gbc.gridy = i++;
//            gridLayout.setConstraints();
//            sourceParamPanel.add(lb);
//            sourceParamPanel.getLayout().
//            //sourceParamPanel.add(lb, LayoutStyle);
//        });
        sourceParamPanel.updateUI();

//        externalSource.get(selectedIndex).getTargetSource().stream().map(c -> c.getLabel()).collect(Collectors.joining("<br />")) + "</pre><html>"  });
//
//
//        tableModel.addRow(new String[]{
//                "フィールド",
//                "<html><pre>" +  externalSource.get(selectedIndex).getTargetSource().stream().map(c -> c.getLabel()).collect(Collectors.joining("<br />")) + "</pre><html>"  });
//        sourcePaneTable.setRowHeight(0, 200);
//
//        DefaultTableModel tableModel = (DefaultTableModel) sourcePaneTable.getModel();
//        for (int i = 0; i < tableModel.getRowCount(); i++) {
//            tableModel.removeRow(0);
//        }
//
//        int selectedIndex = sourceList.getSelectedIndex();
//        if (selectedIndex < 0) return;
//
//        tableModel.addRow(new String[]{
//                "フィールド",
//                "<html><pre>" +  externalSource.get(selectedIndex).getTargetSource().stream().map(c -> c.getLabel()).collect(Collectors.joining("<br />")) + "</pre><html>"  });
//        sourcePaneTable.setRowHeight(0, 200);
    }

    /**
     * Update media table.
     */
    private void updateMediaTable() {
        DefaultTableModel mediaModel = (DefaultTableModel) mediaTable.getModel();
        while (mediaModel.getRowCount() > 0) {
            mediaModel.removeRow(0);
        }

        MediaField[] fields = MediaField.values();
        for (FieldDataMap map : mediaList) {
            String[] rowData = new String[FieldKey.values().length];
            for (int i = 0; i < fields.length; i++) {
                List<String> text = map.get(fields[i]);
                if (!CollectionUtils.isEmpty(text)) {
                    rowData[i] = text.get(0);
                }
            }
            mediaModel.addRow(rowData);
        }
    }

    /**
     * 検索フィールドの更新。
     */
    private void updateSearchField() {
        int rowIndex = mediaTable.getSelectedRow();
        if (rowIndex >= 0 && rowIndex < mediaList.size()) {
            fieldGetButton.setEnabled(true);
            fieldWriteButton.setEnabled(true);
            fieldResetButton.setEnabled(true);
            fieldDeleteButton.setEnabled(true);
            FieldDataMap map = mediaList.get(rowIndex);


            // 検索テキスト
            searchFieldTitleTextField.setText(AppUtils.getSearchText(map.getFirstData(MediaField.TITLE), (SearchFieldUsing)searchFieldTitleComboBox.getSelectedItem()));
            searchFieldArtistTextField.setText(AppUtils.getSearchText(map.getFirstData(MediaField.ARTIST), (SearchFieldUsing)searchFieldArtistComboBox.getSelectedItem()));
            searchFieldAlbumTextField.setText(AppUtils.getSearchText(map.getFirstData(MediaField.ALBUM), (SearchFieldUsing)searchFieldAlbumComboBox.getSelectedItem()));
        } else {
            fieldGetButton.setEnabled(false);
            fieldWriteButton.setEnabled(false);
            fieldResetButton.setEnabled(false);
            fieldDeleteButton.setEnabled(false);
            searchFieldTitleTextField.setText(null);
            searchFieldArtistTextField.setText(null);
            searchFieldAlbumTextField.setText(null);
        }
    }


    /**
     * ファイル、フォルダを読込む。
     * @param files ファイルまたはフォルダの配列。
     */
    private void readFile(File[] files) {
        for (File f : files) {
            readFile(f);
        }
    }

    /**
     * ファイル、フォルダを読込む。
     * @param file ファイルまたはフォルダ。
     */
    private void readFile(File file) {
        if (file.isDirectory()) {
            readFile(file.listFiles());
        } else {
            try {
                String filePath = file.getCanonicalPath();

                // 同じファイルがある場合は登録しない
                if (mediaList.stream().anyMatch(r -> filePath.equals(r.getFirstData(MediaField.FILE_PATH)))) {
                    return;
                }

                AudioFile f = AudioFileIO.read(file);
                Tag tag = f.getTag();

                FieldDataMap map = new FieldDataMap();
                for (int i = 0; i < MediaField.values().length; i++) {
                    MediaField field = MediaField.values()[i];
                    if (field == MediaField.FILE_PATH) {
                        map.put(field, file.getCanonicalPath());
                        continue;
//						} else if (field == MediaField.COVER_ART) {
//							continue;
                    }

                    try {
                        FieldKey key = FieldKey.valueOf(field.name());
                        map.put(field, tag.getAll(key));
                    } catch (IllegalArgumentException iae) {
                        // TODO LOG
                        System.out.println(iae);
                    }
                }

                mediaList.add(map);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }



    // Button Action

    private void getIndividualInfo() {
        HashMap<MediaField, SearchFieldUsing> fieldUsing = new HashMap<>();
        String title = searchFieldTitleTextField.getText();
        String artist = searchFieldArtistTextField.getText();
        String album = searchFieldAlbumTextField.getText();

        int row = mediaTable.getSelectedRow();
        for (AbstractExternalSource source : externalSource) {
            if (!source.getEnabled())
                continue;

            FieldDataMap inputMap = mediaList.get(row);
            FieldDataMap m = new FieldDataMap(inputMap);
            m.putFirstData(MediaField.TITLE, title);
            m.putFirstData(MediaField.ARTIST, artist);
            m.putFirstData(MediaField.ALBUM, album);

            FieldDataMap outputMap = source.getFieldDataMap(m, fieldUsing);
            if (outputMap == null)
                continue;

            if (searchCompareTitleCheckBox.isSelected() && inputMap.containsKey(MediaField.TITLE) && outputMap.containsKey(MediaField.TITLE)) {
                // タイトルが不一致の場合は取得しない
                SearchFieldUsing u = fieldUsing.get(MediaField.TITLE);
                if (u != null) {
                    String inputTitle = AppUtils.removeWhitespace(u.format(inputMap.getFirstData(MediaField.TITLE)), false);
                    String outputTitle = AppUtils.removeWhitespace(u.format(outputMap.getFirstData(MediaField.TITLE)), false);
                    if (!(inputTitle).equals(outputTitle)) {
                        continue;
                    }
                }
            }

            MediaField[] fields = MediaField.values();
            for (int i = 0; i < fields.length; i++) {
                MediaField field = fields[i];
                if (!outputMap.containsKey(field))
                    continue;

                List<String> values = outputMap.get(field);

                if (field == MediaField.TITLE) {
                    // タイトルは取得しない
                    continue;
                } else if (field == MediaField.GENRE) {
                    // ジャンルは一般化する
                    if (CollectionUtils.isEmpty(values)) {
                        inputMap.putNewData(field, "JPop");
                    } else {
                        inputMap.putNewData(field, values.stream().map(c -> AppUtils.generalizeGenre(c)).collect(Collectors.toList()));
                    }
                    continue;
                }
                if (values == null)
                    continue;

                // 不足分のみ取得
                //if (CollectionUtils.isEmpty(inputMap.get(field))) {
                inputMap.put(field, outputMap.get(field));
                //}

            }
        }

        updateMediaTable();
    }

    /**
     * Get selected media info.
     */
    private void getSelectedMediaInfo() {
        HashMap<MediaField, SearchFieldUsing> fieldUsing = new HashMap<>();
        fieldUsing.put(MediaField.TITLE, (SearchFieldUsing)searchFieldTitleComboBox.getSelectedItem());
        fieldUsing.put(MediaField.ARTIST, (SearchFieldUsing)searchFieldArtistComboBox.getSelectedItem());
        fieldUsing.put(MediaField.ALBUM, (SearchFieldUsing)searchFieldAlbumComboBox.getSelectedItem());

        for (int row : mediaTable.getSelectedRows()) {
            // データ取得
            for (AbstractExternalSource source : externalSource) {
                if (!source.getEnabled())
                    continue;

                FieldDataMap inputMap = mediaList.get(row);
                FieldDataMap outputMap =  source.getFieldDataMap(mediaList.get(row), fieldUsing);

                if (outputMap == null)
                    continue;

                if (searchCompareTitleCheckBox.isSelected() && inputMap.containsKey(MediaField.TITLE) && outputMap.containsKey(MediaField.TITLE)) {
                    // タイトルが不一致の場合は取得しない
                    SearchFieldUsing u = fieldUsing.get(MediaField.TITLE);
                    String inputTitle = AppUtils.removeWhitespace(u.format(inputMap.getFirstData(MediaField.TITLE)), false);
                    String outputTitle = AppUtils.removeWhitespace(u.format(outputMap.getFirstData(MediaField.TITLE)), false);
                    if ( !( inputTitle ).equals( outputTitle ) ) {
                        continue;
                    }
               }

                MediaField[] fields = MediaField.values();
                for (int i = 0; i < fields.length; i++) {
                    MediaField field = fields[i];
                    if (!outputMap.containsKey(field))
                        continue;

                    List<String> values = outputMap.get(field);

                    if (field == MediaField.TITLE) {
                        // タイトルは取得しない
                        continue;
                    } else if (field == MediaField.GENRE) {
                        // ジャンルは一般化する
                        if (CollectionUtils.isEmpty(values)) {
                            inputMap.putNewData(field, "JPop");
                        } else {
                            inputMap.putNewData(field, values.stream().map(c -> AppUtils.generalizeGenre(c)).collect(Collectors.toList()));
                        }
                        continue;
                    }
                    if (values == null)
                        continue;

                    // 不足分のみ取得
                    //if (CollectionUtils.isEmpty(inputMap.get(field))) {
                    inputMap.put(field, outputMap.get(field));
                    //}

                }
            }
        }

        updateMediaTable();
    }

    private void writeSelectedMediaInfo() {
        for (int row : mediaTable.getSelectedRows()) {
            FieldDataMap map = mediaList.get(row);

            String filePath = map.getFirstData(MediaField.FILE_PATH);
            if (StringUtils.isEmpty(filePath)) {
                continue;
            }

            Date fileDate = null;
            File file = new File(filePath);
            AudioFile audioFile;
            try {
                audioFile = AudioFileIO.read(file);
                Tag tag = audioFile.getTag();

                for (FieldKey key : FieldKey.values()) {
                    try {
                        MediaField field = MediaField.valueOf(key.name());

                        List<String> vals = map.get(field);
                        if (CollectionUtils.isEmpty(vals))
                            continue;

                        tag.deleteField(key);

                        if (field == MediaField.YEAR) {
                            try {
                                if (!CollectionUtils.isEmpty(vals)) {
                                    fileDate = DateUtils.parseDate(vals.get(0),
                                            "yyyy-MM-dd",
                                            "yyyy/MM/dd",
                                            "yyyy.MM.dd",
                                            "yyyy-MM-dd HH:mm:ss",
                                            "yyyy-MM-dd HH:mm:ss",
                                            "yyyy-MM-dd HH:mm:ss");
                                }
                            } catch (IllegalArgumentException | ParseException ex ) {
                                Logger.e(ex);
                            }

                            if (tag instanceof AbstractID3Tag && !(tag instanceof ID3v24Tag)) {
                                // ID3 v2.4を除くID3タグは年のみ
                                if (fileDate != null) {
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(fileDate);
                                    tag.addField(key, String.valueOf(cal.get(Calendar.YEAR)));
                                    continue;
                                }
                            }

                        }

                        for (String val : vals) {
                            try {
                                tag.addField(key, val);
                            } catch (FieldDataInvalidException fdiex) {
                                Logger.e(fdiex);
                            }
                        }

                    } catch (IllegalArgumentException iaex) {
                        //Logger.e(iaex);
                    }
                }


                audioFile.setTag(tag);
                AudioFileIO.write(audioFile);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            if (fileDate != null) {
                setLastModified(file.getParentFile(), fileDate);
            }
        }
    }


    /**
     * Reset selected media info.
     */
    private void resetSelectedMediaInfo() {
        MediaField[] fields = MediaField.values();
        DefaultTableModel mediaTableModel = (DefaultTableModel) mediaTable.getModel();

        int[] rows = mediaTable.getSelectedRows();
        for (int i = rows.length - 1; i >= 0; i--) {
            int r = rows[i];
            String[] rowData = new String[FieldKey.values().length];
            for (int c = 0; c < fields.length; c++) {
                mediaTableModel.setValueAt(mediaList.get(r).getFirstData(fields[c]), r, c);
            }
            mediaTableModel.fireTableRowsUpdated(r, r);
        }
    }

    /**
     * Remove selected media.
     */
    private void removeSelectedMedia() {
        int[] rows = mediaTable.getSelectedRows();
        for (int i = rows.length - 1; i >= 0; i--) {
            mediaList.remove(rows[i]);
        }
        updateMediaTable();
    }


    /**
     * Setting last modified time to files and directories.
     * @param item file or directory.
     * @param date last modified date.
     */
    private void setLastModified(File item, Date date) {
        if (item.isDirectory()) {
            item.setLastModified(date.getTime());

            File[] children = item.listFiles();
            for (File child : children) {
                setLastModified(child, date);
            }
        } else {
            item.setLastModified(date.getTime());
        }
    }



    /**
     * Drag & drop handler.
     */
    private class DropFileHandler extends TransferHandler  {

        private static final long serialVersionUID = 1L;

        /**
         * ドロップされたものを受け取るか判断 (ファイルのときだけ受け取る)
         */
        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) {
                // ドロップ操作でない場合は受け取らない
                return false;
            }

            if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                // ドロップされたのがファイルでない場合は受け取らない
                return false;
            }

            return true;
        }



        /**
         * ドロップされたファイルを受け取る
         */
        @Override
        public boolean importData(TransferSupport support) {
            // 受け取っていいものか確認する
            if (!canImport(support)) {
                return false;
            }

            // ドロップ処理
            Transferable t = support.getTransferable();
            try {
                // ファイルを受け取る
                @SuppressWarnings("unchecked")
                List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

                // テキストエリアに表示するファイル名リストを作成する
                readFile(files.toArray(new File[files.size()]));
                updateMediaTable();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

    }



}
