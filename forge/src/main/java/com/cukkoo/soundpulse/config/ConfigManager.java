package com.cukkoo.soundpulse.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigManager {

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("soundpulse.json");

    private static final Map<String, String> DEFAULT_COLORS = Map.ofEntries(
            Map.entry("HOSTILE", "CC3333"),
            Map.entry("BLOCKS",  "CC8833"),
            Map.entry("AMBIENT", "33CC33"),
            Map.entry("PLAYERS", "3366CC"),
            Map.entry("MUSIC",   "CC33CC"),
            Map.entry("WEATHER", "33CCCC"),
            Map.entry("NEUTRAL", "CCCC33"),
            Map.entry("VOICE",   "999999")
    );

    private static ConfigManager INSTANCE;

    private SoundPulseConfig config;
    private Set<SoundSource> categoryCache;

    private ConfigManager() {
        this.config = load();
        rebuildCache();
    }

    public static ConfigManager get() {
        if (INSTANCE == null) {
            INSTANCE = new ConfigManager();
        }
        return INSTANCE;
    }

    public SoundPulseConfig getConfig() {
        return config;
    }

    public boolean isCategoryEnabled(SoundSource category) {
        return config.enabled && categoryCache.contains(category);
    }

    public void reload() {
        this.config = load();
        rebuildCache();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH,
                    new GsonBuilder().setPrettyPrinting().create().toJson(config));
        } catch (Exception e) {
            System.err.println("SoundPulse: Config save failed — " + e.getMessage());
        }
    }

    public void setCategoryEnabled(SoundSource source, boolean enabled) {
        String name = source.name();
        if (enabled) {
            if (!config.enabledCategories.contains(name)) {
                config.enabledCategories.add(name);
            }
        } else {
            config.enabledCategories.remove(name);
        }
        save();
        rebuildCache();
    }

    public int getCategoryColor(SoundSource category, float alpha) {
        String hex = config.categoryColors.get(category.name());
        if (hex == null) {
            hex = DEFAULT_COLORS.get(category.name());
        }
        if (hex == null) {
            hex = "FFFFFF";
        }
        int rgb;
        try {
            rgb = Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            rgb = 0xFFFFFF;
        }
        int a = Math.round(Math.min(alpha, config.maxOpacity) * 255.0f) << 24;
        return a | rgb;
    }

    public void setCategoryColor(SoundSource category, String hex) {
        config.categoryColors.put(category.name(), hex.toUpperCase());
        save();
    }

    public boolean isSoundIgnored(String soundId) {
        return config.ignoredSounds.contains(soundId);
    }

    public void addIgnoredSound(String soundId) {
        if (!config.ignoredSounds.contains(soundId)) {
            config.ignoredSounds.add(soundId);
            save();
        }
    }

    public void removeIgnoredSound(String soundId) {
        if (config.ignoredSounds.remove(soundId)) {
            save();
        }
    }

    private SoundPulseConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                SoundPulseConfig cfg = new Gson().fromJson(
                        Files.readString(CONFIG_PATH), SoundPulseConfig.class);

                if (cfg != null) {
                    if (cfg.enabledCategories == null) {
                        cfg.enabledCategories = new java.util.ArrayList<>();
                    }
                    if (cfg.categoryColors == null) {
                        cfg.categoryColors = new java.util.HashMap<>();
                    }
                    if (cfg.ignoredSounds == null) {
                        cfg.ignoredSounds = new java.util.ArrayList<>();
                    }
                    if (cfg.maxOpacity <= 0.0f || cfg.maxOpacity > 1.0f) {
                        cfg.maxOpacity = 0.65f;
                    }
                    return cfg;
                }
            }
        } catch (Exception e) {
            System.err.println("SoundPulse: Config load failed — " + e.getMessage());
        }

        SoundPulseConfig cfg = new SoundPulseConfig();
        save();
        return cfg;
    }

    private void rebuildCache() {
        this.categoryCache = config.enabledCategories.stream()
                .map(name -> {
                    try {
                        return SoundSource.valueOf(name);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
