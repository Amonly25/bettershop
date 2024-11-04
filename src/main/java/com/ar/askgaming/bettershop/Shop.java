package com.ar.askgaming.bettershop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;


public class Shop implements ConfigurationSerializable {

    private BetterShop plugin = BetterShop.getPlugin(BetterShop.class);

    private Block blockShop;
    private World world;
    private Item item;
    private Inventory inventory;
    private OfflinePlayer owner;
    private String title = "-";
    private String subTitle = "-";
    private String name;
    private ArmorStand armorStand;
    private Location location;
    private ItemStack itemStack;
    private boolean isServerShop = false;

    // Constructor created by command
    public Shop(Block targetBlock, ItemStack itemStack, Player owner, String name) {
        plugin.getBlockShopManager().getShops().put(location, this);
        blockShop = targetBlock;
        world = targetBlock.getWorld();
        location = targetBlock.getLocation();
        this.itemStack = itemStack;
        this.owner = owner;
        this.name = name;

        initializeShop();
        save();
    }

    // Desrealization
    public Shop(Map<String, Object> map) {
        name = map.get("name").toString();
        owner = plugin.getServer().getOfflinePlayer(UUID.fromString(map.get("owner").toString()));
        title = map.get("title").toString();
        subTitle = map.get("subtitle").toString();
        location = (Location) map.get("location");
        blockShop = location.getBlock();
        world = location.getWorld();
        itemStack = (ItemStack) map.get("itemstack");
        isServerShop = Boolean.parseBoolean(map.get("isServerShop").toString());

        if (blockShop.getState() instanceof InventoryHolder) {
            inventory = ((InventoryHolder) blockShop.getState()).getInventory();
        }

        initializeShop();
    }
    private void initializeShop() {
        
        if (blockShop.getState() instanceof InventoryHolder) {
            inventory = ((InventoryHolder) blockShop.getState()).getInventory();
        }

        spawnItem();
        setAmorStand();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        map.put("location", location);
        map.put("itemstack", itemStack);
        map.put("owner", owner.getUniqueId().toString());
        map.put("title", title);
        map.put("subtitle", subTitle);
        map.put("name", name);
        map.put("isServerShop", String.valueOf(isServerShop));

        return map;
    }
                
    public void remove() {
        try {
            plugin.getDataHandler().getShopsConfig().set(name, null);
            plugin.getDataHandler().saveShop();
    
            for (ItemStack item : inventory.getContents()) {
                if (item != null) {
                    plugin.getItemShopManager().removeShopProperties(item);
                }
            }
            
            armorStand.remove();
            item.remove();

            plugin.getBlockShopManager().getShops().remove(blockShop.getLocation());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setAmorStand() {

        if (armorStand != null) {
            armorStand.remove();
        }
                    
        armorStand = (ArmorStand) world.spawnEntity(location.clone().add(0.5, 1.5, 0.5), EntityType.ARMOR_STAND);
        armorStand.setVisible(false); // Hacer el armor stand invisible
        armorStand.setCustomName(title);
        armorStand.setCustomNameVisible(true); // Mostrar el nombre del armor stand
        armorStand.setGravity(false); // Evitar que el armor stand caiga
        armorStand.setMarker(true); // Reducir el hitbox del armor stand
    }

    public void setItem(ItemStack toChangue){
        itemStack = toChangue;
        spawnItem();
        save();
    }
    private void spawnItem(){
        if (item != null) {
            item.remove();
        }
        item = world.dropItem(location.clone().add(0.5, 1, 0.5), itemStack);
        item.setUnlimitedLifetime(true);
        item.setPersistent(true);
        item.setInvulnerable(true);
        item.setGravity(false);
        item.setVelocity(item.getVelocity().multiply(0));
        item.setCustomName(subTitle);
        item.setCustomNameVisible(true);
    }
    public void setTitle(String title) {
 
         armorStand.setCustomName(title);
         this.title = title;
         save();
    }
    public void setSubtitle(String description) {

        item.setCustomName(description);
        this.subTitle = description;
        save();
    }
    public void save() {
        plugin.getDataHandler().getShopsConfig().set(name, this);
        plugin.getDataHandler().saveShop();
    }
    public Inventory getInventory() {
        return inventory;
    }
    public OfflinePlayer getOnwer() {
        return owner;
    }
    public void setOnwer(Player onwer) {
        this.owner = onwer;
    }
    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getName() {
        return name;
    }
    public Entity getItem() {
        return item;
    }
    public Block getBlockShop() {
        return blockShop;
    }

    public static Shop deserialize(Map<String, Object> map) {
        
        return new Shop(map);
    }
    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subTitle;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }
    
    public boolean isServerShop() {
        return isServerShop;
    }

    public void setServerShop(boolean isServerShop) {
        this.isServerShop = isServerShop;
        save();
    }
}
