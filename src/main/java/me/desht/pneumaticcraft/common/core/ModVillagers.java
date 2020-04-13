package me.desht.pneumaticcraft.common.core;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.function.Supplier;

public class ModVillagers {
    public static final DeferredRegister<PointOfInterestType> POI = new DeferredRegister<>(ForgeRegistries.POI_TYPES, Names.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = new DeferredRegister<>(ForgeRegistries.PROFESSIONS, Names.MOD_ID);

    public static final RegistryObject<PointOfInterestType> MECHANIC_POI = POI.register("mechanic",
            () -> new PointOfInterestType("mechanic", getAllStates(ModBlocks.CHARGING_STATION.get()), 1, 1));
    public static final RegistryObject<VillagerProfession> MECHANIC = registerProfession("mechanic", ModVillagers.MECHANIC_POI);

    @SuppressWarnings("SameParameterValue")
    private static RegistryObject<VillagerProfession> registerProfession(String name, Supplier<PointOfInterestType> poiType) {
        return PROFESSIONS.register(name, () -> new VillagerProfession(Names.MOD_ID + ":" + name, poiType.get(), ImmutableSet.of(), ImmutableSet.of(), null));
    }

    private static Set<BlockState> getAllStates(Block block) {
        return ImmutableSet.copyOf(block.getStateContainer().getValidStates());
    }

}
