package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.loot.LootFunc;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModLootFunctions {
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Names.MOD_ID);

    public static final RegistryObject<LootItemFunctionType> TE_SERIALIZER =
            LOOT_FUNCTIONS.register("te_serializer", () -> new LootItemFunctionType(new LootFunc.BlockEntitySerializerFunction.Serializer()));
}
