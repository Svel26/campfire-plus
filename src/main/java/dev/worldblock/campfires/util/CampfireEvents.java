package dev.worldblock.campfires.util;

import dev.worldblock.campfires.campfire.CampfireData;
import dev.worldblock.campfires.campfire.CampfireProperties;
import dev.worldblock.campfires.campfire.CampfireTicker;
import dev.worldblock.campfires.registry.ModAttachments;
import dev.worldblock.campfires.registry.ModDataComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

public final class CampfireEvents {
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        List<BlockPos> campfires = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            if (entry.getValue() instanceof CampfireBlockEntity
                    && entry.getValue().getBlockState().getBlock() instanceof CampfireBlock) {
                campfires.add(entry.getKey().immutable());
            }
        }
        if (campfires.isEmpty()) {
            return;
        }
        MinecraftServer server = level.getServer();
        for (BlockPos pos : campfires) {
            server.execute(() -> {
                if (level.isLoaded(pos)) {
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof CampfireBlock) {
                        level.scheduleTick(pos, state.getBlock(), 1);
                    }
                }
            });
        }
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CampfireBlock) || !state.hasProperty(CampfireProperties.FRESH)) {
            return;
        }

        ItemStack stack = event.getItemStack();
        int fresh = state.getValue(CampfireProperties.FRESH);
        boolean lit = state.getValue(CampfireBlock.LIT);
        boolean waterlogged = state.getValue(CampfireBlock.WATERLOGGED);

        if ((stack.getItem() instanceof ShovelItem || stack.is(ItemTags.SHOVELS)) && lit) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
                CampfireData data = campfire.getData(ModAttachments.CAMPFIRE_DATA);
                CampfireTicker.douse(level, pos, state, campfire, data);
                EquipmentSlot slot = event.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                if (!event.getEntity().getAbilities().instabuild) {
                    stack.hurtAndBreak(1, event.getEntity(), slot);
                }
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            8, 0.2, 0.1, 0.2, 0.02);
                }
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
            return;
        }

        if (stack.is(Items.STICK) && !lit && !waterlogged && fresh > 0) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
                CampfireTicker.processStickHolding(level, pos, event.getEntity(), campfire);
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
            return;
        }

        if (stack.is(ItemTags.LOGS) && fresh < 4) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
                CampfireTicker.addLog(level, pos, state, campfire, stack, event.getEntity());
            }
            event.setCanceled(true);
            return;
        }

        if (stack.is(Items.FLINT_AND_STEEL) && !lit && !waterlogged && fresh > 0) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
                if (level.random.nextFloat() < TemperatureHelper.lightChance(level, pos)) {
                    CampfireTicker.prepareFuel(level, pos, campfire);
                    return;
                }
                event.setCanceled(true);
                Player player = event.getEntity();
                EquipmentSlot slot = event.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, slot);
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
                            8, 0.25, 0.1, 0.25, 0.01);
                }
            }
        }
    }

    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        ItemStack crafted = event.getCrafting();
        if (!crafted.is(Items.CAMPFIRE)) {
            return;
        }
        Container matrix = event.getInventory();
        List<ItemStack> logs = new ArrayList<>();
        for (int i = 0; i < matrix.getContainerSize(); i++) {
            ItemStack slot = matrix.getItem(i);
            if (slot.is(ItemTags.LOGS)) {
                logs.add(slot.copyWithCount(1));
            }
        }
        if (logs.size() == 4) {
            crafted.set(ModDataComponents.CAMPFIRE_LOGS.get(), List.copyOf(logs));
        }
    }

    public static void onBlockDrops(BlockDropsEvent event) {
        BlockState state = event.getState();
        if (!(state.getBlock() instanceof CampfireBlock) || !state.hasProperty(CampfireProperties.FRESH)) {
            return;
        }
        if (!(event.getBlockEntity() instanceof CampfireBlockEntity campfire)) {
            return;
        }
        CampfireData data = campfire.getExistingDataOrNull(ModAttachments.CAMPFIRE_DATA);
        if (data == null) {
            return;
        }

        event.getDrops().clear();
        ServerLevel level = event.getLevel();
        BlockPos pos = event.getPos();
        for (ItemStack stack : data.remainingLogs()) {
            event.getDrops().add(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
        }
    }

    private CampfireEvents() {
    }
}
