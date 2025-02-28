package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.ar.askgaming.bettershop.BetterShop;

public class PlayerInteractListener implements Listener{

    private BetterShop plugin;
    public PlayerInteractListener(BetterShop main) {
        plugin = main;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {

        Block b = e.getClickedBlock();
        if (b == null) {
            return;
        }
        if (e.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (plugin.getBlockShopManager().getByBlock(b) != null) {
            e.setCancelled(false);
        }
    }
}
