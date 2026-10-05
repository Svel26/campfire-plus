package dev.worldblock.campfires.compat;

import dev.worldblock.campfires.Campfires;
import dev.worldblock.campfires.campfire.CampfireData;
import dev.worldblock.campfires.campfire.CampfireProperties;
import dev.worldblock.campfires.config.CampfiresConfig;
import dev.worldblock.campfires.registry.ModAttachments;
import dev.worldblock.campfires.util.TemperatureHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin(Campfires.MOD_ID)
public class CampfireJadePlugin implements IWailaPlugin {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Campfires.MOD_ID, "campfire");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CampfireServerData.INSTANCE, CampfireBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CampfireClientData.INSTANCE, CampfireBlock.class);
    }

    private static final class CampfireServerData implements IServerDataProvider<BlockAccessor> {
        private static final CampfireServerData INSTANCE = new CampfireServerData();

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof CampfireBlockEntity campfire)) {
                return;
            }
            CampfireData campfireData = campfire.getExistingDataOrNull(ModAttachments.CAMPFIRE_DATA);
            data.putInt("BurnFresh", accessor.getBlockState().getValue(CampfireProperties.FRESH));
            data.putInt("BurnEmber", accessor.getBlockState().getValue(CampfireProperties.EMBER));
            data.putBoolean("BurnLit", accessor.getBlockState().getValue(CampfireBlock.LIT));
            data.putInt("BurnFuel", campfireData == null ? -1 : campfireData.fuel);
            data.putInt("BurnEmberTimer", campfireData == null ? 0 : campfireData.emberTimer);
            data.putInt("BurnIgnite", campfireData == null ? 0 : campfireData.igniteTimer);
            data.putInt("BurnIgniteTotal", campfireData == null ? 0 : campfireData.igniteTotal);
            data.putInt("BurnPerLog", (int) (CampfiresConfig.TICKS_PER_LOG.get()
                    * TemperatureHelper.burnDurationMultiplier(accessor.getLevel(), accessor.getPosition())));
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }

    private static final class CampfireClientData implements IBlockComponentProvider {
        private static final CampfireClientData INSTANCE = new CampfireClientData();

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (!data.contains("BurnFresh")) {
                return;
            }
            int fresh = data.getInt("BurnFresh");
            int ember = data.getInt("BurnEmber");
            if (data.getBoolean("BurnLit")) {
                int remaining = Math.max(0, data.getInt("BurnFuel"))
                        + Math.max(0, fresh - 1) * data.getInt("BurnPerLog");
                tooltip.add(Component.translatable("jade.campfires.burn_time", formatTicks(remaining)));
                tooltip.add(Component.translatable("jade.campfires.logs", fresh));
            } else if (data.getInt("BurnIgnite") > 0) {
                int total = Math.max(1, data.getInt("BurnIgniteTotal"));
                int percent = (int) ((1.0F - (float) data.getInt("BurnIgnite") / (float) total) * 100.0F);
                tooltip.add(Component.translatable("jade.campfires.igniting", percent));
            } else if (ember > 0) {
                tooltip.add(Component.translatable("jade.campfires.embers", formatTicks(data.getInt("BurnEmberTimer"))));
            } else if (fresh > 0) {
                tooltip.add(Component.translatable("jade.campfires.unlit", fresh));
            } else {
                tooltip.add(Component.translatable("jade.campfires.burnt"));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }

    private static String formatTicks(int ticks) {
        int totalSeconds = Math.max(0, ticks) / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes > 0 ? minutes + "m " + seconds + "s" : seconds + "s";
    }
}
