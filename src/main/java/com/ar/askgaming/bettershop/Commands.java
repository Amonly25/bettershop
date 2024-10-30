package com.ar.askgaming.bettershop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commands implements TabExecutor {

    private Main plugin;
    public Commands(Main main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload");
        }
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /shop <reload>");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player p = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "drop":
                ItemStack item = new ItemStack(Material.DIAMOND);
                Item dropped = p.getWorld().dropItem(p.getLocation().clone().add(0, -10, 0), item);
                plugin.items.put(dropped, dropped.getLocation());
                dropped.setCustomName("Test");
                dropped.setCustomNameVisible(true);
                dropped.setPickupDelay(0);  
                break;
        
            default:
                break;
        }
        return false;
    }

}
