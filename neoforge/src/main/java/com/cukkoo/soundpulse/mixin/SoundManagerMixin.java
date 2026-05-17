package com.cukkoo.soundpulse.mixin;

import com.cukkoo.soundpulse.client.SoundOverlayManager;
import com.cukkoo.soundpulse.config.ConfigManager;
import com.cukkoo.soundpulse.util.SoundDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("SoundPulse");

    @Inject(method = "play", at = @At("HEAD"))
    private void soundpulse$onPlay(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        Identifier soundId = sound.getIdentifier();
        SoundSource category = sound.getSource();
        double x = sound.getX();
        double y = sound.getY();
        double z = sound.getZ();

        LOGGER.info("Sound played: [{}] | Category: {} | Position: ({}, {}, {})",
                soundId, category.getName(), x, y, z);

        if (!ConfigManager.get().isCategoryEnabled(category)) return;
        if (ConfigManager.get().isSoundIgnored(soundId.toString())) return;

        Minecraft client = Minecraft.getInstance();
        net.minecraft.world.entity.Entity camera = client.getCameraEntity();
        if (camera != null) {
            Vec3 soundPos = new Vec3(x, y, z);
            Vec3 playerPos = camera.position();
            float cameraYaw = camera.getYRot();
            double relativeAngle = SoundDirection.calculateRelativeAngle(playerPos, cameraYaw, soundPos);

            // Distance calculation
            double dx = soundPos.x - playerPos.x;
            double dy = soundPos.y - playerPos.y;
            double dz = soundPos.z - playerPos.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Threat detection: creeper priming or TNT igniting
            String path = soundId.getPath();
            boolean threat = path.equals("entity.creeper.primed") || path.equals("entity.tnt.primed");

            SoundOverlayManager.get().trigger(relativeAngle, category, distance, threat);
        }
    }
}
