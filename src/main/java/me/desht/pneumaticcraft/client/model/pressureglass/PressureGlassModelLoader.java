package me.desht.pneumaticcraft.client.model.pressureglass;

import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import java.util.function.Function;

public enum PressureGlassModelLoader implements ICustomModelLoader {
    INSTANCE;

    private static final PressureGlassModel MODEL = new PressureGlassModel();

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        // custom baked model for the block model, but the item model will use the normal JSON model
        return modelLocation.getNamespace().equals(Names.MOD_ID)
                && "pressure_chamber_glass".equals(modelLocation.getPath())
                && modelLocation instanceof ModelResourceLocation
                && !((ModelResourceLocation) modelLocation).getVariant().equals("inventory");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        return MODEL;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

    public static class PressureGlassModel implements IModel {
        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
            return new PressureGlassBakedModel(state, format, bakedTextureGetter);
        }
    }
}
