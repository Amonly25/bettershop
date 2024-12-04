package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.ar.askgaming.bettershop.BlockShop;

public class BlockBreakListener implements Listener{

    private BlockShop plugin;
    public BlockBreakListener(BlockShop main) {
        plugin = main;
    }
    @EventHandler()
    public void onBlockPlace(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (plugin.getBlockShopManager().getByLocation(b.getLocation()) != null){
            e.setCancelled(true);

        }
    } 
}