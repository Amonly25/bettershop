package com.ar.askgaming.bettershop.Auctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.bettershop.Managers.VirtualShopManager;

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
    private String getLang(String key, Player player) {
        return plugin.getLang().getFrom(key, player);
    }

    //#region create
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
            for (Player pl: Bukkit.getOnlinePlayers()){
                pl.sendMessage(getLang("auction.created", pl).replace("{player}", player.getName()).replace("{price}", String.valueOf(price)));
            }

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("Error creating auction.");
            plugin.getShopLogger().log("Error creating auction.");
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
    //#region delete
    public void deleteAuction(Auction auction, Player claimer) {
        String id = auction.getId();
        int slot = claimer.getInventory().firstEmpty();
        if (slot != -1) {
            claimer.getInventory().setItem(slot, auction.getItem());
        } else {
            claimer.getWorld().dropItem(claimer.getLocation(), auction.getItem());
        }
        plugin.getShopLogger().log("Player " + claimer.getName() + " claimed item " + auction.getItem().getType().name() + " from auction " + id);
        auction.getInventory().clear();
        List<Player> viewers = new ArrayList<>();
        auction.getInventory().getViewers().forEach(p -> viewers.add((Player) p));
        viewers.forEach(p -> p.closeInventory());

        auctions.remove(id);
        updateInventory();
        config.set(id.toString(), null);
        saveConfig();
    }
  
    //#region load
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
                auction.createInventoryAndItem();
            }
                
        }
        updateInventory();        
    }

    //#region update
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

    //#region add
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
        if (auction.getNewPrice() < price) {
            auction.setNewPrice(price);
        }
        config.set(id+"", auction);
        saveConfig();
    }

    //#region process
    public void processWinner(OfflinePlayer buyer, Auction auction) {
        OfflinePlayer seller = auction.getOwner();
        double price = auction.getNewPrice();

        for (Player pl: Bukkit.getOnlinePlayers()){
            pl.sendMessage(getLang("auction.won", pl).replace("{player}", buyer.getName()).replace("{price}", String.valueOf(price)).replace("{seller}", seller.getName()));
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
    //#region end auction
    private void processLosers(Auction auction) {
        for (String name : auction.getBets().keySet()) {

            if (name.equals(auction.getWinner().getName())) {
                continue;
            }

            OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
            if (player.isOnline()) {
                Player p = (Player) player;
                p.sendMessage(getLang("auction.lost", p));
            }
            if (plugin.getEconomy() != null) {
                plugin.getEconomy().depositPlayer(player, auction.getBets().get(name));
            }
        }
    }

    //#region end
    public void endAuction(Auction auction) {
        
        auction.setHasEnded(true);
        OfflinePlayer winne = getHighestBet(auction.getId());
        if (winne != null) {
            auction.setWinner(winne);
            processWinner(winne, auction);
            processLosers(auction);
            plugin.getLogger().info("Auction " + auction.getId() + " ended with winner " + winne.getName());
        }else{
            Player player = auction.getOwner().getPlayer();
            if (player != null) {
                player.sendMessage(getLang("auction.end_no_winner", player));
                plugin.getLogger().info("Auction " + auction.getId() + " ended with no winner");
            }
        }
        
        config.set(auction.getId()+"", auction);
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
    public void processAuctionInventoryClick(Inventory inv, Player player) {
        Auction auction = getAuctionByInventory(inv);
        if (auction == null) {
            return;
        }
        if (!auction.isHasEnded()) {
            player.sendMessage(getLang("auction.not_ended", player));
            return;
        }
        if (auction.getWinner() == null && auction.getOwner().equals(player)) {
            deleteAuction(auction, player);
            return;
        } else if (auction.getWinner() != null && auction.getWinner().equals(player)) {
            deleteAuction(auction, player);
        }
    }
    //#region cancel
    public void cancelAuction(Auction auction2) {
        processLosers(auction2);
        auction2.setHasEnded(true);
        config.set(auction2.getId()+"", auction2);
        saveConfig();
        Player player = (Player) auction2.getOwner();
        player.sendMessage(plugin.getLang().getFrom("auction.claim", player));
        plugin.getShopLogger().log("Auction " + auction2.getId() + " was cancelled by " + player.getName());
    }
    public List<String> getPlayerAuctions(Player player) {
        List<String> auctions = new ArrayList<>();
        for (Auction auction : this.auctions.values()) {
            if (auction.getOwner().equals(player)) {
                auctions.add(auction.getId());
            }
        }
        return auctions;
    }
    
}
