package org.example.neyer.imperiaplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final ImperiaPlugin plugin;
    private final Map<UUID, Long> playerEnterTime;
    private final Map<UUID, Long> lastLightningStrikeTime;

    public PlayerListener(ImperiaPlugin plugin) {
        this.plugin = plugin;
        this.playerEnterTime = new HashMap<>();
        this.lastLightningStrikeTime = new HashMap<>();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        boolean isInAnyRegion = plugin.getRegionManager().isInAnyRegion(player.getLocation());

        // Проверяем вход игрока в любую запретную зону
        if (isInAnyRegion) {
            boolean enteredRestrictedZone = false;

            // Проверяем каждый регион из конфигурации
            for (String regionName : plugin.getRegionManager().getRegionNames()) {
                if (plugin.getRegionManager().isInRegion(regionName, player.getLocation())) {
                    enteredRestrictedZone = true;

                    if (!plugin.getRegionManager().isPlayerInRegionWhitelist(regionName, player)) {
                        if (!playerEnterTime.containsKey(playerUUID)) {
                            player.sendMessage(ChatColor.AQUA + "[Уведомление Империи] " + ChatColor.RED + "Вы зашли в запретную территорию, у вас есть 30 секунд чтобы ее покинуть, иначе будет запущен протокол защиты");
                            playerEnterTime.put(playerUUID, System.currentTimeMillis());
                            lastLightningStrikeTime.remove(playerUUID);
                        } else {
                            long enterTime = playerEnterTime.get(playerUUID);
                            long timeInRegion = System.currentTimeMillis() - enterTime;
                            if (timeInRegion >= 30000) {
                                if (!lastLightningStrikeTime.containsKey(playerUUID)) {
                                    player.sendMessage(ChatColor.AQUA + "[Уведомление Империи] " + ChatColor.RED + "Протокол защиты запущен!");
                                    lastLightningStrikeTime.put(playerUUID, System.currentTimeMillis());
                                }
                                if (System.currentTimeMillis() - lastLightningStrikeTime.get(playerUUID) >= 10000) {
                                    player.getWorld().strikeLightning(player.getLocation());
                                    lastLightningStrikeTime.put(playerUUID, System.currentTimeMillis());
                                }
                            }
                        }
                    }
                }
            }

            // Если игрок не находится ни в одной запретной зоне
            if (!enteredRestrictedZone && playerEnterTime.containsKey(playerUUID)) {
                player.sendMessage(ChatColor.AQUA + "[Уведомление Империи] " + ChatColor.GREEN + "Вы успешно покинули запретную зону, протокол защиты не будет запущен. И помните, Империя заботится о Вас!");
                playerEnterTime.remove(playerUUID);
                lastLightningStrikeTime.remove(playerUUID);
            }
        } else {
            // Игрок покинул любую запретную зону
            if (playerEnterTime.containsKey(playerUUID)) {
                player.sendMessage(ChatColor.AQUA + "[Уведомление Империи] " + ChatColor.GREEN + "Вы успешно покинули запретную зону, протокол защиты не будет запущен. И помните, Империя заботится о Вас!");
                playerEnterTime.remove(playerUUID);
                lastLightningStrikeTime.remove(playerUUID);
            }
        }
    }

}
