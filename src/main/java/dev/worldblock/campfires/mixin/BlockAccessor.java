package dev.worldblock.campfires.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface BlockAccessor {
    @Accessor("stateDefinition")
    StateDefinition<Block, BlockState> campfires$getStateDefinition();

    @Invoker("registerDefaultState")
    void campfires$registerDefaultState(BlockState state);
}
