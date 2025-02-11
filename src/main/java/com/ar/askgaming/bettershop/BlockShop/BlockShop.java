package com.ar.askgaming.bettershop.BlockShop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;


public class BlockShop implements ConfigurationSerializable {

    private BetterShop plugin = BetterShop.getPlugin(BetterShop.class);

    private Block blockShop;
    private World world;
    private Inventory inventory;
    private OfflinePlayer owner;
    private String text = "-";
    private String name;
    private Location location;

    private ItemStack itemStack;
    private boolean isServerShop = false;

    private TextDisplay textDisplay;
    private ItemDisplay itemDisplay;

    // Constructor created by command
    public BlockShop(Block targetBlock, ItemStack itemStack, Player owner, String name) {
        
        blockShop = targetBlock;
        world = targetBlock.getWorld();
        location = targetBlock.getLocation();
        this.itemStack = itemStack;
        this.owner = owner;
        this.name = name;

        text = name;

        initializeShop();
    }

    // Desrealization
    public BlockShop(Map<String, Object> map) {
        name = map.get("name").toString();
        owner = plugin.getServer().getOfflinePlayer(UUID.fromString(map.get("owner").toString()));
        text = map.get("text").toString();
        location = (Location) map.get("location");
        blockShop = location.getBlock();

        if (location != null && blockShop.getType().isAir()) {
            blockShop.setType(Material.CHEST);
            plugin.getLogger().warning("BlockShop " + name + " was not found, creating a new empty chest in its place.");
        }

        world = location.getWorld();
        itemStack = (ItemStack) map.get("itemstack");
        if (map.get("itemstack") != null) {
            itemStack = (ItemStack) map.get("itemstack");
        }

        isServerShop = Boolean.parseBoolean(map.get("isServerShop").toString());

        initializeShop();
    }
    private void initializeShop() {
        
        if (blockShop.getState() instanceof InventoryHolder) {
            inventory = ((InventoryHolder) blockShop.getState()).getInventory();
        }

        spawnItem();
        setTextDisplay();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        map.put("location", location);
        map.put("itemstack", itemStack);
        map.put("owner", owner.getUniqueId().toString());
        map.put("text", text);
        map.put("name", name);
        map.put("isServerShop", String.valueOf(isServerShop));

        return map;
    }
            
    private void setTextDisplay() {

        if (textDisplay != null) {
            textDisplay.setText(text);
            return;
        }
                    
        textDisplay = (TextDisplay) world.spawn(location.clone().add(0.5, 1.5, 0.5), TextDisplay.class);
        plugin.protectedEntities.add(textDisplay);
        textDisplay.setText(text);
        textDisplay.setLineWidth(72);
        textDisplay.setBillboard(Billboard.CENTER);
    }
    //#region setItem
    public void setItem(ItemStack toChangue){
        itemStack = toChangue;
        spawnItem();
    }
    private void spawnItem(){
        if (itemDisplay != null) {
            itemDisplay.setItemStack(itemStack);
            return;
        }
        itemDisplay = (ItemDisplay) world.spawn(location.clone().add(0.5, 1.15, 0.5), ItemDisplay.class);
        itemDisplay.setItemStack(itemStack);
        itemDisplay.setItemDisplayTransform(ItemDisplayTransform.GROUND);
        plugin.protectedEntities.add(itemDisplay);
        itemDisplay.setBillboard(Billboard.CENTER);
        
    }
    public void setText(String text) {
 
         textDisplay.setText(text);
         this.text = text;
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

    public Block getBlockShop() {
        return blockShop;
    }

    public static BlockShop deserialize(Map<String, Object> map) {
        
        return new BlockShop(map);
    }
    
    public boolean isServerShop() {
        return isServerShop;
    }
    public Location getLocation() {
        return location;
    }

    public void setServerShop(boolean isServerShop) {
        this.isServerShop = isServerShop;
    }
    public TextDisplay getTextDisplay() {
        return textDisplay;
    }

    public ItemDisplay getItemDisplay() {
        return itemDisplay;
    }
}
