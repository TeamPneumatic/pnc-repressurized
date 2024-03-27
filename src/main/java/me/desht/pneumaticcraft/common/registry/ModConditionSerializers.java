package me.desht.pneumaticcraft.common.registry;

import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.recipes.FluidTagPresentCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModConditionSerializers {
    public static final DeferredRegister<Codec<? extends ICondition>> CONDITIONS
            = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, Names.MOD_ID);

    public static final DeferredHolder<Codec<? extends ICondition>, Codec<FluidTagPresentCondition>> FLUID_TAG_PRESENT
            = CONDITIONS.register("fluid_tag_present", () -> FluidTagPresentCondition.CODEC);
}
