package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import com.ar.askgaming.bettershop.BlockShop;
import com.ar.askgaming.bettershop.Shop;

import net.md_5.bungee.api.chat.hover.content.Item;

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
