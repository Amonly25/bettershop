package com.ar.askgaming.bettershop.Auctions;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;

public class Commands implements TabExecutor{

    private BetterShop plugin;
    private AuctionManager manager;
    public Commands(BetterShop main, AuctionManager manager) {
        plugin = main;
        this.manager = manager;
        plugin.getServer().getPluginCommand("auction").setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("create", "bid", "cancel", "claim", "open", "list");
        } else return null;
    }

    private String getLang(String key, Player player) {
        return plugin.getLang().getFrom(key, player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            manager.openInventory(player, 0);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help" -> player.sendMessage(getLang("auction_help", player));
            case "create" -> handleCreate(player, args);
            case "bid" -> handleBet(player, args);
            case "cancel" -> handleCancel(player, args);
            case "end" -> handleEnd(player, args);
            case "claim" -> handleClaim(player, args);
            case "open" -> manager.openInventory(player, 0);
            case "list" -> listAuctions(player);
            default -> player.sendMessage("Usage: /auction <create/bet/cancel/end/claim/open/list>");
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /auction create <price>");
            return;
        }
        double price = parseDouble(args[1], player);
        if (price <= 0) return;
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(getLang("misc.item_in_hand", player));
            return;
        }
        int amountItemsPublished = manager.getPlayerAuctions(player).size();
        // Recorremos desde amountItemsPublished + 1 hasta 1 buscando el permiso más alto que el jugador tenga
        if (player.hasPermission("bettershop.auctions.unlimited")) {
            manager.createAuction(player, price, item);
            return;
        }
        for (int i = 0; i < 100; i++) {
            String permission = "bettershop.auctions." + i;
            if (player.hasPermission(permission)) {
                if (amountItemsPublished < i) {
                    break;
                } else {
                    player.sendMessage(plugin.getLang().getFrom("auction.max", player));
                    return;
                }
            }
        }
        plugin.getShopLogger().log("Player " + player.getName() + " created an auction for " + item.getType().name() + " for " + price);
        manager.createAuction(player, price, item);
    }

    private void handleBet(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("Usage: /auction bet <id> <price>");
            return;
        }
        
        Auction auction = manager.getAuctionByID(args[1]);
        if (auction == null || auction.getOwner().equals(player)) {
            player.sendMessage(getLang("auction.not_found", player));
            return;
        }
        if (auction.isHasEnded()) {
            player.sendMessage(getLang("auction.ended", player));
            return;
        }
        
        double bet = parseDouble(args[2], player);
        if (bet <= auction.getNewPrice() || bet <= auction.getBets().getOrDefault(player.getName(), 0.0)) {
            player.sendMessage(getLang("auction.must_be_higher", player));
            return;
        }
        
        double moneyNeeded = bet - auction.getBets().getOrDefault(player.getName(), 0.0);
        if (plugin.getEconomy() != null && plugin.getEconomy().has(player, moneyNeeded)) {
            plugin.getEconomy().withdrawPlayer(player, moneyNeeded);
            manager.addBet(args[1], player, bet);
            player.sendMessage(getLang("auction.bid", player).replace("{price}", String.valueOf(bet)));
        } else {
            player.sendMessage(getLang("shop.no_money", player));
        }
    }

    private void handleCancel(Player player, String[] args) {
        Auction auction = validateAuction(player, args, "cancel");
        if (auction == null) return;
        
        if (auction.isHasEnded()) {
            player.sendMessage(getLang("auction.cant_cancel", player));
            return;
        }
        
        manager.cancelAuction(auction);
        player.sendMessage(getLang("auction.cancelled", player));
    }

    private void handleEnd(Player player, String[] args) {
        Auction auction = validateAuction(player, args, "end");
        if (auction == null) return;
        
        manager.endAuction(auction);
    }

    private void handleClaim(Player player, String[] args) {
        Auction auction = manager.getAuctionByID(args[1]);
        if (auction == null) {
            player.sendMessage(getLang("auction.not_found", player));
            return;
        }
        plugin.getAuctionManager().processAuctionInventoryClick(auction.getInventory(), player);
    }

    private void listAuctions(Player player) {
        manager.getAuctions().forEach((id, auction) -> 
            player.sendMessage("§aId: " + id + " | Owner: " + auction.getOwner().getName() + " | Price: " + auction.getNewPrice())
        );
    }

    private Auction validateAuction(Player player, String[] args, String action) {
        if (args.length < 2) {
            player.sendMessage("Usage: /auction " + action + " <id>");
            return null;
        }
        
        Auction auction = manager.getAuctionByID(args[1]);
        if (auction == null) {
            player.sendMessage(getLang("auction.not_found", player));
            return null;
        }
        
        if (!auction.getOwner().equals(player)) {
            player.sendMessage(getLang("misc.no_perm", player));
            return null;
        }
        return auction;
    }

    private double parseDouble(String input, Player player) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            player.sendMessage(getLang("commands.invalid_number", player));
            return -1;
        }
    }
}
