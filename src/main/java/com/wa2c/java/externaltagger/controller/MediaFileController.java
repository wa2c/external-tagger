package com.wa2c.java.externaltagger.controller;

import com.wa2c.java.externaltagger.common.Logger;
import com.wa2c.java.externaltagger.model.FieldDataMap;
import com.wa2c.java.externaltagger.value.MediaField;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class MediaFileController {

    /**
     * Read some media files.
     * @param files Some media files.
     * @param duplicatedData Duplicate files.
     * @return Read data.
     */
    public List<FieldDataMap> readFile(File[] files, Collection<FieldDataMap> duplicatedData) {
        if (files == null)
            return new ArrayList<>(0);
        List<FieldDataMap> list = new ArrayList<>(files.length);
        for (File f : files) {
            try {
                FieldDataMap map = readFile(f, duplicatedData);
                if (map == null)
                    continue;
                list.add(map);
            } catch (Exception e) {
                Logger.d(e);
            }
        }
        return list;
    }

    /**
     * Read a media file.
     * @param file A media file
     * @param duplicatedData Duplicatd files.
     * @return Read data.
     */
    public FieldDataMap readFile(File file, Collection<FieldDataMap> duplicatedData) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        if (file == null)
            return null;

        // remove duplicated file
        String filePath = file.getCanonicalPath();
        if (duplicatedData.stream().anyMatch(r -> filePath.equals(r.getFirstData(MediaField.FILE_PATH)))) {
            return null;
        }

        AudioFile f = AudioFileIO.read(file);
        Tag tag = f.getTag();

        FieldDataMap map = new FieldDataMap();
        for (int i = 0; i < MediaField.values().length; i++) {
            MediaField field = MediaField.values()[i];
            if (field == MediaField.FILE_PATH) {
                map.put(field, file.getCanonicalPath());
                continue;
            }

            if (field == MediaField.LRC_FILE) {
                File lrcFile = getLrcFile(file);
                if (lrcFile != null && lrcFile.exists()) {
                    map.put(field, file.getCanonicalPath());
                    continue;
                }
            }

            try {
                FieldKey key = FieldKey.valueOf(field.name());
                map.put(field, tag.getAll(key));
            } catch (IllegalArgumentException iae) {
                Logger.d(iae);
            }
        }
        return map;
    }

    public File getLrcFile(File mediaFile) {
        try {
            String dirPath = mediaFile.getParentFile().getCanonicalPath();
            String fileName = mediaFile.getName();
            String lrcName = fileName.substring(0 ,fileName.lastIndexOf('.')) + ".lrc";
            return new File(dirPath, lrcName);
        } catch (IOException e) {
            Logger.d(e);
            return null;
        }
    }


    /**
     * Write file
     * @param lrcFilePath LRC file path.
     * @param lyricsText Lyrics text.
     */
    public void writeLrcFile(String lrcFilePath, String lyricsText) throws IOException {
        FileUtils.write(new File(lrcFilePath), lyricsText, StandardCharsets.UTF_8);
    }




    /**
     * Setting last modified time to files and directories.
     * @param item file or directory.
     * @param date last modified date.
     */
    public void setLastModified(File item, Date date) {
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


}
