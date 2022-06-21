package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.loot.LootFunc;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModLootFunctions {
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS = DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, Names.MOD_ID);

    public static final RegistryObject<LootItemFunctionType> TE_SERIALIZER =
            LOOT_FUNCTIONS.register("te_serializer", () -> new LootItemFunctionType(new LootFunc.BlockEntitySerializerFunction.Serializer()));
}
