package com.ar.askgaming.bettershop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.bettershop.Listeners.BlockBreakListener;
import com.ar.askgaming.bettershop.Listeners.InventoryInteractListener;
import com.ar.askgaming.bettershop.Listeners.InventoryMoveItemListener;
import com.ar.askgaming.bettershop.Listeners.PlayerBlockListener;
import com.ar.askgaming.bettershop.Listeners.PlayerPickUpListener;
import com.ar.askgaming.realisticeconomy.RealisticEconomy;

import net.milkbowl.vault.economy.Economy;


public class BlockShop extends JavaPlugin {

    private ShopManager blockShopManager;
    private ItemShopManager itemShopManager;

    private DataHandler dataHandler;
    private Economy vaultEconomy;
    private RealisticEconomy realisticEconomy;

    public  void onEnable() {
        
        saveDefaultConfig();

        ConfigurationSerialization.registerClass(Shop.class,"Shop");

        dataHandler = new DataHandler(this);
        
        blockShopManager = new ShopManager(this);

        getCommand("shop").setExecutor(new Commands(this));

        getServer().getPluginManager().registerEvents(new PlayerPickUpListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryMoveItemListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryInteractListener(this), this);

        //Vault Integration

        if (getServer().getPluginManager().isPluginEnabled("RealisticEconomy")) {
            realisticEconomy = (RealisticEconomy) getServer().getPluginManager().getPlugin("RealisticEconomy");
        } else {
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
        }
    }
    public void onDisable() {
        for (Entity entity : protectedEntities) {
            entity.remove();
        }
        
        for (Shop shop : getBlockShopManager().getShops().values()) {
            shop.getItemDisplay().remove();
            shop.getTextDisplay().remove();
            if (shop.getInventory().getViewers()!=null) {
                shop.getInventory().getViewers().forEach(v -> 
                    v.closeInventory()
                );
            }
            if (shop.getInventory().getContents()!=null) {
                for (int i = 0; i < shop.getInventory().getContents().length; i++) {
                    if (shop.getInventory().getItem(i) != null) {
                        getItemShopManager().removeShopLore(shop.getInventory().getItem(i));
                    }
                }
            }
        }
    }

    public ShopManager getBlockShopManager() {
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
    public void setItemShopManager(ItemShopManager itemShopManager) {
        this.itemShopManager = itemShopManager;
    }
    public List<Entity> protectedEntities = new ArrayList<>();
    
}