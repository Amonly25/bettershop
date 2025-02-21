package com.ar.askgaming.bettershop.Trade;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Trade {

    private Player creator;
    private Player target;
    private ItemStack item;
    private Double price;
    private Inventory inventory;

    public Inventory getInventory() {
        return inventory;
    }
    public Trade(Player creator, Player target, ItemStack item, Double price) {
        this.creator = creator;
        this.target = target;
        this.item = item;
        this.price = price;

        inventory = creator.getServer().createInventory(null, 9, "Trade > " + target.getName());
        inventory.setItem(4, item);
    }
    public Player getCreator() {
        return creator;
    }

    public Player getTarget() {
        return target;
    }

    public ItemStack getItem() {
        return item;
    }

    public Double getPrice() {
        return price;
    }
}
