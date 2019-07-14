package me.desht.pneumaticcraft.client.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.apache.commons.lang3.tuple.Pair.of;

public class BakedMinigunWrapper implements IDynamicBakedModel {
    private static final EmptyMinigunModel EMPTY_MODEL = new EmptyMinigunModel();

    private final IBakedModel original;

    public BakedMinigunWrapper(IBakedModel original) {
        this.original = original;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data) {
        return original.getQuads(state, side, rand, data);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return original.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return original.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return original.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return original.getOverrides();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        switch (cameraTransformType) {
            case GROUND:
            case HEAD:
            case NONE:
            case GUI:
            case FIXED:
                return original.handlePerspective(cameraTransformType);
        }
        return of(EMPTY_MODEL, null);
    }

    public static class EmptyMinigunModel implements IBakedModel {

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
            return Collections.emptyList();
        }

        @Override
        public boolean isAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return false;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return true;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return null;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return null;
        }
    }
}
