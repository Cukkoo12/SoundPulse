package com.cukkoo.soundpulse.util;

import net.minecraft.world.phys.Vec3;

public enum SoundDirection {

    FRONT, FRONT_RIGHT, RIGHT, BACK_RIGHT, BACK, BACK_LEFT, LEFT, FRONT_LEFT;

    public static double calculateRelativeAngle(Vec3 playerPos, float playerYaw, Vec3 soundPos) {
        double deltaX = soundPos.x - playerPos.x;
        double deltaZ = soundPos.z - playerPos.z;
        if (deltaX == 0.0 && deltaZ == 0.0) return 0.0;

        // Sound's absolute angle from +X axis (standard math, counterclockwise positive)
        double absoluteAngle = Math.toDegrees(Math.atan2(deltaZ, deltaX));
        // Player yaw: 0 = South, -90 = East, 90 = West, ±180 = North
        // Convert to math angle: facingAngle = 90 + yaw (0 = South → 90°, -90 = East → 0°, etc.)
        // Relative angle = sound angle relative to facing direction
        return normalizeAngle(absoluteAngle - (90.0 + playerYaw));
    }

    /**
     * Maps a relative angle (-180..180, 0 = in front) to an 8-direction enum value.
     */
    public static SoundDirection fromAngle(double relativeAngle) {
        relativeAngle = normalizeAngle(relativeAngle);
        if (relativeAngle >= -22.5 && relativeAngle < 22.5) return FRONT;
        if (relativeAngle >= 22.5 && relativeAngle < 67.5) return FRONT_RIGHT;
        if (relativeAngle >= 67.5 && relativeAngle < 112.5) return RIGHT;
        if (relativeAngle >= 112.5 && relativeAngle < 157.5) return BACK_RIGHT;
        if (relativeAngle >= 157.5 || relativeAngle < -157.5) return BACK;
        if (relativeAngle >= -157.5 && relativeAngle < -112.5) return BACK_LEFT;
        if (relativeAngle >= -112.5 && relativeAngle < -67.5) return LEFT;
        return FRONT_LEFT;
    }

    public static double normalizeAngle(double angle) {
        angle = angle % 360.0;
        if (angle > 180.0) angle -= 360.0;
        if (angle <= -180.0) angle += 360.0;
        return angle;
    }
}
