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

package me.desht.pneumaticcraft.client.event;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.model.CamoModel;
import me.desht.pneumaticcraft.client.model.custom.CamouflageModel;
import me.desht.pneumaticcraft.client.model.custom.FluidItemModel;
import me.desht.pneumaticcraft.client.model.custom.RenderedItemModel;
import me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClientEventHandler {
    @SubscribeEvent
    public static void onModelBaking(ModelEvent.ModifyBakingResult event) {
        // set up camo models for camouflageable blocks
        for (var block : ModBlocks.BLOCKS.getEntries()) {
            if (block.get() instanceof AbstractCamouflageBlock) {
                for (BlockState state : block.get().getStateDefinition().getPossibleStates()) {
                    ModelResourceLocation loc = BlockModelShaper.stateToModelLocation(state);
                    BakedModel model = event.getModels().get(loc);
                    if (model != null) {
                        event.getModels().put(loc, new CamoModel(model));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onModelRegistry(RegisterGeometryLoaders event) {
        event.register(RL("camouflaged"), CamouflageModel.Loader.INSTANCE);
        event.register(RL("fluid_container_item"), FluidItemModel.Loader.INSTANCE);
        event.register(RL("rendered_item"), RenderedItemModel.Loader.INSTANCE);
    }
}
