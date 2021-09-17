package me.desht.pneumaticcraft.datagen.loot;

import net.minecraft.loot.LootFunctionType;
import net.minecraft.util.registry.Registry;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModLootFunctions {
    public static final LootFunctionType TE_SERIALIZER = Registry.register(Registry.LOOT_FUNCTION_TYPE,
            RL("te_serializer"), new LootFunctionType(new TileEntitySerializerFunction.Serializer())
    );

    @SuppressWarnings("EmptyMethod")
    public static void init() {
        // poke
    }
}
