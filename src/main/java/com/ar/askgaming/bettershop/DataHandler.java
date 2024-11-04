package com.ar.askgaming.bettershop;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class DataHandler {
    private File shopsFile;
    private FileConfiguration shopsConfig;

    public File getShopsFile() {
        return shopsFile;
    }

    public FileConfiguration getShopsConfig() {
        return shopsConfig;
    }

    public DataHandler(BetterShop main) {
        shopsFile = new File(main.getDataFolder(), "shops.yml");
        
        if (!shopsFile.exists()) {
            try {
                shopsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        shopsConfig = new YamlConfiguration();
        try {
            shopsConfig.load(shopsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveShop() {
        try {
            shopsConfig.save(shopsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
