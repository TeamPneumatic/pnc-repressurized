package me.desht.pneumaticcraft.client.model.custom;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class CamouflageModel implements IDynamicBakedModel {
    private final IBakedModel originalModel;

    private CamouflageModel(IBakedModel originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData modelData) {
        if (state == null || !(state.getBlock() instanceof BlockPneumaticCraftCamo)) {
            return originalModel.getQuads(state, side, rand, modelData);
        }
        BlockState camoState = modelData.getData(BlockPneumaticCraftCamo.CAMO_STATE);

        RenderType layer = MinecraftForgeClient.getRenderLayer();
        if (layer == null) {
            layer = RenderType.getSolid(); // workaround for when this isn't set (digging, etc.)
        }
        if (camoState == null && layer == RenderType.getSolid()) {
            // No camo
            return originalModel.getQuads(state, side, rand, modelData);
        } else if (camoState != null && RenderTypeLookup.canRenderInLayer(camoState, layer)) {
            // Steal camo's model
            IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(camoState);
            return model.getQuads(camoState, side, rand, modelData);
        } else {
            // Not rendering in this layer
            return Collections.emptyList();
        }
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
    public boolean isSideLit() {
        return false;
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
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            return new CamouflageModel(baseModel.bakeModel(bakery, baseModel, spriteGetter, modelTransform, modelLocation, true));
        }

        @Override
        public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return baseModel.getTextures(modelGetter, missingTextureErrors);
        }
    }
}
