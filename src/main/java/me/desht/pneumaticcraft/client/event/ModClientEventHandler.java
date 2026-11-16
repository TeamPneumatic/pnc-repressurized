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
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Map;
import java.util.function.Supplier;

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
    public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(RL("minigun"), MinigunItemRenderer.Unbaked.MAP_CODEC);
        event.register(RL("fluid_container_item"), FluidItemModel.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        Block[] toAdd = ModBlocks.BLOCKS.getEntries().stream()
                .filter(h -> h.get() instanceof AbstractPneumaticCraftBlock)
                .map(DeferredHolder::get)
                .toArray(Block[]::new);
        event.registerBlock(PneumaticCraftRepressurizedClient.PARTICLE_HANDLER, toAdd);

        event.registerItem(PneumaticArmorLayerColors.INSTANCE, ModItems.PNEUMATIC_HELMET.get(), ModItems.PNEUMATIC_CHESTPLATE.get(),
                ModItems.PNEUMATIC_LEGGINGS.get(), ModItems.PNEUMATIC_BOOTS.get());
    }

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        registerFluidModel(event, FluidOil.RENDER_PROPS, ModFluids.OIL, ModFluids.OIL_FLOWING);
        registerFluidModel(event, FluidBiodiesel.RENDER_PROPS, ModFluids.BIODIESEL, ModFluids.BIODIESEL_FLOWING);
        registerFluidModel(event, FluidDiesel.RENDER_PROPS, ModFluids.DIESEL, ModFluids.DIESEL_FLOWING);
        registerFluidModel(event, FluidEtchingAcid.RENDER_PROPS, ModFluids.ETCHING_ACID, ModFluids.ETCHING_ACID_FLOWING);
        registerFluidModel(event, FluidEthanol.RENDER_PROPS, ModFluids.ETHANOL, ModFluids.ETHANOL_FLOWING);
        registerFluidModel(event, FluidGasoline.RENDER_PROPS, ModFluids.GASOLINE, ModFluids.GASOLINE_FLOWING);
        registerFluidModel(event, FluidKerosene.RENDER_PROPS, ModFluids.KEROSENE, ModFluids.KEROSENE_FLOWING);
        registerFluidModel(event, FluidLPG.RENDER_PROPS, ModFluids.LPG, ModFluids.LPG_FLOWING);
        registerFluidModel(event, FluidLubricant.RENDER_PROPS, ModFluids.LUBRICANT, ModFluids.LUBRICANT_FLOWING);
        registerFluidModel(event, FluidMemoryEssence.RENDER_PROPS, ModFluids.MEMORY_ESSENCE, ModFluids.MEMORY_ESSENCE_FLOWING);
        registerFluidModel(event, FluidPlastic.RENDER_PROPS, ModFluids.PLASTIC, ModFluids.PLASTIC_FLOWING);
        registerFluidModel(event, FluidVegetableOil.RENDER_PROPS, ModFluids.VEGETABLE_OIL, ModFluids.VEGETABLE_OIL_FLOWING);
        registerFluidModel(event, FluidYeastCulture.RENDER_PROPS, ModFluids.YEAST_CULTURE, ModFluids.YEAST_CULTURE_FLOWING);
    }

    private static void registerFluidModel(RegisterFluidModelsEvent event, PNCFluidRenderProps props, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
        FluidModel.Unbaked model = new FluidModel.Unbaked(new Material(props.getStillTexture()), new Material(props.getFlowingTexture()),
                null, FluidTintSources.constant(props.getTintColor()));
        event.register(model, still, flowing);
    }
}
