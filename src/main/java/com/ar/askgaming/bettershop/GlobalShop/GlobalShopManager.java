package com.ar.askgaming.bettershop.GlobalShop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.bettershop.BetterShop;

public class GlobalShopManager {

    private final File file;
    private FileConfiguration config;
    private final BetterShop plugin;

    private final HashMap<Integer, Inventory> inventories = new HashMap<>();
    private final List<ItemStack> items = new ArrayList<>();

    public GlobalShopManager(BetterShop main) {
        plugin = main;

        new Commands(plugin,this);

        file = new File(plugin.getDataFolder(), "globalshop.yml");
        if (!file.exists()) {
            plugin.saveResource("globalshop.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        List<String> keys = new ArrayList<>(config.getKeys(false));
        if (keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            Object obj = config.get(key);
            if (obj instanceof ItemStack) {
                items.add((ItemStack) obj);
            }
        }
        createInventories();

    }
    public void save(){
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ItemStack> getItems() {
        return items;
    }
    private void createInventories() {
        int size = items.size();
        int pages = (size + 53) / 54; // calculate number of pages
        for (int i = 0; i < pages; i++) {
            Inventory inv = Bukkit.createInventory(null, 54, "Highlight items " + (i + 1));
            inventories.put(i, inv);
        }

        for (int i = 0; i < size; i++) {
            ItemStack item = items.get(i);
            int page = i / 54;
            inventories.get(page).setItem(i % 54, item);
        }
        addNavigationButtons();
    }
    private void addNavigationButtons() {
        for (int page = 0; page < inventories.size(); page++) {
            Inventory inv = inventories.get(page);
            ItemStack prevButton = createNavigationButton("Previous Page", Material.ARROW);
            ItemStack nextButton = createNavigationButton("Next Page", Material.ARROW);

            // Set buttons in the last row (row 5, slots 45-53)
            if (page > 0) {
                inv.setItem(45, prevButton); // "Back" button
            }
            if (page < inventories.size() - 1) {
                inv.setItem(53, nextButton); // "Next" button
            }
        }
    }
        private ItemStack createNavigationButton(String name, Material material) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            button.setItemMeta(meta);
        }
        return button;
    }

    public void sellItem(Player player, ItemStack item, double price) {

        plugin.getItemShopManager().makeItemSellable(player, item, price);
        items.add(item);
        addItemToInventory(item);
        config.set(System.currentTimeMillis()+"", item);
        save();
    }
    private void addItemToInventory(ItemStack item) {

        for (Inventory inv : inventories.values()) {
            if (inv.firstEmpty() != -1) {
                inv.addItem(item);
                return;
            }
        }
        Inventory newInv = Bukkit.createInventory(null, 54, "Items " + (inventories.size() + 1));
        inventories.put(inventories.size(), newInv);
        newInv.addItem(item);
    }
    public void removeItem(ItemStack item) {
        items.remove(item);
        config = new YamlConfiguration();
         for (int i = 0; i < items.size(); i++) {
            config.set(i+"", items.get(i));
        }

        save();
    }
        // Método para manejar clics en los botones de navegación
    public void handleNavigationClick(Player player, InventoryClickEvent event) {
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = meta.getDisplayName();

        if (displayName.equals("Previous Page")) {
            openInventory(player, getCurrentPage(player) - 1);
        } else if (displayName.equals("Next Page")) {
            openInventory(player, getCurrentPage(player) + 1);
        }
    }
    private void openInventory(Player player, int page) {
        if (page >= 0 && page < inventories.size()) {
            player.openInventory(inventories.get(page));
        }
    }
    private int getCurrentPage(Player player) {
        for (Map.Entry<Integer, Inventory> entry : inventories.entrySet()) {
            if (entry.getValue().equals(player.getOpenInventory().getTopInventory())) {
                return entry.getKey();
            }
        }
        return 0; // Default to the first page if not found
    }
}
