package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BlockShop;
import com.ar.askgaming.bettershop.Shop;

public class InventoryInteractListener implements Listener{

    private BlockShop plugin;
    public InventoryInteractListener(BlockShop main) {
        plugin = main;
    }
    @EventHandler()
    public void onClickInventory(InventoryClickEvent e) {
       
        ItemStack i = e.getCurrentItem();

        if (i == null || i.getType() == Material.AIR) {
            return;
        }

        for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
            Inventory inv = shop.getInventory();

            if (inv.equals(e.getClickedInventory()) || inv.equals(e.getInventory())){
                
                //Is shop! cancel any event
                e.setCancelled(true);

                if (e.getWhoClicked() instanceof Player){

                    Player p = (Player) e.getWhoClicked();

                    if (plugin.getItemShopManager().isItemShop(i)){

                        //Return is player has inventory full
                        if (e.getWhoClicked().getInventory().firstEmpty() == -1){
                            e.getWhoClicked().sendMessage("§cYour inventory is full");
                            return;
                        }

                        // Check is owner and cancel item
                        if (shop.getOnwer().equals(p)){     
                            plugin.getItemShopManager().removeShopProperties(i);
                            p.getInventory().addItem(i.clone());
                            i.setAmount(0);
                            p.sendMessage("Has cancelado un item con exito.");
                            return;
                        }

                        // Process user action to buy item
                        int amount;

                        switch (e.getClick()) {
                            case RIGHT:
                                amount = 1;
                                break;
                            case SHIFT_RIGHT:
                                amount = i.getAmount();
                                break;
                            default:
                                return;
                        }
                        
                        if (shop.isServerShop()) {
                            plugin.getItemShopManager().buyItemToServer(p, i, amount);
                        } else {
                            plugin.getItemShopManager().buyItemToPlayer(shop.getOnwer(), p, i, amount);
                        }
                    }
                }
            }
        }   
    }
}
