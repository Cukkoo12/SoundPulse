package com.cukkoo.soundpulse.handler;

import com.cukkoo.soundpulse.client.SoundOverlayManager;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;

public class GuiHandler {

    public static void register() {
        AddGuiOverlayLayersEvent.BUS.addListener(event -> {
            event.getLayeredDraw().add(
                Identifier.fromNamespaceAndPath("soundpulse", "sound_pulse_overlay"),
                (gg, dt) -> SoundOverlayManager.get().render(gg)
            );
        });
    }
}
