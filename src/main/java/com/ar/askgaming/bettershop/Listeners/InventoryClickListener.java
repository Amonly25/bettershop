package com.ar.askgaming.bettershop.Listeners;

import java.util.HashMap;
import java.util.UUID;

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

    private final BetterShop plugin;
    public InventoryClickListener(BetterShop main) {
        plugin = main;
    }

    private final HashMap<Player, Long> lastClick = new HashMap<>();

    @EventHandler()
    public void onClickInventory(InventoryClickEvent e) {
    
        Inventory topInventory = e.getClickedInventory();
        Inventory bottomInventory = e.getInventory();

        ItemStack item = e.getCurrentItem();

        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getWhoClicked();
        ShopType shopType = getShopType(e, topInventory, bottomInventory, player);
        if (shopType == null) return;

        switch (shopType) {
            case BLOCKSHOP:
                handleShopClick(e, player, item, ShopType.BLOCKSHOP, topInventory);
                break;
            case GLOBAL:
                handleShopClick(e, player, item, ShopType.GLOBAL, topInventory);    
                break;
            case SERVER:
                if (!plugin.getServerShopManager().isShopInventory(topInventory)) {
                    return;
                }
                if (mustCancelClick(player)) {
                    return;
                }
                plugin.getItemShopTransactions().processServerShopPurchase(e, player, item);
                lastClick.put(player, System.currentTimeMillis());
                break;
            case AUCTION:
                plugin.getAuctionManager().processAuctionInventoryClick(topInventory, player);
                lastClick.put(player, System.currentTimeMillis());
                break;
            case AUCTION_MENU:
                handleAuctionClick(e, player, item);
                break;
            case TRADE:
                break;
            default:
                break;
        }
    }
    //#region handleShopClick
    private void handleShopClick(InventoryClickEvent e, Player player, ItemStack item, ShopType shopType, Inventory inv) {

        if (!isValidItemShop(item)) {
            return;
        }
        if (mustCancelClick(player)) {
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
                BlockShop blockShop = plugin.getBlockShopManager().getByInventory(inv);
                if (blockShop == null) {
                    return;
                }

                plugin.getItemShopTransactions().processBlockShopPurchase(e, player, item, blockShop);
                lastClick.put(player, System.currentTimeMillis());
                break;
            case GLOBAL:
                plugin.getItemShopTransactions().processGlobalShopPurchase(e, player, item);
                lastClick.put(player, System.currentTimeMillis());
                break;
            default:
                break;
        }
    }
    //#region handleAuctionClick
    private void handleAuctionClick(InventoryClickEvent e, Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) return; // Verifica si el ítem es válido
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return;
        }
        if (mustCancelClick(player)) {
            return;
        }

        for (String s : meta.getLore()) {
            String[] split = s.split(" ");
            if (split.length < 2 || !split[0].contains("id:")) {
                continue;
            }

            Auction auction = plugin.getAuctionManager().getAuctionByID(split[1]);
            if (auction != null) {
                player.openInventory(auction.getInv());
            }
        }
        lastClick.put(player, System.currentTimeMillis());
    }

    //#region getShopType
    private ShopType getShopType(InventoryClickEvent event, Inventory inv, Inventory inv2, Player clicker) {
        
        if (plugin.getGlobalShopManager().isGlobalShopInventory(inv) || plugin.getGlobalShopManager().isGlobalShopInventory(inv2)) {
            event.setCancelled(true);
            return ShopType.GLOBAL;
        }
        if (plugin.getAuctionManager().isAuctionInventory(inv) || plugin.getAuctionManager().isAuctionInventory(inv2)) {
            event.setCancelled(true);
            return ShopType.AUCTION;
        }
        if (plugin.getAuctionManager().isShopInventory(inv)|| plugin.getAuctionManager().isShopInventory(inv2)) {
            event.setCancelled(true);
            return ShopType.AUCTION_MENU;
        }
        if (plugin.getTradeManager().isTradeInvensory(inv) || plugin.getTradeManager().isTradeInvensory(inv2)) {
            event.setCancelled(true);
            return ShopType.TRADE;
        }

        if (plugin.getServerShopManager().isShopInventory(inv) || plugin.getServerShopManager().isShopInventory(inv2)) {
            event.setCancelled(true);
            return ShopType.SERVER;
        }
        for (BlockShop shop : plugin.getBlockShopManager().getShops().values()) {
            if (shop.getInventory().equals(inv) || shop.getInventory().equals(inv2)) {
                event.setCancelled(true);
                return ShopType.BLOCKSHOP;
            }
        }

        return null;
    }
    //#region misc
    private boolean mustCancelClick(Player player) {
        if (lastClick.containsKey(player)) {
            long last = lastClick.get(player);
            if (System.currentTimeMillis() - last < 1000) {
                return true;
            }
        }
        return false;
    }
    private boolean isSeller(ItemStack item, Player player) {
        UUID seller = plugin.getItemShopManager().getSeller(item);
        return seller != null && seller.equals(player.getUniqueId());
    }

    private boolean isValidItemShop(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        return plugin.getItemShopManager().isItemShop(item);
    }
            
}
