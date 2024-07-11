package org.example.neyer.imperiaplugin;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class ReputationManager implements Listener {

    private final ImperiaPlugin plugin;
    private final RegionManager regionManager;
    private final Map<String, Integer> reputationMap;
    private File reputationFile;
    private FileConfiguration reputationConfig;
    private Map<String, BukkitTask> lightningStrikes;
    private Map<String, BukkitTask> zonePresenceTasks;

    public ReputationManager(ImperiaPlugin plugin, RegionManager regionManager) {
        this.plugin = plugin;
        this.regionManager = regionManager;
        this.reputationMap = new HashMap<>();
        this.reputationFile = new File(plugin.getDataFolder(), "reputation.yml");
        this.reputationConfig = YamlConfiguration.loadConfiguration(reputationFile);
        this.lightningStrikes = new HashMap<>();
        this.zonePresenceTasks = new HashMap<>();

        if (!reputationFile.exists()) {
            plugin.saveResource("reputation.yml", false);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                loadReputationData();
            }
        }.runTaskLater(plugin, 20L);
        startPeriodicNotification();
    }

    public void loadReputationData() {
        for (String key : reputationConfig.getKeys(false)) {
            reputationMap.put(key, reputationConfig.getInt(key));
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!reputationMap.containsKey(onlinePlayer.getName())) {
                reputationMap.put(onlinePlayer.getName(), 100);
            }
        }

        saveReputationData();
    }

    public void saveReputationData() {
        for (Map.Entry<String, Integer> entry : reputationMap.entrySet()) {
            reputationConfig.set(entry.getKey(), entry.getValue());
        }

        try {
            reputationConfig.save(reputationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addReputation(String playerName, int amount) {
        int oldReputation = reputationMap.getOrDefault(playerName, 0);
        reputationMap.put(playerName, oldReputation + amount);
        saveReputationData();
        updateTabDisplay(playerName);
        checkLightningStrike(playerName);
    }

    public void removeReputation(String playerName, int amount) {
        int oldReputation = reputationMap.getOrDefault(playerName, 0);
        reputationMap.put(playerName, oldReputation - amount);
        saveReputationData();
        updateTabDisplay(playerName);
        checkLightningStrike(playerName);
    }

    public void listReputation(Player player) {
        for (Map.Entry<String, Integer> entry : reputationMap.entrySet()) {
            player.sendMessage(ChatColor.YELLOW + entry.getKey() + ": " + entry.getValue());
        }
    }

    public void updateTabDisplay(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null && player.isOnline()) {
            TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
            if (tabPlayer != null) {
                String tabPrefix = TabAPI.getInstance().getNameTagManager().getOriginalPrefix(tabPlayer);
                int reputation = reputationMap.getOrDefault(playerName, 0);
                TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, tabPrefix);
                TabAPI.getInstance().getNameTagManager().setSuffix(tabPlayer, ChatColor.GRAY + " [" + ChatColor.YELLOW + reputation + ChatColor.GRAY + "]");
            }
        }
    }

    public void checkLightningStrike(String playerName) {
        int reputation = reputationMap.getOrDefault(playerName, 0);

        if (reputation < 50) {
            if (!lightningStrikes.containsKey(playerName)) {
                scheduleLightningStrike(playerName);
                broadcastLowReputationNotification(playerName);
            }
        } else {
            cancelLightningStrike(playerName);
        }
    }

    private void startPeriodicNotification() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(ChatColor.AQUA + "[Уведомление Империи] И помните, Империя заботится о Вас!");
                }
            }
        }.runTaskTimer(plugin, 0, 20 * 60 * 15); // 15 minutes
    }

    public void scheduleLightningStrike(String playerName) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null && player.isOnline()) {
                    player.getWorld().strikeLightning(player.getLocation());
                    player.sendMessage(ChatColor.AQUA + "[Уведомление Империи] Ваша репутация ниже 50, поэтому Вас бьет молния. И помните, Империя заботится о Вас!");
                } else {
                    cancel();
                    lightningStrikes.remove(playerName);
                }
            }
        }.runTaskTimer(plugin, 0, 20 * 60 * 10); // 10 minutes

        lightningStrikes.put(playerName, task);
    }

    public void cancelLightningStrike(String playerName) {
        BukkitTask task = lightningStrikes.remove(playerName);
        if (task != null) {
            task.cancel();
        }
    }

    public void broadcastLowReputationNotification(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null && player.isOnline()) {
            player.sendMessage(ChatColor.AQUA + "[Уведомление Империи] Ваша репутация опустилась ниже 50, теперь каждые 10 минут вас будет бить молния. И помните, Империя заботится о Вас!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!reputationMap.containsKey(player.getName())) {
            reputationMap.put(player.getName(), 100); // Установка стартовой репутации 100 для новых игроков
            saveReputationData();
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateTabDisplay(player.getName());
            checkLightningStrike(player.getName());
        }, 40L); // 2 seconds delay
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        // Проверяем каждый регион из конфигурации
        for (String regionName : regionManager.getRegionNames()) {
            if (regionManager.isInRegion(regionName, to)) {
                // Игрок вошел в регион
                if (!zonePresenceTasks.containsKey(player.getName())) {
                    if (!regionManager.isPlayerInRegionWhitelist(regionName, player)) {
                        zonePresenceTasks.put(player.getName(), startZonePresenceTask(player.getName()));
                        removeReputation(player.getName(), 1);
                        player.sendMessage(ChatColor.AQUA + "[Уведомление Империи]" + ChatColor.RED + " Вы вошли в запретную зону '" + regionName + "', ваша репутация снижена на 1 и каждую минуту будет снижаться еще на 5.");
                    }
                }
                return; // Выходим из метода, чтобы не продолжать проверку для других регионов
            }
        }

        // Игрок не находится ни в одном регионе из списка
        BukkitTask task = zonePresenceTasks.remove(player.getName());
        if (task != null) {
            task.cancel();
        }
    }





    private BukkitTask startZonePresenceTask(String playerName) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null && player.isOnline()) {
                    Location playerLocation = player.getLocation();
                    for (String regionName : regionManager.getRegionNames()) {
                        if (regionManager.isInRegion(regionName, playerLocation)) {
                            if (!regionManager.isPlayerInRegionWhitelist(regionName, player)) {
                                removeReputation(playerName, 5);
                                player.sendMessage(ChatColor.AQUA + "[Уведомление Империи]" + ChatColor.RED + " Ваша репутация снижается на 5 за нахождение в запретной зоне '" + regionName + "'.");
                            }
                            return; // Выходим из цикла, т.к. игрок находится в регионе
                        }
                    }
                }
                // Если игрок не находится в запретной зоне ни в одном регионе
                cancel();
                zonePresenceTasks.remove(playerName);
            }
        }.runTaskTimer(plugin, 20 * 60L, 20 * 60L); // Delay: 1 minute (20 ticks per second * 60 seconds), Repeat: 1 minute
    }

}
