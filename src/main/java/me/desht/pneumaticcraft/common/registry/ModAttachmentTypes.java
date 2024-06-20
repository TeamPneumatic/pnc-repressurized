package me.desht.pneumaticcraft.common.registry;

import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IActiveEntityHacks;
import me.desht.pneumaticcraft.common.capabilities.ActiveEntityHacks;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES
            = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Names.MOD_ID);

//    public static final Supplier<AttachmentType<Integer>> AIR = ATTACHMENT_TYPES.register(
//            "air", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

    public static final Supplier<AttachmentType<? extends IActiveEntityHacks>> HACKING = ATTACHMENT_TYPES.register(
            "hacking", () -> AttachmentType.serializable(ActiveEntityHacks::new).build());
}
