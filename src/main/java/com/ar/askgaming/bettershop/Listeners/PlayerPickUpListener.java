package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.bettershop.Shop;

public class PlayerPickUpListener implements Listener{

    private BetterShop plugin;
    public PlayerPickUpListener(BetterShop main) {
        plugin = main;
    }
    @EventHandler()
    public void onPickUp(EntityPickupItemEvent e) {
    
        for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
            if (shop.getItem().equals(e.getItem())) {
                e.setCancelled(true);
                break;
            }
        }
    }

}
