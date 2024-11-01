package com.ar.askgaming.bettershop.Listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.bettershop.Main;
import com.ar.askgaming.bettershop.Shop;

public class InventoryInteractListener implements Listener{

    private Main plugin;
    public InventoryInteractListener(Main main) {
        plugin = main;
    }
    @EventHandler()
    public void onMoveItem(InventoryInteractEvent e) {

        for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
            Inventory inv = shop.getInventory();

            if (inv == e.getInventory()){

                if (e.getWhoClicked() instanceof Player){
                    Player player = (Player) e.getWhoClicked();
                    if (shop.getOnwer() == player){

                        Bukkit.broadcast("Owner interact in his inventory shop", "admin");
                        
                        //REVISAR ESTO

                        if (e instanceof InventoryClickEvent){
                           ItemStack i = ((InventoryClickEvent)e).getCurrentItem();
                           if (i != null){

                                if (i.getType() != shop.getItemStack().getType()){
                                    Bukkit.broadcast("Item clicked is not the same as the shop item", "admin");
                                    e.setCancelled(true);
                                    break;
                                }

                               Bukkit.broadcast("Item clicked: " + i.getType().toString(), "admin");
                               ItemMeta meta = i.getItemMeta();
                                 if (meta != null){
                                    List<String> list = List.of("Price: " + shop.getPrice(), "Usar: /pshop buy");
                                      meta.setLore(list);
                                }
                            }
                        }
                        shop.updateHolo();
                        break;
                    } else {
                        Bukkit.broadcast("Another player attemp to interact shop inv, event canceled", "admin");
                        e.setCancelled(true);
                        break;

                    }
                } else {
                    Bukkit.broadcast("Another human entity tried to open a shop, wtf?", "admin");
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }
}
