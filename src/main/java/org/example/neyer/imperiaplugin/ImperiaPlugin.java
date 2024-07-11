package org.example.neyer.imperiaplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImperiaPlugin extends JavaPlugin {

    private RegionManager regionManager;
    private ReputationManager reputationManager;
    private PassportManager passportManager;
    private File regionFile;
    private FileConfiguration regionConfig;

    @Override
    public void onEnable() {
        this.regionFile = new File(getDataFolder(), "region.yml");
        this.regionManager = new RegionManager(regionFile);
        this.reputationManager = new ReputationManager(this, regionManager);
        this.passportManager = new PassportManager(this);


        Bukkit.getLogger().info("ImperiaPlugin Enable");

        loadRegionData();
        loadReputationData();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(reputationManager, this);

        new ReputationHourlyTask(this).runTaskTimer(this, 3600 * 20, 3600 * 20);

        ImperiaCommand imperiaCommand = new ImperiaCommand(this);
        this.getCommand("imperia").setExecutor(imperiaCommand);
        this.getCommand("imperia").setTabCompleter(imperiaCommand);
    }

    @Override
    public void onDisable() {
        saveRegionData();
        saveReputationData();
    }
    public ReputationManager getReputationManagerInstance() {
        return reputationManager;
    }
    public PassportManager getPassportManagerInstance() {
        return passportManager;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public ReputationManager getReputationManager() {
        return reputationManager;
    }

    public PassportManager getPassportManager() {
        return passportManager;
    }

    public void loadRegionData() {
        regionFile = new File(getDataFolder(), "region.yml");
        if (!regionFile.exists()) {
            regionFile.getParentFile().mkdirs();
            saveResource("region.yml", false);
        }

        regionConfig = YamlConfiguration.loadConfiguration(regionFile);
        for (String regionName : regionConfig.getKeys(false)) {
            String startPath = regionName + ".start";
            String endPath = regionName + ".end";

            if (regionConfig.contains(startPath) && regionConfig.contains(endPath)) {
                regionManager.setRegionStart(regionName, regionConfig.getLocation(startPath));
                regionManager.setRegionEnd(regionName, regionConfig.getLocation(endPath));
            }

            String whitelistPath = regionName + ".whitelist";
            if (regionConfig.contains(whitelistPath)) {
                regionManager.setRegionWhitelist(regionName, regionConfig.getStringList(whitelistPath));
            }
        }
    }

    public void saveRegionData() {
        regionFile = new File(getDataFolder(), "region.yml");
        regionConfig = new YamlConfiguration();

        for (String regionName : regionManager.getRegionNames()) {
            Location start = regionManager.getRegionStart(regionName);
            Location end = regionManager.getRegionEnd(regionName);
            List<String> whitelist = regionManager.getRegionWhitelistAsList(regionName);

            if (start != null && end != null) {
                regionConfig.set(regionName + ".start", start);
                regionConfig.set(regionName + ".end", end);
            }

            if (!whitelist.isEmpty()) {
                regionConfig.set(regionName + ".whitelist", whitelist);
            }
        }

        try {
            regionConfig.save(regionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadReputationData() {
        reputationManager.loadReputationData();
    }

    public void saveReputationData() {
        reputationManager.saveReputationData();
    }
}
