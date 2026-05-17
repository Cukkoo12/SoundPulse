package com.cukkoo.soundpulse.client;

import com.cukkoo.soundpulse.config.ConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SoundOverlayManager {

    private static final float FADE_DURATION_NS = 1.5e9f;
    private static final int ARC_LEVELS = 3;
    private static final int BASE_RADIUS = 18;
    private static final int RADIUS_STEP = 14;
    private static final int ARC_STEPS = 30;
    private static final float EXPANSION_AMOUNT = 12.0f;
    private static final float THROB_SPEED = 7.0f;
    private static final float THREAT_STROBE_SPEED = 35.0f;

    private static SoundOverlayManager INSTANCE;

    private final List<ActivePulse> pulses = new ArrayList<>();

    private SoundOverlayManager() {
    }

    public static SoundOverlayManager get() {
        if (INSTANCE == null) {
            INSTANCE = new SoundOverlayManager();
        }
        return INSTANCE;
    }

    public void trigger(double relativeAngle, SoundSource category, double distance, boolean threat) {
        pulses.add(new ActivePulse(relativeAngle, category, distance, threat, System.nanoTime()));
    }

    public void render(GuiGraphicsExtractor extractor) {
        if (pulses.isEmpty()) return;
        if (!ConfigManager.get().getConfig().enabled) {
            pulses.clear();
            return;
        }

        long now = System.nanoTime();
        int screenWidth = extractor.guiWidth();
        int screenHeight = extractor.guiHeight();
        float maxOpacity = ConfigManager.get().getConfig().maxOpacity;

        Iterator<ActivePulse> iter = pulses.iterator();
        while (iter.hasNext()) {
            ActivePulse pulse = iter.next();
            float elapsed = (float) (now - pulse.startTime);

            if (elapsed >= FADE_DURATION_NS) {
                iter.remove();
                continue;
            }

            float progress = elapsed / FADE_DURATION_NS;
            float timeSec = elapsed * 1e-9f;
            float fade = 1.0f - progress;

            // Distance-based thickness: close = thick, far = thin
            int thickness = calcThickness(pulse.distance);
            // Distance-based steps: close = more detail, far = less
            int steps = calcSteps(pulse.distance);

            int color;
            float alpha;

            if (pulse.threat) {
                // Threat strobe: fast red flash
                float strobe = (float) Math.sin(timeSec * THREAT_STROBE_SPEED) > 0.3f ? 1.0f : 0.0f;
                alpha = Math.max(0f, fade * strobe);
                int a = Math.round(Math.min(alpha, maxOpacity) * 255.0f) << 24;
                color = a | 0x00FF0000;
            } else {
                float throb = 0.6f + 0.4f * (float) Math.sin(timeSec * THROB_SPEED);
                alpha = Math.max(0f, fade * throb);
                color = ConfigManager.get().getCategoryColor(pulse.category, alpha);
            }

            // Expand outward as the pulse fades
            float expansion = progress * (EXPANSION_AMOUNT + levelOffset(pulse.distance));

            int dirIndex = angleToDirectionIndex(pulse.relativeAngle);

            for (int level = 0; level < ARC_LEVELS; level++) {
                float vibration = 1.2f * (float) Math.sin(timeSec * 30.0 + level * 2.5);
                float radius = BASE_RADIUS + level * RADIUS_STEP + expansion + vibration;

                drawDirectionalArc(extractor, dirIndex, radius,
                        screenWidth, screenHeight, color, thickness, steps);
            }
        }
    }

    private static int calcThickness(double distance) {
        if (distance < 5.0) return 8;
        if (distance < 15.0) return (int) (4 + 4 * (1.0 - (distance - 5.0) / 10.0));
        if (distance < 30.0) return (int) (2 + 2 * (1.0 - (distance - 15.0) / 15.0));
        return 1;
    }

    private static int calcSteps(double distance) {
        if (distance < 10.0) return 30;
        if (distance < 25.0) return 20;
        return 12;
    }

    private static float levelOffset(double distance) {
        if (distance < 5.0) return 8f;
        if (distance < 15.0) return 4f;
        return 0f;
    }

    /**
     * Maps relativeAngle (-180..180, 0 = front) to one of 8 direction indices:
     * 0=FRONT, 1=FRONT_RIGHT, 2=RIGHT, 3=BACK_RIGHT,
     * 4=BACK, 5=BACK_LEFT, 6=LEFT, 7=FRONT_LEFT
     */
    private static int angleToDirectionIndex(double angle) {
        double a = angle + 22.5;
        if (a < 0) a += 360.0;
        if (a >= 360.0) a -= 360.0;
        return (int) (a / 45.0) % 8;
    }

    /**
     * Draws a circular arc at the given direction on the screen edge,
     * curving toward the center of the screen.
     */
    private static void drawDirectionalArc(GuiGraphicsExtractor extractor, int dir, float radius,
                                            int screenWidth, int screenHeight, int color,
                                            int thickness, int steps) {
        double cx, cy, startAngle, endAngle;

        switch (dir) {
            case 0 -> { // FRONT — top center, curves downward
                cx = screenWidth / 2.0;
                cy = 0;
                startAngle = 0;
                endAngle = Math.PI;
            }
            case 1 -> { // FRONT_RIGHT — top right corner, curves toward center
                cx = screenWidth;
                cy = 0;
                startAngle = Math.PI / 2;
                endAngle = Math.PI;
            }
            case 2 -> { // RIGHT — right center, curves leftward
                cx = screenWidth;
                cy = screenHeight / 2.0;
                startAngle = Math.PI / 2;
                endAngle = 3 * Math.PI / 2;
            }
            case 3 -> { // BACK_RIGHT — bottom right corner, curves toward center
                cx = screenWidth;
                cy = screenHeight;
                startAngle = Math.PI;
                endAngle = 3 * Math.PI / 2;
            }
            case 4 -> { // BACK — bottom center, curves upward
                cx = screenWidth / 2.0;
                cy = screenHeight;
                startAngle = Math.PI;
                endAngle = 2 * Math.PI;
            }
            case 5 -> { // BACK_LEFT — bottom left corner, curves toward center
                cx = 0;
                cy = screenHeight;
                startAngle = 3 * Math.PI / 2;
                endAngle = 2 * Math.PI;
            }
            case 6 -> { // LEFT — left center, curves rightward
                cx = 0;
                cy = screenHeight / 2.0;
                startAngle = -Math.PI / 2;
                endAngle = Math.PI / 2;
            }
            case 7 -> { // FRONT_LEFT — top left corner, curves toward center
                cx = 0;
                cy = 0;
                startAngle = 0;
                endAngle = Math.PI / 2;
            }
            default -> {
                return;
            }
        }

        drawArc(extractor, cx, cy, radius, startAngle, endAngle, color, thickness, steps);
    }

    /**
     * Draws a thick arc as overlapping filled rectangles along the arc path.
     */
    private static void drawArc(GuiGraphicsExtractor extractor, double cx, double cy, double radius,
                                double startAngle, double endAngle, int color,
                                int thickness, int steps) {
        double angleStep = (endAngle - startAngle) / steps;
        int halfThick = Math.max(1, thickness / 2);

        for (int i = 0; i <= steps; i++) {
            double theta = startAngle + i * angleStep;
            int x = (int) Math.round(cx + radius * Math.cos(theta));
            int y = (int) Math.round(cy + radius * Math.sin(theta));
            extractor.fill(x - halfThick, y - halfThick, x + halfThick, y + halfThick, color);
        }
    }

    private static class ActivePulse {
        final double relativeAngle;
        final SoundSource category;
        final double distance;
        final boolean threat;
        final long startTime;

        ActivePulse(double relativeAngle, SoundSource category, double distance, boolean threat, long startTime) {
            this.relativeAngle = relativeAngle;
            this.category = category;
            this.distance = distance;
            this.threat = threat;
            this.startTime = startTime;
        }
    }
}
