package com.ar.askgaming.bettershop.Utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.ar.askgaming.bettershop.BlockShop;

public class ShopLogger {

    private BlockShop plugin;
    public ShopLogger(BlockShop main) {
        plugin = main;
        enabled = main.getConfig().getBoolean("log", true);

        logFile = new File(main.getDataFolder(), "logs.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
    }
    private boolean enabled;
    private File logFile;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void log(String message) {
        if (enabled) {
            
            try (FileWriter fw = new FileWriter(logFile, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
                String timestamp = java.time.LocalDateTime.now().toString();
                out.println("[" + timestamp + "] " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
