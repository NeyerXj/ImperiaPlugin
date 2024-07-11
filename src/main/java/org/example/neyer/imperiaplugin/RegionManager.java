package org.example.neyer.imperiaplugin;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RegionManager {

    private Map<String, Location> regionStarts;
    private Map<String, Location> regionEnds;
    private Map<String, Set<String>> regionWhitelists;
    private File regionFile;
    private YamlConfiguration regionConfig;

    public RegionManager(File file) {
        this.regionFile = file;
        this.regionStarts = new HashMap<>();
        this.regionEnds = new HashMap<>();
        this.regionWhitelists = new HashMap<>();
        this.regionConfig = YamlConfiguration.loadConfiguration(regionFile);
    }

    public void load() {
        if (!regionFile.exists()) {
            regionFile.getParentFile().mkdirs();
            saveDefaultConfig();
        }

        for (String regionName : regionConfig.getKeys(false)) {
            String startPath = regionName + ".start";
            String endPath = regionName + ".end";

            if (regionConfig.contains(startPath) && regionConfig.contains(endPath)) {
                regionStarts.put(regionName, regionConfig.getLocation(startPath));
                regionEnds.put(regionName, regionConfig.getLocation(endPath));
            }

            String whitelistPath = regionName + ".whitelist";
            if (regionConfig.contains(whitelistPath)) {
                regionWhitelists.put(regionName, new HashSet<>(regionConfig.getStringList(whitelistPath)));
            }
        }
    }

    public void save() {
        for (String regionName : regionStarts.keySet()) {
            Location start = regionStarts.get(regionName);
            Location end = regionEnds.get(regionName);
            Set<String> whitelist = regionWhitelists.getOrDefault(regionName, Collections.emptySet());

            if (start != null && end != null) {
                regionConfig.set(regionName + ".start", start);
                regionConfig.set(regionName + ".end", end);
            }

            if (!whitelist.isEmpty()) {
                regionConfig.set(regionName + ".whitelist", new ArrayList<>(whitelist));
            }
        }

        try {
            regionConfig.save(regionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRegionStart(String regionName, Location start) {
        regionStarts.put(regionName, start);
    }

    public void setRegionEnd(String regionName, Location end) {
        regionEnds.put(regionName, end);
    }

    public Location getRegionStart(String regionName) {
        return regionStarts.get(regionName);
    }

    public Location getRegionEnd(String regionName) {
        return regionEnds.get(regionName);
    }

    public boolean isInAnyRegion(Location location) {
        for (String regionName : regionStarts.keySet()) {
            if (isInRegion(regionName, location)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInRegion(String regionName, Location location) {
        Location start = regionStarts.get(regionName);
        Location end = regionEnds.get(regionName);

        if (start == null || end == null) {
            return false;
        }

        double minX = Math.min(start.getX(), end.getX());
        double minY = Math.min(start.getY(), end.getY());
        double minZ = Math.min(start.getZ(), end.getZ());

        double maxX = Math.max(start.getX(), end.getX());
        double maxY = Math.max(start.getY(), end.getY());
        double maxZ = Math.max(start.getZ(), end.getZ());

        return location.getX() >= minX && location.getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    public void addPlayerToRegionWhitelist(String regionName, String playerName) {
        regionWhitelists.computeIfAbsent(regionName, k -> new HashSet<>()).add(playerName);
    }

    public void removePlayerFromRegionWhitelist(String regionName, String playerName) {
        Set<String> whitelist = regionWhitelists.get(regionName);
        if (whitelist != null) {
            whitelist.remove(playerName);
        }
    }

    public boolean isPlayerInRegionWhitelist(String regionName, Player player) {
        Set<String> whitelist = regionWhitelists.get(regionName);
        return whitelist != null && whitelist.contains(player.getName());
    }

    public Set<String> getRegionWhitelist(String regionName) {
        return regionWhitelists.getOrDefault(regionName, Collections.emptySet());
    }

    public void setRegionWhitelist(String regionName, List<String> whitelist) {
        regionWhitelists.put(regionName, new HashSet<>(whitelist));
    }

    public List<String> getRegionWhitelistAsList(String regionName) {
        return new ArrayList<>(getRegionWhitelist(regionName));
    }

    public Set<String> getRegionNames() {
        return regionStarts.keySet();
    }

    private void saveDefaultConfig() {
        if (regionFile != null && !regionFile.exists()) {
            try {
                regionConfig.save(regionFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
