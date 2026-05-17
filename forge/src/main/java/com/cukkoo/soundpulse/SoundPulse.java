package com.cukkoo.soundpulse;

import com.cukkoo.soundpulse.client.SoundOverlayManager;
import com.cukkoo.soundpulse.command.SoundPulseCommands;
import com.cukkoo.soundpulse.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;

@Mod("soundpulse")
public class SoundPulse {

    public SoundPulse() {
        ConfigManager.get();
        SoundOverlayManager.get();
    }

    @Mod.EventBusSubscriber(modid = "soundpulse", value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    private static class CommandHandler {
        @net.minecraftforge.eventbus.api.listener.SubscribeEvent
        static void onRegisterCommands(net.minecraftforge.client.event.RegisterClientCommandsEvent event) {
            SoundPulseCommands.register(event.getDispatcher());
        }
    }
}
