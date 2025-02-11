package com.ar.askgaming.bettershop;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class ShopOwner {

    private UUID owner;
    private HashMap<Number, ItemShop> items = new HashMap<>();

    public UUID getOwner() {
        return owner;
    }
    public HashMap<Number, ItemShop> getItems() {
        return items;
    }
    public void setItems(HashMap<Number, ItemShop> items) {
        this.items = items;
    }

    public boolean isOwnerOfItemStack(ItemStack itemStack) {
        for (ItemShop itemShop : items.values()) {
            if (itemShop.getItem() == itemStack) {
                return true;
            }
        }
        return false;
    }
}
