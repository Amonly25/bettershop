package com.ar.askgaming.bettershop.Managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.bettershop.BlockShop.BlockShop;
import com.ar.askgaming.realisticeconomy.Economy.EconomyService;

public class ItemShopTransactions {

    private final BetterShop plugin;
    private final ItemShopManager itemShopManager;

    public ItemShopTransactions(BetterShop plugin) {
        this.plugin = plugin;
        this.itemShopManager = plugin.getItemShopManager();
    }

    public enum ShopType {
        BLOCKSHOP, GLOBAL, SERVER
    }

    //#region cancelItem
    public void cancelShopItem(Player player, ItemStack item, ShopType shop) {
        plugin.getShopLogger().log("Player " + player.getName() + " cancelled item " + item.getType().name() + " from shop " + shop.toString());
        plugin.getItemShopManager().removeItemShopProperties(item);
        player.getInventory().addItem(item.clone());
        item.setAmount(0);
        player.sendMessage(plugin.getLang().getFrom("shop.item_cancelled", player));
    }
    //#region blockshop
    public void processBlockShopPurchase(InventoryClickEvent e, Player player, ItemStack item, BlockShop shop) {
        int amount;
    
        switch (e.getClick()) {
            case RIGHT:
                amount = 1;
                break;
            case SHIFT_RIGHT:
                amount = item.getAmount();
                break;
            default:
                return;
        }
    
        if (shop.isServerShop()) {
            plugin.getItemShopTransactions().buyItemToServer(player, item, amount);
        } else {
            plugin.getItemShopTransactions().buyItemToPlayer(shop.getOnwer(), player, item, amount);
        }
    }

    //#region buyItem
    private void buyItem(Player buyer, ItemStack item, int amount) {
        double price = itemShopManager.getPrice(item) * amount;
        UUID seller = itemShopManager.getSeller(item);

        String sellerName = seller == null ? "Server" : Bukkit.getOfflinePlayer(seller).getName();

        if (!processPayment(buyer, seller, price)) {
            buyer.sendMessage(plugin.getLang().getFrom("misc.error_buying", buyer));
            return;
        }
    
        buyer.sendMessage(plugin.getLang().getFrom("shop.buy", buyer).replace("{amount}", amount+"").replace("{price}", price+""));
        plugin.getShopLogger().log("Player " + buyer.getName() + " bought " + amount + " of " + item.getType().name() + " for " + price + " from " + sellerName);

        ItemStack clonedItem = item.clone();
        itemShopManager.removeItemShopProperties(clonedItem);
    
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
    //#region payment
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
    public void processGlobalShopPurchase(InventoryClickEvent e, Player player, ItemStack item) {
        int amount;
    
        switch (e.getClick()) {
            case RIGHT:
                amount = 1;
                break;
            case SHIFT_RIGHT:
                amount = item.getAmount();
                break;
            default:
                return;
        }
    
        buyItem(player, item, amount);
    }
    public void processServerShopPurchase(InventoryClickEvent e, Player player, ItemStack item) {
        if (e.getClick() != ClickType.LEFT && e.getClick() != ClickType.SHIFT_LEFT) {
            return;
        }
        Map<Material, Double> currentPrices = plugin.getServerShopManager().getCurrentPrices();
        Material itemType = item.getType();
        
        if (!currentPrices.containsKey(itemType)) {
            player.sendMessage("Este ítem no se puede vender en la tienda.");
            return;
        }
        
        double pricePerItem = currentPrices.get(itemType);
        if (pricePerItem <= 0) {
            player.sendMessage("El precio de este ítem es inválido.");
            return;
        }
        
        int requiredAmount = (e.getClick() == ClickType.SHIFT_LEFT) ? Integer.MAX_VALUE : 1;
        List<ItemStack> itemsToRemove = new ArrayList<>();
        int totalAmount = 0;
        
        for (ItemStack i : player.getInventory().getContents()) {
            if (i != null && i.getType() == itemType) {
                int amount = i.getAmount();
                if (totalAmount + amount >= requiredAmount) {
                    itemsToRemove.add(new ItemStack(itemType, requiredAmount - totalAmount));
                    totalAmount = requiredAmount;
                    break;
                } else {
                    itemsToRemove.add(new ItemStack(itemType, amount));
                    totalAmount += amount;
                }
            }
        }
        
        if (totalAmount == 0) {
            player.sendMessage("No tienes suficientes ítems para vender.");
            return;
        }
        
        double totalPayment = totalAmount * pricePerItem;
        
        for (ItemStack toRemove : itemsToRemove) {
            player.getInventory().removeItem(toRemove);
        }
        plugin.getServerShopManager().updateStats(item, totalAmount);
        player.sendMessage("Vendiste " + totalAmount + " de " + itemType + " por " + totalPayment + " monedas.");
        
        // Aquí puedes agregar la lógica para dar el dinero al jugador
    }
    
}
