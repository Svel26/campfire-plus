package dev.worldblock.campfires.compat;

import com.momosoftworks.coldsweat.api.event.core.registry.BlockTempRegisterEvent;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import dev.worldblock.campfires.campfire.CampfireProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;

public final class ColdSweatBlockTemps {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(ColdSweatBlockTemps::onRegisterBlockTemps);
    }

    private static void onRegisterBlockTemps(BlockTempRegisterEvent event) {
        event.register(new CampfireBlockTemp(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE));
    }

    private static final class CampfireBlockTemp extends BlockTemp {
        private CampfireBlockTemp(Block... blocks) {
            super(blocks);
        }

        @Override
        public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance) {
            return state.is(Blocks.SOUL_CAMPFIRE) ? 0.4D : 0.5D;
        }

        @Override
        public boolean isValid(Level level, BlockPos pos, BlockState state) {
            if (!(state.getBlock() instanceof CampfireBlock) || !state.hasProperty(CampfireProperties.EMBER)) {
                return false;
            }
            return state.getValue(CampfireBlock.LIT) || state.getValue(CampfireProperties.EMBER) > 0;
        }

        @Override
        public double getMinEffect(LivingEntity entity, Level level, BlockPos pos, BlockState state) {
            return 0.0D;
        }

        @Override
        public double getMaxEffect(LivingEntity entity, Level level, BlockPos pos, BlockState state) {
            return 1.0D;
        }

        @Override
        public double getMinTemp(LivingEntity entity, Level level, BlockPos pos, BlockState state) {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double getMaxTemp(LivingEntity entity, Level level, BlockPos pos, BlockState state) {
            return 18.0D;
        }

        @Override
        public boolean isLogarithmic(LivingEntity entity, Level level, BlockPos pos, BlockState state) {
            return true;
        }
    }
}
