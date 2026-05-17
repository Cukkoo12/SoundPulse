package com.cukkoo.soundpulse;

import com.cukkoo.soundpulse.client.SoundOverlayManager;
import com.cukkoo.soundpulse.command.SoundPulseCommands;
import com.cukkoo.soundpulse.config.ConfigManager;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod("soundpulse")
public class SoundPulse {

    public SoundPulse() {
        ConfigManager.get();
        SoundOverlayManager.get();
        NeoForge.EVENT_BUS.addListener(SoundPulseCommands::register);
    }
}
