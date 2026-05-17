package com.cukkoo.soundpulse;

import com.cukkoo.soundpulse.client.SoundOverlayManager;
import com.cukkoo.soundpulse.command.SoundPulseCommands;
import com.cukkoo.soundpulse.config.ConfigManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("soundpulse")
public class SoundPulse {

    public SoundPulse(IEventBus modEventBus) {
        ConfigManager.get();
        SoundOverlayManager.get();
        modEventBus.addListener(SoundPulseCommands::register);
    }
}
