package com.ar.askgaming.bettershop.GlobalShop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BlockShop;
import com.ar.askgaming.bettershop.Commands;
import com.ar.askgaming.bettershop.Auctions.Auction;

public class GlobalShopManager {

    private File file;
    private FileConfiguration config;

    private BlockShop plugin;
    public GlobalShopManager(BlockShop main) {
        plugin = main;

        new Commands(plugin);

        file = new File(plugin.getDataFolder(), "globalshop.yml");
        if (!file.exists()) {
            plugin.saveResource("globalshop.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        List<String> keys = new ArrayList<>(config.getKeys(false));
        for (String key : keys) {
            Object obj = config.get(key);
            if (obj instanceof ItemStack) {
                items.add((ItemStack) obj);
            }
        }
        createInventories();

    }

    private NamespacedKey itemOwner = new NamespacedKey(plugin, "Owner");
    private NamespacedKey itemPrice = new NamespacedKey(plugin, "Price");

    private HashMap<Integer, Inventory> inventories = new HashMap<>();

    private List<ItemStack> items = new ArrayList<>();

    public List<ItemStack> getItems() {
        return items;
    }
    private void createInventories() {
        int size = items.size();
        int pages = size / 54;
        if (size % 54 != 0) {
            pages++;
        }
        for (int i = 0; i < pages; i++) {
            Inventory inv = Bukkit.createInventory(null, 54, "Auctions " + (i + 1));
            inventories.put(i, inv);
        }
        for (int i = 0; i < size; i++) {
            ItemStack item = items.get(i);
            int page = i / 54;
            inventories.get(page).setItem(i % 54, item);
        } 
    }
    public void saveConfig() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void addItemToInventory(Auction auction) {
        ItemStack item = auction.getItem();
        for (Inventory inv : inventories.values()) {
            if (inv.firstEmpty() != -1) {
                inv.addItem(item);
                return;
            }
        }
        Inventory inv = Bukkit.createInventory(null, 54, "Items " + (inventories.size() + 1));
        inventories.put(inventories.size(), inv);
        inv.addItem(item);
    }

    public Auction getGlobalItemStack(ItemStack item) {
        for (ItemStack i : items) {
            if (i.equals(item)) {
                return new Auction(null, 0, 0, i);
            }
        }
        return null;
    }

}
