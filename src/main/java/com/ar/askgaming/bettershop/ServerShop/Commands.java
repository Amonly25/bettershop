package com.ar.askgaming.bettershop.ServerShop;

import java.util.List;

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
                
                break;      
            default:
                break;
        }
        return false;
    }
}
