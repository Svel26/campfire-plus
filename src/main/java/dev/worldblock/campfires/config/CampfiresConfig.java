package dev.worldblock.campfires.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CampfiresConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue TICKS_PER_LOG;
    public static final ModConfigSpec.IntValue EMBER_TICKS;
    public static final ModConfigSpec.BooleanValue RAIN_EXTINGUISHES;
    public static final ModConfigSpec.IntValue RAIN_DOUSE_DELAY;
    public static final ModConfigSpec.BooleanValue SIMPLE_CLOUDS_WEATHER;
    public static final ModConfigSpec.BooleanValue TEMPERATURE_AFFECTS_BURN;
    public static final ModConfigSpec.BooleanValue TEMPERATURE_AFFECTS_LIGHTING;
    public static final ModConfigSpec.IntValue STICK_IGNITE_TICKS;
    public static final ModConfigSpec.IntValue STICK_IGNITE_COLD_BONUS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("burning");
        TICKS_PER_LOG = builder
                .comment("How long a single log burns for, in ticks (20 ticks = 1 second).")
                .defineInRange("ticks_per_log", 6000, 20, 240000);
        EMBER_TICKS = builder
                .comment("How long a burnt-out campfire stays as glowing embers before going cold, in ticks.")
                .defineInRange("ember_ticks", 2400, 20, 240000);
        builder.pop();

        builder.push("rain");
        RAIN_EXTINGUISHES = builder
                .comment("Whether rain douses lit campfires.")
                .define("rain_extinguishes", true);
        RAIN_DOUSE_DELAY = builder
                .comment("How many ticks of continuous rain are needed before a campfire is doused.")
                .defineInRange("rain_douse_delay_ticks", 100, 1, 240000);
        SIMPLE_CLOUDS_WEATHER = builder
                .comment("Use Simple Clouds weather data (when installed) to detect rain.")
                .define("simple_clouds_weather", true);
        builder.pop();

        builder.push("temperature");
        TEMPERATURE_AFFECTS_BURN = builder
                .comment("Whether biome/Cold Sweat temperature changes how long a log burns.")
                .define("temperature_affects_burn_duration", true);
        TEMPERATURE_AFFECTS_LIGHTING = builder
                .comment("Whether cold temperature and snow make lighting a campfire harder.")
                .define("temperature_affects_lighting", true);
        builder.pop();

        builder.push("stick_lighting");
        STICK_IGNITE_TICKS = builder
                .comment("How long the stick-rubbing ignition sequence takes in warm, dry conditions, in ticks.")
                .defineInRange("stick_ignite_ticks", 60, 10, 6000);
        STICK_IGNITE_COLD_BONUS = builder
                .comment("Extra ticks added to the stick-rubbing sequence at the coldest/wettest possible conditions.")
                .defineInRange("stick_ignite_cold_bonus_ticks", 100, 0, 6000);
        builder.pop();

        SPEC = builder.build();
    }

    private CampfiresConfig() {
    }
}
