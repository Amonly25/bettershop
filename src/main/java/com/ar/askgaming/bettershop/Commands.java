package com.ar.askgaming.bettershop;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
            return Arrays.asList("create", "set", "sell", "remove","open","list","help");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Arrays.asList("text", "item");
        }
        return null;
    }

    private Set<Material> transparentMaterials = new HashSet<>(Arrays.asList(Material.AIR, Material.WATER));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /shop <create/set/sell/remove/list/help>");
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
            case "list":
                handleList(p, args);
                break;
            case "help":
                help(p,args);
                break;
            default:
                break;
        }
        return false;
    }
    private String getLang(String path,Player p){
        return plugin.getLang().getFrom(path,p);
    }
    //#region createShop
    public void handleCreateShop(Player p, String[] args, Block targetBlock){

        ItemStack item = p.getInventory().getItemInMainHand();

        if (transparentMaterials.contains(targetBlock.getType())) {
            return;
        }
        if (args.length != 2) {
            p.sendMessage("Usage: /shop create <name>");
            return;
        }

        if (item.getType() == Material.AIR) {
            p.sendMessage(getLang("misc.item_in_hand", p));
            return;
        }

        if (!plugin.getBlockShopManager().hasPermissionAtBlockLocation(p, targetBlock)){
            p.sendMessage(getLang("commands.no_perm", p));
            return;
        }

        for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
            if (shop.getName().equalsIgnoreCase(args[1])) {
                p.sendMessage(getLang("shop.exits", p));
                return;
            }
        }

        if (targetBlock.getState() instanceof InventoryHolder) {
            plugin.getBlockShopManager().createShop(targetBlock,item, p, args[1]);
            return;
        }
        p.sendMessage(getLang("shop.must_be_container", p));

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
                    p.sendMessage(getLang("misc.item_in_hand", p));
                    return;
                }
                try {
                    double d = Double.parseDouble(args[1]);
                    ItemStack i = p.getInventory().getItemInMainHand().clone();
                    if (plugin.getItemShopManager().addItemShop(shop,p.getInventory().getItemInMainHand(), d)){
                        p.sendMessage(getLang("shop.add_item", p).replace("{amount}", i.getAmount()+"").replace("{price}", d+""));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    p.sendMessage(plugin.getLang().getFrom("commands.invalid_amount",p));
                }
            }else{
                p.sendMessage(getLang("commands.no_perm", p));
            }
        } else {
            p.sendMessage(getLang("shop.not_found", p));
        }
    }
    //#region list
    public void handleList(Player p, String[] args) {

        if (plugin.getBlockShopManager().getShops().isEmpty()) {
            p.sendMessage(getLang("shop.not_found", p));
            return;
        }
        if (args.length == 1) {
            p.sendMessage("Shops:");
            for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
                p.sendMessage(shop.getName());
            }

        } 
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("server")) {
                p.sendMessage("Server Shops:");
                for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
                    if (shop.isServerShop()) {
                        p.sendMessage(shop.getName());
                    }
                }
                return;
            } 
            @SuppressWarnings("deprecation")
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[1]);
            if (player != null) {
                p.sendMessage("Shops of " + player.getName());
                for (Shop shop : plugin.getBlockShopManager().getShops().values()) {
                    if (shop.getOnwer().equals(player)) {
                        p.sendMessage(shop.getName());
                    }
                }
            } else {
                p.sendMessage("Player not found!");
            }
        }
        p.sendMessage("Usage: /shop list");

    }
    
    //#region openByCmd
    public void handleOpenByCmd(Player p, String[] args){

        if (args.length != 2) {
            p.sendMessage("Usage: /shop open <name>");
            return;
        }
        if (!p.hasPermission("blockshop.opencommand")) {
            p.sendMessage(getLang("commands.no_perm", p));
            return;
        }
        Shop shop = plugin.getBlockShopManager().getByName(args[1]);
        if (shop != null) {
           // shop.getInventory().getViewers().forEach(v -> v.closeInventory());
            p.openInventory(shop.getInventory());
        } else {
            p.sendMessage(getLang("shop.not_found", p));
        }
    }
   
    //#region removeShop
    public void handleRemoveShop(Player p, String[] args){

        if (args.length != 2) {
            p.sendMessage("Usage: /shop remove <name>");
            return;
        }
        Shop shop = plugin.getBlockShopManager().getByName(args[1]);
        if (shop == null) {
            p.sendMessage(getLang("shop.not_found", p));
            return;
        }
        if (!plugin.getBlockShopManager().hasAdminPermission(p, shop)){
            p.sendMessage(getLang("commands.no_perm", p));
            return;
        }
        plugin.getBlockShopManager().remove(shop);
        p.sendMessage(getLang("shop.removed", p));
    }

    //#region setCommand
    private void handleSetCommand(Player p, String[] args, Block targetBlock) {

        if (args.length < 2) {
            p.sendMessage("Usage: /shop set <text/item>");
            return;
        }
        Shop shop = plugin.getBlockShopManager().getByBlock(targetBlock);

        if (shop == null) {
            p.sendMessage(getLang("shop.not_found", p));
            return;
        }
        if (!plugin.getBlockShopManager().hasAdminPermission(p, shop)){
                p.sendMessage(getLang("commands.no_perm", p));
                return;
        }

        StringBuilder descBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            descBuilder.append(args[i]).append(" ");
        }
        String text = descBuilder.toString().trim();
        int max_text = plugin.getConfig().getInt("max_text_chars",64);
        if (text.length() > max_text) {
            p.sendMessage(getLang("misc.chars_limit", p).replace("{max}", max_text+""));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "item":
                if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                    p.sendMessage(getLang("misc.item_in_hand", p));
                    return;
                }
                ItemStack item = p.getInventory().getItemInMainHand().clone();
                item.setAmount(1);
                shop.setItem(item);
                plugin.getBlockShopManager().save(shop);
                break;
            case "text":
                p.sendMessage(getLang("shop.set_text", p));
                shop.setText(text);
                plugin.getBlockShopManager().save(shop);
                break;
 
            case "servershop":
                if (!p.hasPermission("shop.admin")) {
                    p.sendMessage(getLang("commands.no_perm", p));
                    return;
                }
                p.sendMessage(getLang("shop.set_server_shop", p));
                shop.setServerShop(true);
                plugin.getBlockShopManager().save(shop);
                break;                    
            default:
                p.sendMessage("Usage: /shop set <title/subtitle/item>");
                break;
        }
    }
    private void help(Player p, String[] args) {
        p.sendMessage(plugin.getLang().getFrom("help", p));
    }
}
