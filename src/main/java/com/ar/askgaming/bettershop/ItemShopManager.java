package com.ar.askgaming.bettershop;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.milkbowl.vault.economy.EconomyResponse;

public class ItemShopManager{

    private BetterShop plugin = BetterShop.getPlugin(BetterShop.class);

    private List<String> itemShopLore = List.of(
        "§7Price: §e{price} c/u",
        "§7Right click to buy 1",
        "§7Shift + Right click to buy all",
        "",
        "Owner: Click to cancel"
    );

    private NamespacedKey key = new NamespacedKey(plugin, "bettershop.item_price");

    public boolean isItemShop(ItemStack itemStack) {

        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.DOUBLE)) {
                return true;
            }
        }
        
        return false;
    }

    public void setShopProperties(ItemStack itemStack, double price) {

        ItemMeta meta = itemStack.getItemMeta();

        meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, price);


        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (int i = 0; i < itemShopLore.size(); i++) {
                lore.add(itemShopLore.get(i).replace("{price}", String.valueOf(price)));
            }
            meta.setLore(lore);
        } else {
            List<String> lore = new ArrayList<>();
            for (int i = 0; i < itemShopLore.size(); i++) {
                lore.add(itemShopLore.get(i).replace("{price}", String.valueOf(price)));
            }
            meta.setLore(lore);
        }

        itemStack.setItemMeta(meta);
        
    }
    public void removeShopProperties(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().remove(key);

        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            Predicate<String> isSimilar = str ->
            str.startsWith("§7Price:") || itemShopLore.contains(str);

            // Remove similar elements from list2
             lore.removeIf(isSimilar);
             meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
    }

    public double getPrice(ItemStack i) {
        return i.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
    }

    public boolean addItemShop(Shop shop, ItemStack itemInMainHand, double price) {
        int slot = shop.getInventory().firstEmpty();

        if (slot != -1) {
            ItemStack item = itemInMainHand.clone();
            setShopProperties(item, price);
            itemInMainHand.setAmount(0); // Clear the item in the hand
            shop.getInventory().setItem(slot, item);
            return true;
        }        
        shop.getOnwer().getPlayer().sendMessage("No hay espacio disponible.");
        return false;

    }
    //#region buyItem
    private void buyItem(Player buyer, ItemStack item, int amount, @Nullable OfflinePlayer seller) {
        double price = plugin.getItemShopManager().getPrice(item) * amount;
    
        if (plugin.getEconomy().getBalance(buyer) < price) {
            buyer.sendMessage("§cNo tienes suficiente dinero");
            return;
        }
    
        EconomyResponse response;
        if (seller != null) {
            response = plugin.getRealisticEconomy().getEconomyService().playerPayPlayer(buyer.getUniqueId(), seller.getUniqueId(), price);
        } else {
            response = plugin.getRealisticEconomy().getServerBank().deposit(price);
            if (response.transactionSuccess()) {
                plugin.getEconomy().withdrawPlayer(buyer, price);
            }
        }
    
        if (!response.transactionSuccess()) {
            buyer.sendMessage("§cError al comprar el item");
            return;
        }
    
        buyer.sendMessage("Has comprado con éxito " + amount + " items por " + price);
    
        ItemStack clonedItem = item.clone();
        plugin.getItemShopManager().removeShopProperties(clonedItem);
    
        if (amount == 1) {
            clonedItem.setAmount(1);
            item.setAmount(item.getAmount() - 1);
        } else {
            item.setAmount(0);
        }
    
        buyer.getInventory().addItem(clonedItem);
    }

    public void buyItemToPlayer(OfflinePlayer seller, Player buyer, ItemStack item, int amount) {
        buyItem(buyer, item, amount, seller);
    }
    
    public void buyItemToServer(Player buyer, ItemStack item, int amount) {
        buyItem(buyer, item, amount, null);
    }
    //#endregion
}
