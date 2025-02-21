package com.ar.askgaming.bettershop.Listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.bettershop.Trade.Trade;

public class PlayerQuitListener implements Listener{

    private BetterShop plugin;
    public PlayerQuitListener(BetterShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        List<Trade> toRemove = new ArrayList<>();
        for (Trade trade : plugin.getTradeManager().getTrades()) {
            if (trade.getCreator().equals(player) || trade.getTarget().equals(player)) {
                plugin.getTradeManager().giveItem(trade, player);
                toRemove.add(trade);
            }
        }
        plugin.getTradeManager().getTrades().removeAll(toRemove);
    }
}
