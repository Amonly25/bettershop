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
        creator.sendMessage(plugin.getLang().getFrom("trade.request", target).replace("{player}", target.getName()));
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

        Player creator = trade.getCreator();
        Player target = trade.getTarget();

        if (plugin.getEconomy() == null) {
            creator.sendMessage("Trade failed, economy plugin not found");
            target.sendMessage("Trade failed, economy plugin not found");
            giveItem(trade, trade.getCreator());
            return;
        }

        if (plugin.getEconomy().getBalance(target) < price) {
            creator.sendMessage(plugin.getLang().getFrom("trade.failed_no_money", creator));
            target.sendMessage(plugin.getLang().getFrom("trade.failed_no_money", target));
            giveItem(trade, creator);
            return;
        }
        plugin.getEconomy().withdrawPlayer(target, price);
        plugin.getEconomy().depositPlayer(creator, price);
        creator.sendMessage(plugin.getLang().getFrom("trade.on_receive", creator));
        target.sendMessage(plugin.getLang().getFrom("trade.on_payment", target));
        giveItem(trade, target);
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

        String text = plugin.getLang().getFrom("trade.request_received", target).replace("{player}", sender.getName());

        TextComponent message = new TextComponent(text);

        String click = plugin.getLang().getFrom("trade.click_to_see", target);

        TextComponent clickableText = new TextComponent(click);
      
        clickableText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade see " + sender.getName()));

        clickableText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(click)));

        message.addExtra(clickableText);

        target.spigot().sendMessage(message);

    }

}