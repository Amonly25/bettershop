package com.ar.askgaming.bettershop;

import org.bukkit.block.Block;

public class BlockShopManager {

    private Main plugin;
    public BlockShopManager(Main main) {
        plugin = main;
    }

    public boolean isShop(Block block){

        for (Shop shop : plugin.getShops().values()) {
            if(shop.getBlockShop().equals(block)){
                return true;
            }
        }

        return false;
    }
    public Shop getByBlock(Block block){
        for (Shop shop : plugin.getShops().values()) {
            if(shop.getBlockShop().equals(block)){
                return shop;
            }
        }
        return null;
    }
}
