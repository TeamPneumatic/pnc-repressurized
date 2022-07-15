package me.desht.pneumaticcraft.common.core;

import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.loot.PNCDungeonLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS
            = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Names.MOD_ID);

    public static final RegistryObject<Codec<PNCDungeonLootModifier>> DUNGEON_LOOT
            = LOOT_MODIFIERS.register("dungeon_loot", PNCDungeonLootModifier.CODEC);

}
