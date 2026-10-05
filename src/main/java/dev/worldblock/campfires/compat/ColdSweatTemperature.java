package dev.worldblock.campfires.compat;

import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class ColdSweatTemperature {
    public static double getTemperatureCelsius(Level level, BlockPos pos) {
        try {
            return WorldHelper.getTemperatureAt(level, pos);
        } catch (Throwable ignored) {
            return Double.NaN;
        }
    }

    private ColdSweatTemperature() {
    }
}
