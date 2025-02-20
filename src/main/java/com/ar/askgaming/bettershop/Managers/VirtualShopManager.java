package com.ar.askgaming.bettershop.Managers;

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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.bettershop.BetterShop;

public abstract class VirtualShopManager implements Listener {
    protected final File file;
    protected FileConfiguration config;
    protected final BetterShop plugin;
    protected final String name;
    protected HashMap<Integer, Inventory> inventories = new HashMap<>();
    protected List<ItemStack> items = new ArrayList<>();

    public VirtualShopManager(BetterShop plugin, String configFileName, String name) {
        this.plugin = plugin;
        this.name = name;
        this.file = new File(plugin.getDataFolder(), configFileName);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    protected void loadConfig() {
        if (!file.exists()) {
            plugin.saveResource(file.getName(), false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadItemsFromConfig();
    }

    protected void loadItemsFromConfig() {
        List<String> keys = new ArrayList<>(config.getKeys(false));
        for (String key : keys) {
            Object obj = config.get(key);
            if (obj instanceof ItemStack) {
                items.add((ItemStack) obj);
            }
        }
        createInventories();
    }

    protected void saveConfig() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void createInventories() {
        int size = items.size();
        int pages = (size + 44) / 45;
        for (int i = 0; i < pages; i++) {
            Inventory inv = Bukkit.createInventory(null, 54, name + " " + (i + "/"+ 1));
            inventories.put(i, inv);
            addNavigationButtons(inv);
        }
        distributeItems();
    }

    protected void distributeItems() {
        for (int i = 0, j = 0; i < items.size(); i++, j++) {
            if (j % 54 == 45 || j % 54 == 53) {
                j++;
            }
            int page = j / 54;
            inventories.get(page).setItem(j % 54, items.get(i));
        }
    }

    protected void addNavigationButtons(Inventory inv) {
        inv.setItem(45, createNavigationButton("Previous Page", Material.ARROW));
        inv.setItem(53, createNavigationButton("Next Page", Material.ARROW));
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

    public boolean isShopInventory(Inventory inv) {
        return inventories.containsValue(inv);
    }

    @EventHandler
    public void handleNavigationClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
        if (!isShopInventory(event.getClickedInventory())) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        if (slot == 45) {
            openInventory(player, getCurrentPage(player) - 1);
        } else if (slot == 53) {
            openInventory(player, getCurrentPage(player) + 1);
        }
    }

    public void openInventory(Player player, int page) {
        if (inventories.isEmpty()) {
            player.sendMessage("No items to show");
            return;
        }
        if (page >= 0 && page < inventories.size()) {
            player.openInventory(inventories.get(page));
        }
    }
    protected void addItemToInventory(ItemStack item) {

        for (Inventory inv : inventories.values()) {
            if (inv.firstEmpty() != -1 && inv.firstEmpty() != 45 && inv.firstEmpty() != 53) {
                inv.addItem(item);
                return;
            }
        }
        Inventory newInv = Bukkit.createInventory(null, 54, name + (inventories.size() + 1));
        inventories.put(inventories.size(), newInv);
        addNavigationButtons(newInv);

        newInv.addItem(item);
    }
    private int getCurrentPage(Player player) {
        for (Map.Entry<Integer, Inventory> entry : inventories.entrySet()) {
            if (entry.getValue().equals(player.getOpenInventory().getTopInventory())) {
                return entry.getKey();
            }
        }
        return 0;
    }
    public FileConfiguration getConfig() {
        return config;
    }
}
