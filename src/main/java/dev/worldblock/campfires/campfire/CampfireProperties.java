package dev.worldblock.campfires.campfire;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class CampfireProperties {
    public static final IntegerProperty FRESH = IntegerProperty.create("fresh", 0, 4);
    public static final IntegerProperty EMBER = IntegerProperty.create("ember", 0, 4);

    private CampfireProperties() {
    }
}
