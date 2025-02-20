package com.ar.askgaming.bettershop.Auctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.bettershop.Managers.VirtualShopManager;

import net.md_5.bungee.api.chat.hover.content.Item;

public class AuctionManager extends VirtualShopManager{

    private BetterShop plugin;

    public AuctionManager(BetterShop main) {
        super(main, "auctions.yml", "Auctions");
        plugin = main;
        new Task(main, this);
        loadAuctions();        

        new Commands(main, this);
    }
    
    private HashMap<String, Auction> auctions = new HashMap<>();

    public HashMap<String, Auction> getAuctions() {
        return auctions;
    }  

    public void createAuction(Player player, double price, ItemStack item) {

        try {
            ItemStack clone = item.clone();
            item.setAmount(0);

            String id = getNewID();
            Auction auction = new Auction(id, player, price, clone);
            auctions.put(id, auction);
            config.set(id+"", auction);
            saveConfig();

            ItemStack clon2 = clone.clone();
            addLore(clon2, auction);
            addItemToInventory(clon2);
            player.sendMessage("Auction created with id: " + auction.getId());
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("Error creating auction.");
        }
        
    }
    

    private String getNewID() {
        Integer id = 0;
        while (auctions.containsKey(String.valueOf(id))) {
            id++;
        }
        return id.toString();
    }
    public Auction getAuctionByID(String id) {
        return auctions.get(id);
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void deleteAuction(Auction auction) {
        String id = auction.getId();
        if (auction.getWinner() == null) {
            processLosers(auction);
            Player player = (Player) auction.getOwner();
            player.sendMessage("Your auction has been cancelled.");
            int slot = player.getInventory().firstEmpty();
            if (slot != -1) {
                player.getInventory().setItem(slot, auction.getItem());
            } else {
                player.getWorld().dropItem(player.getLocation(), auction.getItem());
            }
        }
        auctions.remove(id);
        updateInventory();
        config.set(id.toString(), null);
        saveConfig();
    }
    public void loadAuctions() {
        if (!file.exists()) {
            plugin.saveResource("auctions.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        Set<String> keys = config.getKeys(false);
        if (keys == null) {
            return;
        }

        for (String key : keys) {
            Object obj = config.get(key);
            if (obj instanceof Auction) {
                Auction auction = (Auction) obj;
                
                auctions.put(key, auction);
                auction.setId(key);
            }
                
        }
        updateInventory();        

    }

    public void updateInventory(){
        items.clear();
        for (Auction auction : auctions.values()) {
            ItemStack item = auction.getItem().clone();
            addLore(item, auction);
            items.add(item);
        }
        inventories.clear();
        createInventories();
    }

    private void addLore(ItemStack item, Auction auction) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        meta.setDisplayName("Auction - " + auction.getOwner().getName());
        lore.add("Price: " + auction.getNewPrice());
        lore.add("id: " + auction.getId());
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    public void addBet(String id, Player player, double price) {
        Auction auction = auctions.get(id);
        auction.getBets().put(player.getName(), price);
        config.set(id+"", auction);
        saveConfig();
    }

    public void processWinner(OfflinePlayer buyer, Auction auction) {
        OfflinePlayer seller = auction.getOwner();
        double price = auction.getNewPrice();

        if (buyer.isOnline()) {
            Player player = (Player) buyer;
            player.sendMessage("You won the auction for " + price + "!, claim your item!");
        }
        if (seller.isOnline()) {
            Player player = (Player) seller;
            player.sendMessage("Your auction was won by " + buyer.getName() + " for " + price + "!");
        } 
        if (plugin.getEconomy() != null) {
            plugin.getEconomy().depositPlayer(seller, price);
        }
        
    }
    public boolean isAuctionInventory(Inventory inv) {
        for (Auction auction : auctions.values()) {
            if (auction.getInv().equals(inv)) {
                return true;
            }
        }
        return false;
    }
    public Auction getAuctionByInventory(Inventory inv) {
        for (Auction auction : auctions.values()) {
            if (auction.getInv().equals(inv)) {
                return auction;
            }
        }
        return null;
    }

    private void processLosers(Auction auction) {
        for (String name : auction.getBets().keySet()) {
            if (name.equals(auction.getWinner().getName())) {
                continue;
            }
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
            if (player.isOnline()) {
                Player p = (Player) player;
                p.sendMessage("You lost the auction for " + auction.getNewPrice() + "!");
            }
            if (plugin.getEconomy() != null) {
                plugin.getEconomy().depositPlayer(player, auction.getBets().get(name));
            }
        }
    }

    public void endAction(String id) {
        Auction auction = auctions.get(id);
        auction.setHasEnded(true);
        OfflinePlayer winne = getHighestBet(id);
        if (winne != null) {
            auction.setWinner(winne);
            processWinner(winne, auction);
            processLosers(auction);
        }
        config.set(id+"", auction);
        saveConfig();
    }
    public OfflinePlayer getHighestBet(String id) {
        Auction auction = auctions.get(id);
        double highest = 0;
        OfflinePlayer player = null;
        for (String name : auction.getBets().keySet()) {
            double bet = auction.getBets().get(name);
            if (bet > highest) {
                highest = bet;
                player = plugin.getServer().getOfflinePlayer(name);
            }
        }
        return player;
    }
    
}
