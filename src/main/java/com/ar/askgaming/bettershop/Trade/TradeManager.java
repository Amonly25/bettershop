package com.ar.askgaming.bettershop.Trade;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;

public class TradeManager {

    private BetterShop plugin;
    public TradeManager(BetterShop main) {
        plugin = main;

        new Commands(main, this);
    }

    public void createTrade(Player creator, Player target, ItemStack item, double price) {
        // Create trade
        Trade trade = new Trade(creator, target, item, price);
        trades.add(trade);
        target.sendMessage("You have received a trade request from " + creator.getName() + " with item for $" + price);
        creator.sendMessage("Trade request sent to " + target.getName() + " with item for $" + price);
      
    }

    private List<Trade> trades = new ArrayList<Trade>();
    public List<Trade> getTrades() {
        return trades;
    }
    public boolean isTradeInvensory(Inventory inventory) {
        for (Trade trade : trades) {
            if (trade.getInventory().equals(inventory)) {
                return true;
            }
        }
        return false;
    }

}