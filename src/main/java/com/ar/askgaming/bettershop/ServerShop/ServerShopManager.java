package com.ar.askgaming.bettershop.ServerShop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.bettershop.BetterShop;
import com.ar.askgaming.bettershop.Managers.VirtualShopManager;

public class ServerShopManager extends VirtualShopManager {

    public ServerShopManager(BetterShop plugin, String configFileName) {
        super(plugin, configFileName, "ServerShop");

        this.basePrices = new HashMap<>();
        this.soldItems = new HashMap<>();
        this.currentPrices = new HashMap<>();
        this.lastPrice = new HashMap<>();
        this.totalSoldItems = new HashMap<>();

        loadConfig();

        new Commands(plugin, this);
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
    private void addLore(ItemStack item){
        ItemMeta meta = item.getItemMeta();

        List<String> lore = new ArrayList<>();
        lore.add("§fCurrent Price: " + currentPrices.get(item.getType()));
        lore.add("§5Amount Sold: " + totalSoldItems.get(item.getType()));
        lore.add("§5Price Change: " + percent(lastPrice.get(item.getType()), currentPrices.get(item.getType())));
        meta.setLore(lore);
        item.setItemMeta(meta);

    }
    public String percent(double precioAnterior, double precioActual) {
        if (precioAnterior == 0) return "0%";
        double porcentaje = ((precioActual - precioAnterior) / precioAnterior) * 100;
        return String.format("%+.2f%%", porcentaje); // Incluye + o - en la salida
    }

    public void updateStats(Material material, int amount){
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

        saveConfig();
    }

    private final int PRICE_DROP_THRESHOLD = 50; // Cada 50 unidades vendidas, baja el precio
    private final int PRICE_RISE_THRESHOLD = 10; // Si en 24h se venden menos de 10, sube el precio
    private final double PRICE_DROP_PERCENTAGE = 0.10; // Baja un 10%
    private final double PRICE_RISE_PERCENTAGE = 0.05; // Sube un 5%

    private void adjustPrice(Material material, double percentage) {
        double newPrice = currentPrices.get(material) * (1 + percentage);
    
        currentPrices.put(material, newPrice);
        lastPrice.put(material, currentPrices.get(material));
        config.set(material.name() + ".currentPrice", newPrice);
        config.set(material.name() + ".lastPrice", newPrice);
        
    }

    // Método para revisar el mercado cada cierto tiempo (Ej: cada 24h)
    public void dailyPriceAdjustment() {
        for (Material material : basePrices.keySet()) {
            if (soldItems.get(material) < PRICE_RISE_THRESHOLD) {
                adjustPrice(material, PRICE_RISE_PERCENTAGE);

            }
            soldItems.put(material, 0); // Reset diario de ventas
            config.set(material.name() + ".soldItems", 0);

        }
        saveConfig();
    }
    
}
