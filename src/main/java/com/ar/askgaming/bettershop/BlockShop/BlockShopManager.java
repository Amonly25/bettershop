package com.ar.askgaming.bettershop.BlockShop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;

public class BlockShopManager {

    private BetterShop plugin;
    private File file;
    private FileConfiguration config;

    private HashMap<Location, BlockShop> shops = new HashMap<>();

    public HashMap<Location, BlockShop> getShops() {
        return shops;
    }

    public BlockShopManager(BetterShop main) {
        plugin = main;

        new Commands(main, this);

        file = new File(main.getDataFolder(), "shops.yml");
        
        if (!file.exists()) {
            plugin.saveResource("shops.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        loadShops();
    }
    private void loadShops() {
        Set<String> keys = config.getKeys(false);
        if (keys.isEmpty()) return;

        // Iterar sobre todas las keys y cargar cada Shop
        for (String key : keys) {
            Object obj = config.get(key);
            if (obj instanceof BlockShop) {
                BlockShop shop = (BlockShop) obj;

                // Guardar cada Protection en el mapa con su clave
                shops.put(shop.getBlockShop().getLocation(), shop);

                for (ItemStack item : shop.getInventory().getContents()) {
                    if (item != null) {
                        plugin.getItemShopManager().setShopLore(item);
                    }
                }
            }
        }
    }
    public void save(){
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //#region createShop
    public void createShop(Block block, ItemStack itemStack, Player owner, String name) {
        BlockShop shop = new BlockShop(block, itemStack, owner, name);
        shops.put(block.getLocation(), shop);
        config.set(shop.getName(), shop);
        save();
        plugin.getShopLogger().log("Shop created: " + shopToString(shop));
        
    }
    //#region additem
    public boolean addItemShop(Player player, BlockShop shop, ItemStack itemInMainHand, double price) {
        int slot = shop.getInventory().firstEmpty();

        if (slot != -1) {
            ItemStack item = itemInMainHand.clone();
            plugin.getItemShopManager().makeItemSellable(player, item, price);
            shop.getInventory().setItem(slot, item);
            plugin.getShopLogger().log("Item added to shop: " + shop.getName() + " Item: " + item.getType().name() + " Price: " + price + " Amount: " + item.getAmount());
            return true;
        }        

        player.sendMessage(plugin.getLang().getFrom("shop.no_shop_space", player));
        return false;

    }
    //#region remove
    public boolean remove(BlockShop shop) {
        try {
            shops.remove(shop.getBlockShop().getLocation());
            config.set(shop.getName(), null);
            save();
    
            for (ItemStack item : shop.getInventory().getContents()) {
                if (item != null) {
                    plugin.getItemShopManager().removeItemShopProperties(item);
                }
            }
            
            shop.getTextDisplay().remove();
            shop.getItemDisplay().remove();

            plugin.getShopLogger().log("Shop removed: " + shopToString(shop));
            return true;
        
        } catch (Exception e) {
            plugin.getShopLogger().log("Error removing shop: " + shopToString(shop));
            e.printStackTrace();
            return false;
        }
    }
    //#region get
    public BlockShop getByBlock(Block block){
        for (BlockShop shop : getShops().values()) {
            if(shop.getBlockShop().equals(block)){
                return shop;
            }
        }
        return null;
    }
    public BlockShop getByInventory(Inventory inventory) {
        for (BlockShop shop : getShops().values()) {
            if(shop.getInventory().equals(inventory)){
                return shop;
            }
        }
        return null;
    }
    public BlockShop getByName(String string) {
        for (BlockShop shop : getShops().values()) {
            if(shop.getName().equals(string)){
                return shop;
            }
        }
        return null;
    }
    public BlockShop getByLocation(Location location) {
        for (BlockShop shop : getShops().values()) {
            if(shop.getLocation().equals(location)){
                return shop;
            }
        }
        return null;
    }
    //#region hasPermission
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

    public boolean hasAdminPermission(Player player, BlockShop shop) {
        if (player.hasPermission("bettershop.admin")) {
            return true;
        }
        if (shop.getOnwer().getUniqueId().equals(player.getUniqueId())) {
            return true;
        }
        return false;
    }
    private String shopToString(BlockShop shop) {
        Block block = shop.getBlockShop();

        return shop.getName() + " by " + shop.getOnwer().getName() + " at " + block.getWorld().getName() + " " + block.getX() + " " + block.getY() + " " + block.getZ();
    }
    //#region set
    public void setText(BlockShop shop, String text) {
        shop.setText(text);
        config.set(shop.getName(), shop);
        save();
    }   
    public void setItem(BlockShop shop, ItemStack item) {
        shop.setItem(item);
        config.set(shop.getName(), shop);
        save();
    }
    public void setServerShop(BlockShop shop, boolean isServerShop) {
        shop.setServerShop(isServerShop);
        config.set(shop.getName(), shop);
        save();
    }   
}
