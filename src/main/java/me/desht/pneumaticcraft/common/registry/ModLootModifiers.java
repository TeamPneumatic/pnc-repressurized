package me.desht.pneumaticcraft.common.registry;

import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.loot.PNCDungeonLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS
            = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Names.MOD_ID);

    public static final Supplier<MapCodec<PNCDungeonLootModifier>> DUNGEON_LOOT
            = LOOT_MODIFIERS.register("dungeon_loot", PNCDungeonLootModifier.CODEC);

}
