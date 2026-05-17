package com.cukkoo.soundpulse.command;

import com.cukkoo.soundpulse.config.ConfigManager;
import com.cukkoo.soundpulse.config.SoundPulseConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public class SoundPulseCommands {

    public static void register(RegisterClientCommandsEvent event) {
        registerCommands(event.getDispatcher());
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soundpulse")
                .then(Commands.literal("toggle")
                        .executes(ctx -> {
                            SoundPulseConfig config = ConfigManager.get().getConfig();
                            config.enabled = !config.enabled;
                            ConfigManager.get().save();
                            String status = config.enabled ? "§aEnabled" : "§cDisabled";
                            ctx.getSource().sendSuccess(() -> Component.literal("§7[SoundPulse] §fMod " + status), false);
                            return 1;
                        })
                )
                .then(Commands.literal("config")
                        .executes(ctx -> {
                            SoundPulseConfig config = ConfigManager.get().getConfig();
                            ctx.getSource().sendSuccess(() -> Component.literal("§6=== SoundPulse Config ==="), false);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§7Enabled: " + (config.enabled ? "§aYes" : "§cNo")), false);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§7Max Opacity: §f" + config.maxOpacity), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Categories:"), false);
                            for (SoundSource source : SoundSource.values()) {
                                boolean enabled = config.enabledCategories.contains(source.name());
                                String hex = config.categoryColors.getOrDefault(source.name(), "default");
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                        "  " + (enabled ? "§a✔" : "§c✘") + " §f" + source.getName()
                                                + " §7(#" + hex + ")"), false);
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("category")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            boolean enabled = BoolArgumentType.getBool(ctx, "enabled");

                                            try {
                                                SoundSource source = SoundSource.valueOf(name.toUpperCase());
                                                ConfigManager.get().setCategoryEnabled(source, enabled);
                                                String status = enabled ? "§aenabled" : "§cdisabled";
                                                ctx.getSource().sendSuccess(() -> Component.literal(
                                                        "§7[SoundPulse] §fCategory '§e" + source.getName()
                                                                + "§f' " + status), false);
                                                return 1;
                                            } catch (IllegalArgumentException e) {
                                                ctx.getSource().sendFailure(Component.literal(
                                                        "§cInvalid category. Valid: HOSTILE, BLOCKS, AMBIENT, "
                                                                + "PLAYERS, MUSIC, WEATHER, NEUTRAL, VOICE, UI"));
                                                return 0;
                                            }
                                        })
                                )
                        )
                )
                .then(Commands.literal("color")
                        .then(Commands.argument("category", StringArgumentType.word())
                                .then(Commands.argument("hex", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "category");
                                            String hex = StringArgumentType.getString(ctx, "hex");

                                            if (!hex.matches("[0-9A-Fa-f]{6}")) {
                                                ctx.getSource().sendFailure(Component.literal(
                                                        "§cInvalid hex color. Use format: RRGGBB (e.g. FF0000)"));
                                                return 0;
                                            }

                                            try {
                                                SoundSource source = SoundSource.valueOf(name.toUpperCase());
                                                ConfigManager.get().setCategoryColor(source, hex);
                                                ctx.getSource().sendSuccess(() -> Component.literal(
                                                        "§7[SoundPulse] §fCategory '§e" + source.getName()
                                                                + "§f' color set to §#" + hex.toUpperCase()), false);
                                                return 1;
                                            } catch (IllegalArgumentException e) {
                                                ctx.getSource().sendFailure(Component.literal(
                                                        "§cInvalid category. Valid: HOSTILE, BLOCKS, AMBIENT, "
                                                                + "PLAYERS, MUSIC, WEATHER, NEUTRAL, VOICE, UI"));
                                                return 0;
                                            }
                                        })
                                )
                        )
                )
                .then(Commands.literal("ignore")
                        .then(Commands.literal("add")
                                .then(Commands.argument("sound_id", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String soundId = StringArgumentType.getString(ctx, "sound_id");
                                            ConfigManager.get().addIgnoredSound(soundId);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§7[SoundPulse] §fIgnored sound: §e" + soundId), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("sound_id", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String soundId = StringArgumentType.getString(ctx, "sound_id");
                                            ConfigManager.get().removeIgnoredSound(soundId);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§7[SoundPulse] §fRemoved ignored sound: §e" + soundId), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("list")
                                .executes(ctx -> {
                                    var ignored = ConfigManager.get().getConfig().ignoredSounds;
                                    ctx.getSource().sendSuccess(() -> Component.literal("§6=== Ignored Sounds ==="), false);
                                    if (ignored.isEmpty()) {
                                        ctx.getSource().sendSuccess(() -> Component.literal("§7None"), false);
                                    } else {
                                        for (String id : ignored) {
                                            ctx.getSource().sendSuccess(() -> Component.literal("§7- §f" + id), false);
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}
