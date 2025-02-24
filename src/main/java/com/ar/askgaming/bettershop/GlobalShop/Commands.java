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
            case "help":
                player.sendMessage(plugin.getLang().getFrom("globalshop_help", player));
            break;
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
        if (args.length == 1) {
            return List.of("help", "open", "sell");
        }
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
            player.sendMessage(plugin.getLang().getFrom("commands.invalid_number", player));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(plugin.getLang().getFrom("misc.item_in_hand", player));
            return;
        }
        int amountItemsPublished = globalShopManager.getAmountItemsPublished(player);
        if (player.hasPermission("bettershop.globalshop.unlimited")) {
            globalShopManager.sellItem(player, item, price);
            return;
        }
        // Recorremos desde amountItemsPublished + 1 hasta 1 buscando el permiso más alto que el jugador tenga
        for (int i = 0; i < 100; i++) {
            String permission = "bettershop.globalshop." + i;
            if (player.hasPermission(permission)) {
                if (amountItemsPublished < i) {
                    break;
                } else {
                    player.sendMessage(plugin.getLang().getFrom("global_shop.max_items", player));
                    return;
                }
            }
        }
        plugin.getShopLogger().log("Player " + player.getName() + " published on global shop " + item.getType().name() + " for " + price);
        globalShopManager.sellItem(player, item, price);

    }

}
