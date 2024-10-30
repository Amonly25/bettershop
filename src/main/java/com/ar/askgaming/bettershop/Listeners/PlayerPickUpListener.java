package com.ar.askgaming.bettershop.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.ar.askgaming.bettershop.Main;

public class PlayerPickUpListener implements Listener{

    private Main main;
    public PlayerPickUpListener(Main main) {
        this.main = main;
    }
    @EventHandler()
    public void onPickUp(EntityPickupItemEvent e) {

        if (main.items.containsKey(e.getItem())) {
            e.setCancelled(true);
        }
    }
    
}
