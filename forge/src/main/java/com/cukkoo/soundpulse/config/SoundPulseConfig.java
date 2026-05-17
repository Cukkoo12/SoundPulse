package com.cukkoo.soundpulse.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundPulseConfig {

    public boolean enabled = true;
    public float maxOpacity = 0.65f;
    public List<String> enabledCategories = new ArrayList<>(List.of("HOSTILE"));
    public Map<String, String> categoryColors = new HashMap<>();
    public List<String> ignoredSounds = new ArrayList<>();
}
