package com.ar.askgaming.bettershop.Trade;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;

public class Commands implements TabExecutor{

    private TradeManager tradeManager;
    private BetterShop plugin;
    public Commands(BetterShop plugin, TradeManager tradeManager) {
        plugin.getServer().getPluginCommand("trade").setExecutor(this);
        this.tradeManager = tradeManager;
        this.plugin = plugin;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("accept", "see");
        }
        return null;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("Usage: /trade <player>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "accept":
                acceptTrade(player, args);
                break;
            case "test":
                player.sendMessage("Test command");
                break;
            case "see":
                seeTrade(player, args);
                break;
            default:
                processTrade(player, args);
                break;
        }

        return true;
    }
    private void processTrade(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage("Usage: /trade <player> <price>");
            return;
        }

        Player target = canTrade(player, args[0]);
        if (target == null) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("You must hold an item in your hand.");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("Price must be a number.");
            return;
        }
        tradeManager.createTrade(player, target, item.clone(), price);
        item.setAmount(0);
        player.sendMessage("Trade request sent.");
    }
    private void acceptTrade(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage("Usage: /trade accept <player>");
            return;
        }

        Player target = canTrade(player, args[1]);
        if (target == null) {
            return;
        }
        for (Trade trade : tradeManager.getTrades()) {
            if (trade.getTarget() == player && trade.getCreator() == target) {
                // Accept trade
                tradeManager.acceptTrade(trade);
                return;
            }
        }
        player.sendMessage("No trade request found.");
    }
    private void seeTrade(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage("Usage: /trade see <player>");
            return;
        }
        Player target = canTrade(player, args[1]);
        if (target == null) {
            return;
        }

        for (Trade trade : tradeManager.getTrades()) {
            if (trade.getTarget() == player && trade.getCreator() == target) {
                player.openInventory(trade.getInventory());
            }
        }
        player.sendMessage("No trade request found.");
    }
    private Player canTrade(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("Player not found.");
            return null;
        }
        if (target == player) {
            player.sendMessage("You can't trade with yourself.");
            return null;
        }
        if (player.getWorld() != target.getWorld()) {
            player.sendMessage("Player is in a different world.");
            return null;
        }
        if (target.getLocation().distance(player.getLocation()) > 16){
            player.sendMessage("Player is too far away.");
            return null;
        }
        return target;
    }

}
