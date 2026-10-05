package dev.worldblock.campfires.registry;

import dev.worldblock.campfires.Campfires;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Campfires.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> CAMPFIRE_LOGS =
            DATA_COMPONENTS.registerComponentType("campfire_logs", builder -> builder
                    .persistent(ItemStack.CODEC.listOf())
                    .networkSynchronized(ItemStack.LIST_STREAM_CODEC));
}
