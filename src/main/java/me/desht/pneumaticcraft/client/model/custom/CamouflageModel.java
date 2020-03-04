package me.desht.pneumaticcraft.client.model.custom;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class CamouflageModel implements IDynamicBakedModel {
    private final IBakedModel originalModel;

    private CamouflageModel(IBakedModel originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data) {
        if (state == null || !(state.getBlock() instanceof BlockPneumaticCraftCamo)) {
            return originalModel.getQuads(state, side, rand, data);
        }

        BlockState camoState = data.getData(BlockPneumaticCraftCamo.CAMO_STATE);
        IEnviromentBlockReader blockAccess = data.getData(BlockPneumaticCraftCamo.BLOCK_ACCESS);
        BlockPos pos = data.getData(BlockPneumaticCraftCamo.BLOCK_POS);
        if (blockAccess == null || pos == null) {
            return originalModel.getQuads(state, side, rand, data);
        }

        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer == null) {
            layer = BlockRenderLayer.SOLID; // workaround for when this isn't set (digging, etc.)
        }
        if (camoState == null && layer == BlockRenderLayer.SOLID) {
            // No camo
            return originalModel.getQuads(state, side, rand, data);
        } else if (camoState != null && camoState.getBlock().canRenderInLayer(camoState, layer)) {
            // Steal camo's model
            IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(camoState);

            // Their model can be smart too
            return model.getQuads(camoState, side, rand, data);
        }

        return ImmutableList.of(); // Nothing renders
    }


    @Override
    public boolean isAmbientOcclusion() {
        return originalModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return originalModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return originalModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return originalModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return originalModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return originalModel.getOverrides();
    }

    public enum Loader implements IModelLoader<Geometry> {
        INSTANCE;

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
        }

        @Override
        public Geometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            BlockModel baseModel = deserializationContext.deserialize(JSONUtils.getJsonObject(modelContents, "base_model"), BlockModel.class);
            return new CamouflageModel.Geometry(baseModel);
        }
    }

    private static class Geometry implements IModelGeometry<Geometry> {
        private final BlockModel baseModel;

        Geometry(BlockModel baseModel) {
            this.baseModel = baseModel;
        }

        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format, ItemOverrideList overrides) {
            return new CamouflageModel(baseModel.bake(bakery, spriteGetter, sprite, format));
        }

        @Override
        public Collection<ResourceLocation> getTextureDependencies(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors) {
            return baseModel.getTextures(modelGetter, missingTextureErrors);
        }
    }
}
