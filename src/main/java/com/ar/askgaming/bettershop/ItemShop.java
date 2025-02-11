package com.ar.askgaming.bettershop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemShop implements ConfigurationSerializable{

    private ItemStack item;
    private double price;
    private UUID owner;
    private Number id;

    public ItemShop(Player owner, ItemStack item, double price) {
        this.item = item;
        this.price = price;
        this.owner = owner.getUniqueId();
        this.id = System.currentTimeMillis();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("item", item);
        map.put("price", price);
        map.put("owner", owner);
        map.put("id", id);
        return map;
    }

    public ItemStack getItem() {
        return item;
    }
    public void setItem(ItemStack item) {
        this.item = item;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public UUID getOwner() {
        return owner;
    }
    public void setOwner(UUID owner) {
        this.owner = owner;
    }
    public Number getId() {
        return id;
    }
    public void setId(Number id) {
        this.id = id;
    }
    
}
