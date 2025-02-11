package com.ar.askgaming.bettershop.GlobalShop;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.ar.askgaming.bettershop.BetterShop;

public class Commands implements TabExecutor {

    private BetterShop plugin;
    private GlobalShopManager globalShopManager;
    public Commands(BetterShop plugin, GlobalShopManager globalShopManager) {
        plugin.getCommand("globalshop").setExecutor(this);
        this.plugin = plugin;
        this.globalShopManager = globalShopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

}
