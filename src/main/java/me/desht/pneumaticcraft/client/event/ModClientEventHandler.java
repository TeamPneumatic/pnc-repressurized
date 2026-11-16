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
import me.desht.pneumaticcraft.client.PneumaticCraftRepressurizedClient;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.ArmorMainScreen;
import me.desht.pneumaticcraft.client.model.CamoModel;
import me.desht.pneumaticcraft.client.model.custom.FluidItemModel;
import me.desht.pneumaticcraft.client.model.custom.pressure_tube.PressureTubeModel;
import me.desht.pneumaticcraft.client.render.MinigunItemRenderer;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticArmorLayerColors;
import me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock;
import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import me.desht.pneumaticcraft.common.fluid.*;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Map;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

@EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
public class ModClientEventHandler {
    @SubscribeEvent
    public static void onModelBaking(ModelEvent.ModifyBakingResult event) {
        // set up camo models for camouflageable blocks
        for (var block : ModBlocks.BLOCKS.getEntries()) {
            if (block.get() instanceof AbstractCamouflageBlock) {
                Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
                for (BlockState state : block.get().getStateDefinition().getPossibleStates()) {
                    BlockStateModel model = models.get(state);
                    if (model != null) {
                        models.put(state, new CamoModel(model));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(RL("pressure_tube"), PressureTubeModel.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onModelRegistry(RegisterGeometryLoaders event) {
        event.register(RL("fluid_container_item"), FluidItemModel.Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(RL("minigun"), MinigunItemRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        Block[] toAdd = ModBlocks.BLOCKS.getEntries().stream()
                .filter(h -> h.get() instanceof AbstractPneumaticCraftBlock)
                .map(DeferredHolder::get)
                .toArray(Block[]::new);
        event.registerBlock(PneumaticCraftRepressurizedClient.PARTICLE_HANDLER, toAdd);

        for (ItemStack stack :  ArmorMainScreen.ARMOR_STACKS) {
            event.registerItem(PneumaticArmorLayerColors.INSTANCE, stack.getItem());
        }

        event.registerFluidType(FluidOil.RENDER_PROPS, ModFluids.OIL_FLUID_TYPE.get());
        event.registerFluidType(FluidBiodiesel.RENDER_PROPS, ModFluids.BIODIESEL_FLUID_TYPE.get());
        event.registerFluidType(FluidDiesel.RENDER_PROPS, ModFluids.DIESEL_FLUID_TYPE.get());
        event.registerFluidType(FluidEtchingAcid.RENDER_PROPS, ModFluids.ETCHING_ACID_FLUID_TYPE.get());
        event.registerFluidType(FluidEthanol.RENDER_PROPS, ModFluids.ETHANOL_FLUID_TYPE.get());
        event.registerFluidType(FluidGasoline.RENDER_PROPS, ModFluids.GASOLINE_FLUID_TYPE.get());
        event.registerFluidType(FluidKerosene.RENDER_PROPS, ModFluids.KEROSENE_FLUID_TYPE.get());
        event.registerFluidType(FluidLPG.RENDER_PROPS, ModFluids.LPG_FLUID_TYPE.get());
        event.registerFluidType(FluidLubricant.RENDER_PROPS, ModFluids.LUBRICANT_FLUID_TYPE.get());
        event.registerFluidType(FluidMemoryEssence.RENDER_PROPS, ModFluids.MEMORY_ESSENCE_FLUID_TYPE.get());
        event.registerFluidType(FluidPlastic.RENDER_PROPS, ModFluids.PLASTIC_FLUID_TYPE.get());
        event.registerFluidType(FluidVegetableOil.RENDER_PROPS, ModFluids.VEGETABLE_OIL_FLUID_TYPE.get());
        event.registerFluidType(FluidYeastCulture.RENDER_PROPS, ModFluids.YEAST_CULTURE_FLUID_TYPE.get());
    }
}
