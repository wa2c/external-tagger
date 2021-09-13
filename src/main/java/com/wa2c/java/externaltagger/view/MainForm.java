package com.wa2c.java.externaltagger.view;

import com.wa2c.java.externaltagger.Program;
import com.wa2c.java.externaltagger.common.AppUtils;
import com.wa2c.java.externaltagger.common.Logger;
import com.wa2c.java.externaltagger.controller.MediaFileController;
import com.wa2c.java.externaltagger.controller.source.*;
import com.wa2c.java.externaltagger.model.FieldDataMap;
import com.wa2c.java.externaltagger.model.Settings;
import com.wa2c.java.externaltagger.value.MediaField;
import com.wa2c.java.externaltagger.value.SearchFieldUsing;
import com.wa2c.java.externaltagger.view.component.SourceTable;
import com.wa2c.java.externaltagger.view.component.SourceTableModel;
import com.wa2c.java.externaltagger.view.dialog.LrcLyricsDialog;
import com.wa2c.java.externaltagger.view.dialog.SettingsDialog;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
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
    private JButton searchIndividualButton;
    private SourceTable sourceTable;
    private JButton fieldLrcDownloadButton;
    private JButton fieldDateUpdateButton;


    private final List<AbstractExternalSource> externalSource = new ArrayList<>();
    private final List<FieldDataMap> mediaList = new ArrayList<>();

    private final MediaFileController mediaFileController = new MediaFileController();


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

        // initialize
        initializeTablePane();
        initializeSourcePane();
        initializeHeaderPane();
        initializeBottomPane();

        // popup
        initializeItemPopupMenu();
        initializeHeaderPopupMenu();

//        updateSourceList();
        updateSourceTable();
        sourceTable.getColumnModel().getColumn(0).setMaxWidth(sourceTable.getFont().getSize() + 8);
        sourceTable.setRowHeight(sourceTable.getFont().getSize() + 8);

    }

    private void initializeTablePane() {
        // Media Table
        //Object[] header = Arrays.stream(MediaField.values()).map(x -> x != null ? x.getLabel() : null).toArray();
        Object[] header = Program.Pref.visibleColumnList.stream().map(x -> x != null ? x.getLabel() : null).toArray();
        DefaultTableModel tableModel = new DefaultTableModel(header, 0);
        mediaTable.setModel(tableModel);

        mediaTable.setTransferHandler(new DropFileHandler());
        mediaScrollPane.setTransferHandler(new DropFileHandler());
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            adjustColumnSizes(mediaTable, i, 4);
        }
        mediaTable.clearSelection();

        mediaTable.getSelectionModel().addListSelectionListener(e -> updateSearchField());
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
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DELETE:
                        removeSelectedMedia();
                        break;
                    case KeyEvent.VK_L:
                        downloadLrc();
                        break;
                }
            }
        });

        mediaScrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mediaTable.clearSelection();
            }
        });
    }


    /**
     * Initialize source pane.
     */
    private void initializeSourcePane() {
        AbstractExternalSource source;
        source = new SourceAmazonJp();
        externalSource.add(source);
        source = new SourceJLyrics();
        externalSource.add(source);
//        source = new SourceKashiTime();
//        externalSource.add(source);
        source = new SourcePetitLyrics();
        externalSource.add(source);
        source = new SourceJoySound();
        externalSource.add(source);
        source = new SourceKashiGet();
        externalSource.add(source);
        source = new SourceLyricalNonsense();
        externalSource.add(source);

        for (AbstractExternalSource s : externalSource) {
            Boolean val;
            if ((val = Program.Pref.sourceEnabledMap.get(s.getClass().getName())) != null) {
                s.setEnabled(val);
            } else {
                s.setEnabled(false);
            }
        }
    }


    /**
     * Initialize bottom pane.
     */
    private void initializeBottomPane() {
        searchIndividualButton.addActionListener(e -> getIndividualInfo());

        mediaAddButton.addActionListener(e -> {
            JFileChooser fileDialog = new JFileChooser();
            fileDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileDialog.setMultiSelectionEnabled(true);
            if (fileDialog.showOpenDialog(MainForm.this) == JFileChooser.APPROVE_OPTION) {
                readMediaFile(fileDialog.getSelectedFiles());
            }
        });
        mediaClearButton.addActionListener(e -> {
            mediaList.clear();
            updateMediaTable();
        });
        fieldGetButton.addActionListener(e -> {
//                int[] rows = mediaTable.getSelectedRows();
//                DownloadConfirmationDialog dialog = new DownloadConfirmationDialog();
//                dialog.pack();
//                dialog.setVisible(true);
//                if (dialog.getResult() != JOptionPane.OK_OPTION)
//                    return;
            // 情報取得
            //mediaTable.changeSelection();
            getSelectedMediaInfo();
        });
        fieldWriteButton.addActionListener(e -> writeSelectedMediaInfo());
        fieldResetButton.addActionListener(e -> resetSelectedMediaInfo());
        fieldDeleteButton.addActionListener(e -> removeSelectedMedia());
        fieldLrcDownloadButton.addActionListener(e -> downloadLrc());
        fieldDateUpdateButton.addActionListener(e -> updateDate());
        launchSettingsButton.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog();
            dialog.pack();
            dialog.setVisible(true);
        });
    }

    /**
     * Initialize header pane.
     */
    private void initializeHeaderPane() {
        // Search Condition Field
        for (SearchFieldUsing u : SearchFieldUsing.values()) {
            searchFieldTitleComboBox.addItem(u);
            searchFieldArtistComboBox.addItem(u);
            searchFieldAlbumComboBox.addItem(u);
        }
        searchFieldTitleComboBox.setSelectedIndex(0);
        searchFieldArtistComboBox.setSelectedIndex(0);
        searchFieldAlbumComboBox.setSelectedIndex(0);

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


    /**
     * Create item popup menu.
     */
    private void initializeItemPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Open Folder");
        menuItem.addActionListener(e -> {
            if (mediaTable.getSelectedRow() < 0)
                return;

            int row = mediaTable.getSelectedRow();
            String filePath = mediaList.get(row).getFirstData(MediaField.FILE_PATH);
            File f = new File(filePath);
            if (!f.exists() || !f.isFile()) {
                return;
            }

            AppUtils.openFolder(f);
        });
        popupMenu.add(menuItem);
        JMenuItem resetMenuItem = new JMenuItem("Reset");
        resetMenuItem.addActionListener(e -> removeSelectedMedia());
        popupMenu.add(resetMenuItem);
        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.addActionListener(e -> removeSelectedMedia());
        popupMenu.add(deleteMenuItem);

        // Set show event
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
    }

    /**
     * Create header popup menu.
     */
    private void initializeHeaderPopupMenu() {
        JPopupMenu headerPopupMenu = new JPopupMenu();
        JMenuItem headerResetItem = new JMenuItem("Reset");
        headerResetItem.addActionListener(e -> {
            Program.Pref.visibleColumnList.clear();
            Program.Pref.visibleColumnList.addAll(Arrays.asList(MediaField.values()));
        });

        // Set show event
        mediaTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() ==  MouseEvent.BUTTON3) {
                    headerPopupMenu.removeAll();
                    headerPopupMenu.add(headerResetItem);

                    List<MediaField> showItems = Program.Pref.visibleColumnList;
                    for (MediaField f : MediaField.values()) {
                        JCheckBoxMenuItem item = new JCheckBoxMenuItem(f.getLabel());
                        item.setSelected(showItems.contains(f));
                        item.addActionListener(event -> {
                            boolean isChecked = item.isSelected();
                            item.setSelected(isChecked);
                            if (isChecked) Program.Pref.visibleColumnList.add(f);
                            else Program.Pref.visibleColumnList.remove(f);
                        });
                        headerPopupMenu.add(item);
                    }
                    headerPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * テーブルのカラム幅を調整
     * @param table テーブル。
     * @param column カラム番号。
     * @param margin マージン。
     */
    private void adjustColumnSizes(JTable table, int column, int margin) {
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



    private void updateSourceTable() {
        SourceTableModel sourceTableModel = new SourceTableModel(externalSource);
        sourceTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col != 0)
                    return;

                sourceTableModel.removeTableModelListener(this);

                for (int i = 0; i < sourceTableModel.getRowCount(); i++) {
                    sourceTableModel.setValueAt(false, i, 0);
                    externalSource.get(i).setEnabled(false);
                    Program.Pref.sourceEnabledMap.put(externalSource.get(i).getClass().getName(), false);
                }
                sourceTableModel.setValueAt(true, row, 0);
                externalSource.get(row).setEnabled(true);
                Program.Pref.sourceEnabledMap.put(externalSource.get(row).getClass().getName(), true);

                sourceTableModel.addTableModelListener(this);
            }
        });
        sourceTable.getSelectionModel().addListSelectionListener(event -> {
            updateSourceParameter();
            sourceParamPanel.updateUI();
        });

        sourceTable.setModel(sourceTableModel);
        updateSourceParameter();
    }

    private void updateSourceParameter() {
        sourceParamPanel.removeAll();

        int index = sourceTable.getSelectedRow();
        if (index < 0 || index >= externalSource.size())
            return;

        GridBagLayout gridLayout = (GridBagLayout)sourceParamPanel.getLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        int i = 0;
        AbstractExternalSource source = externalSource.get(sourceTable.getSelectedRow());
        for (MediaField f : source.getResultField()) {
            Label lb = new Label(f.getLabel());
            gbc.gridy = i++;
            gbc.gridx = 0;
            gridLayout.setConstraints(lb, gbc);
            sourceParamPanel.add(lb);
        }
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
            fieldLrcDownloadButton.setEnabled(true);
            fieldDateUpdateButton.setEnabled(true);
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
            fieldDateUpdateButton.setEnabled(false);
            fieldLrcDownloadButton.setEnabled(false);
            searchFieldTitleTextField.setText(null);
            searchFieldArtistTextField.setText(null);
            searchFieldAlbumTextField.setText(null);
        }
    }


    /**
     * ファイル、フォルダを読込む。
     * @param files ファイルまたはフォルダの配列。
     */
    private void readMediaFile(File[] files) {
        List<FieldDataMap> map = mediaFileController.readFile(files, mediaList);
        mediaList.addAll(map);
        updateMediaTable();
    }


    // Button Action

    private void getIndividualInfo() {
        int row = mediaTable.getSelectedRow();

        FieldDataMap currentMap = mediaList.get(row);
        FieldDataMap conditionMap = new FieldDataMap(currentMap);
        conditionMap.putFirstData(MediaField.TITLE, searchFieldTitleTextField.getText());
        conditionMap.putFirstData(MediaField.ARTIST, searchFieldArtistTextField.getText());
        conditionMap.putFirstData(MediaField.ALBUM, searchFieldAlbumTextField.getText());

        FieldDataMap resultMap = getResult(conditionMap, new HashMap<>());
        updateMediaInfo(currentMap, resultMap);

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
            FieldDataMap currentMap = mediaList.get(row);
            FieldDataMap resultMap = getResult(currentMap, fieldUsing);
            updateMediaInfo(currentMap, resultMap);
        }

        updateMediaTable();
    }

    /**
     * Get field data map by condition
     * @param conditionMap Search condition.
     * @param fieldUsing Condition using.
     * @return Result map.
     */
    private FieldDataMap getResult(FieldDataMap conditionMap, HashMap<MediaField, SearchFieldUsing> fieldUsing) {
        for (AbstractExternalSource source : externalSource) {
            if (!source.getEnabled())
                continue;

            FieldDataMap resultMap = source.getFieldDataMap(conditionMap, fieldUsing);
            if (resultMap != null) return resultMap;
        }
        return new FieldDataMap();
    }

    /**
     * Update current filed data map.
     * @param currentMap Current map.
     * @param resultMap Result map.
     */
    private void updateMediaInfo(FieldDataMap currentMap, FieldDataMap resultMap) {

        if (searchCompareTitleCheckBox.isSelected()) {
            String currentTitle = currentMap.getFirstData(MediaField.TITLE);
            String resultTitle = resultMap.getFirstData(MediaField.TITLE);
            if (StringUtils.isEmpty(currentTitle) || StringUtils.isEmpty(resultTitle)) {
                JOptionPane.showMessageDialog(this, "タイトルの比較ができません\n\nLocal : " + currentTitle + "\nRemote:" + resultTitle);
                return;
            }

            // タイトルが不一致の場合は取得しない
            SearchFieldUsing u = (SearchFieldUsing) searchFieldTitleComboBox.getSelectedItem();
            if (!AppUtils.sameText(u.format(currentTitle), u.format(resultTitle))) {
                return;
            }
        }

        MediaField[] fields = MediaField.values();
        for (MediaField field : fields) {
            if (!resultMap.containsKey(field))
                continue;

            List<String> values = resultMap.get(field);

            if (field == MediaField.TITLE) {
                // タイトルは取得しない
                continue;
            } else if (field == MediaField.GENRE) {
                // ジャンルは一般化する
                if (CollectionUtils.isEmpty(values)) {
                    currentMap.putNewData(field, "JPop");
                } else {
                    currentMap.putNewData(field, values.stream().map(c -> AppUtils.generalizeGenre(c)).collect(Collectors.toList()));
                }
                continue;
            }
            if (values == null)
                continue;

            currentMap.put(field, resultMap.get(field));
        }
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
                        if (field == MediaField.FILE_PATH || field == MediaField.LRC_FILE) {
                            continue;
                        }

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
                mediaFileController.setLastModified(file.getParentFile(), fileDate);
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

    private void downloadLrc() {
        int row = mediaTable.getSelectedRow();
        FieldDataMap map = mediaList.get(row);

        LrcLyricsDialog dialog = new LrcLyricsDialog(map);
        dialog.pack();
        int width = 800;
        int height = 800;
        int x = this.getX() + (this.getWidth() - width) / 2;
        if (x < 0)
            x = 0;
        int y = this.getY() + (this.getHeight() - height) / 2;
        if (y < 0)
            y = 0;

        dialog.setBounds(x, y, width, height);
        dialog.setVisible(true);

    }

    /**
     * ディレクトリの日付をリリース日にして読み取り専用とする
     */
    private void updateDate() {
        int[] rows = mediaTable.getSelectedRows();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        HashSet<String> completedPathSet = new HashSet<>();

       for (int i = rows.length - 1; i >= 0; i--) {
           int row = rows[i];
           FieldDataMap map = mediaList.get(row);
           File file = new File(map.getFirstData(MediaField.FILE_PATH));
           File parent = file.getParentFile();
           if (completedPathSet.contains(parent.getAbsolutePath()))
               continue; // 処理済パスは実行しない
           completedPathSet.add(parent.getAbsolutePath()); // パス追加

            try {
                String mbid = map.getFirstData(MediaField.MUSICBRAINZ_RELEASEID);
                URL url = new URL("https://musicbrainz.org/ws/2/release/" + mbid + "?fmt=xml");
                Logger.d(url.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setAllowUserInteraction(false);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("GET");
                conn.connect();

                int httpStatusCode = conn.getResponseCode();
                if(httpStatusCode != HttpURLConnection.HTTP_OK){
                    Logger.d("STATUS CODE: " + httpStatusCode);
                    conn.disconnect();
                    throw new Exception();
                }
                Logger.d("RESPONSE MESSAGE: " + conn.getResponseMessage());

                // Input Stream
                try (InputStream inputStream = conn.getInputStream()) {
                    // Parse XML
                    Element resultRootElem = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream).getDocumentElement();
                    Node releaseNode = resultRootElem.getFirstChild();
                    NodeList list = releaseNode.getChildNodes();
                    for (int index = 0; index < list.getLength(); index++) {
                        Node targetNode = list.item(index);
                        if (!targetNode.getNodeName().equals("date")) continue;
                        Date date = dateFormat.parse(targetNode.getTextContent());
                        AppUtils.updateFolderDate(parent, date);
                        break;
                    }
                } finally {
                    conn.disconnect();
                }

            } catch (Exception e) {
                Logger.e(e);
                Logger.e("更新失敗: " + parent.getAbsolutePath());
            }
        }
        updateMediaTable();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
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
                readMediaFile(files.toArray(new File[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
    }



}
