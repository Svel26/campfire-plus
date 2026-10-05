package dev.worldblock.campfires.compat;

import dev.nonamecrackers2.simpleclouds.api.SimpleCloudsAPI;
import dev.nonamecrackers2.simpleclouds.api.common.cloud.ScAPICloudType;
import dev.nonamecrackers2.simpleclouds.api.common.world.ScAPICloudManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

public final class SimpleCloudsWeather {
    public static final float CLOUD_SCALE = 8.0F;

    public static boolean isSimpleCloudsActive(Level level) {
        try {
            ScAPICloudManager manager = SimpleCloudsAPI.getApi().getCloudManager(level);
            return manager != null && !manager.shouldUseVanillaWeather();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isRainingAt(Level level, BlockPos pos) {
        try {
            ScAPICloudManager manager = SimpleCloudsAPI.getApi().getCloudManager(level);
            if (manager == null || manager.shouldUseVanillaWeather()) {
                return false;
            }

            // Must be in a biome that precipitates rain
            if (level.getBiome(pos).value().getPrecipitationAt(pos) != Biome.Precipitation.RAIN) {
                return false;
            }

            // 1. Primary check: 3D rain level from SimpleClouds at world position
            float rainLevel = manager.getRainLevel(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
            if (rainLevel > 0.01F) {
                return true;
            }

            // 2. Secondary check: cloud type at scaled cloud grid position
            Pair<? extends ScAPICloudType, Float> cloud = manager.getCloudTypeAtPosition(
                    (float) (pos.getX() + 0.5F) / CLOUD_SCALE,
                    (float) (pos.getZ() + 0.5F) / CLOUD_SCALE
            );
            if (cloud != null && cloud.getLeft() != null) {
                return cloud.getLeft().weatherType().includesRain() && cloud.getRight() != null && cloud.getRight() > 0.01F;
            }

            return false;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private SimpleCloudsWeather() {
    }
}
