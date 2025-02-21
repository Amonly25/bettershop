package com.ar.askgaming.bettershop.Trade;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

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
        creator.sendMessage("Trade request sent to " + target.getName() + " with item for $" + price);
        sendMessage(creator, target);
      
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

    public void acceptTrade(Trade trade) {

        double price = trade.getPrice();

        if (plugin.getEconomy() == null) {
            trade.getCreator().sendMessage("Trade failed, economy plugin not found");
            trade.getTarget().sendMessage("Trade failed, economy plugin not found");
            giveItem(trade, trade.getCreator());
            return;
        }

        if (plugin.getEconomy().getBalance(trade.getTarget()) < price) {
            trade.getCreator().sendMessage("Trade failed, " + trade.getTarget().getName() + " does not have enough money");
            trade.getTarget().sendMessage("Trade failed, you do not have enough money");
            giveItem(trade, trade.getCreator());
            return;
        }
        plugin.getEconomy().withdrawPlayer(trade.getTarget(), price);
        plugin.getEconomy().depositPlayer(trade.getCreator(), price);
        trade.getCreator().sendMessage("Trade successful, you have received $" + price);
        trade.getTarget().sendMessage("Trade successful, you have paid $" + price);
        giveItem(trade, trade.getTarget());
        trades.remove(trade);

    }
    public void giveItem(Trade trade, Player player) {

        int slot = player.getInventory().firstEmpty();
        if (slot == -1) {
            player.getWorld().dropItem(player.getLocation(), trade.getItem());
        } else {
            player.getInventory().setItem(slot, trade.getItem());
        }
    }


    private void sendMessage(Player sender, Player target){

        String text = "You have received a trade request from " + sender.getName();

        TextComponent message = new TextComponent(text);

        String click = "Click here to see the item";

        TextComponent clickableText = new TextComponent(click);
      
        clickableText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade see " + sender.getName()));

        clickableText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(click)));

        message.addExtra(clickableText);

        target.spigot().sendMessage(message);

    }

}