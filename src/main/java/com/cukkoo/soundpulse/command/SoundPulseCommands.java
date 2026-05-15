package com.cukkoo.soundpulse.command;

import com.cukkoo.soundpulse.config.ConfigManager;
import com.cukkoo.soundpulse.config.SoundPulseConfig;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

public class SoundPulseCommands {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(SoundPulseCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
        dispatcher.register(ClientCommands.literal("soundpulse")
                .then(ClientCommands.literal("toggle")
                        .executes(ctx -> {
                            SoundPulseConfig config = ConfigManager.get().getConfig();
                            config.enabled = !config.enabled;
                            ConfigManager.get().save();
                            String status = config.enabled ? "§aEnabled" : "§cDisabled";
                            ctx.getSource().sendFeedback(Component.literal("§7[SoundPulse] §fMod " + status));
                            return 1;
                        })
                )
                .then(ClientCommands.literal("config")
                        .executes(ctx -> {
                            SoundPulseConfig config = ConfigManager.get().getConfig();
                            ctx.getSource().sendFeedback(Component.literal("§6=== SoundPulse Config ==="));
                            ctx.getSource().sendFeedback(Component.literal(
                                    "§7Enabled: " + (config.enabled ? "§aYes" : "§cNo")));
                            ctx.getSource().sendFeedback(Component.literal(
                                    "§7Max Opacity: §f" + config.maxOpacity));
                            ctx.getSource().sendFeedback(Component.literal("§7Categories:"));
                            for (SoundSource source : SoundSource.values()) {
                                boolean enabled = config.enabledCategories.contains(source.name());
                                String hex = config.categoryColors.get(source.name());
                                if (hex == null) hex = "default";
                                ctx.getSource().sendFeedback(Component.literal(
                                        "  " + (enabled ? "§a✔" : "§c✘") + " §f" + source.getName()
                                                + " §7(#" + hex + ")"));
                            }
                            return 1;
                        })
                )
                .then(ClientCommands.literal("category")
                        .then(ClientCommands.argument("name", StringArgumentType.word())
                                .then(ClientCommands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            boolean enabled = BoolArgumentType.getBool(ctx, "enabled");

                                            try {
                                                SoundSource source = SoundSource.valueOf(name.toUpperCase());
                                                ConfigManager.get().setCategoryEnabled(source, enabled);
                                                String status = enabled ? "§aenabled" : "§cdisabled";
                                                ctx.getSource().sendFeedback(Component.literal(
                                                        "§7[SoundPulse] §fCategory '§e" + source.getName()
                                                                + "§f' " + status));
                                                return 1;
                                            } catch (IllegalArgumentException e) {
                                                ctx.getSource().sendError(Component.literal(
                                                        "§cInvalid category. Valid: HOSTILE, BLOCKS, AMBIENT, "
                                                                + "PLAYERS, MUSIC, WEATHER, NEUTRAL, VOICE, UI"));
                                                return 0;
                                            }
                                        })
                                )
                        )
                )
                .then(ClientCommands.literal("color")
                        .then(ClientCommands.argument("category", StringArgumentType.word())
                                .then(ClientCommands.argument("hex", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "category");
                                            String hex = StringArgumentType.getString(ctx, "hex");

                                            if (!hex.matches("[0-9A-Fa-f]{6}")) {
                                                ctx.getSource().sendError(Component.literal(
                                                        "§cInvalid hex color. Use format: RRGGBB (e.g. FF0000)"));
                                                return 0;
                                            }

                                            try {
                                                SoundSource source = SoundSource.valueOf(name.toUpperCase());
                                                ConfigManager.get().setCategoryColor(source, hex);
                                                ctx.getSource().sendFeedback(Component.literal(
                                                        "§7[SoundPulse] §fCategory '§e" + source.getName()
                                                                + "§f' color set to §#" + hex.toUpperCase()));
                                                return 1;
                                            } catch (IllegalArgumentException e) {
                                                ctx.getSource().sendError(Component.literal(
                                                        "§cInvalid category. Valid: HOSTILE, BLOCKS, AMBIENT, "
                                                                + "PLAYERS, MUSIC, WEATHER, NEUTRAL, VOICE, UI"));
                                                return 0;
                                            }
                                        })
                                )
                        )
                )
                .then(ClientCommands.literal("ignore")
                        .then(ClientCommands.literal("add")
                                .then(ClientCommands.argument("sound_id", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String soundId = StringArgumentType.getString(ctx, "sound_id");
                                            ConfigManager.get().addIgnoredSound(soundId);
                                            ctx.getSource().sendFeedback(Component.literal(
                                                    "§7[SoundPulse] §fIgnored sound: §e" + soundId));
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommands.literal("remove")
                                .then(ClientCommands.argument("sound_id", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String soundId = StringArgumentType.getString(ctx, "sound_id");
                                            ConfigManager.get().removeIgnoredSound(soundId);
                                            ctx.getSource().sendFeedback(Component.literal(
                                                    "§7[SoundPulse] §fRemoved ignored sound: §e" + soundId));
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommands.literal("list")
                                .executes(ctx -> {
                                    var ignored = ConfigManager.get().getConfig().ignoredSounds;
                                    ctx.getSource().sendFeedback(Component.literal("§6=== Ignored Sounds ==="));
                                    if (ignored.isEmpty()) {
                                        ctx.getSource().sendFeedback(Component.literal("§7None"));
                                    } else {
                                        for (String id : ignored) {
                                            ctx.getSource().sendFeedback(Component.literal("§7- §f" + id));
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}
