package com.ar.askgaming.bettershop;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.bettershop.Listeners.BlockBreakListener;
import com.ar.askgaming.bettershop.Listeners.InventoryMoveItemListener;
import com.ar.askgaming.bettershop.Listeners.PlayerBlockListener;
import com.ar.askgaming.bettershop.Listeners.PlayerPickUpListener;

import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import eu.decentsoftware.holograms.plugin.DecentHologramsPlugin;


public class Main extends JavaPlugin {

    private BlockShopManager blockShopManager;

    public  void onEnable() {
        
        saveDefaultConfig();

        getCommand("shop").setExecutor(new Commands(this));

        getServer().getPluginManager().registerEvents(new PlayerPickUpListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryMoveItemListener(this), this);

        blockShopManager = new BlockShopManager(this);
        
    }
    public void onDisable() {
        
        for (Shop shop : shops.values()) {
            shop.remove();
        }
    }

    private HashMap<Location, Shop> shops = new HashMap<>();

    public HashMap<Location, Shop> getShops() {
        return shops;
    }
    public BlockShopManager getBlockShopManager() {
        return blockShopManager;
    }
    
}