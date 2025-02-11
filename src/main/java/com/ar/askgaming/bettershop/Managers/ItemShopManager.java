package com.ar.askgaming.bettershop.Managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.realisticeconomy.Economy.EconomyService;

public class ItemShopManager{

    private List<String> itemShopLore;
    private String priceText;
    private String sellerText;

    private NamespacedKey itemPrice;
    private NamespacedKey itemSeller;

    private BetterShop plugin;
    public ItemShopManager(BetterShop plugin) {
        this.plugin = plugin;
        itemShopLore = plugin.getConfig().getStringList("item_shop.lore");
        priceText = plugin.getConfig().getString("item_shop.price_text","Price:");
        sellerText = plugin.getConfig().getString("item_shop.seller_text","Seller:");
        itemShopLore.add(0, priceText + "{price}");

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
        data.set(itemSeller, PersistentDataType.STRING, seller);
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
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        Map<String, String> replacements = Map.of(
            "{price}", String.valueOf(price),
            "{seller}", seller
        );

        for (String line : itemShopLore) {
            String formattedLine = line;
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                formattedLine = formattedLine.replace(entry.getKey(), entry.getValue());
            }
            if (!lore.contains(formattedLine)) {
                lore.add(formattedLine);
            }
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
        ItemMeta meta = itemStack.getItemMeta();

        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            Predicate<String> isSimilar = str -> str.startsWith(priceText) || itemShopLore.contains(str);
            Predicate<String> isSeller = str -> str.startsWith(sellerText) || itemShopLore.contains(str);

            // Remove similar elements from list2
             lore.removeIf(isSimilar);
             lore.removeIf(isSeller);
             meta.setLore(lore);
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

    //#region buyItem
    private void buyItem(Player buyer, ItemStack item, int amount) {
        double price = getPrice(item) * amount;
        UUID seller = getSeller(item);

        String sellerName = seller == null ? "Server" : Bukkit.getOfflinePlayer(seller).getName();

        if (!processPayment(buyer, seller, price)) {
            buyer.sendMessage(plugin.getLang().getFrom("misc.error_buying", buyer));
            return;
        }
    
        buyer.sendMessage(plugin.getLang().getFrom("shop.buy", buyer).replace("{amount}", amount+"").replace("{price}", price+""));
        plugin.getShopLogger().log("Player " + buyer.getName() + " bought " + amount + " of " + item.getType().name() + " for " + price + " from " + sellerName);

        ItemStack clonedItem = item.clone();
        removeItemShopProperties(clonedItem);
    
        if (amount == 1) {
            clonedItem.setAmount(1);
            item.setAmount(item.getAmount() - 1);
        } else {
            item.setAmount(0);
        }
    
        buyer.getInventory().addItem(clonedItem);
    }

    public void buyItemToPlayer(OfflinePlayer seller, Player buyer, ItemStack item, int amount) {
        buyItem(buyer, item, amount);
    }
    
    public void buyItemToServer(Player buyer, ItemStack item, int amount) {
        buyItem(buyer, item, amount);
    }
    //#endregion
    private boolean processPayment(Player buyer, UUID seller, double price) {

        if (plugin.getRealisticEconomy()!= null) {
            return processRealisticPayment(buyer, seller, price);

        } else if (plugin.getEconomy() != null) {
            return processVaultPayment(buyer, seller, price);
        } 
        
        plugin.getLogger().warning("No economy plugin found, players can't buy items");        
        return false;
    }
    private boolean processRealisticPayment(Player buyer, UUID seller, double price) {
        EconomyService economy = plugin.getRealisticEconomy().getEconomyService();

        if (economy.getBalance(buyer.getUniqueId()) < price) {
            buyer.sendMessage(plugin.getLang().getFrom("shop.no_money", buyer));
            return false;
        }

        boolean success = seller != null
            ? economy.playerPayPlayer(buyer.getUniqueId(), seller, price)
            : plugin.getRealisticEconomy().getServerBank().depositFromPlayerToServer(buyer.getUniqueId(), price);

        if (success && seller != null) {
            Player sellerPlayer = Bukkit.getPlayer(seller);
            if (sellerPlayer != null) {
                sellerPlayer.sendMessage(plugin.getLang().getFrom("shop.sell", sellerPlayer)
                    .replace("{price}", String.valueOf(price))
                    .replace("{player}", buyer.getName()));
            }
        }

        return success;
    }

    private boolean processVaultPayment(Player buyer, UUID seller, double price) {
        if (plugin.getEconomy().getBalance(buyer) < price) {
            buyer.sendMessage(plugin.getLang().getFrom("shop.no_money", buyer));
            return false;
        }
        plugin.getEconomy().withdrawPlayer(buyer, price);
        if (seller != null) {
            OfflinePlayer sellerPlayer = Bukkit.getOfflinePlayer(seller);
            plugin.getEconomy().depositPlayer(sellerPlayer, price);
            Player player = Bukkit.getPlayer(seller);
            if (player != null) {
                player.sendMessage(plugin.getLang().getFrom("shop.sell", player).replace("{price}", price + "").replace("{player}", buyer.getName()));
            }
        }
        return true;
    }
    
}
