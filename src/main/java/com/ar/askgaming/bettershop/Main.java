package com.ar.askgaming.bettershop;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.bettershop.Listeners.PlayerPickUpListener;


public class Main extends JavaPlugin {

    public  void onEnable() {
        
        saveDefaultConfig();

        getCommand("shop").setExecutor(new Commands(this));
        getServer().getPluginManager().registerEvents(new PlayerPickUpListener(this), this);
        
    }
    public void onDisable() {
        getConfig().set("items", items);
        for (Item item : items.keySet()) {
            item.remove();
        }
    }

    public HashMap<Item, Location> items = new HashMap<>();
    
}