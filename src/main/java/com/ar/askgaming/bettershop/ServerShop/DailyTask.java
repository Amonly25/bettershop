package com.ar.askgaming.bettershop.ServerShop;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.ar.askgaming.bettershop.BetterShop;

public class DailyTask extends BukkitRunnable{

    private final BetterShop plugin;
    private final ServerShopManager serverShopManager;

    public DailyTask(BetterShop plugin, ServerShopManager serverShopManager) {
        this.plugin = plugin;
        this.serverShopManager = serverShopManager;

        this.runTaskTimer(plugin, 0, 20 * 60 * 60);
    }

    @Override
    public void run() {
        FileConfiguration config = plugin.getConfig();
        
        int lastExecutedWeek = config.getInt("server_shop.last_executed_week", getCurrentWeek());
        int currentWeek = getCurrentWeek();
        
        String lastExecutedDay = config.getString("server_shop.last_executed_day", "");
        String today = getCurrentDate();

        // Si es una nueva semana, se reinician los precios
        if (currentWeek != lastExecutedWeek) {
            serverShopManager.resetLastPrices();
            config.set("server_shop.last_executed_week", currentWeek);
        }

        // Verificar si el ajuste ya se hizo hoy
        if (today.equals(lastExecutedDay)) {
            return;
        }

        // Ejecutar ajuste de precios diarios
        serverShopManager.dailyPriceAdjustment();
        config.set("server_shop.last_executed_day", today);
        plugin.saveConfig();

        // Notificar a los jugadores en línea
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(plugin.getLang().getFrom("server_shop.daily_price_adjustment", player));
        }
    }

    private int getCurrentWeek() {
        return LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
    }

    private String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}