package com.cukkoo.soundpulse.handler;

import com.cukkoo.soundpulse.client.SoundOverlayManager;
import com.cukkoo.soundpulse.config.ConfigManager;
import com.cukkoo.soundpulse.util.SoundDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

public class SoundHandler {

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null) return;

        var soundId = sound.getIdentifier();
        var category = sound.getSource();
        double x = sound.getX();
        double y = sound.getY();
        double z = sound.getZ();

        if (!ConfigManager.get().isCategoryEnabled(category)) return;
        if (ConfigManager.get().isSoundIgnored(soundId.toString())) return;

        Minecraft client = Minecraft.getInstance();
        var camera = client.getCameraEntity();
        if (camera != null) {
            Vec3 soundPos = new Vec3(x, y, z);
            Vec3 playerPos = camera.position();
            float cameraYaw = camera.getYRot();
            double relativeAngle = SoundDirection.calculateRelativeAngle(playerPos, cameraYaw, soundPos);

            double dx = soundPos.x - playerPos.x;
            double dy = soundPos.y - playerPos.y;
            double dz = soundPos.z - playerPos.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            String path = soundId.getPath();
            boolean threat = path.equals("entity.creeper.primed") || path.equals("entity.tnt.primed");

            SoundOverlayManager.get().trigger(relativeAngle, category, distance, threat);
        }
    }
}
