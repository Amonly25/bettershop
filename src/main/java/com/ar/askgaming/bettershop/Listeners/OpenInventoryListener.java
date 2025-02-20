package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.bettershop.Auctions.Auction;

public class OpenInventoryListener implements Listener{

    private final BetterShop plugin;
    public OpenInventoryListener(BetterShop main) {
        plugin = main;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent e) {
        Inventory inventory = e.getInventory();
        Auction auction = plugin.getAuctionManager().getAuctionByInventory(inventory);
        if (auction != null) {
            auction.updateOrCreateItems();
        }
    }

}
