package com.ar.askgaming.bettershop;

import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.bettershop.Listeners.BlockBreakListener;
import com.ar.askgaming.bettershop.Listeners.InventoryInteractListener;
import com.ar.askgaming.bettershop.Listeners.InventoryMoveItemListener;
import com.ar.askgaming.bettershop.Listeners.PlayerBlockListener;
import com.ar.askgaming.bettershop.Listeners.PlayerPickUpListener;


public class Main extends JavaPlugin {

    private BlockShopManager blockShopManager;
    private DataHandler dataHandler;

    public  void onEnable() {
        
        saveDefaultConfig();

        getCommand("shop").setExecutor(new Commands(this));

        getServer().getPluginManager().registerEvents(new PlayerPickUpListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryMoveItemListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryInteractListener(this), this);

        blockShopManager = new BlockShopManager(this);
        dataHandler = new DataHandler(this);

        if (getServer().getPluginManager().getPlugin("DecentHolograms") == null) {
            getLogger().severe("DecentHolograms not found, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);

        }
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault not found, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);

        }
        
    }
    public void onDisable() {
        
        // for (Shop shop : getBlockShopManager().getShops().values()) {
        //     shop.remove();
        // }
    }

    public BlockShopManager getBlockShopManager() {
        return blockShopManager;
    }
    public DataHandler getDataHandler() {
        return dataHandler;
    }
    
}