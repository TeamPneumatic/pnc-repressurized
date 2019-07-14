package me.desht.pneumaticcraft.client.model.pressureglass;

import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;

import java.util.Collection;
import java.util.Set;
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
    public IUnbakedModel loadModel(ResourceLocation modelLocation) {
        return MODEL;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

    public static class PressureGlassModel implements IUnbakedModel {
        @Override
        public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format) {
            return new PressureGlassBakedModel(format);
        }

        @Override
        public Collection<ResourceLocation> getDependencies() {
            return null;
        }

        @Override
        public Collection<ResourceLocation> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors) {
            return null;
        }
    }
}
