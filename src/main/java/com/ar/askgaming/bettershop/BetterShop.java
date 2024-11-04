package com.ar.askgaming.bettershop;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.bettershop.Listeners.BlockBreakListener;
import com.ar.askgaming.bettershop.Listeners.InventoryInteractListener;
import com.ar.askgaming.bettershop.Listeners.InventoryMoveItemListener;
import com.ar.askgaming.bettershop.Listeners.PlayerBlockListener;
import com.ar.askgaming.bettershop.Listeners.PlayerPickUpListener;
import com.ar.askgaming.realisticeconomy.RealisticEconomy;

import net.milkbowl.vault.economy.Economy;


public class BetterShop extends JavaPlugin {

    private BlockShopManager blockShopManager;
    private ItemShopManager itemShopManager;
    private DataHandler dataHandler;
    private Economy vaultEconomy;
    private RealisticEconomy realisticEconomy;

    public  void onEnable() {
        
        saveDefaultConfig();

        ConfigurationSerialization.registerClass(Shop.class,"Shop");

        dataHandler = new DataHandler(this);
        
        blockShopManager = new BlockShopManager(this);
        itemShopManager = new ItemShopManager();

        getCommand("shop").setExecutor(new Commands(this));

        getServer().getPluginManager().registerEvents(new PlayerPickUpListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryMoveItemListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryInteractListener(this), this);

        //Vault Integration
        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            getLogger().info("Vault found!");
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                getLogger().info("Non economy plugin found! disabling plugin");
                //getServer().getPluginManager().disablePlugin(this);
            } else {
                vaultEconomy = rsp.getProvider();
                getLogger().info("Vault Economy found!");
            }

        } else {
            getLogger().info("Vault not found! disabling plugin");
            //getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().isPluginEnabled("RealisticEconomy")) {
            realisticEconomy = (RealisticEconomy) getServer().getPluginManager().getPlugin("RealisticEconomy");
        }
    }
    public void onDisable() {
        
        for (Shop shop : getBlockShopManager().getShops().values()) {
            shop.getItem().remove();
            shop.getArmorStand().remove();
        }
    }

    public BlockShopManager getBlockShopManager() {
        return blockShopManager;
    }
    public DataHandler getDataHandler() {
        return dataHandler;
    }
    public ItemShopManager getItemShopManager() {
        return itemShopManager;
    }
    public Economy getEconomy() {
        return vaultEconomy;
    }
    public RealisticEconomy getRealisticEconomy() {
        return realisticEconomy;
    }
    
}