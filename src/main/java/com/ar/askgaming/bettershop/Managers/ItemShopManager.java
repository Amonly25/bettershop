package com.ar.askgaming.bettershop.Managers;

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

import com.ar.askgaming.bettershop.BlockShop;
import com.ar.askgaming.bettershop.Shop;

public class ItemShopManager{

    private BlockShop plugin = BlockShop.getPlugin(BlockShop.class);

    private List<String> itemShopLore;
    private String priceText;

    private NamespacedKey key = new NamespacedKey(plugin, "bettershop.item_price");

    public ItemShopManager() {
        itemShopLore = plugin.getConfig().getStringList("item_shop.lore");
        priceText = plugin.getConfig().getString("item_shop.price_text","Price:");
        itemShopLore.add(0, priceText + "{price}");

    }

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
        itemStack.setItemMeta(meta);
        setShopLore(itemStack);
        
    }
    public void setShopLore(ItemStack itemStack){
        double price = getPrice(itemStack);
        ItemMeta meta = itemStack.getItemMeta();
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
    //#region removeProps
    public void removeShopProperties(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().remove(key);
        itemStack.setItemMeta(meta);
        removeShopLore(itemStack);
    }
    public void removeShopLore(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();

        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            Predicate<String> isSimilar = str ->
            str.startsWith(priceText) || itemShopLore.contains(str);

            // Remove similar elements from list2
             lore.removeIf(isSimilar);
             meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
    }

    public double getPrice(ItemStack i) {
        return i.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
    }
    //#region additem
    public boolean addItemShop(Shop shop, ItemStack itemInMainHand, double price) {
        int slot = shop.getInventory().firstEmpty();

        if (slot != -1) {
            ItemStack item = itemInMainHand.clone();
            setShopProperties(item, price);
            itemInMainHand.setAmount(0); // Clear the item in the hand
            shop.getInventory().setItem(slot, item);
            return true;
        }        
        Player p = shop.getOnwer().getPlayer();
        p.sendMessage(plugin.getLang().getFrom("shop.no_shop_space", p));
        return false;

    }
    //#region buyItem
    private void buyItem(Player buyer, ItemStack item, int amount, @Nullable OfflinePlayer seller) {
        double price = plugin.getItemShopManager().getPrice(item) * amount;
        
        if (!processPayment(buyer, seller, price)) {
            buyer.sendMessage(plugin.getLang().getFrom("misc.error_buying", buyer));
            return;
        }
    
        buyer.sendMessage(plugin.getLang().getFrom("shop.buy", buyer).replace("{amount}", amount+"").replace("{price}", price+""));
    
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
    private boolean processPayment(Player buyer, OfflinePlayer seller, double price) {

        if (plugin.getRealisticEconomy()!= null) {
            return processRealisticPayment(buyer, seller, price);

        } else if (plugin.getEconomy() != null) {
            return processVaultPayment(buyer, seller, price);
        } 
        else {
            plugin.getLogger().warning("No economy plugin found, players can't buy items");
        }
        
        return false;
    }
    private boolean processRealisticPayment(Player buyer, OfflinePlayer seller, double price) {
        boolean transactionSuccess = false;
        if (plugin.getRealisticEconomy().getEconomyService().getBalance(buyer.getUniqueId()) < price) {
            buyer.sendMessage(plugin.getLang().getFrom("shop.no_money", buyer));
            return false;
        }
        if (seller != null) {
            transactionSuccess = plugin.getRealisticEconomy().getEconomyService().playerPayPlayer(buyer.getUniqueId(), seller.getUniqueId(), price);
            Player player = seller.getPlayer();
            if (transactionSuccess && player.isOnline()) {

                player.sendMessage(plugin.getLang().getFrom("shop.sell", player).replace("{price}", price + "").replace("{player}", buyer.getName()));
            }

        } else {
            transactionSuccess = plugin.getRealisticEconomy().getServerBank().depositFromPlayerToServer(buyer.getUniqueId(), price);
        }
        return transactionSuccess;
    }
    private boolean processVaultPayment(Player buyer,OfflinePlayer seller, double price) {
        if (plugin.getEconomy().getBalance(buyer) < price) {
            buyer.sendMessage(plugin.getLang().getFrom("shop.no_money", buyer));
            return false;
        }
        plugin.getEconomy().withdrawPlayer(buyer, price);
        if (seller != null) {
            plugin.getEconomy().depositPlayer(seller, price);
            Player player = seller.getPlayer();
            if (player != null) {
                player.sendMessage(plugin.getLang().getFrom("shop.sell", player).replace("{price}", price + "").replace("{player}", buyer.getName()));
            }
        }
        return true;
    }
    
}
