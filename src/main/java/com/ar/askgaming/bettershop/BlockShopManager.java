package com.ar.askgaming.bettershop;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

public class BlockShopManager {

    private BetterShop plugin;

    private HashMap<Location, Shop> shops = new HashMap<>();

    public HashMap<Location, Shop> getShops() {
        return shops;
    }

    public BlockShopManager(BetterShop main) {
        plugin = main;

        FileConfiguration config = plugin.getDataHandler().getShopsConfig();
        Set<String> protectionKeys = config.getKeys(false);

        // Iterar sobre todas las keys y cargar cada Shop
        for (String key : protectionKeys) {
            Object obj = config.get(key);
            if (obj instanceof Shop) {
                Shop shop = (Shop) obj;

                // Guardar cada Protection en el mapa con su clave
                shops.put(shop.getBlockShop().getLocation(), shop);
            }
        }
    }

    public boolean isShop(Block block){

        for (Shop shop : getShops().values()) {
            if(shop.getBlockShop().equals(block)){
                return true;
            }
        }

        return false;
    }
    public Shop getByBlock(Block block){
        for (Shop shop : getShops().values()) {
            if(shop.getBlockShop().equals(block)){
                return shop;
            }
        }
        return null;
    }

    public Shop getByName(String string) {
        for (Shop shop : getShops().values()) {
            if(shop.getName().equals(string)){
                return shop;
            }
        }
        return null;
    }
    
}
