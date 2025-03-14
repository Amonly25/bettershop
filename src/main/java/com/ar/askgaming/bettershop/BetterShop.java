package com.ar.askgaming.bettershop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.bettershop.Auctions.Auction;
import com.ar.askgaming.bettershop.Auctions.AuctionManager;
import com.ar.askgaming.bettershop.BlockShop.BlockShop;
import com.ar.askgaming.bettershop.BlockShop.BlockShopManager;
import com.ar.askgaming.bettershop.GlobalShop.GlobalShopManager;
import com.ar.askgaming.bettershop.Listeners.BlockBreakListener;
import com.ar.askgaming.bettershop.Listeners.InventoryClickListener;
import com.ar.askgaming.bettershop.Listeners.InventoryMoveItemListener;
import com.ar.askgaming.bettershop.Listeners.OpenInventoryListener;
import com.ar.askgaming.bettershop.Listeners.PlayerBlockListener;
import com.ar.askgaming.bettershop.Listeners.PlayerInteractListener;
import com.ar.askgaming.bettershop.Listeners.PlayerQuitListener;
import com.ar.askgaming.bettershop.Managers.ItemShopManager;
import com.ar.askgaming.bettershop.Managers.ItemShopTransactions;
import com.ar.askgaming.bettershop.Managers.LangManager;
import com.ar.askgaming.bettershop.ServerShop.ServerShopManager;
import com.ar.askgaming.bettershop.Trade.Trade;
import com.ar.askgaming.bettershop.Trade.TradeManager;
import com.ar.askgaming.bettershop.Utilities.ShopLogger;
import com.ar.askgaming.realisticeconomy.RealisticEconomy;

import net.milkbowl.vault.economy.Economy;

public class BetterShop extends JavaPlugin {

    private BlockShopManager blockShopManager;
    private ItemShopManager itemShopManager;
    private LangManager langManager;
    private ShopLogger shopLogger;
    private Economy vaultEconomy;
    private RealisticEconomy realisticEconomy;
    private AuctionManager auctionManager;
    private ItemShopTransactions itemShopTransactions;
    private GlobalShopManager globalShopManager;
    private ServerShopManager serverShopManager;
    private TradeManager tradeManager;

    public void onEnable() {
        
        saveDefaultConfig();

        ConfigurationSerialization.registerClass(BlockShop.class,"Shop");
        ConfigurationSerialization.registerClass(Auction.class,"Auction");

        itemShopManager = new ItemShopManager(this);
        itemShopTransactions = new ItemShopTransactions(this);

        blockShopManager = new BlockShopManager(this);
        auctionManager = new AuctionManager(this);
        globalShopManager = new GlobalShopManager(this,"globalshop.yml","GlobalShop");
        serverShopManager = new ServerShopManager(this,"servershop.yml");
        tradeManager = new TradeManager(this);
        
        langManager = new LangManager(this);
        shopLogger = new ShopLogger(this);

        getServer().getPluginManager().registerEvents(new PlayerBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryMoveItemListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);

        new OpenInventoryListener(this);
        new PlayerQuitListener(this);

        //Vault Integration

        if (getServer().getPluginManager().isPluginEnabled("RealisticEconomy")) {
            getLogger().info("RealisticEconomy found!");
            realisticEconomy = (RealisticEconomy) getServer().getPluginManager().getPlugin("RealisticEconomy");
        }
        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                getLogger().info("Non economy plugin found!");
                //getServer().getPluginManager().disablePlugin(this);
            } else {
                vaultEconomy = rsp.getProvider();
                getLogger().info("Vault Economy found!");
            }

        } else {
            getLogger().info("Vault not found!");
            return;
        }
        
    }
    public void onDisable() {
        for (Entity entity : protectedEntities) {
            entity.remove();
        }
        
        for (BlockShop shop : getBlockShopManager().getShops().values()) {
            shop.getItemDisplay().remove();
            shop.getTextDisplay().remove();
            if (shop.getInventory().getViewers()!=null) {
                shop.getInventory().getViewers().forEach(v -> 
                    v.closeInventory()
                );
            }
            if (shop.getInventory().getContents()!=null) {
                for (int i = 0; i < shop.getInventory().getContents().length; i++) {
                    if (shop.getInventory().getItem(i) != null) {
                        getItemShopManager().removeItemShopLore(shop.getInventory().getItem(i));
                    }
                }
            }
        }

        for (Trade trade : getTradeManager().getTrades()) {
            tradeManager.giveItem(trade, trade.getCreator());
            if (trade.getInventory().getViewers()!=null) {
                trade.getInventory().getViewers().forEach(v -> 
                    v.closeInventory()
                );
            }
        }
        for (Auction auction : getAuctionManager().getAuctions().values()) {
            List<HumanEntity> viewers = new ArrayList<>();
            auction.getInventory().getViewers().forEach(v -> 
                    viewers.add(v)
            );
            viewers.forEach(HumanEntity::closeInventory);
            auction.getInventory().clear();
        }
        getAuctionManager().clearInventories();
        getServerShopManager().clearInventories();
        getGlobalShopManager().clearInventories();
    }
    public TradeManager getTradeManager() {
        return tradeManager;
    }
    public ServerShopManager getServerShopManager() {
        return serverShopManager;
    }
    public BlockShopManager getBlockShopManager() {
        return blockShopManager;
    }
    public ItemShopManager getItemShopManager() {
        return itemShopManager;
    }
    public Economy getEconomy() {
        return vaultEconomy;
    }
    public GlobalShopManager getGlobalShopManager() {
        return globalShopManager;
    }
    public ItemShopTransactions getItemShopTransactions() {
        return itemShopTransactions;
    }
    public RealisticEconomy getRealisticEconomy() {
        return realisticEconomy;
    }
    public AuctionManager getAuctionManager() {
        return auctionManager;
    }
    public LangManager getLang() {
        return langManager;
    }
    public ShopLogger getShopLogger() {
        return shopLogger;
    }
    public List<Entity> protectedEntities = new ArrayList<>();

    public static BetterShop getInstance() {
        return BetterShop.getPlugin(BetterShop.class);
    }
    
}