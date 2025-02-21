package com.ar.askgaming.bettershop.GlobalShop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.bettershop.Managers.VirtualShopManager;

public class GlobalShopManager extends VirtualShopManager {

    public GlobalShopManager(BetterShop plugin, String configFileName, String name) {
        super(plugin, configFileName, name);
        
        new Commands(plugin, this);

        loadConfig();
    }

    //#region sellItem
    public void sellItem(Player player, ItemStack itemInMainHand, double price) {
        ItemStack item = itemInMainHand.clone();
        plugin.getItemShopManager().makeItemSellable(player, item, price);
        items.add(item);
        addItemToInventory(item);
        config.set(System.currentTimeMillis()+"", item);
        saveConfig();
        itemInMainHand.setAmount(0);
        player.sendMessage(plugin.getLang().getFrom("global_shop.item_added", player).replace("{price}", String.valueOf(price)).replace("{item}", item.getType().name()));

    }

    public void updateConfig() {
        List<ItemStack> newItems = new ArrayList<>();
        config = new YamlConfiguration();
        
        for (Inventory inv : inventories.values()) {
            for (int slot = 0; slot < inv.getSize(); slot++) {
                if (slot == 45 || slot == 53) {
                    continue;
                }
                ItemStack invItem = inv.getItem(slot);
                if (invItem != null && !invItem.getType().isAir()) {
                    newItems.add(invItem);
                }
            }
        }
        
        for (int i = 0; i < newItems.size(); i++) {
            config.set(String.valueOf(i), newItems.get(i));
        }
        
        saveConfig();
    }

    public boolean isGlobalShopInventory(Inventory inv) {
        return inventories.containsValue(inv);
    }

    // Método para manejar clics en los botones de navegación

    public int getAmountItemsPublished(Player player) {
        int amount = 0;
        for (ItemStack item : items) {
            if (plugin.getItemShopManager().getSeller(item).equals(player.getUniqueId())) {
                amount++;
            }
        }
        return amount;
    }
}
