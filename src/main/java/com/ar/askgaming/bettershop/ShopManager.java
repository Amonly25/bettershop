package com.ar.askgaming.bettershop;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.hover.content.Item;

public class ShopManager {

    private BlockShop plugin;

    private HashMap<Location, Shop> shops = new HashMap<>();

    public HashMap<Location, Shop> getShops() {
        return shops;
    }

    public ShopManager(BlockShop main) {
        plugin = main;
        ItemShopManager itemShopManager = new ItemShopManager();
        plugin.setItemShopManager(itemShopManager);

        FileConfiguration config = plugin.getDataHandler().getShopsConfig();
        Set<String> protectionKeys = config.getKeys(false);

        // Iterar sobre todas las keys y cargar cada Shop
        for (String key : protectionKeys) {
            Object obj = config.get(key);
            if (obj instanceof Shop) {
                Shop shop = (Shop) obj;

                // Guardar cada Protection en el mapa con su clave
                shops.put(shop.getBlockShop().getLocation(), shop);

                for (ItemStack item : shop.getInventory().getContents()) {
                    if (item != null) {
                        itemShopManager.setShopLore(item);
                    }
                }
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
    public boolean hasPermissionAtBlockLocation(Player p, Block block) {

        BlockBreakEvent event = new BlockBreakEvent(block, p);

        // Llama al evento
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            event.setCancelled(true);
            return true;
        } else {
            return false;
        }
        
    }
    
}
