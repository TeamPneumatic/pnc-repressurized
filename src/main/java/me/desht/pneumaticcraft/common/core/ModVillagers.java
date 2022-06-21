/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.core;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI = DeferredRegister.create(ForgeRegistries.POI_TYPES, Names.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, Names.MOD_ID);

    public static final RegistryObject<PoiType> MECHANIC_POI = POI.register("mechanic",
            () -> new PoiType(getAllStates(ModBlocks.CHARGING_STATION.get()), 1, 1));
    public static final RegistryObject<VillagerProfession> MECHANIC = registerProfession("mechanic", ModVillagers.MECHANIC_POI);

    @SuppressWarnings("SameParameterValue")
    private static RegistryObject<VillagerProfession> registerProfession(String name, RegistryObject<PoiType> poiType) {
        return PROFESSIONS.register(name, () -> register(Names.MOD_ID + ":" + name, poiType.getKey(), ModSounds.SHORT_HISS.get()));
    }

    private static Set<BlockState> getAllStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    // adapted from vanilla

    private static VillagerProfession register(String name, ResourceKey<PoiType> poiType, @Nullable SoundEvent soundEvent) {
        return register(name, (poiTypeHolder) -> poiTypeHolder.is(poiType), (poiTypeHolder) -> poiTypeHolder.is(poiType), soundEvent);
    }

    private static VillagerProfession register(String name, Predicate<Holder<PoiType>> p_219655_, Predicate<Holder<PoiType>> p_219656_, @Nullable SoundEvent soundEvent) {
        return register(name, p_219655_, p_219656_, ImmutableSet.of(), ImmutableSet.of(), soundEvent);
    }

    private static VillagerProfession register(String p_219659_, Predicate<Holder<PoiType>> p_219660_, Predicate<Holder<PoiType>> p_219661_, ImmutableSet<Item> p_219662_, ImmutableSet<Block> p_219663_, @Nullable SoundEvent soundEvent) {
        return new VillagerProfession(p_219659_, p_219660_, p_219661_, p_219662_, p_219663_, soundEvent);
    }
}
