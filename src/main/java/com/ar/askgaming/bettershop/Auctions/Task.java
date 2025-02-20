package com.ar.askgaming.bettershop.Auctions;

import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import com.ar.askgaming.bettershop.BetterShop;

public class Task extends BukkitRunnable{

    private BetterShop plugin;
    private AuctionManager auctionManager;

    public Task(BetterShop main, AuctionManager auctionManager) {
        plugin = main;
        this.auctionManager = auctionManager;
        runTaskTimer(plugin, 20*60, 20*60);
    }
    @Override
    public void run() {
        for (Auction auction : auctionManager.getAuctions().values()) {
            long time = auction.getTimeLeft();
            if (time <= 0) {
                OfflinePlayer winner = auctionManager.getHighestBet(auction.getId());
                if (winner != null) {
                    auctionManager.processWinner(winner, auction);

                } 
                auctionManager.endAction(auction.getId());
            } else {
                auction.setTimeLeft(time - 1000*60);
                auctionManager.getConfig().set(auction.getId()+"", auction);
                auctionManager.saveConfig();
            }
        }
    }


}
