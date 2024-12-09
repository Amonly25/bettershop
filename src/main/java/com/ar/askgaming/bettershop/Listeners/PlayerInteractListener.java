package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.ar.askgaming.bettershop.BlockShop;

public class PlayerInteractListener implements Listener{

    private BlockShop plugin;
    public PlayerInteractListener(BlockShop main) {
        plugin = main;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {

        Block b = e.getClickedBlock();
        if (b == null) {
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (plugin.getBlockShopManager().isShop(b)){
            e.setCancelled(false);
        }
    }
}
