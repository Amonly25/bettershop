package com.ar.askgaming.bettershop.Auctions;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BlockShop;

public class Commands implements TabExecutor{

    private BlockShop plugin;
    public Commands(BlockShop main) {
        plugin = main;

        plugin.getServer().getPluginCommand("auction").setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("info", "create", "bet", "set_item");
        } else return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /auction <info/create/bet/set_item>");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player p = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 3) {
                    p.sendMessage("Usage: /auction create <price> <time_minutes>");
                    return true;
                }
                double price;
                int time;

                try {
                    price = Double.parseDouble(args[1]);
                    time = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage("Usage: /auction create <price> <time_minutes>");
                    return true;
                }

                price = Math.round(price * 100.0) / 100.0;

                ItemStack item = p.getInventory().getItemInMainHand();

                if (item == null || item.getType().isAir()) {
                    p.sendMessage("§cYou must hold an item in your hand.");
                    return true;
                }

                if (price <= 0) {
                    p.sendMessage("§cPrice must be greater than 0.");
                    return true;
                }

                if (time <= 0) {
                    p.sendMessage("§cTime must be greater than 0.");
                    return true;
                }

                plugin.getAuctionManager().createAuction(p, price, time, item);

                break;
            case "info":
                    
                break;
            case "bet":
                    
                break;
            case "set_item":
                        
                break;

            default:
                p.sendMessage("Usage: /auction <info/create/bet/set_item>");
                break;
        }

        return true;
    }
}
