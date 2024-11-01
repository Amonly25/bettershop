package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;

import com.ar.askgaming.bettershop.Main;
import com.ar.askgaming.bettershop.Shop;

public class InventoryMoveItemListener implements Listener {

    private Main plugin;
    public InventoryMoveItemListener(Main main) {
        plugin = main;
    }
    @EventHandler()
    public void onMoveItem(InventoryMoveItemEvent e) {


        for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
            Inventory inv = shop.getInventory();

            if (inv == e.getDestination()){
                Bukkit.broadcast("InventoryMoveItemEvent detect on and shop inventory as destinacion, please check. " + shop.getBlockShop().getLocation().toString(), "bettershop.admin");
                break;
            }
            if (inv == e.getSource()){
                Bukkit.broadcast("InventoryMoveItemEvent detect on and shop inventory as source, please check. " + shop.getBlockShop().getLocation().toString(), "bettershop.admin");
                
                break;
            }
        }
    }
}
