package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.ar.askgaming.bettershop.Main;
import com.ar.askgaming.bettershop.Shop;

public class BlockBreakListener implements Listener{

    private Main plugin;
    public BlockBreakListener(Main main) {
        plugin = main;
    }
    @EventHandler()
    public void onBlockPlace(BlockBreakEvent e) {

        for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
            if (e.getBlock().equals(shop.getBlockShop())) {
                e.setCancelled(true);

            }
        }
    } 
}