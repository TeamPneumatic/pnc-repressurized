package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.model.BakedMinigunWrapper;
import me.desht.pneumaticcraft.client.model.CamoModel;
import me.desht.pneumaticcraft.client.model.pressureglass.PressureGlassBakedModel;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
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

        // pressure chamber glass
        for (BlockState state : ModBlocks.PRESSURE_CHAMBER_GLASS.get().getStateContainer().getValidStates()) {
            ModelResourceLocation loc = BlockModelShapes.getModelLocation(state);
            IBakedModel model = event.getModelRegistry().get(loc);
            if (model != null) {
                event.getModelRegistry().put(loc, new PressureGlassBakedModel(DefaultVertexFormats.ITEM));
            }
        }

        // minigun model: using TEISR for in-hand transforms
        ModelResourceLocation mrl = new ModelResourceLocation(ModItems.MINIGUN.get().getRegistryName(), "inventory");
        IBakedModel object = event.getModelRegistry().get(mrl);
        if (object != null) {
            event.getModelRegistry().put(mrl, new BakedMinigunWrapper(object));
        }
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        // pressure glass connected textures
        if (event.getMap().getBasePath().equals("textures")) {
            for (int i = 0; i < PressureGlassBakedModel.TEXTURE_COUNT; i++) {
                ResourceLocation loc = new ResourceLocation(Textures.PRESSURE_GLASS_LOCATION + "window_" + (i + 1));
                event.addSprite(loc);
            }
        }
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Post event) {
        if (event.getMap().getBasePath().equals("textures")) {
            for (int i = 0; i < PressureGlassBakedModel.TEXTURE_COUNT; i++) {
                ResourceLocation loc = new ResourceLocation(Textures.PRESSURE_GLASS_LOCATION + "window_" + (i + 1));
                PressureGlassBakedModel.SPRITES[i] = event.getMap().getSprite(loc);
            }
        }
    }
}
