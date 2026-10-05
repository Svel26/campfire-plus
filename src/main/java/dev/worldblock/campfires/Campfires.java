package dev.worldblock.campfires;

import dev.worldblock.campfires.compat.ColdSweatBlockTemps;
import dev.worldblock.campfires.config.CampfiresConfig;
import dev.worldblock.campfires.registry.ModAttachments;
import dev.worldblock.campfires.registry.ModDataComponents;
import dev.worldblock.campfires.util.CampfireEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Campfires.MOD_ID)
public class Campfires {
    public static final String MOD_ID = "campfires";
    public static final Logger LOGGER = LoggerFactory.getLogger("Campfires");

    public Campfires(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CampfiresConfig.SPEC);

        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModAttachments.ATTACHMENT_TYPES.register(modBus);

        if (ModList.get().isLoaded("cold_sweat")) {
            ColdSweatBlockTemps.register();
        }

        NeoForge.EVENT_BUS.addListener(CampfireEvents::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(CampfireEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(CampfireEvents::onItemCrafted);
        NeoForge.EVENT_BUS.addListener(CampfireEvents::onBlockDrops);
    }
}
