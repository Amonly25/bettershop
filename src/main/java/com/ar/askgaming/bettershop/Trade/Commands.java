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

    private final TradeManager tradeManager;
    private final BetterShop plugin;
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
    private String getLang(String key, Player player) {
        return plugin.getLang().getFrom(key, player);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return false;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("Usage: /trade <player>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help" -> player.sendMessage(getLang("trade_help", player));
            case "accept" -> acceptTrade(player, args);
            case "see" -> seeTrade(player, args);
            default -> processTrade(player, args);
        }

        return true;
    }
    //#region processTrade
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
            player.sendMessage(getLang("misc.item_in_hand", player));
            return;
        }
        double price;
        try {
            price = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(getLang("commands.invalid_number", player));
            return;
        }
        tradeManager.createTrade(player, target, item.clone(), price);
        item.setAmount(0);
    }
    //#region acceptTrade
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
        player.sendMessage(getLang("trade.not_found", player));
    }
    //#region seeTrade
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
                return;
            }
        }
        player.sendMessage(getLang("trade.not_found", player));
    }
    //#region canTrade
    private Player canTrade(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(getLang("trade.not_found", player));
            return null;
        }
        if (target.equals(player)) {
            player.sendMessage(getLang("misc.not_to_yourself", player));
            return null;
        }
        if (player.getWorld() != target.getWorld()) {
            player.sendMessage(getLang("trade.too_far", player));
            return null;
        }
        if (target.getLocation().distance(player.getLocation()) > plugin.getConfig().getInt("trade.max_distance",16)){
            player.sendMessage(getLang("trade.too_far", player));
            return null;
        }
        return target;
    }

}
