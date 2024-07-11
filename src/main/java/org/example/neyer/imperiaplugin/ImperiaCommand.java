package org.example.neyer.imperiaplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImperiaCommand implements CommandExecutor, TabCompleter {

    private final ImperiaPlugin plugin;

    public ImperiaCommand(ImperiaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда доступна только игрокам.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Использование: /imperia <add|remove|setregion|msg|rep> [игрок|сообщение]");
            return true;
        }

        if (args[0].equalsIgnoreCase("give") && args.length == 5 && args[1].equalsIgnoreCase("passport")) {
            String playerName = args[2];
            String positionKey = args[3];
            String age = args[4];

            plugin.getPassportManagerInstance().givePassport(player, playerName, positionKey, age);
            return true;
        } else if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Использование: /imperia add <игрок> <регион>");
                return true;
            }

            String playerName = args[1];
            String regionName = args[2];

            plugin.getRegionManager().addPlayerToRegionWhitelist(regionName, playerName);
            plugin.saveRegionData();
            player.sendMessage(ChatColor.GREEN + "Игрок " + playerName + " добавлен в белый список региона '" + regionName + "'.");
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Использование: /imperia remove <игрок> <регион>");
                return true;
            }

            String playerName = args[1];
            String regionName = args[2];

            plugin.getRegionManager().removePlayerFromRegionWhitelist(regionName, playerName);
            plugin.saveRegionData();
            player.sendMessage(ChatColor.GREEN + "Игрок " + playerName + " удален из белого списка региона '" + regionName + "'.");
        } else if (args[0].equalsIgnoreCase("setregion")) {
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Использование: /imperia setregion <start|end> <name>");
                return true;
            }

            String regionType = args[1].toLowerCase();
            String regionName = args[2];

            if (regionType.equals("start")) {
                plugin.getRegionManager().setRegionStart(regionName, player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Установлена начальная точка региона '" + regionName + "'.");
            } else if (regionType.equals("end")) {
                plugin.getRegionManager().setRegionEnd(regionName, player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Установлена конечная точка региона '" + regionName + "'.");
            } else {
                player.sendMessage(ChatColor.RED + "Использование: /imperia setregion <start|end> <name>");
                return true;
            }

            plugin.saveRegionData();
        } else if (args[0].equalsIgnoreCase("msg")) {
            if (!player.hasPermission("imperia.notif")) {
                player.sendMessage(ChatColor.RED + "У вас нет разрешения на использование этой команды.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Использование: /imperia msg <сообщение>");
                return true;
            }
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            Bukkit.broadcastMessage(ChatColor.AQUA + "[Уведомление Империи] " + ChatColor.RESET + message);
        } else if (args[0].equalsIgnoreCase("rep")) {
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Использование: /imperia rep <add|remove|list> <количество|игрок>");
                return true;
            }

            if (args[1].equalsIgnoreCase("add")) {
                if (!player.hasPermission("imperia.rep.change")) {
                    player.sendMessage(ChatColor.RED + "У вас нет разрешения на использование этой команды.");
                    return true;
                }
                String targetPlayer = args[2];
                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Количество должно быть числом.");
                    return true;
                }
                plugin.getReputationManager().addReputation(targetPlayer, amount);
                player.sendMessage(ChatColor.GREEN + "Добавлено " + amount + " репутации игроку " + targetPlayer);
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (!player.hasPermission("imperia.rep.change")) {
                    player.sendMessage(ChatColor.RED + "У вас нет разрешения на использование этой команды.");
                    return true;
                }
                String targetPlayer = args[2];
                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Количество должно быть числом.");
                    return true;
                }
                plugin.getReputationManager().removeReputation(targetPlayer, amount);
                player.sendMessage(ChatColor.GREEN + "Снято " + amount + " репутации у игрока " + targetPlayer);
            } else if (args[1].equalsIgnoreCase("list")) {
                if (!player.hasPermission("imperia.rep.list")) {
                    player.sendMessage(ChatColor.RED + "У вас нет разрешения на использование этой команды.");
                    return true;
                }
                plugin.getReputationManager().listReputation(player);
            } else {
                player.sendMessage(ChatColor.RED + "Использование: /imperia rep <add|remove|list> <количество|игрок>");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Неверная команда. Использование: /imperia <add|remove|setregion|msg|rep|give> [игрок|сообщение]");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "remove", "setregion", "msg", "rep", "give");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setregion")) {
                return Arrays.asList("start", "end");
            } else if (args[0].equalsIgnoreCase("rep")) {
                return Arrays.asList("add", "remove", "list");
            }else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                return Arrays.asList("passport");
            }
            }else if (args.length == 3 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("passport")) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }else if (args.length == 4 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("passport")) {
            return Arrays.asList("1", "2", "3", "4");
        }else if (args.length == 5 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("passport")) {
            List<String> ages = new ArrayList<>();
            for (int i = 18; i <= 100; i++) {
                ages.add(String.valueOf(i));
            }
            return ages;
        }

            return Collections.emptyList();
    }
}
