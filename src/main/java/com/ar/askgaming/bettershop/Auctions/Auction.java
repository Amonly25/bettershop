package com.ar.askgaming.bettershop.Auctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.bettershop.BetterShop;

public class Auction implements ConfigurationSerializable {

    private BetterShop plugin = BetterShop.getInstance();

    private ItemStack item;
    private OfflinePlayer owner;
    private double basePrice;
    private double newPrice;
    private Long createdTime;
    private Long timeLeft;
    private Inventory inv;
    private String id;
    private OfflinePlayer winner;
    private boolean hasEnded;

    private HashMap<String, Double> bets = new HashMap<>();

    public HashMap<String, Double> getBets() {
        return bets;
    }

    public Auction(String id, Player owner, double price, ItemStack item) {
        this.id = id;
        this.owner = owner;
        this.basePrice = price;
        this.timeLeft = Long.valueOf(86400 * 1000);
        this.createdTime = System.currentTimeMillis();
        this.item = item;
        this.newPrice = price;
        hasEnded = false;

        createInventoryAndItem();
    }
    @SuppressWarnings("unchecked")
    public Auction(Map<String, Object> map) {
        String ownerUUID = (String) map.get("owner");
        this.owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID));
        this.basePrice = (double) map.get("price");
        this.item = (ItemStack) map.get("itemstack");

        Number createdTime = (Number) map.get("created_time");
        this.createdTime = createdTime.longValue();
        
        Number timeLeft = (Number) map.get("time_left");
        this.timeLeft = timeLeft.longValue();
        
        this.newPrice = (double) map.get("newPrice");

        this.bets = (HashMap<String, Double>) map.get("bets");

        hasEnded = (boolean) map.get("ended");
    }
    public void createInventoryAndItem() {
        inv = Bukkit.createInventory(null, 9, "Auction id: " + id + " - " + owner.getName());
        inv.setItem(3, item);
        updateOrCreateItems();
            
    }

    public void updateOrCreateItems() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Info");
        List<String> lore = plugin.getConfig().getStringList("auctions.info_lore");
        List<String> newLore = new ArrayList<>();
        for (String s : lore) {
            s = s.replace("{owner}", owner.getName());
            s = s.replace("{price}", String.valueOf(basePrice));
            s = s.replace("{time}", getTimeLeftFormatted());
            s = s.replace("{ended}", String.valueOf(hasEnded));
            s = s.replace("&", "§");
            newLore.add(s);
        }
        meta.setLore(newLore);
        item.setItemMeta(meta);

        inv.setItem(5, item);

        ItemStack bets = new ItemStack(Material.PAPER);
        ItemMeta metaBets = bets.getItemMeta();
        metaBets.setDisplayName(plugin.getConfig().getString("auctions.bets_name").replace('&', '§'));
        List<String> loreBets = new ArrayList<>();
        for (String key : this.bets.keySet()) {
            loreBets.add(key + " - " + this.bets.get(key));
        }
        metaBets.setLore(loreBets);
        bets.setItemMeta(metaBets);
        inv.setItem(6, bets);
    }
    private String getTimeLeftFormatted(){
        long time = timeLeft;

        if (time < 0) {
            return "0h 0m";
        }

        long hours = time / 3600000;
        time = time - hours * 3600000;
        long minutes = time / 60000;
        return hours + "h " + minutes + "m";
    }
    public double getNewPrice() {
        return newPrice;
    }
    
    public OfflinePlayer getWinner() {
        return winner;
    }
    public void setWinner(OfflinePlayer winner) {
        this.winner = winner;
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Winner");
        List<String> lore = new ArrayList<>();
        lore.add("Winner: " + winner.getName());
        lore.add("Price: " + newPrice);
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(6, item);
        inv.getItem(5).setAmount(0);

    }
    public boolean isHasEnded() {
        return hasEnded;
    }
    public void setHasEnded(boolean hasEnded) {
        this.hasEnded = hasEnded;
    }
    public void setNewPrice(double newPrice) {
        this.newPrice = newPrice;
    }
    public Inventory getInv() {
        return inv;
    }
    public void setInv(Inventory inv) {
        this.inv = inv;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Inventory getInventory() {
        return inv;
    }

    public ItemStack getItem() {
        return item;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public double getPrice() {
        return basePrice;
    }
    public void setPrice(double price) {
        this.basePrice = price;
    }
    public Long getTimeLeft() {
        return timeLeft;
    }
    public void setTimeLeft(Long timeLeft) {
        this.timeLeft = timeLeft;
    }
    public Long getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("owner", owner.getUniqueId().toString());
        map.put("price", basePrice);
        map.put("itemstack", item);
        map.put("created_time", createdTime);
        map.put("time_left", timeLeft);
        map.put("newPrice", newPrice);
        map.put("bets", bets);
        map.put("id", id);
        map.put("ended", hasEnded);
        
        return map;
        
    }
    
}