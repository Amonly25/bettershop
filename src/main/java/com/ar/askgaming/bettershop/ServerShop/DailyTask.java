package com.ar.askgaming.bettershop.ServerShop;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.ar.askgaming.bettershop.BetterShop;

public class DailyTask extends BukkitRunnable{

    private final BetterShop plugin;
    private final ServerShopManager serverShopManager;

    public DailyTask(BetterShop plugin, ServerShopManager serverShopManager) {
        this.plugin = plugin;
        this.serverShopManager = serverShopManager;

        this.runTaskTimer(plugin, 0, 20 * 60 * 60 * 24);
    }

    @Override
    public void run() {
        int lastExecutedWeek = plugin.getConfig().getInt("server_shop.last_executed_week", getCurrentWeek());
        int currentWeek = getCurrentWeek();
        
        if (currentWeek != lastExecutedWeek) {
            // Si es una nueva semana, reseteamos el valor necesario
            serverShopManager.resetLastPrices();
            lastExecutedWeek = currentWeek;
            plugin.getConfig().set("server_shop.last_executed_week", lastExecutedWeek);
            plugin.saveConfig();
        }

        serverShopManager.dailyPriceAdjustment();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(plugin.getLang().getFrom("server_shop.daily_price_adjustment", player));

        }
    }
    private int getCurrentWeek() {
        return LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
    }

}
