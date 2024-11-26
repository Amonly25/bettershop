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

    private BlockShop plugin;
    public Commands(BlockShop main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "set", "sell", "remove");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Arrays.asList("title", "subtitle", "item");
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

            case "create":
                handleCreateShop(p, args, targetBlock);
                break;
            case "set":
                handleSetCommand(p, args, targetBlock);
                break;
            case "sell":
                handleSellItemShop(p, args,targetBlock);
                break;              
            case "open":
                handleOpenByCmd(p, args);
                break;
            case "remove":
                handleRemoveShop(p, args);
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

        if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
            p.sendMessage("Debe tener un item en la mano!");
            return;
        }

        if (!plugin.getBlockShopManager().hasPermissionAtBlockLocation(p, targetBlock)){
            p.sendMessage("No tienes permiso para hacer esto aqui!");
            return;
        }

        for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
            if (shop.getName().equalsIgnoreCase(args[1])) {
                p.sendMessage("Ya hay una tienda con este nombre");
                return;
            }
        }

        if (targetBlock.getState() instanceof InventoryHolder) {
            Shop shop = new Shop(targetBlock, p.getInventory().getItemInMainHand(),p, args[1]);
            return;
        }
        p.sendMessage("Debe ser un bloque con inventario!");

    }

    //#region sellItemShop
    public void handleSellItemShop(Player p, String[] args, Block targetBlock) {
        if (args.length != 2) {
            p.sendMessage("Usage: /shop sell <price_each>");
            return;
        }
        Shop shop = plugin.getBlockShopManager().getByBlock(targetBlock);
        if (shop != null) {
            if (shop.getOnwer().equals(p) || p.hasPermission("shop.admin")) {
                if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                    p.sendMessage("Debe tener un item en la mano!");
                    return;
                }
                try {
                    double d = Double.parseDouble(args[1]);
                    ItemStack i = p.getInventory().getItemInMainHand().clone();
                    if (plugin.getItemShopManager().addItemShop(shop,p.getInventory().getItemInMainHand(), d)){
                        p.sendMessage("Has agregado "+ i.getAmount() + " items a la tienda, a un valor de " + d + " cada uno");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    p.sendMessage(args[1] + " is not a valid number!");
                }
            }else{
                p.sendMessage("You are not the owner of this shop!");
            }
        } else {
            p.sendMessage("Shop not found!");
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

    //#region setCommand
    private void handleSetCommand(Player p, String[] args, Block targetBlock) {

        if (args.length < 3) {
            p.sendMessage("Usage: /shop set <title/subtitle/item>");
            return;
        }
        Shop shop = plugin.getBlockShopManager().getByBlock(targetBlock);

        if (shop == null) {
            p.sendMessage("Shop not found!");
            return;
        }
        if (!shop.getOnwer().equals(p) || !p.hasPermission("shop.admin")) {
            p.sendMessage("You dont have permission to do this!");
            return;
        }

        StringBuilder descBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            descBuilder.append(args[i]).append(" ");
        }
        String text = descBuilder.toString().trim();
        if (text.length() > 32) {
            p.sendMessage("Must be 32 characters or less!");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "item":
                if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                    p.sendMessage("Debe tener un item en la mano!");
                    return;
                }
                ItemStack item = p.getInventory().getItemInMainHand().clone();
                item.setAmount(1);
                shop.setItem(item);
                break;
            case "text":
                p.sendMessage("Has establecido el titulo de la tienda a" + text);
                shop.setText(text);
                break;
 
            case "servershop":
                if (!p.hasPermission("shop.admin")) {
                    p.sendMessage("You dont have permission to set this!");
                    return;
                }
                p.sendMessage("Has establecido la tienda como tienda del servidor");
                shop.setServerShop(true);
                break;                    
            default:
                p.sendMessage("Usage: /shop set <title/subtitle/item>");
                break;
        }
    }
}
