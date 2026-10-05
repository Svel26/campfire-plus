package dev.worldblock.campfires.util;

import dev.worldblock.campfires.compat.ColdSweatTemperature;
import dev.worldblock.campfires.config.CampfiresConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.ModList;

public final class TemperatureHelper {
    public static double getTemperatureCelsius(Level level, BlockPos pos) {
        if (ModList.get().isLoaded("cold_sweat")) {
            double external = ColdSweatTemperature.getTemperatureCelsius(level, pos);
            if (!Double.isNaN(external) && Math.abs(external) < 200.0) {
                return external;
            }
        }
        Biome biome = level.getBiome(pos).value();
        return biome.getBaseTemperature() * 20.0;
    }

    public static double burnDurationMultiplier(Level level, BlockPos pos) {
        if (!CampfiresConfig.TEMPERATURE_AFFECTS_BURN.get()) {
            return 1.0;
        }
        double temperature = getTemperatureCelsius(level, pos);
        double factor = 0.6 + temperature * 0.025;
        Biome.Precipitation precipitation = level.getBiome(pos).value().getPrecipitationAt(pos);
        if (precipitation != Biome.Precipitation.NONE) {
            factor -= 0.15;
        }
        if (CampfireWeather.isRainingAt(level, pos)) {
            factor -= 0.15;
        }
        return Mth.clamp(factor, 0.4, 2.0);
    }

    public static float lightChance(Level level, BlockPos pos) {
        if (!CampfiresConfig.TEMPERATURE_AFFECTS_LIGHTING.get()) {
            return 1.0F;
        }
        double temperature = getTemperatureCelsius(level, pos);
        double chance = 0.5 + temperature * 0.03;
        Biome.Precipitation precipitation = level.getBiome(pos).value().getPrecipitationAt(pos);
        if (precipitation == Biome.Precipitation.SNOW) {
            chance -= 0.1;
        }
        if (CampfireWeather.isRainingAt(level, pos)) {
            chance -= 0.25;
        }
        return Mth.clamp((float) chance, 0.1F, 1.0F);
    }

    private TemperatureHelper() {
    }
}
