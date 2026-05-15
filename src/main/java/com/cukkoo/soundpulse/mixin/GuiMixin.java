package com.cukkoo.soundpulse.mixin;

import com.cukkoo.soundpulse.client.SoundOverlayManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void soundpulse$afterExtractRenderState(GuiGraphicsExtractor extractor, DeltaTracker tracker, CallbackInfo ci) {
        SoundOverlayManager.get().render(extractor);
    }
}
