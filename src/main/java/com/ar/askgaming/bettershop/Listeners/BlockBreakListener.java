package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.bettershop.BlockShop.BlockShop;

public class BlockBreakListener implements Listener{

    private BetterShop plugin;
    public BlockBreakListener(BetterShop main) {
        plugin = main;
    }
    @EventHandler()
    public void onBlockPlace(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        BlockShop shop = plugin.getBlockShopManager().getByBlock(b);
        if (shop != null){
            if (plugin.getBlockShopManager().hasAdminPermission(p, shop)){
                boolean remove = plugin.getBlockShopManager().remove(shop);
                if (remove){
                    p.sendMessage(plugin.getLang().getFrom("blockshop.remove", p));
                } else {
                    p.sendMessage("Error removing blockshop, please check console.");
                }
                return;
            }
            e.setCancelled(true);

        }
    } 
}