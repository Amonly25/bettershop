package com.ar.askgaming.bettershop.BlockShop;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;

public class Commands implements TabExecutor {

    private BetterShop plugin;
    private BlockShopManager manager;
    public Commands(BetterShop main, BlockShopManager manager) {
        plugin = main;
        this.manager = manager;

        plugin.getServer().getPluginCommand("bshop").setExecutor(this);
        plugin.getServer().getPluginCommand("shop").setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "set", "sell", "delete","open","list","help");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Arrays.asList("text", "item");
        }
        return null;
    }

    private Set<Material> transparentMaterials = new HashSet<>(Arrays.asList(Material.AIR, Material.WATER));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player p = (Player) sender;
        if (command.getName().equalsIgnoreCase("shop")) {
            p.sendMessage(getLang("help", p));
            return true;
        }
    
        if (args.length == 0) {
            p.sendMessage(plugin.getLang().getFrom("bshop_help", p));
            return true;
        }

        Block targetBlock = p.getTargetBlock(transparentMaterials, 5);

        switch (args[0].toLowerCase()) {
            case "create": handleCreateShop(p, args, targetBlock); break;
            case "set": handleSetCommand(p, args, targetBlock); break;
            case "sell": handleSellItemShop(p, args, targetBlock); break;
            case "open": handleOpenByCmd(p, args); break;
            case "delete": handleRemoveShop(p, args); break;
            case "list": handleList(p, args); break;
            case "help": p.sendMessage(getLang("bshop_help", p)); break;
            default: p.sendMessage("Unknown command. Use /shop help"); break;
        }
        return true;
    }
    private String getLang(String path,Player p){
        return plugin.getLang().getFrom(path,p);
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
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            p.sendMessage(getLang("misc.item_in_hand", p));
            return;
        }

        if (!manager.hasPermissionAtBlockLocation(p, targetBlock)){
            p.sendMessage(getLang("commands.no_perm", p));
            return;
        }
        BlockShop shop = manager.getByName(args[1].toLowerCase());
        if (shop != null) {
            p.sendMessage(getLang("shop.exits", p));
            return;
        }           

        if (targetBlock.getState() instanceof InventoryHolder) {
            InventoryHolder ih = (InventoryHolder) targetBlock.getState();
            if (ih.getInventory() instanceof DoubleChestInventory) {
                p.sendMessage(getLang("shop.double_chest", p));
                return;
            }
            manager.createShop(targetBlock,item, p, args[1].toLowerCase());
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
        BlockShop shop = manager.getByBlock(targetBlock);
        if (shop == null) {
            p.sendMessage(getLang("shop.not_found", p));
            return;
        }
        if (!manager.hasAdminPermission(p, shop)){
            p.sendMessage(getLang("commands.no_perm", p));
            return;
        }
        if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
            p.sendMessage(getLang("misc.item_in_hand", p));
            return;
        }
        double d;
        try {
            d = Double.parseDouble(args[1]);
        } catch (Exception e) {
            e.printStackTrace();
            p.sendMessage(plugin.getLang().getFrom("commands.invalid_amount",p));
            return;
        }
        ItemStack i = p.getInventory().getItemInMainHand();
        if (manager.addItemShop(p, shop, i, d)){
            p.sendMessage(getLang("shop.add_item", p).replace("{amount}", i.getAmount()+"").replace("{price}", d+""));
            i.setAmount(0);
        }
    }
    //#region list
    private void handleList(Player p, String[] args) {
        if (manager.getShops().isEmpty()) {
            p.sendMessage(getLang("shop.not_found", p));
            return;
        }

        String filter = (args.length == 2) ? args[1].toLowerCase() : "own";
        UUID playerUUID = p.getUniqueId();

        List<BlockShop> shops = manager.getShops().values().stream()
            .filter(shop -> switch (filter) {
                case "server" -> shop.isServerShop();
                default -> shop.getOnwer().getUniqueId().equals(playerUUID);
            })
            .toList(); // `toList()` es más eficiente que `collect(Collectors.toList())` en este caso.

        if (shops.isEmpty()) {
            p.sendMessage(getLang("shop.not_found", p)); // Mensaje consistente con el de arriba.
            return;
        }

        p.sendMessage(getLang(filter.equals("server") ? "shop.server_shops" : "shop.your_shops", p));
        shops.forEach(shop -> p.sendMessage(shop.getName()));
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
        BlockShop shop = manager.getByName(args[1]);
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
            p.sendMessage("Usage: /shop delete <name>");
            return;
        }
        BlockShop shop = manager.getByName(args[1]);
        if (shop == null) {
            p.sendMessage(getLang("shop.not_found", p));
            return;
        }
        if (!manager.hasAdminPermission(p, shop)){
            p.sendMessage(getLang("commands.no_perm", p));
            return;
        }
        manager.remove(shop);
        p.sendMessage(getLang("shop.deleted", p));
    }

    //#region setCommand
    private void handleSetCommand(Player p, String[] args, Block targetBlock) {

        if (args.length < 2) {
            p.sendMessage("Usage: /shop set <text/item>");
            return;
        }
        BlockShop shop = manager.getByBlock(targetBlock);

        if (shop == null) {
            p.sendMessage(getLang("shop.not_found", p));
            return;
        }
        if (!manager.hasAdminPermission(p, shop)){
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
                manager.setItem(shop, item);
                break;
            case "text":
                p.sendMessage(getLang("shop.set_text", p));
                manager.setText(shop, text);
                break;
 
            case "servershop":
                if (!p.hasPermission("shop.admin")) {
                    p.sendMessage(getLang("commands.no_perm", p));
                    return;
                }
                p.sendMessage(getLang("shop.set_server_shop", p));
                manager.setServerShop(shop, true);
                break;                    
            default:
                p.sendMessage("Usage: /shop set <text/item>");
                break;
        }
    }
}
