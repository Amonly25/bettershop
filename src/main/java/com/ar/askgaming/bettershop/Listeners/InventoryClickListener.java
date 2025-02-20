package com.ar.askgaming.bettershop.Listeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.bettershop.Auctions.Auction;
import com.ar.askgaming.bettershop.BlockShop.BlockShop;
import com.ar.askgaming.bettershop.Managers.ItemShopTransactions.ShopType;

public class InventoryClickListener implements Listener{

    private BetterShop plugin;
    public InventoryClickListener(BetterShop main) {
        plugin = main;
    }
    @EventHandler()
    public void onClickInventory(InventoryClickEvent e) {
    
        if (e.getClickedInventory() == null) {
            return;
        }
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        Inventory clickedInventory = e.getClickedInventory();
        Inventory eventInventory = e.getInventory();

        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
    
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        ShopType shopType = null;
        BlockShop shop = null;

        if (plugin.getGlobalShopManager().isGlobalShopInventory(eventInventory) 
            || plugin.getGlobalShopManager().isGlobalShopInventory(clickedInventory)) {
                shopType = ShopType.GLOBAL;

        } else if (plugin.getAuctionManager().isAuctionInventory(eventInventory) || 
            plugin.getAuctionManager().isAuctionInventory(clickedInventory)) {
                e.setCancelled(true);
                return;
        
        }
        else if (plugin.getTradeManager().isTradeInvensory(eventInventory) || 
            plugin.getTradeManager().isTradeInvensory(clickedInventory)) {
                e.setCancelled(true);
                return;
        
        }  else if (plugin.getAuctionManager().isShopInventory(eventInventory) || 
            plugin.getAuctionManager().isShopInventory(clickedInventory)) {
                e.setCancelled(true);
                ItemMeta meta = item.getItemMeta();
                if (!meta.hasLore()) {
                    return;
                }
                List<String> lore = item.getItemMeta().getLore();
                for (String s : lore) {
                    String[] split = s.split(" ");
                    if (split.length < 2) {
                        continue; // Evita errores si la línea no tiene suficientes palabras
                    }
                    
                    if (split[0].contains("id:")) {
                        Auction auction = plugin.getAuctionManager().getAuctionByID(split[1]);
                        if (auction == null) {
                            return;
                        }
                        player.openInventory(auction.getInv());
                    }
                }
                return;
            
        }else if (plugin.getServerShopManager().isShopInventory(eventInventory) || 
            plugin.getServerShopManager().isShopInventory(clickedInventory)) {
                plugin.getItemShopTransactions().processServerShopPurchase(e, player,item);
                e.setCancelled(true);
                return;

        }  else {
    
            for (BlockShop s : plugin.getBlockShopManager().getShops().values()) {
                Inventory shopInventory = s.getInventory();
        
                if (shopInventory.equals(clickedInventory) || shopInventory.equals(eventInventory)) {
                    shopType = ShopType.BLOCKSHOP;
                    shop = s;
                }
            }
        }

        if (shopType == null) {
            return;
        }
        e.setCancelled(true);

        if (!isValidItemShop(item)) {
            return;
        }

        if (isSeller(item, player)) {
            plugin.getItemShopTransactions().cancelShopItem(player, item, shopType);
            if (shopType == ShopType.GLOBAL) {
                plugin.getGlobalShopManager().updateConfig();
            }
            return;
        }

        switch (shopType) {
            case BLOCKSHOP:
                plugin.getItemShopTransactions().processBlockShopPurchase(e, player, item, shop);
                break;
            case GLOBAL:
                plugin.getItemShopTransactions().processGlobalShopPurchase(e, player, item);
                break;
            default:
                break;
        }
    }
    private boolean isSeller(ItemStack item, Player player) {
        return plugin.getItemShopManager().getSeller(item).equals(player.getUniqueId());
    }

    private boolean isValidItemShop(ItemStack item) {
        if (!item.hasItemMeta()) {
            return false;
        }
        return plugin.getItemShopManager().isItemShop(item);
    }
            
}
