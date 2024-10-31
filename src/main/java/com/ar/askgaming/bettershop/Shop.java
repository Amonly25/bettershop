package com.ar.askgaming.bettershop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

public class Shop {

    private Block blockShop;
    private World world;
    private Main plugin;
    private Item item;
    private ItemStack itemStack;
    public ItemStack getItemStack() {
        return itemStack;
    }
    private Inventory inventory;
    private Player onwer;

    private Hologram hologram;

    private ShopType type;

    public enum ShopType {
        BUY,
        SELL
    }

    public Shop(Block targetBlock, ItemStack itemStack, Player owner) {
        plugin = Main.getPlugin(Main.class);

        blockShop = targetBlock;
        this.onwer = owner;
        this.itemStack = itemStack;

        InventoryHolder inventoryHolder = (InventoryHolder) blockShop.getState();
        inventory = inventoryHolder.getInventory();

        world = targetBlock.getWorld();

        plugin.getShops().put(blockShop.getLocation(), this);

        item = world.dropItem(blockShop.getLocation().add(0.5, 1, 0.5), itemStack);
        hologram = DHAPI.createHologram(item.getUniqueId().toString(), item.getLocation().add(0, 1.3, 0), true);
        DHAPI.addHologramLine(hologram, "line1 test");
        DHAPI.addHologramLine(hologram, "line2 test");

       // item.setCustomName("Item name");

        setItem();
    }

    public Entity getItem() {
        return item;
    }
    public Block getBlockShop() {
        return blockShop;
    }
    public void remove() {
        item.remove();
        hologram.delete();
        plugin.getShops().remove(blockShop.getLocation());
    }
    public void setItem(){

        //item.setCustomNameVisible(true);
        item.setUnlimitedLifetime(true);
        item.setPersistent(true);
        item.setInvulnerable(true);
        item.setGravity(false);
        item.setVelocity(item.getVelocity().multiply(0));
    }
    public Inventory getInventory() {
        return inventory;
    }
    public Player getOnwer() {
        return onwer;
    }
    public void setOnwer(Player onwer) {
        this.onwer = onwer;
    }

    public void setType(ShopType type) {
        //item.setCustomName("Shop " + type.toString());
        DHAPI.setHologramLine(hologram, 0,"test");
        DHAPI.setHologramLine(hologram, 1,"test");
        this.type = type;
    }
    public ShopType getType() {
        return type;
    }
}
