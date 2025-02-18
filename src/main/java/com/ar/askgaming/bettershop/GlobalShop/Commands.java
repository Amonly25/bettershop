package com.ar.askgaming.bettershop.GlobalShop;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;

public class Commands implements TabExecutor {

    private BetterShop plugin;
    private GlobalShopManager globalShopManager;
    public Commands(BetterShop plugin, GlobalShopManager globalShopManager) {
        plugin.getServer().getPluginCommand("globalshop").setExecutor(this);
        this.plugin = plugin;
        this.globalShopManager = globalShopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            globalShopManager.openInventory(player,0);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open":
                globalShopManager.openInventory(player,0);
                return true;

            case "sell":
                processSellCommand(player,args);
                return true;
        
            default:
                break;
        }
        return false;


    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    private void processSellCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /globalshop sell <price>");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(args[1]);
        } catch (Exception e) {
            player.sendMessage("§cInvalid price.");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cYou must hold an item in your hand.");
            return;
        }
        int amountItemsPublished = globalShopManager.getAmountItemsPublished(player);
        String permission = "bettershop.globalshop."+amountItemsPublished+1;
        boolean hasPermission = false;

        for (int i = 0; i < 100; i++) {
            hasPermission = player.hasPermission(permission);
            if (hasPermission) {
                break;
            }
        }
        if (!hasPermission) {
            player.sendMessage("§cYou have reached the maximum number of items you can sell.");
            return;
        }

        globalShopManager.sellItem(player, item, price);

    }

}
