package dev.worldblock.campfires.util;

import dev.worldblock.campfires.compat.SimpleCloudsWeather;
import dev.worldblock.campfires.config.CampfiresConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.ModList;

public final class CampfireWeather {
    public static boolean isRainingAt(Level level, BlockPos pos) {
        BlockPos above = pos.above();

        // 1. If it cannot see the sky (covered by roof/ceiling), rain cannot hit it
        if (!level.canSeeSky(above)) {
            return false;
        }

        // 2. If any motion-blocking block is above this spot, it is covered
        if (level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, above).getY() > above.getY()) {
            return false;
        }

        // 3. Simple Clouds localized weather check (when installed and enabled)
        if (CampfiresConfig.SIMPLE_CLOUDS_WEATHER.get() && ModList.get().isLoaded("simpleclouds")) {
            if (SimpleCloudsWeather.isSimpleCloudsActive(level)) {
                return SimpleCloudsWeather.isRainingAt(level, above);
            }
        }

        // 4. Vanilla rain check at the open air block directly above the campfire
        if (level.isRainingAt(above)) {
            return true;
        }

        // 5. Fallback check for active global rain in rain biomes
        if (level.isRaining()) {
            Biome biome = level.getBiome(above).value();
            if (biome.getPrecipitationAt(above) == Biome.Precipitation.RAIN) {
                return true;
            }
        }

        return false;
    }

    private CampfireWeather() {
    }
}
