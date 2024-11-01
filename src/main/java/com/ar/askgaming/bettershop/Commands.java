package com.ar.askgaming.bettershop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class Commands implements TabExecutor {

    private Main plugin;
    public Commands(Main main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "set_type", "set_price", "test_buy");
        }
        return new ArrayList<>();
    }

    private Set<Material> transparentMaterials = new HashSet<>(Arrays.asList(Material.AIR, Material.WATER));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /shop <reload>");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player p = (Player) sender;

        Block targetBlock = p.getTargetBlock(transparentMaterials, 5);
        switch (args[0].toLowerCase()) {

            case "set_buy":
                handleSetType(p, args, targetBlock);
                break;
            case "test_buy":

                if (plugin.getBlockShopManager().isShop(targetBlock)){
                    Shop shop = plugin.getBlockShopManager().getByBlock(targetBlock);
                    // si tiene dinero
                    if (shop.hasStock()){
                        for (ItemStack i : shop.get(5)) {
                            p.getInventory().addItem(i);
                        }
                        p.sendMessage("items bought!");
                    }
                }
                break;
            case "set_price":
                handleSetPrice(p, args, targetBlock);
                break;
            case "create":
                handleCreateShop(p, args,targetBlock);
                break;
            case "open":
                handleOpenByCmd(p, args);
                break;
            case "remove":
                handleOpenByCmd(p, args);
                break;
            default:
                break;
        }
        return false;
    }
    //#region createShop
    public void handleCreateShop(Player p, String[] args, Block targetBlock){

        if (transparentMaterials.contains(targetBlock.getType())) {
            return;
        }
        if (args.length != 2) {
            p.sendMessage("Usage: /shop create <name>");
            return;
        }

        if (p.getInventory().getItemInMainHand() == null) {
            p.sendMessage("Debe tener un item en la mano!");
            return;
        }

        if (targetBlock.getState() instanceof InventoryHolder) {
            Shop shop = new Shop(targetBlock, p.getInventory().getItemInMainHand(),p, args[1]);
            return;
        }
        p.sendMessage("Debe ser un bloque con inventario!");

    }
    
    //#region SetPrice
    public void handleSetPrice(Player p, String[] args, Block targetBlock){

        if (args.length != 2) {
            p.sendMessage("Usage: /shop set_price <price>");
            return;
        }
        if (plugin.getBlockShopManager().isShop(targetBlock)){
            Shop shop = plugin.getBlockShopManager().getByBlock(targetBlock);
            if (shop.getOnwer().equals(p)){
                try {
                    shop.setPrice(Double.parseDouble(args[1]));
                    p.sendMessage("Price set to " + args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage("Invalid price!");
                }
            }else{
                p.sendMessage("You are not the owner of this shop!");
            }
        }
    }

    //#region setType
    public void handleSetType(Player p, String[] args, Block targetBlock){

        if (args.length != 2) {
            p.sendMessage("Usage: /shop set_type <buy/sell>");
            return;
        }
        if (plugin.getBlockShopManager().isShop(targetBlock)){
            Shop shop = plugin.getBlockShopManager().getByBlock(targetBlock);
            if (shop.getOnwer().equals(p)){
                try {
                    shop.setType(Shop.ShopType.valueOf(args[1].toUpperCase()));
                    p.sendMessage("Type set to " + args[1]);
                } catch (IllegalArgumentException e) {
                    p.sendMessage("Invalid type, Usage: /shop set_type <buy/sell>");
                }
            }else{
                p.sendMessage("You are not the owner of this shop!");
            }
        }
    }

    //#region openByCmd
    public void handleOpenByCmd(Player p, String[] args){

        if (args.length != 2) {
            p.sendMessage("Usage: /shop open <name>");
            return;
        }
        Shop shop = plugin.getBlockShopManager().getByName(args[1]);
        if (shop != null) {
           // shop.getInventory().getViewers().forEach(v -> v.closeInventory());
            p.openInventory(shop.getInventory());
        } else {
            p.sendMessage("Shop not found!");
        }
    }
    //#region removeShop
    public void handleRemoveShop(Player p, String[] args){

        if (args.length != 2) {
            p.sendMessage("Usage: /shop remove <name>");
            return;
        }
        Shop shop = plugin.getBlockShopManager().getByName(args[1]);
        if (shop != null) {
            if (shop.getOnwer().equals(p) || p.hasPermission("shop.admin")) {
                shop.remove();
                p.sendMessage("Shop removed!");
            }else{
                p.sendMessage("You are not the owner of this shop!");
            }
        } else {
            p.sendMessage("Shop not found!");
        }
    }
}
