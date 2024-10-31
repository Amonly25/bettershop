package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.ar.askgaming.bettershop.Main;
import com.ar.askgaming.bettershop.Shop;

public class PlayerBlockListener implements Listener{

    private Main plugin;
    public PlayerBlockListener(Main main) {
        plugin = main;
    }
    @EventHandler()
    public void onBlockPlace(BlockPlaceEvent e) {

        for (Shop shop : plugin.getShops().values()) {
            if (e.getBlock().getLocation().getBlockX() == shop.getBlockShop().getLocation().getBlockX() &&
                e.getBlock().getLocation().getBlockY()-1 == shop.getBlockShop().getLocation().getBlockY() &&
                e.getBlock().getLocation().getBlockZ() == shop.getBlockShop().getLocation().getBlockZ()) {
                e.setCancelled(true);
            }
        }

    }
    
}
