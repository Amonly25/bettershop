package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BlockShop;
import com.ar.askgaming.bettershop.Shop;

public class InventoryInteractListener implements Listener{

    private BlockShop plugin;
    public InventoryInteractListener(BlockShop main) {
        plugin = main;
    }
    @EventHandler()
    public void onClickInventory(InventoryClickEvent e) {
    
        Inventory clickedInventory = e.getClickedInventory();
        Inventory eventInventory = e.getInventory();
    
        for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
            Inventory shopInventory = shop.getInventory();
    
            if (shopInventory.equals(clickedInventory) || shopInventory.equals(eventInventory)) {
                e.setCancelled(true);

                ItemStack item = e.getCurrentItem();
    
                if (item == null || item.getType() == Material.AIR) {
                    return;
                }
                
                handleShopInteraction(e, shop, item);
                return;
            }
        }
    }
    
    private void handleShopInteraction(InventoryClickEvent e, Shop shop, ItemStack item) {
    
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
    
        Player player = (Player) e.getWhoClicked();
    
        if (!plugin.getItemShopManager().isItemShop(item)) {
            return;
        }
    
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(plugin.getLang().getFrom("misc.no_space", player));
            return;
        }
    
        if (shop.getOnwer().equals(player)) {
            cancelShopItem(player, item, shop);
        } else {
            processPurchase(e, player, item, shop);
        }
    }
    
    private void cancelShopItem(Player player, ItemStack item, Shop shop) {
        plugin.getItemShopManager().removeShopProperties(item);
        player.getInventory().addItem(item.clone());
        item.setAmount(0);
        player.sendMessage(plugin.getLang().getFrom("shop.item_cancelled", player));
    }
    
    private void processPurchase(InventoryClickEvent e, Player player, ItemStack item, Shop shop) {
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
            plugin.getItemShopManager().buyItemToServer(player, item, amount);
        } else {
            plugin.getItemShopManager().buyItemToPlayer(shop.getOnwer(), player, item, amount);
        }
    }
}
