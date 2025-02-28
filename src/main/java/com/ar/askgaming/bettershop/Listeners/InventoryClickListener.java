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
    
        Inventory clicked = e.getClickedInventory();
        Inventory upper = e.getInventory();

        ItemStack item = e.getCurrentItem();

        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getWhoClicked();
        ShopType shopType = getShopType(e, upper, clicked, player);
        if (shopType == null) return;

        switch (shopType) {
            case BLOCKSHOP:
                handleShopClick(e, player, item, ShopType.BLOCKSHOP, upper);
                break;
            case GLOBAL:
                handleShopClick(e, player, item, ShopType.GLOBAL, upper);    
                break;
            case SERVER:

                if (mustCancelClick(player)) {
                    return;
                }
                plugin.getItemShopTransactions().processServerShopPurchase(e, player, item);
                lastClick.put(player, System.currentTimeMillis());
                break;
            case AUCTION:
                plugin.getAuctionManager().processAuctionInventoryClick(upper, player);
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
    private ShopType getShopType(InventoryClickEvent event, Inventory upper, Inventory clicked, Player clicker) {
        
        if (plugin.getGlobalShopManager().isShopInventory(upper)) {
            event.setCancelled(true);
            if (plugin.getGlobalShopManager().isGlobalShopInventory(clicked)) {
                return ShopType.GLOBAL;
            }
            return null;
        }
        if (plugin.getAuctionManager().isAuctionInventory(upper)) {
            event.setCancelled(true);
            if (plugin.getAuctionManager().isAuctionInventory(clicked)) {
                return ShopType.AUCTION;
            }
            return null;
        }
        if (plugin.getAuctionManager().isShopInventory(upper)) {
            event.setCancelled(true);
            if (plugin.getAuctionManager().isShopInventory(clicked)) {
                return ShopType.AUCTION_MENU;
            }
            return null;
        }
        if (plugin.getTradeManager().isTradeInvensory(upper)) {
            event.setCancelled(true);
            if (plugin.getTradeManager().isTradeInvensory(clicked)) {
                return ShopType.TRADE;
            }
            return null;
        }

        if (plugin.getServerShopManager().isShopInventory(upper)) {
            event.setCancelled(true);
            if (plugin.getServerShopManager().isShopInventory(clicked)) {
                return ShopType.SERVER;
            }
            return null;
        }
        for (BlockShop shop : plugin.getBlockShopManager().getShops().values()) {
            if (shop.getInventory().equals(upper)) {
                event.setCancelled(true);
                if (shop.getInventory().equals(clicked)) {
                    return ShopType.BLOCKSHOP;
                }
                return null;
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
