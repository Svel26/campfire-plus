package dev.worldblock.campfires.registry;

import dev.worldblock.campfires.Campfires;
import dev.worldblock.campfires.campfire.CampfireData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Campfires.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CampfireData>> CAMPFIRE_DATA =
            ATTACHMENT_TYPES.register("campfire_data", () -> AttachmentType.builder(CampfireData::new)
                    .serialize(CampfireData.CODEC)
                    .build());
}
