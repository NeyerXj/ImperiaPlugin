package org.example.neyer.imperiaplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PassportManager {

    private final ImperiaPlugin plugin;
    private final Map<String, PassportData> passportDataMap;
    private final File passportFile;
    private final FileConfiguration passportConfig;

    private final Map<String, String> positionMap;

    public PassportManager(ImperiaPlugin plugin) {
        this.plugin = plugin;
        this.passportDataMap = new HashMap<>();
        this.passportFile = new File(plugin.getDataFolder(), "passports.yml");
        this.passportConfig = YamlConfiguration.loadConfiguration(passportFile);

        this.positionMap = new HashMap<>();
        positionMap.put("1", "Правительство");
        positionMap.put("2", "Судья");
        positionMap.put("3", "Полицейский");
        positionMap.put("4", "Житель");

        if (!passportFile.exists()) {
            try {
                passportFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        loadPassportData();
    }

    public void loadPassportData() {
        for (String key : passportConfig.getKeys(false)) {
            String playerName = key;
            String position = passportConfig.getString(key + ".position");
            String age = passportConfig.getString(key + ".age");
            String firstJoinDate = passportConfig.getString(key + ".firstJoinDate");
            passportDataMap.put(playerName, new PassportData(playerName, position, age, firstJoinDate));
        }
    }

    public void savePassportData() {
        for (Map.Entry<String, PassportData> entry : passportDataMap.entrySet()) {
            String playerName = entry.getKey();
            PassportData data = entry.getValue();
            passportConfig.set(playerName + ".position", data.getPosition());
            passportConfig.set(playerName + ".age", data.getAge());
            passportConfig.set(playerName + ".firstJoinDate", data.getFirstJoinDate());
        }

        try {
            passportConfig.save(passportFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void givePassport(Player giver, String playerName, String positionKey, String age) {
        Player receiver = Bukkit.getPlayerExact(playerName);
        if (receiver == null) {
            giver.sendMessage("Player not found!");
            return;
        }

        String position = positionMap.get(positionKey);
        if (position == null) {
            giver.sendMessage(ChatColor.RED+"Неверный ключ должности"+ChatColor.YELLOW +" 1 - Правительство. 2 - Судья. 3 - Полицейский. 4 - Житель");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String firstJoinDate = receiver.getFirstPlayed() > 0 ? dateFormat.format(new Date(receiver.getFirstPlayed())) : "N/A";
        passportDataMap.put(playerName, new PassportData(playerName, position, age, firstJoinDate));
        savePassportData();

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Пасспорт");
        meta.setAuthor("Империя");
        meta.addPage(
                "Имя: " + playerName + "\n" +
                        "Первый вход: " + firstJoinDate + "\n" +
                        "Возраст: " + age + "\n" +
                        "Должность: " + position +"\n" +
                        "\n"+
                        "И помните Империя заботится о Вас!"
        );
        book.setItemMeta(meta);

        giver.getInventory().addItem(book);
        giver.sendMessage("Пасспорт выдан!");
    }

    public static class PassportData {
        private final String playerName;
        private final String position;
        private final String age;
        private final String firstJoinDate;

        public PassportData(String playerName, String position, String age, String firstJoinDate) {
            this.playerName = playerName;
            this.position = position;
            this.age = age;
            this.firstJoinDate = firstJoinDate;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getPosition() {
            return position;
        }

        public String getAge() {
            return age;
        }

        public String getFirstJoinDate() {
            return firstJoinDate;
        }
    }
}

