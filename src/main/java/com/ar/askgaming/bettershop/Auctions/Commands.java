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

    private String getLang(String key, Player player) {
        return plugin.getLang().getFrom(key, player);
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
                    p.sendMessage(getLang("commands.invalid_number", p));
                    return true;
                }

                ItemStack item = p.getInventory().getItemInMainHand();

                if (item == null || item.getType().isAir()) {
                    p.sendMessage(getLang("misc.item_in_hand", p));
                    return true;
                }

                if (price <= 0) {
                    p.sendMessage(getLang("commands.invalid_number", p));
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
                    p.sendMessage(getLang("commands.invalid_number", p));
                    return true;
                }
                Auction auction = manager.getAuctionByID(id);
                if (auction == null) {
                    p.sendMessage(getLang("auction.not_found", p));
                    return true;
                }
                if (bet <= 0) {
                    p.sendMessage(getLang("commands.invalid_number", p));
                    return true;
                }
                if (bet < auction.getNewPrice()) {
                    p.sendMessage(getLang("auction.must_be_higher", p));
                    return true;
                }
                if (auction.getOwner().equals(p)) {
                    p.sendMessage(getLang("misc.not_to_yourself", p));
                    return true;
                }
                double playerBet = auction.getBets().getOrDefault(p.getName(), 0.0);
                if (playerBet >= bet) {
                    p.sendMessage(getLang("auction.must_be_higher", p));
                    return true;
                }
                double moneyNeeded = bet - playerBet;
                if (plugin.getEconomy() != null) {
                    if (!plugin.getEconomy().has(p, moneyNeeded)) {
                        p.sendMessage(getLang("shop.no_money", p));
                        return true;
                    }
                    plugin.getEconomy().withdrawPlayer(p, moneyNeeded);
                    manager.addBet(id, p, bet);
                    p.sendMessage(getLang("auction.bet", p).replace("{price}", bet + "")); 
                }
                break;
            case "delete":
                if (args.length < 2) {
                    p.sendMessage("Usage: /auction delete <id>");
                    return true;
                }

                Auction auction2 = manager.getAuctionByID(args[1]);
                if (auction2 == null) {
                    p.sendMessage(getLang("auction.not_found", p));
                    return true;
                }
                if (!auction2.getOwner().equals(p)) {
                    p.sendMessage(getLang("misc.no_perm:", p));
                    return true;
                }
                if (auction2.getWinner() != null) {
                    p.sendMessage(getLang("auction.cant_delete", p));
                    return true;

                }
                manager.deleteAuction(auction2);
                p.sendMessage(getLang("auction.deleted", p));

                break;
            case "end":
                if (args.length < 2) {
                    p.sendMessage("Usage: /auction end <id>");
                    return true;
                }

                Auction auction5 = manager.getAuctionByID(args[1]);
                if (auction5 == null) {
                    p.sendMessage(getLang("auction.not_found", p));
                    return true;
                }
                if (!auction5.getOwner().equals(p)) {
                    p.sendMessage(getLang("commands.no_perm", p));
                    return true;
                }

                manager.endAction(auction5);

                break;
            case "claim":
                if (args.length < 2) {
                    p.sendMessage("Usage: /auction claim <id>");
                    return true;
                }

                Auction auction3 = manager.getAuctionByID(args[1]);
                if (auction3 == null) {
                    p.sendMessage(getLang("auction.not_found", p));
                    return true;
                }
                if (!auction3.getWinner().equals(p)) {
                    p.sendMessage(getLang("commands.no_perm", p));
                    return true;
                }
                int slot = p.getInventory().firstEmpty();
                if (slot != -1) {
                    p.getInventory().setItem(slot, auction3.getItem());
                } else {
                    p.getWorld().dropItem(p.getLocation(), auction3.getItem());
                }
                p.sendMessage(getLang("auction.claimed", p));
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
