package org.example.neyer.imperiaplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ReputationHourlyTask extends BukkitRunnable {

    private final ImperiaPlugin plugin;

    public ReputationHourlyTask(ImperiaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int reputationToAdd = 10; // Количество репутации для начисления каждый час

        // Получаем всех онлайн игроков
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getReputationManagerInstance().addReputation(player.getName(), reputationToAdd);
            // Можно добавить оповещение игрока о начислении репутации, если нужно
            player.sendMessage(ChatColor.AQUA + "[Уведомление Империи]"+ChatColor.GREEN+" Получено " + reputationToAdd + " репутации за час игры на сервере.");
        }
    }
}
