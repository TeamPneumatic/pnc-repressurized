package me.desht.pneumaticcraft.client.event;

import me.desht.pneumaticcraft.client.model.CamoModel;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClientEventHandler {
    @SubscribeEvent
    public static void onModelBaking(ModelBakeEvent event) {
        // set up camo models for camouflageable blocks
        for (RegistryObject<Block> block : ModBlocks.BLOCKS.getEntries()) {
            if (block.get() instanceof BlockPneumaticCraftCamo) {
                for (BlockState state : block.get().getStateContainer().getValidStates()) {
                    ModelResourceLocation loc = BlockModelShapes.getModelLocation(state);
                    IBakedModel model = event.getModelRegistry().get(loc);
                    if (model != null) {
                        event.getModelRegistry().put(loc, new CamoModel(model));
                    }
                }
            }
        }
    }
}
