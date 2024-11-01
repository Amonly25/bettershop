package com.ar.askgaming.bettershop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

public class Shop implements ConfigurationSerializable {

    private Main plugin;

    private Block blockShop;
    private World world;
    private Item item;
    private ItemStack itemStack;
    private Inventory inventory;
    private OfflinePlayer onwer;
    private Hologram hologram;
    private ShopType type;
    private int stock = 0;
    private String name;

    private double price = 0;

    public enum ShopType {
        BUY,
        SELL
    }

    public Shop(Block targetBlock, ItemStack itemStack, Player owner, String name) {
        plugin = Main.getPlugin(Main.class);

        blockShop = targetBlock;
        world = targetBlock.getWorld();
        this.onwer = owner;
        this.itemStack = itemStack;
        this.name = name;

        InventoryHolder inventoryHolder = (InventoryHolder) blockShop.getState();
        inventory = inventoryHolder.getInventory();

        item = world.dropItem(blockShop.getLocation().add(0.5, 1, 0.5), itemStack);
        hologram = DHAPI.createHologram(name, item.getLocation().add(0, 1.3, 0), true);
        DHAPI.addHologramLine(hologram, "-");
        DHAPI.addHologramLine(hologram, "Stock: " + stock + "- Price: " + price);

        setItem();

        // Control behavior of the shop before setting up
        plugin.getBlockShopManager().getShops().put(blockShop.getLocation(), this);

    }

    public Shop(Map<String, Object> map) {
        name = map.get("name").toString();
        blockShop = (Block) map.get("block");
        item = (Item) map.get("item");
        itemStack = (ItemStack) map.get("itemStack");
        onwer = plugin.getServer().getOfflinePlayer(UUID.fromString(map.get("onwer").toString()));
        hologram = DHAPI.getHologram(map.get("hologram").toString());
        type = ShopType.valueOf(map.get("type").toString());
        stock = (int) map.get("stock");
        price = (double) map.get("price");
    
    }

    public Entity getItem() {
        return item;
    }
    public Block getBlockShop() {
        return blockShop;
    }
    public void remove() {
        plugin.getDataHandler().getShopsConfig().set(name, null);
        plugin.getDataHandler().saveShop();
        
        item.remove();
        hologram.delete();
        plugin.getBlockShopManager().getShops().remove(blockShop.getLocation());
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
    public OfflinePlayer getOnwer() {
        return onwer;
    }
    public void setOnwer(Player onwer) {
        this.onwer = onwer;
    }
    public ItemStack getItemStack() {
        return itemStack;
    }
    public double getPrice() {
        return price;
    }
    public void setStock(int stock) {
        this.stock = stock;
        updateHolo();
    }
    public void setPrice(double price) {
        this.price = price;
        updateHolo();
    }

    public void updateHolo(){
        DHAPI.setHologramLine(hologram, 1,getHoloText("stock")+stock+" - "+getHoloText("price")+price);
    }

    public void setType(ShopType type) {
        DHAPI.setHologramLine(hologram, 0,getHoloText(type.toString().toLowerCase()));
        this.type = type;
    }
    public ShopType getType() {
        return type;
    }
    public String getName() {
        return name;
    }
    public int getStock() {
        int stock = 0;

        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null) {
                stock += itemStack.getAmount();
            }
        }
        return stock;
    }

    public boolean hasStock(){
        return stock > 0;
    }
    public List<ItemStack> get(int amount){
        List<ItemStack> items = new ArrayList<>();
        if (hasStock()){
            for (ItemStack itemStack : inventory.getContents()) {
                if (itemStack != null) {

                    if (itemStack.getAmount() >= amount) {
                        
                        itemStack.setAmount(itemStack.getAmount() - amount);
                        ItemStack cloned = itemStack.clone();
                        cloned.setAmount(amount);
                        items.add(itemStack); 
                        break;

                    } else {
                        amount -= itemStack.getAmount();
                        items.add(itemStack);
                        inventory.remove(itemStack);
                    }
                }
            }
        }
        return items;
    }

    private String getHoloText(String key){
        return plugin.getConfig().getString("hologram."+key,"Undefined");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        map.put("block", blockShop);
        map.put("item", item);
        map.put("itemStack", itemStack);
        map.put("onwer", onwer.getUniqueId().toString());
        map.put("hologram", hologram.getName());
        map.put("type", type.toString());
        map.put("stock", stock);
        map.put("name", name);
        map.put("price", price);
        
        return map;
    }
    public static Shop deserialize(Map<String, Object> map) {
        return new Shop(map);
    }
    public void save() {
        plugin.getDataHandler().getShopsConfig().set(name, this);
        plugin.getDataHandler().saveShop();
    }

}
