package com.magicbees;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Complicated Bees frames take an on/off config value; Magic Bees' frames get theirs here. */
public final class MBConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.push("frames");
    }

    public static final ModConfigSpec.BooleanValue MAGIC_FRAME = frame("magic");
    public static final ModConfigSpec.BooleanValue RESILIENT_FRAME = frame("resilient");
    public static final ModConfigSpec.BooleanValue GENTLE_FRAME = frame("gentle");
    public static final ModConfigSpec.BooleanValue METABOLIC_FRAME = frame("metabolic");
    public static final ModConfigSpec.BooleanValue NECROTIC_FRAME = frame("necrotic");
    public static final ModConfigSpec.BooleanValue TEMPORAL_FRAME = frame("temporal");
    public static final ModConfigSpec.BooleanValue OBLIVION_FRAME = frame("oblivion");
    /** The three Neo Vitae frames (blood-soaked, blood, frenzied). */
    public static final ModConfigSpec.BooleanValue BLOOD_FRAMES = frame("blood");

    static {
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static ModConfigSpec.BooleanValue frame(String name) {
        return BUILDER.comment("Enable the " + name + " frame").define(name, true);
    }

    private MBConfig() {
    }
}
