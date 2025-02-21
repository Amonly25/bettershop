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
            return List.of("create", "bet", "delete", "claim", "open", "list");
        } else return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player p = (Player) sender;

        if (args.length == 0) {
            manager.openInventory(p, 0);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    p.sendMessage("Usage: /auction create <price> ");
                    return true;
                }
                double price;

                try {
                    price = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage("§cPrice must be a number.");
                    return true;
                }

                ItemStack item = p.getInventory().getItemInMainHand();

                if (item == null || item.getType().isAir()) {
                    p.sendMessage("§cYou must hold an item in your hand.");
                    return true;
                }

                if (price <= 0) {
                    p.sendMessage("§cPrice must be greater than 0.");
                    return true;
                }

                manager.createAuction(p, price, item);

                break;
            case "bet":
                if (args.length < 3) {
                    p.sendMessage("Usage: /auction bet <id> <price>");
                    return true;
                }
                String id = args[1];
                double bet;
                try {
                    bet = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage("§cId and price must be numbers.");
                    return true;
                }
                Auction auction = manager.getAuctionByID(id);
                if (auction == null) {
                    p.sendMessage("§cAuction not found.");
                    return true;
                }
                if (bet <= 0) {
                    p.sendMessage("§cPrice must be greater than 0.");
                    return true;
                }
                if (bet < auction.getNewPrice()) {
                    p.sendMessage("§cPrice must be greater than the current price.");
                    return true;
                }
                if (auction.getOwner().equals(p)) {
                    p.sendMessage("§cYou can't bet on your own auction.");
                    return true;
                }
                double playerBet = auction.getBets().getOrDefault(p.getName(), 0.0);
                if (playerBet >= bet) {
                    p.sendMessage("§cYou can't bet less than your current bet.");
                    return true;
                }
                double moneyNeeded = bet - playerBet;
                if (plugin.getEconomy() != null) {
                    if (!plugin.getEconomy().has(p, moneyNeeded)) {
                        p.sendMessage("§cYou don't have enough money.");
                        return true;
                    }
                    plugin.getEconomy().withdrawPlayer(p, moneyNeeded);
                    manager.addBet(id, p, bet);
                    p.sendMessage("§aBet added successfully for " + bet + "!"); 
                }
                break;
            case "delete":
                if (args.length < 2) {
                    p.sendMessage("Usage: /auction delete <id>");
                    return true;
                }

                Auction auction2 = manager.getAuctionByID(args[1]);
                if (auction2 == null) {
                    p.sendMessage("§cAuction not found.");
                    return true;
                }
                if (!auction2.getOwner().equals(p)) {
                    p.sendMessage("§cYou can't delete an auction that is not yours.");
                    return true;
                }
                if (auction2.getWinner() != null) {
                    p.sendMessage("§cYou can't delete an auction that has a winner.");
                    return true;

                }
                manager.deleteAuction(auction2);
                p.sendMessage("§aAuction deleted successfully.");

                break;
            case "end":
                if (args.length < 2) {
                    p.sendMessage("Usage: /auction end <id>");
                    return true;
                }

                Auction auction5 = manager.getAuctionByID(args[1]);
                if (auction5 == null) {
                    p.sendMessage("§cAuction not found.");
                    return true;
                }
                if (!auction5.getOwner().equals(p)) {
                    p.sendMessage("§cYou can't end an auction that is not yours.");
                    return true;
                }
                if (auction5.getWinner() == null) {
                    p.sendMessage("§cYou can't end an auction that has no winner.");
                    return true;
                }
                manager.endAction(auction5);
                p.sendMessage("§aAuction ended successfully.");

                break;
            case "claim":
                if (args.length < 2) {
                    p.sendMessage("Usage: /auction claim <id>");
                    return true;
                }

                Auction auction3 = manager.getAuctionByID(args[1]);
                if (auction3 == null) {
                    p.sendMessage("§cAuction not found.");
                    return true;
                }
                if (!auction3.getWinner().equals(p)) {
                    p.sendMessage("§cYou can't claim an auction that you didn't win.");
                    return true;
                }
                int slot = p.getInventory().firstEmpty();
                if (slot != -1) {
                    p.getInventory().setItem(slot, auction3.getItem());
                } else {
                    p.getWorld().dropItem(p.getLocation(), auction3.getItem());
                }
                p.sendMessage("§aItem claimed successfully.");
                manager.deleteAuction(auction3);
                
                break;
            case "open":
                manager.openInventory(p, 0);
                break;
            case "list":
                manager.getAuctions().forEach((id4, auction4) -> {
                    p.sendMessage("§aId: " + id4 + " | Owner: " + auction4.getOwner().getName() + " | Price: " + auction4.getNewPrice());
                });
                break;

            default:
                p.sendMessage("Usage: /auction <create/bet/delete/end>");
                break;
        }

        return true;
    }

}
