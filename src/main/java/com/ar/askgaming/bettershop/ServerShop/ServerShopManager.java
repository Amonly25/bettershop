package com.ar.askgaming.bettershop.ServerShop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.bettershop.Managers.VirtualShopManager;

public class ServerShopManager extends VirtualShopManager{

    public ServerShopManager(BetterShop plugin, String configFileName) {
        super(plugin, configFileName, "ServerShop");

        this.basePrices = new HashMap<>();
        this.soldItems = new HashMap<>();
        this.currentPrices = new HashMap<>();
        this.lastPrice = new HashMap<>();
        this.totalSoldItems = new HashMap<>();

        loadConfig();

        new Commands(plugin, this);
        new DailyTask(plugin, this);
    }
    
    private Map<Material, Double> basePrices;
    private Map<Material, Integer> soldItems;
    private Map<Material, Double> currentPrices;
    private Map<Material, Double> lastPrice;
    private Map<Material, Integer> totalSoldItems;

    public Map<Material, Double> getBasePrices() {
        return basePrices;
    }
    public Map<Material, Integer> getSoldItems() {
        return soldItems;
    }
    public Map<Material, Double> getCurrentPrices() {
        return currentPrices;
    }
    public Map<Material, Double> getLastPrice() {
        return lastPrice;
    }
    @Override
    protected void loadItemsFromConfig() {
        Set<String> keys = config.getKeys(false);

        if (keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            Material material = Material.getMaterial(key);
            if (material != null) {
                ItemStack item = new ItemStack(material);
                basePrices.put(material, config.getDouble(key + ".basePrice",1));
                soldItems.put(material, config.getInt(key + ".soldItems",0));
                currentPrices.put(material, config.getDouble(key + ".currentPrice",1));
                lastPrice.put(material, config.getDouble(key + ".lastPrice",1));
                totalSoldItems.put(material, config.getInt(key + ".totalSoldItems",0));
                addLore(item);
                
                items.add(item);
            }
        }
        createInventories();
    }
    //#region add_item
    public void addItem(Material material, double price) {
        ItemStack item = new ItemStack(material);
        basePrices.put(material, price);
        soldItems.put(material, 0);
        currentPrices.put(material, price);
        lastPrice.put(material, price);
        totalSoldItems.put(material, 0);
        addLore(item);
        config.set(material.name() + ".basePrice", price);
        config.set(material.name() + ".soldItems", 0);
        config.set(material.name() + ".currentPrice", price);
        config.set(material.name() + ".lastPrice", price);
        config.set(material.name() + ".totalSoldItems", 0);
        saveConfig();
        items.add(item);
        addItemToInventory(item);
        
    }
    //#region lore
    public void addLore(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        List<String> configLore = plugin.getConfig().getStringList("server_shop.lore");
        List<String> lore = new ArrayList<>();

        for (String t : configLore) {
            t = t.replace('&', '§');
            t = t.replace("{price}", String.valueOf(currentPrices.get(item.getType())));   
            t = t.replace("{percent}", percent(lastPrice.get(item.getType()), currentPrices.get(item.getType())));
            t = t.replace("{sold}", String.valueOf(totalSoldItems.get(item.getType())));
            lore.add(t);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

    }
    public String percent(double precioAnterior, double precioActual) {
        if (precioAnterior == 0) return "§f0%";
        double porcentaje = ((precioActual - precioAnterior) / precioAnterior) * 100;
        String color = porcentaje >= 0 ? "§a" : "§c"; // Verde si es positivo, rojo si es negativo
        return color + String.format("%+.2f%%", porcentaje);
    }
    

    public void updateStats(ItemStack item, int amount){
        Material material = item.getType();
        int sold = soldItems.get(material) + amount;
        soldItems.put(material, sold);
        config.set(material.name() + ".soldItems", sold);

        totalSoldItems.put(material, totalSoldItems.get(material) + amount);
        config.set(material.name() + ".totalSoldItems", totalSoldItems.get(material));

        // Cada X unidades vendidas, bajamos el precio
        if (soldItems.get(material) >= PRICE_DROP_THRESHOLD) {
            adjustPrice(material, -PRICE_DROP_PERCENTAGE);
            soldItems.put(material, 0); // Reiniciar el contador después de ajustar
        }
        addLore(item);
        saveConfig();
    }

    private final int PRICE_DROP_THRESHOLD = 50; // Cada 50 unidades vendidas, baja el precio
    private final double PRICE_DROP_PERCENTAGE = 0.10; // Baja un 10%
    private final double PRICE_RISE_PERCENTAGE = 0.05; // Sube un 5%

    private void adjustPrice(Material material, double percentage) {
        double current = currentPrices.get(material);

        double newPrice = Math.round(current * (1+percentage) * 100.0) / 100.0;
    
        currentPrices.put(material, newPrice);
        config.set(material.name() + ".currentPrice", newPrice);
        
    }
    

    // Método para revisar el mercado cada cierto tiempo (Ej: cada 24h)
    public void dailyPriceAdjustment() {
        for (Material material : basePrices.keySet()) {
            if (soldItems.get(material) == 0) {
                adjustPrice(material, PRICE_RISE_PERCENTAGE);

            }
            for (Inventory inv : inventories.values()) {
                for (int slot = 0; slot < inv.getSize(); slot++) {
                    if (slot == 45 || slot == 53) {
                        continue;
                    }
                    ItemStack invItem = inv.getItem(slot);
                    if (invItem != null && !invItem.getType().isAir()) {
                        addLore(invItem);
                    }
                }
            }
        }
        
        saveConfig();
    }
    public void resetLastPrices() {
        for (Material material : basePrices.keySet()) {
            lastPrice.put(material, currentPrices.get(material));
            config.set(material.name() + ".lastPrice", currentPrices.get(material));
        }
        saveConfig();
    }    
}
