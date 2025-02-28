package com.ar.askgaming.bettershop.Managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.ar.askgaming.bettershop.BetterShop;

public class ItemShopManager{

    private List<String> itemShopLore;

    private NamespacedKey itemPrice;
    private NamespacedKey itemSeller;

    public ItemShopManager(BetterShop plugin) {

        itemShopLore = plugin.getConfig().getStringList("item_shop.lore");

        itemPrice = new NamespacedKey(plugin, "bettershop.item_price");
        itemSeller = new NamespacedKey(plugin, "bettershop.item_seller");

    }

    public boolean isItemShop(ItemStack itemStack) {

        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (data.has(itemPrice, PersistentDataType.DOUBLE) && data.has(itemSeller, PersistentDataType.STRING)) {
                return true;
            }
        }
        
        return false;
    }

    public void setShopProperties(ItemStack itemStack, double price, String seller, UUID sellerUUID) {

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(itemPrice, PersistentDataType.DOUBLE, price);
        data.set(itemSeller, PersistentDataType.STRING, sellerUUID.toString());
        itemStack.setItemMeta(meta);
        setShopLore(itemStack, price, seller);
        
    }
    public void setShopLore(ItemStack item){
        double price = getPrice(item);
        UUID seller = getSeller(item);
        if (price == 0 || seller == null) return;
        setShopLore(item, price, Bukkit.getOfflinePlayer(seller).getName());
    }
    public void setShopLore(ItemStack itemStack, double price, String seller) {
        if (itemStack == null || itemStack.getItemMeta() == null) {
            return;
        }
        
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>();
    
        Map<String, String> replacements = Map.of(
            "${price}", String.format("%.2f", price),
            "{seller}", seller
        );
    
        for (String line : itemShopLore) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                line = line.replace(entry.getKey(), entry.getValue());
            }
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
    
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    //#region removeProps
    public void removeItemShopProperties(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().remove(itemPrice);
        meta.getPersistentDataContainer().remove(itemSeller);   
        itemStack.setItemMeta(meta);
        removeItemShopLore(itemStack);
    }
    public void removeItemShopLore(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) {
            return;
        }
        
        ItemMeta meta = itemStack.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());
            
            lore.removeIf(line -> itemShopLore.stream().anyMatch(shopLine -> line.startsWith(ChatColor.stripColor(shopLine.replace("${price}", "").replace("{seller}", "")))));
            
            meta.setLore(lore.isEmpty() ? null : lore);
        }
        itemStack.setItemMeta(meta);
    }

    public double getPrice(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return 0;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.getOrDefault(itemPrice, PersistentDataType.DOUBLE, 0.0);
    }
    
    public UUID getSeller(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String sellerUUID = data.get(itemSeller, PersistentDataType.STRING);
        return sellerUUID != null ? UUID.fromString(sellerUUID) : null;
    }
    public void makeItemSellable(Player player, ItemStack item, double price){
        setShopProperties(item, price, player.getName(), player.getUniqueId());
        //player.sendMessage(plugin.getLang().getFrom("shop.item_sellable", player).replace("{price}", price+""));
    }
    
}
