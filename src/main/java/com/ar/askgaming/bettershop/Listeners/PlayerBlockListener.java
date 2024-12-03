package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.ar.askgaming.bettershop.BlockShop;
import com.ar.askgaming.bettershop.Shop;

public class PlayerBlockListener implements Listener{

    private BlockShop plugin;
    public PlayerBlockListener(BlockShop main) {
        plugin = main;
    }
    @EventHandler()
    public void onBlockPlace(BlockPlaceEvent e) {

        Block b = e.getBlock();
        Player p = e.getPlayer();

        for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
            if (b.getLocation().getBlockX() == shop.getBlockShop().getLocation().getBlockX() &&
                b.getLocation().getBlockY()-1 == shop.getBlockShop().getLocation().getBlockY() &&
                b.getLocation().getBlockZ() == shop.getBlockShop().getLocation().getBlockZ()) {
                e.setCancelled(true);
                return;
            }

            if (p.isSneaking()){
                return;
            }
            if (shop.getBlockShop().getLocation().distance(b.getLocation() ) > 1){
                continue;
            }
            if (p.getInventory().getItemInMainHand().getType() == shop.getItemStack().getType()){
                p.sendMessage(plugin.getLang().getFrom("misc.cant_use_double_chest", p));
                e.setCancelled(true);
                break;
            }
        }
    }
}
