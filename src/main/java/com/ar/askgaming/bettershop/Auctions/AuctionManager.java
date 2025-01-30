package com.ar.askgaming.bettershop.Auctions;

import java.util.HashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BlockShop;
import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class AuctionManager {

    private BlockShop plugin;

    public AuctionManager(BlockShop main) {
        plugin = main;


        // Load auctions from file
    }
    
    private HashMap<OfflinePlayer, Auction> auctions = new HashMap<>();

    public HashMap<OfflinePlayer, Auction> getAuctions() {
        return auctions;
    }  

    public void createAuction(Player player, double price, int time, ItemStack item) {

        if (auctions.containsKey(player)) {
            player.sendMessage("You already have an auction.");
            return;

        }

        ItemStack clone = item.clone();
        item.setAmount(0);

        Auction auction = new Auction(player, price, time, clone);
        auctions.put(player, auction);

    }
}
