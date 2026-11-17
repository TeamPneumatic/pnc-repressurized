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

package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.PackType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        // added first, so that the providers below get a lookup which includes these registry entries
        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider.ConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, ModWorldGenProvider.PlacedFeatures::bootstrap)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGenProvider.BiomeModifiers::bootstrap)
                .add(Registries.DAMAGE_TYPE, ModDamageTypeProvider::bootstrap)
        );

        event.createProvider(output -> new ModModelProvider(output, event.getResourceManager(PackType.CLIENT_RESOURCES)));
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModLootTablesProvider::new);
        event.createBlockAndItemTags(ModBlockTagsProvider::new, ModItemTagsProvider::new);
        event.createProvider(ModFluidTagsProvider::new);
        event.createProvider(ModBiomeTagsProvider::new);
        event.createProvider(ModEntityTypeTagsProvider::new);
        event.createProvider(ModPoiTypeTagsProvider::new);
        event.createProvider(ModStructureTagsProvider::new);
        event.createProvider(ModGameEventTagsProvider::new);
        event.createProvider(ModDamageTypeTagsProvider::new);
        event.createProvider(ModAdvancementProvider::new);
        event.createProvider(ModGLMProvider::new);
        event.createProvider(ModDataMapProvider::new);
    }
}
