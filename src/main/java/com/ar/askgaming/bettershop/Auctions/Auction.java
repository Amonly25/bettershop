package com.ar.askgaming.bettershop.Auctions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Auction implements ConfigurationSerializable {

    private ItemStack item;
    private OfflinePlayer owner;
    private double price;
    private int createdTime;

    private int timeLeft;

    private Inventory inventory;

    public Auction(Player owner, double price, int timeLeft,ItemStack item) {
        this.owner = owner;
        this.price = price;
        this.timeLeft = timeLeft;
        this.createdTime = (int) System.currentTimeMillis();
        this.item = item;
        
        createInventory();
    }
    public Auction(Map<String, Object> map) {
        String ownerUUID = (String) map.get("owner");
        this.owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID));
        this.price = (double) map.get("price");
        this.item = (ItemStack) map.get("itemstack");
        this.createdTime = (int) map.get("created_time");
        this.timeLeft = (int) map.get("time_left");
        
        createInventory();
    }
    public Inventory getInventory() {
        return inventory;
    }

    public ItemStack getItem() {
        return item;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public int getTimeLeft() {
        return timeLeft;
    }
    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }
    public int getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(int createdTime) {
        this.createdTime = createdTime;
    }
    private void createInventory() {
        inventory = Bukkit.getServer().createInventory(null, 9, owner.getName() + "'s Auction");
        inventory.addItem(item);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("owner", owner.getUniqueId().toString());
        map.put("price", price);
        map.put("itemstack", item);
        map.put("created_time", createdTime);
        map.put("time_left", timeLeft);
        return map;
        

    }
}