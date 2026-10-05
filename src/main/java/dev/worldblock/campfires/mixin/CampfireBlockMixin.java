package dev.worldblock.campfires.mixin;

import dev.worldblock.campfires.campfire.CampfireData;
import dev.worldblock.campfires.campfire.CampfireProperties;
import dev.worldblock.campfires.campfire.CampfireTicker;
import dev.worldblock.campfires.registry.ModAttachments;
import dev.worldblock.campfires.registry.ModDataComponents;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void campfires$addProperties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(CampfireProperties.FRESH, CampfireProperties.EMBER);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void campfires$registerDefaultState(boolean spawnParticles, int fireDamage, BlockBehaviour.Properties properties, CallbackInfo ci) {
        BlockState state = ((BlockAccessor) this).campfires$getStateDefinition().any()
                .setValue(CampfireBlock.LIT, Boolean.TRUE)
                .setValue(CampfireBlock.SIGNAL_FIRE, Boolean.FALSE)
                .setValue(CampfireBlock.WATERLOGGED, Boolean.FALSE)
                .setValue(CampfireBlock.FACING, Direction.NORTH)
                .setValue(CampfireProperties.FRESH, 4)
                .setValue(CampfireProperties.EMBER, 0);
        ((BlockAccessor) this).campfires$registerDefaultState(state);
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void campfires$unlitPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState placed = cir.getReturnValue();
        if (placed != null) {
            cir.setReturnValue(placed
                    .setValue(CampfireBlock.LIT, Boolean.FALSE)
                    .setValue(CampfireProperties.FRESH, 4)
                    .setValue(CampfireProperties.EMBER, 0));
        }
    }

    /**
     * Scheduled block ticks drive the campfire life cycle. Block entity tickers cannot be used here
     * because optimization mods (e.g. Lithium's sleeping block entities) skip them when vanilla does
     * not need them, which is exactly when our timer still needs to run.
     */
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
            CampfireTicker.serverTick(level, pos, state, campfire);
        }
        level.scheduleTick(pos, state.getBlock(), 1);
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        List<ItemStack> logs = stack.get(ModDataComponents.CAMPFIRE_LOGS.get());
        if (logs != null && !logs.isEmpty() && level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
            CampfireData data = campfire.getData(ModAttachments.CAMPFIRE_DATA);
            data.setLogs(logs);
            campfire.setChanged();
        }
        level.scheduleTick(pos, state.getBlock(), 1);
    }
}
