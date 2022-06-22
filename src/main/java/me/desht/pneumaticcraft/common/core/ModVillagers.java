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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;
import java.util.Set;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI
            = DeferredRegister.create(ForgeRegistries.POI_TYPES, Names.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS
            = DeferredRegister.create(ForgeRegistries.PROFESSIONS, Names.MOD_ID);

    public static final RegistryObject<PoiType> MECHANIC_POI
            = POI.register("mechanic", () -> new PoiType(getAllStates(ModBlocks.CHARGING_STATION.get()), 1, 1));
    public static final RegistryObject<VillagerProfession> MECHANIC
            = registerProfession("mechanic", ModVillagers.MECHANIC_POI, ModSounds.SHORT_HISS);

    @SuppressWarnings("SameParameterValue")
    private static RegistryObject<VillagerProfession> registerProfession(String name, RegistryObject<PoiType> poiType, RegistryObject<SoundEvent> sound) {
        return PROFESSIONS.register(name, () -> register(RL(name), poiType, sound.get()));
    }

    private static VillagerProfession register(ResourceLocation name, RegistryObject<PoiType> poi, SoundEvent sound) {
        ResourceKey<PoiType> poiName = Objects.requireNonNull(poi.getKey());
        return new VillagerProfession(
                name.toString(), holder -> holder.is(poiName), holder -> holder.is(poiName),
                ImmutableSet.of(), ImmutableSet.of(), sound
        );
    }

    private static Set<BlockState> getAllStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }
}
