package com.ar.askgaming.bettershop.ServerShop;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

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
            case "open":
                serverShopManager.openInventory(player, 0);
                break;
            case "add":
                addItem(player, args);
                break;      
            case "adjust_now":
                serverShopManager.dailyPriceAdjustment();
                player.sendMessage("Prices adjusted!");
                break;
            default:
                break;
        }
        return false;
    }
    private void addItem(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("Usage: /servershop add <item> <price>");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("Usage: /servershop add <item> <price>");
            return;
        }
        Material material = Material.getMaterial(args[1].toUpperCase());
        if (material == null) {
            player.sendMessage("Invalid material type!");
            return;
        }
        if (plugin.getServerShopManager().getBasePrices().containsKey(material)) {
            player.sendMessage("Item already exists in the server shop!");
            return;
        }
        serverShopManager.addItem(material, amount);
        player.sendMessage("Item added to the server shop!");

    }
}
