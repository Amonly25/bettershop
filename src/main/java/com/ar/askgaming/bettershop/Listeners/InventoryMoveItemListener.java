package com.ar.askgaming.bettershop.Listeners;

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


        for (Shop shop : plugin.getShops().values()) {
            Inventory inv = shop.getInventory();

            if (inv != e.getDestination()){
                break;
            }
            if (shop.getItemStack() != e.getItem()){ 
                e.setCancelled(true);

                break;
            }

        }
    }
}
