package com.ar.askgaming.bettershop.Auctions;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;

public class AuctionManager {

    private BetterShop plugin;
    File file;
    FileConfiguration config;

    public AuctionManager(BetterShop main) {
        plugin = main;

        file = new File(plugin.getDataFolder(), "auctions.yml");
        if (!file.exists()) {
            plugin.saveResource("auctions.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        Set<String> keys = config.getKeys(false);
        if (keys == null) {
            return;
        }
        for (String key : keys) {
            Object obj = config.get(key);
            if (obj instanceof Auction) {
                Auction auction = (Auction) obj;
                auctions.put(auction.getOwner(), auction);
            }
        }
        createInventories();

    }
    
    private HashMap<OfflinePlayer, Auction> auctions = new HashMap<>();

    public HashMap<OfflinePlayer, Auction> getAuctions() {
        return auctions;
    }  

    private HashMap<Integer, Inventory> inventories = new HashMap<>();

    public void createAuction(Player player, double price, int time, ItemStack item) {

        if (auctions.containsKey(player)) {
            player.sendMessage("You already have an auction.");
            return;
        }

        ItemStack clone = item.clone();
        item.setAmount(0);

        Auction auction = new Auction(player, price, time, clone);
        auctions.put(player, auction);
        config.set(player.getUniqueId().toString(), auction);
        saveConfig();

        addItemToInventory(auction);

    }
    private void createInventories() {
        int size = auctions.size();
        int pages = size / 54;
        if (size % 54 != 0) {
            pages++;
        }
        for (int i = 0; i < pages; i++) {
            Inventory inv = Bukkit.createInventory(null, 54, "Auctions " + (i + 1));
            inventories.put(i, inv);
        }
        for (int i = 0; i < size; i++) {
            Auction auction = (Auction) auctions.values().toArray()[i];
            ItemStack item = auction.getItem();
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
        Inventory inv = Bukkit.createInventory(null, 54, "Auctions " + (inventories.size() + 1));
        inventories.put(inventories.size(), inv);
        inv.addItem(item);
    }

    public Auction getAuctionByItemStack(ItemStack item) {
        for (Auction auction : auctions.values()) {
            if (auction.getItem().equals(item)) {
                return auction;
            }
        }
        return null;
    }

}
