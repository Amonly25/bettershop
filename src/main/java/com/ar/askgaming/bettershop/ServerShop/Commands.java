package com.ar.askgaming.bettershop.ServerShop;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;

public class Commands implements TabExecutor {

    private BetterShop plugin;
    private ServerShopManager serverShopManager;
    public Commands(BetterShop plugin, ServerShopManager serverShopManager) {
        plugin.getServer().getPluginCommand("servershop").setExecutor(this);
        this.plugin = plugin;
        this.serverShopManager = serverShopManager;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            serverShopManager.openInventory(player, 0);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                player.sendMessage(plugin.getLang().getFrom("servershop_help", player));
            break;
            case "open":
                serverShopManager.openInventory(player, 0);
                break;
            case "add":
                addItem(player, args);
                break;      
            case "adjust_now":
                if (!player.hasPermission("bettershop.admin")) {
                    player.sendMessage(plugin.getLang().getFrom("commands.no_perm", player));
                    return true;
                }
                serverShopManager.dailyPriceAdjustment();
                player.sendMessage("Prices adjusted!");
                break;
            default:
                break;
        }
        return false;
    }
    private void addItem(Player player, String[] args) {
        if (!player.hasPermission("bettershop.admin")) {
            player.sendMessage(plugin.getLang().getFrom("commands.no_perm", player));
            return;
        }
        if (args.length < 2) {
            player.sendMessage("Usage: /servershop add <price>");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("Usage: /servershop add <price>");
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("You must hold the item in your hand!");
            return;
        }
        Material material = item.getType();

        if (plugin.getServerShopManager().getBasePrices().containsKey(material)) {
            player.sendMessage("Item already exists in the server shop!");
            return;
        }
        serverShopManager.addItem(material, amount);
        player.sendMessage("Item added to the server shop!");

    }
}
