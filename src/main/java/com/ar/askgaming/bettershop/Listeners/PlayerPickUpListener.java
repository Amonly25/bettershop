package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import com.ar.askgaming.bettershop.BlockShop;

public class PlayerPickUpListener implements Listener{

    private BlockShop plugin;
    public PlayerPickUpListener(BlockShop main) {
        plugin = main;
    }
    @EventHandler()
    public void onPickUp(EntityPickupItemEvent e) {
            
        // for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
        //     if (shop.getItem().equals(e.getItem())) {
        //         e.setCancelled(true);
        //     }
        // }
        // if (!e.isCancelled()){
        //     if (plugin.getItemShopManager().isItemShop(e.getItem().getItemStack())) {
        //         e.getItem().getItemStack().setAmount(0);

        //     }
        // }
    }

}
