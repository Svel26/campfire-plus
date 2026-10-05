package dev.worldblock.campfires.campfire;

import dev.worldblock.campfires.config.CampfiresConfig;
import dev.worldblock.campfires.registry.ModAttachments;
import dev.worldblock.campfires.util.CampfireWeather;
import dev.worldblock.campfires.util.TemperatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CampfireTicker {
    public static void serverTick(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        long gameTime = level.getGameTime();
        CampfireData data = campfire.getData(ModAttachments.CAMPFIRE_DATA);
        if (data.lastTick >= gameTime) {
            return;
        }
        data.lastTick = gameTime;

        int fresh = state.getValue(CampfireProperties.FRESH);
        int ember = state.getValue(CampfireProperties.EMBER);
        applyLogTypes(data, fresh);

        if (state.getValue(CampfireBlock.LIT)) {
            tickLit(level, pos, state, campfire, data, fresh, ember);
        } else {
            tickUnlit(level, pos, state, campfire, data, ember);
        }
    }

    private static void applyLogTypes(CampfireData data, int fresh) {
        while (data.logTypes.size() > fresh) {
            data.logTypes.remove(data.logTypes.size() - 1);
        }
        while (data.logTypes.size() < fresh) {
            data.logTypes.add(new ItemStack(Items.OAK_LOG));
        }
    }

    private static void tickLit(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CampfireData data, int fresh, int ember) {
        if (fresh <= 0) {
            douse(level, pos, state, campfire, data);
            return;
        }
        if (data.fuel <= 0) {
            data.fuel = logBurnTicks(level, pos);
        }
        data.fuel--;

        if (CampfiresConfig.RAIN_EXTINGUISHES.get() && ++data.rainCheck >= 20) {
            data.rainCheck = 0;
            if (CampfireWeather.isRainingAt(level, pos)) {
                data.rainTicks += 20;
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            4, 0.2, 0.1, 0.2, 0.01);
                }
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 1.8F + level.random.nextFloat() * 0.4F);

                if (data.rainTicks >= CampfiresConfig.RAIN_DOUSE_DELAY.get()) {
                    douse(level, pos, state, campfire, data);
                    return;
                }
            } else {
                data.rainTicks = Math.max(0, data.rainTicks - 10);
            }
        }

        if (data.fuel <= 0) {
            data.consumeOldest();
            if (fresh > 1) {
                BlockState next = state
                        .setValue(CampfireProperties.FRESH, fresh - 1)
                        .setValue(CampfireProperties.EMBER, ember + 1);
                level.setBlock(pos, next, 3);
                data.fuel = logBurnTicks(level, pos);
            } else {
                douse(level, pos, state
                        .setValue(CampfireProperties.FRESH, 0)
                        .setValue(CampfireProperties.EMBER, ember + 1), campfire, data);
                return;
            }
        }

        campfire.setChanged();
    }

    private static void tickUnlit(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CampfireData data, int ember) {
        if (data.igniteTimer > 0 && tickIgnition(level, pos, state, campfire, data)) {
            return;
        }

        if (ember > 0) {
            data.emberTimer--;
            if (data.emberTimer <= 0) {
                level.setBlock(pos, state.setValue(CampfireProperties.EMBER, 0), 3);
            } else if (level instanceof ServerLevel serverLevel && level.random.nextInt(8) == 0) {
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                        1, 0.1, 0.0, 0.1, 0.005);
            }
            campfire.setChanged();
        }
    }

    public static void douse(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CampfireData data) {
        level.setBlock(pos, state.setValue(CampfireBlock.LIT, Boolean.FALSE), 3);
        data.emberTimer = CampfiresConfig.EMBER_TICKS.get();
        data.rainTicks = 0;
        data.fuel = 0;
        level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
        campfire.setChanged();
    }

    public static void addLog(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire, ItemStack stack, Player player) {
        int fresh = state.getValue(CampfireProperties.FRESH);
        int ember = state.getValue(CampfireProperties.EMBER);
        if (fresh >= 4) {
            return;
        }
        CampfireData data = campfire.getData(ModAttachments.CAMPFIRE_DATA);
        data.addLog(stack);
        BlockState next = state
                .setValue(CampfireProperties.FRESH, fresh + 1)
                .setValue(CampfireProperties.EMBER, Math.max(0, ember - 1));
        if (state.getValue(CampfireBlock.LIT) && data.fuel <= 0) {
            data.fuel = logBurnTicks(level, pos);
        }
        level.setBlock(pos, next, 3);
        level.scheduleTick(pos, state.getBlock(), 1);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        campfire.setChanged();
    }

    public static boolean processStickHolding(Level level, BlockPos pos, Player player, CampfireBlockEntity campfire) {
        if (campfire.getBlockState().getValue(CampfireBlock.LIT)) {
            return false;
        }
        CampfireData data = campfire.getData(ModAttachments.CAMPFIRE_DATA);
        long currentTick = level.getGameTime();

        if (data.igniteTotal <= 0 || data.igniteTimer <= 0) {
            float chance = TemperatureHelper.lightChance(level, pos);
            data.igniteTotal = CampfiresConfig.STICK_IGNITE_TICKS.get()
                    + (int) ((1.0F - chance) * CampfiresConfig.STICK_IGNITE_COLD_BONUS.get());
            data.igniteTotal = Math.max(20, data.igniteTotal);
            data.igniteTimer = data.igniteTotal;
            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.85F, 1.2F + level.random.nextFloat() * 0.2F);
        }

        data.lastIgniteActionTick = currentTick;
        data.ignitingPlayer = player.getUUID();
        level.scheduleTick(pos, campfire.getBlockState().getBlock(), 1);
        campfire.setChanged();
        return true;
    }

    public static boolean startStickIgnition(Level level, BlockPos pos, Player player, ItemStack stack, CampfireBlockEntity campfire) {
        return processStickHolding(level, pos, player, campfire);
    }

    private static boolean tickIgnition(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CampfireData data) {
        long currentTick = level.getGameTime();

        // Check if player is still holding right click on the campfire.
        // If more than 8 ticks have passed without an action, player released the button -> reset progress!
        Player player = data.ignitingPlayer != null ? level.getPlayerByUUID(data.ignitingPlayer) : null;
        boolean stillHolding = player != null
                && (currentTick - data.lastIgniteActionTick <= 8)
                && (player.getMainHandItem().is(Items.STICK) || player.getOffhandItem().is(Items.STICK))
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 36.0;

        if (!stillHolding) {
            data.igniteTimer = 0;
            data.igniteTotal = 0;
            data.ignitingPlayer = null;
            campfire.setChanged();
            return false;
        }

        data.igniteTimer--;
        float progress = data.igniteTotal <= 0 ? 1.0F : 1.0F - (float) data.igniteTimer / (float) data.igniteTotal;

        if (level instanceof ServerLevel serverLevel) {
            int smoke = 1 + (int) (progress * 3.0F);
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.45, pos.getZ() + 0.5,
                    smoke, 0.2, 0.05, 0.2, 0.01);
            if (level.random.nextFloat() < 0.25F + progress * 0.45F) {
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                        1, 0.1, 0.0, 0.1, 0.005);
            }
            if (progress > 0.55F && level.random.nextFloat() < 0.2F) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        pos.getX() + 0.5, pos.getY() + 0.35, pos.getZ() + 0.5,
                        1, 0.1, 0.0, 0.1, 0.005);
            }
        }

        if (data.igniteTimer % 4 == 0 || data.igniteTimer <= 0) {
            float pitch = 1.15F + (progress * 0.25F) + (level.random.nextFloat() - 0.5F) * 0.2F;
            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.85F, pitch);
        }

        if (data.igniteTimer <= 0) {
            finishIgnition(level, pos, state, campfire, data, player);
            return true;
        }
        campfire.setChanged();
        return false;
    }

    private static void finishIgnition(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CampfireData data, Player player) {
        data.igniteTimer = 0;
        data.igniteTotal = 0;
        data.ignitingPlayer = null;

        // Shrink the stick now that rubbing to 100% completed
        if (player != null && !player.getAbilities().instabuild) {
            if (player.getMainHandItem().is(Items.STICK)) {
                player.getMainHandItem().shrink(1);
            } else if (player.getOffhandItem().is(Items.STICK)) {
                player.getOffhandItem().shrink(1);
            }
        }

        float chance = TemperatureHelper.lightChance(level, pos);
        if (level.random.nextFloat() < chance) {
            data.fuel = logBurnTicks(level, pos);
            level.setBlock(pos, state.setValue(CampfireBlock.LIT, Boolean.TRUE), 3);
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.9F, 0.8F + level.random.nextFloat() * 0.2F);
            level.playSound(null, pos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.8F, 1.0F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
                        10, 0.25, 0.15, 0.25, 0.02);
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                        6, 0.2, 0.1, 0.2, 0.01);
            }
        } else {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.1F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        14, 0.3, 0.15, 0.3, 0.02);
            }
        }
        campfire.setChanged();
    }

    public static void prepareFuel(Level level, BlockPos pos, CampfireBlockEntity campfire) {
        CampfireData data = campfire.getData(ModAttachments.CAMPFIRE_DATA);
        data.fuel = logBurnTicks(level, pos);
        campfire.setChanged();
    }

    private static int logBurnTicks(Level level, BlockPos pos) {
        return (int) (CampfiresConfig.TICKS_PER_LOG.get() * TemperatureHelper.burnDurationMultiplier(level, pos));
    }

    private CampfireTicker() {
    }
}
