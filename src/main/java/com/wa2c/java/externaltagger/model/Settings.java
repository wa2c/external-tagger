package com.wa2c.java.externaltagger.model;

import com.google.gson.Gson;
import com.wa2c.java.externaltagger.common.Logger;

import java.io.*;
import java.util.HashMap;

public class Settings {

    public int windowX;
    public int windowY;
    public int windowW;
    public int windowH;

    public int b = 100;

    public HashMap<String, Boolean> sourceEnabledMap = new HashMap<>();




    private static File SETTINGS_FILE = new File(System.getProperty("user.home"), "ExternalTagger.pref");

    public static Settings readSettings() {
        try (BufferedReader br = new BufferedReader(new FileReader(SETTINGS_FILE))) {
            Gson gson = new Gson();
            return gson.fromJson(br, Settings.class);
        } catch(IOException e){
            Logger.e(e);
            return new Settings();
        }
    }


    public static void writeSettings(Settings settings) {
        Gson gson = new Gson();
        String json = gson.toJson(settings);

        try (FileWriter fw = new FileWriter(SETTINGS_FILE, false)) {
            fw.write(json);
        } catch (IOException e) {
            Logger.e(e);
        }
    }

}
