package com.cukkoo.soundpulse;

import com.cukkoo.soundpulse.client.SoundOverlayManager;
import com.cukkoo.soundpulse.command.SoundPulseCommands;
import com.cukkoo.soundpulse.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;

public class SoundPulse implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.get();
        SoundOverlayManager.get();
        SoundPulseCommands.register();
    }
}
