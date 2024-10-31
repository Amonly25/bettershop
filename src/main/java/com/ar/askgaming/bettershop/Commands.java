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
            return Arrays.asList("reload");
        }
        return new ArrayList<>();
    }

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
        Set<Material> transparentMaterials = new HashSet<>(Arrays.asList(Material.AIR, Material.WATER));
        Block targetBlock = p.getTargetBlock(transparentMaterials, 5);
        switch (args[0].toLowerCase()) {

            case "set_buy":

                if (plugin.getBlockShopManager().isShop(targetBlock)){
                    Shop shop = plugin.getBlockShopManager().getByBlock(targetBlock);
                    if (shop.getOnwer().equals(p)){
                        shop.setType(Shop.ShopType.BUY);
                        p.sendMessage("Shop set to buy mode!");
                    }else{
                        p.sendMessage("You are not the owner of this shop!");
                    }
                }
                break;
            case "set_sell":

                if (plugin.getBlockShopManager().isShop(targetBlock)){
                    Shop shop = plugin.getBlockShopManager().getByBlock(targetBlock);
                    if (shop.getOnwer().equals(p)){
                        shop.setType(Shop.ShopType.SELL);
                        p.sendMessage("Shop set to buy mode!");
                    }else{
                        p.sendMessage("You are not the owner of this shop!");
                    }
                }
                break;

            case "create":
                p.sendMessage("Test");

                    // Obtener el bloque objetivo del jugador a una distancia máxima especificada

                    if (p.getInventory().getItemInMainHand() == null) {
                        p.sendMessage("Debe tener un item en la mano!");

                    }
                    if (transparentMaterials.contains(targetBlock.getType())) {
                        break;
                    }
                    if (targetBlock.getState() instanceof InventoryHolder) {
                        Shop shop = new Shop(targetBlock, p.getInventory().getItemInMainHand(),p);

                    }
                    p.sendMessage("Debe ser un bloque con inventario!");

                break;
            default:
                break;
        }
        return false;
    }

}
